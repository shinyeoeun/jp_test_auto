package com.ntscorp.auto_client.report;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.ITestResult;

import com.ntscorp.auto_client.appium.AndroidUtil;
import com.ntscorp.auto_client.getset.DeviceInfo;
import com.ntscorp.auto_client.getset.ReportInfo;
import com.ntscorp.auto_client.getset.TestInfo;
import com.ntscorp.auto_client.selenium.ChromeUtil;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.Markup;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.aventstack.extentreports.reporter.configuration.ChartLocation;
import com.aventstack.extentreports.reporter.configuration.Protocol;
import com.aventstack.extentreports.reporter.configuration.Theme;

public class AutomationReport {
	public static ExtentReports extentReport;
	public static ExtentTest extentClass;
	public ExtentTest extentTest;

	public Map<Long, String> threadList = new HashMap<Long, String>();
	public Map<String, ExtentTest> testList = new HashMap<String, ExtentTest>();

	public static List<String> jsErrorResult = new ArrayList<String>();

	public synchronized ExtentReports getExtentReport() {
		if (extentReport == null) {
			ExtentHtmlReporter htmlReporter = new ExtentHtmlReporter("automation-output/Automation-Test-Reprot.html");

			htmlReporter.config().setChartVisibilityOnOpen(true);
			htmlReporter.config().setTestViewChartLocation(ChartLocation.TOP);
			htmlReporter.config().setTheme(Theme.DARK);
			htmlReporter.config().setTimeStampFormat("yyyy/MM/dd hh:mm:ss a");

			htmlReporter.config().setDocumentTitle("Automation-Test-Reprot");
			htmlReporter.config().setEncoding("UTF-8");
			htmlReporter.config().setProtocol(Protocol.HTTP);
			htmlReporter.config().setReportName("Automation-Test-Reprot" + (TestInfo.isHost() ? " [Host]" : "") + "</a>");

			// add custom css & javascript ???
			htmlReporter.config().setCSS("css-string");
			htmlReporter.config().setJS("js-string");

			extentReport = new ExtentReports();
			extentReport.attachReporter(htmlReporter);
			htmlReporter.setAppendExisting(true);
		}
		return extentReport;
	}

	public synchronized ExtentTest getTest(String testName, String testDescription, String device) {
		String keyVal = testName + device;

		if (!testList.containsKey(keyVal)) {
			Long threadID = Thread.currentThread().getId();

			extentTest = extentClass.createNode(testName,
					testDescription == null ? "" : "[Description] " + testDescription);
			extentTest.assignCategory(device);

			testList.put(keyVal, extentTest);
			threadList.put(threadID, keyVal);
		}
		return testList.get(keyVal);
	}

	public synchronized ExtentTest getTest(String testName) {
		return getTest(testName, "", "");
	}

	public synchronized ExtentTest getTest() {
		Long threadID = Thread.currentThread().getId();

		if (threadList.containsKey(threadID)) {
			String testName = threadList.get(threadID);
			return testList.get(testName);
		}
		return null;
	}

	public synchronized void closeReport() {
		if (extentReport != null) {
			// extentReport.flush();
			try {
				extentReport.flush();
			} catch (NullPointerException npe) {
				System.out.println("### No report generated or no test executed.");
			}
		}
	}

	public void createReport() {
		File from;
		from = new File("Automation-Test-Reprot.html");
		System.out.println(">>> Test Result report : automation-output/" + from.toString());
	}

	public void setTestResult(Method caller, ITestResult result, AndroidUtil appium, ChromeUtil chrome) {
		this.getTest(result.getName(), result.getMethod().getDescription(), "");
		int status = result.getStatus(); // 1: PASS, 2: FAILURE, 3: SKIP, 4:
											// SUCCESS_PERCENTAGE_FAILURE, 16 :
											// STARTED
		String parameter = "";
		if (caller.getParameterCount() > 0) {
			for (int i = 0; i < result.getParameters().length; i++) {
				parameter += result.getParameters()[i];
			}
		}

		if (result.isSuccess()) {
			System.out.println(caller.getName() + " : Passed (" + status + ")");
			extentTest.log(Status.PASS, caller.getName() + " ( " + parameter + " ) ");

			if (TestInfo.getScreenShotLevel().equalsIgnoreCase("all")) {
				try {
					this.addScreenShot(caller, appium, chrome);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else if (status == 3) {
			System.out.println(caller.getName() + " : Skipped (" + status + ")");
			extentTest.log(Status.SKIP, caller.getName() + " ( " + parameter + " ) ");

		} else {
			System.out.println(caller.getName() + " : Failed (" + status + ")");
			extentTest.log(Status.FAIL, caller.getName() + " ( " + parameter + " ) ");
			extentTest.log(Status.INFO, result.getThrowable().getMessage());

			if (TestInfo.getScreenShotLevel().equalsIgnoreCase("all")
					|| TestInfo.getScreenShotLevel().equalsIgnoreCase("failed")) {
				try {
					this.addScreenShot(caller, appium, chrome);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void addScreenShot(Method caller, AndroidUtil appium, ChromeUtil chrome) throws IOException {
		if (appium != null) {
			String filePath = appium.takeScreenShot(caller.getName(), DeviceInfo.getDeviceName());
			extentTest.info("", MediaEntityBuilder.createScreenCaptureFromPath(filePath).build());
		}
		if (chrome != null) {
			String filePath = chrome.takeScreenShot(caller.getName(), "chrome");
			extentTest.info("", MediaEntityBuilder.createScreenCaptureFromPath(filePath).build());
		}
	}

	public static void addPerformanceTimeResult() {
		extentClass = extentReport.createTest("Performance Time Result");
		extentClass.assignCategory("Navigation Time Result");

		String pData[][] = new String[ReportInfo.getWebPerformanceResults().size()][5];
		pData[0][0] = "URL";
		pData[0][1] = "Count";
		pData[0][2] = "Max";
		pData[0][3] = "Average";
		pData[0][4] = "Min";

		int integerData[][] = new int[ReportInfo.getWebPerformanceResults().size()][5];

		for (int i = 1; i < ReportInfo.getWebPerformanceResults().size(); i++) {
			List<String> temp = ReportInfo.getWebPerformanceResults().get(i);
			for (int j = 0; j < temp.size(); j++) {
				pData[i][j] = temp.get(j);
			}

			for (int j = 1; j < temp.size(); j++) {
				integerData[i - 1][j] = Integer.parseInt(temp.get(j));
			}
		}

		Arrays.sort(integerData, new Comparator<int[]>() {
			@Override
			public int compare(int[] o1, int[] o2) {
				int entry1 = o1[2];
				int entry2 = o2[2];

				return Integer.compare(entry2, entry1);
			}
		});

		for (int i = 1; i < ReportInfo.getWebPerformanceResults().size(); i++) {
			List<String> temp = ReportInfo.getWebPerformanceResults().get(i);
			for (int j = 0; j < temp.size(); j++) {
				if (j == 0) {
					pData[i][j] = "<a href=" + temp.get(j) + " target=\"_black\"'>" + temp.get(j) + "</a>";
				} else {
					pData[i][j] = Integer.toString(integerData[i - 1][j]);
				}
			}
		}

		Markup mh = MarkupHelper.createTable(pData);
		extentClass.pass(mh);

		extentReport.flush();
	}

	public static void addJsErrorResult() {
		extentClass = extentReport.createTest("JavaScript Error Result");
		extentClass.assignCategory("JavaScript Error Result");

		for (String jsError : jsErrorResult) {
			extentClass.warning(jsError);
		}
	}
}
