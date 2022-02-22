Druid是阿里巴巴开发的号称为监控而生的数据库连接池，在功能、性能、扩展性方面，都超过其他数据库连接池

Druid 是阿里巴巴开源平台上一个数据库连接池实现，结合了 C3P0、DBCP 等 DB 池的优点，同时加入了日志监控。

Druid 可以很好的监控 DB 池连接和 SQL 的执行情况，天生就是针对监控而生的 DB 连接池。

#### 依赖导入

```xml
<!-- https://mvnrepository.com/artifact/com.alibaba/druid -->
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>druid</artifactId>
    <version>1.2.6</version>
</dependency>
<!-- https://mvnrepository.com/artifact/log4j/log4j -->
<dependency>
    <groupId>log4j</groupId>
    <artifactId>log4j</artifactId>
    <version>1.2.17</version>
</dependency>
```

```Gradle
// https://mvnrepository.com/artifact/com.alibaba/druid
implementation group: 'com.alibaba', name: 'druid', version: '1.2.6'
// https://mvnrepository.com/artifact/log4j/log4j
implementation group: 'log4j', name: 'log4j', version: '1.2.17'
```

#### 一个简单的SpringBoot要配置哪些

- 配置Druid数据源（连接池）： 如同以前 c3p0、dbcp 数据源可以设置数据源连接初始化大小、最大连接数、等待时间、最小连接数 等一样，Druid 数据源同理可以进行设置；


- 配置 Druid web 监控 filter（WebStatFilter）： 这个过滤器的作用就是统计 web 应用请求中所有的数据库信息，比如 发出的 sql 语句，sql 执行的时间、请求次数、请求的 url 地址、以及seesion 监控、数据库表的访问次数 等等。


- 配置 Druid 后台管理 Servlet（StatViewServlet）： Druid 数据源具有监控的功能，并提供了一个 web 界面方便用户查看，类似安装 路由器 时，人家也提供了一个默认的 web 页面；需要设置 Druid 的后台管理页面的属性，比如 登录账号、密码 等；

#### 在哪里配置，怎么配置

在SpringBoot中配置Druid Config配置类,主要有2个，一个是Java Config,一个是在配置文件相关属性

```Java
@Configuration
public class DruidConfig {
	//将自定义的数据源属性值注入到容器中
    @ConfigurationProperties(prefix = "spring.datasource")
    @Bean
    public DataSource druidDataSource(){
        return  new DruidDataSource();
    }

    //配置Druid的监控
    //1、配置一个管理后台的Servlet
    //内置Servlet容器时没有web.xml，所以使用SpringBoot注册Servlet的方式
    @Bean
    public ServletRegistrationBean statViewServlet(){
        ServletRegistrationBean bean = new ServletRegistrationBean(new StatViewServlet(), "/druid/*");
        Map<String,String> initParams = new HashMap<>();

        initParams.put("loginUsername","admin");
        initParams.put("loginPassword","123456");
        //默认就是允许所有访问
        initParams.put("allow","");
        //如initParams.put("allow","localhost")
        //拒绝谁访问
        initParams.put("deny","192.168.15.21");
		//设置初始化参数
        bean.setInitParameters(initParams);
        return bean;
    }


    //2、配置一个web监控的filter
    @Bean
    public FilterRegistrationBean webStatFilter(){
        FilterRegistrationBean bean = new FilterRegistrationBean();
        bean.setFilter(new WebStatFilter());
		//排除哪些请求不需要过滤，从而不进行统计
        Map<String,String> initParams = new HashMap<>();
        initParams.put("exclusions","*.js,*.css,/druid/*");
        bean.setInitParameters(initParams);
		//过滤所有请求
        bean.setUrlPatterns(Arrays.asList("/*"));
        return  bean;
    }
}
```

`application.properties`

