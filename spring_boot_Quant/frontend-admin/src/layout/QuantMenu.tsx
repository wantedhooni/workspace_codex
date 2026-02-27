import { Box, Typography } from "@mui/material";
import { Menu, type MenuProps, useResourceDefinitions } from "react-admin";

type MenuSection = {
  label: string;
  resources: string[];
};

const sections: MenuSection[] = [
  {
    label: "수익 실행",
    resources: ["orders", "trades", "positions", "portfolioSummaries", "executionQualities"]
  },
  {
    label: "리스크 통제",
    resources: ["riskLimits", "riskAlerts", "orderHealth", "orderAudits"]
  },
  {
    label: "회계/정산",
    resources: ["journalVouchers", "ledgerEntries"]
  },
  {
    label: "운영 관리",
    resources: ["users", "roles", "menus", "menuPermissions"]
  },
  {
    label: "내 작업",
    resources: ["accountProfile", "accountSessions"]
  }
];

type ResourceDefinition = {
  hasList?: boolean;
};

export function QuantMenu(props: MenuProps) {
  const definitions = useResourceDefinitions() as Record<string, ResourceDefinition>;

  return (
    <Menu {...props}>
      <Menu.DashboardItem />
      {sections.map((section) => {
        const visibleResources = section.resources.filter((name) => definitions[name]?.hasList);
        if (visibleResources.length === 0) {
          return null;
        }

        return (
          <Box key={section.label} className="quant-menu-section">
            <Typography className="quant-menu-section-title">{section.label}</Typography>
            {visibleResources.map((name) => (
              <Menu.ResourceItem key={name} name={name} />
            ))}
          </Box>
        );
      })}
    </Menu>
  );
}
