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

/**
 * a extension loader can load any implements of one service, which is describe in resources/extensions.
 * ExtensionLoader load class using current class loader, construct new instance and inject type for consuming
 * <p>
 * extension must have these features:
 * implement at least one service,
 * registered in resources/extensions
 * <p>
 * any service must opens to module {@link de.xab.porter.common}
 * <p>
 * comments after # will be ignored
 */
public class ExtensionLoader {
    private static final String FOLDER = "";
    private static final Map<Class<?>, Map<String, Class<?>>> EXTENSION_HOLDER = new ConcurrentHashMap<>();

    public static ExtensionLoader getExtensionLoader() {
        return new ExtensionLoader();
    }

    public <T> T loadExtension(String type, Class<T> clazz) {
        final Class<?> driverClass = loadClass(type, clazz);
        if (!driverClass.getModule().isOpen(driverClass.getPackageName(), this.getClass().getModule())) {
            throw new PorterException(String.format(
                    "cannot access class %s at %s", driverClass.getName(), this.getClass().getModule()));
        }
        try {
            final T instance = (T) driverClass.getConstructor().newInstance();
            injectExtension(instance, type);
            return instance;
        } catch (InstantiationException | IllegalAccessException
                | InvocationTargetException | NoSuchMethodException e) {
            throw new PorterException("extension construct failed", e);
        }
    }

    /**
     * load Class of extensions. PorterException will be thrown as load failed
     *
     * @param type  exactly type of extension
     * @param clazz Class of service
     * @return Class of extension
     */
    public Class<?> loadClass(String type, Class<?> clazz) {
        Class<?> subClass = EXTENSION_HOLDER.computeIfAbsent(clazz, ignored -> new ConcurrentHashMap<>()).get(type);
        if (subClass != null) {
            return subClass;
        }
        final String fileName = FOLDER + clazz.getName();
        final Enumeration<URL> urls;
        final ClassLoader cl = findClassLoader(clazz);
        try {
            urls = cl.getResources(fileName);
        } catch (IOException e) {
            throw new PorterException(String.format("no implement(s) of %s found in %s", clazz.getName(), fileName));
        }
        while (urls != null && urls.hasMoreElements()) {
            final URL url = urls.nextElement();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    url.openStream(), StandardCharsets.UTF_8))) {
                String line;
                final Map<String, Class<?>> specifyClassMap = EXTENSION_HOLDER.get(clazz);
                while ((line = reader.readLine()) != null) {
                    Map.Entry<String, String> entry = parseTypeAndClass(line, clazz);
                    String driverType = entry.getKey();
                    String className = entry.getValue();
                    if (notNullOrEmpty(className)) {
                        subClass = loadClassByName(cl, clazz, className);
                        specifyClassMap.putIfAbsent(driverType, subClass);
                        EXTENSION_HOLDER.put(clazz, specifyClassMap);
                        if (type.equals(driverType)) {
                            return subClass;
                        }
                    }
                }
            } catch (IOException e) {
                throw new PorterException(String.format("load extension class %s failed", clazz.getName()), e);
            }
        }
        throw new PorterException(String.format("type `%s` of extension %s not found", type, clazz.getName()));
    }

    private Map.Entry<String, String> parseTypeAndClass(String line, Class<?> clazz) {
        String driverType;
        String className;
        String newLine = line;
        final int ci = newLine.indexOf('#');
        if (ci >= 0) {
            newLine = newLine.substring(0, ci);
        }
        newLine = newLine.trim();
        if (newLine.length() > 0) {
            final String[] split = newLine.split("=");
            if (split.length == 2) {
                driverType = split[0];
                className = split[1];
                return Map.entry(driverType, className);
            }
        }
        throw new PorterException(String.format(
                "parse extension %s failed. expected `type=foo.bar.Extension`, got %s", clazz.getSimpleName(), line));
    }

    /**
     * load class by given Class name
     *
     * @param loader    classloader to load class
     * @param service   service of class implemented
     * @param className class name to be loaded
     * @return Class if load successfully
     */
    private Class<?> loadClassByName(ClassLoader loader, Class<?> service, String className) {
        try {
            Class<?> subClass = loader.loadClass(className);
            if (implementedInterface(subClass, service)) {
                return subClass;
            } else {
                throw new PorterException(String.format("no implement classes found of class %s", service));
            }
        } catch (ClassNotFoundException e) {
            throw new PorterException(String.format("class %s not found", className), e);
        }
    }

    /**
     * whether extension defined in resources implemented service.
     *
     * @param subClass       extension Class
     * @param interfaceClass service Class
     * @return true if extension if sub class of service
     */
    private boolean implementedInterface(Class<?> subClass, Class<?> interfaceClass) {
        Class<?> currentClass = subClass;
        boolean isImplemented = false;
        while (!isImplemented) {
            if (currentClass == Object.class) {
                break;
            }
            isImplemented = Arrays.stream(currentClass.getInterfaces()).
                    anyMatch(oneInterface -> oneInterface == interfaceClass);
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
