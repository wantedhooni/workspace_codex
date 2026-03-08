export type PageResponse<T> = {
  items: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export type PaginationState = {
  page: number;
  size: number;
};

export function emptyPageResponse<T>(size = 10): PageResponse<T> {
  return {
    items: [],
    page: 0,
    size,
    totalElements: 0,
    totalPages: 0,
  };
}
