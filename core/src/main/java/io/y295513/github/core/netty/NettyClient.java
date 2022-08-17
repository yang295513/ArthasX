package io.y295513.github.core.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;
import lombok.Data;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.locks.LockSupport;

@Data
public class NettyClient {
    private Channel channel;

    public void initNettyClient() throws InterruptedException {
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup).channel(NioSocketChannel.class).handler(new ClientHandler(Thread.currentThread()));

        ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", 9999);

        channelFuture.sync();

        this.channel = channelFuture.channel();
    }

    public void sendCommand(String command) {
        this.channel.writeAndFlush(Unpooled.copiedBuffer(command.getBytes(StandardCharsets.UTF_8)));
    }
}

class ClientHandler extends ChannelInboundHandlerAdapter {
    private final Thread mainThread;

    public ClientHandler(Thread mainThread) {
        this.mainThread = mainThread;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        // 从管道读消息
        ByteBuf buf = (ByteBuf) msg; // 转为ByteBuf类型
        String m = buf.toString(CharsetUtil.UTF_8);  // 转为字符串
        System.out.println(m);
        LockSupport.unpark(mainThread);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // 当出现异常就关闭连接
        cause.printStackTrace();
        ctx.close();
    }
}
