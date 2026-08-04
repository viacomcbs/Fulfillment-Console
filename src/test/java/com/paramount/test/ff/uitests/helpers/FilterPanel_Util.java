package com.paramount.test.ff.uitests.helpers;

import com.paramount.test.ff.common.loginUtil.Verify;
import com.paramount.test.ff.common.loginUtil.WaitUtil;
import com.paramount.test.ff.common.util.WaitUtils;
import com.paramount.test.ff.pageobjects.FilterPanel;
import com.synergy.core.driver.By;

import static com.paramount.test.ff.common.base.BaseTest.driver;

public class FilterPanel_Util {

    FilterPanel filterPanel = new FilterPanel();
    WaitUtils waitUtils = new WaitUtils();

    public void validateTitleFilter(){

    }

    public void validateTitleCategory(String title) {
        By titleFilter = filterPanel.getFilter(title);
        Verify.softAssert(WaitUtil.isDisplay(titleFilter, 5), "Title filter is displayed: " + title);
        WaitUtil.isElementVisible(titleFilter);

        driver.get().finder().findElement(titleFilter).click();
        waitUtils.waitForVisibilityOfElement(titleFilter, 10);


    }
}
