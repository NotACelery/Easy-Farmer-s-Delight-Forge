package dev.celerbi.easyfarmersdelightcompat.integration;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class ReflectionCache {
    private record MethodKey(Class<?> type, String name, List<Class<?>> parameters) {
    }

    private record ArityMethodKey(Class<?> type, String name, int parameterCount, boolean declared) {
    }

    private record FieldKey(Class<?> type, String name) {
    }

    private record ConstructorKey(Class<?> type, List<Class<?>> parameters) {
    }

    private static final Map<String, Optional<Class<?>>> TYPE_CACHE = new ConcurrentHashMap<>();
    private static final Map<MethodKey, Optional<Method>> METHOD_CACHE = new ConcurrentHashMap<>();
    private static final Map<ArityMethodKey, Optional<Method>> ARITY_METHOD_CACHE = new ConcurrentHashMap<>();
    private static final Map<FieldKey, Optional<Field>> FIELD_CACHE = new ConcurrentHashMap<>();
    private static final Map<ConstructorKey, Optional<Constructor<?>>> CONSTRUCTOR_CACHE = new ConcurrentHashMap<>();

    private ReflectionCache() {
    }

    public static Class<?> type(String className) throws ClassNotFoundException {
        Optional<Class<?>> cached = TYPE_CACHE.computeIfAbsent(className, name -> {
            try {
                return Optional.of(Class.forName(name));
            } catch (ClassNotFoundException | LinkageError ignored) {
                return Optional.empty();
            }
        });
        if (cached.isEmpty()) {
            throw new ClassNotFoundException(className);
        }
        return cached.get();
    }

    public static Method publicMethod(Class<?> type, String name, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        MethodKey key = new MethodKey(type, name, parameterList(parameterTypes));
        Optional<Method> cached = METHOD_CACHE.computeIfAbsent(key, ignored -> {
            try {
                return Optional.of(type.getMethod(name, parameterTypes));
            } catch (NoSuchMethodException | LinkageError exception) {
                return Optional.empty();
            }
        });
        if (cached.isEmpty()) {
            throw new NoSuchMethodException(type.getName() + "#" + name);
        }
        return cached.get();
    }

    public static Method publicMethodByArity(Class<?> type, String name, int parameterCount)
            throws NoSuchMethodException {
        return methodByArity(type, name, parameterCount, false);
    }

    public static Method declaredMethodByArity(Class<?> type, String name, int parameterCount)
            throws NoSuchMethodException {
        return methodByArity(type, name, parameterCount, true);
    }

    private static Method methodByArity(Class<?> type, String name, int parameterCount, boolean declared)
            throws NoSuchMethodException {
        ArityMethodKey key = new ArityMethodKey(type, name, parameterCount, declared);
        Optional<Method> cached = ARITY_METHOD_CACHE.computeIfAbsent(key, ignored -> {
            if (!declared) {
                for (Method method : type.getMethods()) {
                    if (method.getName().equals(name) && method.getParameterCount() == parameterCount) {
                        return Optional.of(method);
                    }
                }
                return Optional.empty();
            }

            for (Class<?> current = type; current != null; current = current.getSuperclass()) {
                for (Method method : current.getDeclaredMethods()) {
                    if (method.getName().equals(name) && method.getParameterCount() == parameterCount) {
                        try {
                            method.setAccessible(true);
                        } catch (RuntimeException ignoredAccess) {
                            return Optional.empty();
                        }
                        return Optional.of(method);
                    }
                }
            }
            return Optional.empty();
        });
        if (cached.isEmpty()) {
            throw new NoSuchMethodException(type.getName() + "#" + name + "/" + parameterCount);
        }
        return cached.get();
    }

    public static Field field(Class<?> type, String name) throws NoSuchFieldException {
        FieldKey key = new FieldKey(type, name);
        Optional<Field> cached = FIELD_CACHE.computeIfAbsent(key, ignored -> {
            for (Class<?> current = type; current != null; current = current.getSuperclass()) {
                try {
                    Field field = current.getDeclaredField(name);
                    field.setAccessible(true);
                    return Optional.of(field);
                } catch (NoSuchFieldException ignoredField) {
                } catch (RuntimeException ignoredAccess) {
                    return Optional.empty();
                }
            }
            return Optional.empty();
        });
        if (cached.isEmpty()) {
            throw new NoSuchFieldException(type.getName() + "#" + name);
        }
        return cached.get();
    }

    public static Constructor<?> constructor(Class<?> type, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        ConstructorKey key = new ConstructorKey(type, parameterList(parameterTypes));
        Optional<Constructor<?>> cached = CONSTRUCTOR_CACHE.computeIfAbsent(key, ignored -> {
            try {
                return Optional.of(type.getConstructor(parameterTypes));
            } catch (NoSuchMethodException | LinkageError exception) {
                return Optional.empty();
            }
        });
        if (cached.isEmpty()) {
            throw new NoSuchMethodException(type.getName() + " constructor");
        }
        return cached.get();
    }

    private static List<Class<?>> parameterList(Class<?>[] parameterTypes) {
        return List.copyOf(Arrays.asList(parameterTypes));
    }
}
