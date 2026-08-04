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

public class TC_004_ValidateCreateNewTableView_Ordertab extends BaseTest {

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
    @Description("TC_004_ValidateCreateNewTableView_Ordertab")
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
        Logger.log("Crick on save new view button for create new table view");
        driver.get().finder().findElement(tableView.saveNewButtonTableView()).click();
        Thread.sleep(2000);
        Logger.log("Entering new table view name");
        String tableViewName = "AutomationTableView";
        driver.get().finder().findElement(tableView.inputTableViewName()).click();
        Thread.sleep(2000);
        driver.get().finder().findElement(tableView.inputTableViewName()).sendKeys(tableViewName);
        Thread.sleep(2000);
        Logger.log("Click on create save button on table view popup");
        driver.get().finder().findElement(tableView.saveButtonOnTableViewPopup()).click();
        Thread.sleep(5000);
        Verify.softAssert(DriverUtil.waitForElementVisibleExpicit(tableView.SaveConfirmationMessage(),60),"Table view Ceated");
        Logger.log("Table view created on ordertab validation done.. ");
        softAssert.assertAll();

    }
}
