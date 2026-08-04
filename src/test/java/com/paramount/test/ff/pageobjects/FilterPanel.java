package com.paramount.test.ff.pageobjects;

import com.synergy.core.driver.By;

public class FilterPanel {

    public By getFilter(String filterName) {
        return By.XPath("//div[@class='filter-list-section']/descendant::span[contains(text(),'" + filterName + "')]");
    }

    public By getFilterOpenedModal(String filterName) {
        return By.XPath("");
    }

}
