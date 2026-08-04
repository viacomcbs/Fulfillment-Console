package com.paramount.test.ff.uitests.support;

import org.testng.annotations.DataProvider;

import com.paramount.test.ff.common.util.props.BrowserType;
import com.paramount.test.ff.common.util.props.DesktopOSType;
import com.paramount.test.ff.common.util.props.IProps.ConfigProps;



public class DataProviderManager {

	@DataProvider()
	static public Object[][] allBrowsersDataProvider() {
		if (ConfigProps.RUN_AS_FACTORY) {
			return new Object[][] {
				
				new Object[]{DesktopOSType.MQE_WINDOWS.value() + "::" + BrowserType.CHROME.value() + "::latest"},
				new Object[]{DesktopOSType.MQE_WINDOWS.value() + "::" + BrowserType.FIREFOX.value() + "::latest"},
				new Object[]{DesktopOSType.MQE_WINDOWS.value() + "::" + BrowserType.IEXPLORE.value() + "::latest"},
				new Object[]{DesktopOSType.MQE_MAC.value() + "::" + BrowserType.CHROME.value() + "::latest"}
			};
		} 	else {
			return new Object[][] {
				new Object[] { ConfigProps.OS + ConfigProps.BROWSER }
			};
		}
	}

	@DataProvider()
    static public Object[][] latestBrowsersDataProvider() {
		if (ConfigProps.RUN_AS_FACTORY) {
            return new Object[][] {
            	//new Object[]{DesktopOSType.MQE_MAC.value() + "::" + BrowserType.CHROME.value() + "::latest"},
            	new Object[]{DesktopOSType.MQE_WINDOWS.value() + "::" + ConfigProps.BROWSER + "::latest"}
            };
		} else {
			return new Object[][] {
	            new Object[] { ConfigProps.OS + ConfigProps.BROWSER }
		    };
		}
    }
	
	@DataProvider()
    static public Object[][] latestIEDataProvider() {
        if (ConfigProps.RUN_AS_FACTORY) {
            return new Object[][] {
            	new Object[]{DesktopOSType.MQE_WINDOWS.value() + "::" + BrowserType.IEXPLORE.value() + "::latest"}
            };
        } else {
            return new Object[][] {
                    new Object[] { ConfigProps.OS + ConfigProps.BROWSER }
            };
        }
    }
	
}
