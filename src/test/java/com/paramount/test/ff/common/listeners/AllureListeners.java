package com.paramount.test.ff.common.listeners;

import com.paramount.test.ff.common.driver.CapabilityFactoryO;
import com.paramount.test.ff.common.util.AllureAttachment;
import com.paramount.test.ff.common.util.Logger;
import com.synergy.core.driver.web.WebDriver;
import io.qameta.allure.Attachment;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.nio.file.Files;
import java.nio.file.Paths;

public class AllureListeners implements ITestListener {

    private static String getTestMethodName(ITestResult iTestResult) {
        return iTestResult.getMethod().getConstructorOrMethod().getName();
    }



    public static final ThreadLocal<Boolean> attachSuccess = new ThreadLocal<Boolean>() {
        @Override
        public Boolean initialValue() {
            return true;
        }
    };
    private static final String emptyString = "empty string";

    @Attachment
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

    @Attachment(value = "{0}", type = "text/plain")
    public static String saveTextLog(String message) {
        return message;
    }


    @Override
    public void onStart(ITestContext iTestContext) {
        System.out.println("I am in onStart method " + iTestContext.getName());
        System.out.println(" The driver:" + CapabilityFactoryO.getDriver());
        iTestContext.setAttribute("WebDriver", CapabilityFactoryO.getDriver());
    }

    @Override
    public void onFinish(ITestContext iTestContext) {
        Logger.logConsoleMessage("========TEST FINISHED========");
    }

    @Override
    public void onTestStart(ITestResult iTestResult) {
        System.out.println("I am in onTestStart method " + getTestMethodName(iTestResult) + " start");
    }

    @Override
    public void onTestSuccess(ITestResult iTestResult) {
        System.out.println("I am in onTestSuccess method " + getTestMethodName(iTestResult) + " succeed");
    }

    @Override
    public void onTestFailure(ITestResult iTestResult) {
        System.out.println("I am in onTestFailure method " + getTestMethodName(iTestResult) + " failed");
        Object testClass = iTestResult.getInstance();
        WebDriver driver = CapabilityFactoryO.getDriver();
        // Allure ScreenShot and SaveTestLog
        if (driver instanceof WebDriver) {
            System.out.println("Screenshot captured for test case:" + getTestMethodName(iTestResult));
            attachFailureScreenshot();
        }
        AllureAttachment.attachScreenRecordingLink();
        saveTextLog(getTestMethodName(iTestResult) + " failed and screenshot taken!");
    }


    @Override
    public void onTestSkipped(ITestResult iTestResult) {
        System.out.println("I am in onTestSkipped method " + getTestMethodName(iTestResult) + " skipped");
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult iTestResult) {
        System.out.println("Test failed but it is in defined success ratio " + getTestMethodName(iTestResult));
    }

}
