package com.suixingpay.jdbc;

import com.google.common.collect.MapDifference;
import com.google.gson.Gson;

import com.suixingpay.util.CommFileUtils;
import com.suixingpay.util.DruidUtils;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.StringReader;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Created by yaojie on 16/10/11.
 */
public class JdbcService {
    static CCJSqlParserManager parserManager = new CCJSqlParserManager();

    public static List<String> jdbc_service(String tables) {

        String[] table = tables.split(",");
        List<String> sqlString = new LinkedList();
        for (int i = 0; i < table.length; i++) {
            String tablename = table[i];
            String sql = "select * from " + tablename.toLowerCase();
            sqlString.add(sql);
        }
        return sqlString;
    }

    private static List<String> jdbc_service_delete(String tables) {

        String[] table = tables.split(",");
        List<String> sqlString = new LinkedList();
        for (int i = 0; i < table.length; i++) {
            String tablename = table[i];
            String sql = "delete from " + tablename.toLowerCase();
            sqlString.add(sql);
        }
        return sqlString;

    }


    /**
     * 删除
     */
    public static synchronized void exec_delete(String tables) throws Exception {

        try {
            if(tables.length()<=1)return ;
            String[] table = tables.split(";;");

            for (int i = 0; i < table.length; i++) {
//                Thread.sleep(500);
                String sql="";
                String statement = table[i].trim();
                if(statement.toLowerCase().contains("insert")){
                    Insert insert = (Insert) parserManager.parse(new StringReader(statement));
                    //sql = "delete from " + insert.getTable().getSchemaName().toUpperCase() + "." + insert.getTable().getName().toUpperCase();
                    StringBuilder  del =new StringBuilder("delete from  " + insert.getTable().getSchemaName().toUpperCase()
                            + "." + insert.getTable().getName().toUpperCase() + " where 1=1");
                    //String[] values=insert.getItemsList().toString().replaceAll("\\(","").replaceAll("\\)","").split(",");

                    List<Expression> insert_values_expression = ((ExpressionList) insert
                            .getItemsList()).getExpressions();
                    for (int j= 0; j< insert_values_expression.size(); j++) {
                        del.append(" and ").append(insert.getColumns().get(j)).append(" = "+insert_values_expression.get(j).toString());

                    }


                    sql=del.toString();
                    JdbcDao.excuteTosql(sql);

                    System.out.println("[脚步sql转化（insert）- > (delete )]"+statement);
                    System.out.println("[执行delete]:" + sql);

                }else if(statement.toLowerCase().contains("delete")){

                    sql=statement.toLowerCase().trim().toString();
                    JdbcDao.excuteTosql(sql);

                    System.out.println("[执行delete]:" + sql);
                }else if(statement.toLowerCase().contains("select")){

                    Select select = (Select) parserManager.parse(new StringReader(statement.trim()));
                    PlainSelect plain = (PlainSelect) select.getSelectBody();
                    Expression where_expression = plain.getWhere();
                    String where ="";
                     if(where_expression!=null){
                         where = "where "+where_expression.toString();
                     }
                    sql="delete from "+plain.getFromItem().toString()+" "+where ;

                    JdbcDao.excuteTosql(sql);

                    System.out.println("[脚步sql转化（select） - > (delete)]"+statement);
                    System.out.println("[执行delete]:" + sql);
                }

            }


        } catch (JSQLParserException e) {

            throw new Exception(e);
        }


    }

