package com.ntscorp.auto_client.rest;

import java.io.File;
import java.io.FileOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.ntscorp.auto_client.Utilities;
import com.ntscorp.auto_client.getset.TestInfo;

/**
 * API가 호출될 때마다 response time을 측정하여 xml로 저장
 */
public class ApiResponseTimeUtil implements Utilities {
	private static DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
	private static DocumentBuilder builder = null;
	private static Document document;
	private static Element response;
	private static Element suiteList;
	private Element dataList;
	private String paramData = "";

	private ApiResponseTimeUtil() {
	}

	private static class SingleTon {
		private static final ApiResponseTimeUtil instance = new ApiResponseTimeUtil();
	}

	public static ApiResponseTimeUtil getInstance() {
		return SingleTon.instance;
	}

	/**
	 * 프로젝트 최초 시작 시 xml 데이터를 생성하는 메소드
	 * 
	 * @throws ParserConfigurationException
	 */
	public void init() {
		try {
			builder = documentBuilderFactory.newDocumentBuilder();

			document = builder.newDocument();
			document.setXmlStandalone(true);

			response = document.createElement("response");
			document.appendChild(response);

			Element projectNameElement = document.createElement("projectName");
			projectNameElement.setTextContent(getDate());
			response.appendChild(projectNameElement);

			Element buildTimeElement = document.createElement("buildTime");
			buildTimeElement.setTextContent(TestInfo.getStartTime().toString());
			response.appendChild(buildTimeElement);

			suiteList = document.createElement("suiteList");
			response.appendChild(suiteList);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Suite 시작 시 동작되는 메소드
	 * 
	 * @param suiteName
	 *        수행될 suite의 이름
	 */
	public void setClassName(String suiteName) {
		if (document == null) {
			return;
		}
		
		Element suite = document.createElement("suite");
		suiteList.appendChild(suite);

		Element suiteNameElement = document.createElement("suiteName");
		suiteNameElement.setTextContent(suiteName);
		suite.appendChild(suiteNameElement);

		dataList = document.createElement("dataList");
		suite.appendChild(dataList);
	}

	/**
	 * API 호출 시 동작되는 메소드
	 * 
	 * @param url
	 *        호출되는 API의 url
	 * @param methodType
	 *        호출되는 API의 Http Method Type
	 * @param time
	 *        API 호출 이후 측정된 API Response Time
	 */
	public void setData(String url, String methodType, Long time) {
		if (document == null) {
			return;
		}

		Element data = document.createElement("data");
		dataList.appendChild(data);

		Element urlElement = document.createElement("url");
		urlElement.appendChild(document.createTextNode(url));
		data.appendChild(urlElement);

		Element methodTypeElement = document.createElement("methodType");
		methodTypeElement.appendChild(document.createTextNode(methodType));
		data.appendChild(methodTypeElement);

		Element paramElement = document.createElement("param");
		paramElement.appendChild(document.createTextNode(getParamData()));
		data.appendChild(paramElement);

		Element timeElement = document.createElement("time");
		timeElement.appendChild(document.createTextNode(Long.toString(time)));
		data.appendChild(timeElement);
	}

	/**
	 * 테스트 수행 이후 API Response Time 데이터를 xml 파일로 저장하는 메소드
	 * 
	 * @throws Exception
	 */
	public void saveFile() {
		if (document == null) {
			return;
		}
		
		try {
			TransformerFactory transformerFactory = TransformerFactory.newInstance();

			Transformer transformer = transformerFactory.newTransformer();

			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "yes");

			DOMSource source = new DOMSource(document);

			String filePath = "automation-output//" + refineStartTime(TestInfo.getStartTime().toString()) + ".xml";

			StreamResult result = new StreamResult(new FileOutputStream(new File(filePath)));

			transformer.transform(source, result);
			
			SendResponsTimeReport report = new SendResponsTimeReport();
			report.upload(filePath);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 호출한 API의 Parameter 값 저장
	 * 
	 * @param key
	 *        Parameter key 값
	 * @param value
	 *        Parameter value 값
	 */
	protected void setParamData(String key, Object value) {
		if (paramData.isEmpty())
			paramData = key + "=" + value;
		else
			paramData += "," + key + "=" + value;
	}

	/**
	 * 호출한 API의 Parameter 값 출력
	 * 
	 * @return value Parameter String
	 */
	private String getParamData() {
		String value = paramData;
		paramData = "";

		return value;
	}

	/**
	 * 시작 시간을 형식에 맞게 변경(yyyyMMddhhmm)
	 * 
	 * @return time
	 */
	private String refineStartTime(String time) {
		time = time.replaceAll("-", "");
		time = time.replaceAll(":", "");
		time = time.replaceAll(" ", "");
		time = time.substring(0,  time.length()- 4);
			
		return time;
	}
}
