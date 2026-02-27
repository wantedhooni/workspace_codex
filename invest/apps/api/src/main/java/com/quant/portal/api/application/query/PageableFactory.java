package com.quant.portal.api.application.query;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quant.portal.api.application.exception.ApiException;
import com.quant.portal.api.application.exception.ErrorCode;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

public final class PageableFactory {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private PageableFactory() {
        throw new UnsupportedOperationException("This class should never be instantiated");
    }

    public static Pageable transactionPageable(int page, int perPage, String sort) {
        return transactionPageable(page, perPage, sort, null);
    }

    public static Pageable transactionPageable(int page, int perPage, String sort, String range) {
        return pageable(page, perPage, sort, range, "tradeDate,DESC");
    }

    public static Pageable portfolioPageable(int page, int perPage, String sort) {
        return portfolioPageable(page, perPage, sort, null);
    }

    public static Pageable portfolioPageable(int page, int perPage, String sort, String range) {
        return pageable(page, perPage, sort, range, "createdAt,DESC");
    }

    public static Pageable instrumentPageable(int page, int perPage, String sort) {
        return instrumentPageable(page, perPage, sort, null);
    }

    public static Pageable instrumentPageable(int page, int perPage, String sort, String range) {
        return pageable(page, perPage, sort, range, "ticker,ASC");
    }

    public static Pageable holdingPageable(int page, int perPage, String sort) {
        return holdingPageable(page, perPage, sort, null);
    }

    public static Pageable holdingPageable(int page, int perPage, String sort, String range) {
        return pageable(page, perPage, sort, range, "ticker,ASC");
    }

    public static Pageable quantStrategyPageable(int page, int perPage, String sort) {
        return quantStrategyPageable(page, perPage, sort, null);
    }

    public static Pageable quantStrategyPageable(int page, int perPage, String sort, String range) {
        return pageable(page, perPage, sort, range, "createdAt,DESC");
    }

    public static Pageable quantSignalPageable(int page, int perPage, String sort) {
        return quantSignalPageable(page, perPage, sort, null);
    }

    public static Pageable quantSignalPageable(int page, int perPage, String sort, String range) {
        return pageable(page, perPage, sort, range, "signalDate,DESC");
    }

    public static Pageable macroIndicatorPageable(int page, int perPage, String sort) {
        return macroIndicatorPageable(page, perPage, sort, null);
    }

    public static Pageable macroIndicatorPageable(int page, int perPage, String sort, String range) {
        return pageable(page, perPage, sort, range, "observedDate,DESC");
    }

    public static Pageable menuPermissionPageable(int page, int perPage, String sort) {
        return menuPermissionPageable(page, perPage, sort, null);
    }

    public static Pageable menuPermissionPageable(int page, int perPage, String sort, String range) {
        return pageable(page, perPage, sort, range, "createdAt,DESC");
    }

    public static Pageable adminUserPageable(int page, int perPage, String sort) {
        return adminUserPageable(page, perPage, sort, null);
    }

    public static Pageable adminUserPageable(int page, int perPage, String sort, String range) {
        return pageable(page, perPage, sort, range, "createdAt,DESC");
    }

    private static Pageable pageable(int page, int perPage, String sort, String range, String defaultSort) {
        int[] resolvedPaging = resolvePaging(page, perPage, range);
        int resolvedPage = resolvedPaging[0];
        int resolvedPerPage = resolvedPaging[1];

        if (resolvedPage < 1 || resolvedPerPage < 1) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST, "page and perPage must be greater than 0");
        }

        String[] parsed = parseSort(sort, defaultSort);
        String requestedField = parsed[0];
        Sort.Direction direction = parseDirection(parsed[1]);

        return PageRequest.of(resolvedPage - 1, resolvedPerPage, Sort.by(direction, requestedField));
    }

    private static int[] resolvePaging(int page, int perPage, String range) {
        if (range == null || range.isBlank()) {
            return new int[]{page, perPage};
        }

        try {
            JsonNode node = OBJECT_MAPPER.readTree(range);
            if (!node.isArray() || node.size() != 2) {
                throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST, "range must be a JSON array with two numbers");
            }

            int start = node.get(0).asInt(Integer.MIN_VALUE);
            int end = node.get(1).asInt(Integer.MIN_VALUE);
            if (start < 0 || end < start) {
                throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST, "range must satisfy 0 <= start <= end");
            }

            int resolvedPerPage = end - start + 1;
            int resolvedPage = (start / resolvedPerPage) + 1;
            return new int[]{resolvedPage, resolvedPerPage};
        } catch (ApiException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST, "Invalid range JSON");
        }
    }

    private static String[] parseSort(String sort, String defaultSort) {
        if (sort == null || sort.isBlank()) {
            String[] defaultSplit = defaultSort.split(",");
            return new String[]{defaultSplit[0], defaultSplit[1]};
        }

        String normalizedSort = sort.trim();
        if (normalizedSort.startsWith("[")) {
            return parseJsonSort(normalizedSort);
        }

        String[] split = sort.split(",");
        if (split.length != 2) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST, "sort must be in 'field,DESC|ASC' format");
        }

        return new String[]{split[0].trim(), split[1].trim().toUpperCase()};
    }

    private static String[] parseJsonSort(String sort) {
        try {
            JsonNode node = OBJECT_MAPPER.readTree(sort);
            if (!node.isArray() || node.size() != 2) {
                throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST, "sort JSON must be [field, direction]");
            }
            String field = node.get(0).asText();
            String direction = node.get(1).asText();
            if (field == null || field.isBlank() || direction == null || direction.isBlank()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST, "sort JSON values must not be blank");
            }
            return new String[]{field.trim(), direction.trim().toUpperCase()};
        } catch (ApiException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST, "Invalid sort JSON");
        }
    }

    private static Sort.Direction parseDirection(String direction) {
        try {
            return Sort.Direction.fromString(direction);
        } catch (IllegalArgumentException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST, "Unsupported sort direction: " + direction);
        }
    }
}
