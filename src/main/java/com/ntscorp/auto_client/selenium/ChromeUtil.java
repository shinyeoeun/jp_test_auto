package com.ntscorp.auto_client.selenium;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.SearchContext;

import com.ntscorp.auto_client.MediaUtil;
import com.ntscorp.auto_client.Utilities;
import com.ntscorp.auto_client.data.AutomationTime;
import com.ntscorp.auto_client.data.ContextEvent;
import com.ntscorp.auto_client.getset.ReportInfo;
import com.ntscorp.auto_client.report.AutomationReport;

import static com.ntscorp.auto_client.verity.Verify.verifyTrue;
import static org.testng.Assert.fail;

public class ChromeUtil extends ChromeDriver implements TakesScreenshot, Utilities, AutomationTime { 
	// RemoteWebDriver Hub Address
	public static  String hubAddress = "http://127.0.0.1:4444/wd/hub";
	public MediaUtil media = null;
	
	public String mainWindowHandle = null;
	public String lastWindowHandle = null;
	public String newWindowHandle = null;
	public String beforeWindowHandle = null;
	public String currentWindowHandle = null;
	public List<String> windowHandles = new ArrayList<String>();

	private static Set<Cookie> cookieSet = null;
	
	public ChromeUtil(ChromeOptions options){
		super(options);
	}

	public ChromeUtil(DesiredCapabilities chromeCapability) {
		super(chromeCapability);
	}
	
	public ChromeUtil(){}

	/**
	 * 현재 URL과 Title을 출력 @
	 */
	public void printLocation () {
		printLog ("현재 URL : " + getCurrentUrl());
		printLog ("현재 Title : " + getTitle());
	}
	
	/**
	 * 해당 엘리먼트의 하위 링크 유효성 확인
	 * 
	 * @param locator 확인하려는 링크들의 상위 element
	 * @return 유효성 확인 결과 List
	 */
	public HashMap<String, WebElement> getAllLinks(By locator) {
		HashMap<String, WebElement> linkElement = new HashMap<>();
		List<WebElement> elementList = new ArrayList<WebElement>();
		
		elementList = findElement(locator).findElements(By.tagName("a"));
		elementList.addAll(findElement(locator).findElements(By.tagName("img")));

		for (WebElement element : elementList) {
			if(element.getTagName().equals("a")) {
				linkElement.put(element.getAttribute("href"), element);
			}else if(element.getTagName().equals("img")) {
				linkElement.put(element.getAttribute("src"), element);
				this.beforeCheckingIMG(element);
			}
		}
		
		for(Iterator<String> url = linkElement.keySet().iterator(); url.hasNext();) {
			String value = url.next();
			try {
				new URL(value);
				this.beforeCheckingURL(linkElement.get(value));
			} catch (MalformedURLException e) {
				url.remove();
			} catch (StaleElementReferenceException sere) {
				url.remove();
			}
		}
		return linkElement;
	}
	
	/** 
	 * 페이지 전체의 url 링크의 정상 여부 확인
	 */
	public boolean isBrokenLinkExist() {
		boolean result = true;

		HashMap<String, WebElement> links = getAllLinks(By.tagName("html"));
		printLog("Total Links Count : " + links.size());

		for (String url : links.keySet()) {
			URL effectiveURL = null;
			try {
				effectiveURL = new URL(url);
				if (!isLinkChecked(effectiveURL)) {
					result = false;
				} else {
					this.afterCheckingURL(links.get(url));
					this.afterCheckingIMG(links.get(url));
				}
			} catch (Exception e) {
				// do nothing
			}
		}
		return result;
	}
	
	/**
	 * 해당 locator의 엘리먼트 하위 url 링크의 정상 여부 확인
	 */
	public boolean isBrokenLinkExist(By locator) {
		boolean result = true;

		HashMap<String, WebElement> links = getAllLinks(locator);
		printLog("Total Links Count : " + links.size());

		for (String url : links.keySet()) {
			URL effectiveURL = null;
			try {
				effectiveURL = new URL(url);
				if (!isLinkChecked(effectiveURL)) {
					result = false;
				} else {
					this.afterCheckingURL(links.get(url));
					this.afterCheckingIMG(links.get(url));
				}
			} catch (Exception e) {
				// do nothing
			}
		}
		return result;
	}

