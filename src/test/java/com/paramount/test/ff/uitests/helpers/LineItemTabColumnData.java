package com.paramount.test.ff.uitests.helpers;

/**
 * Manage Columns column list for the Line Items tab (authoritative: docs/lineitem-tab-columns.json).
 */
public final class LineItemTabColumnData {

    private LineItemTabColumnData() {}

    public static final class ColumnDef {
        public final String id;
        public final String label;
        public final String section;

        public ColumnDef(String id, String label, String section) {
            this.id = id;
            this.label = label;
            this.section = section;
        }
    }

    public static final String SECTION = "table";

    public static final ColumnDef[] ALL_COLUMNS = {
            col("titleSeasonEpisode", "Title, Season, Episode"),
            col("season", "Season"),
            col("episode", "Episode"),
            col("lineItemId", "LineItem ID"),
            col("type", "Type"),
            col("orderType", "Order Type"),
            col("fileName", "File name"),
            col("submission", "Submission"),
            col("orderIdOrder", "Order ID (Order level)"),
            col("xytechId", "Xytech ID"),
            col("assetId", "Asset ID"),
            col("dsid", "DSID"),
            col("orderStartDate", "Order start date"),
            col("orderEndDate", "Order end date"),
            col("orderLastUpdated", "Order last updated"),
            col("partner", "Partner"),
            col("brand", "Brand"),
            col("submittedBy", "Submitted by"),
            col("episodeName", "Episode name"),
            col("contentType", "Content type"),
            col("currentSystem", "Current system"),
            col("priority", "Priority"),
            col("purchaseOrderId", "Purchase order ID"),
            col("fastTrack", "Fast Track"),
            col("packageName", "Package name"),
            col("packageId", "Package ID"),
            col("endpoint", "Endpoint"),
            col("language", "Language"),
            col("lastUpdated", "Last updated"),
            col("startTime", "Start time"),
            col("status", "Status"),
            col("optional", "Optional"),
            col("assignedTo", "Assigned to"),
            col("errorMessage", "Error message"),
            col("uuid", "UUID"),
            col("editCrid", "Edit CRID"),
            col("materialId", "Material ID"),
            col("totalSegments", "Total segments"),
            col("activityType", "Activity Type"),
            col("job", "Type"),
            col("cbsId", "CBS ID"),
            col("downstreamSystemName", "Downstream System Name"),
            col("downstreamSystemId", "Downstream System ID"),
            col("vmid", "VMID"),
            col("systemStatus", "System Status"),
            col("deliveryOffset", "Delivery Offset"),
            col("notificationStatus", "Notification Status"),
    };

    private static ColumnDef col(String id, String label) {
        return new ColumnDef(id, label, SECTION);
    }

    public static Object[][] allManageColumnsData() {
        Object[][] data = new Object[ALL_COLUMNS.length][3];
        for (int i = 0; i < ALL_COLUMNS.length; i++) {
            data[i][0] = ALL_COLUMNS[i].id;
            data[i][1] = ALL_COLUMNS[i].label;
            data[i][2] = ALL_COLUMNS[i].section;
        }
        return data;
    }

    public static ColumnDef findByLabel(String label) {
        for (ColumnDef column : ALL_COLUMNS) {
            if (column.label.equals(label)) {
                return column;
            }
        }
        return null;
    }

    public static ColumnDef findById(String id) {
        for (ColumnDef column : ALL_COLUMNS) {
            if (column.id.equals(id)) {
                return column;
            }
        }
        return null;
    }
}
