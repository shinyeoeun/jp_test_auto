package com.ntscorp.auto_client.appium;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Point;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.ntscorp.auto_client.Utilities;
import com.ntscorp.auto_client.data.AutomationTime;
import com.ntscorp.auto_client.getset.DeviceInfo;
import com.ntscorp.auto_client.getset.ReportInfo;
import com.ntscorp.auto_client.getset.TestInfo;

import io.appium.java_client.ios.IOSDriver;

public class IOSUtil extends IOSDriver<WebElement> implements Utilities, AutomationTime {

	public WebDriverWait wait;
	
	public IOSUtil(URL remoteAddress, Capabilities desiredCapabilities) {
		super(remoteAddress, desiredCapabilities);

		System.out.println(">>> Appium Client has been run.");
		System.out.println(">>> [Waiting Time : " + TestInfo.getServerBuildingTime() + " Sec]");
		System.out.println(">>> Start the " + TestInfo.getPackageNameList().get(TestInfo.getClassSize() - 1) + "."
				+ TestInfo.getClassNameList().get(TestInfo.getClassSize() - 1) + ".Class");
		System.out.println("===============================================\n");
		
		wait = new WebDriverWait(this, PAGE_LOAD_TIME_OUT);
		this.setWindowSize();
	}

	/**
	 * 단말기 사이즈 정보를 Setting하는 메소드
	 */
	public void setWindowSize() {
		Dimension winSize = manage().window().getSize();
		
		DeviceInfo.setWinX(winSize.getWidth());
		DeviceInfo.setWinY(winSize.getHeight());
		DeviceInfo.setCenterX((int) (DeviceInfo.getWinX() * 0.5));
		DeviceInfo.setCenterY((int) (DeviceInfo.getWinY() * 0.5));
	}
	
	public void back() {
		super.navigate().back();
		this.waitForPageLoaded();
	}
	
	public void back(int count) {
		for (int i = 0; i < count; i++) {
			back();
		}
	}
	
	public void forward() {
		super.navigate().forward();
		this.waitForPageLoaded();
	}
	
	public void refresh() {
		super.navigate().refresh();
		this.waitForPageLoaded();
	}
	
	/**
	 * Extends Report에 Log를 남기기 위한 메소드
	 * 
	 * @param String logs
	 */
	public void printLog(String logs) {
		System.out.println("[ios] " + logs);

		if (logs.contains("JS error")) {
			logs = logs.replace("-", "=");
			if (ReportInfo.getLogBuf().indexOf(logs) < 0) {
				System.out.println("Last " + logs);
				ReportInfo.setLogBuf("[ios] " + logs);
				ReportInfo.setLogBuf("|");
			}
		} else {
			ReportInfo.setLogBuf("[ios] " + logs);
			ReportInfo.setLogBuf("|");
		}
	}
	
	/**
	 * Element에 설정된 Text를 가져오는 메소드
	 * 
	 * @param locator Text를 가져올 Element의 locator
	 * @return String 설정되어 있는 Text
	 */
	public String getText(By locator) {
		WebElement element = waitForIsElementPresent(locator);
		return element.getText();
	}
	
	/**
	 * locator 정보를 통해 WebElement를 가져오는 메소드
	 * 
	 * @param locator Element의 locator 정보
	 * @return WebElement
	 */
	public WebElement getElement(By locator) {
		WebElement element = null;
		manage().timeouts().implicitlyWait(MIN_WAIT_TIME, TimeUnit.SECONDS);
		
		try {		
			element = locator.findElement((SearchContext) this);
			if (element.isDisplayed()) {
				manage().timeouts().implicitlyWait(MAX_WAIT_TIME, TimeUnit.SECONDS);
				return element;
			}
		} catch (NoSuchElementException e) {
			manage().timeouts().implicitlyWait(MAX_WAIT_TIME, TimeUnit.SECONDS);
			return null;
		}
		manage().timeouts().implicitlyWait(MAX_WAIT_TIME, TimeUnit.SECONDS);
		return null;
	}
	
	/**
	 * locator 정보를 통해 WebElement를 가져오는 메소드
	 * 동일한 locator를 가지는 모든  Element를 리스트형태로 반환
	 * 
	 * @param locator Element의 locator 정보
	 * @return WebElement
	 */
	public List<WebElement> getElements(By locator) {
		List<WebElement> elemList = null;
		manage().timeouts().implicitlyWait(MIN_WAIT_TIME, TimeUnit.SECONDS);
		
		try {		
			elemList = locator.findElements((SearchContext) this);
			manage().timeouts().implicitlyWait(MAX_WAIT_TIME, TimeUnit.SECONDS);
			return elemList;
		} catch (NoSuchElementException e) {
			manage().timeouts().implicitlyWait(MAX_WAIT_TIME, TimeUnit.SECONDS);
			return null;
		}
	}
	
