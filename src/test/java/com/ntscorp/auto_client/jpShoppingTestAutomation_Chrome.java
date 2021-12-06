package com.ntscorp.auto_client;

import com.ntscorp.auto_client.selenium.ChromeUtil;
import com.ntscorp.auto_client.verity.Verify;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;


public class jpShoppingTestAutomation_Chrome extends Formatter implements Utilities{

    ChromeUtil chrome;

    private final String SELLER_ID = "nvqa_gncp_80@naver.com";  // 센터 ID
    private final String SELLER_PW = "gncp80";  // 센터 PW
    private final String CUSTOMER_ID = "nvqa_nshop69@naver.com";  // 구매자 ID
    private final String CUSTOMER_PW = "qatest123";  // 센터 PW

    public String orderItemId;  // 주문번호
    public String trackingStat = "배달완료";  // 배송상태
    public String trackingNo = "test"+getTimeForTrackingNo();  // 송장번호용 (현재시간 yyyymmddhhmm)

    @BeforeClass
    public void setup() {
        chrome = (ChromeUtil) new Automation()
                .pc()
                .chrome()
                .mainUrl("https://mysmartstore.jp/my/order-info")
                .start();
    }


    @Test(description = "쇼핑MY: 로그인 후 주문내역 페이지 진입되는가")
    public void TC_01_MY_로그인_주문내역() {
        chrome.type(By.name("tid"), CUSTOMER_ID);
        chrome.type(By.name("tpasswd"), CUSTOMER_PW);
        chrome.click(By.className("MdBtn01"));
        chrome.refresh();
        chrome.waitForPageToLoaded();
        chrome.waitForIsTextPresent("注文履歴");
    }

    @Test(description = "신용카드 결제 후 주문완료가 되는지")
    public void TC_02_MY_상품주문_신용카드결제() throws InterruptedException {
        chrome.get("https://1ststore.mysmartstore.jp/products/100004253");
        chrome.click(By.xpath("//*[@id='content']/div/div[1]/div[2]/div[4]/div/button[1]"));  // 담기 버튼 클릭
        chrome.click(By.xpath("//*[@id='MODAL_ROOT_ID']/div/div[2]/div/div[4]/button"));  // 모달에서 담기 확정
        chrome.waitForIsTextPresent("見に行く");
        chrome.click(By.xpath("//*[@id='headerGnb']/div/div/button[2]"));  // 헤더 장바구니 버튼 클릭
        // chrome.click(By.id("top_checkbox_all")); // 스토어 전체선택
        Thread.sleep(1000);
        chrome.click(By.className("Cart_txt__mDwoT"));  //주문서 진입
        Thread.sleep(2000);
        chrome.click(By.className("LinkButton_btn_confirm__3V6Um"));    //다음페이지
        chrome.click(By.className("PaymentMethod_radio_label__CZ6gh")); //신용카드 선택
        chrome.click(By.className("LinkButton_btn_confirm__3V6Um"));    //다음페이지
        chrome.click(By.className("LinkButton_btn_confirm__3V6Um"));    //주문확정
        chrome.click(By.linkText("支払い情報を入力する"));
        chrome.click(By.className("dummy-payment_btn_confirm__3ksg1"));    //결제하기 확인
        Thread.sleep(1000);
    }

    @Test(description = "쇼핑MY: 신규주문건 주문상세 내용이 [주문상태=주문접수완료] / [버튼=취소신청]으로 노출되는가")
    public void TC_03_MY_주문확인전_주문상세_버튼노출() throws Exception {
        chrome.get("https://mysmartstore.jp/my/order-info");
        chrome.click(By.className("OrderProductBundle_link_detail__1uVy6"));    //주문상세 진입
        chrome.waitForPageToLoaded();

        Verify.verifyTrue(chrome.isTextPresent("注文受付完了"));  //주문접수완료 텍스트 확인
        Verify.verifyTrue(chrome.isTextPresent("キャンセル申請")); //취소신청 버튼 텍스트 확인
    }

    @Test(description = "쇼핑MY: 신규주문건 [취소신청]클릭시 취소신청 페이지로 이동되는가")
    public void TC_04_MY_주문확인전_취소신청_버튼동작() throws Exception{
        chrome.click(By.xpath("//*[@id='__next']/div/div[1]/div[2]/div[2]/div/div[2]/div[1]/div/div/ul/li/div[2]/div/button[1]"));  // [취소신청]버튼 클릭
        Verify.verifyEquals(chrome.getText(By.className("claimHeader_title__3Ab4T")),"キャンセル申請");
        Verify.verifyEquals(chrome.getText(By.className("ClaimProduct_status__1truu")),"申請後すぐにキャンセルされます");
        List<WebElement> cancleReasonBtn = chrome.getElements(By.cssSelector(".claimReasonCategory_item__79sSS"));
        Verify.verifyTrue(cancleReasonBtn.size() == 5);  // 취소사유 버튼 5개
        Verify.verifyTrue(chrome.isElementPresent(By.className("claimReasonText_text_box__1Us84")));  // 상세사유 텍스트박스

        // Verify.verifyTrue(chrome.isElementPresent(By.className("claimReasonCategory_btn_category__27CBJ"));  // 취소사유 버튼
        // Verify.verifyTrue(chrome.isElementPresent(By.className("claimNotice_btn_submit__2mHee")));  //
    }

