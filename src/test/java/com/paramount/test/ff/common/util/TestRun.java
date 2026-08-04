package com.paramount.test.ff.common.util;

import com.paramount.test.ff.common.util.props.BrowserType;
import com.paramount.test.ff.common.util.props.DesktopOSType;
import com.paramount.test.ff.common.util.props.IProps.ConfigProps;

public class TestRun {
	
	private static final ThreadLocal<DesktopOSType> osType = new ThreadLocal<DesktopOSType>();
    private static final ThreadLocal<BrowserType> browser = new ThreadLocal<BrowserType>();
    private static final ThreadLocal<String> version = new ThreadLocal<String>();
    private static final ThreadLocal<Long> bandwidth = new ThreadLocal<Long>();
    private static final ThreadLocal<Integer> latency = new ThreadLocal<Integer>();
    private static final ThreadLocal<Boolean> authError = new ThreadLocal<Boolean>();

	public static synchronized void init(String runParams) {
		authError.set(false);
    	if (runParams != null) {
    		// set the browser
    		if (TestUtil.isLabExecution()) 
    		{
    			browser.set(BrowserType.getEnumByString(runParams.split("::")[1]));
    			Logger.logMessage("Browser Is: "+ browser.get());
    		} else 
    		{
    			setBrowser(BrowserType.valueOf(ConfigProps.BROWSER.toUpperCase()));
    		}

    		// set the os type
    		if (TestUtil.isLabExecution()) {
    			osType.set(DesktopOSType.getEnumByString(runParams.split("::")[0]));
    			Logger.logMessage("OS Is: "+ osType.get());
    		} else 
    		{
    			osType.set(DesktopOSType.valueOf(ConfigProps.OS.toUpperCase()));
    		}
    					            
    		// set the browser version
    		if (TestUtil.isLabExecution()) 
    		{
    			version.set(runParams.split("::")[2]);
    		}
    	}
    }

	public static synchronized void setOS(DesktopOSType os) {
        TestRun.osType.set(os);
    }
    
    public static synchronized DesktopOSType getOS() {
        return osType.get();
    }

    public static synchronized void setBrowser(BrowserType browser) {
        TestRun.browser.set(browser);
    }
    
    public static synchronized BrowserType getBrowser() {
    	return browser.get(); 
    }
    
    public static synchronized void setNetworkBandwidth(Long bandwidthBytes) {
        TestRun.bandwidth.set(bandwidthBytes);
    }
    
    public static synchronized Long getNetworkBandwidth() {
    	return bandwidth.get(); 
    }
    
    public static synchronized void setNetworkLatency(Integer latencyInMS) {
        TestRun.latency.set(latencyInMS);
    }
    
    public static synchronized Integer getNetworkLatency() {
    	return latency.get(); 
    }
    
    public static synchronized String getVersion() {
    	return version.get(); 
    }
    
    public static Boolean isWindows() {
		return getOS().value().toLowerCase().contains("windows");
	}

	public static Boolean isMac() {
		return getOS().value().toLowerCase().contains("mac");
	}
    
    public static synchronized Boolean isChrome() {
    	return getBrowser().equals(BrowserType.CHROME);
    }
    
    public static synchronized Boolean isSafari() {
    	return getBrowser().equals(BrowserType.SAFARI);
    }
    
    public static synchronized Boolean isEdge() {
    	return getBrowser().equals(BrowserType.MICROSOFTEDGE);
    }
    
    public static synchronized Boolean isIExplore() {
    	return getBrowser().equals(BrowserType.IEXPLORE);
    }
    
    public static synchronized Boolean isFirefox() {
    	return getBrowser().equals(BrowserType.FIREFOX);
    }
    
    public static void setAuthError() {
		authError.set(true);
	}
    
    public static Boolean isLabExecution() {
		String ec2Subnet = System.getenv("EC2_SUBNET");
		return ec2Subnet != null;
	}

}