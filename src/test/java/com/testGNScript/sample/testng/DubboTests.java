package com.testGNScript.sample.testng;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.config.utils.ReferenceConfigCache;
import com.alibaba.dubbo.rpc.service.GenericService;
import com.google.gson.Gson;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yaojie on 16/9/20.
 */

public class DubboTests {


    @Test//(invocationCount=20,threadPoolSize = 5)
    public void test(){

        long id = Thread.currentThread().getId();
        System.out.println("After test-method. Thread id is: " + id);

        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setName("dubbo");//prop.getProperty("application.name")

        //这里配置了dubbo的application信息*(demo只配置了name)*，因此demo没有额外的dubbo.xml配置文件
        RegistryConfig registryConfig = new RegistryConfig();
        //registryConfig.setAddress("zookeeper://172.16.135.139:2181");//(prop.getProperty("registry.address"));

        //这里配置dubbo的注册中心信息，因此demo没有额外的dubbo.xml配置文件
        //dubbo://172.16.136.182:20100/com.lemon.rcs.dubbo.MercRiskInfoDubbo
        ReferenceConfig<GenericService> reference1 = new ReferenceConfig<GenericService>(); // 该实例很重量，里面封装了所有与注册中心及服务提供方连接，请缓存
        reference1.setInterface("com.suixingpay.service.external.system.bmp.ObsBattchService"); // 弱类型接口名
        reference1.setGeneric(true); // 声明为泛化接口

        reference1.setUrl("dubbo://172.16.136.125:20880");
        reference1.setApplication(applicationConfig);

         reference1.setRegistry(registryConfig);


        ReferenceConfigCache cache = ReferenceConfigCache.getCache();
        GenericService genericService1 =  cache.get(reference1);

        Map<String, Object> paramMap =new HashMap();

        paramMap.put("mercSn","111");

       //Object result= genericService1.$invoke("getPage", new String[]{"java.util.Map"}, new Object[]{paramMap});



        Object result= genericService1.$invoke("putInStorage", new String[]{"java.lang.String","java.lang.String","java.lang.String"},
                new Object[]{"700029","001",String.valueOf(System.currentTimeMillis())});
        Gson gson =new Gson();
        System.out.println(result.toString() );



    }
}
