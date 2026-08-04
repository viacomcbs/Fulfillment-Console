package com.paramount.test.ff.uitests.tests;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.loginUtil.DriverUtil;
import com.paramount.test.ff.common.loginUtil.Login;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.pageobjects.TableView;
import com.paramount.test.ff.uitests.helpers.HomePage_util;
import com.paramount.test.ff.uitests.helpers.TableView_utill;
import com.paramount.test.ff.uitests.pageobjects.LoginPage;
import io.qameta.allure.Description;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class ValidateTableViewColumns extends BaseTest {

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
    @Description("Validate Table view columns")
    public void validateHomePage() throws InterruptedException {

        softAssert = new SoftAssert(new Object() {
        }.getClass().getEnclosingMethod().getName(), this.getClass().getSimpleName());

        Logger.logReportMessage("Fulfillment Launching application");
        DriverUtil.launchApplicationOnBrowser();

        login.loginToFF();
        Logger.log("Fulfillment Home page");
        homePage_util.validateHomePage(softAssert);
        Logger.log("click on table view button");
        driver.get().finder().findElement(tableView.getTableViewButton()).click();
        Thread.sleep(2000);
        Logger.log("click on Standard View Mode..");
        driver.get().finder().findElement(tableView.standardViewdropdownButton()).click();
        Thread.sleep(2000);
        Logger.log("Select Standard View Mode..");
        driver.get().finder().findElement(tableView.selectStandardViewMode()).click();
        Thread.sleep(2000);
        //Logger.log("Validating Table View Columns List");
        // ---start validate column names on table view----
       /* for(int i=0; i<tableViewUtill.tableviewOrderColumnList.length; i++)
        {
            String columnName = tableViewUtill.tableviewOrderColumnList[i];
            Logger.log("Validating Column Name: " + columnName);
            tableViewUtill.isColumnNameVisible(softAssert,columnName);
            Thread.sleep(500);
            softAssert.assertAll();
            //softAssert.assertTrue(isColumnVisible, "Column '" + columnName + "' is not visible in the table view.");
        }*/
        // ---end validate column names on table

        //---start validate if column names are selected on table view its should show on UI----
       /* Logger.log("Validating Table View Column Selection and its visibility on UI");
        for(int j=0; j<tableViewUtill.tableviewOrderColumnList.length; j++)
        {
            String s = tableViewUtill.tableviewOrderColumnList[j];
            Logger.log("Validating if Column Name Selected: " + s);
            tableViewUtill.ifColumnNameSelected(s);
            Thread.sleep(500);
            softAssert.assertAll();
            Logger.log("Validating done for selected column on UI");
            //softAssert.assertTrue(isColumnVisible, "Column '" + columnName + "' is not visible in the table view.");
        }*/
        //---end validate if column names are selected on table view its should show on UI----
       // Start validate create new table view & selected default view
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
       /*Logger.log("new table view confirmation popup..");
        softAssert.assertTrue(DriverUtil.isDisplayed(tableView.confirmtiontableviewPopup(),10), "New table view created successfully: " + tableViewName);
        //softAssert.assertAll();*/
        Logger.log("validating new created table view selected by default");
        Logger.log("click on table view button");
        driver.get().finder().findElement(tableView.getTableViewButton()).click();
        Thread.sleep(2000);
        Logger.log("Table view name:-"+DriverUtil.getElement(tableView.tableViewDefaultSelected(tableViewName)).getText());
        softAssert.assertTrue(DriverUtil.getElement(tableView.tableViewDefaultSelected(tableViewName)).getText().equals(tableViewName), "New created table view is selected by default: " + tableViewName);
        softAssert.assertAll();
        // end validate create new table view & selected default view

    }
}
