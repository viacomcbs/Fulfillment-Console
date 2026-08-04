package com.paramount.test.ff.common.util;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.paramount.test.ff.common.util.props.IProps.ConfigProps;

public class TestUtil {

	public static final String serverLabURL = ConfigProps.LAB_URL + "?key=" + ConfigProps.USER_KEY;
	private static final String serverLocalURL = ConfigProps.LAB_URL + "?key=" + ConfigProps.USER_KEY;

	public static Boolean isLabExecution() {
		// String ec2Subnet = System.getenv("EC2_SUBNET");
		// return ec2Subnet != null ? true : false;
		String ci = System.getenv("CI");
		return ci != null;
	}
	
	public static String isLab() {
		String url="";
		if(ConfigProps.ISLOCAL) {
			url =serverLocalURL;
		}else {
			url=serverLabURL;
		}
		return url;
	}

	public static void forceDelete(String filePath) {
		File folder = new File(filePath);
		if (folder.exists()) {
			try {
				FileUtils.forceDelete(folder);
			} catch (IOException e) {
				Logger.logConsoleMessage("Unable to delete folder: " + filePath);
			}
		}
	}

	public static String getServerUrl() {
		// Whitelist/reporting always use the Synergy cloud URL; local browser control is handled
		// separately via SynergyKey.fromSynergyConfigFile() in LocalCapabilityFactory.
		return isLabExecution() ? serverLabURL : serverLocalURL;
	}

	public static String getTestRailDescription() {
		return isLabExecution() ? serverLabURL : serverLocalURL;
	}
}
