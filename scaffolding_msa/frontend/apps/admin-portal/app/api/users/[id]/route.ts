import { NextResponse } from "next/server";
import { loadUser } from "@/lib/backend";

export async function GET(_: Request, context: { params: Promise<{ id: string }> }) {
    try {
        const { id } = await context.params;
        const user = await loadUser(id);
        return NextResponse.json({ data: user });
    } catch (error) {
        return NextResponse.json(
            {
                message: error instanceof Error ? error.message : "Failed to load user",
            },
            { status: 502 }
        );
    }
}

