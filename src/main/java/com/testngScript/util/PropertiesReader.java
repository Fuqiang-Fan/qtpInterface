package com.testngScript.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.WeakHashMap;

/**
 * Created by yaojie on 16/9/22.
 */
public class PropertiesReader {

    private static Map<String, Properties> filePropMapping = new WeakHashMap<String, Properties>();

    private PropertiesReader() {

    }

    /**
     * 取得指定properties文件的指定key的value
     */
    public static String getValue(String fileName, String key)
            throws MissingResourceException {
        final Properties properties = fillProperties(fileName);
        String value = properties.getProperty(key);
        if (value != null) {
            return value.trim();
        } else return "";

    }

    /**
     * 将文件中配置信息填充到properties对象中(用earth的ClassLoader)
     *
     * @return Properties对象
     * @author liuzeyin
     */
    public static Properties fillProperties(String fileName) {
        return fillProperties(fileName, PropertiesReader.class.getClassLoader());
    }

    /**
     * 将文件中配置信息填充到properties对象中(用指定的ClassLoader)
     *
     * @return Properties对象
     * @author liuzeyin
     */
    public static Properties fillProperties(String fileName, ClassLoader cl) {

        if (!fileName.endsWith(".properties")) {
            fileName = fileName + ".properties";
        }

        Properties properties = new Properties();

        if (filePropMapping.containsKey(fileName)) {
            properties = filePropMapping.get(fileName);
        } else {
            InputStream is = cl.getResourceAsStream(fileName);
            try {
                properties.load(is);
                filePropMapping.put(fileName, properties);
            } catch (Exception e) {
                throw new RuntimeException("load properties file error "
                        + fileName, e);
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return properties;

    }


}
