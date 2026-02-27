package com.curd.template.infra.filter;

import com.curd.template.core.error.ApiException;
import com.curd.template.core.error.AppErrorCode;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

public final class ValueCoercion {

    private ValueCoercion() {
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Object convert(String rawValue, Class<?> fieldType) {
        if (rawValue == null) {
            return null;
        }

        String value = rawValue.trim();

        try {
            if (fieldType == String.class) {
                return value;
            }
            if (fieldType == Integer.class || fieldType == int.class) {
                return Integer.valueOf(value);
            }
            if (fieldType == Long.class || fieldType == long.class) {
                return Long.valueOf(value);
            }
            if (fieldType == Double.class || fieldType == double.class) {
                return Double.valueOf(value);
            }
            if (fieldType == BigDecimal.class) {
                return new BigDecimal(value);
            }
            if (fieldType == Boolean.class || fieldType == boolean.class) {
                return Boolean.valueOf(value);
            }
            if (fieldType == Instant.class) {
                return Instant.parse(value);
            }
            if (fieldType == LocalDate.class) {
                return LocalDate.parse(value);
            }
            if (fieldType == LocalDateTime.class) {
                return LocalDateTime.parse(value);
            }
            if (fieldType == OffsetDateTime.class) {
                return OffsetDateTime.parse(value);
            }
            if (fieldType == UUID.class) {
                return UUID.fromString(value);
            }
            if (Enum.class.isAssignableFrom(fieldType)) {
                Class<? extends Enum> enumType = (Class<? extends Enum>) fieldType;
                try {
                    return Enum.valueOf(enumType, value);
                } catch (IllegalArgumentException ignored) {
                    return Enum.valueOf(enumType, value.toUpperCase());
                }
            }
        } catch (RuntimeException exception) {
            throw new ApiException(
                AppErrorCode.FILTER_PARSE_ERROR,
                "Failed to convert value '" + rawValue + "' to " + fieldType.getSimpleName()
            );
        }

        throw new ApiException(AppErrorCode.FILTER_PARSE_ERROR, "Unsupported field type: " + fieldType.getName());
    }
}
