"use client";

import type { ReactNode } from "react";
import { Layout } from "react-admin";
import AppMenu from "./AppMenu";
import AppTopBar from "./AppTopBar";

type AppLayoutProps = {
  children: ReactNode;
};

export default function AppLayout({ children }: AppLayoutProps) {
  return (
    <Layout
      appBar={AppTopBar}
      menu={AppMenu}
      sx={{
        backgroundColor: (theme) => theme.palette.background.default
      }}
    >
      {children}
    </Layout>
  );
}
