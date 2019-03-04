package com.testngScript.jdbc;

import com.testngScript.util.DruidUtils;
import com.testngScript.util.HikariDataSourceUtils;

import org.apache.commons.lang.StringEscapeUtils;

import java.math.BigDecimal;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.testngScript.util.DruidUtils.getOwner;

/**
 * Created by yaojie on 16/10/10.
 */
public class JdbcDao {


    public static ResultSet query(String sql, Object... params) throws SQLException {

        Connection connection = DruidUtils.getConnection();

        PreparedStatement pstm = connection.prepareStatement(sql);

        return pstm.getResultSet();

    }

    public static int insert(String sql, Object... params) throws SQLException {
        Connection dbConnection = null;
        PreparedStatement pstm = null;
        int count = 0;
        try {
            DruidUtils.startTransaction();
            dbConnection = DruidUtils.getConnection();
            pstm = dbConnection.prepareStatement(sql);
            setParameters(pstm, params);
            pstm.executeUpdate();

            DruidUtils.commit();
        } catch (SQLException e) {
            DruidUtils.rollback();
            throw new SQLException(e);
        } finally {

            if (pstm != null) {
                pstm.close();
            }

            if (dbConnection != null) {
                DruidUtils.close();
            }
        }
        return count;

    }

    public static int update(String sql, Object... params) throws SQLException {
        Connection dbConnection = null;
        PreparedStatement pstmt = null;
        int count = 0;
        try {
            DruidUtils.startTransaction();
            dbConnection = DruidUtils.getConnection();
            pstmt = dbConnection.prepareStatement(sql);
            setParameters(pstmt, params);

            count = pstmt.executeUpdate();
            DruidUtils.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            DruidUtils.rollback();
            throw new SQLException(e);
        } finally {
            if (pstmt != null) {
                pstmt.close();
            }

            if (dbConnection != null) {
                DruidUtils.close();
            }
        }
        return count;
    }



    private static int excute(String sql) throws SQLException {
        Connection dbConnection = null;
        PreparedStatement pstmt = null;
        int count = 0;
        try {
            //HikariDataSourceUtils.startTransaction();
            // dbConnection = HikariDataSourceUtils.getConnection();
            dbConnection = DruidUtils.getConnection();
           pstmt = dbConnection.prepareStatement(sql);
            count = pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            DruidUtils.rollback();
            throw new SQLException(e);
        } finally {
            if (pstmt != null) {
                pstmt.close();
            }

            if (dbConnection != null) {
                dbConnection.close();
            }
        }

        return count;
    }


    /**
     * 执行多条sql 语句
     */
    public static void excuteTosql(String sqlString) throws SQLException {
        String[] sqls = sqlString.split(";;");
        for (int i = 0; i < sqls.length; i++) {
            excute(sqls[i].trim());
        }
    }

    /**
     * 为预编译声明传入参数
     *
     * @param pstmt  预编译声明
     * @param params 参数
     */
    private static void setParameters(PreparedStatement pstmt, Object... params) throws SQLException {
        try {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException(e);
        }
    }


