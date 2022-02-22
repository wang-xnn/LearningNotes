log4j slf4j

#### 三级缓存

首先是从一级缓存中去拿bean，当拿不到bean时，且当前bean正在被创建，就会从二级缓存中去获取，当二级拿不到时，从三级缓存中获取factory对象，如果拿到了factory对象，会从factory中去获取bean，最后将该bean放入二级缓存中

![循环依赖.png](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/271af8606b794d5f9bcd04a227459925~tplv-k3u1fbpfcp-watermark.awebp)

假设A和B相互依赖，当创建A到了组装这一步骤时，需要去组装B，这个时候就会去getB，但是get不到，就会去创建B，当创建B到组装这一步骤时，需要去组装A，而A这个bean已经创建过了，并且在缓存中，可以得到A，这个时候B就组装完成了，而当B组装完成了，相应的A也组装完成

1级缓存是缓存的完整bean对象，2级缓存存储的是不完整的bean,只经过实例化的普通对象，3级缓存存储的是bean factory

A 和 B循环依赖

```Java
protected <T> T doGetBean(String name, @Nullable Class<T> requiredType, @Nullable Object[] args, boolean typeCheckOnly) throws BeansException {
        String beanName = this.transformedBeanName(name);
        Object sharedInstance = this.getSingleton(beanName);
        Object bean;
        if (sharedInstance != null && args == null) {
            if (this.logger.isTraceEnabled()) {
                if (this.isSingletonCurrentlyInCreation(beanName)) {
                    this.logger.trace("Returning eagerly cached instance of singleton bean '" + beanName + "' that is not fully initialized yet - a consequence of a circular reference");
                } else {
                    this.logger.trace("Returning cached instance of singleton bean '" + beanName + "'");
                }
            }

            bean = this.getObjectForBeanInstance(sharedInstance, name, beanName, (RootBeanDefinition)null);
        } else {
            if (this.isPrototypeCurrentlyInCreation(beanName)) {
                throw new BeanCurrentlyInCreationException(beanName);
            }

```



- getBean(A)    执行doGetBean(A)方法，先执行getSingleton(),从一级缓存中获取，获取不到

```Java
@Nullable
protected Object getSingleton(String beanName, boolean allowEarlyReference) {
    //从一级缓存中找
    Object singletonObject = this.singletonObjects.get(beanName);
    //一级缓存找不到并且这个对象处于正在创建中
    if (singletonObject == null && this.isSingletonCurrentlyInCreation(beanName)) {
        //
        synchronized(this.singletonObjects) {
            //从二级缓存中找
            singletonObject = this.earlySingletonObjects.get(beanName);
            //如果二级缓存也找不到
            if (singletonObject == null && allowEarlyReference) {
                //从三级缓存beanFactory 创建一个bean对象，放入二级缓存中，删除三级缓存的beanFactory
                ObjectFactory<?> singletonFactory = (ObjectFactory)this.singletonFactories.get(beanName);
                if (singletonFactory != null) {
                    singletonObject = singletonFactory.getObject();
                    this.earlySingletonObjects.put(beanName, singletonObject);
                    this.singletonFactories.remove(beanName);
                }
            }
        }
    }

    return singletonObject;
}
```

- 用beforeSingletonCreation()方法标记这个对象正在创建
- 执行doCreateBean方法，实例化A对象，通过addSingletonFactory()方法把beanFactory放入三级缓存中

```Java
protected void addSingletonFactory(String beanName, ObjectFactory<?> singletonFactory) {
    Assert.notNull(singletonFactory, "Singleton factory must not be null");
    synchronized(this.singletonObjects) {
        if (!this.singletonObjects.containsKey(beanName)) {
            this.singletonFactories.put(beanName, singletonFactory);
            this.earlySingletonObjects.remove(beanName);
            this.registeredSingletons.add(beanName);
        }

    }
}
```

