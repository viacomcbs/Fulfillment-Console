package com.paramount.test.ff.uitests.helpers.leftfilters;

public enum ConsoleTab {
    ORDERS("Orders"),
    LINE_ITEMS("Line Items");

    private final String tabLabel;

    ConsoleTab(String tabLabel) {
        this.tabLabel = tabLabel;
    }

    public String getTabLabel() {
        return tabLabel;
    }
}
