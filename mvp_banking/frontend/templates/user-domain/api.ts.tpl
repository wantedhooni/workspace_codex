import { request } from "../../shared/api/http";
import type { __DOMAIN_PASCAL__Item, Create__DOMAIN_PASCAL__Payload } from "./types";

const BASE_PATH = "/api/user/__DOMAIN_KEBAB__";

export const __DOMAIN_CAMEL__Api = {
  list(token: string) {
    return request<__DOMAIN_PASCAL__Item[]>(BASE_PATH, {}, token);
  },
  create(token: string, payload: Create__DOMAIN_PASCAL__Payload) {
    return request<__DOMAIN_PASCAL__Item>(
      BASE_PATH,
      {
        method: "POST",
        body: JSON.stringify(payload),
      },
      token,
    );
  },
};
