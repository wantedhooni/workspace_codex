import { DataProvider, HttpError } from "@refinedev/core";

type ApiList<T> = {
  items: T[];
  total: number;
};

const buildUrl = (baseUrl: string, resource: string) =>
  `${baseUrl.replace(/\/$/, "")}/${resource}`;

const fetchJson = async (url: string, options?: RequestInit) => {
  const response = await fetch(url, {
    headers: { "Content-Type": "application/json" },
    credentials: "include",
    ...options,
  });

  if (!response.ok) {
    const message = await response.text();
    throw {
      message,
      statusCode: response.status,
    } as HttpError;
  }

  if (response.status === 204) {
    return null;
  }

  return response.json();
};

const extractQueryFilters = (filters: any[] | undefined) => {
  if (!filters) {
    return [] as Array<[string, string]>;
  }

  return filters
    .filter((item) => item && "field" in item && typeof item.field === "string")
    .map((item) => [item.field as string, item.value] as const)
    .filter(([, value]) => value !== undefined && value !== null && String(value).trim().length > 0)
    .map(([field, value]) => [field, String(value).trim()] as [string, string]);
};

export const simpleRestProvider = (apiUrl: string): DataProvider => ({
  getList: async ({ resource, pagination, sorters, filters }) => {
    const { current = 1, pageSize = 20 } = pagination ?? {};
    const sort = sorters?.[0]?.field ?? "id";
    const order = (sorters?.[0]?.order ?? "asc").toUpperCase();
    const queryFilters = extractQueryFilters(filters as any[]);

    const url = new URL(buildUrl(apiUrl, resource));
    url.searchParams.set("page", String(current));
    url.searchParams.set("perPage", String(pageSize));
    url.searchParams.set("sort", sort);
    url.searchParams.set("order", order);

    queryFilters.forEach(([field, value]) => {
      const queryKey = field === "q" ? "searchParam" : field;
      url.searchParams.set(queryKey, value);
    });

    const data = (await fetchJson(url.toString())) as ApiList<any> | any[];

    if (Array.isArray(data)) {
      return {
        data,
        total: data.length,
      };
    }

    return {
      data: data.items,
      total: data.total,
    };
  },
  getMany: async ({ resource, ids }) => {
    const url = new URL(buildUrl(apiUrl, resource));
    ids.forEach((id) => url.searchParams.append("ids", String(id)));
    const data = (await fetchJson(url.toString())) as ApiList<any> | any[];
    return { data: Array.isArray(data) ? data : data.items };
  },
  getOne: async ({ resource, id }) => {
    const data = await fetchJson(`${buildUrl(apiUrl, resource)}/${id}`);
    return { data };
  },
  create: async ({ resource, variables }) => {
    const data = await fetchJson(buildUrl(apiUrl, resource), {
      method: "POST",
      body: JSON.stringify(variables ?? {}),
    });
    return { data };
  },
  update: async ({ resource, id, variables }) => {
    const data = await fetchJson(`${buildUrl(apiUrl, resource)}/${id}`, {
      method: "PATCH",
      body: JSON.stringify(variables ?? {}),
    });
    return { data };
  },
  deleteOne: async ({ resource, id }) => {
    await fetchJson(`${buildUrl(apiUrl, resource)}/${id}`, {
      method: "DELETE",
    });
    return { data: { id } as any };
  },
  getApiUrl: () => apiUrl,
});