    /**
     * 将ResultSet结果集中的记录映射到Map对象中.
     *
     * @param fieldClassName 是JDBC API中的类型名称,
     * @param fieldName      是字段名，
     * @param rs             是一个ResultSet查询结果集,
     * @param fieldValue     Map对象,用于存贮一条记录.
     */
    public static void _recordMappingToMap(String fieldClassName, String fieldName, ResultSet rs, Map fieldValue)
            throws SQLException {
        fieldName = fieldName.toLowerCase();
        // 优先规则：常用类型靠前
        if (fieldClassName.equals("java.lang.String")) {
            String s = rs.getString(fieldName);
            if (rs.wasNull()) {
                fieldValue.put(fieldName, null);
            } else {
                fieldValue.put(fieldName, s);
            }
        } else if (fieldClassName.equals("java.lang.Integer")) {
            int s = rs.getInt(fieldName);
            if (rs.wasNull()) {
                fieldValue.put(fieldName, null);
            } else {
                fieldValue.put(fieldName, s);// 早期jdk需要包装，jdk1.5后不需要包装
            }
        } else if (fieldClassName.equals("java.lang.Long")) {
            long s = rs.getLong(fieldName);
            if (rs.wasNull()) {
                fieldValue.put(fieldName, null);
            } else {
                fieldValue.put(fieldName, s);
            }
        } else if (fieldClassName.equals("java.lang.Boolean")) {
            boolean s = rs.getBoolean(fieldName);
            if (rs.wasNull()) {
                fieldValue.put(fieldName, null);
            } else {
                fieldValue.put(fieldName, s);
            }
        } else if (fieldClassName.equals("java.lang.Short")) {
            short s = rs.getShort(fieldName);
            if (rs.wasNull()) {
                fieldValue.put(fieldName, null);
            } else {
                fieldValue.put(fieldName, s);
            }
        } else if (fieldClassName.equals("java.lang.Float")) {
            float s = rs.getFloat(fieldName);
            if (rs.wasNull()) {
                fieldValue.put(fieldName, null);
            } else {
                fieldValue.put(fieldName, s);
            }
        } else if (fieldClassName.equals("java.lang.Double")) {
            double s = rs.getDouble(fieldName);
            if (rs.wasNull()) {
                fieldValue.put(fieldName, null);
            } else {
                fieldValue.put(fieldName, s);
            }
        } else if (fieldClassName.equals("java.sql.Timestamp")) {
            java.sql.Timestamp s = rs.getTimestamp(fieldName);
            if (rs.wasNull()) {
                fieldValue.put(fieldName, null);
            } else {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                fieldValue.put(fieldName, formatter.format(s));
            }
        } else if (fieldClassName.equals("java.sql.Date") || fieldClassName.equals("java.util.Date") || fieldClassName.equals("oracle.sql.TIMESTAMP")) {
            java.util.Date s = rs.getDate(fieldName);
            if (rs.wasNull()) {
                fieldValue.put(fieldName, null);
            } else {

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                fieldValue.put(fieldName, formatter.format(s));
            }
        } else if (fieldClassName.equals("java.sql.Time")) {
            java.sql.Time s = rs.getTime(fieldName);
            if (rs.wasNull()) {
                fieldValue.put(fieldName, null);
            } else {
                fieldValue.put(fieldName, s);
            }
        } else if (fieldClassName.equals("java.lang.Byte")) {
            byte s = rs.getByte(fieldName);
            if (rs.wasNull()) {
                fieldValue.put(fieldName, null);
            } else {
                fieldValue.put(fieldName, new Byte(s));
            }
        } else if (fieldClassName.equals("[B") || fieldClassName.equals("byte[]")) {
            // byte[]出现在SQL Server中
            byte[] s = rs.getBytes(fieldName);
            if (rs.wasNull()) {
                fieldValue.put(fieldName, null);
            } else {
                fieldValue.put(fieldName, s);
            }
        } else if (fieldClassName.equals("java.math.BigDecimal")) {
            BigDecimal s = rs.getBigDecimal(fieldName);
            if (rs.wasNull()) {
                fieldValue.put(fieldName, null);
            } else {
                fieldValue.put(fieldName, String.valueOf(s));
            }
        } else if (fieldClassName.equals("java.lang.Object") || fieldClassName.equals("oracle.sql.STRUCT")) {
            Object s = rs.getObject(fieldName);
            if (rs.wasNull()) {
                fieldValue.put(fieldName, null);
            } else {
                fieldValue.put(fieldName, s);
            }
        } else if (fieldClassName.equals("java.sql.Array") || fieldClassName.equals("oracle.sql.ARRAY")) {
            java.sql.Array s = rs.getArray(fieldName);
            if (rs.wasNull()) {
                fieldValue.put(fieldName, null);
            } else {
                fieldValue.put(fieldName, s);
            }
        } else if (fieldClassName.equals("java.sql.Clob")) {
            java.sql.Clob s = rs.getClob(fieldName);
            if (rs.wasNull()) {
                fieldValue.put(fieldName, null);
            } else {
                fieldValue.put(fieldName, s);
            }
        } else if (fieldClassName.equals("java.sql.Blob")) {
            java.sql.Blob s = rs.getBlob(fieldName);
            if (rs.wasNull()) {
                fieldValue.put(fieldName, null);
            } else {
                fieldValue.put(fieldName, s);
            }
        } else {// 对于其它任何未知类型的处理
            Object s = rs.getObject(fieldName);
            if (rs.wasNull()) {
                fieldValue.put(fieldName, null);
            } else {
                fieldValue.put(fieldName, s);
            }
        }
    }

    /**
     * 查询结果集转换成map
     */
    public static List<Map> _recordSetToMap(String sql) throws SQLException {
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;
        List<Map> records = new LinkedList();
        try {
            conn = HikariDataSourceUtils.getConnection();
            statement = conn.createStatement();
            rs = statement.executeQuery(sql); // 执行sql查询语句，返回查询数据的结果集
            ResultSetMetaData rsmd = rs.getMetaData();
            int fieldCount = rsmd.getColumnCount();
            while (rs.next()) {
                Map<String, String> valueMap = new LinkedHashMap<String, String>();
                for (int i = 1; i <= fieldCount; i++) {
                    String fieldClassName = rsmd.getColumnClassName(i);
                    String fieldName = rsmd.getColumnName(i);
                    _recordMappingToMap(fieldClassName, fieldName, rs, valueMap);
                }
                records.add(valueMap);
            }

        } catch (SQLException e) {
            throw new SQLException(e);
        } finally {
            // DruidUtils.close(); // 关闭数据库连接
            rs.close();
            statement.close();
            conn.close();

        }
        return records;
    }

