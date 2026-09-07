package com.paramount.test.ff.uitests.helpers.bsd29967;

import com.paramount.test.ff.common.loginUtil.DriverUtil;
import com.paramount.test.ff.common.loginUtil.Verify;
import com.paramount.test.ff.common.loginUtil.WaitUtil;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.pageobjects.Bsd29967Page;
import com.synergy.core.driver.elements.DesktopBrowserElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.paramount.test.ff.common.base.BaseTest.driver;

/** Parse focus URL and read FC Details panel DSIDs for BSD-29967. */
public class Bsd29967DetailsUtil {

    private static final int DETAILS_PANEL_DSID_MAX_WAIT_S = 3;

    private static final Pattern FOCUS_ORDER_PATTERN =
            Pattern.compile("order=([^&]+)__([a-zA-Z0-9_-]+)", Pattern.CASE_INSENSITIVE);

    private final Bsd29967Page bsd29967Page = new Bsd29967Page();

    public List<String> readAndStoreDetailsPanelDsids(SoftAssert softAssert) throws InterruptedException {
        parseOrderFromCurrentUrl(softAssert);
        scrollToIdentifiersSection();
        waitForDetailsPanelDsidsIfNeeded();
        List<String> dsids = readIdentifiersSectionDsids(softAssert);
        Bsd29967SessionHelper.setFcDetailsDsids(dsids);
        return dsids;
    }

    public List<String> tryReadAndStoreDetailsPanelDsids() throws InterruptedException {
        parseOrderFromCurrentUrlOptional();
        scrollToIdentifiersSection();
        waitForDetailsPanelDsidsIfNeeded();
        List<String> dsids = readDsIdsViaLocator();
        if (dsids.isEmpty()) {
            dsids = readDsIdsViaJs();
        }
        Logger.logReportMessage("Details panel DSID values (" + dsids.size() + "): " + dsids);
        Bsd29967SessionHelper.setFcDetailsDsids(dsids);
        return dsids;
    }

    private void parseOrderFromCurrentUrlOptional() {
        String url = driver.get().browser().getCurrentUrl();
        if (url == null || !url.contains(Bsd29967Page.FOCUS_URL_FRAGMENT)) {
            return;
        }
        Matcher matcher = FOCUS_ORDER_PATTERN.matcher(url);
        if (matcher.find()) {
            Bsd29967SessionHelper.setOrderContext(matcher.group(1), matcher.group(2));
            Logger.logReportMessage("Parsed order from URL: id=" + matcher.group(1)
                    + ", env=" + matcher.group(2));
        }
    }

    public void parseOrderFromCurrentUrl(SoftAssert softAssert) {
        String url = driver.get().browser().getCurrentUrl();
        Verify.softAssert1(url != null && url.contains(Bsd29967Page.FOCUS_URL_FRAGMENT),
                "Details panel URL contains focus order fragment (actual=" + url + ")", softAssert);
        if (url == null) {
            return;
        }
        Matcher matcher = FOCUS_ORDER_PATTERN.matcher(url);
        if (matcher.find()) {
            String orderId = matcher.group(1);
            String envSlug = matcher.group(2);
            Bsd29967SessionHelper.setOrderContext(orderId, envSlug);
            Logger.logReportMessage("Parsed order from URL: id=" + orderId + ", env=" + envSlug);
        } else {
            Verify.softAssert1(false, "Could not parse order id/env from URL: " + url, softAssert);
        }
    }

    private void scrollToIdentifiersSection() {
        try {
            if (WaitUtil.isDisplayFast(bsd29967Page.detailsPanelIdentifiersSection(), 3)) {
                DriverUtil.scrollToElement(bsd29967Page.detailsPanelIdentifiersSection());
            }
            driver.get().browser().executeScript(
                    "(function() {"
                            + "var labels = document.querySelectorAll('.section-label, div.section-label');"
                            + "for (var i = 0; i < labels.length; i++) {"
                            + "  if ((labels[i].textContent || '').trim() !== 'Identifiers') continue;"
                            + "  labels[i].scrollIntoView({block: 'center'});"
                            + "  return true;"
                            + "}"
                            + "return false;"
                            + "})()");
            Thread.sleep(400);
        } catch (Exception e) {
            Logger.logConsoleMessage("Scroll to Identifiers section failed: " + e.getMessage());
        }
    }

    private void waitForDetailsPanelDsidsIfNeeded() throws InterruptedException {
        WaitUtil.isDisplayFast(bsd29967Page.detailsPanelIdentifiersSection(), 5);
        if (hasDetailsPanelDsidsLoaded()) {
            Logger.logReportMessage("Details panel DSIDs already loaded — skipping wait");
            return;
        }
        Logger.logReportMessage("Details panel DSIDs not loaded — waiting up to "
                + DETAILS_PANEL_DSID_MAX_WAIT_S + "s");
        long end = System.currentTimeMillis() + (DETAILS_PANEL_DSID_MAX_WAIT_S * 1000L);
        while (System.currentTimeMillis() < end) {
            if (!readDsIdsViaJs().isEmpty()) {
                return;
            }
            Thread.sleep(500);
        }
    }

