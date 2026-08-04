package com.paramount.test.ff.common.util;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.driver.CapabilityFactoryO;

import io.qameta.allure.Allure;
import io.qameta.allure.Attachment;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.StepResult;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

public class AllureAttachment {
	public static final ThreadLocal<Boolean> attachSuccess = new ThreadLocal<Boolean>() {
		@Override
		public Boolean initialValue() {
			return true;
		}
	};
	private static final String emptyString = "empty string";

	//@Attachment(value = "Screenshot", type = "image/png")
	public static byte[] attachScreenshot() {
		attachSuccess.set(true);
		byte[] imageContent = emptyString.getBytes();
		try {
			imageContent = Files.readAllBytes(Paths.get(CapabilityFactoryO.getDriver().screen().getImage().getAbsolutePath()));
		} catch (Exception e) {
			Logger.logConsoleMessage("Failed to capture screenshot. See server logs for details.");
			attachSuccess.set(false);
		}
		return imageContent;
	}

	@Attachment(value = "Screen Recording Link", type = "text/uri-list")
	public static byte[] attachScreenRecordingLink(){
		attachSuccess.set(true);
		String link="https://www.synergyplatform.tech/tests?sessionID={Session_ID}&open=true";

		try {
			link = "https://www.synergyplatform.tech/tests?sessionID=" + BaseTest.driver.get().getSessionID() + "&open=true";
			//Logger.logMessage("Screen Recording :: Navigate to below link: " + link);
		}catch (Exception e){
			Logger.logMessage("WebDriver is null, hence Session id is not created");
		}
		return link.getBytes();
	}

	//@Attachment(value = "Failure Screenshot", type = "image/png")
	public static byte[] attachFailureScreenshot() {
		attachSuccess.set(true);
		byte[] imageContent = emptyString.getBytes();
		try {
			imageContent = Files
					.readAllBytes(Paths.get(CapabilityFactoryO.getDriver().screen().getImage().getAbsolutePath()));
		} catch (Exception e) {
			Logger.logConsoleMessage("Failed to capture screenshot. See server logs for details.");
			attachSuccess.set(false);
		}
		return imageContent;
	}

	//@Attachment(value = "Assert Failure Screenshot", type = "image/png")
	public static byte[] attachAssertScreenshot() {
		attachSuccess.set(true);
		byte[] imageContent = emptyString.getBytes();
		try {
			imageContent = Files
					.readAllBytes(Paths.get(CapabilityFactoryO.getDriver().screen().getImage().getAbsolutePath()));
		} catch (Exception e) {
			Logger.logConsoleMessage("Failed to capture screenshot. See server logs for details.");
			attachSuccess.set(false);
		}
		return imageContent;
	}


	//@Attachment(value = "Screen Recording", type = "video/mp4")
	public static byte[] attachScreenRecording() {
		attachSuccess.set(true);
		byte[] videoContent = emptyString.getBytes();
		try {
			videoContent = Files
					.readAllBytes(Paths.get(CapabilityFactoryO.getDriver().screen().getRecording().getAbsolutePath()));
		} catch (Exception e) {
			Logger.logConsoleMessage("Failed to capture screen recording. See server logs for details.");
			attachSuccess.set(false);
		}
		return videoContent;
	}
	
	public static void log(String message, Status status) {
	    String uuid = UUID.randomUUID().toString();
	    StepResult result = new StepResult().setName(message).setStatus(status);
	    Allure.getLifecycle().startStep(uuid, result);
	    Allure.getLifecycle().stopStep(uuid);
	}
	
	public static void writeStepToFailed(String message) {
	    String uuid = UUID.randomUUID().toString();
	    StepResult result = new StepResult().setName(message).setStatus(Status.FAILED);
	    Allure.getLifecycle().startStep(uuid, result);
	    Allure.getLifecycle().stopStep(uuid);
	}
	
	public static void writeStepToPass(String message) {
	    String uuid = UUID.randomUUID().toString();
	    StepResult result = new StepResult().setName(message).setStatus(Status.PASSED);
	    Allure.getLifecycle().startStep(uuid, result);
	    Allure.getLifecycle().stopStep(uuid);
	}

	//private static SoftAssert softAssertion;
	public static void markStepAsFailed(String errorMessage) {
		//BaseTest.softAssert = new SoftAssert();
		String uuid = UUID.randomUUID().toString();
		Logger.log(errorMessage);
		//StepResult result= new StepResult().withName(errorMessage).withStatus(Status.FAILED);
		StepResult result = new StepResult().setName(errorMessage).setStatus(Status.FAILED);
		Allure.getLifecycle().startStep(uuid, result);
		Allure.getLifecycle().stopStep(uuid);
		BaseTest.softAssert.fail(errorMessage);
	}

}
