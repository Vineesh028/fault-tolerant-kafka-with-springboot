package com.faulttolerant.kafka.listener;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.modelmapper.ModelMapper;
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
import com.faulttolerant.kafka.entity.CountryEntity;
import com.faulttolerant.kafka.model.Country;
import com.faulttolerant.kafka.repository.CountryRepository;
import com.faulttolerant.kafka.util.Constants;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MessageConsumer {
	
	@Value("${app.dlq.topic}")
	private String dlqTopic;
	
	@Autowired
	private KafkaTemplate<String, Country> kafkaTemplate;

	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	private CountryRepository countryRepository;
	
	ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapperInstance();

	@KafkaListener(id = "testKafkaListener", topics = "${app.message.topic}")
	public void consume(ConsumerRecord<String, Country> consumerRecord, Acknowledgment acknowledgment)
			throws IllegalStateException, UnsupportedEncodingException, JsonMappingException, JsonProcessingException {

		String json = String.valueOf(consumerRecord.value());
		Country country = objectMapper.readValue(json, Country.class);
		try {

			log.info("Consuming message {}", country);
			countryRepository.save(modelMapper.map(country, CountryEntity.class));

		} catch (Exception e) {
			log.info("Message consumption failed for message {}", country);
			String originalTopic = consumerRecord.topic();
			ProducerRecord<String, Country> record = new ProducerRecord<>(dlqTopic, country);
			record.headers().add(Constants.ORIGINAL_TOPIC_HEADER_KEY, originalTopic.getBytes(StandardCharsets.UTF_8));
			Header retryCount = consumerRecord.headers().lastHeader(Constants.RETRY_COUNT_HEADER_KEY);
			if (retryCount != null) {
				record.headers().add(retryCount);
			}
			kafkaTemplate.send(record);
		} finally {
			acknowledgment.acknowledge();
		}
	}
}