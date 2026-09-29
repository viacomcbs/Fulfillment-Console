package com.paramount.test.ff.uitests.helpers.leftfilters;

import com.paramount.test.ff.uitests.helpers.managecolumns.ManageColumnOptions;

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
            LeftFilterTestCategory.SEARCH,
            LeftFilterTestCategory.SELECT_ALL,
            LeftFilterTestCategory.TABLE_SYNC,
            LeftFilterTestCategory.SCROLL,
            LeftFilterTestCategory.ACTIVE_FILTERS,
            LeftFilterTestCategory.CLEAR_FILTERS
    );

    /** Episode number: dedicated TC312 range + TC412 Select all; table sync enabled (TC612). */
    private static final Set<LeftFilterTestCategory> EPISODE_NUMBER_SKIPS = EnumSet.of(
            LeftFilterTestCategory.SEARCH,
            LeftFilterTestCategory.SELECT_ALL,
            LeftFilterTestCategory.SCROLL,
            LeftFilterTestCategory.ACTIVE_FILTERS,
            LeftFilterTestCategory.CLEAR_FILTERS
    );

    /** Season number: dedicated TC310 range + TC410 Select all; table sync enabled (TC610). */
    private static final Set<LeftFilterTestCategory> SEASON_NUMBER_SKIPS = EnumSet.of(
            LeftFilterTestCategory.SEARCH,
            LeftFilterTestCategory.SELECT_ALL,
            LeftFilterTestCategory.SCROLL,
            LeftFilterTestCategory.ACTIVE_FILTERS,
            LeftFilterTestCategory.CLEAR_FILTERS
    );

    /** Language: dedicated TC615 expand-order line-item sync (Activity Type-style). */
    private static final Set<LeftFilterTestCategory> LANGUAGE_SKIPS = EnumSet.of(
            LeftFilterTestCategory.TABLE_SYNC
    );

    /** Region: dedicated TC611 count-only sync with zero-count fallback. */
    private static final Set<LeftFilterTestCategory> REGION_SKIPS = EnumSet.of(
            LeftFilterTestCategory.TABLE_SYNC
    );

    /** Demand system: dedicated TC616 count sync + Details panel validation. */
    private static final Set<LeftFilterTestCategory> DEMAND_SYSTEM_SKIPS = EnumSet.of(
            LeftFilterTestCategory.TABLE_SYNC
    );

    /** System Name: dedicated TC614 count-only sync (Environment-style). */
    private static final Set<LeftFilterTestCategory> SYSTEM_NAME_SKIPS = EnumSet.of(
            LeftFilterTestCategory.TABLE_SYNC
    );

    /** Content type: dedicated TC621 order-level column sync (Brand-style). */
    private static final Set<LeftFilterTestCategory> CONTENT_TYPE_SKIPS = EnumSet.of(
            LeftFilterTestCategory.TABLE_SYNC
    );

    /** Delivery Protocol: dedicated TC619 count sync + package Endpoint Info validation. */
    private static final Set<LeftFilterTestCategory> DELIVERY_PROTOCOL_SKIPS = EnumSet.of(
            LeftFilterTestCategory.TABLE_SYNC
    );

    /** Job type: dedicated TC622 expand-order Type column sync on Orders view. */
    private static final Set<LeftFilterTestCategory> JOB_TYPE_SKIPS = EnumSet.of(
            LeftFilterTestCategory.TABLE_SYNC
    );

    /** Line Items: Order Status, Environment, Franchise, Demand system, System Name — dedicated count-only TC602/603/618/616/614. */
    private static final Set<LeftFilterTestCategory> LINE_ITEMS_COUNT_ONLY_SKIPS = EnumSet.of(
            LeftFilterTestCategory.TABLE_SYNC
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
        overrideLineItemsCountOnlyFilters(specs);
        return specs;
    }

    /** Line Items table sync is count-only for filters that use dedicated TC602/603/614/616/618 classes. */
    private static void overrideLineItemsCountOnlyFilters(Map<String, FilterSpec> specs) {
        putLineItemsCountOnlyCheckbox(specs, LineItemsLeftFilter.ORDER_STATUS.getDisplayName(),
                null, "Done: Failed");
        putLineItemsCountOnlyCheckbox(specs, LineItemsLeftFilter.ENVIRONMENT.getDisplayName());
        putLineItemsCountOnlyCheckbox(specs, LineItemsLeftFilter.FRANCHISE.getDisplayName());
        putLineItemsCountOnlyCheckbox(specs, LineItemsLeftFilter.DEMAND_SYSTEM.getDisplayName());
        putLineItemsCountOnlyCheckbox(specs, LineItemsLeftFilter.SYSTEM_NAME.getDisplayName());
    }

    private static void registerOrdersDefaults(Map<String, FilterSpec> specs) {
        putCheckbox(specs, OrdersLeftFilter.LINE_ITEM_STATUS.getDisplayName(), "Delivering", "Status",
                "Delivery Complete");
        putCheckbox(specs, OrdersLeftFilter.ORDER_STATUS.getDisplayName(), null, "Status", null, "Done: Failed");
        putLargeList(specs, OrdersLeftFilter.PARTNER.getDisplayName());
        putCheckbox(specs, OrdersLeftFilter.ENVIRONMENT.getDisplayName(), null, null, null, null);
        putJobTypeCheckbox(specs, OrdersLeftFilter.JOB_TYPE.getDisplayName());
        putCheckbox(specs, OrdersLeftFilter.SUBMITTED_BY.getDisplayName(), null, ManageColumnOptions.SUBMITTED_BY, null, null);
        putLargeList(specs, OrdersLeftFilter.SERIES_TITLE.getDisplayName(), ManageColumnOptions.TITLE_SEASON_EPISODE);
        putCheckbox(specs, OrdersLeftFilter.FLAG.getDisplayName(), null, null, "flag", null);
        putCheckbox(specs, OrdersLeftFilter.ASSIGNED_TO.getDisplayName(), null, ManageColumnOptions.ASSIGNED_TO, "unassign", null);
        putSeasonRange(specs, OrdersLeftFilter.SEASON_NUMBER.getDisplayName(),
                ManageColumnOptions.TITLE_SEASON_EPISODE);
        putRegionCheckbox(specs, OrdersLeftFilter.REGION.getDisplayName());
        putEpisodeRange(specs, OrdersLeftFilter.EPISODE_NUMBER.getDisplayName(),
                ManageColumnOptions.TITLE_SEASON_EPISODE);
        putCheckbox(specs, OrdersLeftFilter.BRAND.getDisplayName(), null, ManageColumnOptions.BRAND, null, null);
        putSystemNameCheckbox(specs, OrdersLeftFilter.SYSTEM_NAME.getDisplayName());
        putLanguageCheckbox(specs, OrdersLeftFilter.LANGUAGE.getDisplayName());
        putDemandSystemCheckbox(specs, OrdersLeftFilter.DEMAND_SYSTEM.getDisplayName());
        putCheckbox(specs, OrdersLeftFilter.ERROR_MESSAGE.getDisplayName(), null, ManageColumnOptions.ERROR_MESSAGES, null, null);
        putCheckbox(specs, OrdersLeftFilter.FRANCHISE.getDisplayName(), null, null, null, null);
        putDeliveryProtocolCheckbox(specs, OrdersLeftFilter.DELIVERY_PROTOCOL.getDisplayName());
        putCheckbox(specs, OrdersLeftFilter.ACTIVITY_TYPE.getDisplayName(), null, ManageColumnOptions.ACTIVITY_TYPE, null, null);
        putContentTypeCheckbox(specs, OrdersLeftFilter.CONTENT_TYPE.getDisplayName());
    }

    private static void putJobTypeCheckbox(Map<String, FilterSpec> specs, String displayName) {
        specs.put(displayName, new FilterSpec(
                displayName, FilterKind.CHECKBOX_LIST, null, null,
                null, null, JOB_TYPE_SKIPS));
    }

    private static void putCheckbox(Map<String, FilterSpec> specs, String displayName,
                                    String sampleOption, String searchText, String secondOption) {
        putCheckbox(specs, displayName, sampleOption, displayName, searchText, secondOption);
    }

    private static void putCheckbox(Map<String, FilterSpec> specs, String displayName,
                                    String sampleOption, String tableColumn, String searchText,
                                    String secondOption) {
        specs.put(displayName, new FilterSpec(
                displayName, FilterKind.CHECKBOX_LIST, sampleOption, tableColumn,
                searchText, secondOption, EnumSet.noneOf(LeftFilterTestCategory.class)));
    }

    private static void putLargeList(Map<String, FilterSpec> specs, String displayName) {
        specs.put(displayName, new FilterSpec(
                displayName, FilterKind.LARGE_LIST, "Antenna TV (Greece)", displayName,
                "antenna", null, PARTNER_SKIPS));
    }

    private static void putLargeList(Map<String, FilterSpec> specs, String displayName, String tableColumn) {
        specs.put(displayName, new FilterSpec(
                displayName, FilterKind.LARGE_LIST, null, tableColumn,
                null, null, PARTNER_SKIPS));
    }

    private static void putRange(Map<String, FilterSpec> specs, String displayName, String tableColumn) {
        specs.put(displayName, new FilterSpec(
                displayName, FilterKind.RANGE, null, tableColumn,
                null, null, RANGE_SKIPS));
    }

    private static void putEpisodeRange(Map<String, FilterSpec> specs, String displayName, String tableColumn) {
        specs.put(displayName, new FilterSpec(
                displayName, FilterKind.RANGE, null, tableColumn,
                null, null, EPISODE_NUMBER_SKIPS));
    }

    private static void putSeasonRange(Map<String, FilterSpec> specs, String displayName, String tableColumn) {
        specs.put(displayName, new FilterSpec(
                displayName, FilterKind.RANGE, null, tableColumn,
                null, null, SEASON_NUMBER_SKIPS));
    }

    private static void putLanguageCheckbox(Map<String, FilterSpec> specs, String displayName) {
        specs.put(displayName, new FilterSpec(
                displayName, FilterKind.CHECKBOX_LIST, null, ManageColumnOptions.LANGUAGE,
                null, null, LANGUAGE_SKIPS));
    }

    private static void putRegionCheckbox(Map<String, FilterSpec> specs, String displayName) {
        specs.put(displayName, new FilterSpec(
                displayName, FilterKind.CHECKBOX_LIST, null, null,
                null, null, REGION_SKIPS));
    }

    private static void putDemandSystemCheckbox(Map<String, FilterSpec> specs, String displayName) {
        specs.put(displayName, new FilterSpec(
                displayName, FilterKind.CHECKBOX_LIST, null, null,
                null, null, DEMAND_SYSTEM_SKIPS));
    }

    private static void putSystemNameCheckbox(Map<String, FilterSpec> specs, String displayName) {
        specs.put(displayName, new FilterSpec(
                displayName, FilterKind.CHECKBOX_LIST, null, null,
                null, null, SYSTEM_NAME_SKIPS));
    }

    private static void putContentTypeCheckbox(Map<String, FilterSpec> specs, String displayName) {
        specs.put(displayName, new FilterSpec(
                displayName, FilterKind.CHECKBOX_LIST, null, ManageColumnOptions.CONTENT_TYPE,
                null, null, CONTENT_TYPE_SKIPS));
    }

    private static void putDeliveryProtocolCheckbox(Map<String, FilterSpec> specs, String displayName) {
        specs.put(displayName, new FilterSpec(
                displayName, FilterKind.CHECKBOX_LIST, null, null,
                null, null, DELIVERY_PROTOCOL_SKIPS));
    }

    private static void putLineItemsCountOnlyCheckbox(Map<String, FilterSpec> specs, String displayName) {
        putLineItemsCountOnlyCheckbox(specs, displayName, null, null);
    }

    private static void putLineItemsCountOnlyCheckbox(Map<String, FilterSpec> specs, String displayName,
                                                      String sampleOption, String secondOption) {
        specs.put(displayName, new FilterSpec(
                displayName, FilterKind.CHECKBOX_LIST, sampleOption, null,
                null, secondOption, LINE_ITEMS_COUNT_ONLY_SKIPS));
    }

    private static FilterSpec defaultCheckboxSpec(String displayName) {
        return new FilterSpec(displayName, FilterKind.CHECKBOX_LIST, null, displayName,
                "a", null, EnumSet.noneOf(LeftFilterTestCategory.class));
    }
}
