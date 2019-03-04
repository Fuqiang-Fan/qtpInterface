package com.testngScript.sample.testng;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.google.gson.Gson;
import com.testngScript.dubboservice.DubboGenericeServer;
import org.json.JSONObject;
import org.skyscreamer.jsonassert.JSONAssert;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Created by yaojie on 16/9/21.
 */
public class SampleDubboDemoTest {


    protected static final Logger logger = LoggerFactory.getLogger(SampleDubboDemoTest.class);

    private String dubbo_url;
    private String dubbo_interface;

    /**
     * <p>
     * description:注:@Parameters定义的参数顺序必须和方法的参数顺序一致,配置文件中的参数只是和注解的参数名称一致
     * </p>
     *
     * @param dubbo_url
     * @param dubbo_interface
     */
    @Parameters({ "dubbo_url", "dubbo_interface" })
    @BeforeMethod
    public void beforeTest(String dubbo_url, String dubbo_interface) {
        this.dubbo_url = dubbo_url;
        this.dubbo_interface = dubbo_interface;
    }

    /***
     *
     * @param method 接口方法
     * @param parameterTypes 参数类型
     * @param parameterValues 参数值
     * @param expected 预期值
     */
       @Test
       @Parameters({"method","parameterTypes","parameterValues","expected"})
        public  void testSayHello
               (String method,String parameterTypes,String parameterValues,String expected) throws Exception {
          try{
                       //实际返回值
                       DubboGenericeServer server=new DubboGenericeServer(dubbo_interface,dubbo_url);
                       Object actual=  server.invoke_dubboServcie(method, parameterTypes, parameterValues,null);


                       System.out.println("实际返回结果:" + actual.equals(expected));
                       //比较预期值,返回值
                       Assert.assertEquals(expected, actual);
               }catch (Exception e ){

                System.out.println(e);
            }

        }


    /***
     * 参数多个值,实际返回结果为对象
     * @param method
     * @param parameterTypes
     * @param parameterValues
     * @param expected
     * @throws Exception
     */
    @Test
    @Parameters({"method","parameterTypes","parameterValues","expected"})
    public void testgetPersonByParms(String method,String parameterTypes,String parameterValues,String expected)throws Exception{

        //实际返回值
        DubboGenericeServer server=new DubboGenericeServer(dubbo_interface,dubbo_url);
        Object actual=  server.invoke_dubboServcie(method, parameterTypes, parameterValues,null);

        System.out.println(new Gson().toJson(actual));



    }


    @Test
    @Parameters({"method","parameterTypes","parameterValues","expected"})
    public void testgetPerson(String method,String parameterTypes,String parameterValues,String expected) throws Exception {

        try{
            //实际返回值
            DubboGenericeServer server=new DubboGenericeServer(dubbo_interface,dubbo_url);
            Object actual=  server.invoke_dubboServcie(method, parameterTypes, parameterValues,null);
            logger.info(new Gson().toJson(actual));
            //实际返回结果是对象,需要进行转化成json格式
            JSONObject data =new JSONObject(new Gson().toJson(actual));
            //json格式是否严格配对, false不严格配对,
            /***
             * json格式比较
             * 1,严格比较,包含值,结构完全一致
             * 2,值一样,结构可以不一致
             * String result = "{id:1,name:\"Juergen\"}";
             *  JSONAssert.assertEquals("{id:1}", result, false); // Pass
             *  JSONAssert.assertEquals("{id:1}", result, true); // Fail
             */
            JSONAssert.assertEquals(expected, data, false);
        }catch (Exception e ){

            System.out.println(e);
        }


    }


    @Test
    @Parameters({"method","parameterTypes","parameterValues","expected"})
    public void testgetPersonByObject(String method,String parameterTypes,String parameterValues,String expected) throws Exception {
        try{
                DubboGenericeServer server=new DubboGenericeServer(dubbo_interface,dubbo_url);
                Object actual=  server.invoke_dubboServcie(method, parameterTypes, parameterValues,null);
                logger.info(new Gson().toJson(actual));
                //实际返回结果是对象,需要进行转化成json格式
                JSONObject data =new JSONObject(new Gson().toJson(actual));
                //json格式是否严格配对, false不严格配对,
                /***
                 * json格式比较
                 * 1,严格比较,包含值,结构完全一致
                 * 2,值一样,结构可以不一致
                 * String result = "{id:1,name:\"Juergen\"}";
                 *  JSONAssert.assertEquals("{id:1}", result, false); // Pass
                 *  JSONAssert.assertEquals("{id:1}", result, true); // Fail
                 */
                JSONAssert.assertEquals(expected, data, false);
            }catch (Exception e ){

                System.out.println(e);
            }
    }

    @Test
    @Parameters({"method","parameterTypes","parameterValues","expected"})
    public void testgetPersonByMap(String method,String parameterTypes,String parameterValues,String expected) throws Exception {

                try{
                DubboGenericeServer server=new DubboGenericeServer(dubbo_interface,dubbo_url);
                Object actual=  server.invoke_dubboServcie(method, parameterTypes, parameterValues,null);
                logger.info(new Gson().toJson(actual));
                //实际返回结果是对象,需要进行转化成json格式
                JSONObject data =new JSONObject(new Gson().toJson(actual));
                //json格式是否严格配对, false不严格配对,
                /***
                 * json格式比较
                 * 1,严格比较,包含值,结构完全一致
                 * 2,值一样,结构可以不一致
                 * String result = "{id:1,name:\"Juergen\"}";
                 *  JSONAssert.assertEquals("{id:1}", result, false); // Pass
                 *  JSONAssert.assertEquals("{id:1}", result, true); // Fail
                 */
                JSONAssert.assertEquals(expected, data, false);

            }catch (Exception e ){

                System.out.println(e);
            }
    }

    @Test
    @Parameters({"method","parameterTypes","parameterValues","expected"})
    public void testgetPersonByListObject(String method,String parameterTypes,String parameterValues,String expected) throws Exception {

        try{
            DubboGenericeServer server=new DubboGenericeServer(dubbo_interface,dubbo_url);
            Object actual=  server.invoke_dubboServcie(method, parameterTypes, parameterValues,null);

            System.out.println("返回值:"+new Gson().toJson(actual));
            //实际返回结果是对象,需要进行转化成json格式

            //json格式是否严格配对, false不严格配对,
            /***
             * json格式比较
             * 1,严格比较,包含值,结构完全一致
             * 2,值一样,结构可以不一致
             * String result = "{id:1,name:\"Juergen\"}";
             *  JSONAssert.assertEquals("{id:1}", result, false); // Pass
             *  JSONAssert.assertEquals("{id:1}", result, true); // Fail
             */




        }catch (Exception e ){
            e.printStackTrace();

            System.out.println(e);
        }
    }
}
