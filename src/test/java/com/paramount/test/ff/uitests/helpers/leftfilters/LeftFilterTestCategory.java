package com.paramount.test.ff.uitests.helpers.leftfilters;

/**
 * Test categories repeated for each left filter (Orders and Line items tabs).
 * <p>
 * Numbering convention (per filter block of ~20 filters):
 * TC1xx BASIC, TC1xx+1 OPTION_ORDER, TC8xx SCROLL, TC2xx SEARCH, TC6xx TABLE_SYNC,
 * TC10xx ACTIVE_FILTERS, TC11xx CLEAR_FILTERS, TC4xx SELECT_ALL.
 * <p>
 * Suite execution order (all left filters): Basic → Option order → Scroll → Search →
 * Table sync → Active filters → Clear → Select all.
 */
public enum LeftFilterTestCategory {
    BASIC(100, "Basic — filter exists, expand/collapse, options visible, Select all at top"),
    SEARCH(200, "Search — search box, type/clear (duplicate check is in DFOC partner suite)"),
    SELECT_ALL(400, "Select all — select/deselect, indeterminate, inside/outside count"),
    TABLE_SYNC(600, "Table sync — filter count matches table total; first visible order row matches selection"),
    SCROLL(800, "Scroll — CDK virtual scroll without error"),
    ACTIVE_FILTERS(1000, "Active filters — selected option(s) appear in Active filters chips"),
    CLEAR_FILTERS(1100, "Clear — active filters has chips and Clear control is available to click");

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
