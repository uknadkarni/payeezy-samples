package io.pivotal.payeezy;

import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class CreditCardPaymentsApplication {
	
	private static Logger logger = Logger.getLogger(PayeezyRequest.class);

	private static String getPayload() {
		String creditCardPayload = "{\"type\":\"visa\",\"cardholder_name\":\"John Smith\",\"card_number\":\"4788250000028291\","
				+ "\"exp_date\":1020,\"cvv\":\"123\"}";

		String payload = "{\"merchant_ref\":\"Astonishing-Sale\",\"transaction_type\":\"authorize\","
				+ "\"method\":\"credit_card\",\"amount\":1299,\"currency_code\":\"USD\",\"credit_card\":"
				+ creditCardPayload + "}";

		return payload;
	}
	
    public static void main(String[] args) {
        SpringApplication.run(CreditCardPaymentsApplication.class, args);
        String payload = getPayload();
        PayeezyRequest request = new CreditCardTransactionRequest(payload);
        String response = request.post();
        logger.info("Response: " + response);
    }
}
