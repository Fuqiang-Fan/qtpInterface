package com.testngScript.util;


import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * Created by yaojie on 16/10/19.
 */
public class HikariDataSourceUtils {
    /**
     * 日志工具
     */
    private static final Logger logger = LogManager.getLogger(HikariDataSourceUtils.class);

    public static String jdbcUrl = "jdbc.oracle.url";
    public static String user = "jdbc.oracle.username";
    public static String password = "jdbc.oracle.password";
    public static String driverName = "jdbc.oracle.driverName";
    public static String properties = "application.properties";
    private static HikariDataSource ds;

    /**
     * 初始化连接池 * @param minimum * @param Maximum
     */
    public static void init() {
        //连接池配置
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(PropertiesReader.getValue(properties, driverName));
        config.setJdbcUrl(PropertiesReader.getValue(properties, jdbcUrl));
        config.setUsername(PropertiesReader.getValue(properties, user));
        config.setPassword(PropertiesReader.getValue(properties, password));
        config.addDataSourceProperty("cachePrepStmts", true);
        config.addDataSourceProperty("prepStmtCacheSize", 500);
        config.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
        config.setAutoCommit(true);
        //池中最小空闲链接数量
        config.setMinimumIdle(5);
        //池中最大链接数量
        config.setMaximumPoolSize(10);
        config.setConnectionTimeout(3000);
        config.setValidationTimeout(3000);
        ds = new HikariDataSource(config);

    }

    public static Connection getConnection() throws SQLException {
        Connection conn;
        try {
            init();
            conn = ds.getConnection();

            System.out.println(conn);
        } catch (Exception e) {

            throw new SQLException(e);
        }
        return conn;
    }


    public static void main(String[] args) throws Exception {

        HikariConfig config = new HikariConfig();
        config.setMaximumPoolSize(5);
        config.setConnectionTimeout(3000);
        config.setValidationTimeout(3000);
        config.setConnectionTestQuery("select 1 from dual");
        config.setDriverClassName("oracle.jdbc.driver.OracleDriver");
        config.setJdbcUrl("jdbc:oracle:thin:@172.16.136.249:1521/interface");
        config.addDataSourceProperty("user", "yaojie");
        config.addDataSourceProperty("password", "yaojie");

        HikariDataSource ds1 = new HikariDataSource(config);


        Connection conn = getConnection();


        PreparedStatement st = conn.prepareStatement("select sysdate from dual");

        ResultSet rs = st.executeQuery();

        while (rs.next()) {
            System.out.println(rs.getString(1));
        }


        conn.close();


    }
}
