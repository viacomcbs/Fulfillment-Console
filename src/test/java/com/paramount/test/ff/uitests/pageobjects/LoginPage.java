package com.paramount.test.ff.uitests.pageobjects;

import com.paramount.test.ff.common.util.Config;
import com.synergy.core.driver.By;

public class LoginPage {
	
	public By getUserNameField() {
		return By.XPath("//input[@autocomplete = 'username']");
	}
	
	public By getPasswordField() {
		if (Config.isLocalExecution()) {
			return By.XPath("//input[@type = 'password']");
		}
		return By.XPath("//input[@name='credentials.passcode' and @type = 'password']");
	}
	
	public By getSignInButton() {
		return By.XPath("//input[@value='Verify']");
	}
	
    public By getNotification() {
        return By.XPath("//a[@aria-label='Select to get a push notification to the Okta Verify app.']");
    }

    public By getOktaVerifyFactorOptions() {
        return By.XPath("//a[contains(@aria-label,'Okta Verify')]"
                + " | //button[contains(.,'Okta Verify')]"
                + " | //input[@value='Send push' or @value='Send Push']"
                + " | //div[@data-se='okta_verify']//a"
                + " | //span[contains(.,'Okta Verify')]/ancestor::a");
    }

    public By getOktaPushSentIndicator() {
        return By.XPath("//*[contains(.,'Push notification sent') or contains(.,'push notification sent')"
                + " or contains(.,'waiting for your response') or contains(.,'Waiting for you to verify')]");
    }

    public By getOktaNumberChallenge() {
        return By.XPath("//*[contains(@class,'number-challenge')]//*[contains(@class,'number')]"
                + " | //span[contains(@class,'number') and string-length(normalize-space()) <= 2]"
                + " | //h1[contains(@class,'number')]");
    }
	
	public By getHomePage() {
		return By.XPath("//span[text()='MEDIA INGEST CONSOLE']");
	}
	
	public By getNextButton() {
		return By.XPath("//input[@value='Next']");
	}
	
	
	
	
	

}
