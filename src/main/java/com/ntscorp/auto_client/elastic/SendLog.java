package com.ntscorp.auto_client.elastic;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;

public class SendLog extends SimpleBeanPropertyFilter{
	private static final ObjectMapper OM = new ObjectMapper();

	private static final String CONTENT_TYPE = "Content-Type";
	private static final String CONTENT_TYPE_VALUE = "application/json";

	public static String ES_TARGET_URL;
	public static String ELASTC_BASE_URL = "http://10.113.101.101:9200/";
	public static String ES_SUITE_URL = ELASTC_BASE_URL + "sqa_auto_suite/_doc";
	public static String ES_CLASS_URL = ELASTC_BASE_URL + "sqa_auto_class/_doc";
	public static String ES_METHOD_URL = ELASTC_BASE_URL + "sqa_auto_method/_doc";
	//public static String ES_METHOD_URL = ELASTC_URL + "temp_auto_method/doc";
	
	public static void sendStatusLog(String url, final StatusLog log) {
		ES_TARGET_URL = url;
		OM.setFilterProvider(new SimpleFilterProvider().addFilter("depthFilter", new SendLog()));

		try {
			HttpResponse<JsonNode> response;
			response = Unirest.post(url)
				.header(CONTENT_TYPE, CONTENT_TYPE_VALUE)
				.body(OM.writeValueAsString(log))
				.asJson();
			
			int responseCode = response.getStatus();
			if (responseCode != 201) {
				// todo : 상태 코드 확인
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// todo 바꿔야 한다.! status code 201..!!
	public static boolean isElasticAlive() {
		try {
			URL url = new URL(ELASTC_BASE_URL);
			HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
			httpConn.setInstanceFollowRedirects(false);
			httpConn.setRequestMethod("HEAD");
			httpConn.connect();
			return true;
		} catch (IOException e) {
			System.out.println("SQA Elastic : Down ");
			return false;
		}
	}
	
	@Override
	public void serializeAsField(Object pojo, JsonGenerator jgen, SerializerProvider provider, PropertyWriter writer) throws Exception {
		if (pojo instanceof StatusLog) {
			if (ES_TARGET_URL.contains("_suite")) {
				String fieldName = writer.getName();
				if (fieldName.equals("result.class.passed") ||
						fieldName.equals("result.class.failed") ||
						fieldName.equals("result.class.skipped") ||
						fieldName.equals("result.class.rate") ||
						
						fieldName.equals("result")) {
					// do not serialize
				} else {
					super.serializeAsField(pojo, jgen, provider, writer);
				}
			}
			
			else if (ES_TARGET_URL.contains("_class")) {
				String fieldName = writer.getName();
				if (fieldName.equals("result.suite.passed") ||
						fieldName.equals("result.suite.failed") ||
						fieldName.equals("result.suite.skipped") ||
						fieldName.equals("result.suite.rate") ||
						
						fieldName.equals("result")) {
					// do not serialize
				} else {
					super.serializeAsField(pojo, jgen, provider, writer);
				}
			}
			
			else if (ES_TARGET_URL.contains("_method")) {
				String fieldName = writer.getName();
				if (fieldName.equals("result.class.passed") ||
						fieldName.equals("result.class.failed") ||
						fieldName.equals("result.class.skipped") ||
						fieldName.equals("result.class.rate") ||
						
						fieldName.equals("result.suite.passed") ||
						fieldName.equals("result.suite.failed") ||
						fieldName.equals("result.suite.skipped") ||
						fieldName.equals("result.suite.rate")) {
					// do not serialize
				} else {
					super.serializeAsField(pojo, jgen, provider, writer);
				}
			}
		} else {
			super.serializeAsField(pojo, jgen, provider, writer);
		}
	}
}