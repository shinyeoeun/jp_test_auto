package com.ntscorp.auto_client;

import java.io.File;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;

import com.aventstack.extentreports.Status;
import com.nts.rmon.RMon;
import com.ntscorp.auto_client.appium.AdbCmd;
import com.ntscorp.auto_client.appium.AndroidEventChecker;
import com.ntscorp.auto_client.appium.ResourceMeasurement;
import com.ntscorp.auto_client.data.AutomationTime;
import com.ntscorp.auto_client.getset.DeviceInfo;
import com.ntscorp.auto_client.getset.ReportInfo;
import com.ntscorp.auto_client.getset.TestInfo;
import com.ntscorp.auto_client.report.AutomationReport;
import com.ntscorp.auto_client.rest.RestUtil;
import com.ntscorp.auto_client.verity.TestMethodListener;

@Listeners(TestMethodListener.class)
public class Formatter extends Automation implements AutomationTime {
	private AutomationReport report = new AutomationReport();
	private AndroidEventChecker eventChecker = new AndroidEventChecker();
	private static RMon rmon = null;
	private static ResourceMeasurement reportData;
	public AdbCmd cmd = new AdbCmd();
	public RestUtil rest = new RestUtil();
	
	@BeforeSuite
	public void beforeSuite(ITestContext context) {
		new File("automation-output").mkdirs();
		TestInfo.setStartTime(new Timestamp(new Date().getTime()));
		reportData = new ResourceMeasurement();

		new ReportInfo();
		
		String osName = System.getProperty("os.name");
		if (osName.contains("Mac")) {
			TestInfo.setOs("Mac");
		} else if (osName.contains("Windows")) {
			TestInfo.setOs("Windows");
		} else {
			TestInfo.setOs(null);
		}

		TestInfo.setSuiteName(context.getCurrentXmlTest().getSuite().getName().replace(" ", "_"));		

		report.getExtentReport();
	}

	@AfterSuite
	public void afterSuite(ITestContext result) {
		if (TestInfo.getTarget().equals("Web")) {
			AutomationReport.addPerformanceTimeResult();
			AutomationReport.addJsErrorResult();
		}
		
		if (TestInfo.isResourceMeasurement()) {
			reportData.createReport("Resource-Test-Result", TestInfo.getStartTime());
		}
		
		report.closeReport();
		
		super.stop();
		report.createReport();
	}

	@BeforeClass
	public void setupClass(ITestContext context) {
		report.threadList.clear();
		report.testList.clear();
		String className = this.getClass().getSimpleName();
		String packageName = this.getClass().getPackage().getName();

		TestInfo.setSuiteName(context.getCurrentXmlTest().getSuite().getName().replace(" ", "_"));
		TestInfo.addClassName(className); // Class Name
		TestInfo.addPackageName(packageName); // Package Name

		AutomationReport.extentClass = AutomationReport.extentReport.createTest(TestInfo.getClassNameList().get(TestInfo
				.getClassSize() - 1), this.getClass().getName());
		AutomationReport.extentClass.assignCategory(TestInfo.getPackageNameList().get(TestInfo.getClassSize() - 1));
		
		rest.getApiResponseTime().setClassName(className);
	}

	@AfterClass
	public void tearDownClass() {
		if (AutomationReport.extentReport != null) {
			AutomationReport.extentReport.flush();
			AutomationReport.extentClass = null;
		}
	}

	@BeforeMethod(alwaysRun = true)
	public void beforeMethod(Method caller) {
		ReportInfo.initLogBuf();
		
		if (TestInfo.getPlatform().equals("Android")) {
			cmd.runCommand("adb -s " + DeviceInfo.getUdid() + " logcat -s EVENT_CHECK -c", false);
			cmd.runCommand("adb -s " + DeviceInfo.getUdid() + " shell am " + eventChecker.startServiceCommand()
					+ " -a EVENT_CHECK_START", false);
		}
		
		if (TestInfo.isResourceMeasurement()) {
			rmon = new RMon(TestInfo.getApkPackage());
			rmon.startMon();
		}
	}

	@AfterMethod
	public void afterMethod(Method caller, ITestResult result) throws InterruptedException {
		if (TestInfo.isResourceMeasurement()) {
			ArrayList<Double> cpu = rmon.getCpu();
			ArrayList<Long> mem = rmon.getMem();
			ArrayList<Integer> upload = rmon.getUpload();
			ArrayList<Integer> download = rmon.getDownload();
			
			for (int i = 0; i < rmon.getListSize(); i++) {
				reportData.resourceData(TestInfo.getSuiteName(),
						TestInfo.getClassNameList().get(TestInfo.getClassSize() - 1),
						caller.getName(),
						cpu.get(i).doubleValue(),
						mem.get(i).longValue(),
						upload.get(i).intValue(),
						download.get(i).intValue());
			}
		}
		
		report.setTestResult(caller, result, android, chrome);
		TestInfo.setMethodName(caller.getName());

		if (TestInfo.getPlatform().equals("Android")) {
			cmd.runCommand("adb -s " + DeviceInfo.getUdid() + " shell am " + eventChecker.startServiceCommand()
					+ " -a EVENT_CHECK_KILL", false);

			StringTokenizer temp = new StringTokenizer(eventChecker.getEventLog().toString(), "|");

			while (temp.hasMoreTokens())
				report.extentTest.log(Status.INFO, temp.nextToken());
		}

		StringTokenizer temp = new StringTokenizer(ReportInfo.getLogBuf().toString(), "|");

		while (temp.hasMoreTokens())
			report.extentTest.log(Status.INFO, temp.nextToken());

		AutomationReport.extentReport.flush();
	}
}