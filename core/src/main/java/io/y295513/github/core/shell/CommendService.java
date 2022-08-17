package io.y295513.github.core.shell;

import com.google.common.base.Throwables;
import io.y295513.github.core.advisor.Enhancer;
import io.y295513.github.core.server.ArthasXCoreBootstrap;
import io.y295513.github.core.shell.command.CommandFactory;
import io.y295513.github.core.shell.command.CommandInterface;
import lombok.Data;

import java.lang.instrument.Instrumentation;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static io.y295513.github.core.server.ArthasXCoreBootstrap.ps;
import static io.y295513.github.core.server.ArthasXCoreBootstrap.responseMessage;

@Data
public class CommendService implements Runnable {
    private final Instrumentation ins;

    @Override
    public void run() {
        try {
            ArthasXCoreBootstrap bootstrap = ArthasXCoreBootstrap.getInstance();
            CommandFactory factory = bootstrap.getCommandFactory();
            NettyServerHandler nettyServerHandler = bootstrap.getNettyServerHandler();
//            Enhancer enhancer = Enhancer.getInstance(ins);
//            ins.addTransformer(enhancer);
            while (true) {
                try {
                    String command = nettyServerHandler.getComment();
                    if (Objects.nonNull(command)) {
                        // 执行命令
                        ps.println("收到命令" + command);
                        factory.processCommand(command);
//                        CommandInterface resetCommand = factory.createCommand(command);
//                        enhancer.addCommand(resetCommand);
                    }
                    TimeUnit.SECONDS.sleep(1);
                } catch (Throwable e) {
                    e.printStackTrace(ps);
                    responseMessage(Throwables.getStackTraceAsString(e));
                }
            }
        } catch (Throwable e) {
            e.printStackTrace(ps);
        }
    }
}
