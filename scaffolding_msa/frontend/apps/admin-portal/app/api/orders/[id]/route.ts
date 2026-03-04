import { NextResponse } from "next/server";
import { loadOrder } from "@/lib/backend";

export async function GET(_: Request, context: { params: Promise<{ id: string }> }) {
    try {
        const { id } = await context.params;
        const order = await loadOrder(id);
        return NextResponse.json({ data: order });
    } catch (error) {
        return NextResponse.json(
            {
                message: error instanceof Error ? error.message : "Failed to load order",
            },
            { status: 502 }
        );
    }
}

