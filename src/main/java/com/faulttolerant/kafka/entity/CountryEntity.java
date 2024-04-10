package com.faulttolerant.kafka.entity;



import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "countries")
public class CountryEntity {
	
	@Id
	private String name;
	
	private String country;
	
	private String latitude;
	
	private String longitude;
	

	
}