```properties
###数据库连接设置
#数据库驱动  这是5.7版本驱动版本
spring.datasource.driver-class-name=com.mysql.jdbc.Driver
#spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.url=jdbc:mysql://localhost:3306/seckill?useUnicode=true&characterEncoding=UTF-8&useSSL=false
spring.datasource.username=root
spring.datasource.password=****
#自定义数据库连接源  druid数据库连接池  默认是hikari:com.zaxxer.hikari.HikariDataSource
spring.datasource.type=com.alibaba.druid.pool.DruidDataSource

###连接池设置
#配置连接池初始化大小，最小、最大、以及获取连接等待超时的时间，单位是毫秒
#spring.datasource.initialSize= 5
#spring.datasource.minIdle= 5
#spring.datasource.maxActive= 20
#spring.datasource.maxWait= 60000
spring.datasource.maxActive=1000
spring.datasource.initialSize=100
spring.datasource.maxWait=60000
spring.datasource.minIdle=500
#配置间隔多久进行一次驱逐检测，检测需要关闭的空闲连接，单位为毫秒
spring.datasource.timeBetweenEvictionRunsMillis= 60000
#配置一个连接在池中最小的生存时间，毫秒，还可以配置最大生存时间
spring.datasource.minEvictableIdleTimeMillis= 300000
#当minIdle超过最小生存时间时，执行keepAlive操作
spring.datasource.keepAlive=true

#用来检测连接是否可用的sql语句
spring.datasource.validationQuery= SELECT 1 FROM DUAL
#配合testOnBorrow为false的情况，连接池判断这条连接是否处于空闲状态，如果是，判断这条连接是否可用
spring.datasource.testWhileIdle= true
#默认是false,如果是true,应用在向连接池申请连接时，会先判断这条连接是否可用
spring.datasource.testOnBorrow= false
#默认是false,如果是true,应用用完这条连接后，连接池回收这条连接时会判断这条连接是否可用
spring.datasource.testOnReturn= false

#是否缓存prepareStatement,也就是PSCache,对于支持游标的数据库，如oracle,提升效果显著
spring.datasource.poolPreparedStatements= true
#要启用PSCache,必须配置大于0，若配置大于0，默认poolPreparedStatements为true
spring.datasource.maxOpenPreparedStatements=20

###配置监控统计拦截的filters  stat:监控统计;wall:防御sql注入;log4j:日志记录
spring.datasource.filters= stat,wall,log4j
spring.datasource.maxPoolPreparedStatementPerConnectionSize= 20
spring.datasource.useGlobalDataSourceStat= true
spring.datasource.connectionProperties= druid.stat.mergeSql=true;druid.stat.slowSqlMillis=500
```



SpringMVC

实现webMVCConfigurer

addInterceptor 添加拦截器

addViewController:添加页面跳转

addResourceHandler:添加静态资源

addArgumentResolvers：添加参数解析器

手工创建filter的bean

```properties
#web static
spring.resources.add-mappings=true
spring.resources.cache-period= 3600
spring.resources.chain.cache=true 
spring.resources.chain.enabled=true
spring.resources.chain.gzipped=true
spring.resources.chain.html-application-cache=true
spring.resources.static-locations=classpath:/static/
```

```Java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    AuthorityInterceptor authorityInterceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // addPathPatterns 用于添加拦截规则
        // excludePathPatterns 用户排除拦截
        // 映射为 user 的控制器下的所有映射
        //registry.addInterceptor(authorityInterceptor).addPathPatterns("/user/login").excludePathPatterns("/index", "/");
        registry.addInterceptor(authorityInterceptor).addPathPatterns("/user/login").excludePathPatterns("/index", "/");;

    }
	//手动创建一个Filter
    @Bean("myFilter")
    public Filter uploadFilter() {
        return new SessionExpireFilter();
    }

    @Bean
    public FilterRegistrationBean uploadFilterRegistration() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new DelegatingFilterProxy("myFilter"));
        registration.addUrlPatterns("/**");
        registration.setName("MyFilter");
        registration.setOrder(1);
        return registration;
    }
    @Autowired
    private UserArgumentResolver userArgumentResolver;
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(userArgumentResolver);
    }


}
```

`Fileter`的自定义

