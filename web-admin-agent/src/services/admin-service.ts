import { ApiClient, QueryStringService } from "@/services/api-client";
import { BaseCrudService } from "@/services/base-crud-service";
import type { AdminRequest, AdminSearchRequest, JsonRecord } from "@/types/admin-api";

/**
 * api-docs.json의 admin-controller 엔드포인트를 담당하는 관리자 서비스입니다.
 * 관리자 계정 목록 조회, 단건 조회, 생성, 수정, 삭제를 제공합니다.
 */
export class AdminService extends BaseCrudService<
  JsonRecord,
  AdminRequest,
  AdminRequest,
  AdminSearchRequest
> {
  constructor(apiClient = new ApiClient(), queryStringService = new QueryStringService()) {
    super("/api/v1/admin", apiClient, queryStringService);
  }
}
