package com.faulttolerant.kafka.model;


import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Country implements Serializable{

	private static final long serialVersionUID = 526877922896529210L;

	private String name;
	
	private String country;
	
	private String latitude;
	
	private String longitude;


}