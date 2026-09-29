package com.paramount.test.ff.uitests.tests.tablevalidation.bsd30019;

import com.paramount.test.ff.common.util.Config;
import com.paramount.test.ff.common.util.EmailUtil;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterEmailReport;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;

/**
 * Sends one consolidated email after {@code run-bsd-30019.ps1} copies three session result files.
 */
public class Bsd30019CombinedEmailReportTest {

    @Test
    public void sendCombinedBsd30019Email() {
        String title = Config.getString("LeftFilterEmailSuiteTitle");
        LeftFilterEmailReport.enable(title != null && !title.isBlank() ? title
                : "BSD-30019 — Error code functionality mismatch between table and left filter");

        String dirPath = Config.getString("AggregateResultsDir");
        File dir = new File(System.getProperty("user.dir"), dirPath.replace("/", File.separator));
        Assert.assertTrue(dir.isDirectory(), "Aggregate results dir missing: " + dir.getAbsolutePath());

        File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".xml"));
        Assert.assertNotNull(files);
        Assert.assertTrue(files.length >= 3, "Expected 3 session result files in " + dir.getAbsolutePath());

        for (File file : files) {
            LeftFilterEmailReport.mergeFromTestNgResultsFile(file);
        }
        LeftFilterEmailReport.removeHarnessResults();
        LeftFilterEmailReport.finalizeConsolidatedResults();
        Assert.assertTrue(LeftFilterEmailReport.getTotalCount() > 0, "No scenarios merged for BSD-30019 email");

        EmailUtil.sendResultEmail("", LeftFilterEmailReport.getPassedCount(),
                LeftFilterEmailReport.getFailedCount(), LeftFilterEmailReport.getSkippedCount(), 0);
    }
}
