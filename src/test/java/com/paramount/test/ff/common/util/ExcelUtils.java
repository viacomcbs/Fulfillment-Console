package com.paramount.test.ff.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelUtils {
	private final String currentDir = System.getProperty("user.dir");
	private String dataKey = "";
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
	private String sheetName = "";
	@SuppressWarnings("unused")
	private String className, tcName, testCaseStatus, fieldName, fieldValue;

	/**
	 * 
	 * @return ConcurrentMap where key is class name and value is list of test
	 *         and it's status
	 */

	public ConcurrentMap<String, List<String>> readPrevRerunReport() {
		ConcurrentMap<String, List<String>> dataMap = new ConcurrentHashMap<>();
		DataFormatter dataFormatter = new DataFormatter();

		try (FileInputStream fileInputStream = new FileInputStream(getLatestFile("ReRun_Report"))) {

			@SuppressWarnings("resource")
			XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream);
			workbook.forEach(sheet -> {
				if (!sheet.getSheetName().equals("Summary")) {
					sheet.forEach(row -> {
						if (row.getRowNum() != 0) {
							List dataValues = new LinkedList();
							row.forEach(cell -> {
								if (cell.getColumnIndex() == 0)
									dataKey = dataFormatter.formatCellValue(cell);
								else
									dataValues.add(dataFormatter.formatCellValue(cell));
							});
							dataMap.put(dataKey, dataValues);
						}
					});
				}
			});

		} catch (IOException e) {
			e.printStackTrace();
		}
		return dataMap;
	}

	/**
	 * 
	 * @return ConcurrentMap where key is pkgName-className and value is list of
	 *         test & it's status
	 */

	public ConcurrentMap<String, List<String>> fetchAllTestResults() {
		ConcurrentMap<String, List<List<String>>> allTests = new ConcurrentHashMap<>();
		List<List<String>> innerList = null;
		List<String> temp = null;
		DataFormatter dataFormatter = new DataFormatter();

		try (FileInputStream fileInputStream = new FileInputStream(getLatestFile("ReRun_Report"))) {

			@SuppressWarnings("resource")
			XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream);
			XSSFSheet sheet;

			for (int i = 1; i < workbook.getNumberOfSheets(); i++) {
				sheet = workbook.getSheetAt(i);
				innerList = Collections.synchronizedList(new ArrayList<>());
				if (!sheet.getSheetName().equals("Summary")) {
					for (Row row : sheet) {
						if (row.getRowNum() != 0) {
							temp = Collections.synchronizedList(new ArrayList<>());
							for (Cell cell : row) {
								temp.add(dataFormatter.formatCellValue(cell));
							}
							innerList.add(temp);
						}
					}
				}
				allTests.put(workbook.getSheetName(i), innerList);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return convertToProperFormat(allTests);
	}

	private ConcurrentMap<String, List<String>> convertToProperFormat(ConcurrentMap<String, List<List<String>>> map) {
		ConcurrentMap<String, List<String>> allTests = new ConcurrentHashMap<>();
		String key = null;
		List<String> testDetails;

		for (Map.Entry<String, List<List<String>>> entry : map.entrySet()) {

			for (List<String> list : entry.getValue()) {
				testDetails = new ArrayList<>();
				key = entry.getKey() + "-" + list.get(0);
				testDetails.add(list.get(1));
				testDetails.add(list.get(2));

				allTests.put(key, testDetails);
			}
		}
		return allTests;
	}

	/**
	 * 
	 * @param testCaseMap
	 *            Excel file with results of all tests executed in current run
	 *            would be created. Every module will be written in a separate
	 *            worksheet. Summary sheet will give high level overview of the
	 *            test result. Individual worksheets will contain detailed
	 *            results.
	 */

	public void createRerunFile(ConcurrentMap<String, List<String>> testCaseMap) {
		String fileName = String.join(File.separator, currentDir, "ReRun_Report", "TestResults_");
		File file = new File(fileName + sdf.format(new Timestamp(System.currentTimeMillis())) + ".xlsx");
		int rowCnt;

		@SuppressWarnings("resource")
		Workbook workbook = new XSSFWorkbook();
		Sheet sheet;
		Row row;

		for (Map.Entry<String, List<String>> outerMap : testCaseMap.entrySet()) {

			sheetName = outerMap.getKey().split("-")[0]; // packageName
			className = outerMap.getKey().split("-")[1];
			

			if (workbook.getSheet("Summary") == null)
				createSummarySheet(workbook, testCaseMap);
			sheetName = WorkbookUtil.createSafeSheetName(sheetName);

			sheet = workbook.getSheet(sheetName);
			
			

			if (sheet == null) {
				rowCnt = 0;
				sheet = workbook.createSheet(sheetName);

				row = sheet.createRow(rowCnt);
				row.createCell(0).setCellValue("Class Name");
				row.createCell(1).setCellValue("Test Case Name");
				row.createCell(2).setCellValue("Test Case Status");

			} else
				rowCnt = sheet.getLastRowNum();

			for (int i = 0; i < outerMap.getValue().size(); i++) {
				row = sheet.createRow(++rowCnt);
				row.createCell(0).setCellValue(className);
				row.createCell(1).setCellValue(outerMap.getValue().get(i));
				row.createCell(2).setCellValue(outerMap.getValue().get(i + 1));

				synchronized (this) {
					try (FileOutputStream outputStream = new FileOutputStream(file)) {
						workbook.write(outputStream);
					} catch (IOException | EncryptedDocumentException e) {
						e.printStackTrace();
					}
				}
				i = i + 1;
			}
		}

	}

	private File getLatestFile(String folderName) {
		Path directory;
		directory = Paths.get(currentDir, folderName);
		Optional<File> mostRecentFile = Arrays.stream(directory.toFile().listFiles()).filter(f -> f.isFile())
				.max((f1, f2) -> Long.compare(f1.lastModified(), f2.lastModified()));
		return mostRecentFile.get();
	}

	private void createSummarySheet(Workbook workbook, ConcurrentMap<String, List<String>> testCaseMap) {

		int rowCnt, totalTcCnt, failCnt, passCnt, skipCnt;
		ConcurrentMap<String, String> summaryMap = new ConcurrentHashMap<>();
		Sheet sheet;
		Row row;

		rowCnt = 0;
		sheet = workbook.createSheet("Summary");
		createHeaderRow(sheet);

		CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
		Font font = sheet.getWorkbook().createFont();
		font.setFontHeightInPoints((short) 11);
		font.setFontName("Calibri");

		setBorders(cellStyle, BorderStyle.THIN);
		cellStyle.setAlignment(HorizontalAlignment.CENTER);

		for (Map.Entry<String, List<String>> map : testCaseMap.entrySet()) {
			if (summaryMap.containsKey(map.getKey().split("-")[0])) {
				totalTcCnt = Integer.parseInt(summaryMap.get(map.getKey().split("-")[0]).split(",")[0]);
				passCnt = Integer.parseInt(summaryMap.get(map.getKey().split("-")[0]).split(",")[1]);
				failCnt = Integer.parseInt(summaryMap.get(map.getKey().split("-")[0]).split(",")[2]);
				skipCnt = Integer.parseInt(summaryMap.get(map.getKey().split("-")[0]).split(",")[3]);
			} else {
				failCnt = 0;
				passCnt = 0;
				skipCnt = 0;
				totalTcCnt = 0;
			}

			// loop through list to get count
			for (int i = 0; i < map.getValue().size(); i++) {
				// get totalCnt by iterating over the list which gives number of
				// methods executed for every pkg-class combination
				totalTcCnt = totalTcCnt + (map.getValue().size() / 2);
				if (map.getValue().get(i + 1).equalsIgnoreCase("P"))
					passCnt++;
				else if (map.getValue().get(i + 1).equalsIgnoreCase("F"))
					failCnt++;
				else
					skipCnt++;
				i = i + 1;
			}

			summaryMap.put(map.getKey().split("-")[0], String.join(",", String.valueOf(totalTcCnt),
					String.valueOf(passCnt), String.valueOf(failCnt), String.valueOf(skipCnt)));
		}

		for (Map.Entry<String, String> entry : summaryMap.entrySet()) {
			row = sheet.createRow(++rowCnt);

			row.createCell(0).setCellStyle(cellStyle);
			sheet.autoSizeColumn(0);
			row.getCell(0).setCellValue(entry.getKey());

			row.createCell(1).setCellStyle(cellStyle);
			sheet.autoSizeColumn(1);
			row.getCell(1).setCellValue(entry.getValue().split(",")[0]);

			row.createCell(2).setCellStyle(cellStyle);
			sheet.autoSizeColumn(2);
			row.getCell(2).setCellValue(entry.getValue().split(",")[1]);

			row.createCell(3).setCellStyle(cellStyle);
			sheet.autoSizeColumn(3);
			row.getCell(3).setCellValue(entry.getValue().split(",")[2]);

			row.createCell(4).setCellStyle(cellStyle);
			sheet.autoSizeColumn(4);
			row.getCell(4).setCellValue(entry.getValue().split(",")[3]);
		}
	}

	private void createHeaderRow(Sheet sheet) {

		CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
		Font font = sheet.getWorkbook().createFont();
		font.setBold(true);
		font.setFontHeightInPoints((short) 12);
		font.setFontName("Calibri");

		cellStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
		cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		cellStyle.setFont(font);
		cellStyle.setAlignment(HorizontalAlignment.CENTER);
		setBorders(cellStyle, BorderStyle.THICK);

		Row row = sheet.createRow(0);

		Cell cellTitle = row.createCell(0);
		cellTitle.setCellStyle(cellStyle);
		cellTitle.setCellValue("Module");

		Cell cellAuthor = row.createCell(1);
		cellAuthor.setCellStyle(cellStyle);
		cellAuthor.setCellValue("Total TC");

		Cell cellPrice = row.createCell(2);
		cellPrice.setCellStyle(cellStyle);
		cellPrice.setCellValue("TC Passed");

		Cell cellFail = row.createCell(3);
		cellFail.setCellStyle(cellStyle);
		cellFail.setCellValue("TC Failed");

		Cell cellSkipped = row.createCell(4);
		cellSkipped.setCellStyle(cellStyle);
		cellSkipped.setCellValue("TC Skipped");
	}

	private CellStyle setBorders(CellStyle cellStyle, BorderStyle borderStyle) {
		cellStyle.setBorderBottom(borderStyle);
		cellStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());

		cellStyle.setBorderLeft(borderStyle);
		cellStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());

		cellStyle.setBorderRight(borderStyle);
		cellStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());

		cellStyle.setBorderTop(borderStyle);
		cellStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
		return cellStyle;
	}

}
