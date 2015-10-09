package io.pivotal.payeezy;

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

import io.pivotal.payeezy.TransactionResponse;

public class PayeezyRequest {

	private String payload;

	private static Logger logger = Logger.getLogger(PayeezyRequest.class);

	private final static String transactionURL = "https://api-cert.payeezy.com/v1/transactions";

	private final static String headerContentType = "application/json";

	@Autowired
	RestTemplate restTemplate;

	protected PayeezyRequest(String payload) {
		this.payload = payload;
	}

	private HttpHeaders getHttpHeader() throws Exception {
		Map<String, String> encriptedKey = getSecurityKeys();
		HttpHeaders header = new HttpHeaders();
		Iterator<String> iter = encriptedKey.keySet().iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			if (HeaderField.PAYLOAD.equals(key))
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
		String hex = Hex.encodeHexString(arr);
		logger.info("Apache common value:{}" + hex);
		return hex.getBytes();
	}

	public static String getMacValue(Map<String, String> data) throws Exception {
		Mac mac = Mac.getInstance("HmacSHA256");
		String apiSecret = data.get(HeaderField.APISECRET);
		logger.debug("API_SECRET:{}" + apiSecret);
		SecretKeySpec secret_key = new SecretKeySpec(apiSecret.getBytes(),
				"HmacSHA256");
		mac.init(secret_key);
		StringBuilder buff = new StringBuilder();
		buff.append(data.get(HeaderField.APIKEY))
				.append(data.get(HeaderField.NONCE))
				.append(data.get(HeaderField.TIMESTAMP));
		if (data.get(HeaderField.TOKEN) != null)
			buff.append(data.get(HeaderField.TOKEN));
		if (data.get(HeaderField.PAYLOAD) != null)
			buff.append(data.get(HeaderField.PAYLOAD));

		logger.info(buff.toString());
		byte[] macHash = mac.doFinal(buff.toString().getBytes("UTF-8"));
		logger.info("MacHAsh:{}" + Arrays.toString(macHash));

		String authorizeString = Base64.encodeBase64String(toHex(macHash));
		logger.info("Authorize: {}" + authorizeString);
		return authorizeString;

	}

	private Map<String, String> getSecurityKeys() throws Exception {
		Map<String, String> returnMap = new HashMap<String, String>();
		long nonce;
		try {

			nonce = Math.abs(SecureRandom.getInstance("SHA1PRNG").nextLong());
			logger.debug("SecureRandom nonce:{}" + nonce);
			returnMap.put(HeaderField.NONCE, Long.toString(nonce));
			returnMap.put(HeaderField.APIKEY, Credentials.API_KEY);
			returnMap.put(HeaderField.TIMESTAMP,
					Long.toString(System.currentTimeMillis()));
			returnMap.put(HeaderField.TOKEN, Credentials.MERCHANT_TOKEN);
			returnMap.put(HeaderField.APISECRET, Credentials.API_SECRET);
			returnMap.put(HeaderField.PAYLOAD, payload);
			returnMap.put(HeaderField.AUTHORIZE, getMacValue(returnMap));

		} catch (NoSuchAlgorithmException e) {
			logger.error(e.getMessage());
			throw new RuntimeException(e.getMessage(), e);
		}
		return returnMap;
	}

	public String post() {
		HttpEntity<String> request = null;
		try {
			request = new HttpEntity<String>(payload, getHttpHeader());
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

}
