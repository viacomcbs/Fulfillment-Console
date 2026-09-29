package com.paramount.test.ff.uitests.helpers;

/**
 * Complete Manage Columns (Order table) column list from DEV UI screenshots.
 * Scroll order: Order columns → PACKAGE COLUMNS → LINE ITEM COLUMNS (58 total).
 */
public final class OrderTabColumnData {

    private OrderTabColumnData() {}

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

    public static final ColumnDef[] ALL_COLUMNS = {
            col("titleSeasonEpisode", "Title, Season, Episode", "order"),
            col("season", "Season", "order"),
            col("episode", "Episode", "order"),
            col("orderId", "Order ID", "order"),
            col("xytechId", "Xytech ID", "order"),
            col("assetId", "Asset ID", "order"),
            col("dsid", "DSID", "order"),
            col("orderStartDate", "Order start date", "order"),
            col("orderEndDate", "Order end date", "order"),
            col("partner", "Partner", "order"),
            col("brand", "Brand", "order"),
            col("partnerProfile", "Partner Profile", "order"),
            col("submittedBy", "Submitted by", "order"),
            col("episodeName", "Episode name", "order"),
            col("contentTypeOrder", "Content type", "order"),
            col("orderType", "Order Type", "order"),
            col("lastUpdatedOrder", "Last updated", "order"),
            col("currentSystem", "Current system", "order"),
            col("assignedToOrder", "Assigned to", "order"),
            col("state", "State", "order"),
            col("priority", "Priority", "order"),
            col("errorMessages", "Error messages", "order"),
            col("purchaseOrderId", "Purchase order ID", "order"),
            col("fastTrack", "Fast Track", "order"),
            col("partnerGoLive", "Partner go live", "order"),
            col("partnerEndDate", "Partner end date", "order"),
            col("deliveryProtocol", "Delivery Protocol", "order"),
            col("deliveryDate", "Delivery date", "order"),
            col("deliveryOffset", "Offset Delivery date", "order"),
            col("ptsPackagingId", "PTS Packaging ID", "order"),
            col("packageName", "Package name", "package"),
            col("packageId", "Package ID", "package"),
            col("processingStartDate", "Processing Start Date", "package"),
            col("lastUpdatedPackage", "Last updated", "package"),
            col("endpoint", "Endpoint", "package"),
            col("shippingDockPackageId", "Shipping Dock Package ID", "package"),
            col("type", "Type", "lineitem"),
            col("lineItemId", "LineItem ID", "lineitem"),
            col("fileName", "File name", "lineitem"),
            col("submission", "Submission", "lineitem"),
            col("orderIdLineItem", "Order ID", "lineitem"),
            col("language", "Language", "lineitem"),
            col("lastUpdatedLineItem", "Last updated", "lineitem"),
            col("optional", "Optional", "lineitem"),
            col("assignedToLineItem", "Assigned to", "lineitem"),
            col("errorMessage", "Error message", "lineitem"),
            col("startTime", "Start time", "lineitem"),
            col("uuid", "UUID", "lineitem"),
            col("editCrid", "Edit CRID", "lineitem"),
            col("contentTypeLineItem", "Content type", "lineitem"),
            col("materialId", "Material ID", "lineitem"),
            col("totalSegments", "Total segments", "lineitem"),
            col("activityType", "Activity Type", "lineitem"),
            col("job", "Type", "lineitem"),
            col("cbsId", "CBS ID", "lineitem"),
            col("downstreamSystemName", "Downstream System Name", "lineitem"),
            col("downstreamSystemId", "Downstream System ID", "lineitem"),
            col("vmid", "VMID", "lineitem"),
            col("systemStatus", "System Status", "lineitem"),
            col("notificationStatus", "Notification Status", "lineitem"),
    };

    private static ColumnDef col(String id, String label, String section) {
        return new ColumnDef(id, label, section);
    }

    /** DataProvider rows: { columnId, columnLabel, section } */
    public static Object[][] allManageColumnsData() {
        Object[][] data = new Object[ALL_COLUMNS.length][3];
        for (int i = 0; i < ALL_COLUMNS.length; i++) {
            data[i][0] = ALL_COLUMNS[i].id;
            data[i][1] = ALL_COLUMNS[i].label;
            data[i][2] = ALL_COLUMNS[i].section;
        }
        return data;
    }

    public static String[] labelsForSection(String section) {
        return java.util.Arrays.stream(ALL_COLUMNS)
                .filter(c -> c.section.equals(section))
                .map(c -> c.label)
                .toArray(String[]::new);
    }

    public static ColumnDef findByLabelAndSection(String label, String section) {
        for (ColumnDef column : ALL_COLUMNS) {
            if (column.label.equals(label) && column.section.equals(section)) {
                return column;
            }
        }
        return null;
    }

    public static ColumnDef findByIdAndSection(String id, String section) {
        for (ColumnDef column : ALL_COLUMNS) {
            if (column.id.equals(id) && column.section.equals(section)) {
                return column;
            }
        }
        return null;
    }
}
