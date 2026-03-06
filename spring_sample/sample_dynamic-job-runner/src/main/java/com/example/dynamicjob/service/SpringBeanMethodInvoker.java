package com.example.dynamicjob.service;

import com.example.dynamicjob.entity.JobDefinition;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component
public class SpringBeanMethodInvoker {

    private final ApplicationContext applicationContext;
    private final ObjectMapper objectMapper;

    public SpringBeanMethodInvoker(ApplicationContext applicationContext, ObjectMapper objectMapper) {
        this.applicationContext = applicationContext;
        this.objectMapper = objectMapper;
    }

    public void invoke(JobDefinition def) throws Exception {
        Object bean = applicationContext.getBean(def.getBeanName());
        Class<?>[] paramTypes = parseParamTypes(def.getArgTypesJson());
        Object[] args = parseArgs(def.getArgsJson(), paramTypes);

        Method method = findMethod(bean, def.getMethodName(), paramTypes);
        method.setAccessible(true);
        method.invoke(bean, args);
    }

    private Method findMethod(Object bean, String methodName, Class<?>[] paramTypes) throws NoSuchMethodException {
        Class<?> targetClass = AopUtils.getTargetClass(bean);
        return targetClass.getMethod(methodName, paramTypes);
    }

    private Class<?>[] parseParamTypes(String argTypesJson) throws Exception {
        if (argTypesJson == null || argTypesJson.isBlank()) {
            return new Class<?>[0];
        }
        JsonNode root = objectMapper.readTree(argTypesJson);
        Class<?>[] types = new Class<?>[root.size()];
        for (int i = 0; i < root.size(); i++) {
            types[i] = resolveClass(root.get(i).asText());
        }
        return types;
    }

    private Object[] parseArgs(String argsJson, Class<?>[] paramTypes) throws Exception {
        if (paramTypes.length == 0) {
            return new Object[0];
        }
        JsonNode root = objectMapper.readTree(argsJson);
        Object[] args = new Object[root.size()];
        for (int i = 0; i < root.size(); i++) {
            args[i] = objectMapper.treeToValue(root.get(i), wrap(paramTypes[i]));
        }
        return args;
    }

    private Class<?> resolveClass(String typeName) throws ClassNotFoundException {
        return switch (typeName) {
            case "int" -> int.class;
            case "long" -> long.class;
            case "double" -> double.class;
            case "boolean" -> boolean.class;
            case "float" -> float.class;
            case "short" -> short.class;
            case "byte" -> byte.class;
            case "char" -> char.class;
            default -> Class.forName(typeName);
        };
    }

    private Class<?> wrap(Class<?> clazz) {
        if (!clazz.isPrimitive()) return clazz;
        if (clazz == int.class) return Integer.class;
        if (clazz == long.class) return Long.class;
        if (clazz == double.class) return Double.class;
        if (clazz == boolean.class) return Boolean.class;
        if (clazz == float.class) return Float.class;
        if (clazz == short.class) return Short.class;
        if (clazz == byte.class) return Byte.class;
        if (clazz == char.class) return Character.class;
        return clazz;
    }
}
