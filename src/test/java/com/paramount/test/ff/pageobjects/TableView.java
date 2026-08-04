package com.paramount.test.ff.pageobjects;

import com.synergy.core.driver.By;

public class TableView {
    public By getTableViewButton() {
        return By.XPath("//button[@id='tableViewButton']");
    }
    public By standardViewdropdownButton() {
        return By.XPath("//div[@class='dropdown-reset-container']");
    }
    public By manageColumnOpenOrderTab() {
        return By.XPath("//span[contains(text(),'Manage columns')]");
    }
    public By selectStandardViewMode() {
        return By.XPath("//div[contains(@class,'option')][.//label[contains(normalize-space(),'Standard view')]]");
    }
    public By StandardViewMode() {
        return By.XPath("//label[contains(text(),'Standard view')]");
    }
    public By SaveConfirmationMessage() {
        return By.XPath("//div[contains(text(),'Table view created')]");
    }

    public By OrderColumnNameOnTableView(String s) {
        return By.XPath("//div[contains(@class, 'table-column')]//div[contains(text(), 'Order columns')]//following::label[contains(text(), '"+s+"')]");
    }
    public By PackageColumnNameOnTableView(String s) {
        return By.XPath("//div[contains(@class, 'table-column')]//div[contains(text(), 'Package columns')]//following::label[contains(text(), '"+s+"')]");
    }
    public By LineitemColumnNameOnTableView(String s) {
        return By.XPath("//div[contains(@class, 'table-column')]//div[contains(text(), 'Line item columns')]//following::label[contains(text(), '"+s+"')]");
    }
    public By CheckboxOnTableView(String s) {
        return By.XPath("//label[(text())='"+s+"']/preceding-sibling::input[@type='checkbox']");
    }
    public By columNameOnUI(String s) {
        return By.XPath("//label[(text())='"+s+"']/preceding-sibling::input[@type='checkbox']");
    }
    public By saveNewButtonTableView() {
        return By.XPath("//span[(text())='Save new view']");
    }
    public By saveNewTableViewPopup() {
        return By.XPath("//span[(text())='Save new table view']");
    }
    public By inputTableViewName() {
        return By.XPath("//input[@placeholder='Enter table view name']");
    }
    public By saveButtonOnTableViewPopup() {
        return By.XPath("//span[text()='Save new']");
    }
    /** Active view name in the table-view dropdown (span or label; panel must be open). */
    public By tableViewDefaultSelected(String s) {
        return By.XPath("//div[@class='dropdown-reset-container']"
                + "//*[self::span or self::label][normalize-space()='" + s + "']"
                + " | //span[normalize-space()='" + s + "']"
                + " | //label[normalize-space()='" + s + "']");
    }

    public By activeTableViewInDropdown(String viewName) {
        return By.XPath("//div[@class='dropdown-reset-container']"
                + "//*[self::span or self::label][normalize-space()='" + viewName + "']");
    }

    /** Named table view option inside the Manage columns dropdown (Standard view, Automation, etc.). */
    public By tableViewDropdownOption(String viewName) {
        return By.XPath("//div[contains(@class,'option')]"
                + "[.//span[normalize-space()='" + viewName + "']"
                + " or .//label[normalize-space()='" + viewName + "']"
                + " or normalize-space()='" + viewName + "']");
    }

    public By tableViewCreatedToast() {
        return By.XPath("//div[contains(@class,'toast-content')]//div[contains(text(),'Table view created')]");
    }

    public By confirmtiontableviewPopup() {
        return By.XPath("//div[contains(@class, 'toast-content')]//div[contains(text(), 'Table view created')]");
    }


    //span[text()='jjj']
  ////span[text()='Table view name']
//div[contains(@class, 'table-column')]//div[contains(text(), 'Order columns')]//following::label[contains(text(), '"+s+"')]
}
