package com.easyzhang.frame.init;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DispatcherServlet extends HttpServlet {

    //这里写一个list集合，用于存放扫描出来所有的文件的类名
    public static List<String> classNames = new ArrayList<>();
    //这里定义一个IOC的Map容器，用于存放实体类对象
    public static Map<String,Object> iocmap = new HashMap<>();
    //这里再定义一个Map容器，用来记录，使用的是ConcurrentHashMap是线程安全的,这里定义的这个Map是用来记录  HandlerMapping，处理器映射器的
    ConcurrentHashMap<String,Method> hm = new ConcurrentHashMap<>();

    @Override
    public void init() throws ServletException {
        System.out.println("Dispatcher初始化开始");
        super.init();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doGet(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }

    private
}
