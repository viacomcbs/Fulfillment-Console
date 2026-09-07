package com.paramount.test.ff.uitests.helpers.bsd29967;

/** One-shot publisher for accumulated BSD-29967 workflow evidence to Jira. */
public final class Bsd29967JiraManualPost {

    private Bsd29967JiraManualPost() {
    }

    public static void main(String[] args) {
        Bsd29967JiraEvidenceUtil.publishAccumulatedEvidenceNow();
    }
}
