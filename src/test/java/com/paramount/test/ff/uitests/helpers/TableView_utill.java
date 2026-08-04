package com.paramount.test.ff.uitests.helpers;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.loginUtil.DriverUtil;
import com.paramount.test.ff.common.loginUtil.Verify;
import com.paramount.test.ff.common.loginUtil.WaitUtil;
import com.paramount.test.ff.common.util.WaitUtils;
import com.paramount.test.ff.pageobjects.TableView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.asserts.SoftAssert;

public class TableView_utill {
    private static final Logger log = LoggerFactory.getLogger(TableView_utill.class);
    TableView tableView = new TableView();
    WaitUtils WaitUtils = new WaitUtils();
    BaseTest baseTest = new BaseTest();
    DriverUtil driverUtil = new DriverUtil();
    public void isColumnNameVisible(SoftAssert softAssert, String columnName) {
        Verify.softAssert(WaitUtil.isDisplay(tableView.OrderColumnNameOnTableView(columnName), 5), "+columnName+ is displayed on Table View");
    }
    public void validateManageColumnIconOrderTab(SoftAssert softAssert) {
        Verify.softAssert(WaitUtil.isDisplay(tableView.getTableViewButton(), 5), "Manage Column Icon Order Tab");

    }
public void ifColumnNameSelected( String s)
{
    if(DriverUtil.isSelectedCheckbox(tableView.CheckboxOnTableView(s)))
    {
        com.paramount.test.ff.common.util.Logger.log("Column Name is Selected on table view: " + s);
        Verify.softAssert(WaitUtil.isDisplay(tableView.columNameOnUI(s), 5), s + " is displayed on UI");
    }
    else
    {
        com.paramount.test.ff.common.util.Logger.log("Column Name is not Selected on table view: " + s);
        DriverUtil.clickOnElement(tableView.CheckboxOnTableView(s),10);

        Verify.softAssert(WaitUtil.isDisplay(tableView.columNameOnUI(s), 5), s + " is displayed on UI");
    }
}
    public String[] tableviewOrderColumnList ={"DSID"}
           /* "Season", "Episode", "Order ID", "Xytech ID", "Asset ID",
            "DSID", "Order start date", "Order end date", "Partner", "Brand", "Partner Profile","Submitted By",
            "Episode name", "Content type", "Order Type", "Last updated", "Current system", "Assigned to",
            "State","Priority", "Error messages", "Purchage Order ID", "Fast Track", "Partner go live",
            "Partner end date"}*/;
    public String[] tableviewPackageColumnList ={"Package name", "Package ID", "Processing Start Date",
             "Last updated", "Endpoint"};
    public String[] tableviewLineitemColumnList ={"Type", "File name", "Submission"," Order ID", "Language",
            "Last updated", "Optional", "Assigned to", "Error message", "Start time", "UUID", "Edit CRID",
            "Material ID", "Total segments"};


}
