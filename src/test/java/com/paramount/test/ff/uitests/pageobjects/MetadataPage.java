package com.paramount.test.ff.uitests.pageobjects;

import com.paramount.test.ff.util.constants.Constants;
import com.synergy.core.driver.By;

public class MetadataPage {
	
	public By getProviderArrow() {
		return By.XPath("//i[@class='bi bi-chevron-down']");
	}
	public By getSelectProvider(){
		return By.XPath("//label[contains(text(),'MIPortal-TestVideo')]");
	}
	
	public By getSelectProvider(String providerName){
		return By.XPath("//label[contains(text(),'"+providerName+"')]");
	}
	
	public By getClickOnOrder() {
		return By.XPath("(//div[@class='order-container'])[1]");
	}
	public By getClickOnOrderByIndex(String index) {
		return By.XPath("(//div[@class='order-container'])["+index+"]");
	}
	
	public By getSelectedProvider() {
		return By.XPath("//div[@class='provider-name' and contains(text(),'MIPortal-TestVideo')]");
	}
	
	public By getSelectedProvider(String providerName) {
		return By.XPath("//div[@class='provider-name' and contains(text(),'"+providerName+"')]");
	}
	
	public By getClickOnIngestButton() {
		if(Constants.version.equals("mip1")) {
		    return By.XPath("(//span[contains(text(),'Ingest New Media')])[1]");
		} else if(Constants.version.equals("mfu")) {
			return By.XPath("(//span[contains(text(),'Ingest new media')])[1]");
		}else {
			System.err.println("Version of application is not mentioned on Config.properties file!");
		}
		return By.XPath("(//span[contains(text(),'Ingest')])[1]");
		
	}
	
	public By getSelectMediaType_dropdown() {
		return By.XPath("//button[contains(@title,'Select')]");
	}
	
	//Subtitle/Caption, Audio,Video Master, Locked File Proxy, Shooting Script,	Music Cue Sheet,Production Script,	Localization Report, Localized Script, Segment Rundown
	public By selectMediaType_dropdownValues(String mediaType) {
		return By.XPath("//div[@title='"+mediaType+"']");
	}
	
	public By getMetadataFieldPage() {
		return By.XPath("//div[contains(@class,'metadata-select-container-wrapper')]");
	}
	
	//Frame Rate
	public By getMetadataDropdown(String dropdownName) {
		return By.XPath("//div[text()='"+dropdownName+"']/parent::div//button[@title='Select']");
	}
	
	//Frame Rate
	public By getMetadataValues(String dropdownName) {
		return By.XPath("//div[text()='"+dropdownName+"']/parent::div//div[@class='option-container']");
	}
	
	public By getMetadataValue(String dropdownName, String value) {
		return By.XPath("//div[text()='"+dropdownName+"']/parent::div//div[@class='option-container' and @title='"+value+"']");
	}
	
	public By getActualMetadataByTitle(String dropdownName) {
		return By.XPath("//div[text()='"+dropdownName+"']/parent::div//div[@class='option-container' and @title]");
	}
	
	//soundfield, audio-type-col, language-col
	public By getAudioMetadata(String dropdownName) {
		return By.XPath("//div[@class='"+dropdownName+"']//button[@title='Select']");
	}
	
	public By getAudioMetadataValue(String metadataValue) {
		return By.XPath("//div[@class='option-container' and @title='"+metadataValue+"']");
	}
	
	public By getAudioMetadataValue_byTitle(String dropdown) {
		return By.XPath("//div[@class='"+dropdown+"']//div[@class='dropdown-menu dropdown-options-list show']//div[@class='option-container' and @title]");
	}
	
	public By getAudioChannelNumberField() {
		return By.XPath("//input[@id='audioChannel']");
	}

	public By getSubmitButton_AudioChannelNumber() {
		return By.XPath("//button[contains(text(),'Submit')]");
	}
}
