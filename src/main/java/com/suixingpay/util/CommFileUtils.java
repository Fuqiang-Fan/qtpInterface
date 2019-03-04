package com.suixingpay.util;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Created by yaojie on 16/10/18.
 */
public class CommFileUtils {

    /**
     * 获取系统的临时目录路径：getTempDirectoryPath()
     */
    public static String getTempDirectoryPath() {

        return System.getProperty("java.io.tmpdir");

    }

    /**
     * 获取代表系统临时目录的文件：getTempDirectory ()
     */
    public static File getTempDirectory() {

        return new File(getTempDirectoryPath());

    }

    /**
     * 获取用户的主目录路径：getUserDirectoryPath()
     */
    public static String getUserDirectoryPath() {

        return System.getProperty("user.home");

    }

    /**
     * 获取代表用户主目录的文件：getUserDirectory()
     */
    public static File getUserDirectory() {

        return new File(getUserDirectoryPath());

    }

    /***
     * @param path
     * @param prefixStr :前缀名
     * @return
     */
    public static File[] getFilesByPathPrefix(File path, final String prefixStr) {

        File[] fileArr = path.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (dir.isDirectory() && name
                        .startsWith(prefixStr)) {
                    return true;
                } else {
                    return false;
                }
            }
        });
        return fileArr;

    }

    /***
     * @param path
     * @param sufixStr :后缀名
     * @return
     */
    public static File[] getFilesByPathAndSuffix(File path,
                                                 final String sufixStr) {
        File[] fileArr = path.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                // System.out.println("prefixStr:"+prefixStr);
                if ((StringUtils.isNotBlank(sufixStr) || (dir.isDirectory() && name
                        .endsWith(sufixStr)))) {
                    return true;
                } else {
                    return false;
                }
            }
        });
        return fileArr;

    }

    public static void writeStringToFile(File file, String data) throws IOException {
        try {
            FileUtils.writeStringToFile(file, data, Charset.forName("UTF-8"), true);
            FileUtils.writeStringToFile(file, "\n", Charset.forName("UTF-8"), true);
        } catch (IOException e) {

            throw new IOException(e);
        }

    }

    /**
     * 文件读取
     */
    public static synchronized List<MapDifference> readStringFromFileLineByLine(String srcPath, String objFilePath) throws IOException {
        List<MapDifference> listMap = new ArrayList();
        try {
            File liftfile = new File(srcPath);
            FileReader liftfileReader = new FileReader(liftfile);
            BufferedReader liftBuffer = new BufferedReader(liftfileReader);

            StringBuffer stringBuffer = new StringBuffer();
            String leftline;


            int count = 0;
//            while ((leftline = liftBuffer.readLine()) != null &&(rightline = rightBuffer.readLine()) != null) {
//              if (leftline.equals(rightline) != true) {
//                    Map left = StringUtils.isNotBlank(leftline)?convertToMap(leftline):Maps.newHashMap();
//                    Map right = StringUtils.isNotBlank(rightline)?convertToMap(rightline):Maps.newHashMap();
//                    MapDifference diff = Maps.difference(left, right);
//                    listMap.add(diff);
//                }
//                count++;
//            }


            List<Map> leftList = new LinkedList<>();
            while ((leftline = liftBuffer.readLine()) != null) {
                Map left = convertToMap(leftline);
                leftList.add(left);
            }


            File rightfile = new File(objFilePath);
            FileReader rightfileReader = new FileReader(rightfile);
            BufferedReader rightBuffer = new BufferedReader(rightfileReader);
            String rightline;

            List<Map> rightList = new LinkedList<>();
            while ((rightline = rightBuffer.readLine()) != null) {
                Map right = convertToMap(rightline);
                rightList.add(right);
                count++;
            }

            listMap = mapDifference(leftList, rightList);

            liftfileReader.close();
            rightfileReader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return listMap;
    }


    private static List<MapDifference> mapDifference(List<Map> leftList, List<Map> rightList) {
        List<MapDifference> mapDifferences = new ArrayList<>();

        if (leftList.size() == rightList.size()) {

            for (int i = 0; i < leftList.size(); i++) {
                Map left = leftList.get(i);
                Map right = rightList.get(i);
                MapDifference differenceMap = Maps.difference(left, right);
                if (!differenceMap.areEqual()) {
                    mapDifferences.add(differenceMap);
                }
            }
        } else if (leftList.size() > rightList.size()) {
            for (int i = 0; i < leftList.size(); i++) {
                MapDifference differenceMap;
                if (i < rightList.size()) {
                    Map left = leftList.get(i);
                    Map right = rightList.get(i);
                    differenceMap = Maps.difference(left, right);
                } else {
                    Map left = leftList.get(i);
                    Map right = Maps.newHashMap();
                    differenceMap = Maps.difference(left, right);
                }
                if (!differenceMap.areEqual()) {
                    mapDifferences.add(differenceMap);
                }
            }

        } else {
            for (int i = 0; i < rightList.size(); i++) {
                MapDifference differenceMap;
                if (i < leftList.size()) {
                    Map left = leftList.get(i);
                    Map right = rightList.get(i);
                    differenceMap = Maps.difference(left, right);
                } else {
                    Map left = Maps.newHashMap();
                    Map right = rightList.get(i);
                    differenceMap = Maps.difference(left, right);
                }
                if (!differenceMap.areEqual()) {
                    mapDifferences.add(differenceMap);
                }
            }
        }


        return mapDifferences;
    }


    /**
     * @param insertSqlString INSERT INTO table_name (列1, 列2,...) VALUES (值1, 值2,....) 解析 转换 Map
     */
    public static Map convertToMap(String insertSqlString) {

        Map map = new LinkedHashMap();

        String line = insertSqlString.substring(0, insertSqlString.indexOf("("));

        String tablename = (line.replace("insert into", "").trim());

        String columns = insertSqlString.substring(insertSqlString.indexOf("("), insertSqlString.indexOf("values")).replace("(", "").replace(")", "");
        String columnValues = insertSqlString.substring(insertSqlString.lastIndexOf("values(") + 7, insertSqlString.lastIndexOf(")"))
                .replace(",'yyyy-mm-dd hh24:mi:ss'", "")
                .replace("'", "").trim();
        String[] colNames = columns.split(",");

        String[] colval = columnValues.split(",");

        for (int i = 0; i < colNames.length; i++) {

            String colName = colNames[i].trim();
            for (int j = i; j < colval.length && j <= i; j++) {
                if (colval[j].contains("to_date(")) {
                    colval[j] = colval[j].trim().replace("to_date(", "to_date('").replace(")", "") + ",'yyyy-mm-dd hh24:mi:ss')";
                }
                map.put(colName, colval[j]);
            }
        }

        if (colNames.length > colval.length) {
            for (int j = colNames.length - colval.length; j >= 1; j--) {
                String col = colNames[colNames.length - j].trim();
                map.put(col, "");

            }
        }

        return map;
    }

}
