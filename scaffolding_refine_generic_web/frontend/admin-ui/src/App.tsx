import { Authenticated, Refine } from "@refinedev/core";
import { ThemedLayoutV2, ThemedSiderV2 } from "@refinedev/antd";
import routerProvider, { NavigateToResource, CatchAllNavigate } from "@refinedev/react-router-v6";
import { BrowserRouter, Link, Outlet, Route, Routes } from "react-router-dom";
import { Menu } from "antd";

import "@refinedev/antd/dist/reset.css";
import "./styles/app.css";

import { simpleRestProvider } from "./providers/simpleRest";
import { authProvider } from "./providers/authProvider";
import { AdminCreate, AdminEdit, AdminList } from "./pages/admins";
import { RoleCreate, RoleEdit, RoleList } from "./pages/roles";
import { PermissionCreate, PermissionEdit, PermissionList } from "./pages/permissions";
import { MenuCreate, MenuEdit, MenuList } from "./pages/menus";
import { ContentCreate, ContentEdit, ContentList } from "./pages/contents";
import { LoginPage } from "./pages/login";
import { StatisticsPage } from "./pages/statistics";
import {
  AccessLogList,
  BannerCreate,
  BannerEdit,
  BannerList,
  BatchJobCreate,
  BatchJobEdit,
  BatchJobList,
  BatchScheduleCreate,
  BatchScheduleEdit,
  BatchScheduleList,
  CommonCodeCreate,
  CommonCodeEdit,
  CommonCodeList,
  LoginPolicyCreate,
  LoginPolicyEdit,
  LoginPolicyList,
  ProgramCreate,
  ProgramEdit,
  ProgramList,
  RoleRouteCreate,
  RoleRouteEdit,
  RoleRouteList,
  ServiceAuditLogList,
} from "./pages/system";

const API_URL = import.meta.env.VITE_API_URL ?? "http://localhost:8090";

