package com.ntscorp.auto_client;

import static org.testng.Assert.fail;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.browserup.bup.BrowserUpProxy;
import com.browserup.bup.BrowserUpProxyServer;
import com.browserup.bup.client.ClientUtil;
import com.ntscorp.auto_client.appium.AdbCmd;
import com.ntscorp.auto_client.appium.AndroidEventChecker;
import com.ntscorp.auto_client.appium.AndroidUtil;
import com.ntscorp.auto_client.appium.ApkManager;
import com.ntscorp.auto_client.appium.AppiumServer;
import com.ntscorp.auto_client.appium.IOSUtil;
import com.ntscorp.auto_client.data.EmulatorList;
import com.ntscorp.auto_client.getset.DeviceInfo;
import com.ntscorp.auto_client.getset.TestInfo;
import com.ntscorp.auto_client.selenium.ChromeUtil;

import io.appium.java_client.remote.AndroidMobileCapabilityType;
import io.appium.java_client.remote.MobileCapabilityType;
import io.github.bonigarcia.wdm.WebDriverManager;

public class Automation {
	private DesiredCapabilities capability = new DesiredCapabilities();
	private ChromeOptions chromeOption = new ChromeOptions();
	private AdbCmd cmd = new AdbCmd();
	private AndroidEventChecker eventChecker = new AndroidEventChecker();

	private AppiumServer appiumServer = null;

	protected static IOSUtil ios = null;
	protected static AndroidUtil android = null;
	protected static ChromeUtil chrome = null;

	private ArrayList<String> deviceSerials = null;
	
	public static boolean clientStarted = false;

	private static boolean isInstalled = false;
	private static boolean isDownloaded = false;

	/**
	 * 자동화 프로젝트의 수행 환경을 PC로 설정하는 메소드
	 * 
	 * @return Automation
	 */
	public Automation pc() {
		TestInfo.setPlatform("PC");
		
		return this;
	}

	/**
	 * 자동화 프로젝트의 수행 환경을 iOS로 설정하는 메소드
	 * 연결되어 있는 iPhone의 정보를 Capability에 Setting
	 * 
	 * @return Automation
	 */
	public Automation iOS() {
		TestInfo.setPlatform("iOS");
		String[] deviceinfo = cmd.runCommand("/usr/local/bin/ideviceinfo -s").split("\n");

		for (int i = 0; i < deviceinfo.length; i++) {
			if (deviceinfo[i].contains("ProductVersion")) {
				DeviceInfo.setOsVersion(deviceinfo[i].split(" ")[1]);
			}
			if (deviceinfo[i].contains("UniqueDeviceID")) {
				DeviceInfo.setUdid(deviceinfo[i].split(" ")[1]);
			}
			if (deviceinfo[i].contains("ProductType")) {
				DeviceInfo.setDeviceName(deviceinfo[i].split(" ")[1]);
			}
		}
		capability.setCapability("platformName", "iOS");
		capability.setCapability(MobileCapabilityType.DEVICE_NAME, DeviceInfo.getDeviceName());
		capability.setVersion(DeviceInfo.getOsVersion());
		capability.setCapability(MobileCapabilityType.UDID, DeviceInfo.getUdid());
		capability.setCapability(MobileCapabilityType.AUTOMATION_NAME, "XCUITest");
		capability.setCapability(MobileCapabilityType.NEW_COMMAND_TIMEOUT, 60 * 2);

		System.out.println("\n===============================================");
		System.out.println(">>> Device Name : " + DeviceInfo.getDeviceName());
		System.out.println(">>> OS Version  : " + DeviceInfo.getOsVersion());
		System.out.println(">>> Device UDID : " + DeviceInfo.getUdid());
		System.out.println("===============================================\n");

		return this;
	}

