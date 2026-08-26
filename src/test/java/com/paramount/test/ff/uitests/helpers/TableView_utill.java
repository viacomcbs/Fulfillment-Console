package com.paramount.test.ff.uitests.helpers;

import com.paramount.test.ff.common.loginUtil.DriverUtil;
import com.paramount.test.ff.common.loginUtil.Verify;
import com.paramount.test.ff.common.loginUtil.WaitUtil;
import com.paramount.test.ff.common.util.WaitUtils;
import com.paramount.test.ff.pageobjects.TableView;
import com.paramount.test.ff.uitests.helpers.managecolumns.ManageColumnOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.asserts.SoftAssert;

public class TableView_utill {
    private static final Logger log = LoggerFactory.getLogger(TableView_utill.class);
    TableView tableView = new TableView();
    WaitUtils WaitUtils = new WaitUtils();
    DriverUtil driverUtil = new DriverUtil();

    public void isColumnNameVisible(SoftAssert softAssert, String columnName) {
        Verify.softAssert(WaitUtil.isDisplay(tableView.OrderColumnNameOnTableView(columnName), 5), "+columnName+ is displayed on Table View");
    }

    public void validateManageColumnIconOrderTab(SoftAssert softAssert) {
        Verify.softAssert(WaitUtil.isDisplay(tableView.getTableViewButton(), 5), "Manage Column Icon Order Tab");
    }

    public void ifColumnNameSelected(String s) {
        if (DriverUtil.isSelectedCheckbox(tableView.CheckboxOnTableView(s))) {
            com.paramount.test.ff.common.util.Logger.log("Column Name is Selected on table view: " + s);
            Verify.softAssert(WaitUtil.isDisplay(tableView.columNameOnUI(s), 5), s + " is displayed on UI");
        } else {
            com.paramount.test.ff.common.util.Logger.log("Column Name is not Selected on table view: " + s);
            DriverUtil.clickOnElement(tableView.CheckboxOnTableView(s), 10);
            Verify.softAssert(WaitUtil.isDisplay(tableView.columNameOnUI(s), 5), s + " is displayed on UI");
        }
    }

    /** @see ManageColumnOptions#ORDER_COLUMNS */
    public String[] tableviewOrderColumnList = ManageColumnOptions.ORDER_COLUMNS;

    /** @see ManageColumnOptions#PACKAGE_COLUMNS */
    public String[] tableviewPackageColumnList = ManageColumnOptions.PACKAGE_COLUMNS;

    /** @see ManageColumnOptions#LINE_ITEM_COLUMNS */
    public String[] tableviewLineitemColumnList = ManageColumnOptions.LINE_ITEM_COLUMNS;
}
