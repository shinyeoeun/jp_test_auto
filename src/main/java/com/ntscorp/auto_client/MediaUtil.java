package com.ntscorp.auto_client;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

import com.ntscorp.auto_client.appium.AndroidUtil;
import com.ntscorp.auto_client.selenium.ChromeUtil;

public class MediaUtil {

	public ChromeUtil chromeUtil = null;
	public AndroidUtil appiumUtil = null;
	public WebElement mediaElement = null;
	public JavascriptExecutor js = null;
	public By mediaPath = null;
	public boolean isAppium;
	
	public static enum events {
		abort, canplay, canplaythrough, durationchange, emptied, ended, error, 
		loadeddata, loadedmetadata, loadstart, 
		pause, play, playing, progress, ratechange, 
		seeked, seeking, stalled, suspend, timeupdate, volumechange, waiting;
	}
	
	public static enum properties {
		audioTracks, autoplay, buffered, 
		controller, controls, crossOrigin, currrentSrc, currentTime, 
		defaultMuted, defaultPlaybackRate, duration, 
		ended, error, loop, mediaGroup, muted, networkState, 
		paused, playbackRate, played, preload, 
		readyState, seekable, seeking, src, startDate, 
		textTracks, videoTracks, volume
	}
	
	public static enum methods {
		addTextTrack("addTextTrack()"), canPlayType("canPlayType()"), 
									load("load()"), play("play()"), pause("pause()");
		String method;
		
		methods (String method) {
			this.method = method;
		}
		
		public String getValue() {
			return method;
		}
	}
	
	/**
	 * MediaUtil 생성자
	 * 브라우저의 드라이버와 제어할 대상 Media의 Element를 인자로 받음.
	 * 
	 * @param ChromeUtil
	 * @param By 플레이어의 Locator
	 */
	public MediaUtil(ChromeUtil chrome, By xpath) {
		isAppium = false;
		chromeUtil = chrome;
		mediaPath = xpath;
		js = (JavascriptExecutor) chrome;
		this.waitReadyState();
	}
	
	/**
	 * MediaUtil 생성자
	 * Android의 드라이버와 제어할 대상 Media의 Element를 인자로 받음.
	 * 
	 * @param AndroidUtil
	 * @param By 플레이어의 Locator
	 */
	public MediaUtil(AndroidUtil appium, By xpath) {
		isAppium = true;
		appiumUtil = appium;
		mediaPath = xpath;
		js = (JavascriptExecutor) appium;
		this.waitReadyState();
	}
	
	/**
	 * Media 설정 후 Media의 readyState값을 통해 제어 준비 확인 메소드
	 */
	public void waitReadyState() {
		for (int i = 0 ; i < 10 ; i ++) {
			try {				
				if(isAppium == false){
					mediaElement = chromeUtil.getElement(mediaPath);					
				}else {
					mediaElement = appiumUtil.getElement(mediaPath);					
				}
				
				Object readyState = this.getProperties(properties.readyState);
				System.out.println("readyState : " + readyState.toString());
				break;
			} catch (Exception e) {
				if(isAppium == false){
					chromeUtil.sleep(1);					
				} else {
					appiumUtil.sleep(1);
				}
				System.out.println(e.getLocalizedMessage());
			}	
		}
	}
	
	public void addTextTrack() {
		this.action(methods.addTextTrack);
	}
	
	public void canPlayType() {
		this.action(methods.canPlayType);
	}
	
	/**
	 * 미디어 영상을 새로고침하는 메소드
	 */
	public void load() {
		this.action(methods.load);
	}
	
	/**
	 * 미디어 영상을 재생하는 메소드
	 */
	public void play() {
		this.action(methods.play);
	}
	
	/**
	 * 미디어 영상을 정지하는 메소드
	 */
	public void pause() {
		this.action(methods.pause);
	}
	
	/**
	 * 미디어 영상의 현재 시간을 Setting하는 메소드
	 * 
	 * @param double 이동하고자하는 재생시점
	 */
	public void setTime(double value) {
		Double maxTime = this.getDuration();
		if (value > maxTime) {
			System.out.println("설정 시간 (" + value + ") 이 전체 재생 시간 (" + maxTime + ") 을 초과 하였습니다.");
			value = maxTime;
		}
		String cmd = "currentTime=" + value;
		this.action(cmd);
	}
	
	/**
	 * 미디어 영상의 볼륨을 설정하는 메소드
	 * double형으로 설정가능하며 0 ~ 1까지 설정 가능
	 * 
	 * @param double valume 크기
	 */
	public void setVolume(double value) {
		String cmd = "volume=" + value;
		this.action(cmd);
	}
	
	/**
	 * 미디어 영상의 duration 값을 가져오는 메소드
	 * 
	 * @return double 영상의 전체 길이 (sec)
	 */
	public double getDuration() {
		Double result = (Double) this.getProperties(properties.duration);
		
		return result;
	}
	
	/**
	 * Enum으로 정리되어 있지 않은 정보 또는 기능을 임의로 입력할 수 있음
	 * 
	 * @param String 영상에서 가져올 정보 또는 실행할 기능
	 * @return Object
	 */
	public Object action(String flag) {
		String cmd = "return arguments[0]." + flag;
		Object result = js.executeScript(cmd, mediaElement);
		
		return result;
	}
	
	/**
	 * 미디어 영상에서 제공되는 동작을 실행하는 메소드
	 * Enum으로 정리된 정보 중에 선택할 수 있음
	 * 
	 * @param Enum method 정보
	 * @return Object
	 */
	public Object action(methods method) {
		String cmd = "return arguments[0]." + method.getValue();
		Object result = js.executeScript(cmd, mediaElement);
		
		return result;
	}
	
	/**
	 * 미디어 영상의 event 정보를 가져오는 메소드
	 * Enum으로 정리된 정보 중에 선택할 수 있음
	 * 
	 * @param Enum event 정보
	 * @return Object
	 */
	public Object action(events events) {
		String cmd = "return arguments[0]." + events;
		Object result = js.executeScript(cmd, mediaElement);
		
		return result;
	}
	
	/**
	 * 미디어 영상의 properties 정보를 가져오는 메소드
	 * Enum으로 정리된 정보 중에 선택할 수 있음
	 * 
	 * @param Enum properties 정보
	 * @return Object
	 */
	public Object getProperties(properties prop) {
		String cmd = "return arguments[0]." + prop;
		Object result = js.executeScript(cmd, mediaElement);
		
		return result;
	}
	
	/**
	 * 영상의 광고를 제거하는 메소드
	 * 영상 전체길이에서 0.1sec 이전으로 재생시점을 이동됨
	 * 광고 노출에 대한 분기처리가 필요함
	 */
	public void skipAdvertisement() {
		String cmd = "return arguments[0].currentTime = arguments[0].duration - 0.1";
		js.executeScript(cmd, mediaElement);
	}
}
