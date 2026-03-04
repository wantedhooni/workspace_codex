type ApiEnvelope<T> = {
    success: boolean;
    data: T;
    error?: {
        code: string;
        message: string;
    } | null;
};

type OverviewResponse = {
    users: Array<{
        id: number;
        email: string;
        name: string;
        status: string;
    }>;
    orders: Array<{
        id: number;
        orderNo: string;
        userId: number;
        amount: number;
        status: string;
    }>;
    config: {
        name: string;
        propertySources: Array<{ name: string }>;
    } | null;
    authIssuer: string | null;
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

export async function loadOverview(): Promise<OverviewResponse> {
    const [usersPayload, ordersPayload, configPayload, openIdPayload] = await Promise.all([
        fetchJson<ApiEnvelope<OverviewResponse["users"]>>(`${gatewayUrl}/api/v1/users`),
        fetchJson<ApiEnvelope<OverviewResponse["orders"]>>(`${gatewayUrl}/api/v1/orders?userId=1`),
        fetchJson<{ name: string; propertySources: Array<{ name: string }> }>(`${gatewayUrl}/config/application/default`),
        fetchJson<{ issuer: string }>(`${gatewayUrl}/.well-known/openid-configuration`),
    ]);

    return {
        users: usersPayload.data ?? [],
        orders: ordersPayload.data ?? [],
        config: configPayload,
        authIssuer: openIdPayload.issuer ?? null,
    };
}

