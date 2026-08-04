package com.paramount.test.ff.uitests.tests.duplicatefilteroptionscheck.orders;

import com.paramount.test.ff.common.loginUtil.Verify;
import com.paramount.test.ff.common.util.Config;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.helpers.partneroptions.PartnerOptionsDuplicateAnalyzer;
import com.paramount.test.ff.uitests.helpers.partneroptions.PartnerOptionsJsonStore;
import io.qameta.allure.Description;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.File;

/**
 * Validates Partner duplicate-free search using a pre-collected JSON list (no virtual-scroll skip).
 */
public class DFOC_O_Partner_ValidateNoDuplicateFilterOptionsFromJson_Batch
        extends DuplicateFilterOptionsOrdersTabBaseTest {

    private static final String FILTER_NAME = OrdersLeftFilter.PARTNER.getDisplayName();

    @Test(priority = 0)
    @Parameters({"partnerBatchIndex", "partnerBatchSize", "PathToPartnerOptionsJson"})
    @Description("Expand Partner, search full partner name, assert exactly one visible result equal to search string")
    public void validatePartnerFilterNoDuplicateOptionsFromJson(
            @Optional("0") String partnerBatchIndex,
            @Optional("75") String partnerBatchSize,
            @Optional("") String pathToPartnerOptionsJson) throws Exception {
        int batchIndex = Integer.parseInt(partnerBatchIndex);
        int batchSize = Integer.parseInt(partnerBatchSize);
        int batchStartIndex = batchIndex * batchSize;

        softAssert = new SoftAssert(new Object() {}.getClass().getEnclosingMethod().getName(), this.getClass().getSimpleName());

        String jsonPath = resolvePartnerOptionsJsonPath(pathToPartnerOptionsJson);
        PartnerOptionsJsonStore.PartnerOptionsData data = PartnerOptionsJsonStore.read(jsonPath);
        Logger.logMessage("Loaded " + data.getPartners().size() + " raw partner row(s), "
                + PartnerOptionsDuplicateAnalyzer.uniqueCount(data.getPartners()) + " unique partner(s) from JSON (environment="
                + data.getEnvironment() + ", collectedAt=" + data.getCollectedAt() + ")");

        Verify.softAssert1(!data.getPartners().isEmpty(),
                "Partner JSON contains at least one option: " + jsonPath, softAssert);

        leftFilterPanelUtil.validateNoDuplicateFilterOptionsFromList(
                softAssert, FILTER_NAME,
                PartnerOptionsDuplicateAnalyzer.uniquePartners(data.getPartners()),
                batchStartIndex, batchSize);
        softAssert.assertAll();
    }

    private String resolvePartnerOptionsJsonPath(String pathFromSuite) {
        if (pathFromSuite != null && !pathFromSuite.trim().isEmpty()) {
            return System.getProperty("user.dir") + pathFromSuite.replace("/", File.separator);
        }
        return Config.getFilePath("PathToPartnerOptionsJson");
    }
}