    /**
     * 查询结果集转换成map
     */
    public static List<Map> _recordSetToInsert(String sql) throws SQLException {
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;
        List<Map> records = new LinkedList();
        try {
            conn = HikariDataSourceUtils.getConnection();
            statement = conn.createStatement();
            rs = statement.executeQuery(sql); // 执行sql查询语句，返回查询数据的结果集
            ResultSetMetaData rsmd = rs.getMetaData();
            int fieldCount = rsmd.getColumnCount();
            String l = null;
            while (rs.next()) {
                Map<String, String> valueMap = new LinkedHashMap<String, String>();
                for (int i = 1; i <= fieldCount; i++) {
                    String fieldClassName = rsmd.getColumnClassName(i);
                    //自动名
                    String fieldName = rsmd.getColumnName(i);
                    l += insertSql(fieldName, rs);
                    _recordMappingToMap(fieldClassName, fieldName, rs, valueMap);
                }
                records.add(valueMap);
            }
        } catch (SQLException e) {
            throw new SQLException(e);
        } finally {
            // DruidUtils.close(); // 关闭数据库连接
            rs.close();
            statement.close();
            conn.close();

        }
        return records;
    }

    public static String insertSql(String fieldName, ResultSet rs) throws SQLException {
        String insertString = fieldName.toLowerCase();
        insertString = insertString + rs.getObject(fieldName) + ",";
        return insertString;
    }

