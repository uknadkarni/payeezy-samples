package com.example;

import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class TransactionCaller {

	private static Logger logger = Logger.getLogger(TransactionCaller.class);


	private final static String NONCE = "nonce";
	private final static String APIKEY = "apikey";
	private final static String APISECRET = "pzsecret";
	private final static String TOKEN = "token";
	private final static String TIMESTAMP = "timestamp";
	private final static String AUTHORIZE = "Authorization";
	private final static String PAYLOAD = "payload";
	private final static String MERCHANTID = "merchantid";
	private final static String TRTOKEN = "trtoken";
	private final static String TA_TOKEN = "ta_token";
	private final static String transactionURL = "https://api-cert.payeezy.com/v1/transactions";

	private final static String ApiSecretkey = "20053246b3aeaa1817026276884d8645c14b2d1eac499f060f586bcb20666889";

	private final static String MerchantToken = "fdoa-a480ce8951daa73262734cf102641994c1e55e7cdf4c02b6";

	private final static String apikey = "AMhPuRSAc9KcwE5HFg8yL9LooCqKAeAG";

	private final static String headerContentType = "application/json";

	@Autowired
	RestTemplate restTemplate;

	private static String getPayload() {
		String creditCardPayload = "{\"type\":\"visa\",\"cardholder_name\":\"John Smith\",\"card_number\":\"4788250000028291\","
				+ "\"exp_date\":1020,\"cvv\":\"123\"}";

		String payload = "{\"merchant_ref\":\"Astonishing-Sale\",\"transaction_type\":\"authorize\","
				+ "\"method\":\"credit_card\",\"amount\":1299,\"currency_code\":\"USD\",\"credit_card\":"
				+ creditCardPayload + "}";

		return payload;
	}

	private static HttpHeaders getHttpHeader(String payload) throws Exception {
		Map<String, String> encriptedKey = getSecurityKeys(payload);
		HttpHeaders header = new HttpHeaders();
		Iterator<String> iter = encriptedKey.keySet().iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			if (PAYLOAD.equals(key))
				continue;
			header.add(key, encriptedKey.get(key));
		}

		header.add("Accept", headerContentType);

		header.setContentType(MediaType.APPLICATION_JSON);

		List<MediaType> mediatypes = new ArrayList<MediaType>();
		mediatypes.add(MediaType.APPLICATION_JSON);

		mediatypes.add(new MediaType("application", "json", Charset
				.forName("UTF-8")));

		header.add("User-Agent", "Java/1.6.0_26");

		return header;
	}

    
    private static byte[] toHex(byte[] arr) {
        String hex=Hex.encodeHexString(arr);
        logger.info("Apache common value:{}" + hex);
        return hex.getBytes();
    }
    
	public static String getMacValue(Map<String, String> data) throws Exception {
		Mac mac = Mac.getInstance("HmacSHA256");
		String apiSecret = data.get(APISECRET);
		logger.debug("API_SECRET:{}" + apiSecret);
		SecretKeySpec secret_key = new SecretKeySpec(apiSecret.getBytes(),
				"HmacSHA256");
		mac.init(secret_key);
		StringBuilder buff = new StringBuilder();
		buff.append(data.get(APIKEY)).append(data.get(NONCE))
				.append(data.get(TIMESTAMP));
		if (data.get(TOKEN) != null)
			buff.append(data.get(TOKEN));
		if (data.get(PAYLOAD) != null)
			buff.append(data.get(PAYLOAD));

		logger.info(buff.toString());
		byte[] macHash = mac.doFinal(buff.toString().getBytes("UTF-8"));
		logger.info("MacHAsh:{}" + Arrays.toString(macHash));

		// Check this
		// String authorizeString = Base64Utils.encodeToString(toHex(macHash));
		String authorizeString = Base64.encodeBase64String(toHex(macHash));
		logger.info("Authorize: {}" + authorizeString);
		return authorizeString;

	}

	private static Map<String, String> getSecurityKeys(String payload)
			throws Exception {
		Map<String, String> returnMap = new HashMap<String, String>();
		long nonce;
		try {

			nonce = Math.abs(SecureRandom.getInstance("SHA1PRNG").nextLong());
			logger.debug("SecureRandom nonce:{}" + nonce);
			returnMap.put(NONCE, Long.toString(nonce));
			returnMap.put(APIKEY, apikey);
			returnMap.put(TIMESTAMP, Long.toString(System.currentTimeMillis()));
			returnMap.put(TOKEN, MerchantToken);
			returnMap.put(APISECRET, ApiSecretkey);
			returnMap.put(PAYLOAD, payload);
			returnMap.put(AUTHORIZE, getMacValue(returnMap));

		} catch (NoSuchAlgorithmException e) {
			logger.error(e.getMessage());
			throw new RuntimeException(e.getMessage(), e);
		}
		return returnMap;
	}

	private static String post(String payload) {
		HttpEntity<String> request = null;
		try {
			request = new HttpEntity<String>(payload, getHttpHeader(payload));
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("Headers: " + request.getHeaders().toString());
		System.out.println("Body: " + request.getBody().toString());
		String url = new String(transactionURL);
		RestTemplate restTemplate = new RestTemplate();

		try {
			ResponseEntity<TransactionResponse> response = restTemplate
					.exchange(url, HttpMethod.POST, request,
							TransactionResponse.class);
			return response.toString();
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	public void makeTransaction() {
		logger.info(post(getPayload()));
	}

}
