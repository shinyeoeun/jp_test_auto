package com.ntscorp.auto_client;

import com.ntscorp.auto_client.selenium.ChromeUtil;
import com.ntscorp.auto_client.verity.Verify;
import org.openqa.selenium.By;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


public class jpShoppingCreditAutomation_Chrome extends Formatter{

    ChromeUtil chrome;

    private final String SELLER_ID = "nvqa_gncp_65@naver.com";  // 센터 ID
    private final String SELLER_PW = "gncp65";  // 센터 PW
    private final String CUSTOMER_ID = "nvqa_nshop69@naver.com";  // 구매자 ID
    private final String CUSTOMER_PW = "qatest123";  // 센터 PW

    @BeforeClass
    public void setup() {
        chrome = (ChromeUtil) new Automation()
                .pc()
                .chrome()
                .mainUrl("https://mysmartstore.jp/my/order-info")
                .start();
        this.login();
    }
    public void login() {
        chrome.type(By.name("tid"), CUSTOMER_ID);
        chrome.type(By.name("tpasswd"), CUSTOMER_PW);
        chrome.click(By.className("MdBtn01"));
    }

    @Test(description = "신용카드 결제 후 주문완료가 되는지")
    public void TC_01() throws InterruptedException {
        chrome.click(By.className("N=com.pfavstore"));  //즐겨찾는 상점 이동
        chrome.click(By.className("KeepStore_link_product__DvAWh"));    //즐겨찾는 상점에서 첫번째 상점 이동
        chrome.click(By.className("dz"));   //상품상세 진입
        chrome.click(By.className("mh"));   //장바구니 담기
        Thread.sleep(2000);
        chrome.click(By.className("vF"));   //장바구니 담기 확인
        Thread.sleep(2000);
        chrome.click(By.className("nd"));   //장바구니로 이동
        Thread.sleep(2000);
        chrome.click(By.className("Cart_txt__mDwoT"));  //주문서 진입
        Thread.sleep(2000);
        chrome.click(By.className("LinkButton_btn_confirm__3V6Um"));    //다음페이지
        chrome.click(By.className("PaymentMethod_radio_label__CZ6gh")); //신용카드 선택
        chrome.click(By.className("LinkButton_btn_confirm__3V6Um"));    //다음페이지
        chrome.click(By.className("LinkButton_btn_confirm__3V6Um"));    //주문확정
        chrome.click(By.id("btnPayment"));  //결제하기
        chrome.click(By.className("highlight"));    //결제하기 확인
        chrome.click(By.className("Success_link_item__3EG2u")); //주문내역 이동
        chrome.click(By.className("OrderProductBundle_link_detail__1uVy6"));    //주문상세 진입
        Verify.verifyTrue(chrome.isTextPresent("注文受付完了"));  //주문접수완료 텍스트 확인
        Verify.verifyTrue(chrome.isTextPresent("キャンセル申請")); //취소신청 버튼 텍스트 확인
    }

    //    @Test(description = "주문확인 전 취소신청")
//    public void TC_01() throws InterruptedException {
//        chrome.click(By.className("OrderProduct_btn_item__25CHI"));
//        chrome.click(By.className("claimReasonCategory_btn_category__27CBJ"));
//        chrome.click(By.className("claimNotice_btn_submit__2mHee"));
//
//        Verify.verifyTrue(chrome.isTextPresent("キャンセル完了"));
//        Thread.sleep(3000);
//
//        chrome.click(By.className("listButton_btn_save__3o--M"));
//        Thread.sleep(3000);
//    }
    @AfterClass
    public void quit() {
        chrome.quit();
    }
}