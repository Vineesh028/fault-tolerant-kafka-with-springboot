package com.faulttolerant.kafka.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.faulttolerant.kafka.service.SenderService;

@RestController
@RequestMapping("/message")
public class MessageController {
	
	@Autowired
	SenderService senderService;
 
    @PostMapping()
    public ResponseEntity<String> invokeTask() throws Exception {
    	
    	String response = senderService.sendData();
    	
        return new ResponseEntity<String>(response, HttpStatus.OK);
    }
    

}
