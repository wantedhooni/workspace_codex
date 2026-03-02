import { useCallback, useEffect, useMemo, useState } from "react";
import { ThemedSiderV2, type RefineThemedLayoutV2SiderProps } from "@refinedev/mui";
import {
  Alert,
  Box,
  CircularProgress,
  Collapse,
  List,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Tooltip,
  Typography,
} from "@mui/material";
import DashboardIcon from "@mui/icons-material/Dashboard";
import AccountBalanceIcon from "@mui/icons-material/AccountBalance";
import PaymentsIcon from "@mui/icons-material/Payments";
import CurrencyExchangeIcon from "@mui/icons-material/CurrencyExchange";
import ScheduleIcon from "@mui/icons-material/Schedule";
import ManageSearchIcon from "@mui/icons-material/ManageSearch";
import MenuIcon from "@mui/icons-material/Menu";
import GavelIcon from "@mui/icons-material/Gavel";
import InsightsIcon from "@mui/icons-material/Insights";
import ReportProblemIcon from "@mui/icons-material/ReportProblem";
import PieChartOutlineIcon from "@mui/icons-material/PieChartOutline";
import CandlestickChartIcon from "@mui/icons-material/CandlestickChart";
import Inventory2Icon from "@mui/icons-material/Inventory2";
import MenuBookIcon from "@mui/icons-material/MenuBook";
import AutoAwesomeIcon from "@mui/icons-material/AutoAwesome";
import QueryStatsIcon from "@mui/icons-material/QueryStats";
import ExpandLessIcon from "@mui/icons-material/ExpandLess";
import ExpandMoreIcon from "@mui/icons-material/ExpandMore";
import { useLocation, useNavigate } from "react-router-dom";
import { api } from "../api/client";
import type { MenuItem } from "../types/models";

const iconByName: Record<string, JSX.Element> = {
  dashboard: <DashboardIcon fontSize="small" />,
  account_balance: <AccountBalanceIcon fontSize="small" />,
  payments: <PaymentsIcon fontSize="small" />,
  currency_exchange: <CurrencyExchangeIcon fontSize="small" />,
  schedule: <ScheduleIcon fontSize="small" />,
  manage_search: <ManageSearchIcon fontSize="small" />,
  menu: <MenuIcon fontSize="small" />,
  policy: <GavelIcon fontSize="small" />,
  risk: <InsightsIcon fontSize="small" />,
  incident: <ReportProblemIcon fontSize="small" />,
  pie_chart: <PieChartOutlineIcon fontSize="small" />,
  candlestick_chart: <CandlestickChartIcon fontSize="small" />,
  inventory_2: <Inventory2Icon fontSize="small" />,
  menu_book: <MenuBookIcon fontSize="small" />,
  auto_awesome: <AutoAwesomeIcon fontSize="small" />,
  query_stats: <QueryStatsIcon fontSize="small" />,
};

const hasNavigablePath = (menu: MenuItem) => menu.path.trim().length > 0;

const isPathSelected = (menu: MenuItem, pathname: string) =>
  hasNavigablePath(menu) && (pathname === menu.path || (menu.path !== "/" && pathname.startsWith(menu.path)));

const hasSelectedDescendant = (menu: MenuItem, pathname: string): boolean =>
  menu.children.some((child) => isPathSelected(child, pathname) || hasSelectedDescendant(child, pathname));

const flattenLeafMenus = (menus: MenuItem[]): MenuItem[] =>
  menus.flatMap((menu) => (menu.children.length === 0 ? [menu] : flattenLeafMenus(menu.children)));

const collectExpandedAncestorKeys = (menus: MenuItem[], pathname: string) => {
  const expandedKeys = new Set<string>();

  const visit = (nodes: MenuItem[], ancestors: string[]) => {
    for (const node of nodes) {
      const nextAncestors = [...ancestors, node.menuKey];
      if (isPathSelected(node, pathname)) {
        ancestors.forEach((key) => expandedKeys.add(key));
      }
      if (node.children.length > 0) {
        visit(node.children, nextAncestors);
      }
    }
  };

  visit(menus, []);
  return expandedKeys;
};

