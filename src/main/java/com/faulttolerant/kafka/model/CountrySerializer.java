package com.faulttolerant.kafka.model;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.faulttolerant.kafka.config.ObjectMapperFactory;

import lombok.NoArgsConstructor;

@Component
@NoArgsConstructor
public class CountrySerializer implements Serializer<Country> {

	ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapperInstance();

	@Override
	public byte[] serialize(String topic, Country data) {
		try {
			if (data == null) {
				return null;
			}
			return objectMapper.writeValueAsBytes(data);
		} catch (SerializationException | JsonProcessingException e) {
			throw new SerializationException("Error serializing country", e);
		}
	}
}