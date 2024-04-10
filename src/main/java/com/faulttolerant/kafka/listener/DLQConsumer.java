package com.faulttolerant.kafka.listener;

import java.nio.charset.StandardCharsets;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.faulttolerant.kafka.config.ObjectMapperFactory;
import com.faulttolerant.kafka.model.Country;
import com.faulttolerant.kafka.util.Constants;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DLQConsumer {
	
	@Value("${retry.count.limit}")
	private int retryLimit;

	ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapperInstance();
	
	@Autowired
	private KafkaTemplate<String, Country> kafkaTemplate;

	@KafkaListener(id = "dlq-topic-consumer", groupId = "dlq-topic-group", topics = "${app.dlq.topic}")
	public void consume(ConsumerRecord<String, Country> consumerRecord, Acknowledgment ack) throws JsonMappingException, JsonProcessingException {
		String json = String.valueOf(consumerRecord.value());
		Country country = objectMapper.readValue(json, Country.class);
		try {
			log.info("Consuming DLQ message {}", country);
			Header originalTopicHeader = consumerRecord.headers().lastHeader(Constants.ORIGINAL_TOPIC_HEADER_KEY);
			if (originalTopicHeader != null) {
				String originalTopic = new String(originalTopicHeader.value(), StandardCharsets.UTF_8);
				Header retryCountHeader = consumerRecord.headers().lastHeader(Constants.RETRY_COUNT_HEADER_KEY);
				int retryCount = 0;
				if (retryCountHeader != null) {
					retryCount = Integer.parseInt(new String(retryCountHeader.value(), StandardCharsets.UTF_8));
				}
				if (retryCount < retryLimit) {
					retryCount += 1;
					log.info("Resending attempt {}", retryCount);
					ProducerRecord<String, Country> record = new ProducerRecord<>(originalTopic, country);
					byte[] retryCountHeaderInByte = Integer.valueOf(retryCount).toString().getBytes(StandardCharsets.UTF_8);
					record.headers().add(Constants.RETRY_COUNT_HEADER_KEY, retryCountHeaderInByte);
					Thread.sleep(5000);
					kafkaTemplate.send(record);
				} else {
					log.error("Retry limit exceeded for message {}", country);
				}
			} else {
				log.error("Unable to resend DLQ message because it's missing the originalTopic header");
			}
		}
		catch (InterruptedException e) {
			log.error("Unable to process DLQ message {}", country);
		} 
		finally {
			ack.acknowledge();
		}
	}
}