package com.test.rpc.server.test;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 服务启动类
 * Created by gongkang on 2017/5/24.
 */
public class BootstrapClass {
    public static void main(String[] args) {
        new ClassPathXmlApplicationContext("spring.xml");
    }
}
