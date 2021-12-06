# STAC - Simple Test Automation Client

- STAC(Simple Test Automation Client) Web과 App의 UI 자동화 테스트를 지원합니다.
- 자동화 수행 완료 후 Report를 local 파일로 생성되며 fail 당시 screen capture와 log를 제공합니다.


## Maven User
`build.grale`에 <http://repo.navercorp.com/maven2/>를 저장소로 추가합니다.


 - Add the dependency:
 
```xml
	<dependency>
		<groupId>com.nts.sqa</groupId>
		<artifactId>stac-client</artifactId>
  		<version>1.0.0</version>
	</dependency>
```

 - Add the repositories:
 
```xml
<repositories>
  <repository>
    <id>navercorp.release</id>
    <url>http://repo.navercorp.com/maven2</url>
    <snapshots>
      <enabled>false</enabled>
    </snapshots>
    <releases>
      <enabled>true</enabled>
    </releases>
  </repository>

  <repository>
	<id>navercorp.snapshot</id>
	<url>http://repo.navercorp.com/m2-snapshot-repository</url>
    <snapshots>
      <enabled>true</enabled>
    </snapshots>
    <releases>
      <enabled>false</enabled>
    </releases>
  </repository>
</repositories>
```

# 사용 방법
- STAC을 사용하기 위해선 JDK 1.8이상 설치 및 환경변수 설정이 되어있어야합니다.
- 모바일 단말기 자동화를 위해 SDK와 Appium을 설치해주세요.
- [Appium install guide](https://github.com/appium/appium/blob/master/docs/en/about-appium/getting-started.md) node 버전 설치
- SDK와 appium은 항상 최신버전으로 설치 상태를 유지하셔야 합니다.

 

## Formatter 상속
- UI자동화를 시작하기 위해서는 Formatter Class를 상속받아야합니다.
- Formatter Class에는 아래 Annotation이 정의되어있습니다.
  
```
	@BeforeSuite, @BeforeClass, @BeforeMethod
	@AfterSuite, @AfterClass, @AfterMethod
```
 
- 상속을 받은 Class에서 @BeforeClass 생성한 후 각 Driver를 생성해주시면됩니다.
- Automation Class 자동화 환경 셋팅을 하기위한 Method가 정의되어있습니다. 
   
 
```java
public class AutomationTest extends Formatter {

    private ChromeUtil chrome;

    @BeforeClass
    public static void setupClass() {
		chrome = (ChromeUtil) new Automation()
			chrome = (ChromeUtil) new Automation()
			.pc()
			.chrome()
			.mainUrl("TestURL")
			.start();
    }

    @AfterClass
    public void teardown() {
        if (chrome != null) {
            super.quit();
        }
    }

    @Test
    public void test() {
        // Your test code here
    }
}
```

- Formatter를 상속받아 추가로 TestNG annotation을 구현한 경우 수행순서는 다음과 같습니다.
  - Before Annotation의 경우 Formatter의 annotation이 먼저 수행됨
  - After Annotation의 경우 상속받은 Class의 Annotation이 먼저 수행됨


## Driver 생성 메소드 설명
                
| 메소드                                | 설명                                                                                                                                                                                                                                                                                                                                                  | 자동화 환경별 필수 사용 여부                                                                                                                                                          |
|---------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| ``pc()``                   | 자동화 프로젝트의 수행 환경을 PC로 설정하는 메소드.                                                                                                                                                                                                           | Web자동화 진행시 필수 |
| ``iOS()``                | 자동화 프로젝트의 수행 환경을 iOS로 설정하는 메소드.                                                                                                                                                                                                                                                                                    | iOS자동화시 필수                                                                                                                                                                    |
| ``android()``                      | 자동화 프로젝트의 수행 환경을 Android로 설정하는 메소드.                                                                                                                                           | android자동화시 필수                                                                                                                                                               |
| ``android(int deviceNumber)``                   | 자동화 프로젝트의 수행 환경을 Android로 설정하는 메소드. 연결되어 있는 단말기 중 Device List에서 인자를 통해 자동화를 수행할 단말기를 선택할 수 있음.                                                                                                                                               | ``android()``와 선택하여 사용                                                                                                                                                                 |
| ``mobileWeb()``                 | 수행할 자동화 프로젝트의 환경이 Mobile Web인 경우 필요한 정보를 Capability에 Setting.                                                                                                                                                                                                                                        | mobile Web 자동화시 필수                                                                                                                                                          |
| ``mobileWeb(EmulatorList emulator)``        | Chrome을 이용한 Mobile Web 자동화를 진행하는 경우 필요한 정보를 Capability에 Setting, 자동화를 수행 할 emulator를 인자를 통해 설정할 수 있음.                                                                                              | chrome 브라우저의 mobile 버전사용시 필수                                                                                                                                                                  |
| ``chrome()``                          | PC 브라우저의 자동화를 진행할 경우 수행 브라우저를 Chrome으로 선택하는 메소드.                                                                                                                                                                                                                                                                                                    | Web browser 자동화시 필수                                                                                                                                                               |
| ``setCapability(String key, Object value)``                          | 제공되고 있는 메소드를 통해 설정이 불가능한 Capability를 추가로 설정할 수 있는 메소드. 기존 Capability Setting과 동일하게 key와 value로 설정 가능.                                                                                                                                                                                                                                                                                                    |                                                                                                                                                                |
| ``useHeadless()``  | Chrome 브라우저의  Headless기능 Setting.                                                                                                                                                    |                                                                                                                                       |
| ``useEventChecker()``          | Android 앱 자동화시 EventChecker의 사용여부를 설정해주는 메소드.                                                                                                                                                                                                                                      |                          |
| ``mainUrl()``                       | 브라우저 자동화를 수행하는 경우 첫 진입 URL을 설정해주는 메소드.                                                                 | Web browser 자동화시 필수                                                                                                                                                                |
| ``packageName(String packageName)``                     | 앱 자동화를 수행할 경우 타겟이되는 앱의 패키지명을 설정해주는 메소드.                                                                                                              | iOS 또는 Android App 자동화시 필수                                                                                                                                                                         |
| ``activityName(String activityName)``                 | 앱 자동화를 수행할 경우 타겟이되는 앱의 엑티비티명을 설정해주는 메소드.                                                                                                                                                                                                                     | Android 자동화시 필수                                                                                                                                                                     |
| ``apkFile(String apkPath)``                 | 앱 자동화를 수행할 경우 APK를 설치해야할 때 사용하는 메소드. 자동화를 수행할 APK의 Path를 통해 앱을 단말기에 설치한다.                                                                                                                                                                                                                     |                                                                                                                                                                      |
| ``apkUrl(String address)``         | APK가 server에 등록되어 있는 경우 URL을 통해 설치해주는 메소드.  APK를 다운로드한 후 단말기에 설치해 준다. APK는 PC의 C:\\apkDownload 경로에 설치됨.                                                                                                                                                                                                                                                                                                                       |                                                                                                                                                                 |
| ``logLevel(String loglevel)``           | Appium의 loglevel을 설정해주는 메소드. debug, error 중 선택가능.                                                                                                                                                                                                                                                                                   |                                                                                                                                                                |
| ``screenshotLever(String screenShotLevel)``         |스크린샷 캡처할 level을 설정하는메소드. String으로 파라미터를 받으면 all/failed 중 선택 가능. failed의 경우 메소드 결과가 fail인 경우에만 캡처됨.                                                                                                                                                                                                                                                                                       |                                                                                                                                                              |
| ``remapHost(String host, String overrideHost)``       | 호스트를 설정하는 메소드. 일반적인 호스트 설정과 동일하게 사용 가능.                                                                                                                                                                                                                                                                                                                     |                                                                                                                                                                      |
| ``setADBPath(String adbPath)``   | Mac에서 Android 자동화를 진행하는 경우 필요. ADB가 설치된 경로를 입력하여 사용가능.                                                                                                                                                                                                                                                                                                                     |                                                                                                                                                                      |
| ``start()``                      | 자동화 설정 후 각 Driver를 실행하는 메소드. Android, iOS, PC 모두 분기처리 되어있으면 Object를 return하여 각 Drvier에 맞게 형변환이 필요함.                                                                                                                                                                                                                                                                               | 자동화 수행시 필수                                                                                                                                                                       |
| ``stop()``                | 자동화 수행 완료 후 Driver를 종료하는 메소드.                                                                                                                                                                                                                                                                       | 자동화 수행 완료시 필수                                                                                                                                                                    |


## 결과 리포트
- 자동화 수행 완료 후 결과리포트가 자동으로 생성되며 /automation-output/Automation-Test-Reprot.html 경로에 생성됩니다.
- 자동화 수행 중 캡쳐된 ScreenShot이 /automation-output/screenshots 폴더에 저장되며 리포트 생성시 참조됩니다.


## Example
+ [Chrome Example](https://oss.navercorp.com/sqa/SQA-Auto-Client/blob/master/src/test/java/com/ntscorp/auto_client/ChromeAutomationExample.java)
+ [Android Example](https://oss.navercorp.com/sqa/SQA-Auto-Client/blob/master/src/test/java/com/ntscorp/auto_client/AndroidAutomationExample.java)
+ [iOS Example](https://oss.navercorp.com/sqa/SQA-Auto-Client/blob/master/src/test/java/com/ntscorp/auto_client/iOSAutomationExample.java)
+ [API Example](https://oss.navercorp.com/sqa/SQA-Auto-Client/blob/master/src/test/java/com/ntscorp/auto_client/APIAutomationExample.java)


## FAQ
 - STAC은 NTS SQA 조직의 자동화 라이브러리의 경량화 버전입니다.
 - 일부 기능이나 연동 환경에 차이가 있을 수 있습니다.
 - 자동화 이슈 또는 궁금한점은 issue등록 또는 아래 DL로 연락부탁드립니다.
 - dl_sqa_automation_test@nts-corp.com
 - issue 등록시 필요 정보
   - Node Version:
   - Appium & Chrome Version:
   - Device SDK Version:
   
 
