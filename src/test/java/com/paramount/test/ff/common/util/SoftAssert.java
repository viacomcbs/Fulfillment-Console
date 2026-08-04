
package com.paramount.test.ff.common.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.testng.Assert;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.synergy.core.reporting.FileUploader;
import com.paramount.test.ff.common.base.*;
import com.paramount.test.ff.common.util.props.IProps.ConfigProps;

import io.qameta.allure.model.Status;

public class SoftAssert extends org.testng.asserts.SoftAssert {
	private String methodName = "";
	private String className = "";
	private String fullMethodName = "";
	private String fullClassName = "";
	private final boolean isTakeScreen = true;
	private final boolean isMakePDF = true;
	String directoryTime = new DateUtil().getDate();

	public SoftAssert(String methodName) {
		this.fullMethodName = methodName;
		this.methodName = methodName.length() > 20 ? methodName.substring(0, 20) : methodName;
		System.setProperty("ScreenshotDirectory", directoryTime);
	}

	public static void endTest(SoftAssert softAssertion) {
		softAssertion.assertAll();
		softAssertion = null;

		softAssertion = new SoftAssert();
	}

	public SoftAssert(String methodName, String className) {
		this.fullMethodName = methodName;
		this.fullClassName = className;
		this.methodName = methodName.length() > 20 ? methodName.substring(0, 20) : methodName;
		this.className = className.length() > 20 ? className.substring(0, 20) : className;
		System.setProperty("ScreenshotDirectory", directoryTime);
	}

	public SoftAssert() {
		methodName = "Assertion";
		System.setProperty("ScreenshotDirectory", directoryTime);
	}

	public void assertValidate(boolean actual, String message) {

		if (actual) {
			AllureAttachment.log(message, Status.PASSED);
			Logger.logMessage(message+" : PASS");
		} else {
			AllureAttachment.log(message, Status.FAILED);
			Logger.log(message+" : FAIL");
		}

		Assert.assertTrue(actual, message);

	}
	
	public void softassertValidate(boolean actual, String message) {
		org.testng.asserts.SoftAssert soft= new org.testng.asserts.SoftAssert();

		if (actual) {
			AllureAttachment.log(message, Status.PASSED);
			Logger.logMessage(message+" : PASS");
		} else {
			AllureAttachment.log(message, Status.FAILED);
			Logger.log(message+" : FAIL");
		}

		soft.assertTrue(actual, message);

	}

	@Override
	public void assertTrue(boolean condition, String message) {
		String methodName = this.methodName;
		if (!condition) {
			methodName = methodName + ":failed";
		}
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertTrue(condition, message);
	}

	//@Override
	public void assertTrue1(boolean condition, String message) {
		String methodName = this.methodName;
		if (!condition) {
			methodName = methodName + ":failed";
		} else {
			Logger.logMessage(message + " :Pass");
		}
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertTrue(condition, message);
	}
	
	public void assertTrue(boolean condition) {
		String message="";
		String methodName = this.methodName;
		String res = ((condition) ? "PASS" : "FAILED");
		if (!condition) {
			methodName = methodName + ":" + res;
			Logger.log(message+" : FAILED");			
			AllureAttachment.log(message, Status.FAILED);
		} else {
			Logger.logMessage(message + " : " + res);
			AllureAttachment.log(message, Status.PASSED);
			
		}
		//if (isTakeScreen)
			//takeScreenshot(methodName);
		super.assertTrue(condition, message);
	}
	
