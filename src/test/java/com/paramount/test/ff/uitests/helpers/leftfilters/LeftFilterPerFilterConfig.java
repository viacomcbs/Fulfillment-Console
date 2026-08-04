package com.paramount.test.ff.uitests.helpers.leftfilters;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.EnumSet;

/**
 * Per-filter test data and which categories apply (range/large-list filters differ).
 */
public final class LeftFilterPerFilterConfig {

    public enum FilterKind {
        CHECKBOX_LIST,
        RANGE,
        LARGE_LIST
    }

    public static final class FilterSpec {
        private final String displayName;
        private final FilterKind kind;
        private final String sampleOption;
        private final String tableColumn;
        private final String searchText;
        private final String secondSampleOption;
        private final Set<LeftFilterTestCategory> skippedCategories;

        FilterSpec(String displayName, FilterKind kind, String sampleOption, String tableColumn,
                   String searchText, String secondSampleOption,
                   Set<LeftFilterTestCategory> skippedCategories) {
            this.displayName = displayName;
            this.kind = kind;
            this.sampleOption = sampleOption;
            this.tableColumn = tableColumn;
            this.searchText = searchText;
            this.secondSampleOption = secondSampleOption;
            this.skippedCategories = skippedCategories;
        }

        public String getDisplayName() {
            return displayName;
        }

        public FilterKind getKind() {
            return kind;
        }

        public String getSampleOption() {
            return sampleOption;
        }

        public String getSecondSampleOption() {
            return secondSampleOption;
        }

        public String getTableColumn() {
            return tableColumn;
        }

        public String getSearchText() {
            return searchText;
        }

        public boolean isSkipped(LeftFilterTestCategory category) {
            return skippedCategories.contains(category);
        }
    }

    private static final Set<LeftFilterTestCategory> PARTNER_SKIPS = EnumSet.of(
            LeftFilterTestCategory.SELECT_ALL,
            LeftFilterTestCategory.TABLE_SYNC
    );

    private static final Set<LeftFilterTestCategory> RANGE_SKIPS = EnumSet.of(
            LeftFilterTestCategory.SELECT_ALL,
            LeftFilterTestCategory.SCROLL,
            LeftFilterTestCategory.ACTIVE_FILTERS,
            LeftFilterTestCategory.CLEAR_FILTERS
    );

    private static final Map<String, FilterSpec> ORDERS_SPECS = buildOrdersSpecs();
    private static final Map<String, FilterSpec> LINE_ITEMS_SPECS = buildLineItemsSpecs();

    private LeftFilterPerFilterConfig() {
    }

    public static FilterSpec specFor(ConsoleTab tab, String filterDisplayName) {
        Map<String, FilterSpec> map = tab == ConsoleTab.ORDERS ? ORDERS_SPECS : LINE_ITEMS_SPECS;
        FilterSpec spec = map.get(filterDisplayName);
        if (spec != null) {
            return spec;
        }
        return defaultCheckboxSpec(filterDisplayName);
    }

    public static boolean isCategoryApplicable(ConsoleTab tab, String filterDisplayName,
                                               LeftFilterTestCategory category) {
        return !specFor(tab, filterDisplayName).isSkipped(category);
    }

    private static Map<String, FilterSpec> buildOrdersSpecs() {
        Map<String, FilterSpec> specs = new HashMap<>();
        registerOrdersDefaults(specs);
        return specs;
    }

    private static Map<String, FilterSpec> buildLineItemsSpecs() {
        Map<String, FilterSpec> specs = new HashMap<>();
        registerOrdersDefaults(specs);
        return specs;
    }

    private static void registerOrdersDefaults(Map<String, FilterSpec> specs) {
        putCheckbox(specs, OrdersLeftFilter.LINE_ITEM_STATUS.getDisplayName(), "Delivering", null, "Delivery Complete");
        putCheckbox(specs, OrdersLeftFilter.ORDER_STATUS.getDisplayName(), "Done: Delivered", null, "Done: Failed");
        putLargeList(specs, OrdersLeftFilter.PARTNER.getDisplayName());
        putCheckbox(specs, OrdersLeftFilter.ENVIRONMENT.getDisplayName(), "Production", "prod", "Staging");
        putCheckbox(specs, OrdersLeftFilter.JOB_TYPE.getDisplayName(), "Fulfillment", "fulfill", null);
        putCheckbox(specs, OrdersLeftFilter.SUBMITTED_BY.getDisplayName(), "System", "sys", null);
        putCheckbox(specs, OrdersLeftFilter.SERIES_TITLE.getDisplayName(), "Survivor", "surv", null);
        putCheckbox(specs, OrdersLeftFilter.FLAG.getDisplayName(), "Priority", "prio", null);
        putCheckbox(specs, OrdersLeftFilter.ASSIGNED_TO.getDisplayName(), "Unassigned", "unassign", null);
        putRange(specs, OrdersLeftFilter.SEASON_NUMBER.getDisplayName());
        putCheckbox(specs, OrdersLeftFilter.REGION.getDisplayName(), "US", "us", null);
        putRange(specs, OrdersLeftFilter.EPISODE_NUMBER.getDisplayName());
        putCheckbox(specs, OrdersLeftFilter.BRAND.getDisplayName(), "Paramount", "param", null);
        putCheckbox(specs, OrdersLeftFilter.SYSTEM_NAME.getDisplayName(), "VMS", "vms", null);
        putCheckbox(specs, OrdersLeftFilter.LANGUAGE.getDisplayName(), "English", "eng", null);
        putCheckbox(specs, OrdersLeftFilter.DEMAND_SYSTEM.getDisplayName(), "OPC", "opc", null);
        putCheckbox(specs, OrdersLeftFilter.ERROR_MESSAGE.getDisplayName(), "Timeout", "time", null);
        putCheckbox(specs, OrdersLeftFilter.FRANCHISE.getDisplayName(), "Star Trek", "trek", null);
        putCheckbox(specs, OrdersLeftFilter.DELIVERY_PROTOCOL.getDisplayName(), "ASPERA", "asper", null);
        putCheckbox(specs, OrdersLeftFilter.ACTIVITY_TYPE.getDisplayName(), "Delivery", "deliv", null);
        putCheckbox(specs, OrdersLeftFilter.CONTENT_TYPE.getDisplayName(), "Episode", "epis", null);
    }

    private static void putCheckbox(Map<String, FilterSpec> specs, String displayName,
                                    String sampleOption, String searchText, String secondOption) {
        specs.put(displayName, new FilterSpec(
                displayName, FilterKind.CHECKBOX_LIST, sampleOption, displayName,
                searchText, secondOption, EnumSet.noneOf(LeftFilterTestCategory.class)));
    }

    private static void putLargeList(Map<String, FilterSpec> specs, String displayName) {
        specs.put(displayName, new FilterSpec(
                displayName, FilterKind.LARGE_LIST, "Antenna TV (Greece)", displayName,
                "antenna", null, PARTNER_SKIPS));
    }

    private static void putRange(Map<String, FilterSpec> specs, String displayName) {
        specs.put(displayName, new FilterSpec(
                displayName, FilterKind.RANGE, null, displayName,
                null, null, RANGE_SKIPS));
    }

    private static FilterSpec defaultCheckboxSpec(String displayName) {
        return new FilterSpec(displayName, FilterKind.CHECKBOX_LIST, null, displayName,
                "a", null, EnumSet.noneOf(LeftFilterTestCategory.class));
    }
}
