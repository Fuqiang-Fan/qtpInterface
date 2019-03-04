package com.testngScript.sample.testng;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.rpc.service.GenericService;
import com.google.gson.Gson;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


public class ApplicationDubboTests {

  private static  Logger logger = LoggerFactory.getLogger(ApplicationDubboTests.class);



    //dubbo 提供者 服务接口地址 172.16.136.198
    String dubbo_url="dubbo://127.0.0.1:20880";
	//dubbo 接口名称
	String dubbo_interface="com.alibaba.dubbo.demo.DemoService";

	//实际返回结果
	Object  result;

	//预期值
	Object  expected;

	//接口方法
	String method ;

	//接口方法参数类型
	String[] parameterTypes=new String[]{} ;

	//接口方法参数值
	Object[] parameterValues=new Object[]{};


	//dubbo服务 实例
	GenericService genericService;
	ReferenceConfig<GenericService> reference ;
	Gson gson=new Gson();
	/**
	 *初始化dubbo 服务
	 */
	@Before
	public void init(){
		reference = new ReferenceConfig<GenericService>(); // 该实例很重量，里面封装了所有与注册中心及服务提供方连接，请缓存
		reference.setInterface(dubbo_interface); // 弱类型接口名
		reference.setGeneric(true); // 声明为泛化接口
		reference.setUrl(dubbo_url);
		reference.setApplication(new ApplicationConfig("dubbo"));
		// 用com.alibaba.dubbo.rpc.service.GenericService可以替代所有接口引用
		genericService = reference.get();
	}

	/**
	 * String sayHello(String name);
	 boolean sayHello(boolean bool);
	 byte sayHello(byte b);
	 char sayHello(char c);
	 int sayHello(int i);
	 float sayHello(float f);
	 double sayHello(double d);
	 long sayHello(long l);

	 String[] sayHellos(String name);
	 */
	@Test
	public void testSayHello(){
		//方法名称
		method="sayHello";
		//参数类型
		parameterTypes=new String[]{"java.lang.String"};
		//参数值
		parameterValues=new Object[]{"张三"};
		//预期值
		expected="Hello:张三";
		//执行调用接口,并返回实际结果
		result= genericService.$invoke(method, parameterTypes, parameterValues);
        //比较预期值,返回值
		org.junit.Assert.assertEquals(expected,result);
	}
	@Test
	public void testSayHelloboolean(){
		//方法名称
		method="sayHello";
		//参数类型
		parameterTypes= new String[]{"boolean"};
		//参数值
		parameterValues=new Object[]{false};
		//预期值
		expected=false;
		//执行调用接口,并返回实际结果
		result= genericService.$invoke(method, parameterTypes, parameterValues);
		//比较预期值,返回值
		org.junit.Assert.assertEquals(expected,result);
	}
   @Test
	public void testSayHellobyte(){
		//方法名称
		method="sayHello";
		//参数类型
		parameterTypes= new String[]{"byte"};
		byte abyte ='a';
		//参数值
		parameterValues=new Object[]{abyte};
		//预期值
		expected=97;
		//执行调用接口,并返回实际结果
		result= genericService.$invoke(method, parameterTypes, parameterValues);
		//比较预期值,返回值
		org.junit.Assert.assertEquals(expected,result);
	}

	/**
	 * 测试返回结果为数组
	 */
	@Test
	public void testSayHelloarray(){
		//方法名称
		method="sayHellos";
		//参数类型
		parameterTypes= new String[]{"java.lang.String"};
		//参数值
		parameterValues=new Object[]{"test"};
		//预期值 数组类型
		String[] expected=new String[]{"test"};
		//执行调用接口,并返回实际结果,
		result= genericService.$invoke(method, parameterTypes, parameterValues);

		/****
		 * 接口返回值是数组
		 */
		//比较预期值,返回值
		org.junit.Assert.assertArrayEquals(expected,(Object[])result);
	}


	@Test
	public void testgetPerson() throws Exception {

		method="getPerson";
		parameterTypes= new String[]{};
		parameterValues=new Object[]{};

		//预期值
		expected="{\"name\":\"张三\",\"amout\":200,\"bigDecimal\":3000,\"message\":\"返回成功\"}";

		//执行调用接口
		result= genericService.$invoke(method, parameterTypes, parameterValues);

		//实际返回结果是对象,需要进行转化成json格式
		JSONObject data =new JSONObject(gson.toJson(result));
		logger.info("实际返回值:" ,gson.toJson(result));

       //json格式是否严格配对, false不严格配对,
		/***
		 * json格式比较
		 * 1,严格比较,包含值,结构完全一致
		 * 2,值一样,结构可以不一致
		 * String result = "{id:1,name:\"Juergen\"}";
		 *  JSONAssert.assertEquals("{id:1}", result, false); // Pass
		 *  JSONAssert.assertEquals("{id:1}", result, true); // Fail
		 */
		JSONAssert.assertEquals(expected.toString(), data, false);

	}



