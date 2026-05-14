import { ApiClient, QueryStringService } from "@/services/api-client";
import { BaseCrudService } from "@/services/base-crud-service";
import type { AccountRequest, AccountSearchRequest, JsonRecord } from "@/types/admin-api";

/**
 * api-docs.json의 account-controller 엔드포인트를 담당하는 계좌 서비스입니다.
 * 현재 명세의 request/response schema가 object로 열려 있어 JsonRecord 기반으로 확장 가능하게 둡니다.
 */
export class AccountService extends BaseCrudService<
  JsonRecord,
  AccountRequest,
  AccountRequest,
  AccountSearchRequest
> {
  constructor(apiClient = new ApiClient(), queryStringService = new QueryStringService()) {
    super("/api/v1/account", apiClient, queryStringService);
  }
}
