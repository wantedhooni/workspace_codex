package com.quant.portal.api.presentation.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quant.portal.api.application.exception.ApiException;
import com.quant.portal.api.application.exception.ErrorCode;
import java.time.LocalDate;
import org.springframework.http.HttpStatus;

public final class FilterParamParser {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private FilterParamParser() {
        throw new UnsupportedOperationException("This class should never be instantiated");
    }

    public static JsonNode parse(String filter) {
        if (filter == null || filter.isBlank()) {
            return null;
        }

        try {
            JsonNode node = OBJECT_MAPPER.readTree(filter);
            if (node == null || !node.isObject()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST, "filter must be a JSON object");
            }
            return node;
        } catch (JsonProcessingException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST, "Invalid filter JSON");
        }
    }

    public static String text(JsonNode filterNode, String field) {
        if (filterNode == null) {
            return null;
        }
        JsonNode value = filterNode.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        String text = value.asText();
        return text == null || text.isBlank() ? null : text;
    }

    public static Long longValue(JsonNode filterNode, String field) {
        if (filterNode == null) {
            return null;
        }
        JsonNode value = filterNode.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        if (value.isNumber()) {
            return value.longValue();
        }
        String text = value.asText();
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST, "Invalid long filter value: " + field);
        }
    }

    public static LocalDate localDate(JsonNode filterNode, String field) {
        String value = text(filterNode, field);
        if (value == null) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (RuntimeException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST, "Invalid date filter value: " + field);
        }
    }

    public static <E extends Enum<E>> E enumValue(JsonNode filterNode, String field, Class<E> enumClass) {
        String value = text(filterNode, field);
        if (value == null) {
            return null;
        }
        try {
            return Enum.valueOf(enumClass, value);
        } catch (RuntimeException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST, "Invalid enum filter value: " + field);
        }
    }
}
