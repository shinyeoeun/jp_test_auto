package com.ntscorp.auto_client.getset;

import java.util.ArrayList;
import java.util.List;

public class ReportInfo {

	protected static StringBuffer logBuf;
	protected static List<List<String>> webPerformanceResults;
	
	public ReportInfo() {
		initWebPerformanceResults();
		initLogBuf();
	}
	
	public static void initWebPerformanceResults() {
		webPerformanceResults = new ArrayList<List<String>>();
		List<String> firstIndex = new ArrayList<String>();
		firstIndex.add(0, "URL");
		firstIndex.add(1, "Count");
		firstIndex.add(2, "Max"); // max
		firstIndex.add(3, "Avg"); // average
		firstIndex.add(4, "Min"); // min
		webPerformanceResults.add(0, firstIndex);
	}
	
	public static List<List<String>> getWebPerformanceResults() {
		return webPerformanceResults;
	}

	public static void setWebPerformanceResults(List<String> currResult) {
		webPerformanceResults.add(currResult);
	}

	public static void initLogBuf() {
		logBuf = null;
		logBuf = new StringBuffer();
	}
	
	public static StringBuffer getLogBuf() {
		return logBuf;
	}

	public static void setLogBuf(String log) {
		logBuf.append(log);
	}
}
