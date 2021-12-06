package com.ntscorp.auto_client.verity;

import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.internal.Utils;
import org.testng.internal.Version;

import com.ntscorp.auto_client.annotation.Issue;
import com.ntscorp.auto_client.annotation.Skip;
import com.ntscorp.auto_client.elastic.SendLog;
import com.ntscorp.auto_client.elastic.StatusLog;
import com.ntscorp.auto_client.getset.DeviceInfo;
import com.ntscorp.auto_client.getset.TestInfo;

public class TestMethodListener implements IInvokedMethodListener, ITestListener {
	public StatusLog log;
	
    public LocalDateTime suiteStartTime, suiteEndTime;
    public LocalDateTime classStartTime, classEndTime;

    public String os;
    public String service;
    public String libVersion;
    public String testngVersion;
    public String javaVersion;
    public String debug;
    
    //public int classCount = 0;
    public int methodCount = 0;
    public int passedCount = 0;
    public int skippedCount = 0;
    public int failedCount = 0;
    
    public boolean elastic;
    
	@Override
	public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
		if (method.isTestMethod()) {
			if (TestMethodErrorBuffer.get() != null) {
				throw new RuntimeException("Stale error buffer detected!");
			}

			TestMethodErrorBuffer.set(new ArrayList<Throwable>());
		}

		Skip skip = method.getTestMethod()
				.getConstructorOrMethod()
				.getMethod()
				.getAnnotation(Skip.class);

		Issue issue = method.getTestMethod()
				.getConstructorOrMethod()
				.getMethod()
				.getAnnotation(Issue.class);

