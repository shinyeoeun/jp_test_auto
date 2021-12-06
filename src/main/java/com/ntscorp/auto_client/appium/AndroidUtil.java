package com.ntscorp.auto_client.appium;

import io.appium.java_client.TouchAction;
import io.appium.java_client.android.Activity;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.touch.offset.ElementOption;
import io.appium.java_client.touch.offset.PointOption;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.HasTouchScreen;
import org.openqa.selenium.interactions.TouchScreen;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteTouchScreen;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.ntscorp.auto_client.Formatter;
import com.ntscorp.auto_client.MediaUtil;
import com.ntscorp.auto_client.Utilities;
import com.ntscorp.auto_client.data.AutomationTime;
import com.ntscorp.auto_client.data.DeviceSize;
import com.ntscorp.auto_client.getset.DeviceInfo;
import com.ntscorp.auto_client.getset.ReportInfo;
import com.ntscorp.auto_client.getset.TestInfo;
import com.ntscorp.auto_client.report.AutomationReport;

import static com.ntscorp.auto_client.verity.Verify.verifyTrue;
import static org.testng.Assert.*;

public class AndroidUtil extends AndroidDriver<WebElement> implements TakesScreenshot, HasTouchScreen, Utilities, AutomationTime{	
	public RemoteTouchScreen touch;
	public TouchAction action;
	public WebDriverWait wait;
	public DeviceSize device;
	public MediaUtil media = null;
	private AdbCmd cmd = new AdbCmd();

	public int avoidTop, avoidBottom;
	
	public String mainWindowHandle = null;
	public String newWindowHandle = null;
	public String beforeWindowHandle = null;
	public List<String> windowHandles = new ArrayList<String>();

	private static Set<Cookie> cookieSet = null;

	public int HTTPStatusCode = 0;
	public String HTTPStatusMessage = null;

	public Activity mainActivity = null;
	
	/**
	 * AndroidUtil의 생성자
	 * AndroidUtil에서 필요한 객체를 초기화하고 단말기 unlock을 실행
	 * 앱 실행 후 단말기 정보를 확인하고 앱 실행까지 대기
	 * 
	 * @param url HubAddress
	 * @param capability Android Driver의 Capability
	 * @throws MalformedURLException
	 */
	public AndroidUtil (URL url, DesiredCapabilities capability) throws MalformedURLException { 
		super (url, capability);
		Formatter.clientStarted = true;

		System.out.println(">>> Appium Client has been run.");
		System.out.println(">>> Waiting Time : " + TestInfo.getServerBuildingTime() + " Sec");
		System.out.println(">>> Start the " + TestInfo.getPackageNameList().get(TestInfo.getClassSize() - 1) + "."
				+ TestInfo.getClassNameList().get(TestInfo.getClassSize() - 1) + ".Class");
		
		wait = new WebDriverWait(this, PAGE_LOAD_TIME_OUT);
		touch = new RemoteTouchScreen(getExecuteMethod());
		action = new TouchAction(this);
		device = new DeviceSize();

		if (this.isDeviceLocked()) {
			System.out.println (">>> Enable Device UnLock");
			cmd.unLock(DeviceInfo.getUdid());
		}
		
		this.manage().timeouts().implicitlyWait(MAX_WAIT_TIME, TimeUnit.SECONDS);
		this.getDeviceInfo();
		System.out.println("===============================================\n");
		
		if (TestInfo.getTarget().equals("App")){
			mainActivity = new Activity(TestInfo.getApkPackage(), currentActivity());
		}
	}
	
