"use client";

import type {
  CreateParams,
  DataProvider,
  DeleteManyParams,
  DeleteParams,
  GetListParams,
  GetManyParams,
  GetManyReferenceParams,
  GetOneParams,
  RaRecord,
  UpdateManyParams,
  UpdateParams
} from "react-admin";
import { HttpError } from "react-admin";
import { getAuthorizationHeader } from "./authProvider";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080/api/v1";
const RESOURCE_PATH_MAP: Record<string, string> = {
  users: "/admin/users",
  "menu-permissions": "/admin/menu-permissions"
};

type ApiListResponse<T> = {
  data: T[];
  total: number;
};

type ApiSingleResponse<T> = {
  data: T;
};

type ApiErrorEnvelope = {
  error?: {
    message?: string;
    details?: unknown;
  };
};

type RequestOptions = {
  method?: "GET" | "POST" | "PUT" | "DELETE";
  body?: unknown;
};

async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const headers: HeadersInit = {
    Accept: "application/json"
  };

  const authHeader = getAuthorizationHeader();
  if (authHeader) {
    headers.Authorization = authHeader;
  }

  const init: RequestInit = {
    method: options.method ?? "GET",
    headers
  };

  if (options.body !== undefined) {
    headers["Content-Type"] = "application/json";
    init.body = JSON.stringify(options.body);
  }

  const response = await fetch(`${API_BASE_URL}${path}`, init);
  if (!response.ok) {
    const errorBody = (await response.json().catch(() => null)) as ApiErrorEnvelope | null;
    const body: Record<string, unknown> = {};
    const details = errorBody?.error?.details;
    if (details && typeof details === "object" && !Array.isArray(details)) {
      body.errors = details;
    } else if (details !== undefined && details !== null) {
      body.details = details;
    }

    throw new HttpError(
      errorBody?.error?.message ?? response.statusText,
      response.status,
      body
    );
  }

  if (response.status === 204) {
    return null as T;
  }

  return (await response.json()) as T;
}

function resolveResourcePath(resource: string): string {
  return RESOURCE_PATH_MAP[resource] ?? `/${resource}`;
}

function buildListPath(resource: string, params: GetListParams | GetManyReferenceParams): string {
  const searchParams = new URLSearchParams();
  const pagination = params.pagination ?? { page: 1, perPage: 25 };
  const sort = params.sort ?? { field: "id", order: "ASC" as const };
  const filter = params.filter ?? {};

  const start = (pagination.page - 1) * pagination.perPage;
  const end = start + pagination.perPage - 1;

  searchParams.set("range", JSON.stringify([start, end]));
  searchParams.set("sort", JSON.stringify([sort.field, sort.order]));
  searchParams.set("filter", JSON.stringify(filter));

  return `${resolveResourcePath(resource)}?${searchParams.toString()}`;
}

async function getList<T extends RaRecord>(resource: string, params: GetListParams) {
  const response = await request<ApiListResponse<T>>(buildListPath(resource, params));
  return {
    data: response.data,
    total: response.total
  };
}

async function getOne<T extends RaRecord>(resource: string, params: GetOneParams) {
  const response = await request<ApiSingleResponse<T>>(`${resolveResourcePath(resource)}/${params.id}`);
  return { data: response.data };
}

async function create<T extends RaRecord>(resource: string, params: CreateParams<T>) {
  const response = await request<ApiSingleResponse<T>>(resolveResourcePath(resource), {
    method: "POST",
    body: params.data
  });
  return { data: response.data };
}

async function update<T extends RaRecord>(resource: string, params: UpdateParams<T>) {
  const response = await request<ApiSingleResponse<T>>(`${resolveResourcePath(resource)}/${params.id}`, {
    method: "PUT",
    body: params.data
  });
  return { data: response.data };
}

async function remove<T extends RaRecord>(resource: string, params: DeleteParams<T>) {
  await request<void>(`${resolveResourcePath(resource)}/${params.id}`, { method: "DELETE" });
  return { data: params.previousData ?? ({ id: params.id } as T) };
}

async function getMany<T extends RaRecord>(resource: string, params: GetManyParams) {
  const results = await Promise.all(
    params.ids.map((id) =>
      request<ApiSingleResponse<T>>(`${resolveResourcePath(resource)}/${id}`).then((res) => res.data)
    )
  );
  return { data: results };
}

async function getManyReference<T extends RaRecord>(resource: string, params: GetManyReferenceParams) {
  const mergedFilter = {
    ...(params.filter ?? {}),
    [params.target]: params.id
  };

  const response = await request<ApiListResponse<T>>(
    buildListPath(resource, { ...params, filter: mergedFilter })
  );

  return {
    data: response.data,
    total: response.total
  };
}

async function updateMany<T extends RaRecord>(resource: string, params: UpdateManyParams<T>) {
  await Promise.all(
    params.ids.map((id) =>
      request<ApiSingleResponse<T>>(`${resolveResourcePath(resource)}/${id}`, {
        method: "PUT",
        body: params.data
      })
    )
  );
  return { data: params.ids };
}

async function deleteMany<T extends RaRecord>(resource: string, params: DeleteManyParams<T>) {
  await Promise.all(params.ids.map((id) => request<void>(`${resolveResourcePath(resource)}/${id}`, { method: "DELETE" })));
  return { data: params.ids };
}

export const dataProvider: DataProvider = {
  getList,
  getOne,
  create,
  update,
  delete: remove,
  getMany,
  getManyReference,
  updateMany,
  deleteMany
};
