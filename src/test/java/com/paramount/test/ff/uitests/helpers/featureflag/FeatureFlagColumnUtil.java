package com.paramount.test.ff.uitests.helpers.featureflag;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.loginUtil.WaitUtil;
import com.paramount.test.ff.common.loginUtil.Verify;
import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.pageobjects.FeatureFlagPage;
import com.paramount.test.ff.uitests.helpers.managecolumns.ManageColumnsUtil;
import com.synergy.core.driver.By;

import static com.paramount.test.ff.common.base.BaseTest.driver;
import static com.paramount.test.ff.uitests.helpers.managecolumns.ManageColumnsUtil.toSection;

/** Manage-columns and grid column helpers for BSD-28459 regression. */
public class FeatureFlagColumnUtil extends BaseTest {

    public enum ColumnSection {
        ORDER, LINE_ITEM
    }

    private static final int GRID_WAIT_S = 15;

    private final FeatureFlagPage page = new FeatureFlagPage();
    private final ManageColumnsUtil manageColumns = new ManageColumnsUtil();

    public void openManageColumnsPanel(SoftAssert softAssert) throws InterruptedException {
        manageColumns.openPanel(softAssert);
    }

    public void validateColumnListed(ColumnSection section, String columnLabel, SoftAssert softAssert) {
        manageColumns.validateColumnListed(toSection(section), columnLabel, softAssert);
    }

    public void enableColumnIfNeeded(ColumnSection section, String columnLabel, SoftAssert softAssert)
            throws InterruptedException {
        manageColumns.enableColumnIfNeeded(toSection(section), columnLabel, softAssert);
    }

    public void saveChangesIfPresent(SoftAssert softAssert) throws InterruptedException {
        manageColumns.saveChangesIfPresent(softAssert);
    }

    public void closeManageColumnsPanel(SoftAssert softAssert) throws InterruptedException {
        manageColumns.closePanel(softAssert);
    }

    public void validateColumnHeaderInGrid(String columnLabel, SoftAssert softAssert) throws InterruptedException {
        WaitUtil.waitForJSToLoad(GRID_WAIT_S);
        Thread.sleep(1000);
        By header = page.gridColumnHeader(columnLabel);
        if (!WaitUtil.isDisplayFast(header, 5)) {
            scrollGridHorizontally();
        }
        Verify.softAssert1(WaitUtil.isDisplay(header, GRID_WAIT_S),
                columnLabel + " column header visible in grid", softAssert);
    }

    public int countVisibleGridRows() {
        try {
            Object result = driver.get().browser().executeScript(
                    "var rows = document.querySelectorAll('#orderTable tbody tr, tbody tr.row, tbody tr');"
                            + "return rows ? rows.length : 0;");
            if (result instanceof Number) {
                return ((Number) result).intValue();
            }
            return WaitUtil.isDisplayFast(page.gridDataRows(), 2) ? 1 : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private void scrollGridHorizontally() {
        try {
            driver.get().browser().executeScript(
                    "var el = document.querySelector('.custom-table-wrapper')"
                            + " || document.querySelector('.table-container')"
                            + " || document.querySelector('#orderTable');"
                            + "if (el) el.scrollLeft = el.scrollWidth;");
        } catch (Exception ignored) {
            // best effort
        }
    }
}
