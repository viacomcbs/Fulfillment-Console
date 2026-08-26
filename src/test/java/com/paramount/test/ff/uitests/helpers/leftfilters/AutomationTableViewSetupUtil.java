package com.paramount.test.ff.uitests.helpers.leftfilters;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.loginUtil.DriverUtil;
import com.paramount.test.ff.common.loginUtil.Verify;
import com.paramount.test.ff.common.loginUtil.WaitUtil;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.pageobjects.PtsPackagingIdPage;
import com.paramount.test.ff.pageobjects.TableView;
import com.paramount.test.ff.uitests.helpers.managecolumns.ManageColumnOptions;
import com.paramount.test.ff.uitests.helpers.managecolumns.ManageColumnsUtil;
import com.synergy.core.driver.By;
import com.synergy.core.driver.elements.DesktopBrowserElement;

import static com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterConstants.DEFAULT_WAIT_SECONDS;
import static com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterConstants.FILTER_EXPAND_WAIT_MS;

/**
 * Shared Manage columns setup for left-filter suites — saved view {@value #AUTOMATION_VIEW_NAME}
 * with all column checkboxes enabled (Orders and Line Items tabs configure separately).
 */
public final class AutomationTableViewSetupUtil extends BaseTest {

    public static final String AUTOMATION_VIEW_NAME = ManageColumnOptions.AUTOMATION_VIEW_NAME;

    private static final int PANEL_WAIT_S = 8;
    private static final int PANEL_SETTLE_MS = 1500;

    private final TableView tableView = new TableView();
    private final PtsPackagingIdPage ptsPage = new PtsPackagingIdPage();
    private final ManageColumnsUtil manageColumns = new ManageColumnsUtil();

    /**
     * Ensures {@link #AUTOMATION_VIEW_NAME} is the active table view on the current tab.
     * Creates the view on first run (all columns checked); later runs only select it from the dropdown.
     */
    public void ensureAAutomationView(SoftAssert softAssert, ConsoleTab tab) throws InterruptedException {
        if (LeftFilterSessionHelper.isAAutomationViewReady(tab)) {
            if (ensureAAutomationViewSelected(softAssert)) {
                return;
            }
            Logger.logMessage(AUTOMATION_VIEW_NAME + " not active after refresh — re-running full setup on "
                    + tab.getTabLabel());
        }

        manageColumns.openPanel(softAssert);
        if (isAAutomationViewActiveInPanel()) {
            Logger.logMessage(AUTOMATION_VIEW_NAME + " table view already active on " + tab.getTabLabel());
            manageColumns.closePanel(softAssert);
            LeftFilterSessionHelper.markAAutomationViewReady(tab);
            return;
        }

        if (selectAAutomationFromDropdown(softAssert)) {
            Logger.logMessage("Selected existing " + AUTOMATION_VIEW_NAME + " table view on " + tab.getTabLabel());
            manageColumns.closePanel(softAssert);
            waitForGridSettle();
            LeftFilterSessionHelper.markAAutomationViewReady(tab);
            Verify.softAssert(true, AUTOMATION_VIEW_NAME + " table view active on " + tab.getTabLabel());
            return;
        }

        Logger.logMessage("Creating " + AUTOMATION_VIEW_NAME + " table view with all columns on "
                + tab.getTabLabel());
        selectStandardViewFromDropdown(softAssert);
        manageColumns.expandLineItemColumnsAccordionIfNeeded();
        int toggled = manageColumns.selectAllUncheckedCheckboxes();
        Logger.logMessage("Manage columns: enabled " + toggled + " unchecked column checkbox(es)");
        saveNewAAutomationView(softAssert);
        manageColumns.closePanel(softAssert);
        waitForGridSettle();
        LeftFilterSessionHelper.markAAutomationViewReady(tab);
        Verify.softAssert(true, AUTOMATION_VIEW_NAME + " table view created on " + tab.getTabLabel());
    }

