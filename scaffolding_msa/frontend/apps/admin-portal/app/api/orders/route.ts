import { NextResponse } from "next/server";
import { loadOrders } from "@/lib/backend";

export async function GET() {
    try {
        const orders = await loadOrders();
        return NextResponse.json({
            data: orders,
            total: orders.length,
        });
    } catch (error) {
        return NextResponse.json(
            {
                message: error instanceof Error ? error.message : "Failed to load orders",
            },
            { status: 502 }
        );
    }
}