	/**
	 * 자동화 수행 중 스크린샷을 캡처할 때 사용
	 * 
	 * @param callerName 스크린샷 찍을 당시 메소드명
	 * @param deviceName 스크린샷을 찍은 단말기명
	 * @return String 스크린샷을 찍은 후 해당 이미지의 링크
	 */
	public String takeScreenShot(String callerName, String deviceName) {
		String destDir;
		DateFormat dateFormat;
		destDir = "automation-output//screenshots";
		
		File scrFile = ((TakesScreenshot) this).getScreenshotAs(OutputType.FILE);
		dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");

		new File(destDir).mkdirs();
		String destFile = dateFormat.format(new Date()) + "_" + deviceName + "_" + callerName + ".png";
		String imgLink = "screenshots//" + destFile;
		
		try {
			FileUtils.copyFile(scrFile, new File(destDir + "//" + destFile));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return imgLink;
	}
	
	/**
	 * 동일한 Locator를 가지고 있는 Element 중 랜덤으로 한개의 Element를 선택
	 * 
	 * @param locator Element의 Locator
	 * @return WebElement
	 */
	public WebElement getRandumElement(By locator) {
		this.waitForIsElementPresent(locator);
		List<WebElement> elements = this.findElements(locator);
		
		return elements.get(getRandomNum(0, elements.size() - 1));
	}
	
	/**
	 * 단말기별 화면 해상도 저장 scroll & swipe 동작 시 활용
	 */
	public void setWindowSize() {
		Dimension winSize = manage().window().getSize();
		
		DeviceInfo.setWinX(winSize.getWidth());
		DeviceInfo.setWinY(winSize.getHeight());
		DeviceInfo.setCenterX((int) (DeviceInfo.getWinX() * 0.5));
		DeviceInfo.setCenterY((int) (DeviceInfo.getWinY() * 0.5));
	}
	
	/**
	 * 모바일웹의 경우 단말기 viewport 정보를 저잘할 때 사용
	 * Scroll & Swipe 동작을 위한 정보
	 */
	public void setWebSize() {
		try {
			DeviceInfo.setWinX(device.size.get(DeviceInfo.getDeviceName()).getViewportX());
			DeviceInfo.setWinY((int) (device.size.get(DeviceInfo.getDeviceName()).getViewportY()*0.8));
		} catch (NullPointerException npe) {
			System.out.println(">>> Device not on the device list. Applies default size.");
			DeviceInfo.setWinX(360);
			DeviceInfo.setWinY(560);
		}
		DeviceInfo.setCenterX((int) (DeviceInfo.getWinX() * 0.5));
		DeviceInfo.setCenterY((int) (DeviceInfo.getWinY() * 0.5));
	}
	
	/**
	 * 단말기 해상도 또는  viewport 정보를 가져오기 위한 메소드
	 * 모바일웹은 경우 Context를 Native로 변경한 후 해상도를 가져옴
	 */
	public void getDeviceInfo() {
		if (TestInfo.getTarget().equals("Web")) {
			this.switchToNative();
		}
		this.setWindowSize();
		
		System.out.println(">>> Device screen : X=" + DeviceInfo.getWinX() + " / Y=" + DeviceInfo.getWinY());
		this.avoidTop = 0;
		this.avoidBottom = DeviceInfo.getWinY();
		
		if (TestInfo.getTarget().equals("Web")) {
//			this.setWebSize();
			this.switchToWeb();
		}
	}
	
	/**
	 * 해당 앱의 상하단 고정 영역의 크기를 저장하기 위한 메소드
	 * Scroll 기능을 사용할 때 해당 영역을 제외하기 위해 사용
	 * 
	 * @param top 상단 고정영역의 Locator
	 * @param bottom 하단 고정영역의 Locator
	 */
	public void setMaskArea(By top, By bottom) {
		if(top == null) {
			DeviceInfo.setMaskTop(100);
		} else {
			DeviceInfo.setMaskTop(top.findElement((SearchContext) this).getSize().getHeight() + 100);
		}
		
		if(bottom == null) {
			DeviceInfo.setMaskBottom(300);
		} else {
			DeviceInfo.setMaskBottom(bottom.findElement((SearchContext) this).getSize().getHeight() + 30);
		}
	}

	public WebElement avoidMaskArea(By locator) {
		WebElement target = null;
		int targetY = 0;

		for (int i = 0; i < 10; i++) {
			target = getElement(locator);

			if (target != null) {
				targetY = getPoint(locator)[1];

				if ((targetY > DeviceInfo.getMaskTop()) && (targetY < DeviceInfo.getMaskBottom())) {
					return target;
				}
			}
			scrollDown();
		}
		fail ("[Test Skipped] 선택 가능한 객체를 찾지 못함");
		return null;
	}
	
	/**
	 * 해당 element의 x , y 좌표를 가져오는 메소드
	 * 
	 * @param locator
	 * @return int[] xy좌표
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
	 * locator에 해당하는 xPath 개수를 가져온다.
	 * 
	 * @param locator 확인하려는 xpath의 locator
	 * @return locator에 해당하는 xpath 개수 int로 반환
	 */
	public int getXpathCount(By locator) {
		List<WebElement> elements = waitForIsAllElementsPresent(locator);
		return elements.size();
	}

	/**
	 * 해당 locator의 Text를 가져온다.
	 * 
	 * @param locator Text가 존재하는 Element의 locator
	 * @return 해당 Element에 존재하는 텍스트 String 반환
	 */
	public String getText(By locator) {
		WebElement element = waitForIsElementPresent(locator);
		return element.getText();
	}
	
	/**
	 * 현재 선택된 Select menu의 Text 가져오기
	 * 
	 * @param locator 가져오려는 Select의 locator
	 * @return 현재 선택된 Select menu의 Text
	 */
	public String getSelectedText(By locator) {
		WebElement element = waitForIsElementPresent(locator);
		String selectedText = "";
		
		if (element.getTagName().equals("select")) {
			
			Select select = new Select(element);
			selectedText = select.getFirstSelectedOption().getText();
		} else {
			List<WebElement> lists = element.findElements(By.tagName("li"));
			
			for (WebElement list : lists) {
				if (list.getAttribute("class").endsWith("on")) {
					selectedText = list.getText();
					
					break;
				}
			}
		}
		return selectedText;
	}
	
	/**
	 * toast내의 개행이 있을 경우 띄어쓰기로 대체하여 가져오기 text 값 가져오기
	 * 
	 * @param target
	 *            text contain word
	 * @return String
	 * @throws Exception
	 *             - Selenium Exception
	 */
	public String getToast(String containText) {
		String toast = null;

		try {
			toast = findElement(By.xpath("//*[contains(@text,'" + containText + "')]")).getText();
		} catch (NoSuchElementException nsee) {
			printLog("토스트를 찾지 못했습니다.");
		}

		toast = toast.replaceAll("\n", " ");
		return toast;
	}
	
	/**
	 * 해당 locator의 Attribute 값 가져오기
	 * 
	 * @param locator Attriubute를 확인하려는 locator
	 * @param attribute 확인하려는 Attribute 
	 * @return 해당 Element의 Attribute 값 String 반환
	 */
	public String getAttribute(By locator, String attr) {
		WebElement element = waitForIsElementPresent(locator);
		return element.getAttribute(attr);
	}
	
	/**
	 * 해당 locator 정보의 element의 갯수를 가져오는 메소드
	 * 
	 * @param locator
	 * @return int element 갯수
	 */
	public int getElementCount(By locator) {
		int count = 0;
		WebElement element = null;
		element = waitForIsElementPresent (locator);
		
		if (element != null) {
			count = this.findElements(locator).size();
			return count;	
		}
		return count;
	}
	
	/**
	 * 해당 locator의 element값을 가져오는 메소드
	 * 
	 * @param locator
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
	 * 동일한 locator를 가진 Element를 리스트 형태로 가져오는 메소드
	 * 
	 * @param locator
	 * @return List<WebElement>
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
	 * 특정 Element 하위 Element 중 동일한 locator를 가진 Element를 리스트 형태로 가져오는 메소드
	 * 
	 * @param base 특정 Element
	 * @param locator 하위 Element의 locator
	 * @return List<WebElement>
	 */
	public List<WebElement> getElements(WebElement base, By locator) {
		List<WebElement> elemList = null;
		manage().timeouts().implicitlyWait(MIN_WAIT_TIME, TimeUnit.SECONDS);
		
		try {		
			elemList = base.findElements(locator);
			manage().timeouts().implicitlyWait(MAX_WAIT_TIME, TimeUnit.SECONDS);
			return elemList;
		} catch (NoSuchElementException e) {
			manage().timeouts().implicitlyWait(MAX_WAIT_TIME, TimeUnit.SECONDS);
			return null;
		}
	}
	
	/**
	 * JSOUP을 사용한 모든 링크 검색
	 * 
	 * @throws Exception
	 */
	public HashSet<String> getAllLinks() {
		List<Element> a = new ArrayList<Element>();
		HashSet<String> allURL  = new HashSet<>();
		Document doc = null;

		try {
			doc = Jsoup.connect(getCurrentUrl()).get();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			printLog("\n==============================================");
			printLog(">>> IOException : 해당 url의 정보를 가져오지 못했습니다.");
			printLog("==============================================\n");
		}

		a.addAll(doc.getElementsByTag("a"));
		a.addAll(doc.getElementsByTag("img"));

		for (Element ele : a) {
			if (ele.tagName().equals("a")) {
				allURL.add(ele.absUrl("href"));
			} else {
				allURL.add(ele.attr("src"));
			}
		}
		
		for (Iterator<String> it = allURL.iterator(); it.hasNext();) {
			String value = it.next();

			if (value.equals("")) {
				it.remove();
			}
		}

		return allURL;
	}
	
	/**
	 * 해당 URL로 이동
	 * 
	 * @param url 이동하려는 주소
	 */
	@Override
	public void get(String url) {
		super.get(url);
		
		if(windowHandles.isEmpty()){
			windowHandles.add(getWindowHandle());			
		}
		waitForPageLoaded();
	}
	
	/**
	 * 현재 페이지의 Activity를 가져오는 메소드
	 * 
	 * @return Activity
	 */
	public Activity getActivity() {
		Activity activity = new Activity(TestInfo.getApkPackage(), currentActivity())
				.setIntentAction("android.intent.action.VIEW");
				
		return activity;
	}
	
	/**
	 * 현재 페이지에서 특정페이지로 이동하기 위한 메소드.
	 * app의 경우 activity이동 , web은 url이동
	 */
	public void goTo(String target) {
		if(TestInfo.getTarget().equals("Web")){
			navigate().to(target);
			waitForPageLoaded();
		}
	}
	
	/**
	 * 진입해려는 Activity의 접근 권한이 있어야 진입가능합니다.
	 * 권한이 없는 경우 홈으로 이동합니다.
	 * @param target
	 */
	public void goTo(Activity target) {
		startActivity(target);
		verifyTrue(this.isActivityPresent(target));
	}
	
	/*
	 * 앱 시작할때의 activity로 돌아가는 메소드
	 */
	public void goHome() {
		startActivity(mainActivity);
		verifyTrue(this.isActivityPresent(mainActivity));
	}
	
	/**
	 * Alert 가져오기
	 * 
	 * @return Alert 객체 반환
	 */
	public Alert getAlert() {
		Alert alert = null;
		try {
			alert = wait.until(ExpectedConditions.alertIsPresent());
			return alert;
		} catch (UnhandledAlertException ua) {
			return alert;
		}
	}
	
	/**
	 * 클릭 후 노출되는 Alert 가져오기
	 * 
	 * @param locator 클릭하려는 Element의 locator 정보
	 * @return 클릭 후 노출 된 Alert 객체 반환
	 */
	public Alert getAlert (By locator) {
		Alert alert = null;
		this.waitForIsElementPresent(locator).click();
		
		alert = wait.until(ExpectedConditions.alertIsPresent());
		//alert = this.switchTo().alert();
		return alert;
	}
	
	/**
	 * 퍼포먼스 타이밍 및 네이게이션 값을 프린트 및 리턴
	 * @return URL, Type, RedirectCount, OnLoadTime 
	 */
	private String getPerformanceTiming() {
		Long onloadTime =  (Long)executeScript("var t = performance.timing;  return(t.loadEventStart - t.navigationStart);");
		Long redirectCount =  (Long)executeScript("return performance.navigation.redirectCount;");
		String type =  Long.toString((Long)executeScript("return performance.navigation.type;"));
		switch (type) {
		case "0":
			type = "NAVIGATE";
			break;
		case "1":
			type = "RELOAD";
			break;
		case "2":
			type = "BACK_FORWARD";
			break;
		case "255":
		default:
			type = "RESERVED";
		}
		String url = getCurrentUrl();
		String result = "URL : "+ url +" | Type : "+ type +" | RedirectCount : "+ redirectCount +" | OnLoad : "+onloadTime+"ms";
		//String log = "[" + url + "] - [" + type + "] - [" + redirectCount + "] - [" + onloadTime + "ms]";
		
		//int listIndex = 0;
		boolean isExsit = false;
		List<String> preResult = null; // 리스트에 있는지 검사
		for (int i = 0 ; i < ReportInfo.getWebPerformanceResults().size() ; i ++) {
			preResult = ReportInfo.getWebPerformanceResults().get(i);
			if (preResult.get(0).equals(url)) {
				isExsit = true;
				break;
			}
		}
		
		List<String> currResult = new ArrayList<String>();
		
		// 동일 URL 이 없고 navigate 된 URL 이라면..
		if (!isExsit) {
			currResult.add(0, url);
			currResult.add(1, "1");
			currResult.add(2, Long.toString(onloadTime)); // max
			currResult.add(3, Long.toString(onloadTime)); // average
			currResult.add(4, Long.toString(onloadTime)); // min
			
			ReportInfo.setWebPerformanceResults(currResult);
		}
		
		// 동일 URL이 있다면..
		else {
			// 접속 카운트
			int connectCount = Integer.parseInt(preResult.get(1));
			
			// 최대값
			long maxTime = Long.parseLong(preResult.get(2));
			if (maxTime < onloadTime) {
				maxTime = onloadTime;
			}
			
			// 평균값
			long avgTime = Long.parseLong(preResult.get(3));
			avgTime = (avgTime * connectCount + onloadTime) / (connectCount + 1);
			
			// 최소값
			long minTime = Long.parseLong(preResult.get(4));
			if (minTime > onloadTime) {
				minTime = onloadTime;
			}
			
			currResult.add(0, url);
			currResult.add(1, Integer.toString(connectCount + 1));
			currResult.add(2, Long.toString(maxTime));
			currResult.add(3, Long.toString(avgTime));
			currResult.add(4, Long.toString(minTime));
			
			ReportInfo.getWebPerformanceResults().remove(preResult);
			ReportInfo.setWebPerformanceResults(currResult);
		}

		return result;
	}

	/********************** WaitMethod ****************************/
	
	/**
	 * Element가 존재할때까지 대기하는 메소드
	 * 
	 * @param locator존재
	 *            확인 할 Element를 지정
	 * @return locator에 존재하는 WebElement (element, null)
	 * @throws Selenium
	 *             Exception
	 */
	public WebElement waitForIsElementPresent(By locator) {
		for (int i = 0; i < MAX_TRY_COUNT; i++) {
			WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
			if (element.isDisplayed()) {
				return element;
			}
		}

		return null;
	} 
	
	/**
	 * Element가 selected or checked 될때까지 대기하는 메소드
	 * 
	 * @param locator
	 *            존재 확인 할 Element를 지정
	 * @return boolean
	 * @throws Exception
	 *             - Selenium Exception
	 */
	public WebElement waitForIsElementSelected(By locator) {
		WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));

