package com.test.rpc.client.test;

import com.test.rpc.client.RpcProxy;
import com.test.rpc.service.TestService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

/**
 * 测试类
 * Created by gongkang on 2017/5/24.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring.xml")
public class ClientTest {

    @Resource
    private RpcProxy rpcProxy;

    @Test
    public void helloTest() {
        TestService helloService = rpcProxy.create(TestService.class);
        String result = helloService.hello("Gongkang");
        System.err.println(result);
    }
}