	/**
	 * 자동화 프로젝트의 수행 환경을 Android로 설정하는 메소드
	 * 연결되어 있는 Android 단말기의 정보를 Capability에 Setting
	 * 연결되어 있는 단말기 중 Device List에서 첫번째로 연결되어 있는 단말기를 Setting
	 * 
	 * @return Automation
	 */
	public Automation android() {
		TestInfo.setPlatform("Android");
		deviceSerials = cmd.getDeviceSerial();

		if (deviceSerials == null || deviceSerials.size() < 1)
			fail("### No connected device.");

		DeviceInfo.setUdid(deviceSerials.get(0));
		DeviceInfo.setDeviceName(cmd.getDeviceModel(DeviceInfo.getUdid()));
		DeviceInfo.setOsVersion(cmd.getDeviceOS(DeviceInfo.getUdid()));
		DeviceInfo.setSdkVersion(cmd.getSDKVersion());

		capability.setCapability("modelName", DeviceInfo.getDeviceName());

		capability.setCapability(MobileCapabilityType.DEVICE_NAME, DeviceInfo.getDeviceName());
		capability.setCapability(MobileCapabilityType.UDID, DeviceInfo.getUdid());
		capability.setCapability(MobileCapabilityType.NEW_COMMAND_TIMEOUT, 60 * 2);

		capability.setCapability(AndroidMobileCapabilityType.DONT_STOP_APP_ON_RESET, true);
		capability.setCapability(AndroidMobileCapabilityType.NATIVE_WEB_SCREENSHOT, true);
		capability.setCapability(AndroidMobileCapabilityType.UNICODE_KEYBOARD, true);
		capability.setCapability(AndroidMobileCapabilityType.RESET_KEYBOARD, true);
		capability.setCapability(AndroidMobileCapabilityType.DEVICE_READY_TIMEOUT, 20);

		capability.setVersion(DeviceInfo.getOsVersion());

		System.out.println("\n===============================================");
		System.out.println(">>> Device Name : " + DeviceInfo.getDeviceName());
		System.out.println(">>> OS Version  : " + DeviceInfo.getOsVersion());
		System.out.println(">>> Device UDID : " + DeviceInfo.getUdid());
		System.out.println("===============================================\n");

		return this;
	}

	/**
	 * 자동화 프로젝트의 수행 환경을 Android로 설정하는 메소드
	 * 연결되어 있는 Android 단말기의 정보를 Capability에 Setting
	 * 연결되어 있는 단말기 중 Device List에서 인자를 통해 자동화를 수행할 단말기를 선택할 수 있음
	 * 
	 * @param int 연결 할 단말기의 순서
	 * @return Automation
	 */
	public Automation android(int deviceNumber) {
		TestInfo.setPlatform("Android");
		deviceSerials = cmd.getDeviceSerial();

		if (deviceSerials == null || deviceSerials.size() < 1)
			fail("### No connected device.");

		DeviceInfo.setUdid(deviceSerials.get(deviceNumber));
		DeviceInfo.setDeviceName(cmd.getDeviceModel(DeviceInfo.getUdid()));
		DeviceInfo.setOsVersion(cmd.getDeviceOS(DeviceInfo.getUdid()));
		DeviceInfo.setSdkVersion(cmd.getSDKVersion());

		capability.setCapability("modelName", DeviceInfo.getDeviceName());

		capability.setCapability(MobileCapabilityType.DEVICE_NAME, DeviceInfo.getDeviceName());
		capability.setCapability(MobileCapabilityType.UDID, DeviceInfo.getUdid());
		capability.setCapability(MobileCapabilityType.NEW_COMMAND_TIMEOUT, 60 * 2);

		capability.setCapability(AndroidMobileCapabilityType.DONT_STOP_APP_ON_RESET, true);
		capability.setCapability(AndroidMobileCapabilityType.NATIVE_WEB_SCREENSHOT, true);
		capability.setCapability(AndroidMobileCapabilityType.UNICODE_KEYBOARD, true);
		capability.setCapability(AndroidMobileCapabilityType.RESET_KEYBOARD, true);
		capability.setCapability(AndroidMobileCapabilityType.DEVICE_READY_TIMEOUT, 20);

		capability.setVersion(DeviceInfo.getOsVersion());

		System.out.println("\n===============================================");
		System.out.println(">>> Device Name : " + DeviceInfo.getDeviceName());
		System.out.println(">>> OS Version  : " + DeviceInfo.getOsVersion());
		System.out.println(">>> Device UDID : " + DeviceInfo.getUdid());
		System.out.println("===============================================\n");

		return this;
	}