    /**
     * 查询sql
     */
    public static synchronized boolean exec_select(String tables) throws Exception {

        try {
            if(tables.length()<=1)return true;
            String[] table = tables.split(";;");


            List<String> list=new ArrayList<>();


            for (int i = 0; i < table.length; i++) {
                String statement=table[i];

                System.out.println("【查询接口数据】"+table[i]);
                Select select = (Select) parserManager.parse(new StringReader(statement.trim()));
                PlainSelect plain = (PlainSelect) select.getSelectBody();
                Expression where_expression = plain.getWhere();
                String where="";
                if(where_expression!=null){
                    where = "where "+where_expression.toString();
                }


                statement="select * from "+plain.getFromItem().toString()+" "+where ;

                Statement stmt= DruidUtils.getConnection().createStatement();
                ResultSet rs= stmt.executeQuery(statement);

                ResultSetMetaData rsmd = rs.getMetaData();
                int fieldCount = rsmd.getColumnCount();

                 boolean bool =true;
                while (rs.next()) {
                    Map<String, String> valueMap = new LinkedHashMap<String, String>();
                    for (int j = 1; j <= fieldCount; j++) {
                        String fieldClassName = rsmd.getColumnClassName(j);
                        String fieldName = rsmd.getColumnName(j);
                        JdbcDao._recordMappingToMap(fieldClassName, fieldName, rs, valueMap);
                    }
                    System.out.println(new Gson().toJson(valueMap));

                    bool=false;
                }

                 if(bool){

                    list.add("【查询数据为空】"+statement);
                 }

            }


            if(list.size()>0){
                for (String str:list){

                    System.out.println(str);
                }
                return false;
            }


            return true;
        } catch (Exception e) {

            throw new Exception(e);
        }


    }

    /**
     * 查询sql
     */
    public static synchronized boolean exec_count(String tables) throws Exception {

        try {
            if(tables.length()<=1)return true;
            String[] table = tables.split(";;");


            List<String> list=new ArrayList<>();


            for (int i = 0; i < table.length; i++) {
                String statement=table[i];

                System.out.println("【查询接口数据】"+table[i]);
                Select select = (Select) parserManager.parse(new StringReader(statement.trim()));
                PlainSelect plain = (PlainSelect) select.getSelectBody();
                Expression where_expression = plain.getWhere();
                String where="";
                if(where_expression!=null){
                    where = "where "+where_expression.toString();
                }


                statement="select * from "+plain.getFromItem().toString()+" "+where ;

                Statement stmt= DruidUtils.getConnection().createStatement();
                ResultSet rs= stmt.executeQuery(statement);
                /**test**/
                ResultSetMetaData rsmd = rs.getMetaData();
                int fieldCount = rsmd.getColumnCount();

                int count=0;
                boolean bool =true;
                while (rs.next()) {
                    Map<String, String> valueMap = new LinkedHashMap<String, String>();
                    for (int j = 1; j <= fieldCount; j++) {
                        String fieldClassName = rsmd.getColumnClassName(j);
                        String fieldName = rsmd.getColumnName(j);
                        JdbcDao._recordMappingToMap(fieldClassName, fieldName, rs, valueMap);
                    }
                    System.out.println(new Gson().toJson(valueMap));
                	count++;
                    bool=false;
                }
                System.out.println("【返回数量】    "+count);

                 if(bool){

                    list.add("【查询数据为空】"+statement);
                 }

            }


            if(list.size()>0){
                for (String str:list){

                    System.out.println(str);
                }
                return false;
            }


            return true;
        } catch (Exception e) {

            throw new Exception(e);
        }


    }
    
    /**
     * 查询sql
     */
    public static synchronized boolean exec_countnull(String tables) throws Exception {

        try {
            if(tables.length()<=1)return true;
            String[] table = tables.split(";;");


            List<String> list=new ArrayList<>();


            for (int i = 0; i < table.length; i++) {
                String statement=table[i];

                System.out.println("【查询接口数据】"+table[i]);
                Select select = (Select) parserManager.parse(new StringReader(statement.trim()));
                PlainSelect plain = (PlainSelect) select.getSelectBody();
                Expression where_expression = plain.getWhere();
                String where="";
                if(where_expression!=null){
                    where = "where "+where_expression.toString();
                }


                statement="select * from "+plain.getFromItem().toString()+" "+where ;

                Statement stmt= DruidUtils.getConnection().createStatement();
                ResultSet rs= stmt.executeQuery(statement);
                int count=0;
                 boolean bool =false;
                while (rs.next()) {
                    count++;
                    bool=true;
                }
                System.out.println("【返回数量】    "+count);
                 if(bool){

                    list.add("【查询数据为空】"+statement);
                 }

            }


            if(list.size()>0){
                for (String str:list){

                    System.out.println(str);
                }
                return false;
            }


            return true;
        } catch (Exception e) {

            throw new Exception(e);
        }


    }

