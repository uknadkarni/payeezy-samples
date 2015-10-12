package io.pivotal.payeezy;

import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource("classpath:application.properties")
public class PayeezyGenericsApplication {
	
	private static Logger logger = Logger.getLogger(PayeezyRequest.class);

	public static void main(String[] args) {
		SpringApplication.run(PayeezyGenericsApplication.class, args);
		PayeezyRequest payeezyRequest = new PayeezyRequest();
        String response = payeezyRequest.post();
        logger.info("Response: " + response);
	}

}
