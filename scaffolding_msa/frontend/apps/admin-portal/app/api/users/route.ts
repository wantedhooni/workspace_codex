import { NextResponse } from "next/server";
import { loadUsers } from "@/lib/backend";

export async function GET() {
    try {
        const users = await loadUsers();
        return NextResponse.json({
            data: users,
            total: users.length,
        });
    } catch (error) {
        return NextResponse.json(
            {
                message: error instanceof Error ? error.message : "Failed to load users",
            },
            { status: 502 }
        );
    }
}

