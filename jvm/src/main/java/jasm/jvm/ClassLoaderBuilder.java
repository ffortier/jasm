package jasm.jvm;

import jasm.runtime.Module;

public class ClassLoaderBuilder {
    public static ClassLoaderBuilder fromModule(Module module) {
        return new ClassLoaderBuilder();
    }

    public <T> T build(Class<T> interfaceClass) {

        return null;
    }

    static class Loader extends ClassLoader {
        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            return super.findClass(name);
        }
    }
}
