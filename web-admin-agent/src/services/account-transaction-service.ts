import { ApiClient, QueryStringService } from "@/services/api-client";
import { BaseCrudService } from "@/services/base-crud-service";
import type {
  AccountTransactionRequest,
  AccountTransactionSearchRequest,
  JsonRecord,
} from "@/types/admin-api";

/**
 * api-docs.json의 account-transaction-controller 엔드포인트를 담당하는 계좌 거래 서비스입니다.
 * 현재 명세의 request/response schema가 object로 열려 있어 JsonRecord 기반으로 확장 가능하게 둡니다.
 */
export class AccountTransactionService extends BaseCrudService<
  JsonRecord,
  AccountTransactionRequest,
  AccountTransactionRequest,
  AccountTransactionSearchRequest
> {
  constructor(apiClient = new ApiClient(), queryStringService = new QueryStringService()) {
    super("/api/v1/accounttransaction", apiClient, queryStringService);
  }
}
