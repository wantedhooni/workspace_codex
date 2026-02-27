import { ThemedLayoutV2, ThemedTitleV2 } from "@refinedev/mui";
import { Outlet } from "react-router-dom";

export function AppLayout() {
  return (
    <ThemedLayoutV2 Title={(props) => <ThemedTitleV2 {...props} text="Derivatives Ops" />}>
      <Outlet />
    </ThemedLayoutV2>
  );
}
