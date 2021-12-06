package com.ntscorp.auto_client.rest;

import java.util.List;

import com.naver.api.security.client.MACManager;
import com.naver.api.util.Type;

/**
 * ApiGateWay를 사용하는 API 호출.
 * 해당 class에서 Assertion을 수행하지 않음.
 */
public class ApiGateWayUtil {
	RestUtil rest;
	String param = "?";

	public ApiGateWayUtil(RestUtil rest) {
		this.rest = rest;
	}

	// ========================= Given =========================

	/**
	 * 단일 Parameter 값을 설정하는 메소드
	 * 
	 * @param key Parameter key 값
	 * @param value Parameter value 값
	 * @return ApiGateWayUtil
	 */
	public ApiGateWayUtil param(String key, Object value) {
		if (param.equals("?"))
			param += key + "=" + value.toString();
		else
			param += "&" + key + "=" + value.toString();

		rest.getApiResponseTime().setParamData(key, value);
		
		return this;
	}

	/**
	 * 배열 형태의 복수 Parameter 값을 설정하는 메소드
	 * 
	 * @param keys String[] 형태의 Parameter key 값
	 * @param values Object[] 형태의 Parameter value 값
	 * @return ApiGateWayUtil
	 * @throws IllegalArgumentException keys와 values의 길이가 다른 경우 
	 */
	public ApiGateWayUtil param(String[] keys, Object[] values) {
		if (keys.length != values.length)
			throw new IllegalArgumentException("keys와 values의 길이가 다릅니다.");

		for (int i = 0; i < keys.length; i++) {
			if (param.equals("?"))
				param += keys[i] + "=" + values[i].toString();
			else
				param += "&" + keys[i] + "=" + values[i].toString();
			
			rest.getApiResponseTime().setParamData(keys[i], values[i]);
		}
		return this;
	}

	/**
	 * 리스트 형태의 복수 Parameter 값을 설정하는 메소드
	 * 
	 * @param keys List 형태의 Parameter key 값
	 * @param values List 형태의 Parameter value 값
	 * @return ApiGateWayUtil
	 * @throws IllegalArgumentException keys와 values의 길이가 다른 경우 
	 */
	public ApiGateWayUtil param(List<String> keys, List<Object> values) {
		if (keys.size() != values.size())
			throw new IllegalArgumentException("keys와 values의 사이즈가 다릅니다.");

		for (int i = 0; i < keys.size(); i++) {
			if (param.equals("?"))
				param += keys.get(i) + "=" + values.get(i).toString();
			else
				param += "&" + keys.get(i) + "=" + values.get(i).toString();
			
			rest.getApiResponseTime().setParamData(keys.get(i), values.get(i));
		}

		return this;
	}

	/**
	 * 입력한 Parameter 값을 String으로 출력하는 메소드
	 * 
	 * @return value Parameter String
	 */
	private String getParam() {
		String value = param.equals("?") ? "" : param;
		param = "?";

		return value;
	}

	/**
	 * 로그인 쿠키를 설정하는 메소드.
	 * RestModel에서 네이버로 로그인 및 쿠키 값을 가져옴.
	 * 
	 * @param id 네이버 계정 아이디
	 * @param pw 네이버 계정 패스워드
	 * @return ApiGateWayUtil
	 */
	public ApiGateWayUtil cookies(String id, String pw) {
		rest.request = rest.request.cookies(rest.model.getCookies(id, pw));
		return this;
	}
	
	/**
	 * neoId 로그인 쿠키를 설정하는 메소드.
	 * RestModel에서 neoId로 로그인 및 쿠키 값을 가져옴.
	 * 
	 * @param id 네이버 계정 아이디
	 * @param pw 네이버 계정 패스워드
	 * @return ApiGateWayUtil
	 */
	public ApiGateWayUtil neoIdcookies(String id, String pw, SocialMedia social) {
		rest.request = rest.request.cookies(rest.model.getNeoIdCookies(id, pw, social));
		return this;
	}


	// ========================= When =========================

	/**
	 * Http Get Method를 호출하는 메소드
	 * 
	 * @param url 호출할 API의 url
	 * @return RestUtil
	 */
	public RestUtil get(String url) {
		rest.response = rest.request.urlEncodingEnabled(false).get(encryptionUrl(url + getParam()));
	
		rest.getApiResponseTime().setData(url, "get", rest.response.time());
		
		return rest;
	}

	/**
	 * Http Get Method를 호출하는 메소드
	 * hmacKey를 별도로 입력하여 사용
	 * 
	 * @param url 호출할 API의 url
	 * @param hmacKey hmac 인증에 사용되는 key 값
	 * @return RestUtil
	 */
	public RestUtil get(String url, String hmacKey) {
		rest.response = rest.request.urlEncodingEnabled(false).get(encryptionUrl(url + getParam(), hmacKey));

		rest.getApiResponseTime().setData(url, "get", rest.response.time());

		return rest;
	}

