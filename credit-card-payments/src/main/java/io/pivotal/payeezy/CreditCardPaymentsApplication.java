package io.pivotal.payeezy;

import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.google.gson.Gson;


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
        Card card = new Card("visa", "John Smith", "4788250000028291", "1020", "123");
        TransactionRequest request = new TransactionRequest();
        request.setAmount("1299");
        request.setCurrency("USD");
        request.setPaymentMethod("credit_card");
        request.setTransactionType("authorize");
        request.setReferenceNo("Astonishing-Sale");
        request.setCard(card);
        // String payload = getPayload();
        Gson gson = new Gson();
        String payload = gson.toJson(request);
        PayeezyRequest payeezyRequest = new CreditCardTransactionRequest(payload);
        String response = payeezyRequest.post();
        logger.info("Response: " + response);
    }
}
