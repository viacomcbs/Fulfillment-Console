package com.paramount.test.ff.uitests.tests.duplicatefilteroptionscheck.orders;

import com.paramount.test.ff.common.loginUtil.Verify;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.SoftAssert;
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
 * Collects any large left-filter option list and exports duplicate analysis reports.
 */
public class DFOC_O_Filter_CollectFilterOptionsToJson extends DuplicateFilterOptionsOrdersTabBaseTest {

    @Test(priority = 0)
    @Parameters({"FilterName", "PathToFilterOptionsJson", "TestEnvironment", "MinCollectionCount",
            "CollectNetworkOnly", "CollectUiScrollOnly", "ExportPrefix"})
    @Description("Collect filter option labels and save JSON plus duplicate analysis TXT files")
    public void collectFilterOptionsToJson(
            @Optional("Partner") String filterName,
            @Optional("") String pathToFilterOptionsJson,
            @Optional("DEV") String testEnvironment,
            @Optional("1000") String minCollectionCount,
            @Optional("false") String collectNetworkOnly,
            @Optional("true") String collectUiScrollOnly,
            @Optional("") String exportPrefix) throws Exception {
        softAssert = new SoftAssert(new Object() {}.getClass().getEnclosingMethod().getName(), this.getClass().getSimpleName());

        int expectedTotal = leftFilterPanelUtil.getTotalOptionCount(filterName);
        int minRequired = Integer.parseInt(minCollectionCount);
        Logger.logMessage(filterName + ": UI reports expected total = "
                + (expectedTotal > 0 ? expectedTotal : "unknown")
                + ", minimum required for this run = " + minRequired);

        List<String> options;
        if ("true".equalsIgnoreCase(collectUiScrollOnly)) {
            Logger.logMessage(filterName + ": using UI virtual-scroll collection only (skipping network capture)");
            options = leftFilterPanelUtil.collectAllFilterOptionLabels(filterName);
        } else if ("true".equalsIgnoreCase(collectNetworkOnly)) {
            options = leftFilterPanelUtil.collectPartnerOptionsNetworkOnly(filterName);
        } else {
            options = leftFilterPanelUtil.collectAllFilterOptionLabelsPreferNetwork(filterName);
        }

        Verify.softAssert1(!options.isEmpty(),
                filterName + " collection returned at least one option", softAssert);
        int uniqueCount = PartnerOptionsDuplicateAnalyzer.uniqueCount(options);

        String jsonPath = resolveJsonPath(pathToFilterOptionsJson);
        if (!options.isEmpty()) {
            PartnerOptionsJsonStore.write(jsonPath, testEnvironment, filterName, expectedTotal, options);
            exportAnalysisFiles(jsonPath, filterName, testEnvironment, options, exportPrefix);
            Logger.logReportMessage(filterName + " JSON saved: " + jsonPath + " ("
                    + options.size() + " raw rows, " + uniqueCount + " unique options)");
        }

        Verify.softAssert1(uniqueCount >= minRequired,
                filterName + " collection should reach minimum " + minRequired
                        + " unique options but collected " + uniqueCount
                        + " unique (" + options.size() + " raw rows)", softAssert);

        softAssert.assertAll();
    }

    private void exportAnalysisFiles(String jsonPath, String filterName, String testEnvironment, List<String> options,
                                     String exportPrefixOverride)
            throws IOException {
        File jsonFile = new File(jsonPath);
        File outputDir = jsonFile.getParentFile();
        String prefix = resolveExportPrefix(filterName, testEnvironment, exportPrefixOverride);

        String rawListPath = new File(outputDir, prefix + "_RawList.txt").getAbsolutePath();
        String exactDuplicatesPath = new File(outputDir, prefix + "_ExactDuplicates.txt").getAbsolutePath();
        String caseInsensitivePath = new File(outputDir, prefix + "_CaseInsensitiveDuplicates.txt").getAbsolutePath();

        PartnerOptionsDuplicateAnalyzer.writeRawList(rawListPath, options);
        PartnerOptionsDuplicateAnalyzer.writeExactDuplicateReport(
                exactDuplicatesPath, PartnerOptionsDuplicateAnalyzer.findExactDuplicates(options));
        PartnerOptionsDuplicateAnalyzer.writeCaseInsensitiveDuplicateReport(
                caseInsensitivePath, PartnerOptionsDuplicateAnalyzer.findCaseInsensitiveDuplicates(options));

        Logger.logReportMessage(filterName + " raw list saved: " + rawListPath);
        Logger.logReportMessage(filterName + " exact duplicate report saved: " + exactDuplicatesPath);
        Logger.logReportMessage(filterName + " case-insensitive duplicate report saved: " + caseInsensitivePath);
    }

    static String toExportPrefix(String filterName, String testEnvironment) {
        String slug = filterName.replaceAll("[^A-Za-z0-9]+", "");
        String env = testEnvironment == null ? "UNKNOWN" : testEnvironment.trim().toUpperCase(Locale.ROOT);
        return slug + "_" + env;
    }

    static String resolveExportPrefix(String filterName, String testEnvironment, String exportPrefixOverride) {
        if (exportPrefixOverride != null && !exportPrefixOverride.trim().isEmpty()) {
            String env = testEnvironment == null ? "UNKNOWN" : testEnvironment.trim().toUpperCase(Locale.ROOT);
            return exportPrefixOverride.trim() + "_" + env;
        }
        return toExportPrefix(filterName, testEnvironment);
    }

    private String resolveJsonPath(String pathFromSuite) {
        if (pathFromSuite != null && !pathFromSuite.trim().isEmpty()) {
            return System.getProperty("user.dir") + pathFromSuite.replace("/", File.separator);
        }
        throw new IllegalStateException("PathToFilterOptionsJson suite parameter is required");
    }
}
