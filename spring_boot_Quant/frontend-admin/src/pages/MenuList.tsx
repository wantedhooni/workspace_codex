import {
  Button,
  Datagrid,
  FunctionField,
  List,
  NumberField,
  NumberInput,
  SelectInput,
  TextField,
  TextInput,
  TopToolbar,
  useDataProvider,
  useNotify,
  usePermissions,
  useRecordContext,
  useRefresh
} from "react-admin";
import { CreateDialogButton } from "../components/CreateDialogButton";
import { RecordDetailButton } from "../components/RecordDetailButton";
import type { AppPermissions } from "../providers/authProvider";

function MenuListActions() {
  const { permissions } = usePermissions<AppPermissions>();
  const canCreate = Boolean((permissions as AppPermissions | undefined)?.menuPermissions?.menus?.canCreate);

  return (
    <TopToolbar>
      {canCreate ? (
        <CreateDialogButton resource="menus" title="메뉴 등록" defaultValues={{ enabled: true, sortOrder: 999 }} successMessage="메뉴가 등록되었습니다.">
          <NumberInput source="parentMenuId" label="상위메뉴ID" />
          <TextInput source="menuKey" label="메뉴키" />
          <TextInput source="menuLabel" label="메뉴명" />
          <TextInput source="path" label="경로" fullWidth />
          <TextInput source="icon" label="아이콘" />
          <NumberInput source="sortOrder" label="정렬" />
          <SelectInput
            source="enabled"
            label="활성"
            choices={[
              { id: true, name: "true" },
              { id: false, name: "false" }
            ]}
          />
        </CreateDialogButton>
      ) : null}
    </TopToolbar>
  );
}

function MenuActionButtons() {
  const record = useRecordContext();
  const dp = useDataProvider();
  const notify = useNotify();
  const refresh = useRefresh();
  const { permissions } = usePermissions<AppPermissions>();

  if (!record) return null;

  const canDelete = Boolean((permissions as AppPermissions | undefined)?.menuPermissions?.menus?.canDelete);
  const protectedMenuKeys = new Set([
    "orders",
    "trades",
    "positions",
    "portfolios",
    "savedViews",
    "portfolioSummaries",
    "orderHealth",
    "riskAlerts",
    "executionQualities",
    "riskLimits",
    "users",
    "roles",
    "menus",
    "menuPermissions",
    "journalVouchers",
    "ledgerEntries",
    "accountProfile",
    "accountSessions",
    "dashboard"
  ]);
  const isProtected = protectedMenuKeys.has(String(record.menuKey ?? ""));

  return (
    <Button
      label="삭제"
      disabled={!canDelete || isProtected}
      onClick={async () => {
        try {
          await dp.delete("menus", { id: record.menuId, previousData: record });
          notify("메뉴 삭제 완료", { type: "info" });
          refresh();
        } catch (error) {
          notify(error instanceof Error ? error.message : "메뉴 삭제 실패", { type: "warning" });
        }
      }}
    />
  );
}

export function MenuList() {
  return (
    <List
      title="메뉴 관리"
      perPage={25}
      actions={<MenuListActions />}
      filters={[
        <TextInput key="menuKey" source="menuKey" label="메뉴키" alwaysOn />,
        <TextInput key="menuLabel" source="menuLabel" label="메뉴명" />,
        <TextInput key="enabled" source="enabled" label="활성여부" />
      ]}
    >
      <Datagrid bulkActionButtons={false} rowClick={false}>
        <NumberField source="menuId" label="Menu ID" />
        <NumberField source="parentMenuId" label="Parent" />
        <TextField source="menuKey" label="메뉴키" />
        <TextField source="menuLabel" label="메뉴명" />
        <TextField source="path" label="경로" />
        <TextField source="icon" label="아이콘" />
        <NumberField source="sortOrder" label="정렬" />
        <TextField source="enabled" label="활성" />
        <FunctionField label="상세" render={() => <RecordDetailButton title="메뉴 상세" />} />
        <FunctionField label="액션" render={() => <MenuActionButtons />} />
      </Datagrid>
    </List>
  );
}
