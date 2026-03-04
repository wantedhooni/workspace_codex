import { ConfigEnvironment, OrderDetail, OrderSummary, UserDetail, UserSummary } from "@/lib/types";

type ApiEnvelope<T> = {
    success: boolean;
    data: T;
    error?: {
        code: string;
        message: string;
    } | null;
};

const gatewayUrl = process.env.GATEWAY_URL ?? process.env.NEXT_PUBLIC_GATEWAY_URL ?? "http://127.0.0.1:8000";

async function fetchJson<T>(url: string): Promise<T> {
    const response = await fetch(url, {
        cache: "no-store",
        headers: {
            Accept: "application/json",
        },
    });

    if (!response.ok) {
        throw new Error(`Upstream request failed: ${response.status}`);
    }

    return response.json() as Promise<T>;
}

export async function loadUsers(): Promise<UserSummary[]> {
    const payload = await fetchJson<ApiEnvelope<UserSummary[]>>(`${gatewayUrl}/api/v1/users`);
    return payload.data ?? [];
}

export async function loadUser(userId: string): Promise<UserDetail> {
    const payload = await fetchJson<ApiEnvelope<UserDetail>>(`${gatewayUrl}/api/v1/users/${userId}`);
    return payload.data;
}

export async function loadOrders(): Promise<OrderSummary[]> {
    const payload = await fetchJson<ApiEnvelope<OrderSummary[]>>(`${gatewayUrl}/api/v1/orders`);
    return payload.data ?? [];
}

export async function loadOrder(orderId: string): Promise<OrderDetail> {
    const payload = await fetchJson<ApiEnvelope<OrderDetail>>(`${gatewayUrl}/api/v1/orders/${orderId}`);
    return payload.data;
}

export async function loadConfig(application: string, profile: string, label?: string): Promise<ConfigEnvironment> {
    const suffix = label ? `/${encodeURIComponent(label)}` : "";
    return fetchJson<ConfigEnvironment>(
        `${gatewayUrl}/config/${encodeURIComponent(application)}/${encodeURIComponent(profile)}${suffix}`
    );
}

