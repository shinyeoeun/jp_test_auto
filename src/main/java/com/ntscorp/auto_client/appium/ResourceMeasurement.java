package com.ntscorp.auto_client.appium;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class ResourceMeasurement {

	private ArrayList<Double> cpu;
	private ArrayList<Long> memory;
	private ArrayList<Integer> upload;
	private ArrayList<Integer> download;
	
	static JSONObject jsonObject;
	static JSONArray resourceArray = new JSONArray();
	static JSONArray avgArray = new JSONArray();
	static JSONObject resourceInfo;

	public ResourceMeasurement() {
		cpu = new ArrayList<Double>();
		memory = new ArrayList<Long>();
		upload = new ArrayList<Integer>();
		download = new ArrayList<Integer>();
	}
	
	public ArrayList<Double> getCpu() {
		return cpu;
	}
	
	public void setCpu(Double cpu) {
		this.cpu.add(cpu);
	}
	
	public ArrayList<Long> getMemory() {
		return memory;
	}
	
	public void setMemory(Long memory) {
		this.memory.add(memory);
	}
	
	public ArrayList<Integer> getUpload() {
		return upload;
	}
	
	public void setUpload(Integer upload) {
		this.upload.add(upload);
	}
	
	public ArrayList<Integer> getDownload() {
		return download;
	}
	
	public void setDownload(Integer download) {
		this.download.add(download);
	}
	
	public void resourceData(String suite_title, String class_title, String method_title,
			double cpu, long memory, int upload, int download) {
		resourceInfo = new JSONObject();

//		resourceInfo.put("projectID", project_id);
		resourceInfo.put("suiteName", suite_title);
		resourceInfo.put("className", class_title);
		resourceInfo.put("methodName", method_title);
		resourceInfo.put("cpu", cpu);
		resourceInfo.put("memory", memory);
		resourceInfo.put("upload", upload);
		resourceInfo.put("download", download);

		resourceArray.add(resourceInfo);
		
		this.setCpu(cpu);
		this.setMemory(memory);
		this.setUpload(upload);
		this.setDownload(download);
	}
	
	public JSONArray calculationAVG() throws ArithmeticException {
		double cpuAVG = 0;
		long memoryAVG = 0;
		int uploadTotal = 0;
		int downloadTotal = 0;
		
		for (int i = 0; i < this.getCpu().size(); i++) {
			cpuAVG = cpuAVG + this.getCpu().get(i);
			memoryAVG = memoryAVG + this.getMemory().get(i);
			uploadTotal = uploadTotal + this.getUpload().get(i);
			downloadTotal = downloadTotal + this.getDownload().get(i);
		}
		resourceInfo = new JSONObject();
		resourceInfo.put("cpu", cpuAVG/this.getCpu().size());
		resourceInfo.put("memory", memoryAVG/this.getMemory().size());
		resourceInfo.put("upload", uploadTotal);
		resourceInfo.put("download", downloadTotal);

		avgArray.add(resourceInfo);

		return avgArray;
	}

	public String createJson(String projectName, Timestamp startTime) {
		jsonObject = new JSONObject();
		jsonObject.put("projectName", projectName);
		jsonObject.put("date", startTime.toString());
		jsonObject.put("resource", resourceArray);
		try {
			jsonObject.put("avg", calculationAVG());
		} catch (ArithmeticException ae) {}
		
		if (!resourceArray.isEmpty()) {
			return jsonObject.toJSONString();
		} else {
			System.out.println("### No resource measurement value.");
			return null;
		}
	}

	public void createReport(String projectName, Timestamp startTime) {
		StringBuffer sbuf = new StringBuffer();
		File file = new File("automation-output//AppResourceReport.html");
		BufferedWriter w;
		
		try {
			file.createNewFile();
			w = new BufferedWriter(new FileWriter(file, false));
			
			URL url = new URL("http://10.113.121.47/data/resourceReport.html");
			URLConnection urlConn = url.openConnection();
			InputStream is = urlConn.getInputStream();
			InputStreamReader isr = new InputStreamReader(is, "UTF-8");
			BufferedReader br = new BufferedReader(isr);

			String str;
			while ((str = br.readLine()) != null) {
				sbuf.append(str + "\r\n");
				
				if (str.contains("var rMon =")) {
					sbuf.append(this.createJson(projectName, startTime) + "; \r\n");
				}
			}

			w.write(sbuf.toString());
			w.close();
			
			System.out.println(">>> App Resource Monitoring Report : AppResourceReport.html");
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}
