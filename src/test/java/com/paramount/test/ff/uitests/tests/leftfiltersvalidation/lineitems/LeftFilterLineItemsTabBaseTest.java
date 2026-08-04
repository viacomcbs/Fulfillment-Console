package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.lineitems;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.loginUtil.DriverUtil;
import com.paramount.test.ff.common.loginUtil.Login;
import com.paramount.test.ff.common.loginUtil.WaitUtil;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.lineitems.LineItemsTabTestSetupHelper;
import com.paramount.test.ff.uitests.helpers.leftfilters.CalendarSetupUtil;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPanelUtil;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterSessionHelper;
import com.paramount.test.ff.uitests.helpers.leftfilters.LineItemsLeftFilter;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import static com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterConstants.FILTER_EXPAND_WAIT_MS;

/**
 * Base setup for Line items tab left-filter validation tests (one @Test per subclass).
 * Uses a shared browser session when run via {@code LeftFilterValidation_DevServerSuite.xml}.
 */
public abstract class LeftFilterLineItemsTabBaseTest extends BaseTest {

    protected Login login;
    protected LeftFilterPanelUtil leftFilterPanelUtil;
    private final LineItemsTabTestSetupHelper lineItemsTabSetup = new LineItemsTabTestSetupHelper();

    protected static final String DEFAULT_FILTER = LineItemsLeftFilter.LINE_ITEM_STATUS.getDisplayName();
    protected static final String LARGE_OPTIONS_FILTER = LineItemsLeftFilter.PARTNER.getDisplayName();
    protected static final String DEFAULT_FILTER_OPTION = "Delivery Complete";
    protected static final String DEFAULT_SEARCH_TEXT = "delivery";

    public LeftFilterLineItemsTabBaseTest() {
        keepDriverAliveAfterTestMethod = true;
    }

    @BeforeClass(alwaysRun = true)
    @Override
    public void atStartSelenium() {
        try {
            if (driver.get() != null) {
                Logger.logReportMessage("Reusing WebDriver for Line items left-filter tests");
                return;
            }
        } catch (Exception ignored) {
            // no driver yet
        }
        super.atStartSelenium();
    }

    @BeforeMethod(alwaysRun = true)
    public void lineItemsLeftFilterSetup() throws InterruptedException {
        login = new Login();
        leftFilterPanelUtil = new LeftFilterPanelUtil();

        softAssert = new SoftAssert("lineItemsLeftFilterSetup", getClass().getSimpleName());

        if (!LeftFilterSessionHelper.isLoggedIn()) {
            Logger.logReportMessage("Launching Fulfillment Console for Line items left-filter tests");
            DriverUtil.launchApplicationOnBrowser();
            login.loginToFF();
            LeftFilterSessionHelper.markLoggedIn();
            setCalendarToYesterdayOnce();
        } else {
            Logger.logReportMessage("Refreshing Fulfillment Console for next Line items left-filter test");
            driver.get().browser().refresh();
            WaitUtil.waitForJSToLoad(45);
            Thread.sleep(FILTER_EXPAND_WAIT_MS);
        }

        lineItemsTabSetup.prepareLineItemsTab(softAssert);
    }

    private void setCalendarToYesterdayOnce() throws InterruptedException {
        if (!LeftFilterSessionHelper.isCalendarSetToYesterday()) {
            new CalendarSetupUtil().setDateRangeToYesterday(softAssert);
            LeftFilterSessionHelper.markCalendarSetToYesterday();
        }
    }
}
