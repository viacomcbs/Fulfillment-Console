package com.paramount.test.ff.fileproperties;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.paramount.test.ff.util.constants.Constants;


public class ExcelUtility {
	
	public static ArrayList<String> ReadDataExcel(String sheetName, String columnName)	{
		int cellNum = 0;
		ArrayList<String> DDoptionList = new ArrayList<String>();

		// int count =0;
		String CellData = null;

		try {
			File f = new File(Constants.TEST_DATA_FILE);
			FileInputStream fis = new FileInputStream(f);
			XSSFWorkbook wb = new XSSFWorkbook(fis);
			XSSFSheet sheet = wb.getSheet(sheetName);

			int totalCells = sheet.getRow(0).getPhysicalNumberOfCells();

			for (int i = 0; i < totalCells; i++) {
				String currentcellData = sheet.getRow(0).getCell(i).getStringCellValue();
				if (currentcellData.equals(columnName)) {
					cellNum = i;
					break;
				}
			}

			int totalRows = sheet.getPhysicalNumberOfRows();

			for (int j = 1; j < totalRows; j++) {
				XSSFCell cell = sheet.getRow(j).getCell(cellNum);

				if (cell == null)

				{
					break;
				}

				switch (cell.getCellType()) {
				case XSSFCell.CELL_TYPE_STRING:
					CellData = cell.getStringCellValue();
					DDoptionList.add(CellData);
					break;
				case XSSFCell.CELL_TYPE_NUMERIC:
					if (DateUtil.isCellDateFormatted(cell)) {
						CellData = String.valueOf(cell.getDateCellValue());
						DDoptionList.add(CellData);
					} else {
						CellData = String.valueOf((long) cell.getNumericCellValue());
						DDoptionList.add(CellData);
					}
					break;
				case XSSFCell.CELL_TYPE_BOOLEAN:
					CellData = Boolean.toString(cell.getBooleanCellValue());
					DDoptionList.add(CellData);
					break;
				case XSSFCell.CELL_TYPE_BLANK:
					CellData = "";
					DDoptionList.add(CellData);
					break;
				}

			}

			wb.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		Logger.getLogger("Count of Excel options " + DDoptionList.size());
		return DDoptionList;
	}

}