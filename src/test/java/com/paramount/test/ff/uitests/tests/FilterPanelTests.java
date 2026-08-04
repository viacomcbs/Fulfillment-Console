package com.paramount.test.ff.uitests.tests;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.loginUtil.DriverUtil;
import com.paramount.test.ff.common.loginUtil.Login;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.pageobjects.FilterPanel;
import com.paramount.test.ff.uitests.helpers.FilterPanel_Util;
import com.paramount.test.ff.uitests.helpers.Filtercategories;
import io.qameta.allure.Description;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class FilterPanelTests extends BaseTest {
    FilterPanel filterpanel;
    Login login;
    FilterPanel_Util filterPanel_util;

    @BeforeTest
    public void setup() throws InterruptedException {
        filterpanel = new FilterPanel();
        login = new Login();
         filterPanel_util = new FilterPanel_Util();

    }

    @Test(priority = 0)
    @Description("Validate Home Page")
    public void validateHomePage() throws InterruptedException {

        softAssert = new SoftAssert(new Object() {
        }.getClass().getEnclosingMethod().getName(), this.getClass().getSimpleName());

        Logger.logReportMessage("Fulfillment Launching application");
        DriverUtil.launchApplicationOnBrowser();

        login.loginToFF();
        Logger.log("Fulfillment Home page Validation");
        Logger.log("Validating Tutorial");
        filterPanel_util.validateTitleCategory(Filtercategories.Title.toString());

    }
}
