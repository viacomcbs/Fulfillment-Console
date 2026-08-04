package com.paramount.test.ff.pageobjects;

import com.synergy.core.driver.By;

public class HomePage {
	
	public By getHeaderTitle() {
		return By.XPath("//span[contains(normalize-space(),'FULFILLMENT CONSOLE')]");
	}
	
	public By filterPanelLabel(){
		return By.XPath("//span[contains(text(),'Filters')]");
	}

	public By AvatarFlagIcon(){
		return By.XPath("//div[contains(@class,'user-flag-filter-container ng-star-inserted')]");
	}

	public By DetailsPanelButton(){
		return By.XPath("//button[text()='Details']");
	}

	public By inputSearchicon(){
		return By.XPath("//div[@class='search-icon hide-search-box']");
	}

	public By inputGobalSearch() {
		return By.XPath("//input[@placeholder='Search']"
				+ " | //input[@placeholder='Search by title, ID or provider']");
	}

	public By activeFiltersButton() {
		return By.XPath("//button[contains(normalize-space(),'Active filters')]");
	}

    public By exportButton() {
        return By.XPath("//button[contains(@class,'export-btn') and contains(normalize-space(),'Export')]"
                + " | //button[normalize-space()='Export']");
    }

    public By exportOrdersMenuItem() {
        return By.XPath("//div[contains(@class,'export-dropdown-menu') and contains(@class,'show')]"
                + "//button[contains(@class,'dropdown-item') and normalize-space()='Orders']"
                + " | //div[contains(@class,'dropdown-menu') and contains(@class,'show')]"
                + "//button[normalize-space()='Orders']");
    }

	public By exportPackagesMenuItem() {
		return By.XPath("//div[contains(@class,'dropdown-menu') and contains(@class,'show')]"
				+ "//button[normalize-space()='Packages']");
	}

	public By exportLineItemsMenuItem() {
		return By.XPath("//div[contains(@class,'dropdown-menu') and contains(@class,'show')]"
				+ "//button[normalize-space()='Line items' or normalize-space()='Line Items']");
	}

	/** In-app async export progress (PROD: spinner + "Exporting File 0%" top-right of grid toolbar). */
	public By exportProgressIndicator() {
		return By.XPath("//*[contains(normalize-space(),'Exporting File')]"
				+ " | //*[contains(@class,'export') and contains(normalize-space(),'Exporting')]"
				+ " | //*[contains(@class,'export-progress') or contains(@class,'exporting')]");
	}

	/** Clickable area when export completes (link, toast, or notification near toolbar). */
	public By exportCompleteNotification() {
		return By.XPath("//*[contains(normalize-space(),'Export') and ("
				+ "contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),'complete')"
				+ " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),'ready')"
				+ " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),'download')"
				+ " or contains(.,'FulfillmentExport') or contains(.,'.xlsx'))]"
				+ "[self::a or self::button or @role='button' or contains(@class,'notification')]"
				+ " | //a[contains(@href,'.xlsx') or contains(@download,'.xlsx')]"
				+ " | //*[contains(@class,'toast')]"
				+ "[.//*[contains(.,'FulfillmentExport') or contains(.,'.xlsx')"
				+ " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),'export')]]");
	}

	/** Toast / snackbar after async Excel export completes (top area of page). */
	public By exportReadyToast() {
		return By.XPath("//div[contains(@class,'toast')]"
				+ "[.//*[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),'export')"
				+ " or contains(.,'FulfillmentExport') or contains(.,'.xlsx')"
				+ " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),'download')"
				+ " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),'ready')]]"
				+ " | //div[contains(@class,'toast-content')]"
				+ "[.//*[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),'export')"
				+ " or contains(.,'FulfillmentExport') or contains(.,'.xlsx')]]");
	}

	/** Clickable link/button inside export-ready toast (opens download / Downloads folder on PROD). */
	public By exportReadyToastAction() {
		return By.XPath("//div[contains(@class,'toast')]"
				+ "[.//*[contains(.,'FulfillmentExport') or contains(translate(normalize-space(.),"
				+ " 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),'export')]]"
				+ "//a[contains(@href,'.xlsx') or contains(.,'.xlsx') or contains(.,'download')]"
				+ " | //div[contains(@class,'toast')]"
				+ "[.//*[contains(.,'FulfillmentExport') or contains(translate(normalize-space(.),"
				+ " 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),'export')]]"
				+ "//button[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
				+ " 'abcdefghijklmnopqrstuvwxyz'),'download') or contains(.,'.xlsx')]"
				+ " | //div[contains(@class,'toast-content')]"
				+ "[.//*[contains(.,'FulfillmentExport') or contains(.,'.xlsx')]]//a | //div[contains(@class,'toast-content')]"
				+ "[.//*[contains(.,'FulfillmentExport') or contains(.,'.xlsx')]]//button");
	}

	/** Top-right notification bell / badge (blinks when export completes on PROD). */
	public By exportNotificationBell() {
		return By.XPath("//header//*[contains(@class,'notification') or contains(@class,'bell')]"
				+ "//i[contains(@class,'bi-bell')]/ancestor::button[1]"
				+ " | //button[contains(@class,'notification') or contains(@aria-label,'notification')]"
				+ " | //*[contains(@class,'notification-badge') or contains(@class,'badge')]"
				+ "[ancestor::*[contains(@class,'header') or contains(@class,'toolbar') or contains(@class,'navbar')][1]]"
				+ "//i[contains(@class,'bi-bell')]/ancestor::*[self::button or self::a][1]"
				+ " | //i[contains(@class,'bi-bell')]/ancestor::button[1]");
	}

	/** Export row inside notification panel (after bell click). */
	public By exportReadyNotificationItem() {
		return By.XPath("//*[contains(@class,'notification') or contains(@class,'dropdown-menu')]"
				+ "//*[contains(.,'FulfillmentExport') or contains(.,'.xlsx')"
				+ " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),'export')]"
				+ "[self::a or self::button or @role='button' or contains(@class,'notification')]"
				+ " | //a[contains(@href,'.xlsx') or contains(@download,'.xlsx')]"
				+ " | //*[contains(@class,'notification-item')]"
				+ "[.//*[contains(.,'FulfillmentExport') or contains(translate(normalize-space(.),"
				+ " 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),'export')]]");
	}

	/** PROD: after async export, toolbar may show a clickable xlsx link where "Exporting File" was. */
	public By exportToolbarDownloadLink() {
		return By.XPath("//div[contains(@class,'table-top') or contains(@class,'table-toolbar')]"
				+ "//*[contains(.,'FulfillmentExport') or contains(.,'.xlsx')]"
				+ "[self::a or self::button or @role='button' or contains(@class,'link')]"
				+ " | //app-fulfillment-main-table-container"
				+ "//*[contains(normalize-space(),'Export') and (contains(.,'.xlsx') or contains(.,'FulfillmentExport'))]"
				+ "][self::a or self::button or @role='button']"
				+ " | //*[contains(@class,'export') and contains(.,'.xlsx')]"
				+ "[self::a or self::button or @role='link']");
	}

}
