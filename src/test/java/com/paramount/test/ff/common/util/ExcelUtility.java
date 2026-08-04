package com.paramount.test.ff.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


public class ExcelUtility {
	
	public String currentDir = System.getProperty("user.dir");
	String fileName = String.join(File.separator, currentDir, "src", "test", "resources", "TestData", "InputData_");
	String dataKey = "";
	String dataValue = "";
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
	File file = new File(fileName+sdf.format(new Timestamp(System.currentTimeMillis()))+".xlsx" );
	String sheetName = "";
	String fieldName, fieldValue;
	
	
	public ConcurrentMap<String, String> readTestData() {
		ConcurrentMap<String, String> dataMap = new ConcurrentHashMap<>();		
		DataFormatter dataFormatter = new DataFormatter();		

		try (FileInputStream fileInputStream = new FileInputStream(getLatestFile())) {

			@SuppressWarnings("resource")
			Workbook workbook = new XSSFWorkbook(fileInputStream);
			workbook.forEach(sheet -> {
				sheet.forEach(row -> {						
					row.forEach(cell -> {
						if (cell.getColumnIndex() == 0)
							dataKey = sheet.getSheetName()+"_"+dataFormatter.formatCellValue(cell);
						else 
						{
//							dataValue = dataFormatter.formatCellValue(cell);
							try {
								dataValue = (int) cell.getNumericCellValue() + "";
							} catch (Exception e) {
								try {
									dataValue = cell.getStringCellValue();
								} catch (Exception e2) {
								}

							}
						}
							
					});
					dataMap.put(dataKey, dataValue);
				});
			});
			

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dataMap;
	}	
	
	
	/**
	 * This method creates new excel file & writes the data in separate sheet for every module
	 * 
	 * @param sheetName : module name
	 * @param fieldName
	 * @param fieldValue
	 */
	public void writeTestData(ConcurrentMap<String, String> dataMap) {
		int rowCnt;
		@SuppressWarnings("resource")
		Workbook workbook = new XSSFWorkbook();
		Sheet sheet;
		Row row;
		
		for (Map.Entry<String, String> entry : dataMap.entrySet()) {
			sheetName = entry.getKey().split("_")[0];
			fieldName = entry.getKey().split("_")[1];
			fieldValue = entry.getValue();
			
			sheet = workbook.getSheet(sheetName);
			if (sheet == null) {
				rowCnt = 0;
				sheet = workbook.createSheet(sheetName);
				row = sheet.createRow(rowCnt);
				row.createCell(0).setCellValue("Field");
				row.createCell(1).setCellValue("Value");
			}
			else
				rowCnt = sheet.getLastRowNum();

			row = sheet.createRow(++rowCnt);
			row.createCell(0).setCellValue(fieldName);
			row.createCell(1).setCellValue(fieldValue);

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
	
	private File getLatestFile() {
		Path testDataDir = Paths.get(currentDir, "src", "test", "resources", "TestData");
		Optional<File> mostRecentFile = Arrays
				.stream(testDataDir.toFile().listFiles())
				.filter(f -> f.isFile())
				.max((f1, f2) -> Long.compare(f1.lastModified(), f2.lastModified()));
		
		return mostRecentFile.get();
	}

}