```Java
/**
 * 重新设置用户session在redis的有效期
 */
@Component
public class SessionExpireFilter implements Filter {
    @Autowired
    RedisService redisService;
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest)servletRequest;

        String loginToken = CookieUtil.readLoginToken(httpServletRequest);

        if(StringUtils.isNotEmpty(loginToken)){
            //判断logintoken是否为空或者""；
            //如果不为空的话，符合条件，继续拿user信息
            User user = redisService.get(UserKey.getByName,loginToken, User.class);
            if(user != null){
                //如果user不为空，则重置session的时间，即调用expire命令
                redisService.expice(UserKey.getByName , loginToken, Const.RedisCacheExtime.REDIS_SESSION_EXTIME);
            }
        }
        filterChain.doFilter(servletRequest,servletResponse);
    }


    @Override
    public void destroy() {

    }
}
```

```Java
/**
 * 使用拦截器统一校验用户权限
 */
@Component
public class AuthorityInterceptor implements HandlerInterceptor {
    @Autowired
    RedisService redisService;

    private Logger logger = LoggerFactory.getLogger(AuthorityInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //请求controller中的方法名
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        //解析HandlerMethod
        String methodName = handlerMethod.getMethod().getName();
        String className = handlerMethod.getBean().getClass().getSimpleName();

        StringBuffer requestParamBuffer = new StringBuffer();
        Map paramMap = request.getParameterMap();
        Iterator it = paramMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String mapKey = (String) entry.getKey();
            String mapValue = "";

            //request的这个参数map的value返回的是一个String[]
            Object obj = entry.getValue();
            if (obj instanceof String[]) {
                String[] strs = (String[]) obj;
                mapValue = Arrays.toString(strs);
            }
            requestParamBuffer.append(mapKey).append("=").append(mapValue);
        }

        //接口限流
        AccessLimit accessLimit = handlerMethod.getMethodAnnotation(AccessLimit.class);
        if(accessLimit == null) {
            return true;
        }
        int seconds = accessLimit.seconds();
        int maxCount = accessLimit.maxCount();
        boolean needLogin = accessLimit.needLogin();
        String key = request.getRequestURI();


        //对于拦截器中拦截manage下的login.do的处理,对于登录不拦截，直接放行
        if (!StringUtils.equals(className, "SeckillController")) {
            //如果是拦截到登录请求，不打印参数，因为参数里面有密码，全部会打印到日志中，防止日志泄露
            logger.info("权限拦截器拦截到请求 SeckillController ,className:{},methodName:{}", className, methodName);
            return true;
        }

        logger.info("--> 权限拦截器拦截到请求,className:{},methodName:{},param:{}", className, methodName, requestParamBuffer);
        User user = null;
        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isNotEmpty(loginToken)) {
            user = redisService.get(UserKey.getByName, loginToken, User.class);
        }

        if(needLogin) {
            if(user == null) {
                render(response, CodeMsg.USER_NO_LOGIN);
                return false;
            }
            key += "_" + user.getId();
        }else {
            //do nothing
        }
        AccessKey ak = AccessKey.withExpire;
        Integer count = redisService.get(ak, key, Integer.class);
        if(count  == null) {
            redisService.set(ak, key, 1, seconds);
        }else if(count < maxCount) {
            redisService.incr(ak, key);
        }else {
            render(response, CodeMsg.ACCESS_LIMIT_REACHED);
            return false;
        }

        /*if (user == null) {
            //重置 重写response一定要重置 这里要添加reset，否则报异常 getWriter() has already been called for this response
            response.reset();
            //geelynote 这里要设置编码，否则会乱码
            response.setCharacterEncoding("UTF-8");
            // 这里要设置返回值类型，因为全部是json接口。
            response.setContentType("application/json;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.print(JsonUtil.obj2String(Result.error(CodeMsg.USER_NO_LOGIN)));
            //response.sendRedirect(request.getContextPath()+"/page/login");
            // 这里要关闭流
            out.flush();
            out.close();
            return false;
        }*/
        return true;
    }


    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    }
    //把失败的登录结果持久化保存到硬盘中
    private void render(HttpServletResponse response, CodeMsg cm)throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        OutputStream out = response.getOutputStream();
        String str  = JSON.toJSONString(Result.error(cm));
        out.write(str.getBytes("UTF-8"));
        out.flush();
        out.close();
    }

}
```

