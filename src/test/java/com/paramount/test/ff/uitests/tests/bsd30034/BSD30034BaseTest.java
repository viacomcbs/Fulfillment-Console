package com.paramount.test.ff.uitests.tests.bsd30034;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import com.paramount.test.ff.uitests.helpers.FilterPanel_Util;
import com.paramount.test.ff.uitests.helpers.FulfillmentJsUtil;
import io.qameta.allure.Link;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

@Link(name = "BSD-30034", url = "https://paramount.atlassian.net/browse/BSD-30034")
public abstract class BSD30034BaseTest extends ManageColumnsBaseTest {

    protected static final String DELIVERY_DATE_ID = "deliveryDate";
    protected static final String DELIVERY_DATE_NAME = "Delivery date";
    protected static final String OFFSET_DELIVERY_DATE_ID = "deliveryOffset";
    protected static final String OFFSET_DELIVERY_DATE_NAME = "Offset Delivery date";
    protected static final String LEGACY_DELIVERY_OFFSET_NAME = "Delivery Offset";
    protected static final String SECTION = "order";

    protected FilterPanel_Util filterPanelUtil;

    @BeforeClass(alwaysRun = true)
    public void bsd30034SuiteLaunch() throws InterruptedException {
        prepareBsd30034SuiteOnce(DELIVERY_DATE_NAME, OFFSET_DELIVERY_DATE_NAME, SECTION);
    }

    @BeforeMethod(alwaysRun = true)
    public void initBsd30034Helpers() throws InterruptedException {
        FulfillmentJsUtil.useFastElementTimeout();
        ensureHelpersInitialized();
        if (filterPanelUtil == null) {
            filterPanelUtil = new FilterPanel_Util();
        }
    }

    protected void openManageColumnsOnce() throws InterruptedException {
        launchFulfillmentConsole();
        filterPanelUtil.ensureOrdersGridHome();
        if (tableViewUtill.isManageColumnsPanelOpen() && tableViewUtill.isStandardViewSelected()) {
            Logger.logReportMessage("Manage Columns already open with Standard View - reusing panel");
            return;
        }
        tableViewUtill.openManageColumns();
        tableViewUtill.selectStandardView();
    }

    protected boolean isColumnListedInManageColumns(String columnName) throws InterruptedException {
        tableViewUtill.scrollToColumn(columnName, SECTION);
        return tableViewUtill.isColumnListed(columnName, SECTION);
    }
}
