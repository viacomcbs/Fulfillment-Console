package com.paramount.test.ff.uitests.helpers.featureflag;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.loginUtil.DriverUtil;
import com.paramount.test.ff.common.loginUtil.Verify;
import com.paramount.test.ff.common.loginUtil.WaitUtil;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.pageobjects.FeatureFlagPage;
import com.synergy.core.driver.By;

import static com.paramount.test.ff.common.base.BaseTest.driver;

/** Manage-columns and grid column helpers for BSD-28459 regression. */
public class FeatureFlagColumnUtil extends BaseTest {

    public enum ColumnSection {
        ORDER, LINE_ITEM
    }

    private static final int PANEL_WAIT_S = 10;
    private static final int GRID_WAIT_S = 15;
    private static final int PANEL_SETTLE_MS = 800;

    private final FeatureFlagPage page = new FeatureFlagPage();

    public void openManageColumnsPanel(SoftAssert softAssert) throws InterruptedException {
        if (WaitUtil.isDisplayFast(page.manageColumnsPanelHeading(), 2)) {
            Logger.logReportMessage("Manage columns panel already open");
            return;
        }
        DriverUtil.scrollToElement(page.tableViewButton());
        Verify.softAssert1(DriverUtil.clickOnElement(page.tableViewButton(), 5),
                "Clicked Table View button", softAssert);
        Thread.sleep(PANEL_SETTLE_MS);
        Verify.softAssert1(WaitUtil.isDisplay(page.manageColumnsPanelHeading(), PANEL_WAIT_S),
                "Manage columns panel is open", softAssert);
    }

    public void validateColumnListed(ColumnSection section, String columnLabel, SoftAssert softAssert) {
        By label = section == ColumnSection.ORDER
                ? page.orderColumnLabel(columnLabel)
                : page.lineItemColumnLabel(columnLabel);
        Verify.softAssert1(WaitUtil.isDisplay(label, PANEL_WAIT_S),
                columnLabel + " listed under " + section.name() + " columns in Manage columns", softAssert);
    }

    public void enableColumnIfNeeded(ColumnSection section, String columnLabel, SoftAssert softAssert)
            throws InterruptedException {
        By checkbox = page.columnCheckbox(columnLabel);
        if (!WaitUtil.isDisplayFast(checkbox, 3)) {
            validateColumnListed(section, columnLabel, softAssert);
        }
        if (DriverUtil.isSelectedCheckbox(checkbox)) {
            Logger.logReportMessage(columnLabel + " already enabled in Manage columns");
            return;
        }
        Verify.softAssert1(DriverUtil.clickOnElement(checkbox, 5),
                "Enabled " + columnLabel + " checkbox in Manage columns", softAssert);
        saveChangesIfPresent(softAssert);
    }

    public void saveChangesIfPresent(SoftAssert softAssert) throws InterruptedException {
        if (WaitUtil.isDisplayFast(page.saveChangesButton(), 2)) {
            Verify.softAssert1(DriverUtil.clickOnElement(page.saveChangesButton(), 5),
                    "Clicked Save changes in Manage columns panel", softAssert);
            Thread.sleep(PANEL_SETTLE_MS);
        }
    }

    public void closeManageColumnsPanel(SoftAssert softAssert) throws InterruptedException {
        if (WaitUtil.isDisplayFast(page.manageColumnsPanelHeading(), 1)) {
            DriverUtil.clickOnElement(page.tableViewButton(), 3);
            Thread.sleep(PANEL_SETTLE_MS);
        }
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
