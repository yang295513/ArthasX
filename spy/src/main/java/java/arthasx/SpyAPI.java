package java.arthasx;

public class SpyAPI {
    public static final AbstractSpy NOPSPY = new NopSpy();
    private static volatile AbstractSpy spyInstance = NOPSPY;

    public static volatile boolean INITED;

    public static AbstractSpy getSpy() {
        return spyInstance;
    }

    public static void setSpy(AbstractSpy spy) {
        spyInstance = spy;
    }

    public static void setNopSpy() {
        setSpy(NOPSPY);
    }

    public static boolean isNopSpy() {
        return NOPSPY == spyInstance;
    }

    public static void init() {
        INITED = true;
    }

    public static boolean isInited() {
        return INITED;
    }

    public static void destroy() {
        setNopSpy();
        INITED = false;
    }

    public static void beforeInvoke(Class<?> clazz, Object[] args) {
        spyInstance.beforeInvoke(clazz, args);
    }

    public static void afterInvoke(Class<?> clazz, Object[] args,
                                   Object returnObject) {
        spyInstance.afterInvoke(clazz, args, returnObject);
    }

    public static void afterException(Class<?> clazz,
                                      Object[] args, Throwable throwable) {
        spyInstance.afterException(clazz, args, throwable);
    }

    public static abstract class AbstractSpy {
        public abstract void beforeInvoke(Class<?> clazz,
                                          Object[] args);

        public abstract void afterInvoke(Class<?> clazz, Object[] args,
                                         Object returnObject);

        public abstract void afterException(Class<?> clazz,
                                            Object[] args, Throwable throwable);
    }

    static class NopSpy extends AbstractSpy {


        @Override
        public void beforeInvoke(Class<?> clazz, Object[] args) {

        }

        @Override
        public void afterInvoke(Class<?> clazz, Object[] args, Object returnObject) {

        }

        @Override
        public void afterException(Class<?> clazz, Object[] args, Throwable throwable) {

        }
    }
}
