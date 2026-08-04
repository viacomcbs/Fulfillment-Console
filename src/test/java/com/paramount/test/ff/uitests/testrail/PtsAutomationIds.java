package com.paramount.test.ff.uitests.testrail;

/**
 * TestRail Git automation IDs (Java TestNG): fully-qualified class + {@code #method}.
 * Paste these into TestRail → Test Case → Automation → Automation ID when linking the Git repo.
 */
public final class PtsAutomationIds {

	private PtsAutomationIds() {
	}

	private static final String ORDERS = "com.paramount.test.ff.uitests.tests.tablevalidation.orders.";

	public static final String O_001 = ORDERS + "FF_PTS_O_001_ValidateColumnAvailableInTableView#validateColumnAvailableInTableView";
	public static final String O_002 = ORDERS + "FF_PTS_O_002_ValidateColumnVisibleInGrid#validateColumnVisibleInGrid";
	public static final String O_011 = ORDERS + "FF_PTS_O_011_ValidateColumnSort#validateColumnSort";
	public static final String O_009 = ORDERS + "FF_PTS_O_009_ValidateColumnSearch#validateColumnSearch";
	public static final String O_015 = ORDERS + "FF_PTS_O_015_ValidateTableMatchesDetails#validateTableMatchesDetails";
	public static final String O_013 = ORDERS + "FF_PTS_O_013_ValidateColumnInExcelExport#validateColumnInExcelExport";
	public static final String O_017 = ORDERS
			+ "FF_PTS_O_017_ValidateColumnHiddenWhenUnselectedAndExportExcludes#validateColumnHiddenWhenUnselectedAndExportExcludes";
}