    /** Lightweight re-select after browser refresh when the view was already configured this suite. */
    public boolean ensureAAutomationViewSelected(SoftAssert softAssert) throws InterruptedException {
        manageColumns.openPanel(softAssert);
        if (isAAutomationViewActiveInPanel()) {
            manageColumns.closePanel(softAssert);
            return true;
        }
        if (selectAAutomationFromDropdown(softAssert)) {
            manageColumns.closePanel(softAssert);
            waitForGridSettle();
            return true;
        }
        manageColumns.closePanel(softAssert);
        return false;
    }

    public boolean isAAutomationViewActiveInPanel() {
        return WaitUtil.isDisplayFast(tableView.activeTableViewInDropdown(AUTOMATION_VIEW_NAME), 2)
                || WaitUtil.isDisplayFast(tableView.tableViewDefaultSelected(AUTOMATION_VIEW_NAME), 2)
                || WaitUtil.isDisplayFast(ptsPage.activeTableViewLabel(AUTOMATION_VIEW_NAME), 2);
    }

    private boolean selectAAutomationFromDropdown(SoftAssert softAssert) throws InterruptedException {
        if (!DriverUtil.clickOnElement(ptsPage.tableViewDropdown(), 5)) {
            return false;
        }
        Thread.sleep(800);
        By option = ptsPage.tableViewOption(AUTOMATION_VIEW_NAME);
        if (!WaitUtil.isDisplayFast(option, 5)) {
            DriverUtil.clickOnElement(ptsPage.tableViewDropdown(), 3);
            return false;
        }
        Verify.softAssert(DriverUtil.clickOnElement(option, DEFAULT_WAIT_SECONDS),
                "Selected " + AUTOMATION_VIEW_NAME + " from table view dropdown");
        Thread.sleep(PANEL_SETTLE_MS);
        return isAAutomationViewActiveInPanel();
    }

    private void selectStandardViewFromDropdown(SoftAssert softAssert) throws InterruptedException {
        Verify.softAssert(DriverUtil.clickOnElement(ptsPage.tableViewDropdown(), DEFAULT_WAIT_SECONDS),
                "Opened table view dropdown for Standard view baseline");
        Thread.sleep(800);
        Verify.softAssert(DriverUtil.clickOnElement(ptsPage.standardViewOption(), DEFAULT_WAIT_SECONDS),
                "Selected Standard view before creating " + AUTOMATION_VIEW_NAME);
        Thread.sleep(PANEL_SETTLE_MS);
    }

    private void saveNewAAutomationView(SoftAssert softAssert) throws InterruptedException {
        DriverUtil.scrollToElement(ptsPage.saveNewViewButton());
        Verify.softAssert(DriverUtil.clickOnElement(ptsPage.saveNewViewButton(), DEFAULT_WAIT_SECONDS),
                "Clicked Save new view for " + AUTOMATION_VIEW_NAME);
        Thread.sleep(PANEL_SETTLE_MS);

        Verify.softAssert(WaitUtil.isDisplayFast(ptsPage.saveNewViewNameInput(), PANEL_WAIT_S),
                "Save new view name input visible");
        DesktopBrowserElement nameInput = driver.get().finder().findElement(ptsPage.saveNewViewNameInput());
        nameInput.click();
        nameInput.executeScript("arguments[0].value='';");
        nameInput.sendKeys(AUTOMATION_VIEW_NAME);
        Thread.sleep(300);

        Verify.softAssert(DriverUtil.clickOnElement(ptsPage.saveNewViewConfirmButton(), DEFAULT_WAIT_SECONDS),
                "Saved new table view as " + AUTOMATION_VIEW_NAME);
        Thread.sleep(PANEL_SETTLE_MS * 2);

        boolean confirmed = WaitUtil.isDisplayFast(ptsPage.tableViewCreatedMessage(), 8)
                || WaitUtil.isDisplayFast(ptsPage.tableViewCreatedToast(), 5)
                || isAAutomationViewActiveInPanel();
        Verify.softAssert(confirmed,
                AUTOMATION_VIEW_NAME + " saved (toast or active view in dropdown)");
    }

    private void waitForGridSettle() throws InterruptedException {
        Thread.sleep(FILTER_EXPAND_WAIT_MS * 2);
        WaitUtil.waitForJSToLoad(15);
    }
}