		if (null != skip) {
			if (TestInfo.getPlatform().equalsIgnoreCase(skip.value().name())) {
				throw new SkipException("Skipped because not used on " + TestInfo.getPlatform() + " platforms");
			}
		} else if (null != issue) {
			throw new SkipException("Skipping this due to Open Defect - " + issue.value());
		}
	}

	@Override
	public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
		if (method.isTestMethod()) {
			List<Throwable> lThrowable = TestMethodErrorBuffer.get();

			if (lThrowable.size() > 0) {
				testResult.setStatus(ITestResult.FAILURE);

				if (testResult.getThrowable() != null) {
					lThrowable.add(testResult.getThrowable());
				}

				int size = lThrowable.size();

				if (size == 1) {
					testResult.setThrowable(lThrowable.get(0));

				} else {
					StringBuffer failureMessage = new StringBuffer("Multiple failures (").append(size).append(")\n");
					StringBuffer fullStack = new StringBuffer();

					for (int i = 0; i < size - 1; i++) {
						failureMessage.append("(").append(i + 1).append(")")
								.append(lThrowable.get(i).getClass().getName()).append(":")
								.append(lThrowable.get(i).getMessage()).append("\n");
						fullStack.append("Failure ").append(i + 1).append(" of ").append(size).append("\n");
						fullStack.append(Utils.stackTrace(lThrowable.get(i), false)[1]).append("\n");
					}

					fullStack.append("Failure ").append(size).append(" of ").append(size).append("\n");
					Throwable last = lThrowable.get(size - 1);
					failureMessage.append("(").append(size).append(")").append(last.getClass().getName()).append(":")
							.append(last.getMessage()).append("\n\n");

					fullStack.append(last.toString());

					testResult.setThrowable(new Throwable(failureMessage.toString() + fullStack.toString()));
					testResult.getThrowable().setStackTrace(last.getStackTrace());
				}
			}
			TestMethodErrorBuffer.remove();
		}
	}
	
	public void setCommonLog(ITestResult iTestResult) {
		LocalDateTime startTime = LocalDateTime.now().minusHours(9);
			
		log = new StatusLog()
			.setService(this.service)
			.setLibVersion(this.libVersion)
			.setTestNgVersion(testngVersion)
			.setSystemOs(this.os)
			.setDepth("method")
			.setClassName(iTestResult.getTestClass().getName())
			.setMethodName(iTestResult.getMethod().getMethodName())
			.setDescription(iTestResult.getMethod().getDescription())
			.setJavaVersion(javaVersion)
			.setStartTime(startTime.toString());
				
		if (this.methodCount == 1) {
			this.classStartTime = startTime;
		}
	}
	
	public void setAppLog(StatusLog targetLog) {
		targetLog.setDeviceName(DeviceInfo.getDeviceName())
			.setDeviceId(DeviceInfo.getUdid())
			.setDeviceOs(DeviceInfo.getOsVersion())
			.setDeviceSdk(DeviceInfo.getSdkVersion());
	}
	
	public void setWebLog() {
		
	}
	
	public void setDriverLog(StatusLog driverSetting) {
		driverSetting.setHeadless(TestInfo.isHeadless())
			.setEventChecker(TestInfo.isEventChecker())
			.setInstallAPK(TestInfo.isEventChecker())
			.setServerBuildingTime(TestInfo.getServerBuildingTime());
	}
	
	public String getLibraryVersion() {
		MavenXpp3Reader reader = new MavenXpp3Reader();
		Model model;
		
		try {
			model = reader.read(new FileReader("pom.xml"));
			List<Dependency> dependecies = model.getDependencies();
			
			for (int i = 0 ; i < dependecies.size() ; i ++) {
				Dependency dependency = dependecies.get(i);
				
				String artifactId = dependency.getArtifactId();
				String groupId = dependency.getGroupId();
				String version = dependency.getVersion();
				
				if (artifactId.equals("stac-client") && groupId.equals("com.nts.sqa")) {
					return version;
				}
			}
		} catch (IOException | XmlPullParserException e) {
			e.printStackTrace();
		}
		return "unknown";
	}

	public void sendResult(ITestResult iTestResult) {
		long duration = iTestResult.getEndMillis() - iTestResult.getStartMillis();
		log.setDuration((int) duration);
		
    	SendLog.sendStatusLog(SendLog.ES_METHOD_URL, log);
	}
	
	@Override
	public void onTestStart(ITestResult iTestResult) {
		elastic = TestInfo.getUseElastic();
		if (elastic) {
			this.methodCount = this.methodCount + 1;
			
			this.setCommonLog(iTestResult);
			if ((DeviceInfo.getDeviceName() != null))
				this.setAppLog(log);
		}
		this.setDriverLog(log);
	}

	@Override
	public void onTestSuccess(ITestResult iTestResult) {
		elastic = TestInfo.getUseElastic();
		if (elastic) {
			this.passedCount = this.passedCount + 1;
			
			log.setResultMethod("passed");
			sendResult (iTestResult);
			
			this.isLastMethod(iTestResult);
		}
	}

	@Override
	public void onTestFailure(ITestResult iTestResult) {
		elastic = TestInfo.getUseElastic();
		if (elastic) {
			this.failedCount = this.failedCount + 1;
			
			log.setResultMethod("failed");
			
			StackTraceElement[] errorLogs = iTestResult.getThrowable().getStackTrace();			
			if (errorLogs.length > 0) {
				for (int i = 0 ; i < errorLogs.length ; i ++) {
					String errorLog = errorLogs[i].toString();
					if (errorLog.contains(iTestResult.getTestClass().getName())) {
						log.setError(errorLog);	
					}
				}
			}
		
			String exceptionMsg = iTestResult.getThrowable().getMessage();			
			if (exceptionMsg != null) {
				log.setException(iTestResult.getThrowable().getMessage());	
			}
			sendResult (iTestResult);
			this.isLastMethod(iTestResult);
		}
	}

	@Override
	public void onTestSkipped(ITestResult iTestResult) {
		elastic = TestInfo.getUseElastic();
		if (elastic) {
			this.skippedCount = this.skippedCount + 1;
			
			log.setResultMethod("skipped");
			sendResult (iTestResult);
			
			this.isLastMethod(iTestResult);
		}
	}

	@Override
	public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
		if (elastic) {
			// todo
		}
	}
	
	public void isLastMethod(ITestResult result) {
		int count = result.getMethod().getTestClass().getTestMethods().length;

		/*
		ITestNGMethod[] methods = result.getMethod().getTestClass().getTestMethods();
		for (int i = 0; i < methods.length ; i ++) {
			System.out.println("check name : " + methods[i].getMethodName());
		}
		*/

		if (count == methodCount) {
			if (elastic) {
				this.sendClassResult(result);
				
				// class 기준으로 healthy check
				elastic = TestInfo.getUseElastic() && SendLog.isElasticAlive();
				//this.classCount = this.classCount + 1;
			}
		}
	}
	
	public void sendClassResult(ITestResult result) {
		StatusLog classLog = new StatusLog();
		
		classLog.setDepth("class")
			.setService(service)
			.setSystemOs(os)
			.setJavaVersion(javaVersion)
			.setLibVersion(libVersion)
			.setTestNgVersion(testngVersion)
			.setClassName(result.getMethod().getTestClass().getName());
		
		classEndTime = LocalDateTime.now().minusHours(9);
		long duration = Duration.between(classStartTime, classEndTime).toMillis();
		classLog.setStartTime(classStartTime.toString())
			.setDuration((int) duration);
				
		if ((DeviceInfo.getDeviceName() != null)) {
			this.setAppLog(classLog);	
		}
		this.setDriverLog(classLog);
		
		int all = this.passedCount + this.skippedCount + this.failedCount;
		double passRate = this.passedCount / (double) all;
		
		classLog.setResultClassRate(passRate)
    		.setResultClassPassed(this.passedCount)
    		.setResultClassFailed(this.failedCount)
    		.setResultClassSkipped(this.skippedCount);
		
		SendLog.sendStatusLog(SendLog.ES_CLASS_URL, classLog);
		
		this.methodCount = 0;
		this.passedCount = 0;
		this.failedCount = 0;
		this.skippedCount = 0;
	}

	// suite run
	@Override
	public void onStart(ITestContext context) {	
		elastic = TestInfo.getUseElastic();
		if (elastic) {
			suiteStartTime = LocalDateTime.now().minusHours(9);
			log = new StatusLog();
					
			os = System.getProperty("os.name");
			libVersion = this.getLibraryVersion();
			testngVersion = Version.VERSION;
			javaVersion = Runtime.class.getPackage().getImplementationVersion();
			
			// TODO : java 버전이 null인 경우 있음 - 확인 필요
			if (javaVersion == null) 
				javaVersion = System.getProperty("java.version");
			if (javaVersion == null)
				javaVersion = "unknown";
		
			log.setSystemOs(os)
				.setService(service)
				.setLibVersion(libVersion)
				.setJavaVersion(javaVersion);
			
			// 어떤 이름을.??
		}
	}
	
	public void setDebugLog(String comment) {
		this.debug = debug + "\n" + comment;
		log.setDebugLog(this.debug);
	}

	@Override
	public void onFinish(ITestContext context) {
		if (elastic) {
			/*
			XmlTest xml = context.getCurrentXmlTest();
			System.out.println("file : " + xml.getSuite().getFileName());
			//  : C:\Users\USER\AppData\Local\Temp\testng-eclipse--764168058\testng-customsuite.xml
			//  : D:\workspace\ElasticTest\testng.xml

			System.out.println("check : " + xml.getName());
			//  : Default test
			*/
			
			String suiteName = context.getCurrentXmlTest().getSuite().getFileName();
			
			//if (elastic && this.classCount > 1) {
			if (!suiteName.contains("customsuite.xml")) {
				StatusLog suiteLog = new StatusLog();
				
				suiteLog.setDepth("suite")
					.setService(service)
					.setSystemOs(os)
					.setJavaVersion(javaVersion)
					.setLibVersion(libVersion)
					.setTestNgVersion(testngVersion);
				
				suiteEndTime = LocalDateTime.now().minusHours(9);
				long duration = Duration.between(suiteStartTime, suiteEndTime).toMillis();
				suiteLog.setStartTime(suiteStartTime.toString())
					.setDuration((int) duration);
						
				if ((DeviceInfo.getDeviceName() != null)) {
					this.setAppLog(suiteLog);	
				}
				this.setDriverLog(suiteLog);
				
				//int all = context.getAllTestMethods().length;
				int passed = context.getPassedTests().size();
				int failed = context.getFailedTests().size();
		    	int skipped = context.getSkippedTests().size();
		    	int all = passed + failed + skipped;
		    	double passRate = passed / (double) all;
		    	
		    	suiteLog.setResultSuiteRate(passRate)
		    		.setResultSuitePassed(passed)
		    		.setResultSuiteFailed(failed)
		    		.setResultSuiteSkipped(skipped);

		    	// add debug log
		    	if (passRate > 1 || passRate < 0) {
			    	suiteLog.setDebugLog("all : " + all + " / passed: " + passed + " / failed : " + failed + " / skipped : " + skipped);
		    	}
				
		    	SendLog.sendStatusLog(SendLog.ES_SUITE_URL, suiteLog);
			}
		}
	}
}
