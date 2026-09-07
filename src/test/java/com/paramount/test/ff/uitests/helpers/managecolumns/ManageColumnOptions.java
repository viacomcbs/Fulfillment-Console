package com.paramount.test.ff.uitests.helpers.managecolumns;

/**
 * Canonical Manage columns option labels for Fulfillment Console table views.
 * Sourced from PROD/UAT Manage columns panel (Orders + Line Items tabs) and
 * {@code docs/manage_column_video_frames/}.
 */
public final class ManageColumnOptions {

    private ManageColumnOptions() {
    }

    /** Panel section identifiers matching the Manage columns UI grouping. */
    public enum Section {
        /** "Order columns" accordion — order-level fields (e.g. PTS Packaging ID, Order ID, DSID). */
        ORDER,
        /** Package-level fields (Package ID, Endpoint, etc.) on the Orders tab. */
        PACKAGE,
        /**
         * Line-item-level flat list above the "LINE ITEM COLUMNS" header on the Orders tab
         * (Activity Type, LineItem ID, UUID — not the Order columns accordion).
         */
        ORDER_LINE_ITEM,
        /** "LINE ITEM COLUMNS" accordion on Orders tab, or flat list on Line Items tab. */
        LINE_ITEM
    }

    // --- Frequently referenced columns (cross-suite) ---

    public static final String ORDER_ID = "Order ID";
    public static final String ORDER_START_DATE = "Order start date";
    public static final String LINE_ITEM_ID = "LineItem ID";
    public static final String DSID = "DSID";
    public static final String PTS_PACKAGING_ID = "PTS Packaging ID";
    public static final String BRAND = "Brand";
    public static final String SUBMITTED_BY = "Submitted By";
    public static final String ASSIGNED_TO = "Assigned to";
    public static final String ACTIVITY_TYPE = "Activity Type";
    public static final String MATERIAL_ID = "Material ID";
    public static final String EDIT_CRID = "Edit CRID";
    public static final String UUID = "UUID";
    public static final String PARTNER_END_DATE = "Partner end date";
    public static final String OPEN_TEXT_ID = "Open Text ID";

    /** Saved table view used by left-filter suites ({@code AutomationTableViewSetupUtil}); sorts first in dropdown. */
    public static final String AUTOMATION_VIEW_NAME = "AAAAA";

    /**
     * Line-item-level columns required on Orders tab for TC620 and expanded-row reads
     * (flat list above "LINE ITEM COLUMNS" header).
     */
    public static final String[] ESSENTIAL_ORDERS_LINE_ITEM_COLUMNS = {
            ACTIVITY_TYPE,
            LINE_ITEM_ID,
            UUID
    };

    /** Minimum columns for Line Items tab automation view. */
    public static final String[] ESSENTIAL_LINE_ITEM_COLUMNS = {
            ACTIVITY_TYPE,
            LINE_ITEM_ID
    };

    /** All options under "Order columns" on the Orders tab Manage columns panel. */
    public static final String[] ORDER_COLUMNS = {
            "Asset ID",
            "Brand",
            "Content type",
            "Current system",
            DSID,
            "Episode",
            "Episode name",
            "Error messages",
            "Fast Track",
            "Last updated",
            OPEN_TEXT_ID,
            "Order end date",
            ORDER_ID,
            "Order start date",
            "Order Type",
            "Partner",
            PARTNER_END_DATE,
            "Partner go live",
            "Partner Profile",
            "Priority",
            PTS_PACKAGING_ID,
            "Purchase Order ID",
            "Season",
            "State",
            "Submitted By",
            "Title, Episode",
            "Xytech ID"
    };

    /** Package-level columns on the Orders tab Manage columns panel. */
    public static final String[] PACKAGE_COLUMNS = {
            "Endpoint",
            "Last updated",
            "Package ID",
            "Package name",
            "Processing Start Date",
            "Shipping Dock Package ID"
    };

    /**
     * Line-item-level fields in the flat list above "LINE ITEM COLUMNS" on the Orders tab.
     * Activity Type and LineItem ID live here (not under the Order columns accordion).
     */
    public static final String[] ORDER_LINE_ITEM_COLUMNS = {
            ACTIVITY_TYPE,
            "Assigned to",
            "CBS ID",
            "Content type",
            "Downstream System ID",
            "Downstream System Name",
            "Error message",
            "Language",
            "Last updated",
            LINE_ITEM_ID,
            "Optional",
            ORDER_ID,
            "Start time",
            "Submission",
            "System Status",
            "Total segments",
            UUID,
            "VMID"
    };

    /** Options under "LINE ITEM COLUMNS" on Orders tab, or the flat Line Items tab list. */
    public static final String[] LINE_ITEM_COLUMNS = {
            ACTIVITY_TYPE,
            "Assigned to",
            "Content type",
            EDIT_CRID,
            "Error message",
            "File name",
            "Language",
            "Last updated",
            LINE_ITEM_ID,
            MATERIAL_ID,
            "Notification Status",
            "Optional",
            ORDER_ID,
            "Start time",
            "Submission",
            "Total segments",
            "Type",
            UUID
    };

    public static String[] columnsForSection(Section section) {
        switch (section) {
            case ORDER:
                return ORDER_COLUMNS;
            case PACKAGE:
                return PACKAGE_COLUMNS;
            case ORDER_LINE_ITEM:
                return ORDER_LINE_ITEM_COLUMNS;
            case LINE_ITEM:
                return LINE_ITEM_COLUMNS;
            default:
                throw new IllegalArgumentException("Unknown section: " + section);
        }
    }

    /**
     * Resolves Manage columns section for a label on the Orders tab.
     * Order-level (e.g. PTS Packaging ID) vs line-item-level (e.g. Activity Type) must not be mixed.
     */
    public static Section defaultSectionForColumn(String columnLabel) {
        for (String name : ORDER_COLUMNS) {
            if (name.equals(columnLabel)) {
                return Section.ORDER;
            }
        }
        for (String name : PACKAGE_COLUMNS) {
            if (name.equals(columnLabel)) {
                return Section.PACKAGE;
            }
        }
        for (String name : ORDER_LINE_ITEM_COLUMNS) {
            if (name.equals(columnLabel)) {
                return Section.ORDER_LINE_ITEM;
            }
        }
        return Section.LINE_ITEM;
    }
}