export const App = () => {
  const groupedSider = (props: any) => (
    <ThemedSiderV2
      {...props}
      render={({ dashboard, logout }) => (
        <>
          {dashboard}
          <Menu.SubMenu key="group-administration" title="Administration">
            <Menu.Item key="admins">
              <Link to="/admins">Admins</Link>
            </Menu.Item>
            <Menu.Item key="roles">
              <Link to="/roles">Roles</Link>
            </Menu.Item>
            <Menu.Item key="permissions">
              <Link to="/permissions">Permissions</Link>
            </Menu.Item>
          </Menu.SubMenu>
          <Menu.SubMenu key="group-content" title="Content">
            <Menu.Item key="contents">
              <Link to="/contents">Contents</Link>
            </Menu.Item>
            <Menu.Item key="banners">
              <Link to="/banners">Banners</Link>
            </Menu.Item>
          </Menu.SubMenu>
          <Menu.SubMenu key="group-system" title="System">
            <Menu.Item key="menus">
              <Link to="/menus">Menus</Link>
            </Menu.Item>
            <Menu.Item key="common-codes">
              <Link to="/common-codes">Common Codes</Link>
            </Menu.Item>
            <Menu.Item key="programs">
              <Link to="/programs">Programs</Link>
            </Menu.Item>
            <Menu.Item key="role-routes">
              <Link to="/role-routes">Role Routes</Link>
            </Menu.Item>
            <Menu.Item key="login-policies">
              <Link to="/login-policies">Login Policies</Link>
            </Menu.Item>
          </Menu.SubMenu>
          <Menu.SubMenu key="group-monitoring" title="Monitoring">
            <Menu.Item key="statistics">
              <Link to="/statistics">Statistics</Link>
            </Menu.Item>
            <Menu.Item key="access-logs">
              <Link to="/access-logs">Access Logs</Link>
            </Menu.Item>
            <Menu.Item key="service-audit-logs">
              <Link to="/service-audit-logs">Service Audit Logs</Link>
            </Menu.Item>
          </Menu.SubMenu>
          <Menu.SubMenu key="group-batch" title="Batch">
            <Menu.Item key="batch-jobs">
              <Link to="/batch-jobs">Batch Jobs</Link>
            </Menu.Item>
            <Menu.Item key="batch-schedules">
              <Link to="/batch-schedules">Batch Schedules</Link>
            </Menu.Item>
          </Menu.SubMenu>
          {logout}
        </>
      )}
    />
  );

  return (
    <BrowserRouter>
      <Refine
        routerProvider={routerProvider}
        dataProvider={simpleRestProvider(API_URL)}
        authProvider={authProvider}
        LoginPage={LoginPage}
        resources={[
          {
            name: "group-administration",
            meta: { label: "Administration" },
          },
          {
            name: "group-content",
            meta: { label: "Content" },
          },
          {
            name: "group-system",
            meta: { label: "System" },
          },
          {
            name: "group-monitoring",
            meta: { label: "Monitoring" },
          },
          {
            name: "group-batch",
            meta: { label: "Batch" },
          },
          {
            name: "admins",
            list: "/admins",
            create: "/admins/create",
            edit: "/admins/edit/:id",
            parentName: "group-administration",
            meta: { parent: "group-administration" },
          },
          {
            name: "roles",
            list: "/roles",
            create: "/roles/create",
            edit: "/roles/edit/:id",
            parentName: "group-administration",
            meta: { parent: "group-administration" },
          },
          {
            name: "permissions",
            list: "/permissions",
            create: "/permissions/create",
            edit: "/permissions/edit/:id",
            parentName: "group-administration",
            meta: { parent: "group-administration" },
          },
          {
            name: "menus",
            list: "/menus",
            create: "/menus/create",
            edit: "/menus/edit/:id",
            parentName: "group-system",
            meta: { parent: "group-system" },
          },
          {
            name: "contents",
            list: "/contents",
            create: "/contents/create",
            edit: "/contents/edit/:id",
            parentName: "group-content",
            meta: { parent: "group-content" },
          },
          {
            name: "common-codes",
            list: "/common-codes",
            create: "/common-codes/create",
            edit: "/common-codes/edit/:id",
            parentName: "group-system",
            meta: { parent: "group-system" },
          },
          {
            name: "programs",
            list: "/programs",
            create: "/programs/create",
            edit: "/programs/edit/:id",
            parentName: "group-system",
            meta: { parent: "group-system" },
          },
          {
            name: "role-routes",
            list: "/role-routes",
            create: "/role-routes/create",
            edit: "/role-routes/edit/:id",
            parentName: "group-system",
            meta: { parent: "group-system" },
          },
          {
            name: "access-logs",
            list: "/access-logs",
            parentName: "group-monitoring",
            meta: { parent: "group-monitoring" },
          },
          {
            name: "service-audit-logs",
            list: "/service-audit-logs",
            parentName: "group-monitoring",
            meta: { parent: "group-monitoring" },
          },
          {
            name: "batch-jobs",
            list: "/batch-jobs",
            create: "/batch-jobs/create",
            edit: "/batch-jobs/edit/:id",
            parentName: "group-batch",
            meta: { parent: "group-batch" },
          },
          {
            name: "batch-schedules",
            list: "/batch-schedules",
            create: "/batch-schedules/create",
            edit: "/batch-schedules/edit/:id",
            parentName: "group-batch",
            meta: { parent: "group-batch" },
          },
          {
            name: "banners",
            list: "/banners",
            create: "/banners/create",
            edit: "/banners/edit/:id",
            parentName: "group-content",
            meta: { parent: "group-content" },
          },
          {
            name: "login-policies",
            list: "/login-policies",
            create: "/login-policies/create",
            edit: "/login-policies/edit/:id",
            parentName: "group-system",
            meta: { parent: "group-system" },
          },
          {
            name: "statistics",
            list: "/statistics",
            parentName: "group-monitoring",
            meta: { parent: "group-monitoring" },
          },
        ]}
        options={{
          syncWithLocation: true,
          warnWhenUnsavedChanges: true,
        }}
      >
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route
            element={
              <Authenticated key="protected">
                <ThemedLayoutV2 Sider={groupedSider}>
                  <Outlet />
                </ThemedLayoutV2>
              </Authenticated>
            }
          >
            <Route index element={<NavigateToResource resource="admins" />} />
            <Route path="/admins" element={<AdminList />} />
            <Route path="/admins/create" element={<AdminCreate />} />
            <Route path="/admins/edit/:id" element={<AdminEdit />} />

            <Route path="/roles" element={<RoleList />} />
            <Route path="/roles/create" element={<RoleCreate />} />
            <Route path="/roles/edit/:id" element={<RoleEdit />} />

            <Route path="/permissions" element={<PermissionList />} />
            <Route path="/permissions/create" element={<PermissionCreate />} />
            <Route path="/permissions/edit/:id" element={<PermissionEdit />} />

            <Route path="/menus" element={<MenuList />} />
            <Route path="/menus/create" element={<MenuCreate />} />
            <Route path="/menus/edit/:id" element={<MenuEdit />} />

            <Route path="/contents" element={<ContentList />} />
            <Route path="/contents/create" element={<ContentCreate />} />
            <Route path="/contents/edit/:id" element={<ContentEdit />} />

            <Route path="/common-codes" element={<CommonCodeList />} />
            <Route path="/common-codes/create" element={<CommonCodeCreate />} />
            <Route path="/common-codes/edit/:id" element={<CommonCodeEdit />} />

            <Route path="/programs" element={<ProgramList />} />
            <Route path="/programs/create" element={<ProgramCreate />} />
            <Route path="/programs/edit/:id" element={<ProgramEdit />} />

            <Route path="/role-routes" element={<RoleRouteList />} />
            <Route path="/role-routes/create" element={<RoleRouteCreate />} />
            <Route path="/role-routes/edit/:id" element={<RoleRouteEdit />} />

            <Route path="/access-logs" element={<AccessLogList />} />

            <Route path="/service-audit-logs" element={<ServiceAuditLogList />} />

            <Route path="/batch-jobs" element={<BatchJobList />} />
            <Route path="/batch-jobs/create" element={<BatchJobCreate />} />
            <Route path="/batch-jobs/edit/:id" element={<BatchJobEdit />} />

            <Route path="/batch-schedules" element={<BatchScheduleList />} />
            <Route path="/batch-schedules/create" element={<BatchScheduleCreate />} />
            <Route path="/batch-schedules/edit/:id" element={<BatchScheduleEdit />} />

            <Route path="/banners" element={<BannerList />} />
            <Route path="/banners/create" element={<BannerCreate />} />
            <Route path="/banners/edit/:id" element={<BannerEdit />} />

            <Route path="/login-policies" element={<LoginPolicyList />} />
            <Route path="/login-policies/create" element={<LoginPolicyCreate />} />
            <Route path="/login-policies/edit/:id" element={<LoginPolicyEdit />} />

            <Route path="/statistics" element={<StatisticsPage />} />
          </Route>
          <Route path="*" element={<CatchAllNavigate to="/admins" />} />
        </Routes>
      </Refine>
    </BrowserRouter>
  );
};
