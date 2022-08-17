package io.y295513.github.core.shell.command;

import java.lang.instrument.ClassFileTransformer;

public interface CommandInterface extends ClassFileTransformer {
    boolean hasMatch(String command);

}