    /**
     * 读取数据库表,表记录发生变化的表
     */
    public static List<Map<String, List<Map>>> _recordSetToMaps() throws SQLException {

        List<Map<String, List<Map>>> mapList = new ArrayList();
        //查询数据不为空的表
        List<String> tables = null;
        try {
            tables = getChangeTablesNameByrecords();

            for (int i = 0; i < tables.size(); i++) {
                Map<String, List<Map>> listMap = new HashMap();
                String sql = "select * from  " + tables.get(i);
                List<Map> maps = _recordSetToMap(sql);
                if (maps.size() > 0) {
                    listMap.put(tables.get(i).toLowerCase(), maps);
                }

                mapList.add(listMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new SQLException(e);
        }
        return mapList;
    }


    public static List<String> createSqltoFile() throws SQLException {
        List<String> stringList = new ArrayList();
        Connection connection = null;
        try {
            List<String> tables = getChangeTablesNameByrecords();
            System.out.println("[接口影响变化的表]：" + Arrays.toString(tables.toArray()));
            // Connection connection=HikariDataSourceUtils.getConnection();
            connection = DruidUtils.getConnection();

            for (int i = 0; i < tables.size(); i++) {
                String sql = "select * from  " + tables.get(i);
                List list = createSqlFile(connection, tables.get(i), sql);
                stringList.addAll(list);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException(e);
        } finally {
            connection.close();
        }

        return stringList;

    }

    /**
     *
     */
    public static void _deleteRecordByTable() throws SQLException {
        //查询数据不为空的表
        List<String> tables = null;
        try {
            tables = getChangeTablesNameByrecords();
            for (int i = 0; i < tables.size(); i++) {
                String sql = " delete from  " + tables.get(i);
                excute(sql);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new SQLException(e);
        }
    }


    public static List<String> getChangeTablesNameByrecords() throws SQLException {
        List<String> tables = new ArrayList();
        String[] owners = getOwner.split(",");
        PreparedStatement pstmt = null;

        ResultSet rs = null;
        Connection conn = null;
        for (int i = 0; i < owners.length; i++) {
            String owner = owners[i];
            try {
                // pstmt = DruidUtils.getConnection().prepareStatement("SELECT owner, table_name  FROM dba_tables t_ where t_.owner='"+owner+"'\n");
                //conn= HikariDataSourceUtils.getConnection();
                conn = DruidUtils.getConnection();

                pstmt = conn.prepareStatement("SELECT owner, table_name  FROM dba_tables t_ where t_.owner='" + owner + "'\n");
                rs = pstmt.executeQuery();

                while (rs.next()) {
                    String ownername = rs.getString("owner");
                    String tablename = rs.getString("table_name");
                    String table = ownername.toUpperCase() + "." + tablename.toUpperCase();
                    java.util.Date currentTime = new java.util.Date();
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                    String dateString = formatter.format(currentTime);
                    boolean bool = getTables(table, dateString);
                    if (bool) {
                        tables.add(table);
                    }
                }
            } catch (SQLException e) {

                throw new SQLException(e);
            } finally {
                //DruidUtils.close();
                rs.close();
                pstmt.close();
                conn.close();
            }

        }
        return tables;
    }

    /**
     * 查询getRow
     */
    public static boolean getTables(String tablename, String dateformat) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            //查询表数据变化 VERSIONS_OPERATION I==新增,U==修改,D==删除 ，闪回查询TOdo
            //第一种方式（块级跟踪）
            /**
             * http://www.mamicode.com/info-detail-44410.html
             */

            String sql = "select ora_rowscn,\n" +
                    "       dbms_rowid.ROWID_BLOCK_NUMBER(rowid) blockid,\n" +
                    "       scn_to_timestamp(ora_rowscn)\n" +
                    "  from  " + tablename + " \n" +
                    "  where to_char(scn_to_timestamp(ora_rowscn),'yyyy-MM-dd')='" + dateformat + "'\n" +
                    " order by scn_to_timestamp(ora_rowscn)";



            conn = DruidUtils.getConnection();

            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            int rowCount = 0;//获得ResultSet的总行数
            while (rs.next()) {
                rowCount = rs.getInt(1);// 获取第一列的值id
            }
            if (rowCount >= 1) {

                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException(e);
        } finally {
            // DruidUtils.close();
            rs.close();
            pstmt.close();
            conn.close();
            //HikariDataSourceUtils.close();
        }
        return false;
    }


    /**
     * 查询结果集,生成insert into 脚本
     */
    public static List<String> createSqlFile(Connection conn, String tablename, String sqlstmt)
            throws SQLException {
        List<String> createSql = new ArrayList();
        //执行语句
        PreparedStatement pstmt;
        ResultSet rs = null;
        //连接
        try {
            //获取sql语句
            pstmt = conn.prepareStatement(sqlstmt,
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
        } catch (Exception e) {

            throw new SQLException("查询初始化失败!" + e);
        }
        //获取结果集
        try {
            rs = pstmt.executeQuery(); //结果集
            ResultSetMetaData rsmd = rs.getMetaData(); //元数据
            while (rs.next()) {
                StringBuffer row = new StringBuffer();
                row.append("insert into " + tablename + "(");
                int count = rsmd.getColumnCount();
                //获取其中的一条记录
                for (int i = 1; i <= count; i++) {
                    if (i == count) {
                        row.append(rsmd.getColumnName(i) + ") ");
                    } else {
                        row.append(rsmd.getColumnName(i)).append(", ");
                    }
                }
                row.append(" values(");
                //获取其中的一条记录
                for (int i = 1; i <= count; i++) {
                    //放入到文件中
                    if (rs.getObject(i) != null)//判断是否为null
                    {
                        switch (rsmd.getColumnType(i)) {
                            case Types.BIT:
                            case Types.INTEGER:
                            case Types.TINYINT:
                            case Types.BIGINT:
                            case Types.REAL:
                            case Types.FLOAT:
                            case Types.DOUBLE:
                            case Types.NUMERIC:
                            case Types.DECIMAL:
                            case Types.LONGVARBINARY:
                            case Types.VARBINARY:
                            case Types.BINARY:
                                row.append(rs.getObject(i));
                                break;
                            case Types.BLOB:
                            case Types.CLOB:
                                break;

                            case Types.DATE:
                            case Types.TIME:
                            case Types.TIMESTAMP:
                            case Types.NULL:
                                row.append("to_date('");
                                row.append(rs.getDate(i) + " " + rs.getTime(i));
                                row.append("'");
                                row.append(",'yyyy-mm-dd hh24:mi:ss')");
                                break;

                            case Types.LONGVARCHAR:
                            case Types.CHAR:
                            case Types.VARCHAR:
                            case Types.OTHER:
                            default:
                                row.append("'");
                                row.append(StringEscapeUtils.escapeSql(rs.getString(i)));
                                row.append("'");
                                break;
                        }//end switch
                    } else {
                        row.append("''");
                    }
                    if (i == count)//判断是否最后一列
                    {
                        row.append(");");
                    } else {
                        row.append(",");
                    }
                }
                //完成一行
                //System.out.println(row.toString());
                createSql.add(row.toString());
                //释放资源
                row = null;
            }
        } catch (SQLException e) {
            // logger.debug("查询出错!" + e);
            throw new SQLException(e);
        } finally {
            try {

                rs.close();
                pstmt.close();
            } catch (Exception e2) {

            }
        }


        return createSql;
    }




}

