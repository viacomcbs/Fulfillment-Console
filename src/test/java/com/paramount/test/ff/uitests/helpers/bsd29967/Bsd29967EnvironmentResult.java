package com.paramount.test.ff.uitests.helpers.bsd29967;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Per-Environment workflow outcome for BSD-29967 multi-env loop. */
public final class Bsd29967EnvironmentResult {

    public static final String STATUS_PASS = "PASS";
    public static final String STATUS_FAIL = "FAIL";
    public static final String STATUS_SKIP = "SKIP";

    private final String environment;
    private final String status;
    private final String orderId;
    private final List<String> fcDsids;
    private final List<String> opsDsids;
    private final String message;
    private final String opsExtractionSource;
    private final String opsExtractionDiagnostics;

    public Bsd29967EnvironmentResult(String environment, String status, String orderId,
            List<String> fcDsids, List<String> opsDsids, String message) {
        this(environment, status, orderId, fcDsids, opsDsids, message, "", "");
    }

    public Bsd29967EnvironmentResult(String environment, String status, String orderId,
            List<String> fcDsids, List<String> opsDsids, String message,
            String opsExtractionSource, String opsExtractionDiagnostics) {
        this.environment = environment == null ? "" : environment.trim();
        this.status = status == null ? "" : status.trim();
        this.orderId = orderId == null ? "" : orderId.trim();
        this.fcDsids = fcDsids == null ? Collections.emptyList() : new ArrayList<>(fcDsids);
        this.opsDsids = opsDsids == null ? Collections.emptyList() : new ArrayList<>(opsDsids);
        this.message = message == null ? "" : message.trim();
        this.opsExtractionSource = opsExtractionSource == null ? "" : opsExtractionSource.trim();
        this.opsExtractionDiagnostics = opsExtractionDiagnostics == null ? "" : opsExtractionDiagnostics.trim();
    }

    public String getEnvironment() {
        return environment;
    }

    public String getStatus() {
        return status;
    }

    public String getOrderId() {
        return orderId;
    }

    public List<String> getFcDsids() {
        return Collections.unmodifiableList(fcDsids);
    }

    public List<String> getOpsDsids() {
        return Collections.unmodifiableList(opsDsids);
    }

    public String getMessage() {
        return message;
    }

    public String getOpsExtractionSource() {
        return opsExtractionSource;
    }

    public String getOpsExtractionDiagnostics() {
        return opsExtractionDiagnostics;
    }

    public boolean isMatch() {
        return Bsd29967DetailsUtil.dsidListsMatch(fcDsids, opsDsids);
    }

    /** True when FC Details panel has at least one DSID — required for email/Jira table rows. */
    public boolean hasPopulatedFcDsids() {
        return Bsd29967DetailsUtil.hasPopulatedDsids(fcDsids);
    }
}