	@Override
	public void assertEquals(boolean actual, boolean expected) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertEquals(actual, expected);
	}

	@Override
	public void assertEquals(boolean actual, boolean expected, String message) {
		System.out.println(actual + " :: " + expected + " :: " + message);
		Logger.logReportMessage(message);
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertEquals(actual, expected, message);
	}

	@Override
	public void assertEquals(byte actual, byte expected) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertEquals(actual, expected);
	}

	@Override
	public void assertEquals(byte actual, byte expected, String message) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertEquals(actual, expected, message);
	}

	@Override
	public void assertEquals(byte[] actual, byte[] expected) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertEquals(actual, expected);
	}

	@Override
	public void assertEquals(byte[] actual, byte[] expected, String message) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertEquals(actual, expected, message);
	}

	@Override
	public void assertEquals(char actual, char expected) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertEquals(actual, expected);
	}

	@Override
	public void assertEquals(char actual, char expected, String message) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertEquals(actual, expected, message);
	}

	@Override
	public void assertEquals(Collection<?> actual, Collection<?> expected) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertEquals(actual, expected);
	}

	@Override
	public void assertEquals(Collection<?> actual, Collection<?> expected, String message) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertEquals(actual, expected, message);
	}

	@Override
	public void assertEquals(double actual, double expected, double delta) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertEquals(actual, expected, delta);
	}

	@Override
	public void assertEquals(double actual, double expected, double delta, String message) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertEquals(actual, expected, delta, message);
	}

	@Override
	public void assertEquals(float actual, float expected, float delta) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertEquals(actual, expected, delta);
	}

	@Override
	public void assertEquals(float actual, float expected, float delta, String message) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertEquals(actual, expected, delta, message);
	}

	@Override
	public void assertEquals(int actual, int expected) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertEquals(actual, expected);
	}

	@Override
	public void assertEquals(int actual, int expected, String message) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertEquals(actual, expected, message);
	}

	@Override
	public void assertEquals(long actual, long expected) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertEquals(actual, expected);
	}

	@Override
	public void assertEquals(long actual, long expected, String message) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertEquals(actual, expected, message);
	}

	@Override
	public void assertEquals(Map<?, ?> actual, Map<?, ?> expected) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertEquals(actual, expected);
	}

	@Override
	public void assertEquals(Object[] actual, Object[] expected) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertEquals(actual, expected);
	}

	@Override
	public void assertEquals(Object[] actual, Object[] expected, String message) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertEquals(actual, expected, message);
	}

	@Override
	public void assertEquals(Set<?> actual, Set<?> expected) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertEquals(actual, expected);
	}

	@Override
	public void assertEquals(Set<?> actual, Set<?> expected, String message) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertEquals(actual, expected, message);
	}

	@Override
	public void assertEquals(short actual, short expected) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertEquals(actual, expected);
	}

	@Override
	public void assertEquals(short actual, short expected, String message) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertEquals(actual, expected, message);
	}

	@Override
	public void assertEquals(String actual, String expected) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertEquals(actual, expected);
	}

	@Override
	public void assertEquals(String actual, String expected, String message) {

		if (actual.equals(expected)) {
			AllureAttachment.log(message, Status.PASSED);
			Logger.logMessage(message+" : PASS");
		} else {
			AllureAttachment.log(message, Status.FAILED);
			Logger.log(message+" : FAIL");
		}
		
		Assert.assertEquals(actual, expected);
		
	}

	@Override
	public <T> void assertEquals(T actual, T expected) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertEquals(actual, expected);
	}

	@Override
	public <T> void assertEquals(T actual, T expected, String message) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertEquals(actual, expected, message);
	}

	@Override
	public void assertEqualsNoOrder(Object[] actual, Object[] expected) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertEqualsNoOrder(actual, expected);
	}

	@Override
	public void assertEqualsNoOrder(Object[] actual, Object[] expected, String message) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertEqualsNoOrder(actual, expected, message);
	}

	@Override
	public void assertFalse(boolean condition) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertFalse(condition);
	}

	@Override
	public void assertFalse(boolean condition, String message) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertFalse(condition, message);
	}

	@Override
	public void assertNotEquals(double actual, double expected, double delta) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertNotEquals(actual, expected, delta);
	}

	@Override
	public void assertNotEquals(double actual, double expected, double delta, String message) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertNotEquals(actual, expected, delta, message);
	}

	@Override
	public void assertNotEquals(float actual, float expected, float delta) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertNotEquals(actual, expected, delta);
	}

	@Override
	public void assertNotEquals(float actual, float expected, float delta, String message) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertNotEquals(actual, expected, delta, message);
	}

	@Override
	public void assertNotEquals(Object actual, Object expected) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertNotEquals(actual, expected);
	}

	@Override
	public void assertNotEquals(Object actual, Object expected, String message) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertNotEquals(actual, expected, message);
	}

	@Override
	public void assertNotNull(Object object) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertNotNull(object);
	}

	@Override
	public void assertNotNull(Object object, String message) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertNotNull(object, message);
	}

	@Override
	public void assertNotSame(Object actual, Object expected) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertNotSame(actual, expected);
	}

	@Override
	public void assertNotSame(Object actual, Object expected, String message) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertNotSame(actual, expected, message);
	}

	@Override
	public void assertNull(Object object) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertNull(object);
	}

	@Override
	public void assertNull(Object object, String message) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertNull(object, message);
	}

	@Override
	public void assertSame(Object actual, Object expected) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertSame(actual, expected);
	}

	@Override
	public void assertSame(Object actual, Object expected, String message) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertSame(actual, expected, message);
	}

	public void assertTrue(boolean condition, boolean isTakeScreen) {
		String methodName = this.methodName;
		if (!condition) {
			methodName = methodName + ":failed";
		}
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertTrue(condition);
	}

	public void assertTrue(boolean condition, String message, boolean isTakeScreen) {
		String methodName = this.methodName;
		if (!condition) {
			methodName = methodName + ":failed";
		}
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertTrue(condition, message);
	}

	public void assertEquals(boolean actual, boolean expected, boolean isTakeScreen) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertEquals(actual, expected);
	}

	public void assertEquals(boolean actual, boolean expected, String message, boolean isTakeScreen) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertEquals(actual, expected, message);
	}

	public void assertEquals(byte actual, byte expected, boolean isTakeScreen) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertEquals(actual, expected);
	}

	public void assertEquals(byte actual, byte expected, String message, boolean isTakeScreen) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertEquals(actual, expected, message);
	}

	public void assertEquals(byte[] actual, byte[] expected, boolean isTakeScreen) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertEquals(actual, expected);
	}

	public void assertEquals(byte[] actual, byte[] expected, String message, boolean isTakeScreen) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertEquals(actual, expected, message);
	}

	public void assertEquals(char actual, char expected, boolean isTakeScreen) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertEquals(actual, expected);
	}

	public void assertEquals(char actual, char expected, String message, boolean isTakeScreen) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertEquals(actual, expected, message);
	}

	public void assertEquals(Collection<?> actual, Collection<?> expected, boolean isTakeScreen) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertEquals(actual, expected);
	}

	public void assertEquals(Collection<?> actual, Collection<?> expected, String message, boolean isTakeScreen) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertEquals(actual, expected, message);
	}

	public void assertEquals(double actual, double expected, double delta, boolean isTakeScreen) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertEquals(actual, expected, delta);
	}

	public void assertEquals(double actual, double expected, double delta, String message, boolean isTakeScreen) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertEquals(actual, expected, delta, message);
	}

	public void assertEquals(float actual, float expected, float delta, boolean isTakeScreen) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertEquals(actual, expected, delta);
	}

	public void assertEquals(float actual, float expected, float delta, String message, boolean isTakeScreen) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertEquals(actual, expected, delta, message);
	}

	public void assertEquals(int actual, int expected, boolean isTakeScreen) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertEquals(actual, expected);
	}

	public void assertEquals(int actual, int expected, String message, boolean isTakeScreen) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertEquals(actual, expected, message);
	}

	public void assertEquals(long actual, long expected, boolean isTakeScreen) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertEquals(actual, expected);
	}

	public void assertEquals(long actual, long expected, String message, boolean isTakeScreen) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertEquals(actual, expected, message);
	}

	public void assertEquals(Map<?, ?> actual, Map<?, ?> expected, boolean isTakeScreen) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertEquals(actual, expected);
	}

	public void assertEquals(Object[] actual, Object[] expected, boolean isTakeScreen) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertEquals(actual, expected);
	}

	public void assertEquals(Object[] actual, Object[] expected, String message, boolean isTakeScreen) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertEquals(actual, expected, message);
	}

	public void assertEquals(Set<?> actual, Set<?> expected, boolean isTakeScreen) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertEquals(actual, expected);
	}

	public void assertEquals(Set<?> actual, Set<?> expected, String message, boolean isTakeScreen) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertEquals(actual, expected, message);
	}

	public void assertEquals(short actual, short expected, boolean isTakeScreen) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertEquals(actual, expected);
	}

	public void assertEquals(short actual, short expected, String message, boolean isTakeScreen) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertEquals(actual, expected, message);
	}

	public void assertEquals(String actual, String expected, boolean isTakeScreen) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertEquals(actual, expected);
	}

	public void assertEquals(String actual, String expected, String message, boolean isTakeScreen) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertEquals(actual, expected, message);
	}

	public <T> void assertEquals(T actual, T expected, boolean isTakeScreen) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertEquals(actual, expected);
	}

	public <T> void assertEquals(T actual, T expected, String message, boolean isTakeScreen) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertEquals(actual, expected, message);
	}

	public void assertEqualsNoOrder(Object[] actual, Object[] expected, boolean isTakeScreen) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertEqualsNoOrder(actual, expected);
	}

	public void assertEqualsNoOrder(Object[] actual, Object[] expected, String message, boolean isTakeScreen) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertEqualsNoOrder(actual, expected, message);
	}

	public void assertFalse(boolean condition, boolean isTakeScreen) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertFalse(condition);
	}

	public void assertFalse(boolean condition, String message, boolean isTakeScreen) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertFalse(condition, message);
	}

	public void assertNotEquals(double actual, double expected, double delta, boolean isTakeScreen) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertNotEquals(actual, expected, delta);
	}

	public void assertNotEquals(double actual, double expected, double delta, String message, boolean isTakeScreen) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertNotEquals(actual, expected, delta, message);
	}

	public void assertNotEquals(float actual, float expected, float delta, boolean isTakeScreen) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertNotEquals(actual, expected, delta);
	}

	public void assertNotEquals(float actual, float expected, float delta, String message, boolean isTakeScreen) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertNotEquals(actual, expected, delta, message);
	}

	public void assertNotEquals(Object actual, Object expected, boolean isTakeScreen) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertNotEquals(actual, expected);
	}

	public void assertNotEquals(Object actual, Object expected, String message, boolean isTakeScreen) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertNotEquals(actual, expected, message);
	}

	public void assertNotNull(Object object, boolean isTakeScreen) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertNotNull(object);
	}

	public void assertNotNull(Object object, String message, boolean isTakeScreen) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertNotNull(object, message);
	}

	public void assertNotSame(Object actual, Object expected, boolean isTakeScreen) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertNotSame(actual, expected);
	}

	public void assertNotSame(Object actual, Object expected, String message, boolean isTakeScreen) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertNotSame(actual, expected, message);
	}

	public void assertNull(Object object, boolean isTakeScreen) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertNull(object);
	}

	public void assertNull(Object object, String message, boolean isTakeScreen) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertNull(object, message);
	}

	public void assertSame(Object actual, Object expected, boolean isTakeScreen) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertSame(actual, expected);
	}

	public void assertSame(Object actual, Object expected, String message, boolean isTakeScreen) {
		if (isTakeScreen)
			takeScreenshot(methodName);
		super.assertSame(actual, expected, message);
	}

	@Override
	public void assertAll() {
		super.assertAll();
		BaseTest.softAssert = null;
		BaseTest.softAssert = new SoftAssert();
		if (ConfigProps.FAILURE_VIDEO_RECORDING) {
			//AllureAttachment.attachScreenshot();
			//AllureAttachment.attachScreenRecording();
		}
		/*
		 * if (isMakePDF) mergeScreenshotsToPDF();
		 */
	}

	public void uploadFileToServer(String fileName, File pdfFile) throws IOException {
		FileUploader fileUploader = new FileUploader("https://www.synergyserver.tech?key=" + ConfigProps.USER_KEY);
		String url = fileUploader.uploadFile(pdfFile); // the file and // number
		// of days to // retain

		// returns the url to the file
		BaseTest.pdfFilesList.add(fileName + "=" + url);
		System.out.println("URL of stored file: " + url);
	}

	public void downloadFileFromServer(String fileName, String fileUrl) throws IOException {
		FileUploader fileUploader = new FileUploader("https://www.synergyserver.tech?key=" + ConfigProps.USER_KEY);
		File srcFile = fileUploader.downloadFile(fileUrl); // the file and // number
		// of days to // retain

		// store in txt file
		String directoryTime = new DateUtil().getDate();
		String pdfDirectory = new File(System.getProperty("user.dir")).getAbsolutePath() + File.separator
				+ "test-output" + File.separator + "screenshots" + File.separator + directoryTime + File.separator
				+ "pdf";
		File destFile = new File(pdfDirectory + File.separator + fileName);
		try {
			FileUtils.copyFile(srcFile, destFile);
		} catch (IOException e) {
		}
	}

	public String takeScreenshot(String fileType) {
		try {
			Calendar calendar = Calendar.getInstance();
			SimpleDateFormat formater = new SimpleDateFormat("dd_MM_yyyy_hh_mm_ss");
			File scrFile = BaseTest.driver.get().screen().getImage();
			try {
				String reportDirectory = new File(System.getProperty("user.dir")).getAbsolutePath() + File.separator
						+ "test-output" + File.separator + "screenshots" + File.separator + directoryTime
						+ File.separator + "assertion";
				if (className == null || className.length() == 0)
					className = methodName;
				if (className != null)
					reportDirectory = reportDirectory + File.separator + className;
				String fileType1 = fileType.split(":")[0];
				String fileType2 = fileType.contains(":") ? "_" + fileType.split(":")[1] : "";

				System.out.println("Assert screenshot dir>" + reportDirectory);
				File destFile = new File(reportDirectory + File.separator + fileType1 + "_"
						+ formater.format(calendar.getTime()) + fileType2 + ".png");
				FileUtils.copyFile(scrFile, destFile);
				System.out.println("Assert screenshot>>" + destFile.getAbsolutePath());
				return destFile.getAbsolutePath();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (

		Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	// Pending merging for multi test case run
	public void mergeScreenshotsToPDF() {

		String directoryTime = new DateUtil().getDate();
		String reportDirectory = new File(System.getProperty("user.dir")).getAbsolutePath() + File.separator
				+ "test-output" + File.separator + "screenshots" + File.separator + directoryTime + File.separator
				+ "assertion";
		if (className == null || className.length() == 0)
			className = methodName;
		if (fullClassName == null || fullClassName.length() == 0)
			fullClassName = fullMethodName;

		reportDirectory = reportDirectory + File.separator + className;
		try {
			File dir = new File(reportDirectory);

			File[] allFiles = dir.listFiles();

			System.out.println(allFiles.length);

			Document document = new Document();

			// Instantiate the PDF writer

			PdfWriter writer = PdfWriter.getInstance(document,
					new FileOutputStream(new File(reportDirectory + File.separator + fullClassName + ".pdf")));
			writer.open();
			document.open();

			for (File singleFile : allFiles) {
				if (singleFile.isFile() && (singleFile.getAbsolutePath().toLowerCase().contains(".png")
						|| singleFile.getAbsolutePath().toLowerCase().contains(".jpg"))) {

					String filename = singleFile.getAbsolutePath();
					System.out.println(filename);
					// open the pdf for writing

					// process content into image Image im = Image.getInstance(filename);

					// set the size of the image im.scaleToFit(((PageSize.A4.getWidth()) * 9) /
					// 10, PageSize.A4.getHeight());

					// add the captured image to PDF document.add(im);
					document.add(new Paragraph(" "));
				}

			} // close the files and write to local system
			document.close();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		} // copy pdf
		// file to common pdf folder
		File srcFile = new File(reportDirectory + File.separator + fullClassName + ".pdf");
		String pdfDirectory = new File(System.getProperty("user.dir")).getAbsolutePath() + File.separator
				+ "test-output" + File.separator + "screenshots" + File.separator + directoryTime + File.separator
				+ "pdf";
		File destFile = new File(pdfDirectory + File.separator + fullClassName + ".pdf");
		System.out.println("PDF file>>" + destFile.getAbsolutePath());
		try {
			FileUtils.copyFile(srcFile, destFile);
		} catch (IOException e) {
		}

		// upload file to synergy url
		if (ConfigProps.UPLOAD_PDF) {
			try {
				uploadFileToServer(fullClassName + ".pdf", srcFile);
			} catch (

			IOException e) {
			}
		}
	}

	public void writeToFile(String line, File file) throws IOException {
		BufferedWriter output = null;
		try {
			output = new BufferedWriter(new FileWriter(file, true));
			output.write(line);
			output.newLine();
		} catch (IOException e) {
		} finally {
			if (output != null) {
				output.close();
			}
		}
	}

	public List<String> readFromFile(String file) throws IOException {
		List<String> allFiles = new ArrayList<String>();
		try {
			Scanner myReader = new Scanner(new File(file));
			while (myReader.hasNextLine()) {
				String data = myReader.nextLine();
				allFiles.add(data);
			}
			myReader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return allFiles;
	}
}
