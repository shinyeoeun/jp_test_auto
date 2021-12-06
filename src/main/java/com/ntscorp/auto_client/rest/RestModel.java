package com.ntscorp.auto_client.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.restassured.http.Cookies;

/**
 * 네이버 아이디로 로그인, 로그인 이후 발생된 쿠키 값을 출력
 */
public class RestModel {
	private static List<io.restassured.http.Cookie> restAssuredCookies = new ArrayList<io.restassured.http.Cookie>();

	/**
	 * 네이버 아이디와 패스워드를 입력 받아 로그인 로그인 쿠키 정보를 저장 및 출력. 쿠키가 이미 저장되어 있을 경우 저장된 쿠키 출력.
	 * 
	 * @param id
	 *        네이버 계정 아이디
	 * @param pw
	 *        네이버 계정 패스워드
	 * @return Cookies 네이버 로그인 쿠키 값 출력
	 */
	public Cookies getCookies(String id, String pw) {
		if (!restAssuredCookies.isEmpty()) {
			return new Cookies(restAssuredCookies);
		}

		WebDriverManager.chromedriver().setup();

		ChromeOptions options = new ChromeOptions();
		options.addArguments("headless");
		WebDriver driver = new ChromeDriver(options);

		driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
		driver.get("https://nid.naver.com");

		driver.findElement(By.id("id")).sendKeys(id);
		driver.findElement(By.id("pw")).sendKeys(pw);
		driver.findElement(By.className("btn_global")).click();

		Set<Cookie> seleniumCookies = driver.manage().getCookies();

		for (Cookie cookie : seleniumCookies)
			restAssuredCookies.add(new io.restassured.http.Cookie.Builder(cookie.getName(), cookie.getValue()).build());

		return new Cookies(restAssuredCookies);
	}

	/**
	 * 네이버 아이디와 패스워드를 입력 받아 로그인 (neoId 로그인) 로그인 쿠키 정보를 저장 및 출력. 쿠키가 이미 저장되어 있을 경우
	 * 저장된 쿠키 출력.
	 * 
	 * @param id
	 *        네이버 계정 아이디
	 * @param pw
	 *        네이버 계정 패스워드
	 * @param social
	 *        로그인 할 Social Media 설정
	 * @return Cookies 네이버 로그인 쿠키 값 출력
	 */
	public Cookies getNeoIdCookies(String id, String pw, SocialMedia social) {
		if (!restAssuredCookies.isEmpty()) {
			return new Cookies(restAssuredCookies);
		}

		WebDriverManager.chromedriver().setup();

		ChromeOptions options = new ChromeOptions();
		options.addArguments("headless");
		WebDriver driver = new ChromeDriver(options);

		driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);

		switch (social) {
		case NAVER:
			naverNeoIdLogin(driver, id, pw);
			break;
		case FACEBOOK:
			facebookNeoIdLogin(driver, id, pw);
			break;
		case LINE:
			lineNeoIdLogin(driver, id, pw);
		case TWITTER:
			twitterNeoIdLogin(driver, id, pw);
		}

		Set<Cookie> seleniumCookies = driver.manage().getCookies();

		for (Cookie cookie : seleniumCookies)
			restAssuredCookies.add(new io.restassured.http.Cookie.Builder(cookie.getName(), cookie.getValue()).build());

