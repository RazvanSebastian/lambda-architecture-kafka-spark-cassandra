package com.sparkjobs;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.Optional;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;
import org.codehaus.jackson.map.ObjectMapper;

import com.datastax.spark.connector.japi.CassandraJavaUtil;
import com.sparkjobs.model.Activity;
import com.sparkjobs.model.ActivityByProduct;
import com.sparkjobs.utils.LogProducer;
import com.sparkjobs.utils.Settings;
import com.sparkjobs.utils.SparkJobsUtils;

import kafka.common.TopicAndPartition;
import kafka.consumer.SimpleConsumer;
import kafka.message.MessageAndMetadata;
import kafka.serializer.StringDecoder;
import scala.Tuple2;

public class StreamJob {

	@SuppressWarnings("serial")
	public static void main(String[] args) throws Exception {
		
		File file = new File(Settings.getInstance().inputFilePath);
		if (!file.exists()) {
			System.out.println("Generate data.csv file");
			LogProducer.load();
		}

		// Spark init
		JavaStreamingContext ssc = SparkJobsUtils.getJavaStreamingContext();
		// A checkpoint must be provivided to store at some interval of time the result
		// if the system fails
		ssc.checkpoint("src/main/resources/checkpoint");

		// Read data from broker cluster
		JavaInputDStream<String> input = KafkaUtils.createDirectStream(ssc, String.class, // keyClass
				String.class, // Class of the values in the Kafka records
				StringDecoder.class, // Class of the key decoder
				StringDecoder.class, // Class of the value decoder
				String.class, // Class of the records in DStream
				initKafkaParams(), initTopic(), new Function<MessageAndMetadata<String, String>, String>() {

					@Override
					public String call(MessageAndMetadata<String, String> messageAndMetadata) throws Exception {
						// Only the message content
						return messageAndMetadata.message();
					}
				});

		// Map to Activity
		JavaDStream<Activity> activitiesDStream = input.map(line -> {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(line, Activity.class);
		});

		// JavaPairDStream <PorductName , ActivityByProduct>
		JavaPairDStream<String, ActivityByProduct> dStream = activitiesDStream
				.transformToPair(new Function<JavaRDD<Activity>, JavaPairRDD<String, ActivityByProduct>>() {

					@Override
					public JavaPairRDD<String, ActivityByProduct> call(JavaRDD<Activity> t) throws Exception {
						// Get SQLContext from JavaRDD t object
						SQLContext sqlContext = SQLContext.getOrCreate(t.rdd().context());

						// Parse to JavaRDD to dataset to be able to apply sql method
						Dataset<Activity> dataset = sqlContext.createDataset(t.rdd(), Encoders.bean(Activity.class));
						dataset.createOrReplaceTempView("activity");

						// Execute sql statement on dataset
						return dataset.sqlContext().sql("SELECT  " + "product, timestamp,"
								+ "SUM( CASE WHEN action = 'page_view' THEN 1 ELSE 0 END) AS pageviewcount, "
								+ "SUM( CASE WHEN action = 'add_to_cart' THEN 1 ELSE 0 END) AS addtocartcount, "
								+ "SUM( CASE WHEN action = 'purchase' THEN 1 ELSE 0 END) AS purchasecount "
								+ "FROM activity " + "GROUP BY product, timestamp")
								// Come back to JavaRDD to perfrom mapToPair
								.toJavaRDD()
								// Map to pair by <Product,ActivityByProduct>
								.mapToPair(new PairFunction<Row, String, ActivityByProduct>() {

									@Override
									public Tuple2<String, ActivityByProduct> call(Row row) throws Exception {
										return new Tuple2<String, ActivityByProduct>(
												// Get product name as String from first column of row
												row.getString(0),
												// Parse entire row to ActivityByProduct object
												new ActivityByProduct(row.getString(0), row.getLong(1), row.getLong(2),
														row.getLong(3), row.getLong(4)));
									}
								});
					}
				});

		// Update the stream at every new data rading from broker cluster
		JavaDStream<ActivityByProduct> finalDStream = dStream.updateStateByKey(
				new Function2<List<ActivityByProduct>, Optional<ActivityByProduct>, Optional<ActivityByProduct>>() {

					@Override
					public Optional<ActivityByProduct> call(List<ActivityByProduct> newItemsPerKey,
							Optional<ActivityByProduct> currentState) throws Exception {
						long[] newStateCounts = new long[3];

						if (currentState.isPresent()) {
							// Initialize currentState
							newStateCounts[0] = currentState.get().getPageviewcount();
							newStateCounts[1] = currentState.get().getAddtocartcount();
							newStateCounts[2] = currentState.get().getPurchasecount();
						} else {
							// Create new currentState with the first item
							if (!newItemsPerKey.isEmpty()) {
								currentState = Optional.of(newItemsPerKey.get(0));
							}
							newStateCounts[0] = 0;
							newStateCounts[1] = 0;
							newStateCounts[2] = 0;
						}

						// Update
						newItemsPerKey.forEach(a -> {
							newStateCounts[0] += a.getPageviewcount();
							newStateCounts[1] += a.getAddtocartcount();
							newStateCounts[2] += a.getPurchasecount();
						});

						// Update the current state with final values
						currentState.get().setPageviewcount(newStateCounts[0]);
						currentState.get().setAddtocartcount(newStateCounts[1]);
						currentState.get().setPurchasecount(newStateCounts[2]);
						return currentState;
					}
				})
				// Map to ActivityByProduct DStream
				.map(tuple -> {
					return tuple._2;
				});

		finalDStream.foreachRDD(new VoidFunction<JavaRDD<ActivityByProduct>>() {

			@Override
			public void call(JavaRDD<ActivityByProduct> t) throws Exception {
					
				CassandraJavaUtil.javaFunctions(t.rdd())
				.writerBuilder("lambda", "stream_activity_by_product", CassandraJavaUtil.mapToRow(ActivityByProduct.class)).saveToCassandra();
			}
		});
		finalDStream.print(100);
		ssc.start();
		ssc.awaitTermination();
	}

	/**
	 * Init Kafka broker list according to documentation: Kafka configuration
	 * parameters. Requires "metadata.broker.list" or "bootstrap.servers" to be set
	 * with Kafka broker(s) (NOT zookeeper servers), specified in
	 * host1:port1,host2:port2 form.
	 * 
	 * @see https://spark.apache.org/docs/2.2.0/api/java/index.html
	 * 
	 * @return kafkaParams
	 */
	private static Map<String, String> initKafkaParams() {
		Map<String, String> kafkaParams = new HashMap<>();
		kafkaParams.put("bootstrap.servers", "localhost:9092,localhost:9093,localhost:9094");
		return kafkaParams;
	}

	/**
	 * Per-topic/partition Kafka offsets defining the (inclusive) starting point of
	 * the stream Topic <=> Map<K,V> where ( K =
	 * TopicAndpartition(topic-name,partition) , V = offset )
	 * 
	 * @see https://spark.apache.org/docs/2.2.0/api/java/index.html
	 * 
	 * @return
	 */
	private static Map<TopicAndPartition, Long> initTopic() {
		Map<TopicAndPartition, Long> topics = new HashMap<>();
		topics.put(new TopicAndPartition("click-stream-topic", 0), new Long(0));
		return topics;
	}

}