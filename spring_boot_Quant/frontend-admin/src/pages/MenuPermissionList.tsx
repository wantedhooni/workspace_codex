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

function MenuPermissionListActions() {
  const { permissions } = usePermissions<AppPermissions>();
  const canCreate = Boolean((permissions as AppPermissions | undefined)?.menuPermissions?.menuPermissions?.canCreate);

  return (
    <TopToolbar>
      {canCreate ? (
        <CreateDialogButton
          resource="menuPermissions"
          title="메뉴권한 등록/수정"
          defaultValues={{
            canRead: true,
            canCreate: false,
            canUpdate: false,
            canDelete: false
          }}
          successMessage="메뉴권한이 저장되었습니다."
        >
          <NumberInput source="menuId" label="Menu ID" />
          <NumberInput source="roleId" label="Role ID" />
          <SelectInput
            source="canRead"
            label="조회"
            choices={[
              { id: true, name: "true" },
              { id: false, name: "false" }
            ]}
          />
          <SelectInput
            source="canCreate"
            label="등록"
            choices={[
              { id: true, name: "true" },
              { id: false, name: "false" }
            ]}
          />
          <SelectInput
            source="canUpdate"
            label="수정"
            choices={[
              { id: true, name: "true" },
              { id: false, name: "false" }
            ]}
          />
          <SelectInput
            source="canDelete"
            label="삭제"
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

function MenuPermissionActionButtons() {
  const record = useRecordContext();
  const dp = useDataProvider();
  const notify = useNotify();
  const refresh = useRefresh();
  const { permissions } = usePermissions<AppPermissions>();

  if (!record) return null;

  const canDelete = Boolean((permissions as AppPermissions | undefined)?.menuPermissions?.menuPermissions?.canDelete);

  return (
    <Button
      label="삭제"
      disabled={!canDelete}
      onClick={async () => {
        try {
          await dp.delete("menuPermissions", { id: record.menuPermissionId, previousData: record });
          notify("메뉴권한 삭제 완료", { type: "info" });
          refresh();
        } catch (error) {
          notify(error instanceof Error ? error.message : "메뉴권한 삭제 실패", { type: "warning" });
        }
      }}
    />
  );
}

export function MenuPermissionList() {
  return (
    <List
      title="메뉴권한 관리"
      perPage={25}
      actions={<MenuPermissionListActions />}
      filters={[
        <TextInput key="roleCode" source="roleCode" label="권한코드" alwaysOn />,
        <TextInput key="menuKey" source="menuKey" label="메뉴키" />
      ]}
    >
      <Datagrid bulkActionButtons={false} rowClick={false}>
        <NumberField source="menuPermissionId" label="ID" />
        <NumberField source="menuId" label="Menu ID" />
        <TextField source="menuKey" label="메뉴" />
        <NumberField source="roleId" label="Role ID" />
        <TextField source="roleCode" label="권한" />
        <TextField source="canRead" label="조회" />
        <TextField source="canCreate" label="등록" />
        <TextField source="canUpdate" label="수정" />
        <TextField source="canDelete" label="삭제" />
        <TextField source="updatedAt" label="수정시각" />
        <FunctionField label="상세" render={() => <RecordDetailButton title="메뉴권한 상세" />} />
        <FunctionField label="액션" render={() => <MenuPermissionActionButtons />} />
      </Datagrid>
    </List>
  );
}
