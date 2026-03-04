import {
    BaseRecord,
    DataProvider,
    GetListParams,
    GetListResponse,
    GetManyParams,
    GetManyResponse,
    GetOneParams,
    GetOneResponse,
} from "@refinedev/core";

type CollectionResponse<TData> = {
    data: TData[];
    total: number;
};

type ItemResponse<TData> = {
    data: TData;
};

async function request<TData>(path: string): Promise<TData> {
    const response = await fetch(path, {
        cache: "no-store",
        headers: {
            Accept: "application/json",
        },
    });

    if (!response.ok) {
        throw new Error(`Request failed: ${response.status}`);
    }

    return response.json() as Promise<TData>;
}

export const adminDataProvider: DataProvider = {
    getApiUrl: () => "/api",
    getList: async <TData extends BaseRecord = BaseRecord>(
        { resource }: GetListParams
    ): Promise<GetListResponse<TData>> => {
        const payload = await request<CollectionResponse<TData>>(`/api/${resource}`);
        return {
            data: payload.data,
            total: payload.total,
        };
    },
    getOne: async <TData extends BaseRecord = BaseRecord>(
        { resource, id }: GetOneParams
    ): Promise<GetOneResponse<TData>> => {
        const payload = await request<ItemResponse<TData>>(`/api/${resource}/${id}`);
        return {
            data: payload.data,
        };
    },
    getMany: async <TData extends BaseRecord = BaseRecord>(
        { resource, ids }: GetManyParams
    ): Promise<GetManyResponse<TData>> => {
        const items = await Promise.all(
            ids.map(async (id) => {
                const payload = await request<ItemResponse<TData>>(`/api/${resource}/${id}`);
                return payload.data;
            })
        );
        return {
            data: items,
        };
    },
    create: async () => {
        throw new Error("admin-portal is read-only by default");
    },
    createMany: async () => {
        throw new Error("admin-portal is read-only by default");
    },
    update: async () => {
        throw new Error("admin-portal is read-only by default");
    },
    updateMany: async () => {
        throw new Error("admin-portal is read-only by default");
    },
    deleteOne: async () => {
        throw new Error("admin-portal is read-only by default");
    },
    deleteMany: async () => {
        throw new Error("admin-portal is read-only by default");
    },
    custom: async () => {
        throw new Error("custom data access is not enabled");
    },
};
