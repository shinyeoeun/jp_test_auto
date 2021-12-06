package com.ntscorp.auto_client.data;

import java.awt.event.KeyEvent;

public enum ContextEvent {
	Back(KeyEvent.VK_B),
	Forward(KeyEvent.VK_B),
	Reload(KeyEvent.VK_R),
	Cast(KeyEvent.VK_C),
	Translate_to_based_language(KeyEvent.VK_T),
	Save_as(KeyEvent.VK_A),
	Print(KeyEvent.VK_P),
	View_page_source(KeyEvent.VK_V),
	Inspect(KeyEvent.VK_N),
	Open_link_in_new_tab(KeyEvent.VK_T),
	Open_link_in_new_window(KeyEvent.VK_W),
	Open_link_in_incoqnito_window(KeyEvent.VK_G),
	Save_link_as(KeyEvent.VK_K),
	Copy_link_address(KeyEvent.VK_E),
	Open_image_in_new_tab(KeyEvent.VK_I),
	Save_image_as(KeyEvent.VK_V),
	Copy_image(KeyEvent.VK_Y),
	Copy_image_adress(KeyEvent.VK_O),
	Search_Google_for_image(KeyEvent.VK_S);
	
	private int keyEvent;
	
	 ContextEvent(int keyEvent){
		this.keyEvent =keyEvent;
	}
	
	public int getKeyEvent() {
		return keyEvent;
	}
}
