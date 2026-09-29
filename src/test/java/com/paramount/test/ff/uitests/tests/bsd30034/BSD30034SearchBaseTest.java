package com.paramount.test.ff.uitests.tests.bsd30034;

import com.paramount.test.ff.uitests.base.ColumnSearchBaseTest;
import com.paramount.test.ff.uitests.helpers.FulfillmentJsUtil;
import org.testng.annotations.BeforeClass;

public abstract class BSD30034SearchBaseTest extends ColumnSearchBaseTest {

    @BeforeClass(alwaysRun = true)
    public void bsd30034SearchSuiteLaunch() throws InterruptedException {
        prepareBsd30034SuiteOnce(BSD30034BaseTest.DELIVERY_DATE_NAME,
                BSD30034BaseTest.OFFSET_DELIVERY_DATE_NAME, BSD30034BaseTest.SECTION);
    }

    @Override
    protected void prepareSearchSuiteOnce() throws InterruptedException {
        ensureHelpersInitialized();
        if (isBsd30034DateColumnsEnabled()) {
            refreshBsd30034GridState();
            markSearchSuitePrepared();
            return;
        }
        super.prepareSearchSuiteOnce();
    }

    @Override
    protected boolean ensureColumnVisibleForSearch(String columnName, String section)
            throws InterruptedException {
        if (isBsd30034DateColumnsEnabled()) {
            filterPanelUtil.ensureOrdersGridHome();
            String columnId = gridSearchUtil.resolveColumnId(columnName, section);
            FulfillmentJsUtil.scrollColumnHeaderIntoView(columnName, columnId);
            if (FulfillmentJsUtil.isColumnVisibleInGrid(columnName, columnId)) {
                return true;
            }
        }
        return super.ensureColumnVisibleForSearch(columnName, section);
    }
}