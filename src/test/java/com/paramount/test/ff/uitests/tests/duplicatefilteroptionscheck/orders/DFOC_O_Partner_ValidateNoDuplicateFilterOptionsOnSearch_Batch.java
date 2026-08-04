package com.paramount.test.ff.uitests.tests.duplicatefilteroptionscheck.orders;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import io.qameta.allure.Description;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Partner duplicate-filter validation for a configurable batch slice.
 * Used by {@code DFOC_O_Partner_ParallelSuite.xml} to split work across Synergy sessions.
 */
public class DFOC_O_Partner_ValidateNoDuplicateFilterOptionsOnSearch_Batch
        extends DuplicateFilterOptionsOrdersTabBaseTest {

    private static final String FILTER_NAME = OrdersLeftFilter.PARTNER.getDisplayName();

    @Test(priority = 0)
    @Parameters({"partnerBatchIndex", "partnerBatchSize"})
    @Description("Validate narrow search inside Partner filter on Orders tab returns exactly one result per option (batch slice)")
    public void validatePartnerFilterNoDuplicateOptionsOnSearch(
            @Optional("0") String partnerBatchIndex,
            @Optional("100") String partnerBatchSize) throws InterruptedException {
        int batchIndex = Integer.parseInt(partnerBatchIndex);
        int batchSize = Integer.parseInt(partnerBatchSize);
        int batchStartIndex = batchIndex * batchSize;

        softAssert = new SoftAssert(new Object() {}.getClass().getEnclosingMethod().getName(), this.getClass().getSimpleName());
        leftFilterPanelUtil.validateNoDuplicateFilterOptionsOnSearch(
                softAssert, FILTER_NAME, batchStartIndex, batchSize);
        softAssert.assertAll();
    }
}
