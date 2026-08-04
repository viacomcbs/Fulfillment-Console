package com.paramount.test.ff.common.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DataFactory {
	
	private ConcurrentMap<String, String> myDataMap = new ConcurrentHashMap<>();
	private ExcelUtility excelUtility;
	
	
	public void setUpDataRepository() {
		excelUtility = new ExcelUtility();
		myDataMap = excelUtility.readTestData();
	}
	
	public String getDataRepository(String moduleName, String myDataContextKey) {
		return myDataMap.get(moduleName+"_"+myDataContextKey);
	}
	
	public void setDataRepository(String moduleName, String myDataContextKey, String myDataContextValue) {
		myDataMap.put(moduleName+"_"+myDataContextKey, myDataContextValue);
	}
	
	public Boolean isContains(String key){
        return myDataMap.containsKey(key);
    }
	
	public void storeDataRepoInExcel() {
		excelUtility = new ExcelUtility();
		excelUtility.writeTestData(myDataMap);
	}

}
