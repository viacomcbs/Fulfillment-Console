package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.partner;

import com.paramount.test.ff.common.loginUtil.Verify;
import com.paramount.test.ff.common.util.Config;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.helpers.partneroptions.PartnerOptionsDuplicateAnalyzer;
import com.paramount.test.ff.uitests.helpers.partneroptions.PartnerOptionsJsonStore;
import com.paramount.test.ff.uitests.tests.duplicatefilteroptionscheck.orders.DuplicateFilterOptionsOrdersTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.File;

/**
 * TC201 — Partner Search (Orders tab): same flow as Order Status TC201, extended to validate
 * every partner name from JSON. For each name: expand Partner → type full exact string →
 * assert exactly one visible option with the same label (no duplicates).
 */
public class LF_O_TC201_Partner_SearchTest extends DuplicateFilterOptionsOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.PARTNER.getDisplayName();

    @Test(priority = 1)
    @Parameters({"partnerBatchIndex", "partnerBatchSize", "PathToPartnerOptionsJson"})
    @Description("TC201 Partner: search full partner name from JSON list; assert exactly one exact-match result")
    public void tc201_partner_searchAllFromJson(
            @Optional("0") String partnerBatchIndex,
            @Optional("0") String partnerBatchSize,
            @Optional("") String pathToPartnerOptionsJson) throws Exception {
        int batchIndex = Integer.parseInt(partnerBatchIndex);
        int batchSize = Integer.parseInt(partnerBatchSize);
        int batchStartIndex = batchIndex * batchSize;

        softAssert = new SoftAssert("tc201_partner_searchAllFromJson", getClass().getSimpleName());

        String jsonPath = resolvePartnerOptionsJsonPath(pathToPartnerOptionsJson);
        PartnerOptionsJsonStore.PartnerOptionsData data = PartnerOptionsJsonStore.read(jsonPath);
        Logger.logMessage("TC201 Partner: loaded " + data.getPartners().size() + " raw partner row(s), "
                + PartnerOptionsDuplicateAnalyzer.uniqueCount(data.getPartners()) + " unique partner(s) from JSON (environment="
                + data.getEnvironment() + ", collectedAt=" + data.getCollectedAt() + ")");

        Verify.softAssert1(!data.getPartners().isEmpty(),
                "Partner JSON contains at least one option: " + jsonPath, softAssert);

        leftFilterPanelUtil.validateNoDuplicateFilterOptionsFromList(
                softAssert, FILTER,
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
