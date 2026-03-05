import { request, withQuery } from "../../shared/api/http";
import type { AdminListParams, Customer, CustomerStatusSummary, PageResponse } from "../../types";

export const adminCustomersApi = {
  customers(token: string, params: AdminListParams) {
    return request<PageResponse<Customer>>(withQuery("/api/admin/customers", params), {}, token);
  },
  customerSummary(token: string, params: AdminListParams) {
    return request<CustomerStatusSummary>(withQuery("/api/admin/customers/summary", params), {}, token);
  },
};
