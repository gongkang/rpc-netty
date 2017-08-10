package com.test.rpc.service;

import com.test.rpc.server.RpcService;

/**
 * Created by gongkang on 2017/5/24.
 */
@RpcService(TestService.class)
public class TestServiceImpl implements TestService {

    @Override
    public String hello(String name) {
        return "Hello " + name;
    }
}
