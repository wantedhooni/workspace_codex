import { ApiClient, QueryStringService } from "@/services/api-client";
import type { JsonRecord, PageResponse } from "@/types/admin-api";

/**
 * ADMIN-API의 공통 CRUD 엔드포인트를 호출하는 기반 서비스입니다.
 * 목록, 단건 조회, 생성, 수정, 삭제 흐름을 도메인 서비스가 재사용합니다.
 */
export class BaseCrudService<
  TItem extends JsonRecord = JsonRecord,
  TCreateRequest extends JsonRecord = JsonRecord,
  TUpdateRequest extends JsonRecord = TCreateRequest,
  TSearchRequest extends JsonRecord = JsonRecord,
> {
  constructor(
    protected readonly endpoint: string,
    protected readonly apiClient = new ApiClient(),
    protected readonly queryStringService = new QueryStringService()
  ) {}

  /**
   * 페이지 정보와 검색 조건으로 도메인 목록을 조회합니다.
   */
  async search(
    searchRequest: TSearchRequest,
    page = 0,
    size = 20
  ): Promise<PageResponse<TItem>> {
    const query = this.queryStringService.stringify({
      page,
      size,
      ...searchRequest,
    });
    const response = await this.apiClient.request<PageResponse<TItem>>(`${this.endpoint}?${query}`);

    return response.data ?? {
      content: [],
      totalElements: 0,
      totalPages: 0,
      page,
      size,
    };
  }

  /**
   * ID 기준으로 도메인 단건을 조회합니다.
   */
  async get(id: string | number): Promise<TItem> {
    const response = await this.apiClient.request<TItem>(`${this.endpoint}/${id}`);
    return response.data ?? ({} as TItem);
  }

  /**
   * 도메인 데이터를 생성합니다.
   */
  async create(body: TCreateRequest): Promise<void> {
    await this.apiClient.request<void>(this.endpoint, {
      method: "POST",
      data: body,
    });
  }

  /**
   * ID 기준으로 도메인 데이터를 수정합니다.
   */
  async update(id: string | number, body: TUpdateRequest): Promise<void> {
    await this.apiClient.request<void>(`${this.endpoint}/${id}`, {
      method: "PATCH",
      data: body,
    });
  }

  /**
   * ID 기준으로 도메인 데이터를 삭제합니다.
   */
  async delete(id: string | number): Promise<void> {
    await this.apiClient.request<void>(`${this.endpoint}/${id}`, {
      method: "DELETE",
    });
  }
}
