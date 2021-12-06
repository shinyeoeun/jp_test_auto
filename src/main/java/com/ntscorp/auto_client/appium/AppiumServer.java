package com.ntscorp.auto_client.appium;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.ntscorp.auto_client.getset.TestInfo;

import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import io.appium.java_client.service.local.flags.AndroidServerFlag;
import io.appium.java_client.service.local.flags.GeneralServerFlag;

public class AppiumServer {
	private static AppiumDriverLocalService server;
	public AdbCmd cmd = new AdbCmd();
	
	int bootstrapPort;
	
	/**
	 * Appium 서버를 실행하는 생성자
	 * Mac과 Window OS를 확인하여 각 OS에 맞게 실행
	 * 
	 * @param String logLevel [Debug, Error]
	 */
	public AppiumServer(String logLevel) {
		bootstrapPort = AvailablePortFinder.getAvailablePort();
		
		if (TestInfo.getOs().contains("Mac")) {
			Map<String, String> env = new HashMap<>(System.getenv());
			env.put("PATH", "/usr/local/bin:" + env.get("PATH"));
			
			server = AppiumDriverLocalService.buildService(new AppiumServiceBuilder()
		             .usingDriverExecutable(new File("/usr/local/bin/node"))
		             .withAppiumJS(new File("/usr/local/lib/node_modules/appium/build/lib/main.js"))
		             .withArgument(GeneralServerFlag.LOG_LEVEL, (logLevel.equals("debug") ? "debug" : "error"))
					 .withArgument(AndroidServerFlag.BOOTSTRAP_PORT_NUMBER, Integer.toString(bootstrapPort))
		             .withIPAddress("127.0.0.1")
		             .usingAnyFreePort()
//		             .withLogFile(new File("target/"+ "Automaion"+".log"))
		             .withEnvironment(env));
		 } 
		 else if (TestInfo.getOs().contains("Windows")) {
			 server = AppiumDriverLocalService.buildService(new AppiumServiceBuilder()
					 .withArgument(GeneralServerFlag.LOG_LEVEL, (logLevel.equals("debug") ? "debug" : "error"))
					 .withArgument(AndroidServerFlag.BOOTSTRAP_PORT_NUMBER, Integer.toString(bootstrapPort))
			         .withIPAddress("127.0.0.1")
		             .usingAnyFreePort());
//		             .withLogFile(new File("target/"+ "Automation" +".log")));
		 } 
		 else {
		     System.out.println("### Unspecified OS found, Appium can't run");
		     System.exit(0);
		 }
	}
	
	/**
	 * 생성자에서 생성된 서버를 가져오는 메소드
	 * 
	 * @return AppiumDriverLocalService Server
	 */
	public AppiumDriverLocalService getServer() {
		return server;
	}
	
	public static URL getAppiumUrl() {
		return server.getUrl();
	}
	
	/**
	 * 생성자에서 설정된 Server를 실행하는 메소드
	 */
	public void start() {
		/*if (osName.contains("Mac")) {
			cmd.runCommand("killall node.exe");
		} else if (osName.contains("Windows")) {
			cmd.runCommand("taskkill /f /im node.exe", false);
		}*/
		
		server.start();

		System.out.println ("\n===============================================");
		System.out.println (">>> Appium Server has been run.");
		System.out.println (">>> Server URL : " + server.getUrl());
		System.out.println (">>> Bootstrap Port : " + bootstrapPort);
		System.out.println ("===============================================\n");
	}
	
	/**
	 * 실행된 Server를 종료하는 메소드
	 */
	public void stop() {
		server.stop();
		
		if (TestInfo.getOs().contains("Mac")) {
			cmd.runCommand("killall node.exe");
		} else if (TestInfo.getOs().contains("Windows")) {
			cmd.runCommand("taskkill /f /im node.exe", false);
		}
		
		System.out.println ("\n===============================================");
		System.out.println (">>> Appium Server has been shut down.");
		System.out.println ("===============================================\n");
	}
}
