package com.almubaraksuleiman.cbts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;

/**
 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2025
 **/

@SpringBootApplication
@EnableScheduling
//@EnableWebSocketMessageBroker
public class CbtEnterpriseApplication {

	public static void main(String[] args) {
		SpringApplication.run(CbtEnterpriseApplication.class, args);
	}

}
