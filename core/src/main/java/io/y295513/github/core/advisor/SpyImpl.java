package io.y295513.github.core.advisor;

import com.alibaba.fastjson.JSON;
import io.y295513.github.core.server.ArthasXCoreBootstrap;

import java.arthasx.SpyAPI.AbstractSpy;

public class SpyImpl extends AbstractSpy {
    @Override
    public void beforeInvoke(Class<?> clazz, Object[] args) {
        ArthasXCoreBootstrap.responseMessage("[{\"beforeInvoke\":" + JSON.toJSONString(args) + "},");
    }

    @Override
    public void afterInvoke(Class<?> clazz, Object[] args, Object returnObject) {
        ArthasXCoreBootstrap.responseMessage("{\"afterInvoke\":" + JSON.toJSONString(returnObject) + "}]");
    }

    @Override
    public void afterException(Class<?> clazz, Object[] args, Throwable throwable) {
        ArthasXCoreBootstrap.responseMessage("{\"afterException\":" + JSON.toJSONString(args) + "}]");
    }
}
