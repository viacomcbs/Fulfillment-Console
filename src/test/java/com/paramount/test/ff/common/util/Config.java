package com.paramount.test.ff.common.util;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import java.util.Map;

import org.testng.Assert;
import org.w3c.dom.Document;

public class Config {

	private static final String SYSTEM_TEST_PROP = "system.test.";
	private static final String USER_DIR_PROP = "user.dir";
	// public static String POST_SPLUNK_DATA =
	// System.getenv("EXCLUDED_TEST_GROUPS") != null
	// ? System.getenv("EXCLUDED_TEST_GROUPS") :
	// System.getProperty("excludedGroups");

	public static String getFilePath(String parameterName) {
		String parameterValue = System.getProperty(SYSTEM_TEST_PROP + parameterName.toLowerCase());
		if (parameterValue != null) {
			return System.getProperty(USER_DIR_PROP) + parameterValue.replace("/", File.separator);
		}
		String propFromXML = getXPathValueFromFile(getConfigFileLocation(), getParameterValue(parameterName));
		System.setProperty(SYSTEM_TEST_PROP + parameterName.toLowerCase(), propFromXML);
		return System.getProperty(USER_DIR_PROP) + propFromXML.replace("/", File.separator);
	}

	public static Integer getInt(String parameterName) {
		String parameterValue = System.getProperty(SYSTEM_TEST_PROP + parameterName.toLowerCase());
		if (parameterValue != null) {
			return Integer.parseInt(parameterValue);
		}
		String propFromXML = getXPathValueFromFile(getConfigFileLocation(), getParameterValue(parameterName));
		System.setProperty(SYSTEM_TEST_PROP + parameterName.toLowerCase(), propFromXML);
		return Integer.parseInt(propFromXML);
	}

	public static Boolean getBoolean(String parameterName) {
		String parameterValue = System.getProperty(SYSTEM_TEST_PROP + parameterName.toLowerCase());
		if (parameterValue != null) {
			return Boolean.valueOf(parameterValue);
		}
		String propFromXML = getXPathValueFromFile(getConfigFileLocation(), getParameterValue(parameterName));
		System.setProperty(SYSTEM_TEST_PROP + parameterName.toLowerCase(), propFromXML);
		return Boolean.valueOf(propFromXML);
	}

	public static String getString(String parameterName) {
		String parameterValue = System.getProperty(SYSTEM_TEST_PROP + parameterName.toLowerCase());
		if (parameterValue != null) {
			return parameterValue;
		}
		String propFromXML = getXPathValueFromFile(getConfigFileLocation(), getParameterValue(parameterName));
		System.setProperty(SYSTEM_TEST_PROP + parameterName.toLowerCase(), propFromXML);
		return propFromXML;
	}

	/**
	 * Overrides {@link TestNGSuiteConfig.xml} defaults with parameters from the active TestNG suite XML
	 * (e.g. {@code LocalExecution=true} in {@code LeftFilterPerFilter_LocalSmokeSuite.xml}).
	 */
	public static void applySuiteParameters(Map<String, String> parameters) {
		if (parameters == null || parameters.isEmpty()) {
			return;
		}
		for (Map.Entry<String, String> entry : parameters.entrySet()) {
			if (entry.getKey() != null && entry.getValue() != null) {
				System.setProperty(SYSTEM_TEST_PROP + entry.getKey().toLowerCase(), entry.getValue());
			}
		}
	}

	public static boolean isLocalExecution() {
		return getBoolean("LocalExecution");
	}

	/** Resolved at runtime so active suite XML overrides TestNGSuiteConfig.xml defaults. */
	public static String getTargetUrl() {
		String env = getString("TestEnvironment").toLowerCase();
		if ("dev".equals(env)) {
			return getString("TargetUrlDEV");
		}
		if ("uat".equals(env)) {
			return getString("TargetUrlUAT");
		}
		if ("prod".equals(env)) {
			return getString("TargetUrlPROD");
		}
		if ("pr".equals(env)) {
			return getString("TargetUrlPR");
		}
		return getString("TargetUrlDEV");
	}

	private static String getConfigFileLocation() {
		String fileLoc = System.getProperty(USER_DIR_PROP) + "/src/test/resources/TestNGSuiteConfig.xml";
		return fileLoc.replace("/", File.separator);
	}

	private static String getParameterValue(String parameterName) {
		return "//parameter[@name='" + parameterName + "']/@value";
	}

	private static String getXPathValueFromFile(String fileLocation, String xpathQuery) {
		String value = null;
		try {
			File file = new File(fileLocation);
			DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = xmlFactory.newDocumentBuilder();
			Document xmlDoc = docBuilder.parse(file);
			XPathFactory xpathFact = XPathFactory.newInstance();
			XPath xpath = xpathFact.newXPath();
			value = (String) xpath.evaluate(xpathQuery, xmlDoc, XPathConstants.STRING);
		} catch (Exception e) {
			Assert.fail("Failed to retrieve configuration value from Config File at '" + fileLocation
					+ "' with xpath query '" + xpathQuery + "'.", e);
		}
		return value;
	}

}
