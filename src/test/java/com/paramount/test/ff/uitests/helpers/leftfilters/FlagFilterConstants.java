package com.paramount.test.ff.uitests.helpers.leftfilters;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Canonical Flag left-filter option labels (Orders / Line Items).
 *
 * <p>UI hierarchy: Select All → {@link #IS_NOT_FLAGGED} → {@link #IS_FLAGGED} → reason children.
 * Checking {@link #IS_FLAGGED} also checks every nested reason — same as Select All for the flagged branch.
 * There is no indeterminate Select All after selecting both top-level options ({@code Is not flagged} + {@code Is flagged}).
 *
 * <p>TC408 indeterminate: select exactly one partial option — {@code Is not flagged} alone,
 * {@code Is flagged} alone (children cascade), or one flagged child only.
 */
public final class FlagFilterConstants {

    public static final String FILTER_DISPLAY_NAME = "Flag";

    public static final String IS_NOT_FLAGGED = "Is not flagged";
    public static final String IS_FLAGGED = "Is flagged";

    public static final List<String> FLAGGED_CHILD_OPTIONS = Collections.unmodifiableList(Arrays.asList(
            "Review failed",
            "Review passed",
            "Images issue",
            "Client rejection",
            "Issues",
            "QC needed",
            "Other"));

    /** Top-level and nested options in UI order (after Select All). */
    public static final List<String> FLAG_OPTION_ORDER = Collections.unmodifiableList(Arrays.asList(
            IS_NOT_FLAGGED,
            IS_FLAGGED,
            "Review failed",
            "Review passed",
            "Images issue",
            "Client rejection",
            "Issues",
            "QC needed",
            "Other"));

    private FlagFilterConstants() {
    }
}
