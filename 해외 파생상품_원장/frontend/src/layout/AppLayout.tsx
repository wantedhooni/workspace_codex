import { ThemedLayoutV2, ThemedTitleV2 } from "@refinedev/mui";
import { Outlet } from "react-router-dom";
import { DbSider } from "./DbSider";

export function AppLayout() {
  return (
    <ThemedLayoutV2 Sider={DbSider} Title={(props) => <ThemedTitleV2 {...props} text="Derivatives Ops" />}>
      <Outlet />
    </ThemedLayoutV2>
  );
}
