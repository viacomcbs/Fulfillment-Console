package com.paramount.test.ff.uitests.tests.bsd30034;

import com.paramount.test.ff.uitests.helpers.GridExport_util.FirstRowExportResult;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

public class TC_BSD_30034_017_ExportData_DeliveryDate extends BSD30034ExportBaseTest {

    @Test(priority = 17)
    @Description("BSD-30034-017: Export Excel Delivery date data matches UI first row")
    public void validateExportDeliveryDateDataMatchesUi() throws InterruptedException {
        initSoftAssert("BSD-30034-017_ExportData_DeliveryDate");
        FirstRowExportResult result = prepareDoneFilterAndExportFirstRow();
        gridExportUtil.verifyExportDeliveryDateData(result);
        softAssert.assertAll();
    }
}