	/**
	 * Http Post Method를 호출하는 메소드
	 * 
	 * @param url 호출할 API의 url
	 * @return RestUtil
	 */
	public RestUtil post(String url) {
		rest.response = rest.request.urlEncodingEnabled(false).post(encryptionUrl(url + getParam()));

		rest.getApiResponseTime().setData(url, "post", rest.response.time());

		return rest;
	}

	/**
	 * Http Post Method를 호출하는 메소드
	 * hmacKey를 별도로 입력하여 사용
	 * 
	 * @param url 호출할 API의 url
	 * @param hmacKey hmac 인증에 사용되는 key 값
	 * @return RestUtil
	 */
	public RestUtil post(String url, String hmacKey) {
		rest.response = rest.request.urlEncodingEnabled(false).post(encryptionUrl(url + getParam(), hmacKey));

		rest.getApiResponseTime().setData(url, "post",  rest.response.time());

		return rest;
	}

	/**
	 * Http Delete Method를 호출하는 메소드
	 * 
	 * @param url 호출할 API의 url
	 * @return RestUtil
	 */
	public RestUtil delete(String url) {
		rest.response = rest.request.urlEncodingEnabled(false).delete(encryptionUrl(url + getParam()));

		rest.getApiResponseTime().setData(url, "delete", rest.response.time());

		return rest;
	}

	/**
	 * Http Delete Method를 호출하는 메소드
	 * hmacKey를 별도로 입력하여 사용
	 * 
	 * @param url 호출할 API의 url
	 * @param hmacKey hmac 인증에 사용되는 key 값
	 * @return RestUtil
	 */
	public RestUtil delete(String url, String hmacKey) {
		rest.response = rest.request.urlEncodingEnabled(false).delete(encryptionUrl(url + getParam(), hmacKey));

		rest.getApiResponseTime().setData(url, "delete", rest.response.time());

		return rest;
	}

	/**
	 * Http Put Method를 호출하는 메소드
	 * 
	 * @param url 호출할 API의 url
	 * @return RestUtil
	 */
	public RestUtil put(String url) {
		rest.response = rest.request.urlEncodingEnabled(false).put(encryptionUrl(url + getParam()));

		rest.getApiResponseTime().setData(url, "put", rest.response.time());

		return rest;
	}

	/**
	 * Http Put Method를 호출하는 메소드
	 * hmacKey를 별도로 입력하여 사용
	 * 
	 * @param url 호출할 API의 url
	 * @param hmacKey hmac 인증에 사용되는 key 값
	 * @return RestUtil
	 */
	public RestUtil put(String url, String hmacKey) {
		rest.response = rest.request.urlEncodingEnabled(false).put(encryptionUrl(url + getParam(), hmacKey));

		rest.getApiResponseTime().setData(url, "put", rest.response.time());


		return rest;
	}

	// ========================= API GW =========================

	/**
	 * HmacKey를 초기화하는 메소드
	 * 
	 * @param hmacKey hmac 인증에 사용되는 key 값
	 * @return this@ApiGateWayUtil
	 */
	public ApiGateWayUtil initialize(String hmacKey) {
		try {
			MACManager.initialize(Type.KEY, hmacKey);
		} catch (Exception e) {
			rest.printLog("HMAC Key 초기화에 실패했습니다.");
			e.printStackTrace();
		}

		return this;
	}

	/**
	 * HmacKey를 사용하여 암호화된 url을 출력하는 메소드
	 * 
	 * @param url hmac 인증에 사용되는 key 값
		 * @param hmacKey hmac 인증에 사용되는 key 값

	 * @return encryptedUrl  암호화된 url 값 
	 */
	private String encryptionUrl(String url) {
		String encryptedUrl = "";

		try {
			encryptedUrl = MACManager.getEncryptUrl(url);
		} catch (Exception e) {
			rest.printLog("Url encryption에 실패했습니다.");
			e.printStackTrace();
		}

		return encryptedUrl;
	}

	/**
	 * HmacKey를 사용하여 암호화된 url을 출력하는 메소드
	 * hmacKey를 별도로 입력하여 사용
	 * 
	 * @param url hmac 인증에 사용되는 key 값
	 * @param hmacKey hmac 인증에 사용되는 key 값
	 * @return encryptedUrl  암호화된 url 값 
	 */
	private String encryptionUrl(String url, String hmacKey) {
		String encryptedUrl = "";

		try {
			encryptedUrl = MACManager.getEncryptUrl(url, Type.KEY, hmacKey);
		} catch (Exception e) {
			rest.printLog("Url encryption에 실패했습니다.");
			e.printStackTrace();
		}

		return encryptedUrl;
	}
}