    @Test(description = "센터: 정상적으로 로그인 되는가")
    public void TC_05_센터_로그인_주문관리() throws Exception{
        chrome.openNewWindow("https://smartstorecenter.jp/");
        chrome.click(By.cssSelector(".btn-area .btn-login"));
        chrome.click(By.linkText("비즈니스 계정으로 로그인"));
        chrome.type(By.name("email"), SELLER_ID);
        chrome.type(By.name("password"), SELLER_PW);
        chrome.click(By.xpath("//button[@type='submit']"));
        chrome.click(By.linkText("허용"));
        chrome.waitForIsTextPresent("MySmartStore | SellerCenter");

        Verify.verifyEquals(chrome.getCurrentUrl(), "https://smartstorecenter.jp/#/home/dashboard");
    }

    @Test(description = "센터: 주문미확인 조회결과에 주문건이 노출되며 주문상세페이지에 진입되는가")
    public void TC_06_센터_주문미확인_주문상세진입() throws Exception {
        chrome.click(By.linkText("注文管理"));
        chrome.click(By.linkText("注文未確認"));
        Thread.sleep(1000);
        chrome.click(By.xpath("//button[@type='submit']"));
        Verify.verifyTrue(chrome.isElementPresent(By.partialLinkText(getSimpleDate())));

        chrome.get("https://smartstorecenter.jp/#/order/detail/" + chrome.findElement(By.partialLinkText(getSimpleDate())).getText());
        Verify.verifyEquals(chrome.getText(By.className("title")),"注文管理");
    }

    @Test(description = "센터: [주문확인]클릭시 주문확인팝업 호출 -> 주문확인처리되는가")
    public void TC_07_센터_주문확인() throws Exception {
        chrome.click(By.xpath("//*[@id='seller-content']/ui-view/div[2]/div/div/div[1]/ncp-order-claim-top-button-group/div/div[1]/div/div/button[1]"));  // 주문확인 버튼 클릭
        chrome.click(By.className("btn-primary")); // 알럿 확인 클릭

        Verify.verifyTrue(chrome.isTextPresent("購入者へのメッセージを入力する場合、注文確認完了のお知らせと一緒に配信されます。"));

        chrome.clear(By.name("message"));
        chrome.type(By.name("message"), getDate() + ": 자동화 스크립트 처리건"); // 비금칙어 입력
        chrome.waitForPageToLoaded();
        chrome.click(By.className("btn-primary"));
        chrome.waitForPageToLoaded();
        Verify.verifyTrue(chrome.isTextPresent("1件のうち、1件に対する注文確認が完了しました。"));  // '발송완료 후 환불처리'버튼 노출
        chrome.click(By.xpath("/html/body/div[1]/div/div/div[2]/div/span/button"));
        chrome.waitForPageToLoaded();

    }

    @Test(description = "센터: [발송처리]클릭시 발송처리팝업 호출 -> 주문확인처리되는가")
    public void TC_08_센터_발송처리() throws Exception {
        chrome.type(By.xpath("//input[@type='text']"), trackingNo); // 송장번호 입력
        chrome.click(By.xpath("//*[contains(text(), '出荷処理')]"));
        chrome.click(By.className("btn-primary"));

        Verify.verifyTrue(chrome.isTextPresent("購入者へのメッセージを入力する場合、出荷処理完了のお知らせと一緒に配信されます。"));

        chrome.type(By.name("message"), getDate() + ": 자동화 스크립트 처리건");
        chrome.waitForPageToLoaded();
        chrome.click(By.className("btn-primary")); // 알럿 확인 클릭
        chrome.waitForPageToLoaded();
        Verify.verifyTrue(chrome.isTextPresent("1件に対する出荷処理が完了しました。"));  // '발송완료 후 환불처리'버튼 노출
        chrome.click(By.xpath("/html/body/div[1]/div/div/div[2]/div/span/button"));  // 팝업종료
        chrome.waitForPageToLoaded();
    }

    @AfterClass
    public void quit() {
        chrome.quit();
    }
}