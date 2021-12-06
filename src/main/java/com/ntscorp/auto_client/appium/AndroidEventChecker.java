package com.ntscorp.auto_client.appium;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.ntscorp.auto_client.getset.DeviceInfo;
import com.ntscorp.auto_client.getset.TestInfo;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.remote.AndroidMobileCapabilityType;
import io.appium.java_client.remote.MobileCapabilityType;

public class AndroidEventChecker {
	private AdbCmd cmd = new AdbCmd();
	private AndroidDriver<WebElement> driver = null;
	
	private String checkPackage = "com.nhn.android.eventchecker";
	private String checkActivity = "com.nhn.android.eventchecker.MainActivity";
	private String apkDirectory = "C:/apkDownload/AndroidEventChecker.apk";

	/**
	 * EventChecker를 설치하기 위한 메소드
	 * 리눅스 서버에 있는 최신버전의 EventChecker를 설치
	 */
	public void installEventChecker() {
		URL url = null;
		try {
			url = new URL("http://10.113.121.47/data/androidEventChecker.apk");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		OutputStream outStream = null;
		InputStream inputStream = null;
		URLConnection urlConnection = null;

		int fileSize = 1024;
		new File("C:/apkDownload").mkdirs();

		try {
			byte[] buf;
			int byteRead;

			urlConnection = url.openConnection();
			inputStream = urlConnection.getInputStream();

			buf = new byte[fileSize];

			outStream = new BufferedOutputStream(
					new FileOutputStream(apkDirectory));

			while ((byteRead = inputStream.read(buf)) != -1) {
				outStream.write(buf, 0, byteRead);
			}
			System.out.println(">>> Android Event Checker Download Completed");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				inputStream.close();
				outStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 자동화 실행 전 EventChecker를 실행하기 위한 메소드
	 */
	public void startEventCheckerApp() {
		DesiredCapabilities capability = new DesiredCapabilities();

		capability.setCapability(MobileCapabilityType.DEVICE_NAME, DeviceInfo.getDeviceName());
		capability.setCapability(MobileCapabilityType.UDID, DeviceInfo.getUdid());
		capability.setCapability(AndroidMobileCapabilityType.APP_PACKAGE, checkPackage);
		capability.setCapability(AndroidMobileCapabilityType.APP_ACTIVITY, checkActivity); //APP_WAIT_ACTIVITY를 사용하는 경우 GUI가 노출됨
		capability.setCapability(MobileCapabilityType.APP, apkDirectory);
		TestInfo.setAppiumUrl(String.format("http://127.0.0.1:%d/wd/hub", TestInfo.getAppiumPortNum()));

		// uiautomator 2.0은 android 5 이상에서만 지원
		if (DeviceInfo.getSdkVersion() > 20)
			capability.setCapability(MobileCapabilityType.AUTOMATION_NAME, "UIAutomator2");
		
		
		driver = new AndroidDriver<WebElement>(AppiumServer.getAppiumUrl(), capability);
		driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
		
	}

	/**
	 * EventChecker에서 필요한 권한을 부여하는 메소드
	 */
	public void permissionGrant() {
		String permissionArr[] = { "CALL_PHONE", "RECEIVE_SMS", "SEND_SMS", "ACCESS_FINE_LOCATION",
				"ACCESS_COARSE_LOCATION", "CAMERA", "READ_EXTERNAL_STORAGE", "WRITE_EXTERNAL_STORAGE",
				"SET_ANIMATION_SCALE", "WRITE_SECURE_SETTINGS" };

		for (String permission : permissionArr) {
			cmd.runCommand("adb -s " + DeviceInfo.getUdid() + " shell pm grant " + checkPackage + " android.permission."
					+ permission);
		}
		driver.quit();
		driver = null;
	}
	
	/**
	 * 
	 * @return
	 */
	public String startServiceCommand() {
		return Integer.parseInt(getDeviceApiLevel(DeviceInfo.getUdid()).trim()) < 26 ? "startservice" : "startforegroundservice" ;
	}

	/**
	 * SDK버전을 가져오는 메소드
	 * 
	 * @param deviceID 디바이스 UDID
	 * @return String SDK버전
	 */
	public String getDeviceApiLevel(String deviceID) {
		return cmd.runCommand("adb -s " + deviceID + " shell getprop ro.build.version.sdk", false);
	}

	/**
	 * EventChecker에서 수집한 로그를 가져오는 메소드
	 * 
	 * @return StringBuffer 발생한 Event에 대한 Log
	 */
	public StringBuffer getEventLog() {
		StringBuffer eventLog = new StringBuffer();
		
		try {
			ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c",
					"adb -s  " + DeviceInfo.getUdid() + " logcat -s EVENT_CHECK -v raw -d");
			builder.redirectErrorStream(true);
			Process p = builder.start();
			BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;

			while ((line = r.readLine()) != null) {
				if (line.contains("---------"))
					continue;

				eventLog.append(line);
				eventLog.append("|");
			}
		} catch (Exception e) {
			e.getStackTrace();
		}
		
		return eventLog;
	}
	
	/**
	 * EventChecker의 최신버전을 확인하기 위한 메소드
	 * 
	 * @return boolean 최신버전 여부
	 */
	public boolean isLatestVersion() {
		String latestVersion = "2.1.2";
		String currentVersion = cmd.runCommand("adb -s " + DeviceInfo.getUdid()
						+ " shell \"dumpsys package com.nhn.android.eventchecker | grep versionName\"", false);

		return currentVersion.contains(latestVersion);
	}

	/**
	 * 노티피케이션 권한 동의 여부를 확인하는 코드
	 * 노티피케이션 권한이 동의되어 있지 않은 경우 동의 유도 문구 출력
	 */
	public void checkNotificationAccessGrant() {
		cmd.runCommand("adb -s " + DeviceInfo.getUdid() + " shell am startservice -a NOTIFICATION_ACCESS_GRANT", false);
		String granted = cmd.runCommand("adb -s " + DeviceInfo.getUdid() + " logcat -s EVENT_NOTIFICATION -d", false);
		if (!granted.contains("Granted")) {
			System.out.println("\n================================================");
			System.out.println("### if you need to check notifications");
			System.out.println("### You should allow EventChecker's notifications manually.");
			System.out.println("================================================\n");
		}
	}
}
