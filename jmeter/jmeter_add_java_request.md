新建maven工程

public Arguments getDefaultParameters();设置可用参数及的默认值；
public void setupTest(JavaSamplerContext arg0)：每个线程测试前执行一次，做一些初始化工作；
public SampleResult runTest(JavaSamplerContext arg0)：开始测试，从arg0参数可以获得参数值；
public void teardownTest(JavaSamplerContext arg0)：测试结束时调用；

```java
package com.dubbo.test;

public class Hello {
    public String sayHello()
    {
        return "Hello";
    }
    public String sayHelloToPerson(String s)
    {
        if(s == null || s.equals(""))
            s = "nobody";
        return (new StringBuilder()).append("Hello ").append(s).toString();
    }
    public int sum(int a,int b)
    {
        return a+b;
    }
}
```

```java
package com.dubbo.test;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import com.dubbo.test.Hello;

public class App extends AbstractJavaSamplerClient {
	private String a;
	private String b;
	/** Holds the result data (shown as Response Data in the Tree display). */
	private String resultData;

	// 这个方法是用来自定义java方法入参的。
	// params.addArgument("num1","");表示入参名字叫num1，默认值为空。
	// 设置可用参数及的默认值；
	public Arguments getDefaultParameters() {
		Arguments params = new Arguments();
		params.addArgument("num1", "");
		params.addArgument("num2", "");
		return params;
	}

	// 每个线程测试前执行一次，做一些初始化工作；
	public void setupTest(JavaSamplerContext arg0) {
	}

	// 开始测试，从arg0参数可以获得参数值；
	public SampleResult runTest(JavaSamplerContext arg0) {
		a = arg0.getParameter("num1");
		b = arg0.getParameter("num2");
		SampleResult sr = new SampleResult();
		sr.setSampleLabel("Java请求哦");
		try {
			sr.sampleStart();// jmeter 开始统计响应时间标记
			Hello test = new Hello();
			// 通过下面的操作就可以将被测方法的响应输出到Jmeter的察看结果树中的响应数据里面了。
			resultData = String.valueOf(test.sum(Integer.parseInt(a), Integer.parseInt(b)));
			if (resultData != null && resultData.length() > 0) {
				sr.setResponseData("结果是：" + resultData, null);
				sr.setDataType(SampleResult.TEXT);
			}
			// System.out.println(resultData);
			sr.setSuccessful(true);
		} catch (Throwable e) {
			sr.setSuccessful(false);
			e.printStackTrace();
		} finally {
			sr.sampleEnd();// jmeter 结束统计响应时间标记
		}
		return sr;
	}

	// 测试结束时调用；
	public void teardownTest(JavaSamplerContext arg0) {
		// System.out.println(end);
		// System.out.println("The cost is"+(end-start)/1000);
	}

	// main只是为了调试用，最后打jar包的时候注释掉。
	/*
	 * public static void main(String[] args) { // TODO Auto-generated method
	 * stub Arguments params = new Arguments(); params.addArgument("num1",
	 * "1");//设置参数，并赋予默认值1 params.addArgument("num2", "2");//设置参数，并赋予默认值2
	 * JavaSamplerContext arg0 = new JavaSamplerContext(params); perftestbbb
	 * test = new perftestbbb(); test.setupTest(arg0); test.runTest(arg0);
	 * test.teardownTest(arg0); }
	 */
}
```

pom.xml 添加
```
<dependency>
  <groupId>org.apache.jmeter</groupId>
  <artifactId>ApacheJMeter_core</artifactId>
  <version>3.2</version>
</dependency>
<!-- https://mvnrepository.com/artifact/org.apache.jmeter/ApacheJMeter_java -->
<dependency>
  <groupId>org.apache.jmeter</groupId>
  <artifactId>ApacheJMeter_java</artifactId>
  <version>3.2</version>
</dependency>
```

mvn clean package

将打出打jar放到D:\apache-jmeter\lib\ext下

重新启动jmeter 即可使用

![java](jmeter_java.png)



