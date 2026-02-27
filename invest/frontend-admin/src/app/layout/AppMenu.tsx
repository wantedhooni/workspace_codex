"use client";

import AccountBalanceWalletIcon from "@mui/icons-material/AccountBalanceWallet";
import AutoGraphIcon from "@mui/icons-material/AutoGraph";
import CandlestickChartIcon from "@mui/icons-material/CandlestickChart";
import GroupIcon from "@mui/icons-material/Group";
import ReceiptLongIcon from "@mui/icons-material/ReceiptLong";
import SecurityIcon from "@mui/icons-material/Security";
import { DashboardMenuItem, Menu, MenuItemLink, usePermissions } from "react-admin";
import { AuthPermissions, hasMenuPermission } from "../../lib/authProvider";

type AppMenuProps = {
  dense?: boolean;
};

export default function AppMenu({ dense = false }: AppMenuProps) {
  const { permissions } = usePermissions<AuthPermissions>();

  return (
    <Menu>
      {hasMenuPermission(permissions, "dashboard", "list") ? <DashboardMenuItem /> : null}
      {hasMenuPermission(permissions, "portfolios", "list") ? (
        <MenuItemLink to="/portfolios" primaryText="Portfolios" leftIcon={<AccountBalanceWalletIcon />} dense={dense} />
      ) : null}
      {hasMenuPermission(permissions, "instruments", "list") ? (
        <MenuItemLink to="/instruments" primaryText="Instruments" leftIcon={<CandlestickChartIcon />} dense={dense} />
      ) : null}
      {hasMenuPermission(permissions, "transactions", "list") ? (
        <MenuItemLink to="/transactions" primaryText="Transactions" leftIcon={<ReceiptLongIcon />} dense={dense} />
      ) : null}
      {hasMenuPermission(permissions, "holdings", "list") ? (
        <MenuItemLink to="/holdings" primaryText="Holdings" leftIcon={<AutoGraphIcon />} dense={dense} />
      ) : null}
      {hasMenuPermission(permissions, "users", "list") ? (
        <MenuItemLink to="/users" primaryText="Users" leftIcon={<GroupIcon />} dense={dense} />
      ) : null}
      {hasMenuPermission(permissions, "quant-strategies", "list") ? (
        <MenuItemLink
          to="/quant-strategies"
          primaryText="Quant Strategies"
          leftIcon={<AutoGraphIcon />}
          dense={dense}
        />
      ) : null}
      {hasMenuPermission(permissions, "quant-signals", "list") ? (
        <MenuItemLink
          to="/quant-signals"
          primaryText="Quant Signals"
          leftIcon={<CandlestickChartIcon />}
          dense={dense}
        />
      ) : null}
      {hasMenuPermission(permissions, "macro-indicators", "list") ? (
        <MenuItemLink
          to="/macro-indicators"
          primaryText="Macro Indicators"
          leftIcon={<ReceiptLongIcon />}
          dense={dense}
        />
      ) : null}
      {hasMenuPermission(permissions, "menu-permissions", "list") ? (
        <MenuItemLink
          to="/menu-permissions"
          primaryText="Menu Permissions"
          leftIcon={<SecurityIcon />}
          dense={dense}
        />
      ) : null}
    </Menu>
  );
}
