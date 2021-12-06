package com.ntscorp.auto_client;

import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

import com.ntscorp.auto_client.report.AutomationReport;

public interface Utilities {
	/**
	 * 현재 날짜 및 시간 정보 [yyyy.MM.dd_HH:mm:ss]를 가져오는 메소드
	 * 
	 * @return date [yyyy.MM.dd_HH:mm:ss]
	 * @throws Exception
	 */
	default public String getDate() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat form = new SimpleDateFormat("[yyyy.MM.dd_HH:mm:ss]");
		return form.format(cal.getTime());	
	}

	/**
	 * 최종 리다이렉트 URL 가져오기
	 * 
	 * @param url 리다이렉트 전 최초 URL
	 * @return 최종 리다이렉트된 URL 객체 반환
	 */
	default public URL getRedirectedURL(URL url) {
		try {
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setInstanceFollowRedirects(false);
			con.connect();
			int resCode = con.getResponseCode();

			if (resCode == HttpURLConnection.HTTP_SEE_OTHER || resCode == HttpURLConnection.HTTP_MOVED_PERM
					|| resCode == HttpURLConnection.HTTP_MOVED_TEMP) {
				String Location = con.getHeaderField("Location");
				con.disconnect();
				return getRedirectedURL(new URL(Location));
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return url;
	}
	
	/** 무작위 숫자 생성
	 */
	default public int getRandomNum() {
		return (int)(Math.random() * 1000 + 1);
	}
	
	/** 무작위 숫자 생성 (범위 지정)
	 */
	default public int getRandomNum(int min, int max) {
		return (int)(Math.random() * (max - min)) + min;
	}	
	
	/**
	 * @param int 리턴받을 스트링의 길이
	 * @param boolean 특수문자 포함 여부
	 * @return String 랜덤문자들로 생성된
	 */
	default public String getRandomString(int length, boolean specialLetters) {
		String characters;
		if (specialLetters) {
			characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJLMNOPQRSTUVWXYZ1234567890!@#$%^&*()_+`~=[]{};:',./<>?";
		} else {
			characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJLMNOPQRSTUVWXYZ1234567890";
		}

		StringBuilder result = new StringBuilder();

		for (int i = 0; i < length; i++) {
			Random rand = new Random();
			result.append(characters.charAt(rand.nextInt(characters.length())));
		}

		return result.toString();
	}
	
	/**
	 * 0~9 숫자 중 랜덤으로 임의 숫자를 String형으로 가져오는 메소드
	 * 
	 * @param int 랜덤으로 가져올 숫자의 갯수
	 * @return String 숫자를 String형태로 반환
	 */
	default public String getRandomNumText(int length) {
		String characters = "1234567890";
		StringBuilder result = new StringBuilder();

		while (length > 0) {
			Random rand = new Random();
			result.append(characters.charAt(rand.nextInt(characters.length())));
			length--;
		}
		return result.toString();	
	}
	
	/**
	 * 현재 날짜 및 시간 정보 [yyMMdd]를 가져오는 메소드
	 * 
	 * @return date [yyMMdd]
	 * @throws Exception
	 */
	default public String getSimpleDate() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat form = new SimpleDateFormat("yyMMdd");
		return form.format(cal.getTime());	
	}
	
	/**
	 * 현재 시간을 [yyyyMMddHHmmss] 형식으록 가져오는 매소드
	 * 
	 * @return String [yyyyMMddHHmmss]
	 */
	default public String getTime() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat form = new SimpleDateFormat("yyyyMMddHHmmss");
		
		return form.format(cal.getTime());	
	}

	/**
	 * 현재 시간을 [yyyyMMddHHmmss] 형식으록 가져오는 매소드
	 *
	 * @return String [yyyyMMddHHmmss]
	 */
	default public String getTimeForTrackingNo() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat form = new SimpleDateFormat("yyyyMMdd");

		return form.format(cal.getTime());
	}
	
	/** class name return 메소드
	 */
	default public String printClassName(Object obj) {
        return (obj.getClass().getName());
    }
	
	/**
	 * method name return 메소드
	 */
	default public Method[] printMethodName(Object obj) {
		return (obj.getClass().getMethods());
	}
	
	/**
	 * Sleep 메소드
	 * 
	 * @param double 0.1초 단위로 원하는 시간 입력
	 */
	default public void sleep(double sec) {
		long waitSec = (long) sec * 1000;
		try {
			Thread.sleep(waitSec);
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}
	}
	
	/**
	 * Extends Report에서 작성자명을 수정하는 메소드
	 * 
	 * @param String author
	 */
	default public void assginAuthor(String author) {
		AutomationReport.extentClass.assignAuthor(author);
	}
}