	/**
	 * 해당 Element의 좌표값을 가져오는 메소드
	 * Element가 시작하는 좌표값을 가져오며 배열형태로 가져옴
	 * 
	 * @param locator Element의 locator 정보
	 * @return int[] x=0, y=1
	 */
	public int[] getPoint(By locator) {
		int xy[] = new int[2];
		
		WebElement element = null;
		element = waitForIsElementPresent (locator);
		
		Point point = element.getLocation();
		
		xy[0] = point.getX();
		xy[1] = point.getY();

		return xy;
	}
	
	/**
	 * 해당 Element를 Click하는 메소드
	 * 
	 * @param locator Element의 locator 정보
	 */
	public void click(By locator) {
		WebElement element = waitForIsElementPresent(locator);

		if (TestInfo.getTarget().equals("App")){
			element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
			element.click();
			this.waitForPageLoaded();
		}
	}

	/**
	 * 해당 Element를 Click하는 메소드
	 * 
	 * @param WebElement
	 */
	public void click(WebElement element) {
		if (TestInfo.getTarget().equals("App")) {
			element.click();
			this.waitForPageLoaded();
		}
	}

	/**
	 * 문자 타이핑(locator 기반)
	 * 
	 * @param locator 입력하려는 위치 locator
	 * @param inputText 입력하려는 문자열
	 */
	public void type(By locator, String inputText) {
		WebElement element = waitForIsElementPresent(locator);
		element.sendKeys(inputText);
	}
	
	/**
	 * 문자 타이핑(element 기반)
	 * 
	 * @param locator 입력하려는 위치 element
	 * @param inputText 입력하려는 문자열
	 */
	public void type(WebElement element, String inputText) {
		element.sendKeys(inputText);
	}
	
	/**
	 * 텍스트 박스에 입력되어있는 Text를 Clear하는 메소드
	 * 
	 * @param locator
	 */
	public void clear(By locator) {
		WebElement element = waitForIsElementPresent(locator);
		element.clear();
	}
	
	/**
	 * 해당 Element의 노출 여부를 확인하는 메소드
	 * 노출 여부는 isDisplyed와 isEnabled로 확인
	 * 
	 * @param locator Element의 locator 정보
	 * @return boolean
	 */
	public boolean isElementPresent(By locator) {
		boolean result = false;
		manage().timeouts().implicitlyWait(MIN_WAIT_TIME, TimeUnit.SECONDS);
		
		try {		
			WebElement element = locator.findElement((SearchContext) this);
			if (element.isDisplayed()) {
				manage().timeouts().implicitlyWait(MAX_WAIT_TIME, TimeUnit.SECONDS);
				result = true;
			} else if(element.isEnabled()) {
				manage().timeouts().implicitlyWait(MAX_WAIT_TIME, TimeUnit.SECONDS);
				result = true;
			}
		} catch (NoSuchElementException e) {
			manage().timeouts().implicitlyWait(MAX_WAIT_TIME, TimeUnit.SECONDS);
			return false;
		}
		manage().timeouts().implicitlyWait(MAX_WAIT_TIME, TimeUnit.SECONDS);
		return result;
	}
	
	/**
	 * 해당 Element의 노출 여부를 확인하는 메소드
	 * 노출 여부는 isDisplyed와 isEnabled로 확인
	 * 
	 * @param WebElement
	 * @return boolean
	 */	
	public boolean isElementPresent(WebElement element) {
		boolean result = false;
		manage().timeouts().implicitlyWait(MIN_WAIT_TIME, TimeUnit.SECONDS);
		
		try {
			if (element.isDisplayed()) {
				manage().timeouts().implicitlyWait(MAX_WAIT_TIME, TimeUnit.SECONDS);
				result = true;
			} else if(element.isEnabled()) {
				manage().timeouts().implicitlyWait(MAX_WAIT_TIME, TimeUnit.SECONDS);
				result = true;
			}
		} catch (NoSuchElementException e) {
			manage().timeouts().implicitlyWait(MAX_WAIT_TIME, TimeUnit.SECONDS);
			return false;
		}
		manage().timeouts().implicitlyWait(MAX_WAIT_TIME, TimeUnit.SECONDS);
		return result;
	}

