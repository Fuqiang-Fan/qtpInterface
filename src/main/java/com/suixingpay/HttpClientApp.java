package com.suixingpay;

import com.google.gson.Gson;

import com.suixingpay.httpclient.HttpClientUtil;
import com.suixingpay.httpclient.common.HttpConfig;
import com.suixingpay.httpclient.common.HttpHeader;
import com.suixingpay.httpclient.common.HttpMethods;
import com.suixingpay.jdbc.JdbcDao;
import com.suixingpay.jdbc.JdbcService;
import com.suixingpay.util.JsonUtils;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.http.Header;
import org.skyscreamer.jsonassert.JSONAssert;
import org.testng.Assert;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yaojie on 2017/2/15.
 */
public class HttpClientApp {


    static Gson gson = new Gson();


    @Test
    @Parameters({"httpURL", "method", "parameterValues", "expected", "init", "script_sql", "isCrateFile", "filename","interface_select_sql"})
    public void run
            (String httpURL, String method, String parameterValues,
             String expected, boolean init, String script_sql, boolean isCrateFile, String filename,String interface_select_sql) throws Exception {
        try {
            //System.out.println("init:"+init);
            if (init) {
                System.out.println(">>>>>>>>初始化数据>>>>>>>>>>>>");
                JdbcDao.excuteTosql(script_sql);
            }
            parameterValues=parameterValues.replaceAll("'","\"");


            expected=expected.replaceAll("'","\"");

            Map<String, Object> map = new HashMap<>();
            map.put("action", "create");
            map.put("type", 1);
            map.put("investorId", "4");
            map.put("businessId" , "ZCJF20190301141935674") ;
            map.put("channelId", "A4");

            //设置header信息
            Header[] headers = HttpHeader.custom().contentType("application/x-www-form-urlencoded").build();
            //插件式配置请求参数（网址、请求参数、编码、client）
            HttpConfig config = HttpConfig.custom()
                    .headers(headers) //设置headers，不需要时则无需设置
                    .url(httpURL) //设置请求的url
                    .map(map)
                    //.json(parameterValues) //设置请求参数，没有则无需设置
                    .encoding("utf-8");//设置请求和返回编码，默认就是Charset.defaultCharset()
            System.out.println("config="+config);
            String actual = "";
            if (method.equals(HttpMethods.GET.getName())) {
                //使用方式：
                actual = HttpClientUtil.get(config);   //post请求
            } else if (method.equals(HttpMethods.POST.getName())) {
                //使用方式：
                actual = HttpClientUtil.post(config);   //post请求

            }

            System.out.println("[接口返回值]:" + actualToString(actual));

            interface_select_sql = trans2parameter(interface_select_sql,actual);

//            if (StringUtils.isNotBlank(method) && isCrateFile) {
//                /**
//                 * 数据库验证数据
//                 * 实际值 与预期值进行比较
//                 */
//                JdbcService.writeFiletoSql(httpURL,method+"-"+filename,".sql");
//            }

            //返回值非json格式,验证json格式
            if (JsonUtils.validate(expected)) {
                /***
                 * json格式比较
                 * 1,严格比较,包含值,结构完全一致
                 * 2,值一样,结构可以不一致
                 * String result = "{id:1,name:\"Juergen\"}";
                 *  JSONAssert.assertEquals("{id:1}", result, false); // Pass
                 *  JSONAssert.assertEquals("{id:1}", result, true); // Fail
                 */
                JSONAssert.assertEquals(expected, actual, false);
            } else {
                Assert.assertEquals(actual.toString(), expected.toString());
            }

            //数据验证

            boolean bool = JdbcService.exec_select(interface_select_sql);

            System.out.println("[验证接口数据是否正确]" + bool);
            Assert.assertEquals(bool, true);



        } catch (Exception e) {

            //异常处理,显示到reprot中
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } finally {

            /**
             * 删除数据表记录
             */
            // JdbcService._deleteRecord();



            //执行select -> del
            JdbcService.exec_delete(interface_select_sql);


            if (init) {
                String sql= getSql(script_sql);
                //删除初始化的数据
                JdbcService.exec_delete(sql);

            }

        }

    }

    public static String actualToString(Object actual) {

        String str = "";
        if (actual instanceof Map) {

            str = gson.toJson(actual);
        } else {
            str = actual.toString();
        }

        return str;
    }

    public static Map<String, String> mapStringToMap(String str) {
        str = str.substring(1, str.length() - 1);
        String[] strs = str.split(",");
        Map<String, String> map = new LinkedMap();
        for (String string : strs) {
            String key = string.split("=")[0].trim();
            String value = string.split("=")[1].trim();
            map.put(key, value);
        }
        return map;
    }

    /**
     *
     * 剔除delete 语句，保留初始化中insert 语句
     * @param initsql
     * @return
     */
    public static String getSql(String initsql){
        StringBuilder str=new StringBuilder("");
        String[] sql=initsql.split(";;");
        int num=0;
        for (int i = 0; i < sql.length; i++) {
            num++;
            if(sql[i].contains("Insert")){
                continue;
            }else {
                if(num==sql.length){
                    str.append(sql[i]);
                }else {
                    str.append(sql[i]).append(";;");
                }
            }
        }


        return str.toString();
    }


    public static String trans2parameter(String strSql,String response){
        List<String> paras = new LinkedList<String>();
        Pattern p = Pattern.compile("%([^%+])*%");
        Matcher m = p.matcher(strSql);
        while(m.find()){
            String mmm = m.group();
            paras.add(m.group().replaceAll("%",""));
        }
        //paras.clear();
        if(paras.size()>0){
            String [] respo = response.split(",");
            //Map<String,String> paraMap = new LinkedHashMap<String,String>();
            for(int j = 0;j < paras.size();j++){
                String paraKey = paras.get(j);
                String paraValue = "";
                for(int i = 0;i < respo.length;i++){
                    if(respo[i].contains(paraKey)){
                        String [] temp = respo[i].split(":");
                        for(int num = 0; num < temp.length ;num++){
                            if(temp[num].contains(paraKey)){
                                paraValue = temp[num+1].replaceAll("\"","");
                                paraValue = paraValue.replaceAll("}","");
                                paraValue = paraValue.replaceAll("]","");
                            }
                        }
                        break;
                    }
                }
                strSql = strSql.replaceAll("%"+paraKey+"%",paraValue);
            }
        }

        return strSql;
    }
}