    /**
     * bid=1008433,merc_id= 800567970110088
     * 转换成map
     */
    private static Map _recordStringToMap(String str) {
        Map<String, Object> map = new HashMap();
        java.util.StringTokenizer items = null;
        for (StringTokenizer entrys = new StringTokenizer(str, ","); entrys.hasMoreTokens();
             map.put(items.nextToken().trim(), items.hasMoreTokens() ? ((Object) (items.nextToken().trim())) : null))
            items = new StringTokenizer(entrys.nextToken(), "=");
        return map;
    }


    public static List<Map> _recordStringToList(String columnsValues) {
        List<Map> mapList = new LinkedList();
        String[] values = columnsValues.split(";");
        for (int i = 0; i < values.length; i++) {
            Map<String, Object> map = _recordStringToMap(values[i]);
            mapList.add(map);
        }
        return mapList;
    }

    /**
     * bid=1008433, merc_id= 800567970110088
     * 转换成 INSERT INTO table_name (列1, 列2,...) VALUES (值1, 值2,....)
     */
    public static void delete(String tables) throws Exception {

        List<String> sql = jdbc_service_delete(tables);

        for (int i = 0; i < sql.size(); i++) {
            String script_sql = sql.get(i);
            try {
                JdbcDao.excuteTosql(script_sql);
            } catch (SQLException e) {

                throw new SQLException(e);
            }

        }

    }


    /**
     * @param tables
     * @param columnsValue
     * @return
     * @throws Exception
     */
    public static boolean _verifyDatebaseRecordSet(String tables, String columnsValue) throws Exception {
        List<String> sqlStrs = jdbc_service(tables);
        List<Map> expectedSet = _recordStringToList(columnsValue);
        Gson gson = new Gson();
        for (int i = 0; i < sqlStrs.size(); i++) {
            String sql = sqlStrs.get(i);
            try {
                List<Map> recordSet = JdbcDao._recordSetToMap(sql);
                System.out.println("查询sql===>>>>" + sql + "\n" + "结果集:" + new Gson().toJson(recordSet));
                System.out.println("expectedSet:" + new Gson().toJson(expectedSet));
                Map expected = expectedSet.get(i);
                //JSONAssert.assertEquals(new Gson().toJson(expected), new Gson().toJson(recordSet), false);
                for (int j = 0; j < recordSet.size(); j++) {
                    //数据库结果,实际值
                    Map actual = recordSet.get(j);
                    //预期值
                    String actualToJson = gson.toJson(actual);
                    String expectedToJson = gson.toJson(expected);
                    // System.out.println("实际值::" + actualToJson);
                    // System.out.println("期望值: " + expectedToJson);
                    /***
                     * json格式比较
                     * 1,严格比较,包含值,结构完全一致
                     * 2,值一样,结构可以不一致
                     * String result = "{id:1,name:\"Juergen\"}";
                     *  JSONAssert.assertEquals("{id:1}", result, false); // Pass  包含
                     *  JSONAssert.assertEquals("{id:1}", result, true); // Fail 完全一致
                     */

                    // JSONAssert.assertEquals(expectedToJson, actualToJson, false);
                }

            } catch (SQLException e) {
                e.printStackTrace();
                throw new SQLException(e);
            }
        }
        return false;
    }