	/**
	 * 해당 Element의 노출 여부를 확인하는 메소드
	 * Element가 노출될 때까지 20초동안 대기
	 * 
	 * @param locator Element의 locator 정보
	 * @return WebElement 노출 여부를 확인하는 Element를 반환
	 */
	public WebElement waitForIsElementPresent(By locator) {
		WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));

		if (element != null) {
			return element;	
		}

		return null;
	}
	
	/**
	 * 해당 Element의 visible 값을 확인하는 메소드
	 * 
	 * @param locator Element의 locator 정보
	 * @return boolean
	 */
	public boolean isVisible(By locator) {
		boolean result = false;
		manage().timeouts().implicitlyWait(MIN_WAIT_TIME, TimeUnit.SECONDS);
		
		try {		
			WebElement element = locator.findElement((SearchContext) this);
			if (element.getAttribute("visible").equals("true")) {
				manage().timeouts().implicitlyWait(MAX_WAIT_TIME, TimeUnit.SECONDS);
				result = true;
			}
		} catch (NoSuchElementException e) {
			manage().timeouts().implicitlyWait(MAX_WAIT_TIME, TimeUnit.SECONDS);
			return false;
		}
		manage().timeouts().implicitlyWait(MAX_WAIT_TIME, TimeUnit.SECONDS);
		return result;
	}
	
	/**
	 * 해당 Element의 enabled 값을 확인하는 메소드
	 * 
	 * @param locator Element의 locator 정보
	 * @return boolean
	 */
	public boolean isEnabled(By locator) {
		boolean result = false;
		manage().timeouts().implicitlyWait(MIN_WAIT_TIME, TimeUnit.SECONDS);
		
		try {		
			WebElement element = locator.findElement((SearchContext) this);
			if (element.getAttribute("enabled").equals("true")) {
				manage().timeouts().implicitlyWait(MAX_WAIT_TIME, TimeUnit.SECONDS);
				result = true;
			}
		} catch (NoSuchElementException e) {
			manage().timeouts().implicitlyWait(MAX_WAIT_TIME, TimeUnit.SECONDS);
			return false;
		}
		manage().timeouts().implicitlyWait(MAX_WAIT_TIME, TimeUnit.SECONDS);
		return result;
	}
	
	/**
	 * 페이지 로딩이 완료될 때까지 최대 20초 대기
	 */
	public void waitForPageLoaded() {
		if (TestInfo.getTarget().equals("App")) {
			manage().timeouts().implicitlyWait(MAX_WAIT_TIME, TimeUnit.SECONDS);
		}
	}
	
	public void scrollDown() {
		Map<String, Object> args = new HashMap<>();
		args.put("direction", "down");
		this.executeScript("mobile: scroll", args);
	}
	
	public void scrollUp() {
		Map<String, Object> args = new HashMap<>();
		args.put("direction", "up");
		this.executeScript("mobile: scroll", args);
	}
	
	public void scrollToTop(By locator) {
		WebElement target = waitForIsElementPresent(locator);
		int startY = target.getLocation().getY();

		Map<String, Object> args = new HashMap<>();
		args.put("duration", 1);
		args.put("fromX", DeviceInfo.getCenterX());
		args.put("fromY", startY);
		args.put("toX", DeviceInfo.getCenterX());
		args.put("toY", 0);
		this.executeScript("mobile: dragFromToForDuration", args);
	}
	
	public void swipeRight() {
		Map<String, Object> args = new HashMap<>();
		args.put("direction", "right");
		this.executeScript("mobile: swipe", args);
	}
	
	public void swipeLeft() {
		Map<String, Object> args = new HashMap<>();
		args.put("direction", "left");
		this.executeScript("mobile: swipe", args);
	}
	
	public void swipeToRight(By locator) {
		WebElement target = waitForIsElementPresent(locator);
		int startX = target.getLocation().getX();
		int targetY = target.getLocation().getY();

		Map<String, Object> args = new HashMap<>();
		args.put("duration", 1);
		args.put("fromX", startX);
		args.put("fromY", targetY);
		args.put("toX", DeviceInfo.getWinX() - 5);
		args.put("toY", targetY);
		this.executeScript("mobile: dragFromToForDuration", args);
	}
	
	public void swipeToLeft(By locator) {
		WebElement target = waitForIsElementPresent(locator);
		int startX = target.getLocation().getX();
		int targetY = target.getLocation().getY();

		Map<String, Object> args = new HashMap<>();
		args.put("duration", 1);
		args.put("fromX", startX);
		args.put("fromY", targetY);
		args.put("toX", 0);
		args.put("toY", targetY);
		this.executeScript("mobile: dragFromToForDuration", args);
	}
	
	public void dragNdrop(By from, By to) {
		WebElement fromEle = findElement(from);
		WebElement toEle = findElement(to);
		
		Map<String, Object> args = new HashMap<>();
		args.put("duration", 1);
		args.put("fromX", fromEle.getLocation().getX());
		args.put("fromY", fromEle.getLocation().getY());
		args.put("toX", toEle.getLocation().getX());
		args.put("toY", toEle.getLocation().getY());
		this.executeScript("mobile: dragFromToForDuration", args);
	}
}
