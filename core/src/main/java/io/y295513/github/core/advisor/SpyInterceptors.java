package io.y295513.github.core.advisor;

import com.alibaba.bytekit.asm.binding.Binding;
import com.alibaba.bytekit.asm.interceptor.annotation.*;

import java.arthasx.SpyAPI;

public class SpyInterceptors {

    public static class SpyInterceptor1 {

        @AtEnter(inline = true)
        public static void atEnter(@Binding.This Object target, @Binding.Class Class<?> clazz,
                                   @Binding.MethodInfo String methodInfo, @Binding.Args Object[] args) {
            SpyAPI.beforeInvoke(clazz, args);
        }
    }

    public static class SpyInterceptor2 {
        @AtExit(inline = true)
        public static void atExit(@Binding.This Object target, @Binding.Class Class<?> clazz,
                                  @Binding.MethodInfo String methodInfo, @Binding.Args Object[] args, @Binding.Return Object returnObj) {
            SpyAPI.afterInvoke(clazz, args, returnObj);
        }
    }

    public static class SpyInterceptor3 {
        @AtExceptionExit(inline = true)
        public static void atExceptionExit(@Binding.This Object target, @Binding.Class Class<?> clazz,
                                           @Binding.MethodInfo String methodInfo, @Binding.Args Object[] args,
                                           @Binding.Throwable Throwable throwable) {
            SpyAPI.afterException(clazz, args, throwable);
        }
    }

}
