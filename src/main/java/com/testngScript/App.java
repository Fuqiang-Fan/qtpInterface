package com.testngScript;

import com.google.gson.Gson;
import com.testngScript.dubboservice.DubboGenericeServer;
import com.testngScript.jdbc.JdbcDao;
import com.testngScript.jdbc.JdbcService;
import com.testngScript.util.JsonUtils;
import com.testngScript.util.WriteUtils;
import org.apache.commons.collections.map.LinkedMap;
import org.skyscreamer.jsonassert.JSONAssert;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yaojie on 16/10/21.
 */

public class App {
    private String dubbo_url;
    private String dubbo_interface;

    static Gson gson = new Gson();

    /**
     * <p>
     * description:注:@Parameters定义的参数顺序必须和方法的参数顺序一致,配置文件中的参数只是和注解的参数名称一致
     * </p>
     */
    @Parameters({"dubbo_url", "dubbo_interface"})
    @BeforeMethod
    public void beforeTest(String dubbo_url, String dubbo_interface) {
        this.dubbo_url = dubbo_url;
        this.dubbo_interface = dubbo_interface;

    }


    @Test(testName = "ddd",groups = {"test"})
    @Parameters({"dubbo_url", "dubbo_interface","method", "parameterTypes", "parameterValues", "expected", "init", "script_sql", "isCrateFile", "filename", "interface_select_sql","interface_sql","null_select_sql","useFileValue","version"})
    public void run
            (String dubbo_url, String dubbo_interface,String method, String parameterTypes, String parameterValues,
             String expected, boolean init, String script_sql, String isCrateFile, String filename, String interface_select_sql,@Optional Boolean interface_sql,@Optional String null_select_sql,@Optional String useFileValue,@Optional String version) throws Exception {
        boolean init1=init;
        String script_sql1=script_sql;

        Boolean interface_sql1=interface_sql;
        String   interface_select_sql1=interface_select_sql;
        if (null == null_select_sql){
            null_select_sql = "";
        }
        if (null == useFileValue){
            useFileValue = "";
        }

        if (null != dubbo_url){
            this.dubbo_url = dubbo_url;
        }

        if (null != dubbo_interface){
            this.dubbo_interface = dubbo_interface;
        }

        try {
            //System.out.println("init:"+init);
            parameterValues = parameterValues.replaceAll("'", "\"");
            parameterValues = parameterValueTrans2(parameterValues);
            //parameterValues = parameterValues.replaceAll("#Random#",getStringRandom(10));
            expected = expected.replaceAll("'", "\"");

            if(useFileValue.length()>0){ //如果输入不为空，认为需要取记录文件中的结果

                parameterValues=JsonUtils.returnJson(useFileValue, parameterValues);
            }


            if (init1) {
                System.out.println(">>>>>>>>初始化数据>>>>>>>>>>>>");
                System.out.println(script_sql1);
                JdbcDao.excuteTosql(script_sql1);

            }


            DubboGenericeServer server = new DubboGenericeServer(dubbo_interface, dubbo_url);
            Object actual=null;
            if(parameterTypes.length()<=1||parameterValues.length()<=1){
                actual = server.$invoke(method,null,null,version);
            }else{
                actual = server.invoke_dubboServcie(method, parameterTypes, parameterValues,version);
            }

            System.out.println("[接口返回值]:" + actualToString(actual));

            //转换sql中的变量（%%）从返回值中拿到变量赋值进去
            String response = actualToString(actual);
            interface_select_sql = trans2parameter(interface_select_sql,response);
            null_select_sql = trans2parameter(null_select_sql,response);


            //isCrateFile为true 可以存接口返回值
            if(isCrateFile.length()>0){
                WriteUtils.writeProperties(isCrateFile,actualToString(actual));
            }
//
//            if (StringUtils.isNotBlank(method) && isCrateFile) {
//                /**
//                 * 数据库验证数据
//                 * 实际值 与预期值进行比较
//                 */
//                JdbcService.writeFiletoSql(dubbo_interface, method + "-" + filename, ".sql");
//            }

//            //返回值非json格式,验证json格式
            if (JsonUtils.validate(expected)) {
                /***
                 * json格式比较
                 * 1,严格比较,包含值,结构完全一致
                 * 2,值一样,结构可以不一致
                 * String result = "{id:1,name:\"Juergen\"}";
                 *  JSONAssert.assertEquals("{id:1}", result, false); // Pass
                 *  JSONAssert.assertEquals("{id:1}", result, true); // Fail
                 */
                if(actualToString(actual).length()==6){
                    System.out.println("验证码！！！！");
                }else{
                    JSONAssert.assertEquals(expected, actualToString(actual), false);
                }
            } else {
                if (actual instanceof Map) {
                    Map expectedMap = mapStringToMap(expected.trim());
                    JSONAssert.assertEquals(gson.toJson(expectedMap), gson.toJson(actual), false);
                } else {
                    Assert.assertEquals(actual.toString(), expected.toString());

                }
            }


            if(null==interface_sql||interface_sql==true){
                System.out.println(">>>>>>>>验证sql是否有数据>>>>>>>>>>>>");
                boolean  bool = JdbcService.exec_count(interface_select_sql);
/*            System.out.println(">>>>>>>>验证sql有参数的校验是否有数据>>>>>>>>>>>>");
            boolean  boolpara = JdbcService.exec_count(interface_select_sql);*/
                System.out.println(">>>>>>>>验证sql是否无数据>>>>>>>>>>>>");
                boolean  boolnull = JdbcService.exec_countnull(null_select_sql);
                if(bool&&boolnull){	//	当验证sql都正确才认为验证通过
                    bool=true;
                }else{
                    bool=false;
                }
                System.out.println("[验证接口数据是否正确]" + bool);
                Assert.assertEquals(bool, true);

            }




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

            if (init1) {
                String sql=HttpClientApp.getSql(script_sql1);
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

    //生成随机数字和字母,
    public static String getStringRandom(int length) {

        String val = "";
        Random random = new Random();

        //参数length，表示生成几位随机数
        for(int i = 0; i < length; i++) {

            String charOrNum = random.nextInt(2) % 2 == 0 ? "char" : "num";
            //输出字母还是数字
            if( "char".equalsIgnoreCase(charOrNum) ) {
                //输出是大写字母还是小写字母
                int temp = random.nextInt(2) % 2 == 0 ? 65 : 97;
                val += (char)(random.nextInt(26) + temp);
            } else if( "num".equalsIgnoreCase(charOrNum) ) {
                val += String.valueOf(random.nextInt(10));
            }
        }
        return val;
    }
    //根据正则取符合要求的组成list
    public static List<String> getSubUtil(String soap,String rgex){
        List<String> list = new ArrayList<String>();
        Pattern pattern = Pattern.compile(rgex);// 匹配的模式
        Matcher m = pattern.matcher(soap);
        while (m.find()) {
            int i = 1;
            list.add(m.group(i));
            i++;
        }
        return list;
    }
    public static String parameterValueTrans2(String para){
        para = para.replaceAll("#Today#",getToday());
        para = para.replaceAll("#Now#",getNow());
        int randomNum = 0;
        List<String> list = getSubUtil(para,"#(.*?)#");
        for(String s:list){
            if(s.startsWith("Random_")){
                randomNum = Integer.parseInt(s.substring(s.indexOf("_")+1));
                System.out.println(randomNum);
                String random = getStringRandom(randomNum);
                para = para.replaceAll("#"+s+"#",random);
            }

        }
        return para;
    }

    public static String trans2parameter(String strSql,String response){
        strSql = strSql.replaceAll("#Today#",getToday());
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



    public static String getToday(){
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");//设置日期格式
        return df.format(new Date());
    }

    public static String getNow(){
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");//设置日期格式
        System.out.println(df);
        return df.format(new Date());
    }
}
