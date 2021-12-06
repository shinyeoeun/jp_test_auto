package com.ntscorp.auto_client.appium;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.ntscorp.auto_client.getset.DeviceInfo;
import com.ntscorp.auto_client.getset.TestInfo;

public class AdbCmd {
	Map<String, String> devices = new HashMap<String, String>();
	ArrayList<String> deviceSerial = new ArrayList<String>();
	ArrayList<String> deviceModel = new ArrayList<String>();
	
	static String osName = System.getProperty("os.name");

	Process p;

	/**
	 * CMD를 열어 명령어를 입력하는 메소드
	 * 
	 * @param String
	 * @return String
	 */
	public String runCommand(String command) {
		System.out.println("> " + command);
		String allLine = "";
		String line = "";
		try {
			if (osName.contains("Mac")) {
				if (command.contains("adb")) {
					p = new ProcessBuilder(command.replaceAll("adb", TestInfo.getAdbPath()).split(" ")).start();
				} else {
					p = new ProcessBuilder(command.split(" ")).start();
				}
			} else if (osName.contains("Windows")) {
				p = new ProcessBuilder("cmd", "/c", command).start();
			}
			// 외부 프로그램 출력 읽기
			BufferedReader stdOut = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			// "표준 출력"과 "표준 에러 출력"을 출력
			while ((line = stdOut.readLine()) != null) {
				if (line.isEmpty()) {
					break;
				}
				allLine = allLine + "" + line + "\n";
				if (line.contains("Console LogLevel: debug") && line.contains("Complete")) {
					break;
				}
			}
			while ((line = stdError.readLine()) != null) {
				if (line.contains("Console LogLevel: debug") && line.contains("Complete")) {
					break;
				}
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return allLine;
	}

	/**
	 * CMD를 열어 명령어를 입력하는 메소드 Console에 노출여부 옵션
	 * 
	 * @param String,
	 *            boolean
	 * @return String
	 */
	public String runCommand(String command, boolean print) {
		if (print) {
			System.out.println("> " + command);
		}
		String allLine = "";
		String line = "";
		try {
			if (osName.contains("Mac")) {
				if (command.contains("adb")) {
					p = new ProcessBuilder(command.replace("adb", TestInfo.getAdbPath()).split(" ")).start();
				} else {
					p = new ProcessBuilder(command.split(" ")).start();
				}
			} else {
				p = new ProcessBuilder("cmd", "/c", command).start();
			}
			// 외부 프로그램 출력 읽기
			BufferedReader stdOut = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			// "표준 출력"과 "표준 에러 출력"을 출력
			while ((line = stdOut.readLine()) != null) {
				if (line.isEmpty()) {
					break;
				}
				allLine = allLine + "" + line + "\n";
				if (line.contains("Console LogLevel: debug") && line.contains("Complete")) {
					break;
				}
			}
			while ((line = stdError.readLine()) != null) {
				if (line.contains("Console LogLevel: debug") && line.contains("Complete")) {
					break;
				}
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return allLine;
	}

	/**
	 * RunCommand 메소드를 확용하여 ADB를 시작하는 메소드
	 */
	protected void startADB() {
		String output = runCommand("adb start-server", false);
		String[] lines = output.split("\n");

		if (lines[0].contains("internal or external command")) {
			System.out.println("### ANDROID_HOME environment variable is not set.");
			System.exit(0);
		}
	}

	/**
	 * RunCommand 메소드를 활용하여 ADB를 종료하는 메소드
	 */
	protected void stopADB() {
		runCommand("adb kill-server", false);
	}

	/**
	 * 연결되어 있는 Device의 UDID를 리스트 형태로 가져오는 메소드
	 * 
	 * @return ArratList<String>
	 */
	public ArrayList<String> getDeviceSerial() {
		startADB();

		String output = runCommand("adb devices", false);
		String[] lines = output.split("\n");
		if (lines.length <= 1) {
			System.out.println("### No connected device.");
			System.exit(0);
			// stopADB();
			return null;
		} else {
			for (int i = 1; i < lines.length; i++) {
				lines[i] = lines[i].replaceAll("\\s+", "");
				if (lines[i].contains("device")) {
					lines[i] = lines[i].replaceAll("device", "");
					String deviceID = lines[i];
					deviceSerial.add(deviceID);
				} else if (lines[i].contains("unauthorized")) {
					lines[i] = lines[i].replaceAll("unauthorized", "");
					String deviceID = lines[i];
					deviceSerial.add(deviceID);
				} else if (lines[i].contains("offline")) {
					lines[i] = lines[i].replaceAll("offline", "");
					String deviceID = lines[i];
					deviceSerial.add(deviceID);
				}
			}
			return deviceSerial;
		}
	}

	/**
	 * 해당 디바이스의 모뎅명을 가져올 때 사용
	 * 
	 * @param deviceID
	 *            디바이스 UDID
	 * @return String 디바이스 모델명
	 */
	public String getDeviceModel(String deviceID) {
		String deviceModelName = null;
		deviceModelName = runCommand("adb -s " + deviceID + " shell getprop ro.product.model", false).replaceAll("\\W",
				"");
		return deviceModelName;
	}

	/**
	 * 해당 디바이스의 OS 버전을 가져올 때 사용
	 * 
	 * @param deviceID
	 *            디바이스 UDID
	 * @return String 디바이스 OS버전
	 */
	public String getDeviceOS(String deviceID) {
		String deviceOS = null;
		deviceOS = runCommand("adb -s " + deviceID + " shell getprop ro.build.version.release", false).replaceAll("\n",
				"");
		return deviceOS;
	}

	/**
	 * 특정 앱을 종료할 때 사용
	 * 
	 * @param deviceID
	 *            디바이스 UDID
	 * @param appPackage
	 *            종료할 앱의 패키지명
	 */
	public void closeRunningApp(String deviceID, String appPackage) {
		String cmd = "adb -s " + deviceID + " shell am force-stop " + appPackage;
		runCommand(cmd);
	}

	/**
	 * 특정 앱의 데이터를 지울 때 사용
	 * 
	 * @param deviceID
	 *            디바이스 UDID
	 * @param appPackage
	 *            데이터를 지울 앱의 패키지명
	 */
	public void clearAppData(String deviceID, String appPackage) {
		String cmd = "adb -s " + deviceID + " shell pm clear " + appPackage;
		runCommand(cmd);
	}

	/**
	 * 특정 앱의 설치 유무를 확인 할 때 사용
	 * 
	 * @param appPackage
	 *            설치 유무를 확인할 앱의 패키지명
	 * @return boolean 설치 유무
	 */
	public boolean isAppInstalled(String appPackage) {
		String cmd = "adb -s " + DeviceInfo.getUdid() + " shell pm list package " + appPackage;
		String result = runCommand(cmd, false);

		if (result.contains(appPackage))
			return true;

		return false;
	}

	/**
	 * 특정 앱의 실행 유무를 확인 할 때 사용
	 * 
	 * @param deviceID
	 *            디바이스 UDID
	 * @param appPackage
	 *            실행 유무를 확인 할 앱의 패키지명
	 * @return boolean 실행 유무
	 */
	public boolean isAppRunning(String deviceID, String appPackage) {
		String cmd = "adb -s " + deviceID + " shell ps " + appPackage;
		String result = executeCommand(cmd);

		if (result.contains(appPackage))
			return true;

		return false;
	}

	/**
	 * PC에 있는 TestData를 단말기에 넣는 메소드 <br>
	 * 단말기 /storage/emulated/0/AppiumData 경로에 data가 들어감 <br>
	 * pc경로는 '/' 구분자로 설정하여야 합니다.
	 * 
	 * @param PCPath
	 *            String
	 * @param dataName
	 */
	public void pushTestData(String PCPath, String dataName) {
		String cmd = "adb push " + PCPath + "\\" + dataName + " " + "/storage/emulated/0/AppiumData/" + dataName;
		System.out.println(runCommand(cmd));
	}

	/**
	 * 단말기에 있는 자동화 data의 경로를 불러오는 메소드
	 * 
	 * @param dataName
	 * @return dataPath 단말기의 data경로
	 */
	public String getTestDataPath(String dataName) {
		String dataPath = "/storage/emulated/0/AppiumData/" + dataName;
		return dataPath;
	}

	/**
	 * ADB를 이용하여 KeyEvent를 입력할 때 사용
	 * 
	 * @param deviceID
	 *            디바이스 UDID
	 * @param event
	 *            입력하고자하는 Event 번호
	 */
	public void keyEvent(String deviceID, int event) {
		String cmd = "adb -s " + deviceID + " shell input keyevent " + event;
		runCommand(cmd);
	}

	/**
	 * ADB를 이용하여 KeyEvent를 입력할 때 사용
	 * 
	 * @param deviceID
	 *            디바이스 UDID
	 * @param event
	 *            입력하고자하는 Event
	 */
	public void keyEvent(String deviceID, String event) {
		String cmd = "adb -s " + deviceID + " shell input keyevent " + event;
		runCommand(cmd);
	}

	/**
	 * Appium Settings앱을 활용한 단말기 잠금해제 단말기 잠금해제를 할 때 사용하며 잠금패턴, 비밀번호등이 없어야 사용가능
	 * 
	 * @param deviceID
	 *            디바이스 UDID
	 */
	public void unLock(String deviceID) {
		String cmd = "adb -s " + deviceID + " shell am start -n io.appium.unlock/.Unlock";
		runCommand(cmd);
	}

	/**
	 * Node 서버를 종료할 때 사용
	 */
	protected void nodeTaskKill() {
		String cmd = "taskkill /f /im node.exe";
		runCommand(cmd, false);

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 프롬프트를 이용하여 Command입력할 때 사용
	 * 
	 * @param command
	 *            입력할 Command
	 * @return String 출력되는 Log
	 */
	public String executeCommand(String command) {
		String logs = null;
		String temp = null;
		System.out.println("> command : " + command);
		try {
			Process p = Runtime.getRuntime().exec(command);
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while ((temp = stdInput.readLine()) != null) {
				temp = new String(temp.getBytes(), "UTF-8");
				logs = logs + "\n" + temp;
				// System.out.println(logs);
			}
		} catch (UnsupportedEncodingException uee) {
			uee.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return logs;
	}

	/**
	 * 연결된 단말기의 SDK버전을 가져올 때 사용
	 * 
	 * @return int SDK버전
	 */
	public int getSDKVersion() {
		int version = 0;

		String cmd = "adb -s " + DeviceInfo.getUdid() + " shell getprop ro.build.version.sdk";
		version = Integer.parseInt(runCommand(cmd, false).replace("\n", ""));

		return version;
	}

	/**
	 * Appium을 사용할 때 설치되어 있는 Appium버전 확인 Appium의 저버전 사용을 방지하기 위한 메소드
	 */
	public void checkAppiumVersion() {
		String cmd = "appium -v";
		int version = 0;
		try {
			version = Integer.parseInt(runCommand(cmd, false).replaceAll("[.]", "").replaceAll("\n", ""));
		} catch (NumberFormatException nfe) {
			System.out.println("\n================================================");
			System.out.println("### Please check if Appium is installed.");
			System.out.println("### npm install -g appium");
			System.out.println("### If the installation is normal, please reboot the PC and re-run.");
			System.out.println("================================================\n");
			System.exit(0);
		}
		if (version < 160) {
			System.out.println("\n===============================================");
			System.out.println("### Appium is a lower version.");
			System.out.println("### Please update to version 1.6.0 or higher.");
			System.out.println("===============================================\n");
			System.exit(0);
		}
	}

	/**
	 * JDK 환경 변수 설정이 되어있는지 확인하기 위한 메소드
	 */
	public void checkJDKEnvironmentVariable() {
		String java = "%JAVA_HOME%";
		if (runCommand("echo " + java, false).equals(java)) {
			System.out.println("\n===============================================");
			System.out.println(">>> JAVA_HOME environment variable is not set.");
			System.out.println("===============================================\n");
			System.exit(0);
		}
	}

	/**
	 * SDK 환경 변수 설정이 되어있는지 확인하기 위한 메소드
	 */
	public void checkSDKEnvironmentVariable() {
		String android = "%ANDROID_HOME%";
		if (runCommand("echo " + android, false).equals(android)) {
			System.err.println("\n===============================================");
			System.err.println("### ANDROID_HOME environment variable is not set.");
			System.err.println("===============================================\n");
			System.exit(0);
		}
	}
}