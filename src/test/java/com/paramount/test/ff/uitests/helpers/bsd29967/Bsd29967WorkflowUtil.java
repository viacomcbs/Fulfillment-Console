package com.paramount.test.ff.uitests.helpers.bsd29967;

import com.paramount.test.ff.common.loginUtil.Verify;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPanelUtil;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterOptionRotationUtil;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Loops round-robin 3 non-zero + mandatory UWFFSP Environment workflows; max 2 orders per workflow when DSIDs blank. */
public class Bsd29967WorkflowUtil {

    static final String MANDATORY_ENVIRONMENT = "UWFFSP";
    private static final int MAX_NON_ZERO_ENV_WORKFLOWS = 3;
    private static final int MAX_ORDERS_PER_WORKFLOW = 2;

    public void validateAllNonZeroEnvironmentWorkflows(LeftFilterPanelUtil filterUtil,
            Bsd29967ColumnUtil columnUtil,
            Bsd29967DetailsUtil detailsUtil,
            Bsd29967OpsConsoleUtil opsConsoleUtil,
            SoftAssert softAssert) throws InterruptedException {
        Bsd29967SessionHelper.clearEnvironmentResults();
        String filterName = OrdersLeftFilter.ENVIRONMENT.getDisplayName();
        List<String> allNonZero = filterUtil.resolveAllNonZeroFilterOptions(filterName);
        Verify.softAssert1(!allNonZero.isEmpty(),
                filterName + " has at least one non-zero option", softAssert);
        if (allNonZero.isEmpty()) {
            return;
        }

        List<String> environments = selectEnvironmentWorkflows(allNonZero);
        Bsd29967SessionHelper.setExpectedEnvironmentCount(environments.size());
        Logger.logReportMessage("BSD-29967 — validating " + environments.size()
                + " Environment workflow(s) (round-robin " + MAX_NON_ZERO_ENV_WORKFLOWS
                + " non-zero + mandatory " + MANDATORY_ENVIRONMENT + "): " + environments
                + " | available non-zero: " + allNonZero);

        for (String environment : environments) {
            try {
                validateEnvironmentWorkflow(filterUtil, columnUtil, detailsUtil, opsConsoleUtil,
                        environment, softAssert);
            } catch (Exception e) {
                handleWorkflowInfrastructureFailure(filterUtil, columnUtil, environment, e);
            }
        }

        int passed = Bsd29967SessionHelper.getPassedEnvironmentCount();
        int skipped = Bsd29967SessionHelper.getSkippedEnvironmentCount();
        Logger.logReportMessage("BSD-29967 environment summary — PASS=" + passed
                + ", FAIL=" + Bsd29967SessionHelper.getFailedEnvironmentCount()
                + ", SKIP=" + skipped + ", TOTAL=" + environments.size());
        Verify.softAssert1(Bsd29967SessionHelper.getFailedEnvironmentCount() == 0,
                "No Environment workflow DSID mismatches (FAIL="
                        + Bsd29967SessionHelper.getFailedEnvironmentCount() + ")", softAssert);
        Verify.softAssert1(passed > 0,
                "At least one Environment validated (PASS=" + passed + ", SKIP=" + skipped + ")", softAssert);
    }

    private void handleWorkflowInfrastructureFailure(LeftFilterPanelUtil filterUtil,
            Bsd29967ColumnUtil columnUtil,
            String environment,
            Exception error) throws InterruptedException {
        if (!isInfrastructureFailure(error)) {
            if (error instanceof InterruptedException) {
                throw (InterruptedException) error;
            }
            if (error instanceof RuntimeException) {
                throw (RuntimeException) error;
            }
            throw new RuntimeException(error);
        }
        String skipMessage = "Synergy session unavailable — skipped ("
                + summarizeInfrastructureCause(error) + ")";
        Logger.logReportMessage("BSD-29967 SKIP " + environment + " — " + skipMessage);
        Bsd29967SessionHelper.recordEnvironmentResult(new Bsd29967EnvironmentResult(
                environment, Bsd29967EnvironmentResult.STATUS_SKIP, "",
                Collections.emptyList(), Collections.emptyList(), skipMessage));
        try {
            columnUtil.returnToOrdersGrid(filterUtil);
            columnUtil.clearEnvironmentFilterForNextIteration(filterUtil);
        } catch (Exception cleanupError) {
            Logger.logConsoleMessage("BSD-29967 cleanup after infrastructure failure skipped: "
                    + cleanupError.getMessage());
        }
    }

    static boolean isInfrastructureFailure(Throwable error) {
        Throwable current = error;
        while (current != null) {
            if (current instanceof java.net.SocketException
                    || current instanceof java.net.UnknownHostException) {
                return true;
            }
            String typeName = current.getClass().getName();
            if (typeName.contains("ServerFailureException")) {
                return true;
            }
            String message = current.getMessage();
            if (message != null) {
                String lower = message.toLowerCase();
                if (lower.contains("server failed to respond")
                        || lower.contains("connection reset")
                        || lower.contains("synergyserver")
                        || lower.contains("no such host is known")
                        || lower.contains("max test time limit")
                        || lower.contains("test time limit per session")) {
                    return true;
                }
            }
            current = current.getCause();
        }
        return false;
    }

    private static String summarizeInfrastructureCause(Throwable error) {
        Throwable root = error;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        String message = root.getMessage();
        if (message == null || message.trim().isEmpty()) {
            return root.getClass().getSimpleName();
        }
        if (message.length() > 120) {
            return message.substring(0, 117) + "...";
        }
        return message;
    }

