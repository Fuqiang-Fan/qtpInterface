package com.testngScript.dubboservice;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.utils.ReferenceConfigCache;
import com.alibaba.dubbo.rpc.service.GenericException;
import com.alibaba.dubbo.rpc.service.GenericService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by yaojie on 16/9/21.
 */
public class DubboGenericeServer {

    String dubbo_interface;
    String dubbo_url;

    public DubboGenericeServer(String dubbo_interface, String dubbo_url) {
        this.dubbo_interface = dubbo_interface;
        this.dubbo_url = dubbo_url;
    }


    private DubboGenericeServer() {
    }

    public Object $invoke(String method, String[] parameterTypes, Object[] args,String version) throws GenericException {
        ReferenceConfig<GenericService> reference = new ReferenceConfig<GenericService>(); // 该实例很重量，里面封装了所有与注册中心及服务提供方连接，请缓存
        reference.setInterface(dubbo_interface); // 弱类型接口名
        reference.setGeneric(true); // 声明为泛化接口
        reference.setUrl(dubbo_url);
        reference.setApplication(new ApplicationConfig("dubbo"));
        reference.setTimeout(50000);
//        Map map = new HashMap();
//        map.put("connect.timeout","15000");
//        reference.setParameters(map);

        if (StringUtils.isNotEmpty(version)){
            reference.setVersion(version);
        }
   //
        // ReferenceConfigCache cache = ReferenceConfigCache.getCache();
        GenericService genericService = reference.get();
        Object object = genericService.$invoke(method, parameterTypes, args);
        //销毁
        reference.destroy();
        return object;
    }

    public Object invoke_dubboServcie(String method, String parameterTypes, String inputJson, String  version) throws Exception {

        Object[] args = transFrom(inputJson, parameterTypes);
        String[] paramTypes = new String[]{};

        if (StringUtils.isNotEmpty(parameterTypes)) {
            paramTypes = parameterTypes.split(",");
            for (int i = 0; i < paramTypes.length; i++) {
                paramTypes[i] = paramTypes[i].replaceAll("class:", "");
            }
        }
        Object json = $invoke(method, paramTypes, args,version);
        return json;

    }

    public static Object invoke_dubboServcie(String dubbo_interface, String dubbo_url,
                                             String method, String parameterTypes, String inputJson) throws Exception {
        ReferenceConfig<GenericService> reference = new ReferenceConfig<GenericService>(); // 该实例很重量，里面封装了所有与注册中心及服务提供方连接，请缓存
        reference.setInterface(dubbo_interface); // 弱类型接口名
        reference.setGeneric(true); // 声明为泛化接口
        reference.setUrl(dubbo_url);
        reference.setApplication(new ApplicationConfig("dubbo"));
        reference.setTimeout(50000);
        // 用com.alibaba.dubbo.rpc.service.GenericService可以替代所有接口引用
        // GenericService genericService = reference.get();
        ReferenceConfigCache cache = ReferenceConfigCache.getCache();
        GenericService genericService = cache.get(reference);

        Object[] args = transFrom(inputJson, parameterTypes);
        String[] paramTypes = new String[]{};

        if (StringUtils.isNotEmpty(parameterTypes)) {
            paramTypes = parameterTypes.split(",");
            for (int i = 0; i < paramTypes.length; i++) {
                paramTypes[i] = paramTypes[i].replaceAll("class:", "");
            }
        }
        //执行调用接口,并返回实际结果
        Object result = genericService.$invoke(method, paramTypes, args);
        return result;

    }


    /***
     *
     *
     * @param inputvalue
     * @param parameterTypes
     * @return
     * @throws Exception
     */
    public static Object[] transFrom(String inputvalue, String parameterTypes) throws Exception {
        Gson gson = new Gson();

        if (StringUtils.isEmpty(parameterTypes) && StringUtils.isEmpty(inputvalue)) {
            return new Object[]{};
        }
        //参数类型
        String[] parameterType = parameterTypes.split(",");
        //参数值
        String[] args = inputvalue.split("#");
        //输入参数,输入值 个数必须相等
        org.testng.Assert.assertEquals(parameterType.length, args.length);

        Object[] objects = new Object[args.length];

        for (int i = 0; i < parameterType.length; i++) {
            String temp = parameterType[i];
            if (temp.equals("java.lang.String")) {
                objects[i] = args[i];
            } else if (temp.contains("[]")) {
                String[] tmp = args[i].replaceAll("\\[", "").replaceAll("\\]", "").split(",");
                Object[] o = new Object[temp.length()];
                for (int j = 0; j < tmp.length; j++) {
                    o[j] = tmp[j];
                }
                objects[i] = o;
            } else if (temp.contains("java.util.List")) {
                ArrayList arrayList = jsonToArrayList(inputvalue, Object.class);
                objects[i] = arrayList;
            } else if (temp.contains("java.util.Map") || temp.contains("class:")) {
                inputvalue = args[i];
                Map<String, Object> retMap2 = gson.fromJson(inputvalue,
                        new TypeToken<Map<String, Object>>() {
                        }.getType());
                retMap2.remove("class");
                objects[i] = retMap2;
            } else {
                objects[i] = args[i];
            }
        }
        return objects;
    }

    public static <T> ArrayList<T> jsonToArrayList(String json, Class<T> clazz) {
        Type type = new TypeToken<ArrayList<JsonObject>>() {
        }.getType();
        ArrayList<JsonObject> jsonObjects = new Gson().fromJson(json, type);

        ArrayList<T> arrayList = new ArrayList();
        for (JsonObject jsonObject : jsonObjects) {
            arrayList.add(new Gson().fromJson(jsonObject, clazz));
        }
        return arrayList;
    }


}
