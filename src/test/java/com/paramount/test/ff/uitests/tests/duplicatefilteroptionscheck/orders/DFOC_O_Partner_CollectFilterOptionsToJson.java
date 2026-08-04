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
import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Collects Partner filter labels via AppSync GraphQL network capture (preferred), with UI scroll fallback.
 * Output is consumed by {@link DFOC_O_Partner_ValidateNoDuplicateFilterOptionsFromJson_Batch}.
 */
public class DFOC_O_Partner_CollectFilterOptionsToJson extends DuplicateFilterOptionsOrdersTabBaseTest {

    private static final String FILTER_NAME = OrdersLeftFilter.PARTNER.getDisplayName();

    @Test(priority = 0)
    @Parameters({"PathToPartnerOptionsJson", "TestEnvironment", "MinPartnerCollectionCount", "CollectNetworkOnly", "CollectUiScrollOnly"})
    @Description("Collect Partner filter option labels via GraphQL network capture and save to JSON")
    public void collectPartnerFilterOptionsToJson(
            @Optional("") String pathToPartnerOptionsJson,
            @Optional("DEV") String testEnvironment,
            @Optional("1000") String minPartnerCollectionCount,
            @Optional("true") String collectNetworkOnly,
            @Optional("false") String collectUiScrollOnly) throws Exception {
        softAssert = new SoftAssert(new Object() {}.getClass().getEnclosingMethod().getName(), this.getClass().getSimpleName());

        int expectedTotal = leftFilterPanelUtil.getTotalOptionCount(FILTER_NAME);
        int minRequired = Integer.parseInt(minPartnerCollectionCount);
        Logger.logMessage(FILTER_NAME + ": UI reports expected total = "
                + (expectedTotal > 0 ? expectedTotal : "unknown")
                + ", minimum required for this run = " + minRequired);

        List<String> partners;
        if ("true".equalsIgnoreCase(collectUiScrollOnly)) {
            Logger.logMessage(FILTER_NAME + ": using UI virtual-scroll collection only (skipping network capture)");
            partners = leftFilterPanelUtil.collectAllFilterOptionLabels(FILTER_NAME);
        } else if ("true".equalsIgnoreCase(collectNetworkOnly)) {
            partners = leftFilterPanelUtil.collectPartnerOptionsNetworkOnly(FILTER_NAME);
        } else {
            partners = leftFilterPanelUtil.collectAllFilterOptionLabelsPreferNetwork(FILTER_NAME);
        }
        Verify.softAssert1(!partners.isEmpty(),
                FILTER_NAME + " collection returned at least one partner option", softAssert);
        int uniquePartnerCount = PartnerOptionsDuplicateAnalyzer.uniqueCount(partners);

        String jsonPath = resolvePartnerOptionsJsonPath(pathToPartnerOptionsJson);
        if (!partners.isEmpty()) {
            PartnerOptionsJsonStore.write(jsonPath, testEnvironment, FILTER_NAME, expectedTotal, partners);
            exportPartnerAnalysisFiles(jsonPath, testEnvironment, partners);
            Logger.logReportMessage("Partner JSON saved: " + jsonPath + " ("
                    + partners.size() + " raw rows, " + uniquePartnerCount + " unique partners)");
        }

        Verify.softAssert1(uniquePartnerCount >= minRequired,
                FILTER_NAME + " collection should reach minimum " + minRequired
                        + " unique partners but collected " + uniquePartnerCount
                        + " unique (" + partners.size() + " raw rows)", softAssert);

        softAssert.assertAll();
    }

    private void exportPartnerAnalysisFiles(String jsonPath, String testEnvironment, List<String> partners)
            throws IOException {
        File jsonFile = new File(jsonPath);
        File outputDir = jsonFile.getParentFile();
        String envSuffix = testEnvironment == null ? "UNKNOWN" : testEnvironment.trim().toUpperCase(Locale.ROOT);

        String rawListPath = new File(outputDir, "PartnerNames_" + envSuffix + "_RawList.txt").getAbsolutePath();
        String exactDuplicatesPath = new File(outputDir, "PartnerNames_" + envSuffix + "_ExactDuplicates.txt")
                .getAbsolutePath();
        String caseInsensitivePath = new File(outputDir, "PartnerNames_" + envSuffix + "_CaseInsensitiveDuplicates.txt")
                .getAbsolutePath();

        PartnerOptionsDuplicateAnalyzer.writeRawList(rawListPath, partners);
        PartnerOptionsDuplicateAnalyzer.writeExactDuplicateReport(
                exactDuplicatesPath, PartnerOptionsDuplicateAnalyzer.findExactDuplicates(partners));
        PartnerOptionsDuplicateAnalyzer.writeCaseInsensitiveDuplicateReport(
                caseInsensitivePath, PartnerOptionsDuplicateAnalyzer.findCaseInsensitiveDuplicates(partners));

        Logger.logReportMessage("Partner raw list saved: " + rawListPath);
        Logger.logReportMessage("Partner exact duplicate report saved: " + exactDuplicatesPath);
        Logger.logReportMessage("Partner case-insensitive duplicate report saved: " + caseInsensitivePath);
    }

    private String resolvePartnerOptionsJsonPath(String pathFromSuite) {
        if (pathFromSuite != null && !pathFromSuite.trim().isEmpty()) {
            return System.getProperty("user.dir") + pathFromSuite.replace("/", File.separator);
        }
        return Config.getFilePath("PathToPartnerOptionsJson");
    }
}
