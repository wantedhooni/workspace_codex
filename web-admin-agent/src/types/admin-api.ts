export type JsonRecord = Record<string, unknown>;

export type DomainKey = "admin" | "account" | "accounttransaction";

export type NavigationKey = "dashboard" | DomainKey;

export interface ApiResponse<T> {
  success?: boolean;
  data?: T;
  message?: string;
  timestamp?: number;
}

export interface PageResponse<T> {
  content?: T[];
  totalElements?: number;
  totalPages?: number;
  page?: number;
  size?: number;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface AdminAuthResponse {
  accessToken?: string;
  refreshToken?: string;
  tokenType?: string;
}

export interface AuthSession extends AdminAuthResponse {
  email: string;
}

export interface FieldConfig {
  name: string;
  label: string;
  type?: "text" | "email" | "password" | "number" | "date" | "textarea";
  required?: boolean;
  placeholder?: string;
}

export interface DomainConfig {
  key: DomainKey;
  label: string;
  description: string;
  endpoint: string;
  searchFields: FieldConfig[];
  formFields: FieldConfig[];
  columnFields: string[];
}
