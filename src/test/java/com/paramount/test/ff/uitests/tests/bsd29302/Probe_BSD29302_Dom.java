package com.paramount.test.ff.uitests.tests.bsd29302;

import com.paramount.test.ff.common.util.Logger;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Probe_BSD29302_Dom extends BSD29302BaseTest {

    @Test(enabled = false)
    public void dumpDomForPackageExpand() throws Exception {
        launchFulfillmentConsole();
        packageDetailsUtil.prepareDeliveredOrdersView();
        tableViewUtill.openManageColumns();
        tableViewUtill.selectStandardView();
        if (tableViewUtill.isColumnListed(COLUMN_NAME, SECTION)) {
            tableViewUtill.enableColumn(COLUMN_NAME, SECTION);
        }
        tableViewUtill.closeManageColumnsIfOpen();
        Thread.sleep(2000);

        Object step1 = runScript(
                "var out = {};"
                        + "out.dropdownToggles = Array.from(document.querySelectorAll('button.dropdown-toggle')).slice(0, 15)"
                        + ".map(function(b){return {text:(b.textContent||'').trim(), cls:b.className};});"
                        + "out.delivered = document.querySelectorAll('msc-status.delivered-items').length;"
                        + "var pkgBtn = document.querySelector('button.dropdown-toggle:not(.ellipsisButton)');"
                        + "if (pkgBtn) { pkgBtn.click(); out.clickedPkgBtn = (pkgBtn.textContent||'').trim(); }"
                        + "return JSON.stringify(out, null, 2);");
        Thread.sleep(1200);

        Object step2 = runScript(
                "return JSON.stringify({"
                        + "shippingDock: (document.body.textContent||'').split('Shipping Dock Package ID').length - 1,"
                        + "packageGroups: document.querySelectorAll('.package-group, [class*=package-group]').length,"
                        + "innerPackages: document.querySelectorAll('.inner-package').length,"
                        + "dropdownMenu: (document.querySelector('.dropdown-menu.show')||{}).textContent||''"
                        + "}, null, 2);");

        Object step3 = runScript(
                "var cb = document.querySelector('msc-status.delivered-items');"
                        + "if (cb) {"
                        + "  var row = cb.closest('[class*=row], tr, div');"
                        + "  if (row) {"
                        + "    var checkbox = row.querySelector('input[type=checkbox]');"
                        + "    if (checkbox) checkbox.click(); else row.click();"
                        + "  }"
                        + "}"
                        + "var details = Array.from(document.querySelectorAll('button')).find(function(b){"
                        + "  return (b.textContent||'').trim() === 'Details';"
                        + "});"
                        + "if (details) details.click();"
                        + "return 'selected';");
        Thread.sleep(1500);

        Object step4 = runScript(
                "return JSON.stringify({"
                        + "shippingDockInDetails: (document.body.textContent||'').split('Shipping Dock Package ID').length - 1,"
                        + "uuidCount: ((document.body.textContent||'').match(/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}/g)||[]).length,"
                        + "detailsSnippet: (document.body.textContent||'').slice("
                        + "Math.max(0, (document.body.textContent||'').indexOf('Shipping Dock') - 20), "
                        + "Math.max(0, (document.body.textContent||'').indexOf('Shipping Dock') + 120))"
                        + "}, null, 2);");

        String payload = "step1:\n" + step1 + "\nstep2:\n" + step2 + "\nstep3: " + step3 + "\nstep4:\n" + step4;
        Files.createDirectories(Paths.get("test-output"));
        Files.write(Paths.get("test-output", "bsd29302-dom-probe.json"), payload.getBytes(StandardCharsets.UTF_8));
        Logger.logReportMessage("Wrote test-output/bsd29302-dom-probe.json");
    }

    private Object runScript(String script) {
        return com.paramount.test.ff.common.base.BaseTest.driver.get().browser().executeScript(script);
    }
}
