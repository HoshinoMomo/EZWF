package com.easyzhang.user.controllers;

import com.easyzhang.frame.annotations.EZAutowired;
import com.easyzhang.frame.annotations.EZController;
import com.easyzhang.frame.annotations.EZParam;
import com.easyzhang.frame.annotations.EZRequestMapping;
import com.easyzhang.user.service.ezshow.HelloService;

/**
 * @author EasyZhang
 * @date 2018/8/25 -  17:04
 */
@EZController
@EZRequestMapping("/hello")
public class HelloController {

    @EZAutowired
    private HelloService helloService;

    @EZRequestMapping("/world")
    public String world(){
        return helloService.hello();
    }

    @EZRequestMapping("/china")
    public String china(@EZParam("user") String user){
        return helloService.china(user);
    }

}
