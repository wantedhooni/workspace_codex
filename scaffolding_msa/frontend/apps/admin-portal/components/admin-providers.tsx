"use client";

import { PropsWithChildren, useState } from "react";
import { Refine } from "@refinedev/core";
import { CssBaseline, ThemeProvider, createTheme } from "@mui/material";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { adminDataProvider } from "@/lib/data-provider";

const theme = createTheme({
    palette: {
        mode: "light",
        primary: {
            main: "#b7592b",
        },
        secondary: {
            main: "#154a5b",
        },
        background: {
            default: "#f3ede2",
            paper: "#fffaf4",
        },
    },
    shape: {
        borderRadius: 18,
    },
    typography: {
        fontFamily: "var(--font-admin-sans), system-ui, sans-serif",
        h1: {
            fontWeight: 700,
        },
        h2: {
            fontWeight: 700,
        },
        h3: {
            fontWeight: 700,
        },
        button: {
            textTransform: "none",
            fontWeight: 600,
        },
    },
});

export function AdminProviders({ children }: PropsWithChildren) {
    const [queryClient] = useState(() => new QueryClient());

    return (
        <QueryClientProvider client={queryClient}>
            <ThemeProvider theme={theme}>
                <CssBaseline />
                <Refine
                    dataProvider={adminDataProvider}
                    resources={[
                        { name: "users", list: "/users" },
                        { name: "orders", list: "/orders" },
                    ]}
                    options={{
                        syncWithLocation: false,
                        warnWhenUnsavedChanges: false,
                    }}
                >
                    {children}
                </Refine>
            </ThemeProvider>
        </QueryClientProvider>
    );
}

