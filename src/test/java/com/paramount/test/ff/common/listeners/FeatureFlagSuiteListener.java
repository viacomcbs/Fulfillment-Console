package com.paramount.test.ff.common.listeners;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.helpers.featureflag.FeatureFlagSessionHelper;
import org.testng.ISuite;
import org.testng.ISuiteListener;

public class FeatureFlagSuiteListener implements ISuiteListener {

    @Override
    public void onStart(ISuite suite) {
        FeatureFlagSessionHelper.enableSharedSession();
        Logger.logReportMessage("Feature flag cleanup shared session enabled: " + suite.getName());
    }

    @Override
    public void onFinish(ISuite suite) {
        if (FeatureFlagSessionHelper.isSharedSessionEnabled()) {
            FeatureFlagSessionHelper.stopSharedDriver();
        }
    }
}
