package de.xab.porter.common.spi;

import de.xab.porter.api.exception.PorterException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static de.xab.porter.common.util.Strings.notNullOrEmpty;

public class ExtensionLoader {
    private static final String FOLDER = "extensions/";
    private static final Map<Class<?>, Map<String, Class<?>>> EXTENSION_HOLDER = new ConcurrentHashMap<>();

    public static ExtensionLoader getExtensionLoader() {
        return new ExtensionLoader();
    }

    public <T> T loadExtension(String type, Class<T> clazz) {
        Class<?> driverClass = loadClass(type, clazz);
        if (!driverClass.getModule().isOpen(driverClass.getPackageName(), this.getClass().getModule())) {
            throw new PorterException
                    (String.format("cannot access class %s at %s", driverClass.getName(), this.getClass().getModule()));
        }
        try {
            T instance = (T) driverClass.getConstructor().newInstance();
            injectExtension(instance, type);
            return instance;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new PorterException("extension construct failed", e);
        }
    }

    public Class<?> loadClass(String type, Class<?> clazz) {
        Class<?> subClass = EXTENSION_HOLDER.computeIfAbsent(clazz, ignored -> new ConcurrentHashMap<>()).get(type);
        if (subClass != null) {
            return subClass;
        }
        String fileName = FOLDER + clazz.getName();
        Enumeration<URL> urls;
        ClassLoader cl = findClassLoader(clazz);
        try {
            urls = cl.getResources(fileName);
        } catch (IOException e) {
            throw new PorterException(String.format("no implement(s) of %s found in %s", clazz.getName(), fileName));
        }
        while (urls != null && urls.hasMoreElements()) {
            URL url = urls.nextElement();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    url.openStream(), StandardCharsets.UTF_8))) {
                String line;
                Map<String, Class<?>> specifyClassMap = EXTENSION_HOLDER.get(clazz);
                while ((line = reader.readLine()) != null) {
                    String driverType = null;
                    String className = null;
                    int ci = line.indexOf('#');
                    if (ci >= 0) {
                        line = line.substring(0, ci);
                    }
                    line = line.trim();
                    if (line.length() > 0) {
                        String[] split = line.split("=");
                        if (split.length == 2) {
                            driverType = split[0];
                            className = split[1];
                        } else {
                            throw new PorterException(String.format
                                    ("parse extension %s failed. expected `ExtensionClass=foo.bar.Driver`, got %s",
                                            clazz.getSimpleName(), line));
                        }
                    }
                    if (notNullOrEmpty(className)) {
                        try {
                            subClass = cl.loadClass(className);
                            if (implementedInterface(subClass, clazz)) {
                                specifyClassMap.putIfAbsent(driverType, subClass);
                                EXTENSION_HOLDER.put(clazz, specifyClassMap);
                                if (type.equals(driverType)) {
                                    return subClass;
                                }
                            } else {
                                throw new PorterException
                                        (String.format("no implement classes found in %s of class %s", fileName, clazz));
                            }
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                            throw new PorterException(String.format("class %s not found", className), e);
                        }
                    }
                }
            } catch (IOException e) {
                throw new PorterException(String.format("load extension class %s failed", clazz.getName()), e);
            }
        }
        throw new PorterException(String.format("type `%s` of extension %s not found", type, clazz.getName()));
    }

    private boolean implementedInterface(Class<?> subClass, Class<?> interfaceClass) {
        Class<?> currentClass = subClass;
        boolean isImplemented = false;
        while (!isImplemented) {
            if (currentClass == Object.class) {
                break;
            }
            isImplemented = Arrays.stream(currentClass.getInterfaces())
                    .anyMatch(oneInterface -> oneInterface == interfaceClass);
            currentClass = currentClass.getSuperclass();
        }
        return isImplemented;
    }

    private <T> void injectExtension(T instance, String type) {
        for (Method method : instance.getClass().getMethods()) {
            if (!isTypeSetter(method)) {
                continue;
            }
            try {
                method.invoke(instance, type);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new PorterException(String.format("instance %s inject %s failed", instance, type));
            }
        }
    }

    private boolean isTypeSetter(Method method) {
        return method.getName().startsWith("setType")
                && Modifier.isPublic(method.getModifiers())
                && method.getParameterCount() == 1;
    }

    private ClassLoader findClassLoader(Class<?> clazz) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = clazz.getClassLoader();
        }
        if (cl == null) {
            cl = ClassLoader.getSystemClassLoader();
        }
        return cl;
    }
}
