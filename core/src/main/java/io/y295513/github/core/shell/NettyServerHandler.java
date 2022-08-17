package io.y295513.github.core.shell;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.y295513.github.core.server.ArthasXCoreBootstrap;

import java.nio.charset.StandardCharsets;

@Sharable
public class NettyServerHandler extends ChannelInboundHandlerAdapter {
    private volatile String comment;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object obj) {
        ByteBuf byteBuf = (ByteBuf) obj;
        String[] split = byteBuf.toString(StandardCharsets.UTF_8).split("-");
        setComment(split[1]);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        ChannelGroup channelGroup = ArthasXCoreBootstrap.getInstance().getChannelGroup();
        channelGroup.add(ctx.channel());
    }


    public synchronized String getComment() {
        String command = comment;
        this.comment = null;
        return command;
    }

    public synchronized void setComment(String comment) {
        this.comment = comment;
    }
}
