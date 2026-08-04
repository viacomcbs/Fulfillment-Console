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
import com.synergy.core.driver.elements.DesktopBrowserElement;
import io.qameta.allure.Description;
import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class TC_002_ValidateOpenManageColumn_Ordertab extends BaseTest {

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
    @Description("TC_002_ValidateOpenManageColumn_Ordertab")
    public void validateOpenManageColumnIcon() throws InterruptedException {

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
        Verify.softAssert(WaitUtil.isDisplay(tableView.manageColumnOpenOrderTab(), 5), "Table View Manage column open");
        Logger.log("manage column table view Open on ordertab validation done.. ");
        softAssert.assertAll();

    }
}
