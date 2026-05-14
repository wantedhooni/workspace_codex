import { meApi } from "@/services/auth/auth.api";
import type { JwtPrincipal } from "@/services/auth/auth.types";

export async function getCurrentUser(): Promise<JwtPrincipal | null> {
  try {
    const response = await meApi();

    return response.data;
  } catch {
    return null;
  }
}