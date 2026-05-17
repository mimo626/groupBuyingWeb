package com.example.groupbuyingweb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication //(exclude = {DataSourceAutoConfiguration.class})
public class GroupbuyingwebApplication {

	public static void main(String[] args) {
		SpringApplication.run(GroupbuyingwebApplication.class, args);
	}

}