		return new Cookies(restAssuredCookies);
	}

	/**
	 * neoId 로그인 (네이버)
	 * 
	 * @param driver
	 * @param id 네이버 계정 아이디
	 * @param pw 네이버 계정 패스워드
	 */ 
	private void naverNeoIdLogin(WebDriver driver, String id, String pw) {
		driver.get("https://nid.naver.com");

		driver.findElement(By.id("id")).sendKeys(id);
		driver.findElement(By.id("pw")).sendKeys(pw);
		driver.findElement(By.className("btn_global")).click();

		driver.get("https://www.vlive.tv/auth/login?loginSvc=naver");
	}

	/**
	 * neoId 로그인 (Facebook)
	 * 
	 * @param driver
	 * @param id Facebook 계정 아이디
	 * @param pw Facebook 계정 패스워드
	 */ 
	private void facebookNeoIdLogin(WebDriver driver, String id, String pw) {
		driver.get("https://www.facebook.com/login.php?");

		driver.findElement(By.id("email")).sendKeys(id);
		driver.findElement(By.id("pass")).sendKeys(pw);
		driver.findElement(By.id("loginbutton")).click();

		driver.get("https://www.vlive.tv/auth/login?loginSvc=facebook");
	}

	/**
	 * neoId 로그인 (Line)
	 *
	 * @param driver
	 * @param id Line 계정 아이디
	 * @param pw Line 계정 패스워드

	private void lineNeoIdLogin(WebDriver driver, String id, String pw) {
		driver.get(
				"https://access.line.me/oauth2/v2.1/login?returnUri=%2Foauth2%2Fv2.1%2Fauthorize%2Fconsent%3Fscope%3Dprofile%26response_type%3Dcode%26state%3DC90AvjeWN_BCKDso%26redirect_uri%3Dhttp%253A%252F%252Fwww.vlive.tv%252Fauth%252Fcallback%26client_id%3D1431797326&loginChannelId=1431797326#/");

		driver.findElement(By.xpath("//*[@class='MdInputTxt01']/input")).sendKeys(id);
		driver.findElement(By.xpath("//*[@class='MdInputTxt01']/input")).sendKeys(pw);
		driver.findElement(By.xpath("//*[@class='MdBtn01']")).click();

		driver.get("https://www.vlive.tv/auth/login?loginSvc=line");
	}
	 */

	/**
	 * neoId 로그인 (Line)
	 *
	 * @param driver
	 * @param id Line 계정 아이디
	 * @param pw Line 계정 패스워드
	 */
	public void lineNeoIdLogin(WebDriver driver, String id, String pw) {
		driver.get(
				"https://access.line.me/oauth2/v2.1/login?loginState=aj5CpsUdWCjJehFiY8i2Av&loginChannelId=1655718137&returnUri=%2Foauth2%2Fv2.1%2Fauthorize%2Fconsent%3Fscope%3Dprofile%2Bemail%2Bopenid%26response_type%3Dcode%26state%3DevckHF6t3LCelBnd%26redirect_uri%3Dhttps%253A%252F%252Fdev-accounts.mysmartstore.jp%252Foauth%252Fcallback%26client_id%3D1655718137#/");

		driver.findElement(By.xpath("//*[@class='MdInputTxt01']/input")).sendKeys(id);
		driver.findElement(By.xpath("//*[@class='MdInputTxt01']/input")).sendKeys(pw);
		driver.findElement(By.xpath("//*[@class='MdBtn01']")).click();

	}

	/**
	 * neoId 로그인 (Twitter)
	 * 
	 * @param driver
	 * @param id Twitter 계정 아이디
	 * @param pw Twitter 계정 패스워드
	 */ 
	private void twitterNeoIdLogin(WebDriver driver, String id, String pw) {
		driver.get("https://twitter.com/login/");

		driver.findElement(By.xpath("//*[@class='js-username-field email-input js-initial-focus']")).sendKeys(id);
		driver.findElement(By.xpath("//*[@class='js-password-field']")).sendKeys(pw);
		driver.findElement(By.xpath("//*[@class='submit EdgeButton EdgeButton--primary EdgeButtom--medium']")).click();

		driver.get("https://www.vlive.tv/auth/login?loginSvc=twitter");
	}

	/**
	 * 저장된 로그인 쿠키 값 제거
	 */
	public void clearCookies() {
		if (!restAssuredCookies.isEmpty()) {
			restAssuredCookies.clear();
		}
	}
}