package com.kafka;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import com.kafka.config.IKafkaConstants;
import com.kafka.model.Activity;
import com.kafka.producer.ProducerCreator;

public class KafkaMain {

	public static void main(String[] args) {
		List<Activity> list = importData();
		if (list.isEmpty())
			System.out.println("Empty list!");
		else
			runProducer(list);
		// runConsumer();
	}

//	static void runConsumer() {
//		KafkaConsumer<Long, ActivityByProducer> consumer = ConsumerCreator.createConsumer();
//		ActivityByProducerSerializer serializer = new ActivityByProducerSerializer();
//		int noMessageFound = 0;
//		while (true) {
//			ConsumerRecords<Long, ActivityByProducer> consumerRecords = consumer.poll(1000);
//			// 1000 is the time in milliseconds consumer will wait if no record is found at
//			// broker.
//			if (consumerRecords.count() == 0) {
//				noMessageFound++;
//				if (noMessageFound > IKafkaConstants.MAX_NO_MESSAGE_FOUND_COUNT)
//					// If no message found count is reached to threshold exit loop.
//					break;
//				else
//					continue;
//			}
//			// print each record.
//			consumerRecords.forEach(new Consumer<ConsumerRecord<Long, ActivityByProducer>>() {
//				public void accept(ConsumerRecord<Long, ActivityByProducer> record) {
//					System.out.println("Record Key " + record.key());
//					System.out.println("Record value " + record.value().toString());
//					System.out.println("Record partition " + record.partition());
//					System.out.println("Record offset " + record.offset());
//				}
//			});
//			// commits the offset of record to broker.
//			consumer.commitAsync();
//		}
//		consumer.close();
//		serializer.close();
//	}

	static void runProducer(List<Activity> activites) {
		Producer<Long, Activity> producer = ProducerCreator.createProducer();
		Random random = new Random();

		while (true) {
			// Send one record from random index
			int index = random.nextInt(196);
			ProducerRecord<Long, Activity> record = new ProducerRecord<Long, Activity>(IKafkaConstants.TOPIC_NAME,
					activites.get(index));
			try {
				RecordMetadata metadata = producer.send(record).get();
				System.out.println("Record sent with key " + index + " to partition " + metadata.partition()
						+ " with offset " + metadata.offset());
			} catch (ExecutionException e) {
				System.out.println("Error in sending record");
				System.out.println(e);
			} catch (InterruptedException e) {
				System.out.println("Error in sending record");
				System.out.println(e);
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private static List<Activity> importData() {
		List<Activity> list = new ArrayList<Activity>();
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new FileReader(new File("src/main/resources/data.csv")));
			String line = "";
			while ((line = bufferedReader.readLine()) != null) {
				String[] lineArray = line.split(",");
				if (lineArray.length == 8) {
					Activity activity = new Activity(Long.parseLong(lineArray[0]), lineArray[1], lineArray[2],
							lineArray[3], lineArray[4], lineArray[5], lineArray[6], lineArray[7]);
					list.add(activity);
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (bufferedReader != null)
				try {
					bufferedReader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return list;
	}
}
