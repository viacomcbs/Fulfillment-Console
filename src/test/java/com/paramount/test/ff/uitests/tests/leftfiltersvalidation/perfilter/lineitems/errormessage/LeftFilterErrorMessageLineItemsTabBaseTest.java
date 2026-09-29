package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.lineitems.errormessage;

import com.paramount.test.ff.uitests.helpers.managecolumns.ManageColumnOptions;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.lineitems.LeftFilterLineItemsTabBaseTest;

/** Line Items tab Error message filter tests — ensures {@link ManageColumnOptions#ERROR_MESSAGE} is on the grid. */
public abstract class LeftFilterErrorMessageLineItemsTabBaseTest extends LeftFilterLineItemsTabBaseTest {

    @Override
    protected String manageColumnsColumnToEnable() {
        return ManageColumnOptions.ERROR_MESSAGE;
    }
}
