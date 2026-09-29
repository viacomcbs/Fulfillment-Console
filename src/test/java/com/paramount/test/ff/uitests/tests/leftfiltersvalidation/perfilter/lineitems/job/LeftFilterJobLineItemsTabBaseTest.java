package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.lineitems.job;

import com.paramount.test.ff.uitests.helpers.managecolumns.ManageColumnOptions;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.lineitems.LeftFilterLineItemsTabBaseTest;

/** Line Items tab base for Job Type left-filter tests — enables Type in Manage columns once per session. */
public abstract class LeftFilterJobLineItemsTabBaseTest extends LeftFilterLineItemsTabBaseTest {

    @Override
    protected String manageColumnsColumnToEnable() {
        return ManageColumnOptions.JOB;
    }
}
