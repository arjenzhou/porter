package de.xab.porter.common.spi;

import de.xab.porter.api.annoation.Inject;

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

/**
 * a extension loader can load any implements of one service, which is describe in resources/extensions.
 * ExtensionLoader load class using current class loader, construct new instance and inject type for consuming
 * <p>
 * extension must have these features:
 * implement at least one service,
 * registered in resources/META-INF/porter
 * <p>
 * any extensions must opens to module {@link de.xab.porter.common}
 * <p>
 * comments after # will be ignored
 */
@SuppressWarnings("unchecked")
public class ExtensionLoader<T> {
    private static final String FOLDER = "META-INF/porter/";
    private static final Map<Class<?>, ExtensionLoader<?>> LOADERS = new ConcurrentHashMap<>();
    private final Map<String, Class<T>> extensions = new ConcurrentHashMap<>();
    private Class<T> service;

    public static <T> ExtensionLoader<T> getExtensionLoader(Class<T> service) {
        if (service == null) {
            throw new IllegalArgumentException("service is null");
        }
        if (!service.isInterface()) {
            throw new IllegalArgumentException(String.format("service %s is not a interface", service));
        }
        ExtensionLoader<T> loader = (ExtensionLoader<T>) LOADERS.get(service);
        if (loader == null) {
            LOADERS.putIfAbsent(service, new ExtensionLoader<T>());
            loader = (ExtensionLoader<T>) LOADERS.get(service);
            loader.service = service;
        }
        return loader;
    }

    private Class<T> loadExtensionClass(String type) {
        Class<T> extensionClass = this.extensions.get(type);
        ClassLoader classLoader = findClassLoader(this.service);
        if (extensionClass == null) {
            synchronized (this.extensions) {
                extensionClass = this.extensions.get(type);
                if (extensionClass == null) {
                    String extensionName = null;
                    try {
                        extensionName = findExtensionName(classLoader, type);
                        extensionClass = (Class<T>) classLoader.loadClass(extensionName);
                    } catch (IOException e) {
                        throw new TypeNotPresentException(type, e);
                    } catch (ClassNotFoundException e) {
                        throw new IllegalArgumentException(
                                String.format("cannot load class %s for %s: %s", extensionName, type, this.service));
                    }
                    if (!isServiceImplementation(extensionClass, this.service)) {
                        throw new IllegalStateException(extensionName + " not implemented " + this.service);
                    }
                    this.extensions.put(type, extensionClass);
                }
            }
        }
        if (!extensionClass.getModule().isOpen(extensionClass.getPackageName(), this.getClass().getModule())) {
            throw new RuntimeException(String.format(
                    "cannot access class %s at %s", extensionClass.getName(), this.getClass().getModule()));
        }
        return extensionClass;
    }

    public T loadExtension(String delegationType, String type) {
        Class<T> clazz = loadExtensionClass(type);
        T t;
        try {
            t = clazz.getConstructor().newInstance();
        } catch (InstantiationException | NoSuchMethodException
                | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(String.format("cannot create extension %s of %s", type, this.service), e);
        }
        inject(clazz, t, delegationType);
        return t;
    }

    /**
     * inject dependencies on setter
     */
    private void inject(Class<T> clazz, T t, String delegationType) {
        if (delegationType != null) {
            for (Method method : clazz.getMethods()) {
                Class<?> dependencyClass = null;
                if (injectable(method)) {
                    try {
                        dependencyClass = method.getParameterTypes()[0];
                        method.invoke(t, ExtensionLoader.getExtensionLoader(dependencyClass).
                                loadExtension(null, delegationType));
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(String.format("unable to inject %s to method %s",
                                dependencyClass, method.getName()), e);
                    }
                }
            }
        }
    }

    private boolean injectable(Method method) {
        return method.isAnnotationPresent(Inject.class)
                && method.getName().startsWith("set")
                && method.getParameterTypes().length == 1
                && Modifier.isPublic(method.getModifiers());
    }

    private String findExtensionName(ClassLoader classLoader, String type) throws IOException {
        Enumeration<URL> urls;
        String resourceFolder = FOLDER + this.service.getName();
        urls = classLoader.getResources(resourceFolder);
        while (urls != null && urls.hasMoreElements()) {
            URL url = urls.nextElement();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    url.openStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    Map.Entry<String, String> tuple = parseLine(line);
                    if (tuple == null) {
                        continue;
                    }
                    String extensionType = tuple.getKey();
                    String extensionName = tuple.getValue();
                    if (type.equals(extensionType)) {
                        return extensionName;
                    }
                }
            }
        }
        throw new IOException("no appropriate type found for " + this.service);
    }

    private Map.Entry<String, String> parseLine(String line) {
        String extensionType;
        String extensionName;
        int ci = line.indexOf('#');
        if (ci >= 0) {
            line = line.substring(0, ci);
        }
        line = line.trim();
        if (line.length() > 0) {
            final String[] split = line.split("=");
            if (split.length == 2) {
                extensionType = split[0];
                extensionName = split[1];
                return Map.entry(extensionType, extensionName);
            }
        }
        return null;
    }

    private boolean isServiceImplementation(Class<T> extensionClass, Class<T> serviceClass) {
        Class<?> currentClass = extensionClass;
        boolean isImplemented = false;
        while (!isImplemented) {
            if (currentClass == Object.class) {
                break;
            }
            isImplemented = Arrays.stream(currentClass.getInterfaces()).
                    anyMatch(service -> service == serviceClass);
            currentClass = currentClass.getSuperclass();
        }
        return isImplemented;
    }

    private ClassLoader findClassLoader(Class<T> clazz) {
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