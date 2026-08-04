package com.paramount.test.ff.uitests.helpers.leftfilters;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Left-panel filter names for the Line items tab (from msc-left-filter-panel HTML).
 * Update if Line items tab exposes a different filter set than Orders.
 */
public enum LineItemsLeftFilter {
    LINE_ITEM_STATUS("Line Item Status"),
    ORDER_STATUS("Order Status"),
    ENVIRONMENT("Environment"),
    JOB_TYPE("Job type"),
    SUBMITTED_BY("Submitted by"),
    PARTNER("Partner"),
    SERIES_TITLE("Series title"),
    FLAG("Flag"),
    ASSIGNED_TO("Assigned To"),
    SEASON_NUMBER("Season number"),
    REGION("Region"),
    EPISODE_NUMBER("Episode number"),
    BRAND("Brand"),
    SYSTEM_NAME("System Name"),
    LANGUAGE("Language"),
    DEMAND_SYSTEM("Demand system"),
    ERROR_MESSAGE("Error message"),
    FRANCHISE("Franchise"),
    DELIVERY_PROTOCOL("Delivery Protocol"),
    ACTIVITY_TYPE("Activity Type"),
    CONTENT_TYPE("Content type");

    private final String displayName;

    LineItemsLeftFilter(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static List<String> allDisplayNames() {
        return Arrays.stream(values())
                .map(LineItemsLeftFilter::getDisplayName)
                .collect(Collectors.toList());
    }
}
