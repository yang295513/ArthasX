package io.y295513.github.core.server;

import com.alibaba.fastjson.JSON;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.y295513.github.core.config.Configure;
import io.y295513.github.core.shell.CommendService;
import io.y295513.github.core.shell.NettyServerHandler;
import io.y295513.github.core.shell.command.CommandFactory;
import io.y295513.github.core.shell.command.CommandInterface;
import lombok.Data;

import javax.crypto.interfaces.PBEKey;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.instrument.Instrumentation;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.JarFile;


/**
 * 直接new出来的对象使用的是调用者的类加载器，即ArthasXClassLoader
 *
 * @author yang.yue
 */
@Data
public class ArthasXCoreBootstrap {
    private static ArthasXCoreBootstrap arthasXBootstrap;
    private Thread commendService;
    private Configure configure;
    private AtomicBoolean isBindRef = new AtomicBoolean(false);
    private Instrumentation instrumentation;
    private CommandFactory commandFactory;
    private NettyServerHandler nettyServerHandler = new NettyServerHandler();

    private ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private ChannelFuture channelFuture;
    private Thread shutdown;

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

    public synchronized static ArthasXCoreBootstrap getInstance(Instrumentation instrumentation, String args) throws Throwable {
        if (arthasXBootstrap != null) {
            return arthasXBootstrap;
        }
        return getInstance(instrumentation, new HashMap<>());
    }

    /**
     * 单例
     *
     * @param instrumentation JVM增强
     * @return ArthasXCoreBootstrap 单例
     * @throws Throwable
     */
    public synchronized static ArthasXCoreBootstrap getInstance(Instrumentation instrumentation, Map<String, String> args) throws Throwable {
        if (arthasXBootstrap == null) {
            arthasXBootstrap = new ArthasXCoreBootstrap(instrumentation, args);
        }
        return arthasXBootstrap;
    }

    public synchronized static ArthasXCoreBootstrap getInstance() {
        return arthasXBootstrap;
    }


    private ArthasXCoreBootstrap(Instrumentation instrumentation, Map<String, String> args) throws Throwable {
        this.instrumentation = instrumentation;
        this.configure = stuffConfig(args);

        // 初始化spy
        initSpy();
        this.commandFactory = new CommandFactory(instrumentation);

        // 启动agent服务
        bind(configure);
        commendService = new Thread(new CommendService(instrumentation), "commendService");
        commendService.start();
        ps.println("agent服务启动成功");

        // 注册关闭回调
        shutdown = new Thread(ArthasXCoreBootstrap.this::destroy, "shutdown-hooker");
        Runtime.getRuntime().addShutdownHook(shutdown);

        isBindRef.set(true);
    }

    private Configure stuffConfig(Map<String, String> args) {
        // todo 可以使用 外部传入参数解析
        return new Configure(args);
    }

    private void destroy() {
        if (Objects.isNull(shutdown)) {
            Runtime.getRuntime().removeShutdownHook(shutdown);
        }
        this.channelFuture.channel().closeFuture();
    }


    // 开启连接
    private void bind(Configure configure) {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(new DefaultThreadFactory("arthas-NettyBoss", true));
        NioEventLoopGroup workerGroup = new NioEventLoopGroup(new DefaultThreadFactory("arthas-NettyWorker", true));

        serverBootstrap.group(bossGroup, workerGroup)
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) {
                        nioSocketChannel.pipeline().addLast(nettyServerHandler);
                    }
                });

        this.channelFuture = serverBootstrap.bind(configure.getPort());
    }

    private void initSpy() throws Throwable {
        ps.println("初始化spy");
        ClassLoader parent = ClassLoader.getSystemClassLoader().getParent();
        Class<?> spyClass = null;
        if (parent != null) {
            try {
                spyClass = parent.loadClass(configure.getSpyApiClassNAME());
            } catch (Throwable e) {
                // 忽略
            }
        }
        if (spyClass == null) {
            File spyJar = new File(configure.getSpyJarPath());
            // 将Spy添加到BootstrapClassLoader
            instrumentation.appendToBootstrapClassLoaderSearch(new JarFile(spyJar));
        }
    }

    /**
     * 判断服务端是否已经启动
     *
     * @return true:服务端已经启动;false:服务端关闭
     */
    public boolean isBind() {
        return isBindRef.get();
    }

    public static void responseMessage(String msg) {
        ArthasXCoreBootstrap instance = ArthasXCoreBootstrap.getInstance();
        instance.getChannelGroup().forEach(ch -> ch.writeAndFlush(Unpooled.copiedBuffer(msg.getBytes(StandardCharsets.UTF_8))));
        ps.println(msg);
    }
}
