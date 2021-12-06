package com.ntscorp.auto_client.elastic;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFilter;

@JsonFilter("depthFilter")
//@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StatusLog {
	// info
	@JsonProperty("info.os")
	public String systemOs;

	public StatusLog setSystemOs(String systemOs) {
		this.systemOs = systemOs;
		return this;
	}

	@JsonProperty("info.depth")
	public String depth;

	public StatusLog setDepth(String depth) {
		this.depth = depth;
		return this;
	}

	// version
	@JsonProperty("version.library")
	public String libVersion;

	public StatusLog setLibVersion(String libVersion) {
		this.libVersion = libVersion;
		return this;
	}

	@JsonProperty("version.java")
	public String javaVersion;

	public StatusLog setJavaVersion(String javaVersion) {
		this.javaVersion = javaVersion;
		return this;
	}
	
	@JsonProperty("version.testng")
	public String testngVersion;

	public StatusLog setTestNgVersion(String testngVersion) {
		this.testngVersion = testngVersion;
		return this;
	}

	// name
	@JsonProperty("name.service")
	public String service;

	public StatusLog setService(String service) {
		this.service = service;
		return this;
	}

	@JsonProperty("name.package")
	public String packageName;

	public StatusLog setPackageName(String packageName) {
		this.packageName = packageName;
		return this;
	}

	@JsonProperty("name.class")
	public String className;

	public StatusLog setClassName(String className) {
		this.className = className;
		return this;
	}

	@JsonProperty("name.method")
	public String methodName;

	public StatusLog setMethodName(String methodName) {
		this.methodName = methodName;
		return this;
	}

	@JsonProperty("name.description")
	public String description;

	public StatusLog setDescription(String description) {
		this.description = description;
		return this;
	}

	// time
	@JsonProperty("time.start")
	public String stime;

	public StatusLog setStartTime(String stime) {
		this.stime = stime;
		return this;
	}

	@JsonProperty("time.elapsed")
	public int duration;

	public StatusLog setDuration(int duration) {
		this.duration = duration;
		return this;
	}

	// log
	@JsonProperty("log.exception")
	public String exception;

	public StatusLog setException(String exception) {
		this.exception = exception;
		return this;
	}

	@JsonProperty("log.error")
	public String error;

	public StatusLog setError(String error) {
		this.error = error;
		return this;
	}

	@JsonProperty("log.debug")
	public String debugLog;

	public StatusLog setDebugLog(String debugLog) {
		this.debugLog = debugLog;
		return this;
	}

	// device
	@JsonProperty("device.name")
	public String deviceName;

	public StatusLog setDeviceName(String deviceName) {
		this.deviceName = deviceName;
		return this;
	}

	@JsonProperty("device.os")
	public String deviceOs;

	public StatusLog setDeviceOs(String deviceOs) {
		this.deviceOs = deviceOs;
		return this;
	}

	@JsonProperty("device.id")
	public String deviceId;

	public StatusLog setDeviceId(String deviceId) {
		this.deviceId = deviceId;
		return this;
	}

	@JsonProperty("device.sdk")
	public int deviceSdk;

	public StatusLog setDeviceSdk(int deviceSdk) {
		this.deviceSdk = deviceSdk;
		return this;
	}

	// result -> method
	@JsonProperty("result")
	public String resultMethod;

	public StatusLog setResultMethod(String resultMethod) {
		this.resultMethod = resultMethod;
		return this;
	}

	// result -> class
	@JsonProperty("result.class.passed")
	public int resultClassPassed;

	public StatusLog setResultClassPassed(int resultClassPassed) {
		this.resultClassPassed = resultClassPassed;
		return this;
	}

	@JsonProperty("result.class.failed")
	public int resultClassFailed;

	public StatusLog setResultClassFailed(int resultClassFailed) {
		this.resultClassFailed = resultClassFailed;
		return this;
	}

	@JsonProperty("result.class.skipped")
	public int resultClassSkipped;

	public StatusLog setResultClassSkipped(int resultClassSkipped) {
		this.resultClassSkipped = resultClassSkipped;
		return this;
	}

	@JsonProperty("result.class.rate")
	public double resultClassRate;

	public StatusLog setResultClassRate(double resultClassRate) {
		this.resultClassRate = resultClassRate;
		return this;
	}

	// result -> suite
	@JsonProperty("result.suite.passed")
	public int resultSuitePassed;

	public StatusLog setResultSuitePassed(int resultSuitePassed) {
		this.resultSuitePassed = resultSuitePassed;
		return this;
	}

	@JsonProperty("result.suite.failed")
	public int resultSuiteFailed;

	public StatusLog setResultSuiteFailed(int resultSuiteFailed) {
		this.resultSuiteFailed = resultSuiteFailed;
		return this;
	}

	@JsonProperty("result.suite.skipped")
	public int resultSuiteSkipped;

	public StatusLog setResultSuiteSkipped(int resultSuiteSkipped) {
		this.resultSuiteSkipped = resultSuiteSkipped;
		return this;
	}

	@JsonProperty("result.suite.rate")
	public double resultSuiteRate;

	public StatusLog setResultSuiteRate(double resultSuiteRate) {
		this.resultSuiteRate = resultSuiteRate;
		return this;
	}
	
	//driver
	@JsonProperty("driver.headless")
	public boolean isHeadless;

	public StatusLog setHeadless(boolean isHeadless) {
		this.isHeadless = isHeadless;
		return this;
	}
	
	@JsonProperty("driver.evnetChecker")
	public boolean isEventChecker;

	public StatusLog setEventChecker(boolean isEventChecker) {
		this.isEventChecker = isEventChecker;
		return this;
	}
	
	@JsonProperty("driver.installAPK")
	public boolean isInstallAPK;

	public StatusLog setInstallAPK(boolean isInstallAPK) {
		this.isInstallAPK = isInstallAPK;
		return this;
	}
	
	@JsonProperty("driver.serverBuildingTime")
	public int serverBuildingTime;

	public StatusLog setServerBuildingTime(int serverBuildingTime) {
		this.serverBuildingTime = serverBuildingTime;
		return this;
	}
	
	@Override
	public String toString() {
		String info = "";
		
		info = "Info : " + this.systemOs + " / " + this.depth + "\n"
			+ "Version : " + this.libVersion + " / " + this.javaVersion + " / " + this.testngVersion + "\n"
			+ "Name : " + this.service + " / " + this.packageName + " / " + this.className + this.methodName + " / " + this.description + "\n"
			+ "Time : " + this.stime + " / " + this.duration + "\n"
			+ "Log : " + this.exception + " / " + this.error + " / " + this.debugLog + "\n"
			+ "Device : " + this.deviceName + " / " + this.deviceOs + " / " + this.deviceId + " / " + this.deviceSdk + "\n"
			+ "ResultMethod" + this.resultMethod + "\n"
			+ "ResultClass" + this.resultClassPassed + " / " + this.resultClassFailed + " / " + this.resultClassSkipped + " / " + this.resultClassRate + "\n"
			+ "ResultSuite" + this.resultSuitePassed + " / " + this.resultClassFailed + " / " + this.resultSuiteSkipped + " / " + this.resultSuiteRate + "\n"
			+ "Driver" + this.isHeadless + " / " + this.isEventChecker + " / " + this.isInstallAPK + " / " + this.serverBuildingTime;
		
		return info;
	}
}