package io.y295513.github.loader;

import java.net.URL;
import java.net.URLClassLoader;


public class ArthasXClassloader extends URLClassLoader {
    public ArthasXClassloader(URL[] urls) {
        super(urls, ClassLoader.getSystemClassLoader().getParent());
    }

    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        // 判断目标类有没有被加载
        final Class<?> loadedClass = findLoadedClass(name);
        if (loadedClass != null) {
            return loadedClass;
        }

        // 优先从parent（ExtClassLoader -> BootstrapClassLoader）里加载系统类，避免抛出ClassNotFoundException
        if (name != null && (name.startsWith("sun.") || name.startsWith("java."))) {
            // 使用父类的loadClass，父类的loadClass遵循双亲委派模型
            return super.loadClass(name, resolve);
        }
        try {
            // 加载ArthasXCore和Core依赖的三方的加载，不符合抛出ClassNotFoundException
            Class<?> aClass = findClass(name);
            if (resolve) {
                resolveClass(aClass);
            }
            return aClass;
        } catch (Exception e) {
            // ignore
        }
        return super.loadClass(name, resolve);
    }
}
