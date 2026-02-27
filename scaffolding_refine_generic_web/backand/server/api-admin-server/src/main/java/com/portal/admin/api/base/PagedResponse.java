package com.portal.admin.api.base;

import java.util.List;

public record PagedResponse<T>(List<T> items, int total) {
}