    /**
     * 生成文件
     */
    public static synchronized void writeFileJson(String directory, String filename, String suffix) throws Exception {
        //提取数据库记录
        List<Map<String, List<Map>>> recordList = JdbcDao._recordSetToMaps();

        String path = System.getProperty("user.dir") + "/" + directory;
        String pathfilename = "";
        File filedirectory = new File(path);
        //文件夹存在
        if (filedirectory.isDirectory()) {
            File[] file = CommFileUtils.getFilesByPathPrefix(new File(path), filename);
            int count = 0;
            if (file.length > 0) {
                List<Integer> list = new ArrayList();
                for (File aFile : file) {
                    String fileName = aFile.getName();
                    String suffixs = fileName.substring(0, fileName.lastIndexOf("."));//如果想获得不带点的后缀，
                    String nums = StringUtils.substringAfter(suffixs, "_");
                    if (StringUtils.isNotBlank(nums)) {
                        list.add(Integer.valueOf(nums));
                    } else {
                        list.add(0);
                    }
                }
                count = Collections.max(list) + 1;
                pathfilename = path + "/" + filename + "_" + count + suffix;
                for (int i = 0; i < recordList.size(); i++) {
                    Map<String, List<Map>> linesRecord = recordList.get(i);
                    if (linesRecord.size() > 0) {
                        CommFileUtils.writeStringToFile(new File(pathfilename), new Gson().toJson(linesRecord));
                    }
                }
            } else {
                pathfilename = path + "/" + filename + suffix;
                for (int i = 0; i < recordList.size(); i++) {
                    Map<String, List<Map>> linesRecord = recordList.get(i);
                    if (linesRecord.size() > 0) {
                        CommFileUtils.writeStringToFile(new File(pathfilename), new Gson().toJson(linesRecord));
                    }
                }
            }
        } else {
            for (int i = 0; i < recordList.size(); i++) {
                Map<String, List<Map>> linesRecord = recordList.get(i);
                if (recordList.get(i).size() > 0) {
                    pathfilename = path + "/" + filename + suffix;
                    FileUtils.forceMkdir(filedirectory);
                    CommFileUtils.writeStringToFile(new File(pathfilename), new Gson().toJson(linesRecord));
                }

            }

        }

    }

    /***
     * 生成insert.sql
     * @param directory
     * @param filename
     * @param suffix
     * @throws Exception
     */
    public static synchronized void writeFiletoSql(String directory, String filename, String suffix) throws Exception {
        List<String> recordList = JdbcDao.createSqltoFile();


        String path = System.getProperty("user.dir") + "/outputFile/" + directory;
        System.out.println("[文件路径:]" + path);
        String pathfilename = "";
        File filedirectory = new File(path);
        File[] file;
        //文件夹存在
        if (filedirectory.isDirectory()) {
            file = CommFileUtils.getFilesByPathPrefix(new File(path), filename);
            int count = 0;
            if (file.length > 0) {
                List<Integer> list = new ArrayList();
                for (int j = 0; j < file.length; j++) {
                    String fileName = file[j].getName();
                    String suffixs = fileName.substring(0, fileName.lastIndexOf("."));//如果想获得不带点的后缀，
                    String nums = StringUtils.substringAfter(suffixs, "_");
                    if (StringUtils.isNotBlank(nums)) {
                        list.add(Integer.valueOf(nums));
                    } else {
                        list.add(0);
                    }
                }
                count = Collections.max(list) + 1;
                pathfilename = path + "/" + filename + "_" + count + suffix;
                for (int i = 0; i < recordList.size(); i++) {
                    CommFileUtils.writeStringToFile(new File(pathfilename), recordList.get(i));
                }
            } else {
                pathfilename = path + "/" + filename + suffix;
                for (int i = 0; i < recordList.size(); i++) {
                    CommFileUtils.writeStringToFile(new File(pathfilename), recordList.get(i));
                    System.out.println(recordList.get(i));
                }
            }


        } else {
            for (int i = 0; i < recordList.size(); i++) {
                pathfilename = path + "/" + filename + suffix;
                FileUtils.forceMkdir(filedirectory);
                CommFileUtils.writeStringToFile(new File(pathfilename), recordList.get(i));
                System.out.println(recordList.get(i));
            }

        }
        /****
         * 文件读取并且比较
         */
        if (StringUtils.isNotBlank(pathfilename) && pathfilename.contains("_")) {
            String leftFile = path + "/" + filename + suffix;
            List<MapDifference> diffs = CommFileUtils.readStringFromFileLineByLine(leftFile, pathfilename);
            for (MapDifference dif : diffs) {
                System.out.println("[文件差异对比:]" + "" + dif.toString());
            }

        }

    }


    public static synchronized void _deleteRecord() throws Exception {

        try {
            List<String> tables = JdbcDao.getChangeTablesNameByrecords();
            for (int i = 0; i < tables.size(); i++) {
                String sql = "delete from  " + tables.get(i);
                JdbcDao.excuteTosql(sql);
            }
            System.out.println("[删除接口数据] :" + Arrays.toString(tables.toArray()));
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException(e);
        }

    }

}

