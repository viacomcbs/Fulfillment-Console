package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.diagnostic;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.loginUtil.DriverUtil;
import com.paramount.test.ff.common.loginUtil.Login;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.featureflag.FeatureFlagNetworkUtil;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPanelUtil;
import com.paramount.test.ff.uitests.helpers.orders.OrdersTabTestSetupHelper;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

/** Login-only setup for left-filter load diagnostics — no calendar or recovery steps. */
public abstract class LeftFilterLoadDiagnosticBaseTest extends BaseTest {

    protected Login login;
    protected LeftFilterPanelUtil leftFilterPanelUtil;
    protected FeatureFlagNetworkUtil networkUtil;

    private final OrdersTabTestSetupHelper ordersTabSetup = new OrdersTabTestSetupHelper();

    @BeforeClass(alwaysRun = true)
    @Override
    public void atStartSelenium() {
        super.atStartSelenium();
    }

    @BeforeMethod(alwaysRun = true)
    public void loginForLeftFilterDiagnostic() throws InterruptedException {
        login = new Login();
        leftFilterPanelUtil = new LeftFilterPanelUtil();
        networkUtil = new FeatureFlagNetworkUtil();
        softAssert = new SoftAssert("loginForLeftFilterDiagnostic", getClass().getSimpleName());

        Logger.logReportMessage("Launching Fulfillment Console — left-filter load diagnostic");
        DriverUtil.launchApplicationOnBrowser();
        login.loginToFF();
        ordersTabSetup.waitForLoginReadyForLeftFilters();

        // Hooks must be installed on Fulfillment Console SPA — not on Okta login page.
        networkUtil.installDiagnosticHooksOnCurrentPage();
        Logger.logReportMessage("Console/Network hooks installed on Fulfillment Console page");
    }
}