- popolateBean 组装这个对象的属性，发现需要B的bean对象注入
- 执行doGetBean方法从一级缓存中找B，没有，把b标记为正在创建状态，执行doCreateBean方法，创建b的实例化对象，把b的bean factory放入三级缓存中，组装B的属性，发现需要A对象，doGetBean、getSingleton发现这个a对象不仅一级缓存中没有，而且处于正在创建状态，从二级缓存中找，还是没有，于是执行beanFactory，创建一个a的bean对象放入二级缓存中，并且注入到b对象中，放入一级缓存，再把b注入到a对象中

https://segmentfault.com/a/1190000040227141

1. 实例化，简单理解就是new了一个对象
2. 属性注入，为实例化中new出来的对象填充属性
3. 初始化，执行aware接口中的方法，初始化方法，完成`AOP`代理



#### @Async

SpringBoot 使用异步注解，需要在启动类上添加@EnableAsync来开启异步调用  ，在需要执行异步调用的方法上添加@Async("taskExectutor")注解标注，可以配置一个线程池taskExectutor对象

```java
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Async {

    /**
     * A qualifier value for the specified asynchronous operation(s).
     * <p>May be used to determine the target executor to be used when executing this
     * method, matching the qualifier value (or the bean name) of a specific
     * {@link java.util.concurrent.Executor Executor} or
     * {@link org.springframework.core.task.TaskExecutor TaskExecutor}
     * bean definition.
     * <p>When specified on a class level {@code @Async} annotation, indicates that the
     * given executor should be used for all methods within the class. Method level use
     * of {@code Async#value} always overrides any value set at the class level.
     * @since 3.1.2
     */
    String value() default "";

}
```

```java
@Configuration
public class AsyncConfig {

    private static final int MAX_POOL_SIZE = 50;

    private static final int CORE_POOL_SIZE = 20;

    @Bean("taskExecutor")
    public AsyncTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setMaxPoolSize(MAX_POOL_SIZE);
        taskExecutor.setCorePoolSize(CORE_POOL_SIZE);
        taskExecutor.setThreadNamePrefix("async-task-thread-pool");
        taskExecutor.initialize();
        return taskExecutor;
    }
}
```

带有返回值的Future<T>，需要用到AsyncResult

自调用的情况跟@Transactional差不多，因为本质都是使用aop代理的

Spring 已经实现的异常线程池：
1. SimpleAsyncTaskExecutor：不是真的线程池，这个类不重用线程，每次调用都会创建一个新的线程。
2. SyncTaskExecutor：这个类没有实现异步调用，只是一个同步操作。只适用于不需要多线程的地方
3. ConcurrentTaskExecutor：Executor的适配类，不推荐使用。如果ThreadPoolTaskExecutor不满足要求时，才用考虑使用这个类
4. SimpleThreadPoolTaskExecutor：是Quartz的SimpleThreadPool的类。线程池同时被quartz和非quartz使用，才需要使用此类
5. **ThreadPoolTaskExecutor ：最常使用，推荐。 其实质是对java.util.concurrent.ThreadPoolExecutor的包装**

![img](E:\git repository\LearningNotes\images\future.jpg)

futureTask  继承自RunnableFuture,有2种构造方式，分别实现runnable和callable,里面有状态，

future类

```Java
public interface Future<V>{
    //正常结束了，就不能取消，返回false,否则返回true
    boolean cancel(boolean mayInterruptIfRunning);
    //在正常完成任务前被取消了，返回true
    boolean isCancelled();
    //任务被正常完成了，抛出异常或被取消了，已经结束了返回true,
    boolean isDone();
    //返回结果
    V get() throws InterruptedException, ExecutionException;
    //
    V get(long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException;
    
}
```

