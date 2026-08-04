package com.paramount.test.ff.common.util.props;

import com.paramount.test.ff.common.util.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertyReader {

    public static String readFromProperties(String key){
        Properties prop= new Properties();
        String value="";
        try{
            //File file= new File("C:\\Users\\10700840\\MIC_AutomationNew\\src\\test\\resources\\config.properties");
            File file= new File("./src/test/resources/config.properties");

            FileInputStream fis=new FileInputStream(file);
            prop.load(fis);
            value= prop.getProperty(key);

        }catch (IOException io){
            Logger.log("File not found or unable to read the file: " + io.getMessage());
        }
        return  value;
    }

    public static void main(String[] args) {
        String v = readFromProperties("app");
        System.out.println("The app : " + v);
    }
}

