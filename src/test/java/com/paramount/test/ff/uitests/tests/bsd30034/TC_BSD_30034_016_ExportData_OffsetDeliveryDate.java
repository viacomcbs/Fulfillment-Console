package com.paramount.test.ff.uitests.tests.bsd30034;

import com.paramount.test.ff.uitests.helpers.GridExport_util.FirstRowExportResult;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

public class TC_BSD_30034_016_ExportData_OffsetDeliveryDate extends BSD30034ExportBaseTest {

    @Test(priority = 16)
    @Description("BSD-30034-016: Export Excel Offset Delivery date data matches UI first row")
    public void validateExportOffsetDeliveryDateDataMatchesUi() throws InterruptedException {
        initSoftAssert("BSD-30034-016_ExportData_OffsetDeliveryDate");
        FirstRowExportResult result = prepareDoneFilterAndExportFirstRow();
        gridExportUtil.verifyExportOffsetDeliveryDateData(result);
        softAssert.assertAll();
    }
}