package com.paramount.test.ff.uitests.tests.bsd29174;

import io.qameta.allure.Description;
import org.testng.annotations.Test;

/**
 * Base for per-status tooltip vs expanded line-item count validation.
 * Flow: Yesterday filter → Line Item Status filter → first order → hover chip → compare counts.
 */
public abstract class StatusCountMatchTest extends BSD29174BaseTest {

    protected abstract String statusLabel();

    /** Text typed into the Line Item Status filter search box. */
    protected String filterSearchTerm() {
        return LineItemStatusConstants.filterSearchTerm(statusLabel());
    }

    @Test
    @Description("BSD-29174: Yesterday + Line Item Status filter; tooltip count matches expanded line items on first order")
    public void validateTooltipCountMatchesExpandedLineItems() throws InterruptedException {
        initSoftAssert(getClass().getSimpleName() + "_" + statusLabel().replace(' ', '_'));
        orderStatusTooltipUtil.verifyTooltipCountViaLineItemStatusFilter(
                filterPanelUtil, filterSearchTerm(), statusLabel(), false);
        softAssert.assertAll();
    }
}
