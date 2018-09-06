package com.sparkjobs.utils;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaStreamingContext;

public final class SparkJobsUtils {

	private static SparkConf sparkConf;
	private static JavaStreamingContext javaStreamingContext;
	private static SparkContext sparkContext;
	private static SparkSession sparkSession;


	static {
		sparkConf = new SparkConf()
				.setAppName("JavaKafkaWordCount")
				.setMaster("local")
				.set("spark.executor.memory", "1g")
				.set("spark.cassandra.connection.host", "127.0.0.1")
				.set("spark.cassandra.connection.port", "9040")
				.set("spark.cassandra.auth.username", "cassandra")
				.set("spark.cassandra.auth.password", "cassandra");
		javaStreamingContext = new JavaStreamingContext(sparkConf, new Duration(30000));
		sparkContext = SparkContext.getOrCreate(sparkConf);
		sparkSession = SparkSession.builder().config(sparkConf).getOrCreate();
	}

	public static final JavaStreamingContext getJavaStreamingContext() {
		return javaStreamingContext;
	}

	public static final SparkSession getSparkSesion() {
		return sparkSession;
	}

	public static final SparkContext getSparkContext() {
		return sparkContext;
	}

}