    static List<String> selectEnvironmentWorkflows(List<String> allNonZero) {
        LeftFilterOptionRotationUtil.RotationResult rotation =
                LeftFilterOptionRotationUtil.pickNext(
                        OrdersLeftFilter.ENVIRONMENT.getDisplayName(),
                        allNonZero,
                        MAX_NON_ZERO_ENV_WORKFLOWS);
        List<String> selected = new ArrayList<>(rotation.getSelected());
        Bsd29967EmailReport.setWorkflowSelectionSummary(rotation.getSummary());
        if (!selected.contains(MANDATORY_ENVIRONMENT) && allNonZero.contains(MANDATORY_ENVIRONMENT)) {
            selected.add(MANDATORY_ENVIRONMENT);
        }
        return selected;
    }

    private void validateEnvironmentWorkflow(LeftFilterPanelUtil filterUtil,
            Bsd29967ColumnUtil columnUtil,
            Bsd29967DetailsUtil detailsUtil,
            Bsd29967OpsConsoleUtil opsConsoleUtil,
            String environment,
            SoftAssert softAssert) throws InterruptedException {
        Logger.logReportMessage("BSD-29967 — Environment workflow: " + environment);
        Bsd29967SessionHelper.resetWorkflowIterationState();
        columnUtil.applyEnvironmentFilter(filterUtil, environment, softAssert);

        if (!columnUtil.hasMultiSubmissionIconInFirstVisibleRows()) {
            String skipMessage = "No (i) icon in first visible Orders rows — skipped";
            Logger.logReportMessage("BSD-29967 SKIP " + environment + " — " + skipMessage);
            Bsd29967SessionHelper.recordEnvironmentResult(new Bsd29967EnvironmentResult(
                    environment, Bsd29967EnvironmentResult.STATUS_SKIP, "",
                    Collections.emptyList(), Collections.emptyList(), skipMessage));
            columnUtil.clearEnvironmentFilterForNextIteration(filterUtil);
            return;
        }

        int iconCount = columnUtil.countMultiSubmissionIconsInGrid();
        int maxAttempts = Math.min(Math.max(iconCount, 1), MAX_ORDERS_PER_WORKFLOW);
        List<String> fcDsids = Collections.emptyList();
        int chosenIconIndex = -1;

        for (int iconIdx = 1; iconIdx <= maxAttempts; iconIdx++) {
            if (!columnUtil.openDetailsForMultiSubmissionIconIndex(iconIdx, softAssert)) {
                Logger.logReportMessage("BSD-29967 — (i) row " + iconIdx
                        + " not available in " + environment + ", stopping order attempts");
                break;
            }
            fcDsids = detailsUtil.tryReadAndStoreDetailsPanelDsids();
            if (!fcDsids.isEmpty()) {
                chosenIconIndex = iconIdx;
                Logger.logReportMessage("BSD-29967 — order attempt " + iconIdx
                        + " DSIDs loaded for " + environment);
                break;
            }
            String orderId = Bsd29967SessionHelper.getOrderId();
            Logger.logReportMessage("BSD-29967 SKIP order attempt " + iconIdx
                    + " (order=" + orderId + ") — DSIDs blank, trying next (i) row in "
                    + environment + " (max " + MAX_ORDERS_PER_WORKFLOW + " attempts)");
            columnUtil.returnToOrdersGrid(filterUtil);
            Thread.sleep(500);
        }

        if (fcDsids.isEmpty()) {
            String skipMessage = "No order with populated DSIDs after " + maxAttempts + " (i) row attempt(s)";
            Logger.logReportMessage("BSD-29967 SKIP " + environment + " — " + skipMessage);
            Bsd29967SessionHelper.recordEnvironmentResult(new Bsd29967EnvironmentResult(
                    environment, Bsd29967EnvironmentResult.STATUS_SKIP, "",
                    Collections.emptyList(), Collections.emptyList(), skipMessage));
            columnUtil.clearEnvironmentFilterForNextIteration(filterUtil);
            return;
        }

        Verify.softAssert1(chosenIconIndex > 0,
                environment + ": FC Details DSIDs populated on (i) row " + chosenIconIndex, softAssert);

        List<String> opsDsids = opsConsoleUtil.fetchLatestJobDsIdList(softAssert);
        if (opsDsids.isEmpty() && Bsd29967SessionHelper.getOpsExtractionDiagnostics().isEmpty()) {
            Bsd29967SessionHelper.setOpsExtractionDiagnostics("ops-console returned empty dsIdList");
        }
        boolean match = Bsd29967DetailsUtil.dsidListsMatch(fcDsids, opsDsids);
        String orderId = Bsd29967SessionHelper.getOrderId();
        Logger.logReportMessage("BSD-29967 compare — Environment=" + environment
                + ", Order=" + orderId + ", FC=" + fcDsids + ", Ops=" + opsDsids
                + ", Match=" + (match ? "Y" : "N"));

        String status = match ? Bsd29967EnvironmentResult.STATUS_PASS : Bsd29967EnvironmentResult.STATUS_FAIL;
        String message = match ? "FC Details DSIDs match ops-console dsIdList"
                : "FC Details DSIDs mismatch ops-console dsIdList";
        Bsd29967SessionHelper.recordEnvironmentResult(new Bsd29967EnvironmentResult(
                environment, status, orderId, fcDsids, opsDsids, message,
                Bsd29967SessionHelper.getOpsExtractionSource(),
                Bsd29967SessionHelper.getOpsExtractionDiagnostics()));
        Verify.softAssert1(match,
                environment + ": FC Details DSIDs match ops-console latest job dsIdList (FC="
                        + fcDsids + ", Ops=" + opsDsids + ")",
                softAssert);

        columnUtil.returnToOrdersGrid(filterUtil);
        columnUtil.clearEnvironmentFilterForNextIteration(filterUtil);
    }
}
