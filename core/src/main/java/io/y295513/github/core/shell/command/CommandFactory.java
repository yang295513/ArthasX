package io.y295513.github.core.shell.command;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static io.y295513.github.core.server.ArthasXCoreBootstrap.ps;
import static io.y295513.github.core.server.ArthasXCoreBootstrap.responseMessage;

@Data
public class CommandFactory {
    private static List<CommandInterface> commandInterfaces = new ArrayList<>();
    private final Instrumentation instrumentation;

    static {
        // todo 调整成多例
        commandInterfaces.add(new WatchCommand());
    }

    public CommandInterface createCommand(String command) throws ClassNotFoundException {
        if (Objects.isNull(command)) {
            return null;
        }
        for (CommandInterface commandInterface : commandInterfaces) {
            if (commandInterface.hasMatch(command)) {
                if (commandInterface instanceof EnhancerCommand) {
                    EnhancerCommand enhancerCommand = (EnhancerCommand) commandInterface;

                    String[] split = command.split(" ");
                    if (split.length != 3) {
                        ps.println("命令不合法" + command);
                    }

                    enhancerCommand.setClassName(split[1]);
                    enhancerCommand.setMethodName(split[2]);
                    enhancerCommand.setClazz(Thread.currentThread().getContextClassLoader().loadClass(split[1]));
                } else {
                    // todo 目前不受支持
                    ps.println("不受支持的命令" + JSONObject.toJSONString(command));
                    continue;
                }

                return commandInterface;
            }
        }
        return null;
    }

    public CommandInterface processCommand(String command) throws ClassNotFoundException, UnmodifiableClassException {
        if (Objects.isNull(command)) {
            return null;
        }
        for (CommandInterface commandInterface : commandInterfaces) {
            if (commandInterface.hasMatch(command)) {
                String className;
                if (commandInterface instanceof EnhancerCommand) {
                    EnhancerCommand enhancerCommand = (EnhancerCommand) commandInterface;

                    String[] split = command.split(" ");
                    if (split.length < 3) {
                        responseMessage("命令不合法" + command);
                        continue;
                    }

                    className = split[1];
                    enhancerCommand.setClassName(split[1].replace(".", "/"));
                    enhancerCommand.setMethodName(split[2]);
                } else {
                    // todo 目前不受支持
                    responseMessage("不受支持的命令" + JSONObject.toJSONString(command));
                    continue;
                }

                instrumentation.addTransformer(commandInterface, true);
                // Class.forName使用的是调用者的类加载器加载
                ps.println("重新加载类" + className);
                instrumentation.retransformClasses(Thread.currentThread().getContextClassLoader().loadClass(className));
                return commandInterface;
            }
            responseMessage("不受支持的命令" + JSONObject.toJSONString(command));
        }
        return null;
    }

}
