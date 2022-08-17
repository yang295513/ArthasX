package io.y295513.github.core;

import com.google.common.collect.Lists;
import com.sun.tools.attach.VirtualMachine;
import io.y295513.github.core.netty.NettyClient;
import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.locks.LockSupport;

public class ArthasXClient {
    private static final String agentPath = System.getProperty("user.dir") + "\\agent\\target\\agent-1.0-SNAPSHOT.jar";


    public static void main(String[] args) throws IOException {
        String resultStr = execCommandLine("jps -l");
        Scanner scanner = new Scanner(System.in);
        // 解析并打印命令
        String[] lines = resultStr.split("\n");
        List<String> logs = Lists.newArrayListWithCapacity(lines.length);
        int count = 0;
        List<String> pidList = Lists.newArrayListWithCapacity(lines.length);
        for (String line : lines) {
            String[] strings = line.split(" ");
            pidList.add(strings[0]);

            if (strings.length < 2) {
                logs.add("[" + count++ + "]" + ":" + "未知进程");
            } else {
                logs.add("[" + count++ + "]" + ":" + strings[1]);
            }
        }
        logs.forEach(ArthasXClient::println);

        println("请输入序号");
        int index = -1;
        do {
            if (scanner.hasNextInt()) {
                index = scanner.nextInt();
                scanner.nextLine();
            }
        } while (index < 0 || index >= lines.length);

        // 准备连接虚拟机
        println("准备连接到" + pidList.get(index));
        VirtualMachine vm = null;
        try {
            vm = VirtualMachine.attach(pidList.get(index));
            println("连接成功");
            vm.loadAgent(agentPath);
            println("loadAgent成功");

            NettyClient nettyClient = new NettyClient();
            nettyClient.initNettyClient();
            while (true) {
                print(">");
                if (scanner.hasNext()) {
                    String cmd = scanner.nextLine();
                    if ("stop".equals(cmd)) {
                        nettyClient.getChannel().closeFuture();
                        break;
                    }
                    nettyClient.sendCommand("tenantId-" + cmd);
                    LockSupport.park();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (Objects.nonNull(vm)) {
                vm.detach();
                scanner.close();
            }
        }
    }

    @SneakyThrows
    public static String execCommandLine(String command) {
        Process exec = Runtime.getRuntime().exec(command);
        BufferedReader bufferedInputStream = new BufferedReader(new InputStreamReader(exec.getInputStream()));
        StringBuilder stringBuilder = new StringBuilder();
        String buffer;

        while ((buffer = bufferedInputStream.readLine()) != null) {
            stringBuilder.append(buffer).append("\n");
        }

        return stringBuilder.toString();
    }


    public static void println(String str) {
        System.out.println(str);
    }

    public static void print(String str) {
        System.out.print(str);
    }
}
