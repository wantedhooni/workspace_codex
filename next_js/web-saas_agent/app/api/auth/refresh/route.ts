import { NextResponse } from "next/server";
import { AuthCookieStore, authService } from "@/lib/auth-service";

export async function POST() {
  try {
    const { refreshToken } = await AuthCookieStore.getTokens();

    if (!refreshToken) {
      return NextResponse.json({ success: false, message: "리프레시 토큰이 없습니다." }, { status: 401 });
    }

    const result = await authService.refresh(refreshToken);

    if (!result.data?.accessToken || !result.data.refreshToken) {
      throw new Error("토큰 갱신 응답에 토큰이 없습니다.");
    }

    await AuthCookieStore.setTokens(result.data);
    return NextResponse.json({ success: true, message: "토큰이 갱신되었습니다." });
  } catch (error) {
    await AuthCookieStore.clearTokens();

    return NextResponse.json(
      {
        success: false,
        message: error instanceof Error ? error.message : "토큰 갱신에 실패했습니다.",
      },
      { status: 401 },
    );
  }
}