	/**
	 * 수행할 자동화 프로젝트의 환경이 Mobile Web인 경우 필요한 정보를 Capability에 Setting
	 * iOS, Android, PC 환경이 모두 분기처리 되어있으며 PC인 경우 Nexus_5X 단말기를 디폴트로 선택
	 * 
	 * @return Automation
	 */
	public Automation mobileWeb() {
		TestInfo.setTarget("Web");
		WebDriverManager.chromedriver().setup();

		if (TestInfo.getPlatform().equals("iOS")) {

		} else if (TestInfo.getPlatform().equals("Android")) {
			cmd.closeRunningApp(DeviceInfo.getUdid(), "com.android.chrome");
			TestInfo.setApkPackage("com.android.chrome");

			capability.setCapability("chromedriverExecutable", WebDriverManager.chromedriver().getBinaryPath());
			capability.setCapability(MobileCapabilityType.BROWSER_NAME, "chrome");
			capability.setCapability(MobileCapabilityType.PLATFORM_NAME, "android");
			capability.setCapability(MobileCapabilityType.UNEXPECTED_ALERT_BEHAVIOUR,
				UnexpectedAlertBehaviour.IGNORE);
			
		} else if (TestInfo.getOs().equals("PC")) {
			TestInfo.setPlatform("Mobile");
			
			String tamp = EmulatorList.Nexus_5X.name().replaceAll("_", " ");
			Map<String, String> mobileEmulation = new HashMap<String, String>();
			mobileEmulation.put("deviceName", tamp);

			capability.setCapability(CapabilityType.TAKES_SCREENSHOT, true);
			capability.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.IGNORE);
			chromeOption.setExperimentalOption("mobileEmulation", mobileEmulation);

			capability.setCapability(ChromeOptions.CAPABILITY, chromeOption);
		}

