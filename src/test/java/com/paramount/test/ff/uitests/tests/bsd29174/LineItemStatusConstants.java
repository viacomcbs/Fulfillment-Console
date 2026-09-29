package com.paramount.test.ff.uitests.tests.bsd29174;

/**
 * Line item statuses shown in order-level status summary tooltips (BSD-29174 / BSD-28250).
 */
public final class LineItemStatusConstants {

    public static final String PROCESSING_FAILED = "Processing failed";
    /** PROD tooltip/filter label (e.g. "Processing 1"). */
    public static final String PROCESSING = "Processing";
    public static final String PACKAGING_PENDING = "Packaging pending";
    /** PROD Line Item Status filter label (e.g. "Packaging (928)"). */
    public static final String PACKING = "Packaging";
    public static final String PACKING_PENDING = "Packing pending";
    public static final String READY_FOR_DELIVERY = "Ready for delivery";
    public static final String DELIVERY_FAILED = "Delivery failed";
    public static final String DELIVERY_CANCELLED = "Delivery cancelled";
    /** PROD UI uses title case: "Delivery Complete". */
    public static final String DELIVERY_COMPLETED = "Delivery Complete";
    public static final String DELIVERED = "Delivered";
    /** PROD Line Item Status filter label. */
    public static final String PENDING = "Delivery Pending";
    public static final String IN_PROGRESS = "In progress";

    public static final String[] ALL_STATUSES = {
            PROCESSING_FAILED,
            PROCESSING,
            PACKAGING_PENDING,
            PACKING,
            PACKING_PENDING,
            READY_FOR_DELIVERY,
            DELIVERY_FAILED,
            DELIVERY_CANCELLED,
            DELIVERY_COMPLETED,
            DELIVERED,
            PENDING,
            IN_PROGRESS
    };

    /**
     * Short search terms for the Line Item Status filter box (avoids hitting the main grid search).
     */
    public static String filterSearchTerm(String statusLabel) {
        if (statusLabel == null) {
            return "";
        }
        switch (statusLabel) {
            case PROCESSING_FAILED:
                return "failed";
            case PROCESSING:
                return "Process";
            case PACKAGING_PENDING:
                return "Packag pending";
            case PACKING:
                return "Packag";
            case PACKING_PENDING:
                return "Packing pend";
            case READY_FOR_DELIVERY:
                return "Ready for";
            case DELIVERY_FAILED:
                return "Delivery fail";
            case DELIVERY_CANCELLED:
                return "cancelled";
            case DELIVERY_COMPLETED:
                return "Delivery Comp";
            case DELIVERED:
                return "Delivered";
            case PENDING:
                return "Delivery Pend";
            case IN_PROGRESS:
                return "In progress";
            default:
                return statusLabel.length() > 10 ? statusLabel.substring(0, 10) : statusLabel;
        }
    }

    private LineItemStatusConstants() {
    }
}