	@Test
	public void testgetPersonByParms(){

		//测试接口参数 ,为基本类型的数组
		method="getPersonByParms";
		parameterTypes= new String[]{"java.lang.String","java.lang.String[]"};

		parameterValues=new Object[]{"随行付",new String[]{"随行付A","随行付B"}};

		System.out.println(Arrays.toString(parameterValues));

		System.out.println(gson.toJson(parameterValues));


		result= genericService.$invoke(method, parameterTypes, parameterValues);

		System.out.println(gson.toJson(result));
	}


	@Test
	public void testgetPersonArray(){

		/**
		 * 参数为对象数组
		 */
		method="getPersonArrays";
		parameterTypes= new String[]{"java.lang.String","com.alibaba.dubbo.demo.Person[]"};

		List<Map<String, Object>> lst =new ArrayList();

		Map<String, Object> person = new HashMap<String, Object>();
		person.put("name", "xxx");
		person.put("password", "你密码");
		person.put("calss", "com.alibaba.dubbo.demo.Person");
		lst.add(person);
		Map<String, Object> person1 = new HashMap<String, Object>();
		person1.put("name", "xxx");
		person1.put("password", "你密码");
		person1.put("calss", "com.alibaba.dubbo.demo.Person");
		lst.add(person1);


		//接口 输入值
		parameterValues=new Object[]{"随行付",lst.toArray()};


		result= genericService.$invoke(method, parameterTypes, parameterValues);



		System.out.println(gson.toJson(result));
	}


	@Test
	public void testResultObject(){
		// 用Map表示POJO参数，如果返回值为POJO也将自动转成Map
		Map<String, Object> person = new HashMap<String, Object>();
		person.put("name", "xxx");
		person.put("password", "你密码");
		result= genericService.$invoke("getPersonList", new String[]{"com.alibaba.dubbo.demo.Person"}, new Object[]{person}); // 如果返回POJO将自动转成Map
		System.out.println(gson.toJson(result));
		reference.destroy();

	}

	@Test
	public void testResultList(){
		Map<String, Object> person = new HashMap<String, Object>();
		person.put("name", "abc");
		person.put("password", "abc密码");
		result= genericService.$invoke("getPersonByPerson" , new String[]{"com.alibaba.dubbo.demo.Person"}, new Object[]{person}); // 如果返回POJO将自动转成Map
		System.out.println(gson.toJson(result));
	}

	/**
	 * Map<String,User> getMap(Map<String,User> map)
	 Map<String,Object> getMap1(String value);
	 Map<String,Object> getHashMap(Map<String,Object> map);
	 */
	@Test
	public void testgetHashMap(){
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("name", "getHashMap");
		map.put("password", "getHashMap密码");

		System.out.println(gson.toJson(map));
		result= genericService.$invoke("getHashmap", new String[]{"java.util.Map"}, new Object[]{map});
		System.out.println(gson.toJson(result));

	}


	@Test
	public void testgetHashMapUser() throws Exception{
		String json="{\"name\":\"马33333\"}";
		Object o =gson.fromJson(json,Object.class);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("name", o);
		result= genericService.$invoke("getMap", new String[]{"java.util.Map"}, new Object[]{map});
		System.out.println(gson.toJson(result));
	}

	@Test
	public void getPersonLists() throws Exception{
		List<Map<String, Object>> users = new ArrayList<Map<String, Object>>();
		Map<String, Object> user = new HashMap<String, Object>();
		user.put("class", "com.alibaba.dubbo.demo.Person");
		user.put("name", "actual.provider");
		users.add(user);

		Map<String, Object> user1 = new HashMap<String, Object>();
		user1.put("class", "com.alibaba.dubbo.demo.Person");
		user1.put("name", "actual.provider");

		users.add(user1);

		System.out.println(gson.toJson(users));


		result = (List<Map<String, Object>>) genericService.$invoke("getPersonLists", new String[]{List.class.getName()},
				new Object[]{users});

		System.out.println(gson.toJson(result));


	}


	/****
	 * json 比较
	 * @param expectedjson 预期值json
	 * @param result 实际返回值
	 * @param bool 严格与否,顺序不重要
	 * String result = "{id:1,name:\"Juergen\"}";
	 *  JSONAssert.assertEquals("{id:1}", result, false); // Pass
	 *  JSONAssert.assertEquals("{id:1}", result, true); // Fail
     */
	public  void compare(String expectedjson,Object result ,boolean bool) throws  Exception{
		String resultjson =gson.toJson(result);
		JSONObject data =new JSONObject(resultjson);
		JSONAssert.assertEquals(expectedjson, data, bool);
	}

}
