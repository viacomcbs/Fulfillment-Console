package com.paramount.test.ff.uitests.helpers.bsd29967;

import com.paramount.test.ff.common.util.Config;

/** Ops-console Order Inspector base URLs by FC test environment (BSD-29967). */
public final class OpsConsoleUrlUtil {

    /** DEV and PROD FC runs use the legacy ops-console host. */
    public static final String DEFAULT_ORIGIN = "https://contentplatform.viacom.com";

    /** UAT FC runs use the UAT ops-console host. */
    public static final String UAT_ORIGIN = "https://uat.contentplatform.viacom.com";

    private OpsConsoleUrlUtil() {
    }

    public static String resolveOrigin() {
        String env = Config.getString("TestEnvironment");
        if (env != null && "UAT".equalsIgnoreCase(env.trim())) {
            return UAT_ORIGIN;
        }
        return DEFAULT_ORIGIN;
    }

    public static String orderUrlPrefix() {
        return resolveOrigin() + "/ops-console-api-dev-ui/order/";
    }

    public static String orderUrl(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            return orderUrlPrefix();
        }
        return orderUrlPrefix() + orderId.trim();
    }
}
