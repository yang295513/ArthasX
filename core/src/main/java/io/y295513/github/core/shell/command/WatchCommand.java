package io.y295513.github.core.shell.command;

import com.alibaba.bytekit.asm.MethodProcessor;
import com.alibaba.bytekit.asm.interceptor.InterceptorProcessor;
import com.alibaba.bytekit.asm.interceptor.parser.DefaultInterceptorClassParser;
import com.alibaba.bytekit.utils.AsmUtils;
import com.alibaba.deps.org.objectweb.asm.ClassReader;
import com.alibaba.deps.org.objectweb.asm.Opcodes;
import com.alibaba.deps.org.objectweb.asm.tree.ClassNode;
import com.alibaba.deps.org.objectweb.asm.tree.MethodNode;
import com.google.common.base.Throwables;
import io.y295513.github.core.advisor.SpyImpl;
import io.y295513.github.core.advisor.SpyInterceptors.SpyInterceptor1;
import io.y295513.github.core.advisor.SpyInterceptors.SpyInterceptor2;
import io.y295513.github.core.advisor.SpyInterceptors.SpyInterceptor3;
import io.y295513.github.core.server.ArthasXCoreBootstrap;

import java.arthasx.SpyAPI;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public class WatchCommand extends EnhancerCommand {

    @Override
    public boolean hasMatch(String command) {
        return command.contains("watch");
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        if (this.getClassName().equals(className)) {
            SpyImpl spyImpl = new SpyImpl();
            SpyAPI.setSpy(spyImpl);
            DefaultInterceptorClassParser defaultInterceptorClassParser = new DefaultInterceptorClassParser();
            final List<InterceptorProcessor> interceptorProcessors = new ArrayList<>();

            interceptorProcessors.addAll(defaultInterceptorClassParser.parse(SpyInterceptor1.class));
            interceptorProcessors.addAll(defaultInterceptorClassParser.parse(SpyInterceptor2.class));
            interceptorProcessors.addAll(defaultInterceptorClassParser.parse(SpyInterceptor3.class));

            this.setNoEnhancerClassBytes(classfileBuffer);

            try {
                ClassNode classNode = new ClassNode(Opcodes.ASM9);
                ClassReader classReader = AsmUtils.toClassNode(classfileBuffer, classNode);

                Set<String> methods = classNode.methods.stream().map(it -> it.name).collect(Collectors.toSet());
                if (!methods.contains(this.getMethodName())) {
                    ArthasXCoreBootstrap.responseMessage("方法不存在" + this.getClassName() + "方法名" + this.getMethodName());
                    return classfileBuffer;
                }

                for (MethodNode method : classNode.methods) {
                    if (method.name.equals(this.getMethodName())) {
                        // 找到了不能立刻结束，因为存在重载方法
                        MethodProcessor methodProcessor = new MethodProcessor(classNode, method);
                        for (InterceptorProcessor interceptor : interceptorProcessors) {
                            interceptor.process(methodProcessor);
                        }
                    }
                }

                return AsmUtils.toBytes(classNode, loader, classReader);
            } catch (Exception e) {
                ArthasXCoreBootstrap.responseMessage(Throwables.getStackTraceAsString(e));
            }
            return classfileBuffer;
        }

        return classfileBuffer;
    }
}
