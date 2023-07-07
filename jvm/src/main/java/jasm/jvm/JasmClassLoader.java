package jasm.jvm;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class JasmClassLoader extends ClassLoader {
    public static final String MAIN_CLASS = "jasm.jvm.JasmClassLoader$Main";

    private final AtomicReference<Class<?>> mainClass = new AtomicReference<>(null);

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (MAIN_CLASS.equals(name)) {
            return mainClass.updateAndGet(current -> current != null ? current : defineMainClass());
        }

        return super.findClass(name);
    }

    private Class<?> defineMainClass() {
        return Class.class;
    }
}
