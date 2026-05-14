import { NextResponse } from "next/server";
import { AuthCookieStore, authService, type LoginRequest } from "@/lib/auth-service";

export async function POST(request: Request) {
  try {
    const payload = (await request.json()) as LoginRequest;
    const result = await authService.login(payload);

    if (!result.data?.accessToken || !result.data.refreshToken) {
      throw new Error("로그인 응답에 토큰이 없습니다.");
    }

    await AuthCookieStore.setTokens(result.data);

    return NextResponse.json({
      success: true,
      message: result.message ?? "로그인되었습니다.",
    });
  } catch (error) {
    return NextResponse.json(
      {
        success: false,
        message: error instanceof Error ? error.message : "로그인에 실패했습니다.",
      },
      { status: 401 },
    );
  }
}
