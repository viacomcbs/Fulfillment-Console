package com.paramount.test.ff.uitests.tests.bsd29174;

import com.paramount.test.ff.common.util.Logger;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Probe_BSD29174_Dom extends BSD29174BaseTest {

    @Test(enabled = false)
    public void dumpDomForStatusTooltips() throws Exception {
        launchFulfillmentConsole();
        orderStatusTooltipUtil.searchOrder(KNOWN_ORDER_ID);
        Thread.sleep(1500);

        Object step1 = orderStatusTooltipUtil.dumpGridDiagnostics(KNOWN_ORDER_ID);

        orderStatusTooltipUtil.hoverStatusIconForOrder(KNOWN_ORDER_ID);
        Thread.sleep(1000);

        Object step2 = runScript(
                "return JSON.stringify({"
                        + "tooltip: (function(){"
                        + "  var nodes = document.querySelectorAll('.cdk-overlay-container [class*=tooltip], [role=tooltip]');"
                        + "  for (var i = 0; i < nodes.length; i++) {"
                        + "    var t = (nodes[i].textContent || '').trim();"
                        + "    if (t) return t;"
                        + "  }"
                        + "  return null;"
                        + "})(),"
                        + "overlayCount: document.querySelectorAll('.cdk-overlay-container *').length"
                        + "}, null, 2);");

        String payload = "step1:\n" + step1 + "\nstep2:\n" + step2;
        Files.createDirectories(Paths.get("test-output"));
        Files.write(Paths.get("test-output", "bsd29174-dom-probe.json"), payload.getBytes(StandardCharsets.UTF_8));
        Logger.logReportMessage("Wrote test-output/bsd29174-dom-probe.json");
    }

    private Object runScript(String script) {
        return com.paramount.test.ff.common.base.BaseTest.driver.get().browser().executeScript(script);
    }
}
