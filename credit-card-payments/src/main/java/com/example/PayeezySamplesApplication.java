package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class PayeezySamplesApplication {


    public static void main(String[] args) {
        SpringApplication.run(PayeezySamplesApplication.class, args);
        
        TransactionCaller tc = new TransactionCaller();
        tc.makeTransaction();
    }
}
