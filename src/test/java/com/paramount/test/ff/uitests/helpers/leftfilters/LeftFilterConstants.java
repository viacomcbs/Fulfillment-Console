package com.paramount.test.ff.uitests.helpers.leftfilters;

public final class LeftFilterConstants {

    public static final String SELECT_ALL_LABEL = "Select All";
    public static final int SELECT_ALL_DISABLED_OPTION_THRESHOLD = 1000;
    /** Partner (and other large-list filters): Select all tooltip when option count exceeds threshold. */
    public static final String SELECT_ALL_LARGE_LIST_TOOLTIP_SNIPPET = "fall below 1000 through filtering";
    public static final String PARTNER_SELECT_ALL_SEARCH_TEXT = "com";
    public static final int DEFAULT_WAIT_SECONDS = 10;
    public static final int QUICK_ELEMENT_TIMEOUT_MS = 2000;
    public static final int FILTER_EXPAND_WAIT_MS = 1500;
    /** Brief home-page settle after login before opening calendar (avoid long idle wait). */
    public static final int POST_LOGIN_HOME_SETTLE_MS = 10_000;
    public static final int SEARCH_SETTLE_POLL_MS = 400;
    public static final int SEARCH_SETTLE_MAX_ATTEMPTS = 15;
    public static final int SCROLL_SETTLE_MS = 400;
    public static final int MAX_STALE_SCROLL_ATTEMPTS = 30;
    public static final int COLLECTION_MAX_STALE_SCROLL_ATTEMPTS = 80;
    public static final int PARTNER_DUPLICATE_CHECK_BATCH_SIZE = 75;
    /** Option-order smoke: verifying the first N visible labels is enough (Partner / virtual scroll). */
    public static final int OPTION_ORDER_ALPHA_SAMPLE_SIZE = 3;

    public static final int SCROLL_STEP_PX = 300;

    /** Pause after real pointer hover before polling for float-ui tooltip (ms). */
    public static final int TOOLTIP_HOVER_SETTLE_MS = 750;
    /** Explicit wait window for visible float-ui-content after hover (seconds). */
    public static final int TOOLTIP_VISIBLE_WAIT_SECONDS = 5;
    public static final int TOOLTIP_POLL_INTERVAL_MS = 250;
    public static final int TOOLTIP_HOVER_MAX_ATTEMPTS = 2;

    private LeftFilterConstants() {
    }
}
