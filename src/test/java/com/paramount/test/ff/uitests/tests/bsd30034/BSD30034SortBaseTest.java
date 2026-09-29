package com.paramount.test.ff.uitests.tests.bsd30034;

import com.paramount.test.ff.uitests.base.SortBaseTest;
import com.paramount.test.ff.uitests.helpers.FulfillmentJsUtil;
import org.testng.annotations.BeforeClass;

public abstract class BSD30034SortBaseTest extends SortBaseTest {

    @BeforeClass(alwaysRun = true)
    public void bsd30034SortSuiteLaunch() throws InterruptedException {
        prepareBsd30034SuiteOnce(BSD30034BaseTest.DELIVERY_DATE_NAME,
                BSD30034BaseTest.OFFSET_DELIVERY_DATE_NAME, BSD30034BaseTest.SECTION);
    }

    @Override
    protected void prepareSortSuiteOnce() throws InterruptedException {
        ensureHelpersInitialized();
        if (isBsd30034DateColumnsEnabled()) {
            refreshBsd30034GridState();
            markSortSuitePrepared();
            return;
        }
        super.prepareSortSuiteOnce();
    }

    @Override
    protected boolean ensureColumnVisibleForSort(String columnName, String section) throws InterruptedException {
        if (isBsd30034DateColumnsEnabled()) {
            filterPanelUtil.ensureOrdersGridHome();
            String columnId = gridSortUtil.resolveColumnId(columnName, section);
            FulfillmentJsUtil.scrollColumnHeaderIntoView(columnName, columnId);
            if (FulfillmentJsUtil.isColumnVisibleInGrid(columnName, columnId)) {
                return true;
            }
        }
        return super.ensureColumnVisibleForSort(columnName, section);
    }
}