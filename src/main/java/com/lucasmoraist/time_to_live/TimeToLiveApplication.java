package com.lucasmoraist.time_to_live;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class TimeToLiveApplication {

	public static void main(String[] args) {
		SpringApplication.run(TimeToLiveApplication.class, args);
	}

}
