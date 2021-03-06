package com.weixin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootApplication
@EnableScheduling
//public class QunApplication extends SpringBootServletInitializer {
public class QunApplication  {
	public static void main(String[] args) {
		System.setProperty("jsse.enableSNIExtension", "false");
		SpringApplication.run(QunApplication.class, args);
	}

	@Bean
	public ExecutorService executorService(){
		return Executors.newCachedThreadPool();
	}


//	@Override
//	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
//		System.setProperty("jsse.enableSNIExtension", "false");
//		return application.sources(QunApplication.class);
//	}

}
