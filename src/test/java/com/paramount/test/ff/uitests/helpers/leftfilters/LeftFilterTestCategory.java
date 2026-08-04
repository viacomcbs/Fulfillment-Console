package com.paramount.test.ff.uitests.helpers.leftfilters;

/**
 * Test categories repeated for each left filter (Orders and Line items tabs).
 * <p>
 * Numbering convention (per filter block of ~20 filters):
 * TC1xx BASIC, TC2xx SEARCH, TC4xx SELECT_ALL, TC6xx TABLE_SYNC,
 * TC8xx SCROLL, TC10xx ACTIVE_FILTERS, TC11xx CLEAR_FILTERS.
 */
public enum LeftFilterTestCategory {
    BASIC(100, "Basic — filter exists, expand/collapse, options visible, Select all at top"),
    SEARCH(200, "Search — search box, type/clear (duplicate check is in DFOC partner suite)"),
    SELECT_ALL(400, "Select all — select/deselect, indeterminate, inside/outside count"),
    TABLE_SYNC(600, "Table sync — selected option matches table rows and record count"),
    SCROLL(800, "Scroll — CDK virtual scroll without error"),
    ACTIVE_FILTERS(1000, "Active filters — chips reflect single and multi selection"),
    CLEAR_FILTERS(1100, "Clear — remove from active chips or top Clear near Save filter");

    private final int idOffset;
    private final String description;

    LeftFilterTestCategory(int idOffset, String description) {
        this.idOffset = idOffset;
        this.description = description;
    }

    public int getIdOffset() {
        return idOffset;
    }

    public String getDescription() {
        return description;
    }

    /** e.g. filter index 1 (Order Status) + BASIC(100) => TC101 */
    public int testCaseId(int filterIndexOneBased) {
        return idOffset + filterIndexOneBased;
    }
}
