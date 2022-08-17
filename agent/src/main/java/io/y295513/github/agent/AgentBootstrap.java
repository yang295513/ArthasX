package io.y295513.github.agent;

import io.y295513.github.loader.ArthasXClassloader;

import java.arthasx.SpyAPI;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.instrument.Instrumentation;
import java.net.URL;

/**
 * @author yang.yue
 */
public class AgentBootstrap {
    private static final String ARTHASX_CORE_PATH = "D:\\myCode\\ArthasX\\core\\target\\arthasX-core.jar";
    private static final String SPY_API_CLASS_PATH = "java.arthasx.SpyAPI";
    private static final String ARTHASX_CORE_BOOTSTRAP_CLASSPATH = "io.y295513.github.core.server.ArthasXCoreBootstrap";
    private static final String ARTHASX_CORE_BOOTSTRAP_INSTANCE_NAME = "getInstance";
    private static final String IS_BIND_METHOD_NAME = "isBind";

    private static volatile ClassLoader arthasXClassLoader;

    public static void premain(String args, Instrumentation inst) {
        main(args, inst);
    }

    public static void agentmain(String args, Instrumentation inst) {
        main(args, inst);
    }

    public static PrintStream ps = System.err;

    static {
        try {
            File log = new File(System.getProperty("user.dir"), "arthasX.log");
            if (!log.exists()) {
                log.createNewFile();
            }
            ps = new PrintStream(new FileOutputStream(log, true));
        } catch (Throwable t) {
            t.printStackTrace(ps);
        }
    }

    private static synchronized void main(String args, Instrumentation inst) {
        println("Agent启动!!!");
        // 尝试判断arthasx是否已在运行，如果是的话，直接就退出
        try {
            // 加载不到会抛异常
            Class.forName(SPY_API_CLASS_PATH);
            if (SpyAPI.isInited()) {
                println("跳过初始化");
                return;
            }
        } catch (Throwable e) {
            // ignore
        }
        // 加载Core
        try {
            File arthasXCoreJarFile = new File(ARTHASX_CORE_PATH);
            ClassLoader arthasXLoader = getClassLoader(arthasXCoreJarFile);

            Thread bindingThread = new Thread(() -> {
                try {
                    bind(inst, arthasXLoader, args);
                } catch (Throwable throwable) {
                    throwable.printStackTrace(ps);
                }
            }, "arthasx-binding-thread");

            bindingThread.start();
            bindingThread.join();
        } catch (Throwable ex) {
            ex.printStackTrace(ps);
        }
    }

    private static void bind(Instrumentation inst, ClassLoader agentLoader, String args) throws Throwable {
        Class<?> bootstrapClass = agentLoader.loadClass(ARTHASX_CORE_BOOTSTRAP_CLASSPATH);
        Object bootstrap = bootstrapClass.getMethod(ARTHASX_CORE_BOOTSTRAP_INSTANCE_NAME, Instrumentation.class, String.class).invoke(null, inst, args);
        boolean isBind = (Boolean) bootstrapClass.getMethod(IS_BIND_METHOD_NAME).invoke(bootstrap);
        if (!isBind) {
            println("绑定失败");
        }
        println("绑定成功");
    }


    private static ClassLoader getClassLoader(File arthasXCoreJarFile) throws Throwable {
        // 构造自定义的类加载器，尽量减少ArthasX对现有工程的侵蚀
        return loadOrDefineClassLoader(arthasXCoreJarFile);
    }

    private static ClassLoader loadOrDefineClassLoader(File arthasXCoreJarFile) throws Throwable {
        if (arthasXClassLoader == null) {
            arthasXClassLoader = new ArthasXClassloader(new URL[]{arthasXCoreJarFile.toURI().toURL()});
        }
        return arthasXClassLoader;
    }

    public static void println(String msg) {
        ps.println(msg);
    }
}
