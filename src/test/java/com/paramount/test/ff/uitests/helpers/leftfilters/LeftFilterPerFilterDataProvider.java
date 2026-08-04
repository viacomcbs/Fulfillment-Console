package com.paramount.test.ff.uitests.helpers.leftfilters;

import org.testng.annotations.DataProvider;

import java.util.ArrayList;
import java.util.List;

public final class LeftFilterPerFilterDataProvider {

    private LeftFilterPerFilterDataProvider() {
    }

    @DataProvider(name = "ordersPerFilterMatrix")
    public static Object[][] ordersPerFilterMatrix() {
        return buildMatrix(ConsoleTab.ORDERS, OrdersLeftFilter.allDisplayNames());
    }

    @DataProvider(name = "lineItemsPerFilterMatrix")
    public static Object[][] lineItemsPerFilterMatrix() {
        return buildMatrix(ConsoleTab.LINE_ITEMS, LineItemsLeftFilter.allDisplayNames());
    }

    private static Object[][] buildMatrix(ConsoleTab tab, List<String> filterNames) {
        List<Object[]> rows = new ArrayList<>();
        for (int i = 0; i < filterNames.size(); i++) {
            String filterName = filterNames.get(i);
            int filterIndex = i + 1;
            for (LeftFilterTestCategory category : LeftFilterTestCategory.values()) {
                if (LeftFilterPerFilterConfig.isCategoryApplicable(tab, filterName, category)) {
                    rows.add(new Object[]{filterIndex, filterName, category});
                }
            }
        }
        return rows.toArray(new Object[0][]);
    }
}
