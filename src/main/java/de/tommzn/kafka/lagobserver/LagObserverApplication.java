package de.tommzn.kafka.lagobserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LagObserverApplication {

	public static void main(String[] args) {
		SpringApplication.run(LagObserverApplication.class, args);
	}

}