public final class **AsyncResult<V>** extends [Object](http://docs.oracle.com/javase/7/docs/api/java/lang/Object.html?is-external=true) implements [Future](http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/Future.html?is-external=true)<V>

将异步方法调用的结果包装为`Future` 对象，保持与业务接口签名的兼容性。

构造函数中指定的值将由容器检索并提供给客户端。

请注意，此对象不会传递给客户端。只是为了方便将结果值提供给容器。因此，应用程序不应调用其实例方法

```Java
//如何在异步任务中获取返回值
Future<String> res=new AsyncResult<>(result);//把结果存入一个asyncresult对象中
String ans=res.get();       //通过get方法获取结果
```

springboot-context

https://zhuanlan.zhihu.com/p/54459770

Future 相当于一个存储器，它存储了 Callable 的 call 方法的任务结果。除此之外，我们还可以通过 Future 的 **isDone 方法来判断任务是否已经执行完毕了**，还可以通过 **cancel 方法取消这个任务**，或限时获取任务的结果等

#### spring的后置处理器

**beanPostProcessor**  postProcessorBeforeInitialization postProcessorAfterInialization 

**aware接口**   beanNameAware setBeanName()

beanFactoryAware setBeanFactory()

ApplicationContextAware setApplicationContext()

初始化**InializationBean**  afterPropertySet()

 初始化前  初始化  **aop生成代理对象**   

**DisposableBean**  destory()或者自定义销毁办法

#### 如何声明这是一个bean

- @Component 注解作用于类，可以搭配@ComponentScan定义要扫描的路劲找到标识的类将其自动装配到spring的容器中

  value 和 basePackages（别名）的作用是一样的，都是用于指定创建容器时要扫描的包，包名可以使用正则表达式。basePackageClasses 用于指定要扫描的类。includeFilters 和 excludeFilters 分别用于指定满足、排除过滤条件的 Bean，需要通过 @Filter 定义。lazyInit 用于指定是否延迟初始化。

```Java
@ComponentScan(value="com.wangxnn.service")//这个注解一般是放到启动类上
@Component
public class User{}
```

- @Bean 注解作用于方法   使用@Configuration标明这是一个配置类

  当配置类作为AnnotationConfigApplicationContext 对象创建的参数时，该注解可以不写

```Java
@Configuration
public class AppConfig {
    @Bean  //方法名是beanname  返回值是bean的类型
    public TransferService transferService() {
        return new TransferServiceImpl();
    }

}
```

等于

```xml
<beans>
    <bean id="transferService" class="com.acme.TransferServiceImpl"/>
</beans>
```

然后启动spring-context找到这个配置，一般来说注入bean配置文件有3种

```Java
//bean.xml
ClassPathXmlApplicationContext applicationcontext = new ClassPathXmlApplicationContext("bean.xml");
//JavaConfig      
AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(JavaConfig.class);
        
FileSystemXmlApplicationContext applicationcontext = new FileSystemXmlApplicationContext("E:/spring/springlearn/bean.xml");
```

- @Repository 对应dao层  @Service 对应服务层  @Controller层 对应springMVC控制层
- @Autowire 自动注入 先byType 再byName

​     与 @Autowired 组合使用，不能单独使用，按照**属性的类型和名称**找到对应的 Bean 进行注入。value 用于指定 Bean 的 id

- @Resource 自动注入先byName再byType

​	name 用于指定 Bean 的id,以上注入都只能注入其他 Bean 类型的数据，而基本类型和 String 类型无法使用上述注解实现。另外，集合	类型的注入只能通过 XML 来实现。

- @Value  用于注入基本类型和String类型的值  @Scope  用于指定bean的作用范围 属性为value
- @Aspect 表明当前类是一个切面类
- @Pointcut  用来定义切点，作用于方法，value指定切入点表达式

​	表达式为 ：execution(访问修饰符 返回值 包名.类名.方法名  参数）

> 访问修饰符可以省略：void com.service.impl.AccountServiceImpl.saveAccount()
>
> 返回值可以使用通配符，表示任意返回值：* com.service.impl.AccountServiceImpl.saveAccount()
>
> 包名可以使用..表示当前包及其子包：* *..AccountServiceImpl.saveAccount()
>
> 包名可以使用通配符，表示任意包。但是有几级包，就需要写几个*
>
> 类名和方法名都可以使用*来实现通配：* *..*.*()
>
> 参数列表：基本类型直接写名称，引用类型写包名类名的方式，可以使用通配符表示任意类型，但是必须有参数，可以使用..表示有无参数均可，有参数可以是任意类型
>
> 全通配写法：* *..*.*(..)
>
> 实际开发中的通常写法：切到业务层实现类下的所有方法，即* com.service.impl.*.*(..)

- @Before、@AfterReturning、@AfterThrowing、@AfterReturning、@Around

  作用：前置通知、后置通知、异常通知、最终通知、环绕通知

  其它：spring 基于注解的后置通知和最终通知有顺序问题，此时可以使用环绕通知

```Java
@Aspect
@Component
public class aspectTest {
    @Pointcut("execution(* com.wangxnn.service.*.*(..))")
    public void pointTest(){}

    @Before("pointTest()")
    public void beforeTest01(JoinPoint joinPoint){
        System.out.println("test before");
    }

    @After("pointTest()")
    public void afterTest(JoinPoint joinPoint){
        System.out.println("方法执行后");
    }

}
```

**@DeclareParents**

- 作用：用于引入新的类来增强服务
- 属性：value 指定要增强功能的目标对象，defaultImpl 引入增强功能的类

#### SpringMVC的工作原理

DispatchServlet

RestController

**Restful风格**   REST representational status transfer 表现层状态转移

- 看Url就知道要什么
- 看http method就知道干什么
- 看http status code就知道结果如何

POST\DELETE\GET\PUT     增删查改      简单、性能很高、可见、易于扩展

```Java
post /user/1   创建id为1的user
put  /user/1/?  更新id为1的user
get  /user/1     获取id为1的user
delete /user/1   删除id为1的user
```

#### Json

JSON（JavaScript Object Notation）是一种轻量级的数据交换格式，易于人阅读和编写，同时也易于机器解析和生成。 JSON建构于两种结构：名称/值对的集合（相当于 Java 中的 Map<String, Object>）、值的有序列表（相当于 Java 中的 List），并具有以下形式：

- 对象是一个无序的“名称/值对”集合。一个对象以 `{`左括号 开始， `}`右括号 结束，每个“名称”后跟一个 `:`冒号 ，“名称/值对”之间使用 `,`逗号 分隔
- 数组是值（value）的有序集合。一个数组以 `[`左中括号 开始， `]`右中括号 结束，值之间使用 `,`逗号 分隔
- 值（value）可以是双引号括起来的字符串、数值、`true`、`false`、 `null`、对象或数组，这些结构可以嵌套

#####  简介

Spring Boot 内置了 jackson 来完成 JSON 的序列化和反序列化操作，依赖由 `spring-boot-starter-web` 引入，因此在 Spring Boot 中不需要引入依赖。jackson 使用 `ObjectMapper` 类将 Java Bean 序列化成 JSON 字符串，或者将 JSON 字符串反序列化成 Java Bean。

参考：[默认转换工具Jackson](https://gitee.com/link?target=https%3A%2F%2Fblog.csdn.net%2Fu013089490%2Farticle%2Fdetails%2F83585794)、[利用Jackson封装常用JsonUtil工具类](https://gitee.com/link?target=https%3A%2F%2Fwww.cnblogs.com%2Fchristopherchan%2Fp%2F11071098.html)

##### 常见注解

1. **@JsonProperty**：作用在属性上，用来为 JSON Key 指定一个别名
2. **@JsonIgnore**：作用在属性上，用来忽略此属性
3. **@JsonFormat**：作用在属性上，用于日期格式化

[jackson实现JsonUtils工具类](https://www.cnblogs.com/christopherchan/p/11071098.html)

```Java
package com.wangxnn.springboottest.Json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;

@Slf4j
public class JsonUtils {
    private static ObjectMapper objectMapper=new ObjectMapper();
    private static final String DATE_FORMAT="yyyy-MM-dd hh-mm-ss";
    static {
        //对象所有字段列入
        objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        //禁止默认转换timestamps格式
        objectMapper.configure(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS,false);
        objectMapper.setDateFormat(new SimpleDateFormat(DATE_FORMAT));
        //忽略空bean转为json的错误
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS,false);
        //忽略在json字符串中存在，但java bean中不存在某属性的情况
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
    }
    //对象转为json字符串
    public static <T> String object2Str(T obj){
        if(obj==null){
            return null;
        }
        try {
            return obj instanceof String?(String)obj:objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("Parse Object to String error:"+e.getMessage());
            return null;
        }
    }
    //对象转为格式化的json字符串
    public static <T> String obj2StrPretty(T obj){
        if(obj==null){
            return null;
        }
        try {
            return obj instanceof String?(String) obj:objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("parse obj to String error:"+e.getMessage());
            return null;
        }
    }
    //将json字符串转为obj
    public static <T> T string2Obj(String str,Class<T> clazz){
        if(StringUtils.isEmpty(str)){
            return null;
        }
        try {
            return clazz.equals(String.class)?(T)str:objectMapper.readValue(str,clazz);
        } catch (JsonProcessingException e) {
            log.warn("parse string to object error:"+e.getMessage());
            return null;
        }
    }
    public static <T> T string2Obj(String str, TypeReference<T> typeReference) {
        if (StringUtils.isEmpty(str) || typeReference == null) {
            return null;
        }
        try {
            return (T) (typeReference.getType().equals(String.class) ? str : objectMapper.readValue(str, typeReference));
        } catch (JsonProcessingException e) {
            log.warn("Parse String to Object error", e);
            return null;
        }
    }

    public static <T> T string2Obj(String str, Class<?> collectionClazz, Class<?>... elementClazzes) {
        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(collectionClazz, elementClazzes);
        try {
            return objectMapper.readValue(str, javaType);
        } catch (IOException e) {
            log.warn("Parse String to Object error : {}" + e.getMessage());
            return null;
        }
    }

}

```

#### FastJson

```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>fastjson</artifactId>
    <version>1.2.47</version>
</dependency>
```



```Java
// com.alibaba.fastjson.JSON 类的常用静态方法

// 将 JavaBean 序列化为 JSON 字符串（主要）
public static final String toJSONString(Object object);
// 将 JavaBean 序列化为带格式的 JSON 字符串 
public static final String toJSONString(Object object, boolean prettyFormat);
// 将 JavaBean 序列化为 JSONObject 或 JSONArray；JSONArray 相当于 List，JSONObject 相当于 Map
public static final Object toJSON(Object javaObject); 

// 把 JSON 字符串反序列化为JavaBean（主要）
public static final <T> T parseObject(String text, Class<T> clazz);
// 把 JSON 字符串反序列化为JavaBean 集合 
public static final <T> List<T> parseArray(String text, Class<T> clazz);

// 把 JSON 字符串反序列化为 JSONObject 或 JSONArray
public static final Object parse(String text);
// 把 JSON 字符串反序列化为 JSONObject
public static final JSONObject parseObject(String text);
// 把 JSON 字符串反序列化为 JSONArray
public static final JSONArray parseArray(String text);
```

#### 单点登录

Single sign on  SSO

单点登录就是**在多个系统中，用户只需一次登录，各个系统即可感知该用户已经登录**，例如淘宝和天猫

httpRequest httpResponce能包含那些？cookie session?会话技术？

```java
HttpSession session, HttpServletResponse response) {

    //判断验证码是否正确
```

普通的登录认证，在一次session对话中，用户访问、登录====>服务器认证用户名+密码======>服务器核对用户名和密码，确认，在session中创建这个用户的会话=====>把sessionId返回给用户====>用户把sessionId加入cookie，每次请求都要判断cookie中的id,方便登录

http协议是无状态的

JWT的结构

```markdown
token   String   ==============>header.payload.signature
令牌组成  header-标头   payload-有效载荷   signature-签名
# 1 header
标头一般由2部分组成，令牌的类型(即JWT)和所使用的的签名算法 如HMAC SHA256或RSA，它会使用BASE64编码，组成JWT的第一部分
BASE64是一种编码，并不是一种加密，可以翻译回原来的样子
```

```json
{
    "alg":"HS256",
    "typ":"JWT"
}
```

```markdown
# 2 payload
第二部分是有效负载，它包含了声明，声明是指实体(通常是用户)和其他数据的声明，同样，它也会使用BASE64编码
```

```json
{
    "sub":"1234567890",
    "name":"Tom",
    "admin":true
}
```

```markdown
# 3 signature
signature需要使用第一部分和第二部分被编码过的值+提供的一个密钥（某一个盐值），使用第一部分中指定的签名算法进行签名，签名的目的是保证JWT没有被篡改过

```





postMapping 和getMapping有什么不同