    private boolean hasDetailsPanelDsidsLoaded() {
        List<String> values = readDsIdsViaJs();
        if (!values.isEmpty()) {
            return true;
        }
        return !readDsIdsViaLocator().isEmpty();
    }

    private List<String> readIdentifiersSectionDsids(SoftAssert softAssert) {
        List<String> values = readDsIdsViaLocator();
        if (values.isEmpty()) {
            values = readDsIdsViaJs();
        }
        Verify.softAssert1(!values.isEmpty(),
                "Details panel Identifiers section contains at least one DSID (span.dsIds-value)", softAssert);
        Logger.logReportMessage("Details panel DSID values (" + values.size() + "): " + values);
        return values;
    }

    private List<String> readDsIdsViaLocator() {
        List<String> values = new ArrayList<>();
        try {
            for (DesktopBrowserElement el : driver.get().finder()
                    .findElements(bsd29967Page.detailsPanelDsIdsValues())) {
                String text = el.getText().trim();
                if (isPopulatedDsid(text)) {
                    values.add(text);
                }
            }
        } catch (Exception e) {
            Logger.logConsoleMessage("Details panel DSID read via locator failed: " + e.getMessage());
        }
        return values;
    }

    @SuppressWarnings("unchecked")
    private List<String> readDsIdsViaJs() {
        List<String> values = new ArrayList<>();
        try {
            Object result = driver.get().browser().executeScript(
                    "(function() {"
                            + "var values = [];"
                            + "function pushVal(t) {"
                            + "  t = (t || '').replace(/\\s+/g, ' ').trim().replace(/^\"+|\"+$/g, '');"
                            + "  if (t && t !== '-') values.push(t);"
                            + "}"
                            + "function identifiersSection() {"
                            + "  var labels = document.querySelectorAll('.section-label, div.section-label');"
                            + "  for (var i = 0; i < labels.length; i++) {"
                            + "    if ((labels[i].textContent || '').trim() !== 'Identifiers') continue;"
                            + "    var section = labels[i].nextElementSibling;"
                            + "    if (section && section.className && section.className.indexOf('section-content') >= 0) {"
                            + "      return section;"
                            + "    }"
                            + "    return labels[i].parentElement;"
                            + "  }"
                            + "  return null;"
                            + "}"
                            + "var identifiers = identifiersSection();"
                            + "if (!identifiers) return values;"
                            + "var dsidLabels = identifiers.querySelectorAll('.label, div.label');"
                            + "for (var i = 0; i < dsidLabels.length; i++) {"
                            + "  if ((dsidLabels[i].textContent || '').trim().toUpperCase() !== 'DSID') continue;"
                            + "  var root = dsidLabels[i].parentElement;"
                            + "  if (!root) continue;"
                            + "  var dsidSpans = root.querySelectorAll('span.dsIds-value, span[class*=\"dsIds-value\"]');"
                            + "  for (var j = 0; j < dsidSpans.length; j++) {"
                            + "    pushVal(dsidSpans[j].innerText || dsidSpans[j].textContent);"
                            + "  }"
                            + "}"
                            + "return values;"
                            + "})()");
            if (result instanceof List) {
                for (Object item : (List<?>) result) {
                    if (item != null) {
                        String text = String.valueOf(item).trim();
                        if (isPopulatedDsid(text)) {
                            values.add(text);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logger.logConsoleMessage("Details panel DSID JS read failed: " + e.getMessage());
        }
        return values;
    }

    private static boolean isPopulatedDsid(String text) {
        return text != null && !text.trim().isEmpty() && !"-".equals(text.trim());
    }

    public static List<String> normalizeDsidList(List<String> values) {
        List<String> normalized = new ArrayList<>();
        if (values == null) {
            return normalized;
        }
        for (String value : values) {
            if (value == null) {
                continue;
            }
            String trimmed = value.trim();
            if (!trimmed.isEmpty() && !"-".equals(trimmed)) {
                normalized.add(trimmed.toUpperCase(Locale.ROOT));
            }
        }
        normalized.sort(String::compareTo);
        List<String> unique = new ArrayList<>();
        String previous = null;
        for (String item : normalized) {
            if (!item.equals(previous)) {
                unique.add(item);
                previous = item;
            }
        }
        return unique;
    }

    public static boolean hasPopulatedDsids(List<String> dsids) {
        return !normalizeDsidList(dsids).isEmpty();
    }

    public static boolean dsidListsMatch(List<String> fcDsids, List<String> opsDsids) {
        List<String> a = normalizeDsidList(fcDsids);
        List<String> b = normalizeDsidList(opsDsids);
        if (a.isEmpty() || b.isEmpty()) {
            return false;
        }
        return a.equals(b);
    }
}