		for (int i = 0; i < MAX_TRY_COUNT; i++) {
			if (isSelected(locator))
				return element;
		}

		return null;
	}
	
	/**
	 * Element가 Visible인지 확인
	 * 
	 * @param locator 확인하려는 Element의 locator
	 * @return Visible 상태 여부 boolean 리턴
	 */
	public WebElement waitForIsVisible(By locator) {
		WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
		element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));

		try {		
			if (element.isDisplayed()) {
				return element;
			}
		} catch (Exception e) {
			printLog(e.getMessage());
			return null;
		}
		printLog (locator + " : 해당 엘리먼트가 " + TIME_OUT_SEC + "초내에 visible 되지 않음");
		return null;
	}

	/**
	 * Element가 Invisible(not visible)인지 확인
	 * 
	 * @param locator 확인하려는 Element의 locator
	 * @return Invisible 유무 boolean 반환
	 */
	public WebElement waitForIsNotVisible(By locator) {
		WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));

		try {
			if (wait.until(ExpectedConditions.invisibilityOfElementLocated(locator))) {
				return element;
			}
		} catch (Exception e) {
			printLog(e.getMessage());
			return null;
		}
		printLog (locator + " : 해당 엘리먼트가 " + TIME_OUT_SEC + "초내에 invisible 되지 않음");
		return null;
	}
	
	/**
	 * searchText 나타날 때까지 기다리는 메소드
	 * 
	 * @param searchText 나타날때까지 기다릴 Text
	 * @return Text Present 유무
	 */
	public WebElement waitForIsTextPresent(String searchText) {
		for (int i = 0; i < MAX_TRY_COUNT; i++) {
			WebElement element = null;

			if (isTextPresent(searchText)){
				element = getElement(com.ntscorp.auto_client.appium.By.text(searchText));
				return element;
			}
			
			sleep(1);
		}
		printLog (searchText + " : 해당 Text가 " + TIME_OUT_SEC + "초내에 나타나지 않음");
		return null;
	}
	
	/**
	 * 페이지의 load complete 대기
	 */
	public void waitForPageLoaded() {
		if (TestInfo.getTarget().equals("App")) {
			if(this.getPageSource().contains("android.webkit.WebView")) {
//				waitForJSLoaded();
			} else {
				manage().timeouts().implicitlyWait(MAX_WAIT_TIME, TimeUnit.SECONDS);
			}
		} else {
			LogEntries logEntries = this.manage().logs().get(LogType.BROWSER);
			for (LogEntry entry : logEntries) {
				if (!entry.getLevel().toString().equalsIgnoreCase("WARNING")) {
					if (!AutomationReport.jsErrorResult.contains("<a href="+getCurrentUrl()+" target=\"_black\"'>" +getCurrentUrl()+ "</a><br>" + entry.getMessage())) {
						AutomationReport.jsErrorResult.add("<a href="+getCurrentUrl()+" target=\"_black\"'>" +getCurrentUrl()+ "</a><br>" + entry.getMessage());
					}
				}
			}
			waitForJSLoaded();
			this.getPerformanceTiming();
		}
	}

	/**
	 * 프로그레스바가 사라질 때까지 기다리는 메소드 try 10회 X 회당 대기 4 sec X implict wait 3 sec : 최대
	 * 120 sec 대기
	 * 
	 * @param null
	 * @return whether progress bar disappeared or not
	 * @throws Exception
	 *             - Exception
	 */
	public boolean waitProgressCompleted() {	
		manage().timeouts().implicitlyWait(MAX_WAIT_TIME, TimeUnit.SECONDS);

		return true;
		
		/*for (int i = 0; i < MAX_TRY_COUNT; i++) {
			if (!getPageSource().contains("android.widget.ProgressBar")) {
				return true;
			}
			sleep(2);
		}
		WebElement progressBar = findElement(By.className("android.widget.ProgressBar"));
		System.out.println("Progress Bar가 사라지지 않음 : " + progressBar.toString());
		return false;*/
	}
	
	/*
	 * 페이지의 JS가 로드될때 까지 대기하는 메소드
	 * Angular의 경우 사용 여부 판단 후 wait
	 */
	private boolean waitForJSLoaded() {
	   WebDriverWait wait = new WebDriverWait(this, PAGE_LOAD_TIME_OUT);
	   
	   if(TestInfo.getTarget().equals("Web")){
		   boolean  isAngularJS = (boolean) executeJavascript(this, "return window.angular !== undefined");

			// wait for AngularJS to load
			if(isAngularJS){ 
				 new ExpectedCondition<Boolean>() {
					@Override
					public Boolean apply(WebDriver arg0) {
						return (Boolean) ((JavascriptExecutor) arg0).executeScript("return (angular.element(document).injector() !== undefined) && (angular.element(document).injector().get('$http').pendingRequests.length === 0)");
					}
				};
			}
	   }
		
	    // wait for jQuery to load
	    ExpectedCondition<Boolean> jQueryLoad = new ExpectedCondition<Boolean>() {
	      @Override
	      public Boolean apply(WebDriver arg0) {
	        try {
	          return ((Long) ((JavascriptExecutor) arg0).executeScript("return jQuery.active") == 0);
	        }
	        catch (Exception e) {
	          return true;
	        }
	      }
	    };

	    // wait for Javascript to load
	    ExpectedCondition<Boolean> jsLoad = new ExpectedCondition<Boolean>() {
	      @Override
	      public Boolean apply(WebDriver arg0) {
	        return ((JavascriptExecutor) arg0).executeScript("return document.readyState")
	            .toString().equals("complete");
	      }
	    };

	  return wait.until(jQueryLoad) && wait.until(jsLoad);
	}
	
	/**
	 * locator에 해당하는 모든 엘리먼트 반환 
	 * 
	 * @param locator 가져오려는 Element의 locator
	 * @return locator에 해당하는 모든 엘리먼트 List
	 */
	public List<WebElement> waitForIsAllElementsPresent (By locator) {
		int tryCnt = 0;
		WebDriverWait wait = (WebDriverWait) new WebDriverWait(this, PAGE_LOAD_TIME_OUT);

		while (tryCnt < MAX_TRY_COUNT) {
			try {
				List<WebElement> elements = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(locator));
				return elements;

			} catch (StaleElementReferenceException e) {
				printLog("** Catch StaleElement Exception : #" + tryCnt);
				sleep(1);
				tryCnt = tryCnt + 1;
			} catch (Exception e) {
				printLog (e.getMessage());
				return null;
			}
		}
		fail (locator + " : 해당 엘리먼트가 " + TIME_OUT_SEC + "초내에 노출되지 않음");
		return null;
	}

	/**
	 * 메인윈도우로 포커스 전환 메소드
	 * @throws Exception
	 */
	@SuppressWarnings(value = { "동작 이상 있을 수 있음" })
	public void selectMainWindow() {
		Set<String> getHandles = getWindowHandles();
		Iterator<String> winHandles = getHandles.iterator();
		
		String mainWindow = winHandles.next();
		switchTo().window(mainWindow);
	}
	
	/**
	 * 특정 윈도우로 전환
	 */
	public void selectWindow(String winHandle) {
		switchTo().window(winHandle);
	}
	
	/**
	 * 특정 윈도우 닫기
	 * 
	 * @param winHandle 닫으려는 윈도우 handle
	 */
	public void closeWindow(String winHandle) {
		switchTo().window(winHandle).close();
	}
	
	/**
	 * 메인 윈도우 외의 모든 오픈 윈도우 close 메소드 @
	 */
	public void closeNewWindows() {
		String tempHandle = null;
			
		Set<String> getHandles = getWindowHandles();
		Iterator<String> winHandles = getHandles.iterator();
			
		int winSize = getHandles.size();
			
		if (winSize > 2) {
			while (winHandles.hasNext()) {
				tempHandle = winHandles.next();
				if (!tempHandle.equals(mainWindowHandle)) {
					switchTo().window(tempHandle).close();	
				}
			}
		}
		switchTo().window(mainWindowHandle); 
	}
	
	/**
	 * 프레임 전환
	 * 
	 * @param frameName 전환하려는 프레임 이름
	 */
	public void switchToFrame(By frameName) {
		WebElement element = waitForIsElementPresent(frameName);
		switchTo().frame(element);
	} 
	
	/**
	  * 로그를 출력하는 메소드
	  * 메소드를 실행하는 system의 정보를 출력 (platform, browser info)
	  * @param 출력할 메시지
	  * @return void
	  */
	public void printLog(String logs) {
		System.out.println("[android] " + logs);

		if (logs.contains("JS error")) {
			logs = logs.replace("-", "=");
			if (ReportInfo.getLogBuf().indexOf(logs) < 0) {
				System.out.println("Last " + logs);
				ReportInfo.setLogBuf("[android] " + logs);
				ReportInfo.setLogBuf("|");
			}
		} else {
			ReportInfo.setLogBuf("[android] " + logs);
			ReportInfo.setLogBuf("|");
		}
	}
	
	
	/********************** IsMethod ****************************/
	
	/**
	 * locator에 해당하는 엘리먼트가 존재하는 지 확인
	 * 
	 * @param locator 확인하려는 locator
	 * @return 엘리먼트 유무 boolean 반환 
	 */
	public boolean isElementPresent(By locator) {
		boolean result = false;
		manage().timeouts().implicitlyWait(MIN_WAIT_TIME, TimeUnit.SECONDS);
		
		try {		
			WebElement element = locator.findElement((SearchContext) this);
			if (element.isDisplayed()) {
				manage().timeouts().implicitlyWait(MAX_WAIT_TIME, TimeUnit.SECONDS);
				result = true;
			}
		} catch (NoSuchElementException nsee) {
			manage().timeouts().implicitlyWait(MAX_WAIT_TIME, TimeUnit.SECONDS);
			return false;
		} catch (WebDriverException wde) {
			manage().timeouts().implicitlyWait(MAX_WAIT_TIME, TimeUnit.SECONDS);
			return false;			
		}
		manage().timeouts().implicitlyWait(MAX_WAIT_TIME, TimeUnit.SECONDS);
		return result;
	}
	
	/**
	 * 해당 엘리먼트가 존재하는 지 확인
	 * 
	 * @param WebEelement 노출 확인하려는 Element
	 * @return 엘리먼트 유무 boolean 반환 
	 */
	public boolean isElementPresent(WebElement element) {
		boolean result = false;
		manage().timeouts().implicitlyWait(MIN_WAIT_TIME, TimeUnit.SECONDS);
		
		try {		
			if (element.isDisplayed()) {
				manage().timeouts().implicitlyWait(MAX_WAIT_TIME, TimeUnit.SECONDS);
				result = true;
			}
		} catch (NoSuchElementException nsee) {
			manage().timeouts().implicitlyWait(MAX_WAIT_TIME, TimeUnit.SECONDS);
			return false;
		} catch (WebDriverException wde) {
			manage().timeouts().implicitlyWait(MAX_WAIT_TIME, TimeUnit.SECONDS);
			return false;			
		}
		manage().timeouts().implicitlyWait(MAX_WAIT_TIME, TimeUnit.SECONDS);
		return result;
	}
	
	/**
	 * Element가 selected or checked 되었는지 확인하는 메소드
	 * 
	 * @param locator 존재 확인 할 Element를 지정
	 * @return boolean
	 * @throws Selenium Exception
	 */
	public boolean isSelected(By locator) {
		boolean result = false;
		manage().timeouts().implicitlyWait(MIN_WAIT_TIME, TimeUnit.SECONDS);
		
		try {		
			WebElement element = locator.findElement((SearchContext) this);
			if (element.getAttribute("checked").equals("true") 
					|| element.getAttribute("selected").equals("true")) {
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
	 * 해당 Element의 isEnabled 속성 값이 true인지 확인
	 * 
	 * @param locator 해당 Element의 locator
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
	 * Activity가 전환될때까지 대기하는 메소드
	 * 
	 * @param Activity 전환을 대기하는 Activity
	 * @return boolean 전환 확인에 따른 true vs false
	 * @throws Selenium Exception
	 */
	public boolean isActivityPresent(Activity expectedActivity) {
		for (int second = 0; second <= TIME_OUT_SEC; second += WAIT_SEC) {
			String currentActivity = currentActivity();
			if (currentActivity.contains(expectedActivity.getAppActivity())) {
				return true;
			}
			sleep(1);
		}
		
		return false;
	}
	
	/**
	 * 인자로 받은 Text가 노출되는지를 확인하는 메소드
	 * 
	 * @param String 확인하고자 하는 Text
	 * @return boolean
	 */
	public boolean isTextPresent(String searchText) {
		boolean result = false;
		manage().timeouts().implicitlyWait(MIN_WAIT_TIME, TimeUnit.SECONDS);
		
		try {
			if(TestInfo.getTarget().equals("App")){
				if (findElement(By.xpath("//*[contains(@text,'"+searchText+"')]")) != null) {
					manage().timeouts().implicitlyWait(MAX_WAIT_TIME, TimeUnit.SECONDS);
					return true;
				}
			} else {
				if (findElement(By.xpath("//*[contains(text(),'"+searchText+"')]")) != null) {
					manage().timeouts().implicitlyWait(MAX_WAIT_TIME, TimeUnit.SECONDS);
					return true;
				}
			}
		} catch (NoSuchElementException e) {
			manage().timeouts().implicitlyWait(MAX_WAIT_TIME, TimeUnit.SECONDS);
			return false;
		}
		manage().timeouts().implicitlyWait(MAX_WAIT_TIME, TimeUnit.SECONDS);
		return result;
	}
	
	/**
	 * Element 속성 중 Description에 인자로 받은 String 값이 노출되는지 확인하는 메소드
	 * 
	 * @param String 확인하고자 하는 Description
	 * @return boolean
	 */
	public boolean isDescriptionPresent(String searchText) {
		boolean result = false;
		manage().timeouts().implicitlyWait(MIN_WAIT_TIME, TimeUnit.SECONDS);
		
		try {		
			if (findElement(By.xpath("//*[contains(@content-desc,'"+searchText+"')]")) != null) {
				manage().timeouts().implicitlyWait(MAX_WAIT_TIME, TimeUnit.SECONDS);
				return true;
			}
		} catch (NoSuchElementException e) {
			manage().timeouts().implicitlyWait(MAX_WAIT_TIME, TimeUnit.SECONDS);
			return false;
		}
		manage().timeouts().implicitlyWait(MAX_WAIT_TIME, TimeUnit.SECONDS);
		return result;
	}
	
	/**
	 * 이미지 엑박여부 확인
	 * 
	 * @return 이미지 상태 boolean
	 */
	public boolean isImagesPresent() {
		boolean result = true;
		List<WebElement> elementList = findElements(By.tagName("img"));

		printLog("All images count : " + elementList.size());

		for (WebElement element : elementList) {
			if (element.isDisplayed()) {
				try {
					if (!isImagePresent(element))
						result = false;
					
				} catch (Exception e) {
					printLog(e.getMessage());

					result = false;
				}
			}
		}

		return result;
	}
	
	/**
	 * 이미지 엑박여부 확인
	 * 
	 * @return boolean
	 */
	public boolean isImagePresent(WebElement element) {
		boolean ImagePresent = false;
		
		try {
			ImagePresent = (boolean) ((JavascriptExecutor) this).executeScript(
					"return arguments[0].complete && typeof arguments[0].naturalWidth != \"undefined\" && arguments[0].naturalWidth > 0", element);
		} catch (Exception e) {
			ImagePresent = false;
			e.getMessage();
		}
		
		if (!ImagePresent)
			printLog(element.getAttribute("src") + " --> [Not displayed]");
		else {
			printLog(element.getAttribute("src") + " --> [Displayed]");
			
			return true;
		}
		
		return false;
	}

	/**
	 * 페이지 전체의 url 링크의 정상 여부 확인
	 * 
	 * @return boolean
	 * @throws Exception
	 */
	public boolean isBrokenLinkExist() {
		boolean result = true;

		HashSet<String> links = getAllLinks();
		printLog("Total Links Count : " + links.size());

		for (String url : links) {
			try {
				if (!isLinkChecked(new URL(url)))
					result = false;

			} catch (Exception e) {
				printLog(">>> " + url + "의 정보가 잘못되었습니다." + e.getMessage());
			}
		}

		return result;
	}
	
	/**
	 * url 링크 접속 후 상태 확인
	 * 
	 * @param url
	 * @return boolean HTTPResponseCode가 200인 경우만 true
	 * @throws Exception
	 */
	public boolean isLinkChecked(URL url) {
		boolean result = true;

		try {
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.connect();
			
			printLog("URL: " + url);
			printLog("-> " + connection.getResponseCode() + "-" + connection.getResponseMessage());
			
			connection.disconnect();

			if (connection.getResponseCode() == 404)
				result = false;

		} catch (IOException ioe) {
			printLog("LinkTest Fail");
		}

		return result;
	}

	/**
	 * Alert이 나타났는지 확인하는 메소드
	 * 
	 * @return boolean Alert 노출 유무
	 */
	public boolean isAlertPresent() {
		ExpectedCondition<Alert> alert = ExpectedConditions.alertIsPresent();

		if (alert.apply(this) != null)
			return true;

		return false;
	}
	
	/**
	 * toast의 전체 text를 입력 받아 띄어쓰기를 기준으로 split한여 첫번째 인덱스에 담긴 문작으로 getToast를 한다.
	 * 입력받은 Toast의 Text와 getToast의 Text와 비교
	 * 
	 * @param String 확인하고자 하는 토스트 문구
	 * @return boolean
	 * @throws Exception - Selenium Exception
	 */
	public boolean isToastPresent(String toastAllText) {
		String parmToastText = toastAllText;
		String[] toastText = parmToastText.split(" ");

		if (toastAllText.equals(getToast(toastText[0])))
			return true;
		
		return false;
	}
	
	public boolean isWebView() {
		String currentContext = getContext();
		return this.isWebView(currentContext);
	}
	
	public boolean isWebView(String context) {
		if (context.contains("WEBVIEW") || context.contains("CHROMIUM")) 
			return true;
		return false;
	}
	
	public boolean isNative() {
		String currentContext = getContext();
		return this.isNative(currentContext);
	}
	
	public boolean isNative(String context) {
		if (context.contains("NATIVE"))
			return true;
		return false;
	}
	
	/**
	 * Element click
	 * 
	 * @param locator 존재 확인 및 click 후 프로그레스바 completed 대기
	 * @return boolean
	 * @throws Exception - Selenium Exception
	 */
	public void click(By locator) {
		WebElement element = waitForIsElementPresent(locator);

		if (TestInfo.getTarget().equals("App")){
			element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
			element.click();
			waitForPageLoaded();
		}else {
			this.clickException(element);
		}

	}
	
	public void click(WebElement element) {
		if (TestInfo.getTarget().equals("App")) {
			element.click();
			waitForPageLoaded();
		}else {
			this.clickException(element);
		}
	}
	
	private boolean clickException(WebElement element) {			
		for (int i = 0; i < MAX_TRY_COUNT; i++) {
			WebElement ele = element;
			try {
				if (ele != null) {
					highlightElement (ele);
					ele.click();
					waitForPageLoaded();
					
					return true;
				}else {
					fail (">>> Element가 null값으로 Click동작을 수행할 수 없습니다.");
					return false;
				}
			} catch (StaleElementReferenceException e){
				printLog ("** Catch StaleElement Exception : #" + i );
			} catch(UnhandledAlertException uae){
				printLog(">>> Click동작으로 Alert이 노출되었습니다. ");
				return true;
			} catch (NoSuchWindowException nswe) {
				printLog(">>> Click동작으로 Window가 닫혔습니다. ");
				return true;
			}catch (Exception e) {
				printLog (e.getMessage());
			}
			sleep(1);
		}
		fail (element + " : 해당 엘리먼트가 " + TIME_OUT_SEC + "초내에 노출되지 않음");
		return false;
	}
	
	/********************** Touch Screen ****************************/
	
	/**
	 * App Coordinate Touch
	 * 
	 * @param App Element의 x,y 좌표
	 * @throws Selenium Exception
	 */
	public void touch(int x, int y) {
		action = new TouchAction(this);
		action.press(PointOption.point(x + 1, y + 1)).release().perform();
		waitForPageLoaded();
	}
	
	/**
	 * App Coordinate Touch
	 * 
	 * @param App Element의 x,y 좌표
	 * @throws Selenium Exception
	 */
	public void touch(int[] location) {
		action = new TouchAction(this);
		action.press(PointOption.point(location[0] + 1, location[1] + 1)).release().perform();
		waitForPageLoaded();
	}
	
	/**
	 * Web / App Element Touch
	 * 
	 * @param Weblocator
	 * @throws Selenium Exception
	 */
	public void touch(WebElement element) {
		action = new TouchAction(this);
		if (TestInfo.getTarget().equals("Web")) {
			cmd.runCommand("adb -s " + DeviceInfo.getUdid() + " shell input touchscreen tap " + element.getLocation().getX() + " " + element.getLocation().getY(), false);
		} else {
			action.press(ElementOption.element(element)).perform();			
		}
		
		waitForPageLoaded();
	}

	public void touch(By locator) {
		action = new TouchAction(this);
		WebElement element = waitForIsElementPresent(locator);
		
		if (TestInfo.getTarget().equals("Web")) {
			cmd.runCommand("adb -s " + DeviceInfo.getUdid() + " shell input touchscreen tap " + element.getLocation().getX() + " " + element.getLocation().getY(), false);
		} else {
			action.tap(ElementOption.element(element)).perform();			
		}

		waitForPageLoaded();
	}
	
	/**
	 * 입력한 문자열 전부 삭제
	 * 
	 * @param locator 문자열 입력할 위치
	 */
	public void clear(By locator) {
		WebElement element = waitForIsElementPresent(locator);
		element.clear();
	}
	
	public void keyEvent(int event) {
		cmd.keyEvent(DeviceInfo.getUdid(), event);			
	}
	
	/**
	 * Element 더블 클릭
	 * 
	 * @param 클릭하려는 Element
	 * @return 더블클릭 성공 여부 boolean 반환
	 */
	public void doubleClick(WebElement element) {		
		action = new TouchAction(this);
		if (TestInfo.getTarget().equals("Web")) {
			cmd.runCommand("adb -s " + DeviceInfo.getUdid() + " shell input touchscreen tap " + element.getLocation().getX() + " " + element.getLocation().getY(), false);
			cmd.runCommand("adb -s " + DeviceInfo.getUdid() + " shell input touchscreen tap " + element.getLocation().getX() + " " + element.getLocation().getY(), false);
		} else {		
			action.tap(ElementOption.element(element)).perform();
			action.tap(ElementOption.element(element)).perform();
		}
		waitForPageLoaded();
	}

	/**
	 * locator에 해당하는 Element 더블 클릭
	 * 
	 * @param locator 클릭하려는 Element의 locator
	 * @return 더블클릭 성공 여부 boolean 반환
	 */
	public void doubleClick(By locator) {		
		action = new TouchAction(this);
		WebElement element = waitForIsElementPresent(locator);

		if (TestInfo.getTarget().equals("Web")) {
			cmd.runCommand("adb -s " + DeviceInfo.getUdid() + " shell input touchscreen tap " + element.getLocation().getX() + " " + element.getLocation().getY(), false);
			cmd.runCommand("adb -s " + DeviceInfo.getUdid() + " shell input touchscreen tap " + element.getLocation().getX() + " " + element.getLocation().getY(), false);
		} else {		
			action.tap(ElementOption.element(element)).perform().perform();
		}
		waitForPageLoaded();
	}
	
	/**
	 * Element click / toggle & switch on
	 * @param locator swtich off 상태이면 클릭 (set on)
	 */
	public void setSwitchOn(By locator) {
		WebElement element = waitForIsElementPresent (locator);
		String isChecked = element.getAttribute("checked");
		
		if (isChecked.equals("false")) { 
			element.click();
			waitForPageLoaded();
		}
	}
	
	/**
	 * Element click / toggle & switch off
	 * @param locator swtich on 상태이면 클릭 (set off)
	 */
	public void setSwitchOff(By locator) {
		WebElement element = waitForIsElementPresent (locator);
		String isChecked = element.getAttribute("checked");
		
		if (isChecked.equals("true")) {
			element.click();			
			waitForPageLoaded();
		}
	}
	
	/**
	 * 기존 문자열 가져와서 입력한 인자 text와 합쳐서 입력
	 * 
	 * @param locator
	 *            element정보
	 * @param String
	 *            입력 text
	 * @param boolean
	 *            이어쓰기 boolean 값
	 * @throws Exception
	 *             -Selenium Exception
	 */
	public void type(By locator, String text, boolean isContinue) {
		WebElement element = waitForIsElementPresent(locator);

		if (isContinue) {
			String continuousText = element.getText();
			element.sendKeys(continuousText + text);
		} else {
			element.sendKeys(text);
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
	 * Long press element action
	 * 
	 * @param target locator
	 */
	public void longPress(WebElement element) {
		action = new TouchAction(this);
		if (TestInfo.getTarget().equals("Web"))
			cmd.runCommand("adb -s " + DeviceInfo.getUdid() + " shell input touchscreen draganddrop "
					+ element.getLocation().getX() + " " + element.getLocation().getY() + " "
					+ element.getLocation().getX() + " " + element.getLocation().getY() + " 1500", false);
		else
			action.longPress(ElementOption.element(element)).release().perform();

		waitForPageLoaded();
	}

	public void longPress(By locator) {
		action = new TouchAction(this);
		WebElement element = waitForIsElementPresent(locator);

		if (TestInfo.getTarget().equals("Web"))
			cmd.runCommand("adb -s " + DeviceInfo.getUdid() + " shell input touchscreen draganddrop "
					+ element.getLocation().getX() + " " + element.getLocation().getY() + " "
					+ element.getLocation().getX() + " " + element.getLocation().getY() + " 1500", false);
		else
			action.longPress(ElementOption.element(element)).release().perform();

		waitForPageLoaded();
	}
	
	/**
	 * Element 드래그 앤 드랍 
	 * 
	 * @param sourceLocate 드래그하려는 Element
	 * @param tagetLocate 드랍하려는 Element
	 */
	public void dragAndDrop(By src, By dst) {
		action = new TouchAction(this);
		
		WebElement srcElement = waitForIsElementPresent(src);
		WebElement dstElement = waitForIsElementPresent(dst);	

		if (TestInfo.getTarget().equals("Web")) {
			cmd.runCommand("adb -s " + DeviceInfo.getUdid() + " shell input touchscreen draganddrop "
					+ srcElement.getLocation().getX() + " " + srcElement.getLocation().getY() + " "
					+ dstElement.getLocation().getX() + " " + dstElement.getLocation().getY() + " 1500", false);
		} else {
			action.longPress(ElementOption.element(srcElement)).moveTo(ElementOption.element(dstElement)).release()
					.perform();
		}

		waitForPageLoaded();
	}

	/**
	 * Element 드래그 앤 드랍 
	 * 
	 * @param sourceLocate 드래그하려는 Element
	 * @param tagetLocate 드랍하려는 Element
	 */
	public void dragAndDrop(WebElement src, WebElement dst) {
		action = new TouchAction(this);
		
		if (TestInfo.getTarget().equals("Web")) {
			cmd.runCommand("adb -s " + DeviceInfo.getUdid() + " shell input touchscreen draganddrop "
					+ src.getLocation().getX() + " " + src.getLocation().getY() + " "
					+ dst.getLocation().getX() + " " + dst.getLocation().getY() + " 1500", false);
		} else {
			action.longPress(ElementOption.element(src)).moveTo(ElementOption.element(dst)).release().perform();			
		}

		waitForPageLoaded();
	}
	
	/**
	 * 모바일 웹 새로고침 
	 */
	public void refresh() {
		navigate().refresh();
		waitForPageLoaded();
	}
	
	/** 앱 최상단에서 Pull Down 하여 리프레쉬
	 */
	public void pullDownRefresh() {
		scrollUp();
	}
	
	/**
	 * Horizontal swipe
	 * 
	 * @param direction swipe 범위 (ex 2 : 가로 20% -> 80% 범위로 swipe)
	 * @return count swipe 수행 횟수
	 */
	protected void swipe(double direction, int count) {
		action = new TouchAction(this);
		
		if (direction > 1.0 || direction < 0.1)
			fail("swipe direction : 0.1 ~ 1.0");

		int startX = (int) (DeviceInfo.getWinX() * direction);
		int startY = DeviceInfo.getWinY() / 2;
		int endX = (int) (DeviceInfo.getWinX() * (1 - direction));
		int endY = startY;

		for (int i = 0; i < count; i++) {
			if (TestInfo.getTarget().equals("Web")) {
				cmd.runCommand("adb -s " + DeviceInfo.getUdid() + " shell input touchscreen swipe " + startX + " " + startY + " " + endX + " " + endY + " 1500", false);
			} else {
				action.press(PointOption.point(startX, startY)).waitAction().moveTo(PointOption.point(endX, endY)).release().perform();
				wait.until(ExpectedConditions.refreshed(ExpectedConditions.visibilityOfElementLocated(By.xpath("//android.widget.FrameLayout"))));
			}
		}
	}
	
	public void swipeRight () {
		swipe(0.8, 1);
	}

	public void swipeRight(int count) {
		for (int i = 0; i < count; i++)
			swipeRight();
	}
	
	public void swipeLeft () {
		swipe(0.2, 1);
	}

	public void swipeLeft(int count) {
		for (int i = 0; i < count; i++)
			swipeLeft();
	}
	
	public void swipeBy(By locator) {
		action = new TouchAction(this);
		WebElement target = waitForIsElementPresent(locator);

		int y = target.getLocation().getY();
		int h = target.getSize().getHeight();

		int startX = (int) (DeviceInfo.getWinX() * 0.8);
		int startY = y + h / 2;
		int endX = (int) (DeviceInfo.getWinX() * (1 - 0.8));
		int endY = startY;

		if (TestInfo.getTarget().equals("Web")) {
			cmd.runCommand("adb -s " + DeviceInfo.getUdid() + " shell input touchscreen swipe " + startX + " " + startY + " " + endX + " " + endY + " 1500", false);			
		} else {
			action.press(PointOption.point(startX, endY)).waitAction().moveTo(PointOption.point(endX, endY)).release().perform();			
		}
	}
	
	public void swipeBy(WebElement target) {
		action = new TouchAction(this);
		int y = target.getLocation().getY();
		int h = target.getSize().getHeight();

		int startX = (int) (DeviceInfo.getWinX() * 0.8);
		int startY = y + h / 2;
		int endX = (int) (DeviceInfo.getWinX() * (1 - 0.8));
		int endY = startY;

		if (TestInfo.getTarget().equals("Web")) {
			cmd.runCommand("adb -s " + DeviceInfo.getUdid() + " shell input touchscreen swipe " + startX + " " + startY + " " + endX + " " + endY + " 1500", false);			
		} else {
			action.press(PointOption.point(startX, endY)).waitAction().moveTo(PointOption.point(endX, endY)).release().perform();			
		}
	}
	
	@Deprecated
	public void swipe(WebElement target) {
		action = new TouchAction(this);
		int x = target.getLocation().getX();
		int y = target.getLocation().getY();
		int w = target.getSize().getWidth();

		action.press(PointOption.point(w - x, y)).waitAction().moveTo(PointOption.point(w / 3, y)).release().perform();
	}
	
	/**
	 * vertical scroll
	 * 
	 * @param direction scroll 범위 (ex 0.2 : 세로 20% -> 80% 범위로 하단 sroll)
	 * @return count scroll 수행 횟수
	 */
	public void scroll(double direction, int count) {
		action = new TouchAction(this);
		
		if (direction > 1.0 || direction < 0.1)
			fail("scoll direction : 0.1 ~ 1.0");

		int startY = (int) (DeviceInfo.getWinY() * direction);
		int startX = DeviceInfo.getWinX() / 2;
		int endY = (int) (DeviceInfo.getWinY() * (1 - direction));
		int endX = startX;

		for (int i = 0; i < count; i++) {
			if (TestInfo.getTarget().equals("Web")) {
				cmd.runCommand("adb -s " + DeviceInfo.getUdid() + " shell input touchscreen swipe " + startX + " " + startY + " " + endX + " " + endY + " 1500", false);
			} else {
				action.press(PointOption.point(startX, startY)).waitAction().moveTo(PointOption.point(endX, endY)).release().perform();
				wait.until(ExpectedConditions.refreshed(ExpectedConditions.visibilityOfElementLocated(By.xpath("//android.widget.FrameLayout"))));
			}
			waitForPageLoaded();
		}
	}

	/**
	 * Vertical scroll
	 * 
	 * @param direction
	 *            : "up" or "down" / 1회 수행
	 * @throws Exception
	 *             - Exception
	 */
	public void scrollUp() {
		scroll(0.3, 1);
	}

	public void scrollUp(int count) {
		for (int i = 0; i < count; i++)
			scrollUp();
	}

	public void scrollDown() {
		scroll(0.7, 1);
	}

	public void scrollDown(int count) {
		for (int i = 0; i < count; i++)
			scrollDown();
	}

	/**
	 * 해당 Element 스크롤
	 * 
	 * @param String
	 *            타겟 text
	 */
	public void scrollTo(WebElement element) {
		
		if (TestInfo.getTarget().equals("Web")) {
			JavascriptExecutor js = (JavascriptExecutor) this;
			js.executeScript("arguments[0].scrollIntoView();", element);
		}else{
			printLog("App에서는 사용 불가능한 Scroll 기능입니다.");
		}
		// fail("scroll : element not found");
	}

	/**
	 * 해당 Element까지 스크롤
	 * 
	 * @param String
	 *            타겟 text
	 */
	public void scrollTo(By locator) {
		if (TestInfo.getTarget().equals("Web")) {
			WebElement element = waitForIsElementPresent(locator);

			JavascriptExecutor js = (JavascriptExecutor) this;
			js.executeScript("arguments[0].scrollIntoView();", element);
		} else {
			for (int i = 0; i < MAX_TRY_COUNT; i++) {
				if (isElementPresent(locator)) {
					break;
				}
				scrollDown();
			}
		}
	}

	/**
	 * locator를 페이지 최상단까지 스크롤
	 * 
	 * @param locator
	 */
	public void scrollToTop(By locator) {
		action = new TouchAction(this);

		WebElement target = waitForIsElementPresent(locator);
		int startY = target.getLocation().getY();
		int endY = DeviceInfo.getMaskTop();

		action.longPress(PointOption.point(DeviceInfo.getCenterX(), startY)).moveTo(PointOption.point(DeviceInfo.getCenterX(), endY)).release().perform();
	}

	public void scrollToTop(WebElement element) {
		action = new TouchAction(this);

		int startY = element.getLocation().getY();
		int endY = DeviceInfo.getMaskTop();

		action.longPress(PointOption.point(DeviceInfo.getCenterX(), startY)).moveTo(PointOption.point(DeviceInfo.getCenterX(), endY)).release().perform();
	}

	/**
	 * 한 페이지를 스크롤
	 */
	public void scrollPage() {
		action = new TouchAction(this);

		action.longPress(PointOption.point(DeviceInfo.getCenterX(), (DeviceInfo.getWinY() - DeviceInfo.getMaskBottom()))).moveTo(PointOption.point(DeviceInfo.getCenterX(), DeviceInfo.getMaskTop())).release().perform();
	}
	
	/**
	 * 동영상 또는 음원 seeking 디능 메소드
	 * 
	 * @param locator
	 * @param double
	 */
	public void seekBar(By locator, double target) {
		action = new TouchAction(this);
		WebElement seekBar = waitForIsElementPresent(locator);

		int startX = seekBar.getLocation().getX();
		int endX = startX + seekBar.getSize().getWidth();
		int startY = seekBar.getLocation().getY();
		int moveTo = (int) (endX * target);
		
		action.press(PointOption.point(moveTo, startY)).perform();
	}
	
	/**
	 * back 메소드
	 */
	public void back() {
		navigate().back();
		waitForPageLoaded();
	}
	
	/**
	 * 입력한 숫자만큼 뒤로가기
	 * 
	 * @param count 뒤로 가려는 횟수
	 */
	public void back(int count) {
		for (int i = 0; i < count; i++) {
			back();
		}
	}
	
	/**
	 * forward 이동 메소드
	 */
	public void forward() {
		this.navigate().forward();
		this.waitForPageLoaded();
	}
	
	/**
	 * 해당 위치의 highlight 메소드
	 * 
	 * @param locator
	 *            element
	 * @return void
	 */
	public void highlightElement(By locator) {
		String currentContext = getContext();
		if (currentContext.contains("WEBVIEW") || currentContext.contains("CHROMIUM")) {
			WebElement element = waitForIsElementPresent(locator);
			JavascriptExecutor js = (JavascriptExecutor) this;
			js.executeScript("arguments[0].setAttribute('style', arguments[1]);", element, "border: 3px solid red;");
		}
	}

	public void highlightElement(WebElement element) {
		String currentContext = getContext();
		if (currentContext.contains("WEBVIEW") || currentContext.contains("CHROMIUM")) {
			JavascriptExecutor js = (JavascriptExecutor) this;
			js.executeScript("arguments[0].setAttribute('style', arguments[1]);", element, "border: 3px solid red;");
		}
	}
	
	public void openBrowser(String url) {		
		cmd.executeCommand("adb shell am start -a android.intent.action.VIEW -d " + url);
		assertTrue (this.isElementPresent(By.id("com.android.chrome:id/url_bar")));
		assertTrue (this.isElementPresent(By.className("android.webkit.WebView")));
		
		waitForPageLoaded();
	}
	
	public void openChrome(String url) {
		//cmd.executeCommand("adb shell am start -n com.android.chrome/com.google.android.apps.chrome.Main");
		this.openApp("com.android.chrome", "com.google.android.apps.chrome.Main");
		this.click(By.id("com.android.chrome:id/url_bar"));
		this.type(By.id("com.android.chrome:id/url_bar"), url);
		cmd.keyEvent(DeviceInfo.getUdid(), "KEYCODE_ENTER");
		
		waitForPageLoaded();
	}
	
	public void closeChrome() {
		cmd.closeRunningApp(DeviceInfo.getUdid(), "com.android.chrome");
	}
	
	public void openApp(String packageName, String activityName) {
		cmd.executeCommand("adb shell am start -n " + packageName + "/" + activityName);
		sleep(3);
	}

	
	/********************** Handler ****************************/
	
	/**
	 * NATIVE -> WEBVIWE 전환
	 */
	public boolean switchToWeb() {
		//String currentContext = getContext();
		if (isNative()) {
			Set<String> contextNames = getContextHandles();
			
			for (String contextName : contextNames) {				
				if (isWebView(contextName)) {
					context(contextName);
					return true;
				}
			}
			fail("Webview context로 전환할 수 없습니다.");
			return false;
		}
        return true;
	}

	/**
	 * WEBVIWE -> NATIVE 전환
	 */
	public boolean switchToNative() {
		if (isWebView()) {
			Set<String> contextNames = getContextHandles();
			
			for (String contextName : contextNames) {
				if (contextName.contains("NATIVE")) {
					context(contextName);
					return true;
				}
			}
			fail("Native context로 전환할 수 없습니다.");
			return false;
		}
        return true;
	}
	
	/**
	 * 새 윈도우(탭) 생성, 생성 후 해당 윈도우(탭)으로 스위치 필요
	 * 
	 * @param url
	 *            새 윈도우(탭)을 생성하며 이동하려는 url
	 */
	public void openNewWindow(String url) {
		beforeWindowHandle = getWindowHandle();
		JavascriptExecutor js = (JavascriptExecutor) this;
		js.executeScript("window.open('" + url + "', '_blank')");
		switchToWindow(url);
	}
	
	/**
	 * 윈도우(탭)의 타이틀 또는 URL을 입력하여 해당 윈도우(탭)으로 전환
	 * 
	 * @param titleOrUrl 전환하려는 윈도우의 타이틀 또는 URL
	 * @return 전환 성공 값 boolean 반환
	 */
	public boolean switchToWindow(String titleOrUrl) {
		if (titleOrUrl.contains("http")) { // URL일 경우
			try {
				titleOrUrl = getRedirectedURL(new URL(titleOrUrl)).toString();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		
		for (int second = 0; second<TIME_OUT_SEC; second++) {
			Set<String> winHandles = getWindowHandles();
			int winSize = winHandles.size();
			printLog("오픈 된 윈도우 # : " + winSize);
			if (!winHandles.isEmpty()) {
				for (String windowsId : winHandles) {
					if (titleOrUrl.contains("http") && switchTo().window(windowsId).getCurrentUrl().contains(titleOrUrl)) {
						
						if(!windowHandles.contains(windowsId)){
							windowHandles.add(getWindowHandle());							
						} else {
							windowHandles.remove(getWindowHandle());
							windowHandles.add(getWindowHandle());
						}
						waitForPageLoaded();
						printLog("새창 URL : " + getCurrentUrl());
						printLog("새창 Title : " + getTitle());
						return true;
					} else if (switchTo().window(windowsId).getTitle().contains(titleOrUrl)) {
						
						if(!windowHandles.contains(windowsId)){
							windowHandles.add(getWindowHandle());							
						} else {
							windowHandles.remove(getWindowHandle());
							windowHandles.add(getWindowHandle());
						}
						waitForPageLoaded();
						printLog("새창 URL : " + getCurrentUrl());
						printLog("새창 Title : " + getTitle());
						return true;
					} 
				}
				sleep(WAIT_SEC);
			}
		} 
		return false;
	}
	
	/**
	 * 현재 윈도우(탭)를 닫은 후 이전에 위치했던 윈도우(탭)으로 전환
	 * @return 최신 윈도우 닫기 여부 boolean 반환
	 */
	public boolean closeCurrentWindow() {
		int beforeWindowsCount = 0;
		int afterWindowsCount = 0;
		
		String beforeWindows = windowHandles.get(windowHandles.size() - 2);
		String currentWindows = windowHandles.get(windowHandles.size() - 1);
		
		beforeWindowsCount = getWindowHandles().size();
		switchTo().window(currentWindows).close();
		windowHandles.remove(currentWindows);
		switchTo().window(beforeWindows);
		afterWindowsCount = getWindowHandles().size();
		
		if (beforeWindowsCount == afterWindowsCount) {
			verifyTrue(false, "현재창이 정상적으로 닫히지 않음");
			return false;
		}
		
		return true;
	}
	
	/**
	 * 현재 윈도우(탭)을 제외한 모든 윈도우(탭)을 닫음
	 * @return 현재 윈도우(탭)만 남기고 정상적으로 닫힘 여부 boolean 반환
	 */
	public boolean closeAllWindowsExceptCurrentWindow() {
		
		for (int i = 0; i<windowHandles.size()-1; i++) {
			switchTo().window(windowHandles.get(i)).close();
			switchTo().window(windowHandles.get(windowHandles.size()-1));
		}
		getWindowHandles().size();
		
		if (getWindowHandles().size() == 1) {
			
			windowHandles.removeAll(windowHandles);
			windowHandles.add(getWindowHandle());
			return true;
		} else {
			
			verifyTrue(false, "윈도우들이 정상적으로 닫히지 않음");
			
			return false;
		}
	}
	
	/**
	 * 이전 윈도우로 스위치
	 */
	public void switchToBeforeWindow() {
		try{
			int beforeHandleIndex = windowHandles.size() - 2;
			String temp = windowHandles.get(beforeHandleIndex);
			
			switchTo().window(windowHandles.get(beforeHandleIndex));
			windowHandles.remove(beforeHandleIndex);
			windowHandles.add(temp);
			
		} catch (IndexOutOfBoundsException ioobe) {
			printLog("이동 할 수 있는 윈도우가 없습니다.");
		}
	}
	
	/**
	 * 새로 열린 윈도우로 스위치
	 * !! openNewWindow() 사용 시 자동으로 전환되므로 해당 메소드는 사용하지 마세요 !! 
	 */
	public boolean switchToNewWindow() {
		for (int second = 0; second <= TIME_OUT_SEC; second += WAIT_SEC) {
			Set<String> currentHandles = this.getWindowHandles();
			//printLog("현재 윈도우 개수: " +currentHandles.size());
			
			if (currentHandles.size() > windowHandles.size()) {

				for (String handle : currentHandles) {
					if (!windowHandles.contains(handle)) {
						switchTo().window(handle);
						windowHandles.add(handle);

						return true;
					} else if (currentHandles.size() == windowHandles.size()) {
						printLog("새로 열린 윈도우가 없습니다.");
						return false;
					}
				}
			}
			sleep(1);
		}
		return false;
	}
	
	public boolean closeNewWindow() {
		int beforeWindowsCount = 0;
		int afterWindowsCount = 0;
		
		String beforeWindows = windowHandles.get(windowHandles.size() - 2);
		String currentWindows = windowHandles.get(windowHandles.size() - 1);
		
		beforeWindowsCount = getWindowHandles().size();
		switchTo().window(currentWindows).close();
		windowHandles.remove(currentWindows);
		switchTo().window(beforeWindows);
		afterWindowsCount = getWindowHandles().size();
		
		if (beforeWindowsCount == afterWindowsCount) {
			verifyTrue(false, "현재창이 정상적으로 닫히지 않음");
			return false;
		}
		
		return true;
	}
	
	/*
	 * 첫번쨰 배열의 Handle로 이동
	 */
	public void switchToFirstWindow() {
		switchTo().window(windowHandles.get(0));
	}
	
	/* 
	 * 배열의 마지막 Handle을 삭제하는 메소드
	 * 취소버튼으로 팝업 or 탭이 닫히는 Click메소드 다음에 사용
	 */
	public void removeLastWindow() {
		switchToBeforeWindow();
		windowHandles.remove(windowHandles.size() - 2);
	}
	
	/****************************Override*****************************************/
	
	
	/**
	 * get으로 가져온 cookie 정보를 브라우저에 Setting 해주는 메소드
	 * 
	 * @param driver util
	 */
	public void setCookie(AndroidUtil util) {
		for(Cookie cookie : cookieSet){
			System.out.println(cookie.toString());
			if(cookie.toString().contains("ad.naver.com")){
				//do nothing
			} else if(cookie.toString().contains("nid.naver.com")) {
				//do nothing
			}else {
				util.manage().addCookie(cookie);
			}
		}
	}
	
	/**
	 * 현재 페이지의 cookie 정보를 가져오는 메소드
	 * 
	 * @param driver util
	 */
	public void getCookie(AndroidUtil util) {
		cookieSet = util.manage().getCookies();
	}
	
	/**
	 * @param util 드라이버 입력
	 * @param script 실행시킬 스크립트
	 * @return 실행결과 Object 반환
	 */
	public Object executeJavascript(AndroidUtil util, String script) {
		JavascriptExecutor js = (JavascriptExecutor) util;
		return js.executeScript(script);
	}

	@Override
	public TouchScreen getTouch() {
		return touch;
	}
	
	public MediaUtil getMedia(By locater) {
		media = new MediaUtil(this, locater);
		return media;
	}
	
	public void startApp(String packageName, String activityName) {
		Activity target = new Activity (packageName, activityName);
		super.startActivity(target);
	}
	
	/**
	 * 앱고정 키기
	 */
	public void fixAppOn() {
		String command = "adb -s " + DeviceInfo.getUdid() + " shell \"task_id=$(dumpsys activity | grep -A2 '(dumpsys activity recents)'| grep '#'| cut -d ' ' -f 7| cut -c 2-); am task lock $task_id\"";
		cmd.runCommand(command, false);
		this.click(com.ntscorp.auto_client.appium.By.text("시작"));
	}
	
	/**
	 * 앱고정 끄기
	 */
	public void fixAppOff() {
		String command = "adb -s " + DeviceInfo.getUdid() + " shell am task lock stop \"dumpsys activity | grep -A2 '(dumpsys activity recents)'| grep '#'| cut -d ' ' -f 7| cut -c 2-\"";
		cmd.runCommand(command, false);
	}
	
	/**
	 * 애니메이션 제거
	 */
	public void animationOff() {
		String command = "adb -s " + DeviceInfo.getUdid() + " shell pm grant com.nhn.android.eventchecker android.permission.SET_ANIMATION_SCALE";
		cmd.runCommand(command, false);
		command = "adb -s " + DeviceInfo.getUdid() + " shell am broadcast -a io.appium.settings.animation --es setstatus disable";
		cmd.runCommand(command, false);
	}
	
	/**
	 * 애니메이션 추가
	 */
	public void animationOn() {
		String command = "adb -s " + DeviceInfo.getUdid() + " shell am broadcast -a io.appium.settings.animation --es setstatus enable";
		cmd.runCommand(command, false);
	}
}
