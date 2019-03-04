package com.suixingpay.util;

import com.alibaba.druid.pool.DruidDataSource;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yaojie on 16/9/28.
 */
public class DruidUtils {

    /**
     * 日志工具
     */
    // private static final Logger LOG = LoggerFactory.getLogger(JdbcDao.class);

    public static String jdbcUrl = "jdbc.mysql.url";
    public static String user = "jdbc.mysql.username";
    public static String password = "jdbc.mysql.password";

    public static String driverName = "jdbc.mysql.driverName";

    //public static String owner = "jdbc.oracle.owner";

    public static String properties = "application.properties";

    public static String getOwner = "";

    private static DruidDataSource dataSource = new DruidDataSource();
    //声明线程共享变量
    public static ThreadLocal<Connection> container = new ThreadLocal<Connection>();

    //配置说明，参考官方网址
    //http://blog.163.com/hongwei_benbear/blog/static/1183952912013518405588/
    static {
        dataSource.setUrl(PropertiesReader.getValue(properties, jdbcUrl));
        dataSource.setUsername(PropertiesReader.getValue(properties, user));//用户名
        dataSource.setPassword(PropertiesReader.getValue(properties, password));//密码
        dataSource.setDriverClassName(PropertiesReader.getValue(properties, driverName));
        dataSource.setInitialSize(150); //初始化连接数
        dataSource.setMaxActive(1500);//最大连接数
        dataSource.setMinIdle(0);//最小空闲连接
        dataSource.setMaxWait(60000);//最大等待时间
        dataSource.setValidationQuery("SELECT 1 from dual");
        dataSource.setTestOnBorrow(false);
        dataSource.setTestWhileIdle(true);
        dataSource.setPoolPreparedStatements(true);
        //getOwner = PropertiesReader.getValue(properties, owner);

    }


    /**
     * 获取数据连接
     */
    public static Connection getConnection() {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
//             LOG.info(Thread.currentThread().getName()+"连接已经开启......");
            container.set(conn);
        } catch (Exception e) {
//            LOG.error("连接获取失败" +e);
            e.printStackTrace();
        }
        return conn;
    }

    /***获取当前线程上的连接开启事务*/
    public static void startTransaction() {
        Connection conn = container.get();//首先获取当前线程的连接
        if (conn == null) {//如果连接为空
            conn = getConnection();//从连接池中获取连接
            container.set(conn);//将此连接放在当前线程上
            // LOG.info(Thread.currentThread().getName()+"空连接从dataSource获取连接");
        } else {
            //LOG.info(Thread.currentThread().getName()+"从缓存中获取连接");
        }
        try {
            conn.setAutoCommit(false);//开启事务
        } catch (Exception e) {
            e.printStackTrace();
            // LOG.error("连接获取失败" +e);
        }
    }

    //提交事务
    public static void commit() {
        try {
            Connection conn = container.get();//从当前线程上获取连接if(conn!=null){//如果连接为空，则不做处理
            if (null != conn) {
                conn.commit();//提交事务
                // LOG.info(Thread.currentThread().getName()+"事务已经提交......");
            }
        } catch (Exception e) {
            e.printStackTrace();
            //LOG.error("连接获取失败" +e);
        }
    }


    /***回滚事务*/
    public static void rollback() {
        try {
            Connection conn = container.get();//检查当前线程是否存在连接
            if (conn != null) {
                conn.rollback();//回滚事务
                container.remove();//如果回滚了，就移除这个连接
            }
        } catch (Exception e) {
            e.printStackTrace();
            // LOG.error("连接获取失败" +e);
        }
    }

    /***关闭连接*/
    public static void close() {
        try {
            Connection conn = container.get();
            if (conn != null) {
                conn.close();
                // LOG.info(Thread.currentThread().getName()+"连接关闭");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            try {
                container.remove();//从当前线程移除连接切记
            } catch (Exception e2) {
                e2.printStackTrace();
                //LOG.error("连接获取失败" +e2);
            }
        }
    }

    //获取数据库中所有表的表名，并添加到列表结构中。
    public static List getTableNameList(Connection conn) throws SQLException {

        DatabaseMetaData dbmd = conn.getMetaData();

        //访问当前用户ANDATABASE下的所有表

        ResultSet rs = dbmd.getTables("null", PropertiesReader.getValue(properties, user).toLowerCase(), "%", new String[]{"TABLE"});

        //System.out.println("kkkkkk"+dbmd.getTables("null", "%", "%", new String[] { "TABLE" }));

        List tableNameList = new ArrayList();

        while (rs.next()) {

            tableNameList.add(rs.getString("TABLE_NAME"));

        }

        return tableNameList;

    }
    // 获取数据表中所有列的列名，并添加到列表结构中。

    public static List getColumnNameList(Connection conn, String tableName)

            throws SQLException {

        DatabaseMetaData dbmd = conn.getMetaData();

        ResultSet rs = dbmd.getColumns(null, "%", tableName, "%");

        List columnNameList = new ArrayList();

        while (rs.next()) {

            columnNameList.add(rs.getString("COLUMN_NAME"));

        }

        return columnNameList;

    }




}
