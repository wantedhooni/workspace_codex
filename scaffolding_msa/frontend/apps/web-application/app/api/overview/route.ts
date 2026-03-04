import { NextResponse } from "next/server";
import { loadOverview } from "@/lib/backend";

export async function GET() {
    try {
        const overview = await loadOverview();
        return NextResponse.json(overview);
    } catch (error) {
        return NextResponse.json(
            {
                message: error instanceof Error ? error.message : "Failed to load overview",
            },
            { status: 502 }
        );
    }
}
