import { NextResponse } from "next/server";
import { AuthCookieStore, authService } from "@/lib/auth-service";

export async function POST() {
  const { accessToken, refreshToken } = await AuthCookieStore.getTokens();

  try {
    if (accessToken || refreshToken) {
      await authService.logout(accessToken, refreshToken);
    }
  } finally {
    await AuthCookieStore.clearTokens();
  }

  return NextResponse.json({
    success: true,
    message: "로그아웃되었습니다.",
  });
}
