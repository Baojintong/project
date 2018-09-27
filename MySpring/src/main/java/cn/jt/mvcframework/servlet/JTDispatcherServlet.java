package cn.jt.mvcframework.servlet;

import cn.jt.mvcframework.annotation.JTAutowired;
import cn.jt.mvcframework.annotation.JTController;
import cn.jt.mvcframework.annotation.JTRequestMapping;
import cn.jt.mvcframework.annotation.JTService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.util.*;

public class JTDispatcherServlet extends HttpServlet {

    private static final long uid = 1L;

    //跟web.xml中的param-name的值一致
    private static final String LOCATION = "contextConfigLocation";

    //保存所有的配置信息
    private Properties p = new Properties();

    //保存所有被扫描的相关的类名
    private List<String> classNames = new ArrayList<String>();

    //核心IOC容器，保存所有初始化的bean
    private Map<String, Object> ioc = new HashMap<String, Object>();

    //保存所有的Url和方法的映射关系
    private Map<String, Method> handlerMapping = new HashMap<String, Method>();

    public JTDispatcherServlet() {
        super();
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        //1.加载配置文件
        doLoadConfig(config.getInitParameter(LOCATION));

        //2.扫描所有相关的类
        doScanner(p.getProperty("scanPackage"));

        //3.初始化所有相关类的实例，并保存到IOC容器中
        doInstance();

        //4.依赖注入
        doAutowired();

        //5.构造HandlerMapping
        initHandlerMapping();

        //6.等等请求，匹配URL 定位方法 反射调用执行
        //调用doGet或者doPost方法

        //提示信息
        System.out.println("spring 初始化");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            //匹配对应的方法
            doDispatch(req,resp);

        } catch (Exception e) {
            resp.getWriter().write("500:\r\n"+Arrays.toString(e.getStackTrace())
                    .replaceAll("\\[|\\]", "")
                    .replaceAll("\\s", "\r\n"));
        }
    }

    /**
     * doLoadConfig()方法的实现，将文件读取到Properties对象中：
     *
     * @param location
     */
    private void doLoadConfig(String location) {
        InputStream fis = null;

        try {
            fis = this.getClass().getClassLoader().getResourceAsStream(location);
            //1.读取配置文件
            p.load(fis);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != fis) {
                    fis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 递归扫描出所有的Class文件
     *
     * @param packageName
     */
    private void doScanner(String packageName) {
        //将所有的包路径转换为文件路径
        URL url = this.getClass().getClassLoader().getResource("/" + packageName.replaceAll("\\.", "/"));
        File dir = new File(url.getFile());
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                doScanner(packageName + "." + file.getName());
            } else {
                classNames.add(packageName + "." + file.getName().replace(".class", "").trim());
            }
        }
    }

    /**
     * 首字母小写
     *
     * @param str
     * @return
     */
    private String lowerFistCase(String str) {
        char[] chars = str.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

    /**
     * doInstance()方法，初始化所有相关的类，并放入到IOC容器之中。
     * IOC容器的key默认是类名首字母小写，如果是自己设置类名，则优先使用自定义的。
     * 因此，要先写一个针对类名首字母处理的工具方法。
     */
    private void doInstance() {
        if (classNames.size() == 0) {
            return;
        }
        try {
            for (String className : classNames) {
                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(JTController.class)) {
                    //默认将首字母小写为beanName
                    String beanName = lowerFistCase(clazz.getSimpleName());
                    ioc.put(beanName, clazz.newInstance());
                } else if (clazz.isAnnotationPresent(JTService.class)) {
                    JTService service = clazz.getAnnotation(JTService.class);
                    String beanName = service.value();
                    //如果用户设置了名字，就用用户自己设置
                    if (!"".equals(beanName.trim())) {
                        ioc.put(beanName, clazz.newInstance());
                        continue;
                    }
                    //如果自己没设置，就按接口类型创建一个实例
                    Class<?>[] interfaces = clazz.getInterfaces();
                    for (Class<?> i : interfaces) {
                        ioc.put(i.getName(), clazz.newInstance());
                    }
                } else {
                    continue;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doAutowired() {
        if (ioc.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            //拿到实例对象中的所有属性
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for (Field field : fields) {
                if (!field.isAnnotationPresent(JTAutowired.class)) {
                    continue;
                }
                JTAutowired autowired = field.getAnnotation(JTAutowired.class);
                String beanName = autowired.value().trim();
                if ("".equals(beanName)) {
                    beanName = field.getType().getName();
                }
                //设置私有属性的访问权限
                field.setAccessible(true);
                try {

                    field.set(entry.getValue(), ioc.get(beanName));

                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
            }
        }
    }

    /**
     * initHandlerMapping()方法，将GPRequestMapping中配置的信息和Method进行关联，并保存这些关系。
     */
    private void initHandlerMapping() {
        if (ioc.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Class<?> clazz = entry.getValue().getClass();
            if (!clazz.isAnnotationPresent(JTController.class)) {
                continue;
            }
            String baseUrl = "";
            //获取controller的url配置
            if (clazz.isAnnotationPresent(JTRequestMapping.class)) {
                JTRequestMapping requestMapping = clazz.getAnnotation(JTRequestMapping.class);
                baseUrl=requestMapping.value();
            }

            //获取Method的url配置
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                //没有加RequestMapping注解直接忽略
                if (!method.isAnnotationPresent(JTRequestMapping.class)) {
                    continue;
                }

                //映射URL
                JTRequestMapping requestMapping = method.getAnnotation(JTRequestMapping.class);

                String url = ("/" + baseUrl + "/" + requestMapping.value().replaceAll("/+", "/"));
                handlerMapping.put(url, method);
                System.out.println("mapped " + url + "," + method);
            }

        }
    }

    private void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (this.handlerMapping.isEmpty()) {
            return;
        }

        String url = request.getRequestURI();
        String contextPath = request.getContextPath();
        System.out.println(url+"---"+contextPath);
        url = url.replace(contextPath, "")
                .replaceAll("/+", "/");
        System.out.println(this.handlerMapping);
        System.out.println(url);
        if (!this.handlerMapping.containsKey(url)) {
            response.getWriter().write("404");
            return;
        }
        Map<String, String[]> params = request.getParameterMap();
        Method method = this.handlerMapping.get(url);

        //获取方法的参数列表
        Class<?>[] parameterTypes = method.getParameterTypes();

        //获取请求的参数
        Map<String, String[]> parameterMap = request.getParameterMap();

        //保存参数值
        Object[] paramValues = new Object[parameterTypes.length];

        //方法的参数列表
        for (int i = 0; i < parameterTypes.length; i++) {
            //根据参数名称做某些处理
            Class parameterType = parameterTypes[i];
            if (parameterType == HttpServletRequest.class) {
                //参数类型已明确，这边强转类型
                paramValues[i] = request;
                continue;
            } else if (parameterType == HttpServletResponse.class) {
                paramValues[i] = response;
                continue;
            } else if (parameterType == String.class) {
                for (Map.Entry<String, String[]> param : parameterMap.entrySet()) {
                    String value = Arrays.toString(param.getValue())
                            .replaceAll("\\[|\\]", "")
                            .replaceAll("\\s", ",");
                    paramValues[i] = value;
                }
            }
        }
        try {
            String beanName = lowerFistCase(method.getDeclaringClass().getSimpleName());
            //利用反射机制来调用
            method.invoke(this.ioc.get(beanName), paramValues);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
