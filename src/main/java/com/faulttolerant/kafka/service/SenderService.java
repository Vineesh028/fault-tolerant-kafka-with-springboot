package com.faulttolerant.kafka.service;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.faulttolerant.kafka.model.Country;
import com.faulttolerant.kafka.util.Constants;

@Service
public class SenderService {
	
	@Value("${app.message.topic}")
	private String messageTopic;
	
	@Autowired
	private KafkaTemplate<String, Country> kafkaTemplate;

	public String sendData() {
		
		File file;
		try {
			file = new ClassPathResource(Constants.CSV_FILE).getFile();
			 MappingIterator<Country> countryIter = new CsvMapper()
					 .readerWithTypedSchemaFor(Country.class)
					 .readValues(file);
			List<Country> countries = countryIter.readAll();
			countries.forEach( e->{
				ProducerRecord<String, Country> record = new ProducerRecord<>(messageTopic, e);
				kafkaTemplate.send(record);
			
			});
			
		
		} catch (IOException e) {
			e.printStackTrace();
		}

		return "Messages sent";
				
	}

}
