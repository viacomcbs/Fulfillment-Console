package com.paramount.test.ff.common.listeners;

import java.util.List;

import org.testng.IAlterSuiteListener;
import org.testng.xml.XmlSuite;

import com.paramount.test.ff.common.util.Config;

/**
 * Loads suite XML parameters into {@link Config} before any test listener static
 * initializers read {@code ConfigProps} (IDE runs do not set {@code suiteXmlFile}).
 */
public class SuiteConfigAlterListener implements IAlterSuiteListener {

	@Override
	public void alter(List<XmlSuite> suites) {
		if (suites == null || suites.isEmpty()) {
			return;
		}
		XmlSuite suite = suites.get(0);
		String suiteFile = suite.getFileName();
		if (suiteFile != null && !suiteFile.trim().isEmpty()) {
			System.setProperty("suiteXmlFile", suiteFile.trim());
		}
		Config.bootstrapFromSuite(suite);
	}

}
