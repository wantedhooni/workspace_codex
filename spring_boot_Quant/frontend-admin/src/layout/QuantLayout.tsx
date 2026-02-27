import { Layout, type LayoutProps } from "react-admin";
import { QuantAppBar } from "./QuantAppBar";
import { QuantMenu } from "./QuantMenu";

export function QuantLayout(props: LayoutProps) {
  return <Layout {...props} appBar={QuantAppBar} menu={QuantMenu} />;
}
