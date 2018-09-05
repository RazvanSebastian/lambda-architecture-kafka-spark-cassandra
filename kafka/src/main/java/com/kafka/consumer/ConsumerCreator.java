package com.kafka.consumer;

import java.util.Collections;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.LongDeserializer;

import com.kafka.config.IKafkaConstants;
import com.kafka.model.Activity;
import com.kafka.model.ActivitySerializer;


public class ConsumerCreator {
	public static KafkaConsumer<Long, Activity> createConsumer() {
		Properties props = new Properties();
		
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, IKafkaConstants.KAFKA_BROKERS);

		/*
		 * GROUP_ID_CONFIG: The consumer group id used to identify to which group this
		 * consumer belongs.
		 */
		props.put(ConsumerConfig.GROUP_ID_CONFIG, IKafkaConstants.GROUP_ID_CONFIG);

		/*
		 * KEY_DESERIALIZER_CLASS_CONFIG: The class name to deserialize the key object.
		 * We have used Long as the key so we will be using LongDeserializer as the
		 * deserializer class. You can create your custom deserializer by implementing
		 * the Deserializer interface provided by Kafka.
		 */
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());

		/*
		 * VALUE_DESERIALIZER_CLASS_CONFIG: The class name to deserialize the value
		 * object. We have used String as the value so we will be using
		 * StringDeserializer as the deserializer class. You can create your custom
		 * deserializer
		 */
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ActivitySerializer.class.getName());

		/*
		 * MAX_POLL_RECORDS_CONFIG: The max count of records that the consumer will
		 * fetch in one iteration.
		 */
		props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, IKafkaConstants.MAX_POLL_RECORDS);

		/*
		 * ENABLE_AUTO_COMMIT_CONFIG: When the consumer from a group receives a message
		 * it must commit the offset of that record. If this configuration is set to be
		 * true then, periodically, offsets will be committed, but, for the production
		 * level, this should be false and an offset should be committed manually.
		 */
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

		/*
		 * AUTO_OFFSET_RESET_CONFIG: For each consumer group, the last committed offset
		 * value is stored. This configuration comes handy if no offset is committed for
		 * that group, i.e. it is the new group created.
		 * 
		 * 1. earliest will cause the consumer to fetch records from the beginning of offset i.e from zero.
		 * 2. latest will cause the consumer to fetch records from the new records. By new records mean those created after the consumer group became active.
		 */
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, IKafkaConstants.OFFSET_RESET_EARLIER);
		
		KafkaConsumer<Long, Activity> consumer = new KafkaConsumer<Long, Activity>(props);
		consumer.subscribe(Collections.singletonList(IKafkaConstants.TOPIC_NAME));
		return consumer;
	}
}
