package com.paramount.test.ff.common.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.qameta.allure.Step;
import org.apache.log4j.Level;
import org.testng.Reporter;
//import ru.yandex.qatools.allure.annotations.Step;

public abstract class Logger {

    @Step()
    public static void logMessage(String inMessage) {
        logConsoleMessage(inMessage);
        Reporter.log(inMessage);
    }
    
    //@Step("{0}")
    public static void logReportMessage(String message) {
    logConsoleMessage(message);
    Reporter.log(message);
    //AllureAttachment.attachScreenshot();
    //AllureAttachment.attachScreenRecording();
    }
    
    //@Step()
    public static void log(String message) {
    	logConsoleMessage(message);
        //Reporter.log(message);
        //AllureAttachment.attachScreenshot();
        //AllureAttachment.attachScreenRecording();
	}

    @Step
    public static void subStep(String message){
        log(message);
    }

    @Step
    public static void failedStep(String message){
        log(message);
        //AllureAttachment.attachFailureScreenshot();
    }

    public static void logMessage(Object inMessage) {
        logMessage(String.valueOf(inMessage));
    }

    public static void logConsoleMessage(String inMessage) {
        Long threadID = Thread.currentThread().getId();
        System.out.println(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS").format(new Date()) + "-thread-" + threadID
                + " " + inMessage);
    }

    public static void disableLog4JConsoleOutput() {
        Logger.logConsoleMessage("Disabling log4j console output.");
        org.apache.log4j.Logger.getLogger("org.BIU.utils.logging.ExperimentLogger").setLevel(Level.OFF);
        org.apache.log4j.Logger.getRootLogger().setLevel(Level.OFF);
    }

    public static void enableLog4JConsoleOutput() {
        Logger.logConsoleMessage("Enabling log4j console output.");
        org.apache.log4j.Logger.getLogger("org.BIU.utils.logging.ExperimentLogger").setLevel(Level.ALL);
        org.apache.log4j.Logger.getRootLogger().setLevel(Level.ALL);
    }
    //@Step("{0}")
    public static void logReportMessage1(String inMessage) {
        logConsoleMessage(inMessage);
        Reporter.log(inMessage);
    }
    
    }