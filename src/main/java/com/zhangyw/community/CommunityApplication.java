package com.zhangyw.community;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
//@SpringBootApplication(exclude= {DataSourceAutoConfiguration.class})
public class CommunityApplication {

	@PostConstruct
	public void init() {
		// ElasticSearch and Redis will boot Netty both, while ElasticSearch checks if other applications are using Netty.
		// To solve this conflict, set the system property according to Netty4Utils.setAvailableProcessors()
		System.setProperty("es.set.netty.runtime.available.processors", "false");
	}

	public static void main(String[] args) {
		SpringApplication.run(CommunityApplication.class, args);
	}

}
