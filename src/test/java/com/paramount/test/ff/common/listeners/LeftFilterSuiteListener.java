package com.paramount.test.ff.common.listeners;

import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterSessionHelper;
import org.testng.ISuite;
import org.testng.ISuiteListener;

/**
 * Stops the shared WebDriver once after all left-filter validation tests finish.
 */
public class LeftFilterSuiteListener implements ISuiteListener {

    @Override
    public void onStart(ISuite suite) {
        LeftFilterSessionHelper.enableSharedSession();
    }

    @Override
    public void onFinish(ISuite suite) {
        if (LeftFilterSessionHelper.isSharedSessionEnabled()) {
            LeftFilterSessionHelper.stopSharedDriver();
        }
    }
}
