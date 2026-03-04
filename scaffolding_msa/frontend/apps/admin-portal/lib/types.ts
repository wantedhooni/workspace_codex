export type UserSummary = {
    id: number;
    email: string;
    name: string;
    status: string;
};

export type UserDetail = UserSummary & {
    createdAt: string;
    updatedAt: string;
};

export type OrderSummary = {
    id: number;
    orderNo: string;
    userId: number;
    amount: number;
    status: string;
};

export type OrderDetail = OrderSummary & {
    userName: string;
    createdAt: string;
    updatedAt: string;
};

export type ConfigEnvironment = {
    name: string;
    profiles: string[];
    label: string | null;
    propertySources: Array<{
        name: string;
        source: Record<string, string | number | boolean | null>;
    }>;
};

export type AdminConfigQuery = {
    application: string;
    profile: string;
    label: string;
};

