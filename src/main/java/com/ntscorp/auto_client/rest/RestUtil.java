package com.ntscorp.auto_client.rest;

import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.ntscorp.auto_client.getset.ReportInfo;
import com.ntscorp.auto_client.verity.Verify;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.path.xml.XmlPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class RestUtil {
	public Response response;
	public RequestSpecification request = RestAssured.given();
	private XmlPath xmlPath = null;
	private JsonPath jsonPath = null;

	public RestModel model = new RestModel();
	private ApiGateWayUtil apigw = new ApiGateWayUtil(this);
	private ApiResponseTimeUtil apiResponseTime = ApiResponseTimeUtil.getInstance();

	public Assert assertion = new Assert(this);
	
	// ========================= Given =========================

	/**
	 * ApiGateWayUtil 객체를 호출하는 메소드 
	 * Parameter 값 초기화를 위해 RestUtil 초기화 진행 이후 ApiGateWayUtil 객체 리턴
	 * 
	 * @return ApiGateWayUtil API 호출 전까지 ApiGateWayUtil을 Fluent Style로 사용 
	 */
	public ApiGateWayUtil apigw() {
		return this.reset().apigw;
	}

	/**
	 * request, response, xmlPath, jsonPath를 모두 초기화하는 메소드
	 * 
	 * @return RestUtil
	 */
	public RestUtil reset() {
		request = RestAssured.given();
		response = null;
		xmlPath = null;
		jsonPath = null;

		return this;
	}

	/**
	 * 모든 로그인 쿠키을 값 제거하는 메소드
	 * 
	 * @return RestUtil
	 */
	public RestUtil clearCookies() {
		model.clearCookies();
		return this;
	}

	/**
	 * ContentType을 변경하는 메소드
	 * 
	 * @param type 변경할 ContentType
	 * @return RestUtil
	 */
	public RestUtil contentType(ContentType type) {
		request = request.contentType(type);
		return this;
	}

	/**
	 * 네이버 로그인 쿠키를 저장하는 메소드.
	 * ※ 반드시 테스트 계정으로 로그인 해야 함.
	 * 
	 * @param id 네이버 계정 아이디
	 * @param pw 네이버 계정 패스워드
	 * @return RestUtil
	 */
	public RestUtil cookies(String id, String pw) {
		request = request.cookies(model.getCookies(id, pw));
		return this;
	}
	
	/**
	 * neoId 로그인 쿠키를 저장하는 메소드.
	 * ※ 반드시 테스트 계정으로 로그인 해야 함.
	 * 
	 * @param id 네이버 계정 아이디
	 * @param pw 네이버 계정 패스워드
	 * @param social 로그인 할 Social Media 설정 {@value SocialMedia}
	 * @return RestUtil
	 */
	public RestUtil neoIdcookies(String id, String pw, SocialMedia social) {
		request = request.cookies(model.getNeoIdCookies(id, pw, social));
		return this;
	}

	/**
	 * 단일 Parameter(Object Type) 값을 설정하는 메소드
	 * ※해당 메소드 호출 시 기존의 request, response, xmlPath,jsonPath 값을 모두 초기화
	 * 
	 * @param key Parameter key 값
	 * @param value Parameter value 값
	 * @return RestUtil
	 */
	public RestUtil param(String key, Object value) {
		this.reset();

		request = request.param(key, value);
		getApiResponseTime().setParamData(key, value);

		return this;
	}

	/**
	 * 단일 Parameter(Collection Type) 값을 설정하는 메소드 
	 * ※해당 메소드 호출 시 기존의 request, response, xmlPath,jsonPath 값을 모두 초기화
	 * 
	 * @param key Parameter key 값
	 * @param value Parameter value 값
	 * @return RestUtil
	 */
	public RestUtil param(String key, Collection<?> value) {
		this.reset();

		request = request.param(key, value);
		getApiResponseTime().setParamData(key, value);

		return this;
	}

	/**
	 * 배열 형태의 복수 Parameter 값을 설정하는 메소드
	 *  ※해당 메소드 호출 시 기존의 request, response, xmlPath,jsonPath 값을 모두 초기화
	 * 
	 * @param keys  String[] 형태의 Parameter key 값
	 * @param values  Object[] 형태의 Parameter value 값
	 * @return RestUtil
	 */
	public RestUtil param(String[] keys, Object[] values) {
		this.reset();
		this.param(Arrays.asList(keys), Arrays.asList(values));

		return this;
	}

	/**
	 * 리스트 형태의 복수 Parameter 값을 설정하는 메소드
	 * ※해당 메소드 호출 시 기존의 request, response, xmlPath,jsonPath 값을 모두 초기화
	 * 
	 * @param key List 형태의 Parameter key 값
	 * @param value  List 형태의 Parameter value 값
	 * @return RestUtil
	 */
	public RestUtil param(List<String> keys, List<Object> values) {
		this.reset();

		for (int i = 0; i < keys.size(); i++) {
			request = request.param(keys.get(i), values.get(i));
			getApiResponseTime().setParamData(keys.get(i), values.get(i));
		}

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
		response = request.get(url);

		getApiResponseTime().setData(url, "get", response.time());

		return this;
	}

	/**
	 * Http Post Method를 호출하는 메소드
	 * 
	 * @param url 호출할 API의 url
	 * @return RestUtil
	 * @throws IllegalArgumentException
	 */
	public RestUtil post(String url) {
		try {
			response = request.post(url);

			getApiResponseTime().setData(url, "post", response.time());
		} catch (IllegalArgumentException e) {
			contentType(ContentType.TEXT);
			post(url);
		}

		return this;
	}

	/**
	 * Http Post Method를 호출하는 메소드
	 * Content Type 변경
	 * 
	 * @param url 호출할 API의 url
	 * @param type 변경할 Content Type
	 * @return RestUtil
	 */
	public RestUtil post(String url, ContentType type) {
		this.contentType(type);
		response = request.post(url);

		getApiResponseTime().setData(url, "post", response.time());

		return this;
	}

	/**
	 * Http Delete Method를 호출하는 메소드
	 * 
	 * @param url 호출할 API의 url
	 * @return RestUtil
	 */
	public RestUtil delete(String url) {
		response = request.delete(url);

		getApiResponseTime().setData(url, "delete", response.time());

		return this;
	}

	/**
	 * Http Put Method를 호출하는 메소드
	 * 
	 * @param url 호출할 API의 url
	 * @return RestUtil
	 */
	public RestUtil put(String url) {
		response = request.put(url);

		getApiResponseTime().setData(url, "put", response.time());

		return this;
	}

	// ========================= Then =========================

	/**
	 * xml, json RootPath를 설정하는 메소드
	 * 
	 * @param rootPath xml 또는 json RootPath
	 * @return RestUtil
	 */
	public RestUtil root(String rootPath) {
		if (response.contentType().contains("xml") || response.contentType().contains("html")) {
			xmlPath = response.xmlPath().setRoot(rootPath);
		} else {
			jsonPath = response.jsonPath().setRoot(rootPath);
		}

		return this;
	}

	/**
	 * 모든 Response 로그를 출력하는 메소드
	 */
	public void printLog() {
		response.then().log().all();
	}

	/**
	 * StatusCode를 출력하는 메소드
	 * 
	 * @return statusCode Response에 노출된 Status Code
	 */
	public int statusCode() {
		int statusCode = response.getStatusCode();
		return statusCode;
	}

	/**
	 * 입력한 경로의 String 값을 출력하는 메소드
	 * 
	 * @param path 찾고자 하는 String 값의 경로
	 * @return text String 값
	 */
	public String getString(String path) {
		String text;

		if (response.contentType().contains("xml")) {
			text = xmlPath == null ? response.xmlPath().getString(path) : xmlPath.getString(path);
		} else {
			text = jsonPath == null ? response.jsonPath().getString(path) : jsonPath.getString(path);
		}

		if (text.isEmpty())
			this.printLog(path + "is null");

		// 단일 String인줄 알았는데 Json List인 경우.. 일단은 String 값이 return되도록 함.
		if (text.contains("[") && text.contains("]")) {
			text = getPattenizedValue(text, "\\[([^\"]*)\\]");
		}

		if (text.equals(":"))
			return "";

		return text;
	}

	/**
	 * 입력한 경로의 String 값을 출력하는 메소드
	 * 
	 * @param path 찾고자 하는 String 값의 경로
	 * @return text String 값
	 */
	public String getText(String path) {
		return getString(path);
	}

	/**
	 * 입력한 경로의 int 값을 출력하는 메소드
	 * 
	 * @param path 찾고자 하는 int 값의 경로
	 * @return num int 값
	 */
	public int getInt(String path) {
		int num;

		if (response.contentType().contains("xml")) {
			num = xmlPath == null ? response.getBody().xmlPath().getInt(path) : xmlPath.getInt(path);
		} else {
			num = jsonPath == null ? response.getBody().jsonPath().getInt(path) : jsonPath.getInt(path);
		}

		return num;
	}

	/**
	 * 입력한 경로의 int 값을 출력하는 메소드
	 * 
	 * @parampath 찾고자 하는 int 값의 경로
	 * @return num int 값
	 */
	public int getCount(String path) {
		return getInt(path);
	}

	/**
	 * 입력한 경로의 Long 값을 출력하는 메소드
	 * 
	 * @param 찾고자 하는 Long 값의 경로
	 * @return num Long 값
	 */
	public Long getLong(String path) {
		Long num;

		if (response.contentType().contains("xml")) {
			num = xmlPath == null ? response.getBody().xmlPath().getLong(path) : xmlPath.getLong(path);
		} else {
			num = jsonPath == null ? response.getBody().jsonPath().getLong(path) : jsonPath.getLong(path);
		}

		return num;
	}

	/**
	 * 입력한 경로의 Boolean 값을 출력하는 메소드
	 * 
	 * @param 찾고자 하는 Boolean 값의 경로
	 * @return bool Boolean 값
	 */
	public Boolean getBoolean(String path) {
		Boolean bool;

		if (response.contentType().contains("xml")) {
			bool = xmlPath == null ? response.getBody().xmlPath().getBoolean(path) : xmlPath.getBoolean(path);
		} else {
			bool = jsonPath == null ? response.getBody().jsonPath().getBoolean(path) : jsonPath.getBoolean(path);
		}

		return bool;
	}

	/**
	 * 입력한 경로의 String List 값을 출력하는 메소드
	 * 
	 * @param path 찾고자 하는 String List 값의 경로
	 * @return list String list의 값
	 */
	public List<String> getList(String path) {
		List<Object> list;

		if (response.contentType().contains("xml")) {
			list = xmlPath == null ? response.getBody().xmlPath().getList(path) : xmlPath.getList(path);
		} else {
			list = jsonPath == null ? response.getBody().jsonPath().getList(path) : jsonPath.getList(path);

			// 리스트 내에 리스트가 있는 경우 1번 정도 풀어서 하나의 리스트로 리턴함
			list = refineJsonList(list);
		}

		if (list.size() == 0)
			this.printLog(path + ": null");

		List<String> strings = new ArrayList<>(list.size());
		for (Object object : list) {
			strings.add(object != null ? object.toString() : null);
		}

		return strings;
	}

	/**
	 * 리스트 안에 리스트가 있는 경우 리스트를 풀어서 정리
	 */
	@SuppressWarnings("unchecked")
	private List<Object> refineJsonList(List<Object> list) {
		int i = 0;

		while (i < list.size()) {
			if (list.get(i) instanceof List) {
				List<Object> newList = new ArrayList<Object>();
				for (Object list1 : list) {
					if ((List<Object>) list1 != null && ((List<Object>) list1).size() != 0)
						newList.addAll((List<Object>) list1);
				}

				list = newList;
			}

			i++;
		}

		return list;
	}

	
	/**
	 * 입력한 경로의 int List 값을 출력하는 메소드
	 * 
	 * @param path 찾고자 하는 int List 값의 경로
	 * @return list int list의 값
	 */
	public List<Integer> getIntList(String path) {
		return getList(path).stream().map(Integer::parseInt).collect(Collectors.toList());
	}

	/**
	 * 입력한 경로의 int List 값을 출력하는 메소드
	 * 
	 * @param path 찾고자 하는 int List 값의 경로
	 * @param mustExist 리스트의 값이 반드시 존재해야 하는지 체크
	 * @return list int list의 값
	 */
	public List<Integer> getIntList(String path, Boolean mustExist) {
		if (mustExist)
			return getIntList(path);

		try {
			return getIntList(path);
		} catch (NumberFormatException e) {
			List<String> list = getList(path);
			list.removeAll(Collections.singleton(null));
			return list.stream().map(Integer::parseInt).collect(Collectors.toList());
		}
	}

	/**
	 * 입력한 경로의 Boolean List 값을 출력하는 메소드
	 * 
	 * @param path 찾고자 하는 Boolean List 값의 경로
	 * @return list Boolean list의 값
	 */
	public List<Boolean> getBooleanList(String path) {
		return getList(path).stream().map(Boolean::parseBoolean).collect(Collectors.toList());
	}

	/**
	 * 입력한 경로의 Boolean List 값을 출력하는 메소드
	 * 
	 * @param path 찾고자 하는 Boolean List 값의 경로
	 * @param mustExist 리스트의 값이 반드시 존재해야 하는지 확인
	 * @return list Boolean list의 값
	 */
	public List<Boolean> getBooleanList(String path, Boolean mustExist) {
		if (mustExist)
			return getBooleanList(path);

		try {
			return getBooleanList(path);
		} catch (NumberFormatException e) {
			List<String> list = getList(path);
			list.removeAll(Collections.singleton(null));
			return list.stream().map(Boolean::parseBoolean).collect(Collectors.toList());
		}
	}

	/**
	 * 입력한 url의 링크가 dead링크인지 확인하는 메소드
	 * 
	 * @param url dead링크인지 확인할 url
	 * @return Boolean 링크 확인 결과
	 */
	public Boolean isLinkAlive(String url) {
		try {
			URL link = new URL(url);
			HttpURLConnection httpConnection = (HttpURLConnection) link.openConnection();
			httpConnection.setConnectTimeout(2000);
			httpConnection.connect();

			if (httpConnection.getResponseCode() == 200) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * text의 내용 중 pattern에 의해 refine 가능한 값이 있을 경우 해당 값을 반환하는 메소드
	 * 
	 * @param text Origin Text
	 * @param pattern 정제할 패턴
	 * @return text Refine된 결과 텍스트
	 */
	public String getPattenizedValue(String text, String pattern) {
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(text);
		while (m.find())
			return m.group(1);

		return text;
	}

	/**
	 * Date가 Format에 맞는지 확인하는 메소드
	 * 
	 * @param date 확인할 날짜
	 * @param format 확인할 포멧
	 * @return Boolean 포멧 확인 결과
	 */
	public Boolean isDateFormated(String date, String format) {
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat(format);
			dateFormat.setLenient(false);
			dateFormat.parse(date);
			return true;
		} catch (ParseException e) {
			return false;
		}
	}

	/**
	 * Date가 Format에 맞는지 확인하는 메소드
	 * 
	 * @param date 확인할 날짜
	 * @param format 확인할 포멧
	 * @param isLenient 포멧을 엄격하게 확인할 것인지 설정
	 * @return Boolean 포멧 확인 결과
	 */
	public Boolean isDateFormated(String date, String format, Boolean isLenient) {
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat(format);
			dateFormat.setLenient(isLenient);
			dateFormat.parse(date);
			return true;
		} catch (ParseException e) {
			return false;
		}
	}

	// ========================= ETC Module =========================
	
	public ApiResponseTimeUtil getApiResponseTime() {
		return apiResponseTime;
	}
	
	/**
	 * 로그를 출력하는 메소드 
	 * 메소드를 실행하는 system의 정보를 출력 (platform, browser info)
	 * 
	 * @param logs 출력할 로그
	 * @return void
	 */
	public void printLog(String logs) {
		System.out.println("[api] " + logs);

		if (logs.contains("JS error")) {
			logs = logs.replace("-", "=");
			if (ReportInfo.getLogBuf().indexOf(logs) < 0) {
				System.out.println("Last " + logs);
				ReportInfo.setLogBuf("[api] " + logs);
				ReportInfo.setLogBuf("|");
			}
		} else {
			ReportInfo.setLogBuf("[api] " + logs);
			ReportInfo.setLogBuf("|");
		}
	}

	// ========================= Assertion =========================

	/**
	 * Response 값 Assertion
	 */
	public class Assert {
		RestUtil rest;

		public Assert(RestUtil rest) {
			this.rest = rest;
		}

		/**
		 * Response의 Status Code가 200인지 확인하는 메소드
		 * 
		 * @return void Assertion Result
		 */
		public void statusCode() {
			int statusCode = rest.statusCode();
			Verify.verifyEquals(statusCode, 200);
		}

		/**
		 * Response의 Status Code와 Status Code 기대값이 동일한지 확인하는 메소드
		 * 
		 * @param expectStatusCode Status Code 기대값
		 * @return void Assertion Result
		 */
		public void statusCode(int expectStatusCode) {
			int statusCode = rest.statusCode();
			Verify.verifyEquals(statusCode, expectStatusCode);
		}

		/**
		 * list 내에 item 값이 반드시 존재하는지 확인하는 메소드
		 * 
		 * @param path 확인하고자 하는 list의 경로
		 * @return Boolean 모든 item 값이 존재하는 지 확인 결과
		 */
		public Boolean isListItemExist(String path) {
			List<String> list = rest.getList(path);

			if (list == null || list.isEmpty()) {
				rest.printLog("list를 가져올 수 없습니다.");
				return false;
			}

			for (int i = 0; i < list.size(); i++) {
				if (list.get(i) == null)
					continue;

				if (list.get(i).isEmpty()) {
					rest.printLog(path + "의 " + i + "번째 index의 값이 없습니다.");
					return false;
				}
			}

			return true;
		}

		/**
		 * list 내의 item 값이 0보다 큰 값인지 확인하는 메소드
		 * 
		 * @param path 확인하고자 하는 list의 경로
		 * @param mustExist 리스트의 값이 반드시 존재해야 하는지 체크
		 * @return Boolean 0보다 큰지 확인 결과
		 */
		public Boolean isItemBiggerThanZero(String path, Boolean mustExist) {
			List<Integer> list = rest.getIntList(path, mustExist);

			if (list.isEmpty() || list == null)
				return false;

			for (int i = 0; i < list.size(); i++) {
				if (mustExist && list.get(i) == null) {
					rest.printLog(path + "의 " + i + "번째 index의 값이 없습니다.");
					return false;
				}

				if (list.get(i) <= 0) {
					rest.printLog(path + "의 " + i + "번째 index의 값이 0과 같거나 0보다 작습니다.");
					return false;
				}
			}

			return true;
		}

		/**
		 * list 내의 item 값이 모두 Boolean인지 확인하는 메소드
		 * 
		 * @param path 확인하고자 하는 list의 경로
		 * @return Boolean Data Type 확인 결과
		 */
		public Boolean isListItemBoolean(String path, Boolean mustExist) {
			List<Boolean> list = rest.getBooleanList(path, mustExist);

			if (list.isEmpty() || list == null)
				return false;

			for (int i = 0; i < list.size(); i++) {
				if (mustExist && list.get(i) == null) {
					rest.printLog(path + "의 " + i + "번째 index의 값이 없습니다.");
					return false;
				}

				if (!(list.get(i) instanceof Boolean)) {
					rest.printLog(path + "의 " + i + "번째 index의 값의 type이 Boolean이 아닙니다.");
					return false;
				}
			}

			return true;
		}

		/**
		 * list 내의 item 값이 모두 입력한 data format에 맞는지 확인하는 메소드
		 * 
		 * @param path 확인하고자 하는 list의 경로
		 * @param 확인할 포멧
		 * @param isLenient 포멧을 엄격하게 확인할 것인지 설정
		 * @return Boolean 포멧 확인 결과
		 */
		public Boolean validateDateToListItem(String path, String format, Boolean isLenient, Boolean mustExist) {
			List<String> list = rest.getList(path);

			if (list.isEmpty() || list == null)
				return false;

			for (int i = 0; i < list.size(); i++) {
				if (list.get(i) == null)
					if (mustExist) {
						rest.printLog(path + "의 " + i + "번째 index의 값이 없습니다.");
						return false;
					} else
						continue;

				if (!rest.isDateFormated(list.get(i), format, isLenient)) {
					rest.printLog(path + "의 " + i + "번째 index의 date type이 형식과 맞지 않습니다.");
					return false;
				}
			}

			return true;
		}

		/**
		 * list 내에 item 값이 모두 Alive 링크인지 확인하는 메소드
		 * 
		 * @param path 확인하고자 하는 list의 경로
		 * @return Boolean Alive 링크 확인 결과
		 */
		public Boolean isLinkAlive(String path, Boolean isMustPresent) {
			List<String> urls = rest.getList(path);

			if (urls.isEmpty() || urls == null)
				return false;

			for (int i = 0; i < urls.size(); i++) {
				if (urls.get(i) == null) {
					if (isMustPresent) {
						rest.printLog(path + "의 " + i + "번째 index의 값이 없습니다.");
						return false;
					} else
						continue;
				}

				if (!rest.isLinkAlive(urls.get(i))) {
					rest.printLog(path + "의 " + i + "번째 index의 url이 dead link입니다.");
					rest.printLog(urls.get(i));
					return false;
				}
			}

			return true;
		}
	}
}