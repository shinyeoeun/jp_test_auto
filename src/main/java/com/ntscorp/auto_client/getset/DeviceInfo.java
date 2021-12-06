package com.ntscorp.auto_client.getset;

public class DeviceInfo {
	protected static String udid = null;
	protected static String deviceName = null;
	protected static String osVersion = null;
	protected static int sdkVersion = 0;
	protected static int winX, winY;
	protected static int centerX, centerY;
	protected static int maskTop = 200;
	protected static int maskBottom = 300;

	public static String getUdid() {
		return udid;
	}

	public static void setUdid(String udid) {
		DeviceInfo.udid = udid;
	}

	public static String getDeviceName() {
		return deviceName;
	}

	public static void setDeviceName(String deviceName) {
		DeviceInfo.deviceName = deviceName;
	}

	public static String getOsVersion() {
		return osVersion;
	}

	public static void setOsVersion(String osVersion) {
		DeviceInfo.osVersion = osVersion;
	}

	public static int getSdkVersion() {
		return sdkVersion;
	}

	public static void setSdkVersion(int sdkVersion) {
		DeviceInfo.sdkVersion = sdkVersion;
	}

	public static int getWinX() {
		return winX;
	}

	public static void setWinX(int winX) {
		DeviceInfo.winX = winX;
	}

	public static int getWinY() {
		return winY;
	}

	public static void setWinY(int winY) {
		DeviceInfo.winY = winY;
	}

	public static int getCenterX() {
		return centerX;
	}

	public static void setCenterX(int centerX) {
		DeviceInfo.centerX = centerX;
	}

	public static int getCenterY() {
		return centerY;
	}

	public static void setCenterY(int centerY) {
		DeviceInfo.centerY = centerY;
	}

	public static int getMaskTop() {
		return maskTop;
	}

	public static void setMaskTop(int maskTop) {
		DeviceInfo.maskTop = maskTop;
	}

	public static int getMaskBottom() {
		return maskBottom;
	}

	public static void setMaskBottom(int maskBottom) {
		DeviceInfo.maskBottom = maskBottom;
	}
}
