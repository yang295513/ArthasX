package io.y295513.github.core.shell.command;

import lombok.Data;

import java.lang.instrument.ClassFileTransformer;

/**
 * @author yang.yue
 */
@Data
public abstract class EnhancerCommand implements CommandInterface, ClassFileTransformer {
    private String className;
    private String methodName;
    private byte[] noEnhancerClassBytes;
    private Class<?> clazz;
}
