package com.paramount.test.ff.uitests.tests.bsd29174;

public class TC_BSD_29174_011_DeliveryFailed extends StatusCountMatchTest {
    @Override
    protected String statusLabel() {
        return LineItemStatusConstants.DELIVERY_FAILED;
    }
}