	/**
	 * URL 링크 접속 후 상태 확인
	 * 
	 * @param url 유효성을 확인할 URL 객체
	 * @return OK, Not Found
	 */
	public boolean isLinkChecked(URL url) {
		boolean result = true;

		try {
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.connect();
			
			if(connection.getResponseCode() != 200) {
				printLog("URL: " + url);
				printLog(connection.getResponseCode() + " [" + connection.getResponseMessage() + "]");				
			}
			
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
	 * 이미지 엑박여부 확인
	 * @return 이미지 상태 boolean
	 */
	public Boolean isImagesPresent() {
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
	 * 해당 Element에서 노출되는 이미지가 정상 노출되는지 확인하는 메소드
	 * 
	 * @param WebElement element
	 * @return boolean 이미지 노출 여부
	 */
	public boolean isImagePresent(WebElement element) {
		Boolean ImagePresent = false;
		
		try {
			ImagePresent = (Boolean) ((JavascriptExecutor) this).executeScript(
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
	 * 특정 Text가 해당 페이지에서 노출되는지 확인하는 메소드
	 * 
	 * @param String 확인하고자하는 메소드
	 * @return boolean Text 노출 여부
	 */
	public boolean isTextPresent(String searchText) {
		boolean result = false;
		manage().timeouts().implicitlyWait(MIN_WAIT_TIME, TimeUnit.SECONDS);
		
		try {		
			if (findElement(By.xpath("//*[contains(text(),'"+searchText+"')]")) != null) {
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
	 * 해당 URL로 이동
	 * 
	 * @param url 이동하려는 주소
	 */
	@Override
	public void get(String url) {
		super.get(url);
		
		if(windowHandles.isEmpty()){
			mainWindowHandle = getWindowHandle(); 
			windowHandles.add(getWindowHandle());
		}
		waitForPageToLoaded();
	}

	/**
	 *  뒤로가기
	 */
	public void back() {
		navigate().back();
		waitForPageToLoaded();
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
	 * 앞으로가기
	 */
	public void forward() {
		navigate().forward();
		waitForPageToLoaded();
	}

	/**
	 * 입력한 숫자만큼 앞으로가기
	 * 
	 * @param count 앞으로 가려는 횟수
	 */
	public void forward(int count) {
		for (int total = 0; total < count; total++) {
			navigate().forward();
			waitForPageToLoaded();
		}
	}

	/**
	 * 새로고침 
	 */
	public void refresh() {
		navigate().refresh();
		waitForPageToLoaded();
	}
	
	/**
	 * 브라우저 메소드 최대크기
	 */
	public void windowMaximize() {
		manage().window().maximize();
	}
	
	/**
	 * @param util 드라이버 입력
	 * @param script 실행시킬 스크립트
	 * @return 실행결과 Object 반환
	 */
	public Object executeJavascript(ChromeUtil util, String script) {
		JavascriptExecutor js = (JavascriptExecutor) util;
		return js.executeScript(script);
	}
	
	/**
	 * 로그 출력
	 * 
	 * @param logs 출력하려는 로그 입력
	 */
	public void printLog(String logs) {
		System.out.println("[chrome] " + logs);

		if (logs.contains("JS error")) {
			logs = logs.replace("-", "=");
			if (ReportInfo.getLogBuf().indexOf(logs) < 0) {
				System.out.println("Last " + logs);
				ReportInfo.setLogBuf("[chrome] " + logs);
				ReportInfo.setLogBuf("|");
			}
		} else {
			ReportInfo.setLogBuf("[chrome] " + logs);
			ReportInfo.setLogBuf("|");
		}
	}
	
	/**
	 * 
	 * 해당 locator에서 우클릭해 context 메뉴 호출 후 event 선택, 
	 * link 관련 이벤트는 a태그, image 관련 이벤트는 img태그가 포함된 경우에만 사용 가능
	 * 
	 * @param locator 좌클릭하려는 Element의 locator
	 * @param contextEvent  사용하고 싶은 event
	 * @return 이벤트 클릭 성공 여부
	 */
	public boolean contextMenu(By locator, ContextEvent contextEvent) {
		int tryCnt = 0;
		WebElement element = null;
		Actions action = new Actions(this);
		Robot robot = null;

		while (tryCnt < MAX_TRY_COUNT) {
			try {
				robot = new Robot();
				if (locator != null) {
					WebDriverWait wait = (WebDriverWait) new WebDriverWait(this, TIME_OUT_SEC);
					element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
					element = wait.until(ExpectedConditions.elementToBeClickable(locator));
					scrollToElement(locator);
					action.contextClick(element).build().perform();
				} else {
					robot.keyPress(KeyEvent.VK_CONTEXT_MENU);
					robot.keyRelease(KeyEvent.VK_CONTEXT_MENU);
					sleep(0.5);
				}
				robot.keyPress(contextEvent.getKeyEvent());
				robot.keyRelease(contextEvent.getKeyEvent());
				return true;
			} catch (StaleElementReferenceException e) {
				printLog(locator + "는 유효하지 않은 경로입니다.");
				tryCnt++;
			} catch (AWTException e) {
				printLog("로봇 사용중에 애러가 발생했습니다.");
				tryCnt++;
			} catch (Exception e) {
				printLog(e.getMessage());
				fail(locator + "경로에 엘리먼트가 없습니다.");
				return false;
			}
		}
		fail(locator + " : 해당 엘리먼트가 MAX TRY COUNT(" + MAX_TRY_COUNT + ") 안에 노출되지 않았습니다.");
		return false;
	}

	/**
	 * 브라우저 context 메뉴 호출 후 event 선택
	 * link, image 관련 이벤트 사용 불가
	 * 
	 * @param contextEvent  사용하고 싶은 event
	 * @return 이벤트 클릭 성공 여부
	 */
	public boolean browserContextMenu(ContextEvent contextEvent) {
		try {
			if (contextEvent.name().contains("link"))	throw new Exception();
			if (contextEvent.name().contains("image")) throw new Exception();
			return contextMenu(null, contextEvent);
		} catch (Exception e) {
			printLog(e.getMessage());
			fail("경로가 없는 경우엔 link, image 관련 이벤트는 사용 할 수 없습니다.");
			return false;
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
	 * 물리키 입력
	 * 
	 * @param locator 키입력을 진행할 위치
	 * @param value 입력할 키값
	 */
	public void pressKeys(By locator, Keys value) {
		WebElement element = waitForIsElementPresent(locator);
		element.sendKeys(value);
	}
	
	/**
	 * 입력한 문자열 전부 삭제
	 * 
	 * @param locator 문자열 입력할 위치
	 */
	public void clear (By locator) {
		WebElement element = waitForIsElementPresent(locator);
		element.clear();
	}

	/**
	 * 해당 엘리먼트 주의에 실선 테두리 박스로 하이라이트 노출
	 * 
	 * @param element 하이라이트하려는 Element
	 */
	public void highlightElement(WebElement element) {
		JavascriptExecutor js = (JavascriptExecutor) this;
		String originStyle = element.getAttribute("style");
		js.executeScript("arguments[0].setAttribute('style', arguments[1]);", element, originStyle + "border: 3px solid yellow;");
		js.executeScript("arguments[0].setAttribute('style', arguments[1]);", element, originStyle);
	}

	/**
	 * 해당 locator의 element 주의에 실선 테두리 박스로 하이라이트 노출
	 * 
	 * @param By 하이라이트하려는 Element의 locator
	 */
	public void highlightElement(By locator) {
		JavascriptExecutor js = (JavascriptExecutor) this;
		WebElement element = waitForIsElementPresent(locator);
		String originStyle = element.getAttribute("style");
		js.executeScript("arguments[0].setAttribute('style', arguments[1]);", element, originStyle + "border: 3px solid yellow;");
		js.executeScript("arguments[0].setAttribute('style', arguments[1]);", element, originStyle);
	}

	/**
	 * link를 Check하기 전 배경색상 변경하는 메소드
	 * 
	 * @param WebElement
	 */
	public void beforeCheckingURL(WebElement element) {
		JavascriptExecutor js = (JavascriptExecutor) this;
		String originStyle = element.getAttribute("style");
		js.executeScript("arguments[0].setAttribute('style', arguments[1]);", element, originStyle + "background-color:#FF6C6C;");
	}

	/**
	 * link를 Check한 후 배경색상 변경하는 메소드
	 * 
	 * @param WebElement
	 */
	public void afterCheckingURL(WebElement element) {
		JavascriptExecutor js = (JavascriptExecutor) this;
		String originStyle = element.getAttribute("style");
		js.executeScript("arguments[0].setAttribute('style', arguments[1]);", element, originStyle + "background-color:#89FF82;");
	}
	
	/**
	 * 이미지의 link를 Check하기 전 이미지를 딤드시키는 메소드
	 * 
	 * @param WebElement
	 */
	public void beforeCheckingIMG(WebElement element) {
		JavascriptExecutor js = (JavascriptExecutor) this;
		String originStyle = element.getAttribute("style");
		js.executeScript("arguments[0].setAttribute('style', arguments[1]);", element, originStyle + "mix-blend-mode:multiply; opacity:0.3;");
	}
	
	/**
	 * 이미지의 link를 Check한 후 이미지를 딤드를 제거하는 메소드
	 * 
	 * @param WebElement
	 */
	public void afterCheckingIMG(WebElement element) {
		JavascriptExecutor js = (JavascriptExecutor) this;
		String originStyle = element.getAttribute("style");
		js.executeScript("arguments[0].setAttribute('style', arguments[1]);", element, originStyle + "opacity:1;");
	}
	
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
	 * 해당 Element가 체크되었는지 확인
	 * 
	 * @param locator 확인하려는 Elemenet
	 * @return Element의 체크 여부 boolean 반환
	 */
	public boolean isChecked(By locator) {
		try {
			WebElement element = locator.findElement((SearchContext) this);
			if (element.isSelected()) {
				return true;
			}
		} catch (NoSuchElementException e) {
			return false;
		}
		return false;
	}

	/**
	 * Element가 존재할 때까지 대기 후 해당 Element 반환 
	 * 
	 * @param locator 가져오려는 엘리먼트 loacator
	 * @return 찾은 WebElement 반환 
	 */
	public WebElement waitForIsElementPresent(By locator) {
		int tryCnt = 0;
		WebDriverWait wait = (WebDriverWait) new WebDriverWait(this,	PAGE_LOAD_TIME_OUT);
		WebElement element = null;
//		final Logger log = Logger.getLogger(ExpectedConditions.class.getName());
		while (tryCnt < MAX_TRY_COUNT) {
			try {
		
				element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
				
				//nosuch 로그 제거용
//				element = (new WebDriverWait(this, 2)).until(new ExpectedCondition<WebElement>() {
//
//					@Override
//					public WebElement apply(WebDriver d) {
//						
//						return d.findElement(locator);
//						try{
//							return d.findElements(By.className("gnb_txt")).stream().findFirst().orElseThrow(
//									() -> new NoSuchElementException("Cannot locate an element using " + locator));
//							
//						} catch (WebDriverException e) {
//						      log.log(Level.WARNING,
//						              String.format("WebDriverException thrown by findElement(%s)", locator), e);
//						      throw e;
//						}
//					}
//				});

				if (element != null) {				
					highlightElement(element);
					return element;	
				} else {
					printLog ("null element..!!");
					return null;
				}
			} catch (StaleElementReferenceException e) {
				printLog("** Catch StaleElement Exception : #" + tryCnt);
				sleep(1);
				tryCnt = tryCnt + 1;
			} catch (Exception e) {
				printLog(e.getMessage());
				return null;
			}
		}
		fail (locator + " : 해당 엘리먼트가 " + TIME_OUT_SEC	+ "초내에 노출되지 않음");
		return null;
	} 
	
	/**
	 * locator에 해당하는 모든 엘리먼트 반환 
	 * 
	 * @param locator 가져오려는 Element의 locator
	 * @return locator에 해당하는 모든 엘리먼트 List
	 */
	public List<WebElement> waitForIsAllElementsPresent (By locator) {
		int tryCnt = 0;
		WebDriverWait wait = new WebDriverWait(this, PAGE_LOAD_TIME_OUT);

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
	 * 프레임 전환
	 * 
	 * @param frameName 전환하려는 프레임 이름
	 */
	public void switchToFrame(By frameName) {
		WebElement element = waitForIsElementPresent(frameName);
		switchTo().frame(element);
	} 
	
	/**
	 * 해당 locator의 Element에 마우스 위로 이동
	 * 
	 * @param locator 마우스 위로 이동하려는 Element의 locator
	 */
	public void mouseOver (By locator) {
		int target_width = 0;
		int target_height = 0;
		 
	    Actions action = new Actions (this);	    
		WebElement element = waitForIsElementPresent(locator);
	    
	    target_width = (element.getSize().width)/2;
	    target_height = (element.getSize().height)/2;
	    
	    action.moveToElement(element, target_width, target_height).perform();	    
	    waitForPageToLoaded();
	 }
	
	/**
	 * 해당 Element에 마우스 위로 이동
	 * 
	 * @param element 마우스 위로 이동하려는 Element
	 */
	public void mouseOver(WebElement element) {
		int target_width = 0;
		int target_height = 0;
		
		Actions action = new Actions(this);
		target_width = (element.getSize().width) / 2;
		target_height = (element.getSize().height) / 2;
		action.moveToElement(element, target_width, target_height).perform();
		
		waitForPageToLoaded();
	}
	
	/**
	 * 해당 Element의 마우스 위로 이동
	 * 
	 * @param locator 마우스 위로 이동하려는 Element의 Locator
	 * @param xOffset Locator의 X offset
	 * @param yOffset Locator의 Y offset
	 */
	public void mouseOver (By locator, int offset_X, int offset_Y) {
	    Actions action = new Actions (this);	    
		WebElement element = waitForIsElementPresent(locator);
	
	    action.moveToElement(element, offset_X, offset_Y).perform();	    
	    waitForPageToLoaded();
	 }

	/**
	 * 해당 locator의 Element 클릭
	 * 
	 * @param locator 클릭하려는 Element의 locator
	 * @return 클릭 성공 여부 boolean 반환
	 */
	public boolean click(By locator) {
		WebDriverWait wait = new WebDriverWait(this, PAGE_LOAD_TIME_OUT);
		WebElement element = null;
		
		for (int i = 0; i < MAX_TRY_COUNT; i++) {
			element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
			try {
				if (element != null) {
					highlightElement(element);
					executeScript("arguments[0].click()", element);
					waitForPageToLoaded();
					
					return true;
				}else {
					fail (">>> Element가 null값으로 Click동작을 수행할 수 없습니다.");
					return false;
				}
			} catch (StaleElementReferenceException e) {
				printLog("** Catch StaleElement Exception : #" + i);
			} catch(UnhandledAlertException uae){
				printLog(">>> Click동작으로 Alert이 노출되었습니다. ");
				return true;
			} catch (NoSuchWindowException nswe) {
				printLog(">>> Click동작으로 Window가 닫혔습니다. ");
				return true;
			} catch (Exception e) {
				printLog(e.getMessage());
			}
			sleep(1);
		}
		fail(locator + " : 해당 엘리먼트가 " + TIME_OUT_SEC + "초내에 노출되지 않음");
		return false;
	}
	
	/**
	 * Element 클릭 후 기다리는 메소드
	 * 
	 * @param WebElement
	 */
	public boolean click(WebElement element) {		
		for (int i = 0; i < MAX_TRY_COUNT; i++) {
			WebElement ele = element;
			try {
				if (ele != null) {
					highlightElement (ele);
					int beforeClick = getWindowHandles().size();
					executeScript("arguments[0].click()", ele);
					int afterClick = getWindowHandles().size();

					if (beforeClick > afterClick) {
						printLog(">>> Click 동작으로 브라우저가 닫혔습니다. PageToLoaded를 수행하지 않습니다.");
					} else {
						waitForPageToLoaded();
					}
					return true;
				}else {
					fail (">>> Element가 null값으로 Click동작을 수행할 수 없습니다.");
					return false;
				}
			} catch (StaleElementReferenceException e){
				printLog ("** Catch StaleElement Exception : #" + i);
			} catch(UnhandledAlertException uae){
				printLog(">>> Click동작으로 Alert이 노출되었습니다. ");
				return true;
			} catch (NoSuchWindowException nswe) {
				printLog(">>> Click동작으로 Window가 닫혔습니다. ");
				return true;
			} catch (Exception e) {
				printLog (e.getMessage());
			}
			sleep(1);
		}
		fail (element + " : 해당 엘리먼트가 " + TIME_OUT_SEC + "초내에 노출되지 않음");
		return false;
	}
	
	/**
	 * locator에 해당하는 Element 더블 클릭
	 * 
	 * @param locator 클릭하려는 Element의 locator
	 * @return 더블클릭 성공 여부 boolean 반환
	 */
	public boolean doubleClick(By locator) {		 
		WebElement element = null;
		WebDriverWait wait = new WebDriverWait(this, PAGE_LOAD_TIME_OUT);
		Actions action = new Actions(this);
		
		for (int i = 0; i < MAX_TRY_COUNT; i++) {
			try {
				element = wait.until(ExpectedConditions.elementToBeClickable(locator));
				if (element != null) {
					highlightElement (element);
					int beforeClick = getWindowHandles().size();
					action.doubleClick(element).perform();
					int afterClick = getWindowHandles().size();

					if (beforeClick > afterClick) {
						printLog(">>> Click 동작으로 브라우저가 닫혔습니다. PageToLoaded를 수행하지 않습니다.");
					} else {
						waitForPageToLoaded();
					}
					return true;
				}else {
					fail (">>> Element가 null값으로 Click동작을 수행할 수 없습니다.");
				}
			}
			catch (StaleElementReferenceException e){
				printLog ("** Catch StaleElement Exception : #" + i );
			}
			catch (Exception e) {
				printLog (e.getMessage());
				return false;
			}
			sleep(1);
		}
		fail (locator + " : 해당 엘리먼트가 " + TIME_OUT_SEC + "초내에 노출되지 않음");
		return false;
	} 
	
	/**
	 * Select 메뉴 선택 
	 * 
	 * @param selectLocator Select의 locator 정보
	 * @param byType 셀렉트 Type 지정 (text 또는 index 또는 value)
	 * @param optionLocator 셀렉트할 정보
	 * @return Select 성공 여부 boolean 반환
	 */
	public boolean select(By selectLocator, String byType, String optionLocator) {
		boolean result = false;
		WebElement element = waitForIsElementPresent(selectLocator);
		Select select = new Select(element);

		try {
			if (byType.equals("text")) {
				select.selectByVisibleText(optionLocator);
			} else if (byType.equals("index")) {
				select.selectByIndex(Integer.parseInt(optionLocator));
			} else if (byType.equals("value")) {
				select.selectByValue(optionLocator);
			} else {
				printLog("unknown option type");
				return false;
			}
			waitForPageToLoaded();
			result = true;
		} catch (NoSuchElementException e) {
			result = false;
		}
		return result;
	}

	/**
	 * Select 엘리먼트 반환
	 * @param locator 가져오려는 Select의 locator
	 * @return 해당 locator의 Select 반환
	 */
	public Select select(By locator) {
		WebElement element = waitForIsElementPresent(locator);
		Select select = new Select(element);

		return select;
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
	 * form에서 submit
	 * 
	 * @param formLocator submit 하려는 form의 locator
	 */
	public void submit(By formLocator) {
		WebElement element = waitForIsElementPresent(formLocator);
		element.submit();
		waitForPageToLoaded();
	}
	
	/**
	 * Alert 가져오기
	 * 
	 * @return Alert 객체 반환
	 */
	public Alert getAlert() {
		WebDriverWait wait = new WebDriverWait(this, PAGE_LOAD_TIME_OUT);
		Alert alert = wait.until(ExpectedConditions.alertIsPresent());
		
		return alert;
	}
	
	/**
	 * 클릭 후 노출되는 Alert 가져오기
	 * 
	 * @param locator 클릭하려는 Element의 locator 정보
	 * @return 클릭 후 노출 된 Alert 객체 반환
	 */
	public Alert getAlert(By locator) {
		WebDriverWait wait = new WebDriverWait(this, PAGE_LOAD_TIME_OUT);

		WebElement element = waitForIsElementPresent (locator);
		executeScript("arguments[0].click()", element);
		
		Alert alert = wait.until(ExpectedConditions.alertIsPresent());

		return alert;
	}

	/**
	 * Element가 Select되었는지 확인
	 * 
	 * @param locator 확인하려는 Element의 locator
	 * @return Select 되었는지 여부 boolean 반환
	 */
	public boolean isSelected (By locator) {
		WebDriverWait wait = (WebDriverWait) new WebDriverWait(this, PAGE_LOAD_TIME_OUT);

		try {
			wait.until(ExpectedConditions.presenceOfElementLocated(locator));
			if (wait.until(ExpectedConditions.elementToBeSelected(locator))) { 
				return true;
			}
		} catch (Exception e){
			printLog (e.getMessage());
			return false;
		}
		fail (locator + " : 해당 엘리먼트가 " + TIME_OUT_SEC + "초내에 select 되지 않음");
		return false;
	}
	
	/**
	 * Element가 Visible인지 확인
	 * 
	 * @param locator 확인하려는 Element의 locator
	 * @return Visible 상태 여부 boolean 리턴
	 */
	public WebElement waitForIsVisible(By locator) {
		WebDriverWait wait = new WebDriverWait(this, PAGE_LOAD_TIME_OUT);
		WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
		element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));

		try {			
			if (element.isDisplayed()) {
				return element;
			}
		} catch (Exception e){
			printLog (e.getMessage());
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
		WebDriverWait wait = new WebDriverWait(this, PAGE_LOAD_TIME_OUT);
		WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
		
		try {
			if (wait.until(ExpectedConditions.invisibilityOfElementLocated(locator))) {
				return element;
			}
		} catch (Exception e){
			printLog (e.getMessage());
			return null;
		}
		printLog (locator + " : 해당 엘리먼트가 " + TIME_OUT_SEC + "초내에 invisible 되지 않음");
		return null;
	}
	
	/**
	 * 해당 텍스트가 존재하는지 확인
	 * 
	 * @param searchText 확인하려는 텍스트
	 * @return 해당 텍스트 존재 여뷰 boolean 반환
	 */
	public WebElement waitForIsTextPresent (String searchText) {
		
		for (int i = 0; i < MAX_TRY_COUNT; i++) {
			if (waitForIsElementPresent(By.xpath("//*[contains(text(),'" + searchText + "')]")) != null) {
				return waitForIsElementPresent(By.xpath("//*[contains(text(),'" + searchText + "')]"));
			}
			sleep(1);
		}
		printLog (searchText + " : 해당 Text가 " + TIME_OUT_SEC + "초내에 나타나지 않음");
		return null;
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
	 * locator에 해당하는 value attribute값 가져오기
	 * 
	 * @param locator value값이 존재하는 locator 
	 * @return 해당 locator에 존재하는 value 값 String 반환
	 */
	public String getValue (By locator) {
		WebElement element = waitForIsElementPresent(locator);
		return element.getAttribute("value");
	}
	
	/**
	 * 해당 locator의 Attribute 값 가져오기
	 * 
	 * @param locator Attriubute를 확인하려는 locator
	 * @param attribute 확인하려는 Attribute 
	 * @return 해당 Element의 Attribute 값 String 반환
	 */
	public String getAttribute (By locator, String attr) {
		WebElement element = waitForIsElementPresent(locator);
		return element.getAttribute(attr);
	}
	
	/**
	 * 해당 Element의 Attribute 값 가져오기
	 * 
	 * @param element Attriubute를 확인하려는 Element
	 * @param attribute 확인하려는 Attribute 
	 * @return 해당 Element의 Attribute 값 String 반환
	 */
	public String getAttribute(WebElement element, String attribute) {
		return element.getAttribute(attribute);
	}
	
	/**
	 * 해당 Element의 css value 가져오기
	 * 
	 * @param locator css value를 가져오려는 locator
	 * @param propertyName 가져오려는 value
	 * @return 해당 Element의 css value String으로 반환
	 */
	public String getCssValue(By locator, String propertyName) {
		WebElement element = waitForIsElementPresent(locator);
		return element.getCssValue(propertyName);
	}
	
	/**
	 * 해당 Element의 css value 가져오기
	 * 
	 * @param locator css value를 가져오려는 Element
	 * @param propertyName 가져오려는 value
	 * @return 해당 Element의 css value String으로 반환
	 */
	public String getCssValue(WebElement element, String propertyName) {
		return element.getCssValue(propertyName);
	}
	
	/**
	 * 현재 frame을 가져온다.
	 * @return 현재 프레임 String 반환
	 */
	public String getFrame () {
		JavascriptExecutor jsExecutor = (JavascriptExecutor) this;
		return (String) jsExecutor.executeScript("return self.location.toString()");
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
	
	/**
	 * 해당 타이틀을 가진 페이지로 이동했는지 확인
	 * 
	 * @param Title 이동 확인하려는 페이지의 타이틀
	 * @return 해당 타이틀을 가진 페이지로 이동했는지 여부 boolean 반환
	 */
	public boolean waitForTitle(String Title) {
		WebDriverWait wait = new WebDriverWait(this, PAGE_LOAD_TIME_OUT);
		
		if (wait.until(ExpectedConditions.titleContains(Title))) {
			return true;
		}
		fail (Title + " : 해당 Title을 포함하는 페이지가 " + TIME_OUT_SEC + "초내에 노출되지 않음");
		return false;
	}
	
	/**
	 * 메인 윈도우로 전환
	 */
	public void selectMainWindow() {
		switchTo().window(windowHandles.get(0));
		String firstHandle = windowHandles.get(0);
		windowHandles.remove(0);
		windowHandles.add(firstHandle);
	}
	

	/**
	 * 특정 윈도우로 전환
	 */
	public void selectWindow(String winHandle) {
		switchTo().window(winHandle);
		
		for(String handle : windowHandles) {
			
			if(handle.equals(winHandle)) {
				switchTo().window(handle);
				windowHandles.remove(handle);
				windowHandles.add(handle);
				break;
			}
		}
	}
	
	/**
	 * 윈도우 닫기
	 * 
	 * @param winHandle 닫으려는 윈도우 handle
	 */
	public void closeWindow(String winHandle) {
		switchTo().window(winHandle).close();
		
		for(String handle : windowHandles) {
			
			if(handle.equals(winHandle)) {
				windowHandles.remove(handle);
			}
		}
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
	 * Element 드래그 앤 드랍 
	 * 
	 * @param sourceLocate 드래그하려는 Element
	 * @param tagetLocate 드랍하려는 Element
	 */
	public void dragAndDrop (By sourceLocate, By tagetLocate) {
		WebElement source_element = null;
		WebElement target_element = null;
	    Actions action = new Actions (this);
	    
	    source_element = waitForIsElementPresent (sourceLocate);
	    target_element = waitForIsElementPresent (tagetLocate);
	    
	    action.dragAndDrop(source_element, target_element).perform();  
	 }

	/**
	 * 페이지의 load complete 대기 @ - Exception
	 */
	public void waitForPageToLoaded() {

		LogEntries logEntries = this.manage().logs().get(LogType.BROWSER);
		for (LogEntry entry : logEntries) {
			if (!entry.getLevel().toString().equalsIgnoreCase("WARNING")) {
				if (!AutomationReport.jsErrorResult.contains("<a href="+getCurrentUrl()+" target=\"_black\"'>" +getCurrentUrl()+ "</a><br>" + entry.getMessage())) {
					AutomationReport.jsErrorResult.add("<a href="+getCurrentUrl()+" target=\"_black\"'>" +getCurrentUrl()+ "</a><br>" + entry.getMessage());
				}
			}
		}

		try {
			waitForJSLoaded();
		} catch (Exception e) {
			printLog(e.getMessage());
		}
		
		this.getPerformanceTiming();
	}

	/*
	 * 페이지의 JS가 로드될때 까지 대기라는 메소드
	 * Angular의 경우 사용 여부 판단 후 wait
	 */
	private boolean waitForJSLoaded() {
	    WebDriverWait wait = new WebDriverWait(this, PAGE_LOAD_TIME_OUT);
	    
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
	 * JavaScript를 이용하여 Element가 화면에 노출되도록 이동하는 메소드
	 * 
	 * @param By 이동하려는 Element의 locator
	 */
	public void scrollToElement (By locator) {
		WebElement element = waitForIsElementPresent(locator);		
		executeScript("arguments[0].scrollIntoView(true);", element);
	}
	
	/**
	 * JavaScript를 이용하여 Element가 화면에 노출되도록 이동하는 메소드
	 * 
	 * @param WebElement 이동하려는 Element
	 */
	public void scrollToElement (WebElement element) {
		executeScript("arguments[0].scrollIntoView(true);", element);
	}

	/**
	 * JavaScript를 이용하여 Element가 화면에 노출되도록 이동하는 메소드
	 * 단, 화면에 해당 Element가 노출 중인 경우 이동하지 않음
	 * 
	 * @param By 이동하려는 Element의 locator
	 */
	public void scrollToElementIfNeeded (By locator) {
		WebElement element = waitForIsElementPresent(locator);		
		executeScript("arguments[0].scrollIntoViewIfNeeded(true);", element);
	}

	/**
	 * JavaScript를 이용하여 Element가 화면에 노출되도록 이동하는 메소드
	 * 단, 화면에 해당 Element가 노출 중인 경우 이동하지 않음
	 * 
	 * @param WebElement 이동하려는 Element
	 */
	public void scrollToElementIfNeeded (WebElement element) {
		executeScript("arguments[0].scrollIntoViewIfNeeded(true);", element);
	}
	
	public String takeScreenShot(String callerName, String browserName) {
		String destDir;
		DateFormat dateFormat;
		destDir = "automation-output//screenshots";
		
		File scrFile = ((TakesScreenshot) this).getScreenshotAs(OutputType.FILE);
		dateFormat = new SimpleDateFormat("dd-MMM-yyyy_hh_mm_ssaa");

		new File(destDir).mkdirs();
		String destFile = dateFormat.format(new Date()) + "_" + browserName + "_" + callerName + ".png";
		String imgLink = "screenshots//" + destFile;

		try {
			FileUtils.copyFile(scrFile, new File(destDir + "//" + destFile));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return imgLink;
	}	
	
	/**
	 * 두 문자열 간의 유사성 확인
	 * 
	 * @param text 문자열
	 * @param comparisonTarget 비교하려는 문자열 
	 * @param rate 만족해야 하는 유사성 입력 0.0 ~ 1.0 (0~100%)
	 * @return 두 문자열의 일치율이 rate 이상일경우 true 아닐경우 false 반환
	 */
	public boolean checkSimilarText(String text, String comparisonTarget, double rate) {
		ArrayList<Character> textCharacters = new ArrayList<Character>();
		ArrayList<Character> comparisonTargetCharacters = new ArrayList<Character>();
		Double concordanceRate = 0.0;
		int misMatch = 0;
		int textSize = 0;

		for (int i = 0; i < text.length(); i++) {
			textCharacters.add(text.charAt(i));
		}

		textSize = textCharacters.size();

		for (int i = 0; i < comparisonTarget.length(); i++) {
			comparisonTargetCharacters.add(comparisonTarget.charAt(i));
		}

		if (text.length() < comparisonTarget.length()) {
			for (int i = 0; i < textCharacters.size(); i++) {
				char textChar = textCharacters.get(i);
				for (int j = 0; j < comparisonTargetCharacters.size(); j++) {
					if (comparisonTargetCharacters.get(j).equals(textChar)) {
						comparisonTargetCharacters.remove(j);
					}
				}
			}
			printLog("불일치 문자: " + comparisonTargetCharacters);
			misMatch = comparisonTargetCharacters.size();
		} else {
			for (int i = 0; i < comparisonTargetCharacters.size(); i++) {
				char comparisonTargetChar = comparisonTargetCharacters.get(i);
				for (int j = 0; j < textCharacters.size(); j++) {
					if (textCharacters.get(j).equals(comparisonTargetChar)) {
						textCharacters.remove(j);
					}
				}
			}
			printLog("불일치 문자: " + textCharacters);
			misMatch = textCharacters.size();
		}
		concordanceRate = 1 - ((double) misMatch / textSize);
		printLog("일치율: " + concordanceRate);

		if (concordanceRate >= rate) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 현재 페이지의 cookie 정보를 가져오는 메소드
	 * 
	 * @param driver util
	 */
	public void getCookie(ChromeUtil util) {
		cookieSet = util.manage().getCookies();
	}
	
	/**
	 * get으로 가져온 cookie 정보를 브라우저에 Setting 해주는 메소드
	 * 
	 * @param driver util
	 */
	public void setCookie(ChromeUtil util) {
		for(Cookie cookie : cookieSet){
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
	 * 해당 locator의 element값을 가져오는 메소드
	 * 
	 * @param By Element를 가져올 locator
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
	 * 해당 locator의 element값을 가져오는 메소드
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
	 * 특정 Element의 하위 Element를 가져오는 메소드
	 * 
	 * @param WebElement base가 될 Element
	 * @param By 하위 Element의 locator
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
	 * 새 윈도우(탭) 생성, 생성 후 해당 윈도우(탭)으로 스위치 필요
	 * 
	 * @param url
	 *            새 윈도우(탭)을 생성하며 이동하려는 url
	 */
	public void openNewWindow(String url) {
		beforeWindowHandle = getWindowHandle();
		JavascriptExecutor js = (JavascriptExecutor) this;
		js.executeScript("window.open('" + url + "', '_blank')");
		switchToNewWindow();
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
							waitForPageToLoaded();
						} else {
							windowHandles.remove(getWindowHandle());
							windowHandles.add(getWindowHandle());
						}
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
						waitForPageToLoaded();
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
	
	/**
	 * switchToNewWindow를 이용해서 포커싱한 윈도우를 닫고 이전 Window로 포커싱하는 메소드
	 * 
	 * @return boolean 윈도우의 close 여부
	 */
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
	
	public MediaUtil getMedia(By locater) {
		media = new MediaUtil(this, locater);

		return media;
	}
	
	/* 
	 * 배열의 마지막 Handle을 삭제하는 메소드
	 * 취소버튼으로 팝업 or 탭이 닫히는 Click메소드 다음에 사용
	 */
	public void removeLastWindow() {
		switchToBeforeWindow();
		windowHandles.remove(windowHandles.size() - 2);
	}
}

