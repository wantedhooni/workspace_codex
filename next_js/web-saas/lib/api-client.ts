import {
  clearAuthCookies,
  getAccessToken,
  getRefreshToken,
  setAuthCookies,
} from "@/lib/auth-cookies";
import type {
  ApiResponse,
  UserAuthResponse,
} from "@/services/auth/auth.types";

const API_BASE_URL = process.env.API_BASE_URL;

if (!API_BASE_URL) {
  throw new Error("API_BASE_URL is not defined");
}

type ApiClientOptions = Omit<RequestInit, "body"> & {
  body?: unknown;
  auth?: boolean;
};

async function parseJson<T>(response: Response): Promise<T> {
  const text = await response.text();

  if (!text) {
    return null as T;
  }

  return JSON.parse(text) as T;
}

async function refreshAccessToken() {
  const refreshToken = await getRefreshToken();

  if (!refreshToken) {
    await clearAuthCookies();
    return null;
  }

  const response = await fetch(`${API_BASE_URL}/api/v1/auth/refresh`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ refreshToken }),
    cache: "no-store",
  });

  if (!response.ok) {
    await clearAuthCookies();
    return null;
  }

  const result = await parseJson<ApiResponse<UserAuthResponse>>(response);

  if (!result?.success || !result.data) {
    await clearAuthCookies();
    return null;
  }

  await setAuthCookies(result.data);

  return result.data.accessToken;
}

export async function apiClient<T>(
  path: string,
  options: ApiClientOptions = {}
): Promise<T> {
  const { body, auth = true, headers, ...init } = options;

  const requestHeaders = new Headers(headers);

  if (body !== undefined && !requestHeaders.has("Content-Type")) {
    requestHeaders.set("Content-Type", "application/json");
  }

  if (auth) {
    const accessToken = await getAccessToken();

    if (accessToken) {
      requestHeaders.set("Authorization", `Bearer ${accessToken}`);
    }
  }

  const requestUrl = `${API_BASE_URL}${path}`;

  let response = await fetch(requestUrl, {
    ...init,
    headers: requestHeaders,
    body: body !== undefined ? JSON.stringify(body) : undefined,
    cache: "no-store",
  });

  if (response.status === 401 && auth) {
    const newAccessToken = await refreshAccessToken();

    if (newAccessToken) {
      requestHeaders.set("Authorization", `Bearer ${newAccessToken}`);

      response = await fetch(requestUrl, {
        ...init,
        headers: requestHeaders,
        body: body !== undefined ? JSON.stringify(body) : undefined,
        cache: "no-store",
      });
    }
  }

  const data = await parseJson<T>(response);

  if (!response.ok) {
    const message =
      data && typeof data === "object" && "message" in data
        ? String(data.message)
        : "API request failed";

    throw new Error(message);
  }

  return data;
}