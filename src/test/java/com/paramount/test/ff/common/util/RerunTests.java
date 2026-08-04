package com.paramount.test.ff.common.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class RerunTests {
	private ConcurrentMap<String, ConcurrentMap<String, String>> failedTests;
	private String sheetName = "";
	@SuppressWarnings("unused")
	private String className, tcName, testCaseStatus;
	private ConcurrentMap<String, String> testCaseStatusMap = new ConcurrentHashMap<>();
	private final String fileName = String.join(File.separator, System.getProperty("user.dir"), "rerun", "ReRun_TC_");
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
	File file = new File(fileName + sdf.format(new Timestamp(System.currentTimeMillis())) + ".xlsx");

	public void setFailedTests(ConcurrentMap<String, ConcurrentMap<String, String>> failedTests) {
		this.failedTests = failedTests;
	}

	public void prepareTests() {
		// create rerun folder if not created
		File reRunFolder = new File(String.join(File.separator, System.getProperty("user.dir"), "rerun"));
		if (!reRunFolder.exists())
			reRunFolder.mkdir();

		// create screenshot folder if not created
		File reportDirectory = new File(String.join(File.separator, System.getProperty("user.dir"), "screenshots"));
		if (!reportDirectory.exists())
			reportDirectory.mkdir();

	}

	public void createRerunFile() {
		writeTestData(failedTests);
	}

	public void writeTestData(ConcurrentMap<String, ConcurrentMap<String, String>> testCaseMap) {
		int rowCnt;
		@SuppressWarnings("resource")
		Workbook workbook = new XSSFWorkbook();
		Sheet sheet;
		Row row;

		for (Map.Entry<String, ConcurrentMap<String, String>> outerMap : testCaseMap.entrySet()) {

			sheetName = outerMap.getKey().split("-")[0]; // packageName
			className = outerMap.getKey().split("-")[1];
			testCaseStatusMap = outerMap.getValue();

			sheet = workbook.getSheet(sheetName);
			if (sheet == null) {
				rowCnt = 0;
				sheet = workbook.createSheet(sheetName);
				row = sheet.createRow(rowCnt);
				row.createCell(0).setCellValue("Class Name");
				row.createCell(1).setCellValue("TestCase Name");
				row.createCell(2).setCellValue("Test Case Status");
			} else
				rowCnt = sheet.getLastRowNum();

			for (Map.Entry<String, String> innerMap : testCaseStatusMap.entrySet()) {
				row = sheet.createRow(++rowCnt);
				row.createCell(0).setCellValue(className);
				row.createCell(1).setCellValue(innerMap.getKey());
				row.createCell(2).setCellValue(innerMap.getValue());

				synchronized (this) {
					try (FileOutputStream outputStream = new FileOutputStream(file)) {
						workbook.write(outputStream);
					} catch (IOException | EncryptedDocumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}

	}

}
