package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.lineitems.deliveryprotocol;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LineItemsLeftFilter;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.lineitems.LeftFilterLineItemsTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC119 — DeliveryProtocol Basic. */
public class LF_LI_TC119_DeliveryProtocol_BasicTest extends LeftFilterLineItemsTabBaseTest {

    private static final String FILTER = LineItemsLeftFilter.DELIVERY_PROTOCOL.getDisplayName();
    private static final int FILTER_INDEX = 19;

    @Test(priority = 1)
    @Description("TC119: DeliveryProtocol — Basic")
    public void tc119_deliveryProtocolBasic() throws InterruptedException {
        softAssert = new SoftAssert("tc119_deliveryProtocolBasic", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.LINE_ITEMS);
        leftFilterPanelUtil.validateBasicSmoke(softAssert, FILTER);
        softAssert.assertAll();
    }
}
