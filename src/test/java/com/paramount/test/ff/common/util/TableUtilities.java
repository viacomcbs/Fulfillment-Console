package com.paramount.test.ff.common.util;



import java.util.List;

import com.synergy.core.driver.By;
import com.synergy.core.driver.elements.DesktopBrowserElement;

public class TableUtilities {
	
	
	
	public By  getRow(DesktopBrowserElement table,Tabletype type) {
		
		
		String  rowXpath ="";
	switch (type) {
	case TABLE:
		 rowXpath = "";
		 break;
	case DIV:
		rowXpath = "//div[@class='cdk-virtual-scroll-content-wrapper']/div";
		break;

	default:
		break;
	}
		
	return By.XPath(rowXpath);
	}
	
	
	public int getRows(DesktopBrowserElement table) {
			
	List<DesktopBrowserElement> rows = table.findDesktopBrowserElements(By.TagName("tr"));
	
	return rows.size();

	}
	
	//Tabletype type = Tabletype.DIV;
	public int getRows(DesktopBrowserElement table,Tabletype type) {
		
		List<DesktopBrowserElement> rows = table.findDesktopBrowserElements(By.TagName("div"));
		
		return rows.size();

		}
	
	
	String xpath = "//div[@class='cdk-virtual-scroll-content-wrapper']//div[contains(@class,'table-cell')]";
	
	public String getCellValueByColumnNumber(DesktopBrowserElement table,int colNum) {
		
		Tabletype type = Tabletype.DIV;
		By rowXpath = getRow(table,type);
		List<DesktopBrowserElement> rows = table.findDesktopBrowserElements(rowXpath);
		
		System.out.println("The Table rows size : " + rows.size());
		
		Tabletype type1 = Tabletype.DIV;
		DesktopBrowserElement  row = table.findDesktopBrowserElement(getRowByNumber(1, type1));
		DesktopBrowserElement cell = row.findDesktopBrowserElement(getCellByNumber(2,type1));
		
		System.out.println("The cell value : " + cell.getText());
		
		return cell.getText();
	
	}
	
	
	public By getTable(Tabletype type) {
		String  tableXpath ="";
		switch (type) {
		case TABLE:
			tableXpath = "";
			 break;
		case DIV:
			tableXpath = "//div[@class='cdk-virtual-scroll-content-wrapper']";
			break;

		default:
			break;
		}
			
		return By.XPath(tableXpath);
		}
		
		
	
	
	String tableXpath ="//div[@class='cdk-virtual-scroll-content-wrapper']/descendant::div[contains(@class,'package-group package-details')][1]";
	
	public By getRowByNumber(int number,Tabletype type) {
		String  rowXpath ="";
		switch (type) {
		case TABLE:
			 rowXpath = "";
			 break;
		case DIV:
			rowXpath = "./descendant::div[contains(@class,'package-group package-details')]["+number+"]";
			break;

		default:
			break;
		}
			
		return By.XPath(rowXpath);
		}
	
	
	public By getCellByNumber(int number,Tabletype type) {
		String  columnXpath ="";
		switch (type) {
		case TABLE:
			columnXpath = "";
			 break;
		case DIV:
			columnXpath = "./div["+number+"]";
			break;

		default:
			break;
		}
			
		return By.XPath(columnXpath);
		}
	
	public By getAllCellsByColNumber(int number,Tabletype type) {
		String  columnXpath ="";
		switch (type) {
		case TABLE:
			columnXpath = "";
			 break;
		case DIV:
			columnXpath = "./descendant::div[contains(@class,'inner-package')]";
			break;

		default:
			break;
		}
			
		return By.XPath(columnXpath);
		}
		
		
	}


