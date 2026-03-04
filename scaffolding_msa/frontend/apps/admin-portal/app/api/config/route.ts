import { NextRequest, NextResponse } from "next/server";
import { loadConfig } from "@/lib/backend";

export async function GET(request: NextRequest) {
    const application = request.nextUrl.searchParams.get("application") ?? "application";
    const profile = request.nextUrl.searchParams.get("profile") ?? "default";
    const label = request.nextUrl.searchParams.get("label") ?? undefined;

    try {
        const environment = await loadConfig(application, profile, label);
        return NextResponse.json(environment);
    } catch (error) {
        return NextResponse.json(
            {
                message: error instanceof Error ? error.message : "Failed to load config",
            },
            { status: 502 }
        );
    }
}