export function DbSider(props: RefineThemedLayoutV2SiderProps) {
  const navigate = useNavigate();
  const location = useLocation();
  const [menus, setMenus] = useState<MenuItem[]>([]);
  const [expandedKeys, setExpandedKeys] = useState<Record<string, boolean>>({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchMenus = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await api.get<MenuItem[]>("/menus/my");
      setMenus(res);
    } catch {
      setMenus([]);
      setError("메뉴를 불러오지 못했습니다. 백엔드 상태를 확인 후 다시 시도하세요.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void fetchMenus();
  }, [fetchMenus]);

  useEffect(() => {
    const ancestorKeys = collectExpandedAncestorKeys(menus, location.pathname);
    if (ancestorKeys.size === 0) {
      return;
    }

    setExpandedKeys((prev) => {
      const next = { ...prev };
      let changed = false;
      ancestorKeys.forEach((key) => {
        if (!next[key]) {
          next[key] = true;
          changed = true;
        }
      });
      return changed ? next : prev;
    });
  }, [menus, location.pathname]);

  const collapsedLeafMenus = useMemo(() => flattenLeafMenus(menus), [menus]);

  const renderTooltip = (menu: MenuItem) => (
    <Box sx={{ maxWidth: 280, py: 0.25 }}>
      <Typography variant="subtitle2" sx={{ fontWeight: 700 }}>
        {menu.title}
      </Typography>
      <Typography variant="caption" sx={{ display: "block", mt: 0.5, lineHeight: 1.5 }}>
        {menu.description ?? "화면 설명이 아직 등록되지 않았습니다."}
      </Typography>
    </Box>
  );

  const toggleExpanded = (menuKey: string) => {
    setExpandedKeys((prev) => ({
      ...prev,
      [menuKey]: !prev[menuKey],
    }));
  };

  const renderMenuNode = (menu: MenuItem, collapsed: boolean, level = 0): JSX.Element => {
    const icon = menu.icon ? iconByName[menu.icon] : undefined;
    const hasChildren = menu.children.length > 0;
    const selectedSelf = isPathSelected(menu, location.pathname);
    const selectedDescendant = hasChildren && hasSelectedDescendant(menu, location.pathname);
    const expanded = Boolean(expandedKeys[menu.menuKey]) || selectedDescendant;

    const handleClick = () => {
      if (hasChildren && !collapsed) {
        toggleExpanded(menu.menuKey);
        return;
      }
      if (hasNavigablePath(menu)) {
        navigate(menu.path);
      }
    };

    return (
      <Box key={menu.menuKey}>
        <Tooltip title={renderTooltip(menu)} placement="right" arrow enterDelay={250}>
          <ListItemButton
            selected={selectedSelf || (!hasNavigablePath(menu) && selectedDescendant)}
            onClick={handleClick}
            sx={{
              borderRadius: 1,
              mb: 0.5,
              pl: collapsed ? 1 : 1 + level * 2,
            }}
          >
            <ListItemIcon sx={{ minWidth: collapsed ? 0 : 32, justifyContent: "center" }}>
              {icon ?? <MenuIcon fontSize="small" />}
            </ListItemIcon>
            {!collapsed && <ListItemText primary={menu.title} />}
            {!collapsed && hasChildren && (expanded ? <ExpandLessIcon fontSize="small" /> : <ExpandMoreIcon fontSize="small" />)}
          </ListItemButton>
        </Tooltip>
        {!collapsed && hasChildren && (
          <Collapse in={expanded} timeout="auto" unmountOnExit>
            <List dense disablePadding>
              {menu.children.map((child) => renderMenuNode(child, collapsed, level + 1))}
            </List>
          </Collapse>
        )}
      </Box>
    );
  };

  return (
    <ThemedSiderV2
      {...props}
      render={({ collapsed, logout }) => (
        <Box sx={{ display: "flex", flexDirection: "column", minHeight: "100%" }}>
          {loading ? (
            <Box sx={{ display: "flex", justifyContent: "center", py: 3 }}>
              <CircularProgress size={18} />
            </Box>
          ) : error ? (
            <Box sx={{ p: 1 }}>
              <Alert severity="warning" onClose={() => void fetchMenus()}>
                {error}
              </Alert>
            </Box>
          ) : (
            <List dense sx={{ px: 1 }}>
              {(collapsed ? collapsedLeafMenus : menus).map((menu) => renderMenuNode(menu, collapsed))}
            </List>
          )}
          <Box sx={{ mt: "auto" }}>{logout}</Box>
        </Box>
      )}
    />
  );
}
