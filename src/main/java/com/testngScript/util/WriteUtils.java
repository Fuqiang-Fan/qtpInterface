package com.testngScript.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Properties;

public class WriteUtils {
	public static void main(String[] args) throws Exception{
		
        
	}
	
	public static String returnActual(String useFileValue) throws Exception {
		 // 获得类加载器，然后把文件作为一个流获取
        InputStream in = new FileInputStream("config.properties");  
        // 创建Properties实例
        Properties prop = new Properties();
        // 将Properties和流关联
        prop.load(in);
        String value=prop.getProperty(useFileValue);
        // 关闭资源
        in.close();
        return value;
    }
	
	
	
	
	
	public static void writeProperties(String isCrateFile,String value)  {
		Properties properties = new Properties();  
        OutputStream output = null;  
        try {
        	properties.load(new FileInputStream("config.properties"));
            output = new FileOutputStream("config.properties"); 
            properties.setProperty(isCrateFile, value);  

            properties.store(output, "andieguo modify" + new Date().toString());// 保存键值对到文件中  
        } catch (IOException io) {  
            io.printStackTrace();  
        } finally {  
            if (output != null) {  
                try {  
                    output.close();  
                } catch (IOException e) {  
                    e.printStackTrace();  
                }  
            }  
        }  
    }  
		    
	
}
