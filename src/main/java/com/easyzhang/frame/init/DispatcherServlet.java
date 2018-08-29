package com.easyzhang.frame.init;

import com.easyzhang.frame.annotations.EZAutowired;
import com.easyzhang.frame.annotations.EZController;
import com.easyzhang.frame.annotations.EZRequestMapping;
import com.easyzhang.frame.annotations.EZService;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class DispatcherServlet extends HttpServlet {

    /**
     * 这里写一个list集合，用于存放扫描出来所有的文件的类名
     */
    public static List<String> classNames = new ArrayList<>();

    /**
     * 这里定义一个IOC的Map容器，用于存放实体类对象
     */
    public static Map<String,Object> iocmap = new HashMap<>();

    /**
     * 这里再定义一个Map容器，用来记录，
     * 使用的是ConcurrentHashMap是线程安全的,
     * 这里定义的这个Map是用来记录  HandlerMapping，处理器映射器的
     */
    ConcurrentHashMap<String,Method> hm = new ConcurrentHashMap<>();

    @Override
    public void init() throws ServletException {
        System.out.println("Dispatcher初始化开始");
        String basePackage = "com.easyzhang.user";
        scanPackage(basePackage);
        instance();
        ioc();
        super.init();
        System.out.println("Dispatcher初始化完成");

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 完整路径
        String url = req.getRequestURI();
        // 跟路径
        String path = req.getContextPath();
        // 计算出method上配置的路径
        String finallyUrl = url.replace(path, "");
        System.out.println("请求路径:"+finallyUrl);

        iocmap.size();
        Method method = hm.get(finallyUrl);
        try {
           Object o = method.invoke(iocmap.get(firstLowerName(method.getDeclaringClass().getSimpleName())));
           PrintWriter objectOutputStream = resp.getWriter();
           objectOutputStream.print(o);
           objectOutputStream.close();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * 扫描所有的类
     * @param basePackage
     */
    private void scanPackage(String basePackage){
        URL url = this.getClass().getClassLoader().getResource("/"+basePackage.replaceAll("\\.","/"));
        System.out.println("扫描路径:"+url);
        String pathFile = url.getFile();
        File file = new File(pathFile);
        String[] fileList = file.list();
        for (String path : fileList) {
            File eachFile = new File(pathFile +"/"+ path);
            if (eachFile.isDirectory()) {
                scanPackage(basePackage + "." + eachFile.getName());
            } else {
                classNames.add(basePackage + "." + eachFile.getName());
            }
        }
    }

    /**
     * 给controller service 创建对象
     */
    private void instance(){
        if(classNames.size()<=0){
            return;
        }
        classNames.forEach(className->{
            try {
                Class<?> clazz = Class.forName(className.replace(".class", "").trim());
                if(clazz.isAnnotationPresent(EZController.class)){
                    Object object = clazz.getDeclaredConstructor().newInstance();
                    iocmap.put(firstLowerName(clazz.getSimpleName()),object);
                    EZRequestMapping requestMapping = clazz.getAnnotation(EZRequestMapping.class);
                    String value = requestMapping.value();
                    Method[] methods = clazz.getMethods();
                    for(Method method : methods){
                        if(method.isAnnotationPresent(EZRequestMapping.class)){
                            EZRequestMapping requestMappingM = method.getAnnotation(EZRequestMapping.class);
                            String valueM = requestMappingM.value();
                            System.out.println("requestMapping:" + value + valueM);
                            hm.put(value + valueM, method);
                        }
                    }
                }else if(clazz.isAnnotationPresent(EZService.class)){
                    Object object = clazz.getDeclaredConstructor().newInstance();
                    iocmap.put(firstLowerName(clazz.getSimpleName()),object);
                    Class<?>[] interfaces = clazz.getInterfaces();
                    for (Class<?> class1 : interfaces) {
                        if (iocmap.get(class1) != null) {
                            throw new RuntimeException(class1.getName() + "接口不能被多个类实现！");
                        }
                        iocmap.put(firstLowerName(class1.getSimpleName()),object);
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
                System.out.println("出问题咯");
            }
        });
    }

    /**
     * IOC 是啥
     */
    private void ioc(){
        if(iocmap.isEmpty()){
            return;
        }
        try {
            for(Map.Entry<String,Object> entry : iocmap.entrySet()){
                // 拿到里面的所有属性
                Field[] fields = entry.getValue().getClass().getDeclaredFields();
                for (Field field : fields) {
                    // 可访问私有属性
                    field.setAccessible(true);
                    if (field.isAnnotationPresent(EZAutowired.class)){
                        if(field.getAnnotation(EZAutowired.class).value().isEmpty()){
                            Object object = iocmap.get(field.getAnnotation(EZAutowired.class).value());
                            if(Objects.isNull(object)){
                                throw new Exception(field.getAnnotation(EZAutowired.class).value()+"未初始化");
                            }
                            field.set(entry.getValue(),object);
                        }else {
                            Object object = iocmap.get(firstLowerName(field.getType().getSimpleName()));
                            field.set(entry.getValue(),object); }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private String firstLowerName(String name) {
        name = name.substring(0, 1).toLowerCase() + name.substring(1);
        return  name;
    }
}