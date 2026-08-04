package com.paramount.test.ff.fileproperties;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import com.paramount.test.ff.util.constants.Constants;

public class ConfigurationUtility {
		

	public static String getConfigProperty(String key){
		try {
		Properties prop = readPropertiesFile(Constants.configPropertiesPath);
		return prop.getProperty(key);
		}
		catch (Exception e) {
			return "";
		}
	}
	
	public static Properties readPropertiesFile(String fileName) throws IOException {
	      FileInputStream fis = null;
	      Properties prop = null;
	      try {
	         fis = new FileInputStream(fileName);
	         prop = new Properties();
	         prop.load(fis);
	      } catch(FileNotFoundException fnfe) {
	         fnfe.printStackTrace();
	      } catch(IOException ioe) {
	         ioe.printStackTrace();
	      } finally {
	         fis.close();
	      }
	      return prop;
	}
	
}
