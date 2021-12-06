package com.ntscorp.auto_client.appium;

import java.util.List;

import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

public class By extends org.openqa.selenium.By {	
	public static org.openqa.selenium.By text (String target) {
		org.openqa.selenium.By locator = null;
		locator = xpath("//*[@text='"+target+"']");
		
		return locator;
	}
	
	public static org.openqa.selenium.By desc (String target) {
		org.openqa.selenium.By locator = null;
		locator = xpath("//*[contains(@content-desc,'"+target+"')]");
		 
		return locator;
	}
	
	public static int[] bounds (String target) {
		StringBuffer sb = new StringBuffer();
		sb.append(target);
		sb.deleteCharAt(0).deleteCharAt(sb.length()-1);
		target = sb.toString();
		target = target.replaceAll("\\]\\[", ",");
		String[] loc = target.split(",");
		int[] location = new int[4];
		
		for(int i = 0; i < loc.length; i++){
			location[i] = Integer.parseInt(loc[i]);
		}
		
		int[] point = new int[2];
		
		point[0] = (location[0] + location[2]) / 2;
		point[1] = (location[1] + location[3]) / 2;
				
		return point;
	}
	
	@Override
	public List<WebElement> findElements(SearchContext arg0) {
		return null;
	}
}

 