import { NextResponse } from "next/server";
import { AuthCookieStore, authService } from "@/lib/auth-service";

export async function GET() {
  try {
    const { accessToken } = await AuthCookieStore.getTokens();

    if (!accessToken) {
      return NextResponse.json({ success: false, message: "인증이 필요합니다." }, { status: 401 });
    }

    const result = await authService.me(accessToken);
    return NextResponse.json(result);
  } catch (error) {
    return NextResponse.json(
      {
        success: false,
        message: error instanceof Error ? error.message : "사용자 정보를 조회하지 못했습니다.",
      },
      { status: 401 },
    );
  }
}
