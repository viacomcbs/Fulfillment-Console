package com.paramount.test.ff.uitests.tests;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.loginUtil.DriverUtil;
import com.paramount.test.ff.common.loginUtil.Login;
import com.paramount.test.ff.common.loginUtil.Verify;
import com.paramount.test.ff.common.loginUtil.WaitUtil;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.pageobjects.TableView;
import com.paramount.test.ff.uitests.helpers.HomePage_util;
import com.paramount.test.ff.uitests.helpers.TableView_utill;
import com.paramount.test.ff.uitests.pageobjects.LoginPage;
import io.qameta.allure.Description;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class TC_003_ValidateStandardViewModeManageColumn_OrderTab extends BaseTest {

    LoginPage loginPage;
    Login login;
    HomePage_util homePage_util;
    TableView tableView;
    TableView_utill tableViewUtill;

    @BeforeTest
    public void setup() throws InterruptedException {
        loginPage = new LoginPage();
        login = new Login();
        homePage_util = new HomePage_util();
        tableView = new TableView();
        tableViewUtill= new TableView_utill();

    }
    @Test(priority = 0)
    @Description("TC_003_ValidateStandardViewModeManageColumn_OrderTab")
    public void validateStandardViewManageColumnIcon() throws InterruptedException {

        softAssert = new SoftAssert(new Object() {
        }.getClass().getEnclosingMethod().getName(), this.getClass().getSimpleName());

        Logger.logReportMessage("Fulfillment Launching application");
        DriverUtil.launchApplicationOnBrowser();

        login.loginToFF();
        Logger.log("Fulfillment Home page");
        homePage_util.validateHomePage(softAssert);
        Logger.log("click on manage column table view icon");
        driver.get().finder().findElement(tableView.getTableViewButton()).click();
        Thread.sleep(2000);
        Logger.log("click on Standard View Mode..");
        driver.get().finder().findElement(tableView.standardViewdropdownButton()).click();
        Thread.sleep(2000);
        Logger.log("Select Standard View Mode..");
        driver.get().finder().findElement(tableView.selectStandardViewMode()).click();
        Thread.sleep(2000);
        Verify.softAssert(WaitUtil.isDisplay(tableView.StandardViewMode(), 5), "Standard View Mode");
        Logger.log("manage column table view selected standard vier mode on ordertab validation done.. ");
        softAssert.assertAll();

    }
}
