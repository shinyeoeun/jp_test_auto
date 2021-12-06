package com.ntscorp.auto_client.getset;

import java.sql.Timestamp;
import java.util.ArrayList;

public class TestInfo {
	protected static String os = "";
	protected static String platform = "";
	protected static String target = "";
	protected static String suiteName = null;
	protected static ArrayList<String> packageNameList = new ArrayList<>();
	protected static ArrayList<String> classNameList = new ArrayList<>();
	protected static String methodName = null;
	protected static boolean isInstallAPK = false;
	protected static String apkAddr = null;
	protected static String apkName = null;
	protected static String apkDir = "C:\\apkDownload";
	protected static String apkPackage = null;
	protected static String apkActivity = null;
	protected static int projectID;
	protected static int bootcampPort;
	protected static int appiumPortNum;
	protected static String appiumUrl;
	protected static String serviceMainUrl = null;
	protected static Timestamp startTime = null;
	protected static String screenShotLevel = "failed";
	protected static String logLevel = "error";
	protected static boolean host = false;
	protected static ArrayList<String> hosts = new ArrayList<>();
	protected static ArrayList<String> overrideHosts = new ArrayList<>();
	protected static String adbPath = null;
	protected static boolean isHeadless = false;
	protected static boolean isEventChecker = false;
	protected static int serverBuildingTime = 0;
	protected static boolean useElastic = true;
	protected static boolean isResourceMeasurement = false;

	public static String getOs() {
		return os;
	}

	public static void setOs(String os) {
		TestInfo.os = os;
	}

	public static String getPlatform() {
		return platform;
	}

	public static void setPlatform(String platform) {
		TestInfo.platform = platform;
	}

	public static String getTarget() {
		return target;
	}

	public static void setTarget(String target) {
		TestInfo.target = target;
	}

	public static String getSuiteName() {
		return suiteName;
	}

	public static void setSuiteName(String suiteName) {
		TestInfo.suiteName = suiteName;
	}

	public static ArrayList<String> getPackageNameList() {
		return packageNameList;
	}

	public static void addPackageName(String packageName) {
		TestInfo.packageNameList.add(packageName);
	}

	public static ArrayList<String> getClassNameList() {
		return classNameList;
	}

	public static void addClassName(String className) {
		TestInfo.classNameList.add(className);
	}

	public static int getClassSize() {
		return TestInfo.classNameList.size();
	}

	public static String getMethodName() {
		return methodName;
	}

	public static void setMethodName(String methodName) {
		TestInfo.methodName = methodName;
	}

	public static int getBootcampPort() {
		return bootcampPort;
	}

	public static void setBootcampPort(int bootcampPort) {
		TestInfo.bootcampPort = bootcampPort;
	}

	public static int getAppiumPortNum() {
		return appiumPortNum;
	}

	public static void setAppiumPortNum(int appiumPortNum) {
		TestInfo.appiumPortNum = appiumPortNum;
	}

	public static String getAppiumUrl() {
		return appiumUrl;
	}

	public static void setAppiumUrl(String appiumUrl) {
		TestInfo.appiumUrl = appiumUrl;
	}

	public static int getProjectID() {
		return projectID;
	}

	public static void setProjectID(int projectID) {
		TestInfo.projectID = projectID;
	}
	
	public static String getApkAddr() {
		return apkAddr;
	}

	public static void setApkAddr(String apkAddr) {
		TestInfo.apkAddr = apkAddr;
	}

	public static String getApkName() {
		return apkName;
	}

	public static void setApkName(String apkName) {
		TestInfo.apkName = apkName;
	}

	public static String getApkDir() {
		return apkDir;
	}

	public static void setApkDir(String apkDir) {
		TestInfo.apkDir = apkDir;
	}

	public static String getApkPackage() {
		return apkPackage;
	}

	public static void setApkPackage(String apkPackage) {
		TestInfo.apkPackage = apkPackage;
	}

	public static String getApkActivity() {
		return apkActivity;
	}

	public static void setApkActivity(String apkActivity) {
		TestInfo.apkActivity = apkActivity;
	}

	public static String getServiceMainUrl() {
		return serviceMainUrl;
	}

	public static void setServiceMainUrl(String serviceMainUrl) {
		TestInfo.serviceMainUrl = serviceMainUrl;
	}

	public static Timestamp getStartTime() {
		return startTime;
	}

	public static void setStartTime(Timestamp startTime) {
		TestInfo.startTime = startTime;
	}

	public static String getScreenShotLevel() {
		return screenShotLevel;
	}

	public static void setScreenShotLevel(String screenShot) {
		TestInfo.screenShotLevel = screenShot;
	}

	public static String getLogLevel() {
		return logLevel;
	}

	public static void setLogLevel(String logLevel) {
		TestInfo.logLevel = logLevel;
	}

	public static boolean isHost() {
		return host;
	}

	public static void setHost(boolean isHost) {
		TestInfo.host = isHost;
	}

	public static ArrayList<String> getHosts() {
		return hosts;
	}

	public static void setHost(String host) {
		TestInfo.hosts.add(host);
	}

	public static ArrayList<String> getOverrideHosts() {
		return overrideHosts;
	}

	public static void setOverrideHost(String overrideHost) {
		TestInfo.overrideHosts.add(overrideHost);
	}

	public static String getAdbPath() {
		return adbPath;
	}

	public static void setAdbPath(String adbPath) {
		TestInfo.adbPath = adbPath;
	}
	
	public static boolean isHeadless() {
		return isHeadless;
	}

	public static void setHeadless(boolean isHeadless) {
		TestInfo.isHeadless = isHeadless;
	}

	public static boolean isEventChecker() {
		return isEventChecker;
	}

	public static void setEventChecker(boolean isEventChecker) {
		TestInfo.isEventChecker = isEventChecker;
	}

	public static boolean isInstallAPK() {
		return isInstallAPK;
	}

	public static void setInstallAPK(boolean isInstallAPK) {
		TestInfo.isInstallAPK = isInstallAPK;
	}
	
	public static int getServerBuildingTime() {
		return serverBuildingTime;
	}

	public static void setServerBuildingTime(int serverBuildingTime) {
		TestInfo.serverBuildingTime = serverBuildingTime;
	}
	
	public static void setUseElastic(boolean useEs) {
		TestInfo.useElastic = useEs;
	}
	
	public static boolean getUseElastic() {
		return useElastic;
	}

	public static boolean isResourceMeasurement() {
		return isResourceMeasurement;
	}

	public static void setResourceMeasurement(boolean isMeasureResource) {
		TestInfo.isResourceMeasurement = isMeasureResource;
	}
}