		return this;
	}

	/**
	 * Chrome을 이용한 Mobile Web 자동화를 진행하는 경우 필요한 정보를 Capability에 Setting
	 * 자동화를 수행 할 emulator를 인자를 통해 설정할 수 있음.
	 * 
	 * @param EmulatorList Enum으로 정리된 emulator 중 선택
	 * @return Automation
	 */
	public Automation mobileWeb(EmulatorList emulator) {
		TestInfo.setPlatform("Mobile");
		TestInfo.setTarget("Web");
		WebDriverManager.chromedriver().setup();

		String tamp = emulator.name().replaceAll("_", " ");
		Map<String, String> mobileEmulation = new HashMap<String, String>();
		mobileEmulation.put("deviceName", tamp);

		capability.setCapability(CapabilityType.TAKES_SCREENSHOT, true);
		capability.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.IGNORE);
		chromeOption.setExperimentalOption("mobileEmulation", mobileEmulation);

		capability.setCapability(ChromeOptions.CAPABILITY, chromeOption);

		return this;
	}

	/**
	 * 수행할 자동화 프로젝트의 환경이 Mobile App인 경우 필요한 정보를 Capability에 Setting
	 * iOS, Android 환경이 분기처리 되어있음
	 * 
	 * @return Automation
	 */
	public Automation mobileApp() {
		TestInfo.setTarget("App");

		if (TestInfo.getPlatform().equals("iOS")) {
			
		} else if (TestInfo.getPlatform().equals("Android")) {
			// uiautomator 2.0은 android 5 이상에서만 지원
			if (DeviceInfo.getSdkVersion() > 20)
				capability.setCapability(MobileCapabilityType.AUTOMATION_NAME, "UIAutomator2");
		} else {
			fail("### No selected OS.");
		}

		return this;
	}

	/**
	 * PC 브라우저의 자동화를 진행할 경우 수행 브라우저를 Chrome으로 선택하는 메소드
	 * 
	 * @return Automation
	 */
	public Automation chrome() {
		TestInfo.setTarget("Web");
		WebDriverManager.chromedriver().setup();

		// Enable Flash
		Map<String, Object> prefs = new HashMap<>();
		prefs.put("profile.default_content_setting_values.plugins", 1);
		prefs.put("profile.content_settings.plugin_whitelist.adobe-flash-player", 1);
		prefs.put("profile.content_settings.exceptions.plugins.*,*.per_resource.adobe-flash-player", 1);
		chromeOption.setExperimentalOption("prefs", prefs);

		capability.setCapability(CapabilityType.TAKES_SCREENSHOT, true);
		capability.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.IGNORE);

		return this;
	}

	/**
	 * 제공되고 있는 메소드를 통해 설정이 불가능한 Capability를 추가로 설정할 수 있는 메소드
	 * 기존 Capability Setting과 동일하게 key와 value로 설정 가능
	 * 
	 * @param String 설정할 Capability의 key값
	 * @param Object  Capability의 value값
	 * @return Automation
	 */
	public Automation setCapability(String key, Object value) {
		capability.setCapability(key, value);

		return this;
	}

	/**
	 * Chrome 브라우저의  Headless기능 Setting
	 * 
	 * @return Automation
	 */
	public Automation useHeadless() {
		chromeOption.addArguments("HEADLESS");
		TestInfo.setHeadless(true);

		return this;
	}

	/**
	 * EventChecker의 사용여부를 설정해주는 메소드
	 * 
	 * @return Automation
	 */
	public Automation useEventChecker() {
		TestInfo.setEventChecker(true);

		return this;
	}
	
	public Automation useResourceMeasurement() {
		TestInfo.setResourceMeasurement(true);
		
		return this;
	}

	/**
	 * 브라우저 자동화를 수행하는 경우 첫 진입 URL을 설정해주는 메소드
	 * 
	 * @param String mainURL
	 * @return Automation
	 */
	public Automation mainUrl(String mainURL) {
		TestInfo.setServiceMainUrl(mainURL);

		return this;
	}

	/**
	 * 앱 자동화를 수행할 경우 타겟이되는 앱의 패키지명을 설정해주는 메소드
	 * 
	 * @param String packageName
	 * @return Automation
	 */
	public Automation packageName(String packageName) {
		TestInfo.setApkPackage(packageName);

		if (TestInfo.getTarget().equals("iOSApp")) {
			capability.setCapability("app", packageName);
		} else if (TestInfo.getTarget().equals("AndroidApp")) {
			capability.setCapability(AndroidMobileCapabilityType.APP_PACKAGE, packageName);
		}

		return this;
	}

	/**
	 * 앱 자동화를 수행할 경우 타겟이되는 앱의 엑티비티명을 설정해주는 메소드
	 * 
	 * @param String activityName
	 * @return Automation
	 */
	public Automation activityName(String activityName) {
		TestInfo.setApkActivity(activityName);
		if (TestInfo.getTarget().equals("AndroidApp")) {
			capability.setCapability(AndroidMobileCapabilityType.APP_ACTIVITY, activityName);
		}

		return this;
	}

	private void installAPK() {
		TestInfo.setInstallAPK(true);
		if (!isInstalled) {
			System.out.println(">>> Install the APK file.");

			TestInfo.setApkName(ApkManager.getNewestFile(TestInfo.getApkDir()).getName());
			File appDir = new File(TestInfo.getApkDir());
			File app = new File(appDir, TestInfo.getApkName());

			capability.setCapability(MobileCapabilityType.APP, app.getAbsolutePath());
			isInstalled = true;
		} else {
			capability.setCapability(MobileCapabilityType.NO_RESET, true);
		}
	}
	
	/**
	 * 앱 자동화를 수행할 경우 APK를 설치해야할 때 사용하는 메소드
	 * 자동화를 수행할 APK의 Path를 통해 앱을 단말기에 설치한다.
	 * 
	 * @param String 설치할 apk의 경로
	 * @return Automation
	 */
	public Automation apkFile(String apkPath) {
		File apk = new File(apkPath);
		TestInfo.setApkDir(apk.getParent());
		TestInfo.setApkName(apk.getName());

		this.installAPK();

		return this;
	}

	/**
	 * APK가 server에 등록되어 있는 경우 URL을 통해 설치해주는 메소드
	 * APK를 다운로드한 후 단말기에 설치해 준다. APK는 PC의 C:\\apkDownload 경로에 설치됨
	 * 
	 * @param String APk를 받아올 URL
	 * @return Automation
	 */
	public Automation apkUrl(String address) {
		URL url = null;

		try {
			url = new URL(address);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		if (!isDownloaded) {
			System.out.println(">>> Download the APK file.");
			ApkManager.downloadApkFile(url);
			isDownloaded = true;
		}
		this.installAPK();
		
		return this;
	}

	/**
	 * Appium의 loglevel을 설정해주는 메소드
	 * debug, error 중 선택가능
	 * 
	 * @param String loglevel
	 * @return Automation
	 */
	public Automation logLevel(String loglevel) {
		TestInfo.setLogLevel(loglevel);

		return this;
	}
	
	/**
	 * 스크린샷 캡처할 level을 설정하는메소드
	 * String으로 파라미터를 받으면 all/failed 중 선택 가능
	 * failed의 경우 메소드 결과가 fail인 경우에만 캡처됨
	 * 3
	 * @param screenShotLevel
	 * @return
	 */
	public Automation screenshotLever(String screenShotLevel) {
		TestInfo.setScreenShotLevel(screenShotLevel);
		
		return this;
	}
	
	/**
	 * 호스트를 설정하는 메소드
	 * 일반적인 호스트 설정과 동일하게 사용 가능
	 * 
	 * @param host
	 * @param overrideHost
	 * @return
	 */
	public Automation remapHost(String host, String overrideHost) {
		TestInfo.setHost(true);
		TestInfo.setHost(host);
		TestInfo.setOverrideHost(overrideHost);

		return this;
	}
	
	/**
	 * Mac에서 Android 자동화를 진행하는 경우 필요
	 * ADB가 설치된 경로를 입력하여 사용가능
	 * 
	 * @param adbPath
	 * @return
	 */
	public Automation setADBPath(String adbPath) {
		TestInfo.setAdbPath(adbPath);
		
		return this;
	}

	/**
	 * 자동화 설정 후 각 Driver를 실행하는 메소드
	 * Android, iOS, PC 모두 분기처리 되어있으면 Object를 return하여 각 Drvier에 맞게 형변환이 필요함.
	 * 
	 * @return Object 각 Driver 정보를 가지고있는 Object
	 */
	public Object start() {
		//this.stop();

		if (TestInfo.getPlatform().equals("iOS") || TestInfo.getPlatform().equals("Android")) {
			appiumServer = new AppiumServer(TestInfo.getLogLevel());
			appiumServer.start();
		}

		Runnable r = new startAppiumClient();
		Thread t = new Thread(r);

		if (TestInfo.getPlatform().equals("iOS")) {
			ios = new IOSUtil(AppiumServer.getAppiumUrl(), capability);

			if (TestInfo.getTarget().equals("App")) {
				//				TestInfo.setApkPackage((String) appium.getCapabilities().getCapability("app"));
			} else if (TestInfo.getTarget().equals("Web")) {
				ios.get(TestInfo.getServiceMainUrl());
			}
			return ios;
		} else if (TestInfo.getPlatform().equals("Android")) {
			if (!isInstalled) {
				capability.setCapability(MobileCapabilityType.NO_RESET, true);
			}

			cmd.runCommand("adb -s " + DeviceInfo.getUdid() + " shell input keyevent 3", false);
			unlock();
			cmd.runCommand("adb -s " + DeviceInfo.getUdid() + " shell input keyevent 3", false);

			if (TestInfo.isEventChecker()) {
				if (cmd.isAppInstalled("com.nhn.android.eventchecker")) {
					if (!eventChecker.isLatestVersion()) {
						eventChecker.installEventChecker();
						eventChecker.startEventCheckerApp();
						if (DeviceInfo.getSdkVersion() > 22) {
							eventChecker.permissionGrant();
						}
					} else {
						cmd.runCommand("adb shell am force-stop com.nhn.android.eventchecker", false);
					}
				} else {
					eventChecker.installEventChecker();
					eventChecker.startEventCheckerApp();
					if (DeviceInfo.getSdkVersion() > 22) {
						eventChecker.permissionGrant();
					}
				}
				eventChecker.checkNotificationAccessGrant();
			}

			t.start();
			try {
				android = new AndroidUtil(AppiumServer.getAppiumUrl(), capability);
				t.interrupt();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}

			if (TestInfo.getTarget().equals("App")) {
				TestInfo.setApkPackage((String)android.getCapabilities().getCapability("appPackage"));
				TestInfo.setApkActivity((String)android.getCapabilities().getCapability("appActivity"));
			} else if (TestInfo.getTarget().equals("Web")) {
				if (TestInfo.getServiceMainUrl() != null) {
					android.get(TestInfo.getServiceMainUrl());
				}
			}

			return android;
		} else if (TestInfo.getPlatform().equals("PC") || TestInfo.getPlatform().equals("Mobile")) {
			if (TestInfo.isHost()) {
				capability.setCapability(CapabilityType.PROXY, this.getSeleniumProxy());
				capability.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true);
			}
			chromeOption.setPageLoadStrategy(PageLoadStrategy.NONE);
			capability.setCapability(ChromeOptions.CAPABILITY, chromeOption);

			chrome = new ChromeUtil(capability);

			if (TestInfo.getServiceMainUrl() != null) {
				chrome.get(TestInfo.getServiceMainUrl());
			}

			System.out.println("\n===============================================");
			System.out.println(">>> Chrome Driver has been executed.");
			System.out.println(">>> Start the " + TestInfo.getPackageNameList().get(TestInfo.getClassSize() - 1) + "."
				+ TestInfo.getClassNameList().get(TestInfo.getClassSize() - 1) + ".Class");
			System.out.println("===============================================\n");

			return chrome;
		}

		return null;
	}

	/**
	 * 자동화 수행 완료 후 Driver를 종료하는 메소드
	 */
	public void stop() {
		if (ios != null) {
			ios.quit();
			capability = new DesiredCapabilities();

			System.out.println("\n===============================================");
			System.out.println(">>> Appium Client has been shut down.");
			System.out.println("===============================================\n");
		}
		if (android != null) {
			android.quit();
			cmd.closeRunningApp(DeviceInfo.getUdid(), TestInfo.getApkPackage());
			capability = new DesiredCapabilities();

			System.out.println("\n===============================================");
			System.out.println(">>> Appium Client has been shut down.");
			System.out.println("===============================================\n");
		}
		if (appiumServer != null) {
			appiumServer.stop();
			appiumServer = null;

			System.out.println("\n===============================================");
			System.out.println(">>> Appium Server has been shut down.");
			System.out.println("===============================================\n");
		}
		if (chrome != null) {
			try {
				chrome.quit();
				System.out.println("\n===============================================");
				System.out.println(">>> Chrome Driver has been shut down.");
				System.out.println("===============================================\n");
			} catch (WebDriverException we) {
				System.out.println("\n===============================================");
				System.out.println(">>> Exception occurred during Chrome shutdown.");
				System.out.println("===============================================\n");
			}
		}
	}

	/**
	 * host.txt 파일에 작성된 정보를 SeleniumProxy 추가
	 * 
	 * @return Proxy
	 */
	private Proxy getSeleniumProxy() {
		Map<String, String> hostRemapping = new HashMap<String, String>();

		try {
			for (int i = 0; i < TestInfo.getHosts().size(); i++) {
				hostRemapping.put(TestInfo.getHosts().get(i), TestInfo.getOverrideHosts().get(i));
			}
		} catch (NullPointerException npe) {
			System.out.println("\n===============================================");
			System.out.println("### No hosts entered for hosts.txt");
			System.out.println("### Please check the host settings.");
			System.out.println("===============================================\n");
			System.exit(0);
		}

		BrowserUpProxy browserUpProxy = new BrowserUpProxyServer();
		browserUpProxy.setTrustAllServers(true);
		browserUpProxy.getHostNameResolver().remapHosts(hostRemapping);
		browserUpProxy.start();

		Proxy seleniumProxy = ClientUtil.createSeleniumProxy(browserUpProxy);

		return seleniumProxy;
	}

	/**
	 * ADB를 이용해 단말기 잠금해제를 동작해주는 메소드
	 * Swipe를 이용해 단말기 잠금을 해제한다.
	 */
	private void unlock() {
		int width = 0;
		int height = 0;
		String command = "adb -s " + DeviceInfo.getUdid() + " shell wm size";
		String physicalSize[] = cmd.runCommand(command, false).split("\n");
		String size[] = physicalSize[0].replace("Physical size: ", "").split("x");

		try {
			width = Integer.parseInt(size[0]);
			height = Integer.parseInt(size[1]);

			cmd.runCommand("adb -s " + DeviceInfo.getUdid() + " shell input touchscreen swipe " + width / 10 + " "
				+ (height - height / 3) + " " + width + " " + (height - height / 3), false);
		} catch (NumberFormatException nfe) {
			System.out.println("\n===============================================");
			System.out.println("### Please check if the device is connected properly.");
			System.out.println("### Automation cannot run when connected as unauthenticated.");
			System.out.println("===============================================\n");
			System.exit(0);
		}
	}

	/**
	 * Appium Client가 실행되는 동아 대기 상태를 확인시켜주는 메소드
	 */
	private class startAppiumClient implements Runnable {
		int i = 0;
		
		@Override
		public void run() {
			clientStarted = false;

			System.out.println("\n===============================================");
			System.out.println(">>> Appium Client is running.");
			while (!clientStarted) {
				System.out.print(".");
				i++;
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					System.out.println("");
				}
			}
			TestInfo.setServerBuildingTime(i);
		}
	}
}
