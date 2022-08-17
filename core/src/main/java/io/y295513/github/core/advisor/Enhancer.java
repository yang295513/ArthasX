package io.y295513.github.core.advisor;

import com.alibaba.bytekit.asm.MethodProcessor;
import com.alibaba.bytekit.asm.interceptor.InterceptorProcessor;
import com.alibaba.bytekit.asm.interceptor.parser.DefaultInterceptorClassParser;
import com.alibaba.bytekit.utils.AsmUtils;
import com.alibaba.deps.org.objectweb.asm.ClassReader;
import com.alibaba.deps.org.objectweb.asm.Opcodes;
import com.alibaba.deps.org.objectweb.asm.tree.ClassNode;
import com.alibaba.deps.org.objectweb.asm.tree.MethodNode;
import io.y295513.github.core.advisor.SpyInterceptors.SpyInterceptor1;
import io.y295513.github.core.advisor.SpyInterceptors.SpyInterceptor2;
import io.y295513.github.core.advisor.SpyInterceptors.SpyInterceptor3;
import io.y295513.github.core.server.ArthasXCoreBootstrap;
import io.y295513.github.core.shell.command.CommandInterface;
import io.y295513.github.core.shell.command.EnhancerCommand;
import lombok.Data;

import java.arthasx.SpyAPI;
import java.io.PrintStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;
import java.util.*;

// todo 待处理cglib问题
@Data
public class Enhancer implements ClassFileTransformer {
    private volatile Map<String, CommandInterface> needEnhancerCommand = new HashMap<>();
    private volatile Map<String, CommandInterface> resetCacheMap = new HashMap<>();
    private final Instrumentation ins;
    private PrintStream ps = ArthasXCoreBootstrap.ps;

    private static Enhancer instance;

    public static synchronized Enhancer getInstance(Instrumentation instrumentation) {
        if (Objects.isNull(instance)) {
            instance = new Enhancer(instrumentation);
        }
        return instance;
    }

    public static synchronized Enhancer getInstance() {
        return instance;
    }

    public synchronized void addCommand(CommandInterface command) throws UnmodifiableClassException {
        if (command instanceof EnhancerCommand) {
            EnhancerCommand enhancerCommand = (EnhancerCommand) command;
            needEnhancerCommand.put(enhancerCommand.getClassName(), enhancerCommand);
            resetCacheMap.remove(enhancerCommand.getClassName());
            ins.retransformClasses(enhancerCommand.getClazz());
        } else {
            // 不受支持

        }
    }

    public synchronized void addReset(CommandInterface command) {
        if (command instanceof EnhancerCommand) {
            EnhancerCommand enhancerCommand = (EnhancerCommand) command;
            needEnhancerCommand.remove(enhancerCommand.getClassName());
            resetCacheMap.put(enhancerCommand.getClassName(), enhancerCommand);
        } else {
            // 不受支持

        }
    }

    @Override
    public synchronized byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        SpyImpl spyImpl = new SpyImpl();
        SpyAPI.setSpy(spyImpl);

        ClassNode classNode = new ClassNode(Opcodes.ASM9);
        ClassReader classReader = AsmUtils.toClassNode(classfileBuffer, classNode);

        // reset
        className = className.replace("/", ".");
        CommandInterface command = resetCacheMap.get(className);
        if (Objects.nonNull(command)) {
            if (!(command instanceof EnhancerCommand)) {
                ps.println("该命令不受支持");
                return classfileBuffer;
            }
            EnhancerCommand enhancerCommand = (EnhancerCommand) command;
            resetCacheMap.remove(className);
            ps.println("还原" + className);
            return enhancerCommand.getNoEnhancerClassBytes();
        }

        command = needEnhancerCommand.get(className);
        if (Objects.isNull(command)) {
            ps.println(className + "无需增强");
            return classfileBuffer;
        }

        if (!(command instanceof EnhancerCommand)) {
            ps.println("该命令不受支持");
            return classfileBuffer;
        }
        EnhancerCommand enhancerCommand = (EnhancerCommand) command;

        DefaultInterceptorClassParser defaultInterceptorClassParser = new DefaultInterceptorClassParser();
        final List<InterceptorProcessor> interceptorProcessors = new ArrayList<>();

        interceptorProcessors.addAll(defaultInterceptorClassParser.parse(SpyInterceptor1.class));
        interceptorProcessors.addAll(defaultInterceptorClassParser.parse(SpyInterceptor2.class));
        interceptorProcessors.addAll(defaultInterceptorClassParser.parse(SpyInterceptor3.class));

        // 记录增强前结果
        enhancerCommand.setNoEnhancerClassBytes(classfileBuffer);

        try {


            for (MethodNode method : classNode.methods) {
                if (method.name.equals(enhancerCommand.getMethodName())) {
                    // 找到了不能立刻结束，因为存在重载方法
                    MethodProcessor methodProcessor = new MethodProcessor(classNode, method);
                    for (InterceptorProcessor interceptor : interceptorProcessors) {
                        interceptor.process(methodProcessor);
                    }
                }
            }

            return AsmUtils.toBytes(classNode, loader, classReader);
        } catch (Exception e) {
            ps.println("增强失败");
            e.printStackTrace(ps);
        }
        return classfileBuffer;
    }

    // 批量增强
    public synchronized void reSet() {
        try {
            Class<?>[] classes = resetCacheMap.values().stream().filter(it -> it instanceof EnhancerCommand).map(it -> ((EnhancerCommand) it).getClazz()).toArray(Class[]::new);
            if (classes.length > 0) {
                ins.retransformClasses(classes);
            }
        } catch (Exception e) {
            e.printStackTrace(ps);
        }
    }
}
