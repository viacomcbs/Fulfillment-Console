package com.paramount.test.ff.uitests.tests;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.loginUtil.DriverUtil;
import com.paramount.test.ff.common.loginUtil.Login;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.common.util.props.IProps;
import com.paramount.test.ff.common.util.props.PropertyReader;
import com.paramount.test.ff.uitests.helpers.HomePage_util;
import com.paramount.test.ff.uitests.pageobjects.LoginPage;
import org.testng.annotations.BeforeTest;

public class FilterValidation extends BaseTest {
    LoginPage loginPage;
    Login login;
    HomePage_util homePage_util;
    String application   = IProps.ConfigProps.APPLICATION;

    @BeforeTest
    public void setup() {
        loginPage = new LoginPage();
        login = new Login();
        homePage_util = new HomePage_util();


    }


    public void validateFilterpanel() throws InterruptedException {
    application = PropertyReader.readFromProperties("app");
        softAssert = new SoftAssert(new Object() {
        }.getClass().getEnclosingMethod().getName(), this.getClass().getSimpleName());

        switch (application){
            case "mic":
                Logger.logReportMessage("Fulfillment Launching application");
                System.out.println("The application:" + application);
                ValidMICFilterPanel();
                break;
            case "QC Workbench":
                Logger.logReportMessage("Fulfillment Launching application");
                break;
            default:
                Logger.logReportMessage("Invalid Application Name");
                throw new IllegalArgumentException("Invalid Application Name: " + application);
        }
    }

    public void ValidMICFilterPanel() throws InterruptedException {
        Logger.logReportMessage("Fulfillment Launching application");
        DriverUtil.launchApplicationOnBrowser();

        login.loginToFF();
        Logger.log("Fulfillment Home page Validation");
        Logger.log("Validating Tutorial");
        homePage_util.validateHomePage(softAssert);
    }



}