参数解析器的构造

```Java
package com.wangxnn.seckill.config;

import com.wangxnn.seckill.pojo.User;
import com.wangxnn.seckill.service.IUserService;
import com.wangxnn.seckill.utils.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Parameter;
@Component
public class UserArgumentResolver implements HandlerMethodArgumentResolver {
    @Autowired
    private IUserService userService;
    //条件的判断
    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        Class<?> clazz = methodParameter.getParameterType();
        return clazz== User.class;
    }

    @Override
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {
        HttpServletRequest request = nativeWebRequest.getNativeRequest(HttpServletRequest.class);
        HttpServletResponse response = nativeWebRequest.getNativeResponse(HttpServletResponse.class);
        String cookie = CookieUtil.getCookieValue(request, "userCookie");
        if(StringUtils.isEmpty(cookie)){
            return null;
        }
        User user = userService.getUserByCookie(request, response, cookie);
        return user;
    }
}

```

接口限流使用的方法：计数器、令牌桶、漏桶

自定义注解     需要实现一个验证器类，验证器类需要实现`ConstraintValidator`泛型接口

现在`@interface`定义一个注解即相关属性，属性表示为 类型+属性名()+默认值：`String message() default "hello";`

然后加上@Constraint 定义验证逻辑，里面是验证类

自定义验证器类，需要实现`ConstraintValidator`<接口名，要验证的对象类型>，里面有`initailize`和`isValid``两个方法需要重写`，可以从`initailize`中得到注解的一些属性设置，然后在`isValid`中定义注解逻辑

```Java
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(
        validatedBy = {IsMobileValidator.class}
)
public @interface IsMobile {
    boolean required() default true;
    String message() default "手机号码格式错误";
	//分组验证，可以根据一些不同的场景定义注解的使用
    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

```

```Java
public class IsMobileValidator implements ConstraintValidator<IsMobile,String> {
    private boolean required;
    @Override
    public void initialize(IsMobile constraintAnnotation) {
        required=constraintAnnotation.required();
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        if(required){
            return ValidationUtil.isMobile(s);
        }else{
            if(StringUtils.isEmpty(s)){
                return true;
            }else {
                return ValidationUtil.isMobile(s);
            }
        }
    }
}
```

#### 怎么描述这个项目

用户访问页面--->已经登录了，在doFilter里更新Redis里的过期时间----->没有登录，不更新

登录/login/doLogin/--->验证参数是否符合@Valid isMobile  在UserController-->调用userService的服务doLogin(LoginVo)-->调用userMapper 通过手机号码获取用户信息User--->比较号码和密码，不对抛出异常，对的话继续利用UUID.randomUUID()生成一个cookie,放入Redis中，将生成的这个Cookie放入response中，返回ResponBeam,登录成功---->前段转到goods/toList商品页面--->



查询redis中是否存在这个页面--->如果有，直接返回，如果没有，查询到List<GoodVo> 后通过thmyleaf引擎渲染后插入到Redis中再返回

在商品列表页面，有这些商品的各种信息，点进详情页面，秒杀页面，时间、数量和购买按钮  seckill/doSeckill

相关引用：[开启Druid监控统计功能(SQL监控、慢SQL记录、Spring监控、去广告)](https://blog.csdn.net/weixin_44730681/article/details/107944048)

goodsController  

- /goods/toList    商品列表详情页  用redis存储页面    goodsService.findAllGoodsVo()+静态页面
- /goods/toDetail/{goodsId}      单个商品详情页    goodsService.findGoodsByGoodsId,用Redis存储
- /goods/detail/goodsId  页面静态化优化后，只需要返回一个RespBean对象夹着一个细节信息的对象

seckillController

- 秒杀逻辑，用户是否登录、是否重复购买、库存是否为空、可以加一个Map、判断库存是否为空，减一，生成一个消息，加入消息队列，等待处理订单，返回抢购正在处理
- 在消息队列接收端处理，再次判断超卖--表？重复购买？调用orderService.seckill处理消息，生成正常商品订单和秒杀订单

秒杀商品表减库存，