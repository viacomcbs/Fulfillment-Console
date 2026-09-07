package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.diagnostic;

import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterLoadDiagnosticUtil;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/**
 * Dev diagnostic: login, wait for left filter to load, capture Console + API errors.
 * Review report log DIAGNOSTIC sections — test always completes without UI assertions.
 */
public class LF_Diag_001_CaptureLeftFilterLoadConsoleAndApiErrors extends LeftFilterLoadDiagnosticBaseTest {

    private final LeftFilterLoadDiagnosticUtil diagnosticUtil = new LeftFilterLoadDiagnosticUtil();

    @Test
    @Description("LF_Diag_001 — Login and capture Console/API errors while left filter loads")
    public void captureLeftFilterLoadConsoleAndApiErrors() throws InterruptedException {
        diagnosticUtil.captureLeftFilterLoadErrors(leftFilterPanelUtil, networkUtil);
    }
}
