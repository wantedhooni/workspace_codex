import {
  Button,
  Datagrid,
  FunctionField,
  List,
  NumberField,
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

function RoleListActions() {
  const { permissions } = usePermissions<AppPermissions>();
  const canCreate = Boolean((permissions as AppPermissions | undefined)?.menuPermissions?.roles?.canCreate);

  return (
    <TopToolbar>
      {canCreate ? (
        <CreateDialogButton resource="roles" title="권한 등록" defaultValues={{ systemRole: false }} successMessage="권한이 등록되었습니다.">
          <TextInput source="roleCode" label="권한코드" />
          <TextInput source="roleName" label="권한명" />
          <TextInput source="description" label="설명" fullWidth />
          <SelectInput
            source="systemRole"
            label="시스템권한"
            choices={[
              { id: false, name: "false" },
              { id: true, name: "true" }
            ]}
          />
        </CreateDialogButton>
      ) : null}
    </TopToolbar>
  );
}

function RoleActionButtons() {
  const record = useRecordContext();
  const dp = useDataProvider();
  const notify = useNotify();
  const refresh = useRefresh();
  const { permissions } = usePermissions<AppPermissions>();

  if (!record) return null;

  const canDelete = Boolean((permissions as AppPermissions | undefined)?.menuPermissions?.roles?.canDelete);
  const isSystemRole = Boolean(record.systemRole);

  return (
    <Button
      label="삭제"
      disabled={!canDelete || isSystemRole}
      onClick={async () => {
        try {
          await dp.delete("roles", { id: record.roleId, previousData: record });
          notify("권한 삭제 완료", { type: "info" });
          refresh();
        } catch (error) {
          notify(error instanceof Error ? error.message : "권한 삭제 실패", { type: "warning" });
        }
      }}
    />
  );
}

export function RoleList() {
  return (
    <List
      title="권한 관리"
      perPage={25}
      actions={<RoleListActions />}
      filters={[
        <TextInput key="roleCode" source="roleCode" label="권한코드" alwaysOn />,
        <TextInput key="roleName" source="roleName" label="권한명" />
      ]}
    >
      <Datagrid bulkActionButtons={false} rowClick={false}>
        <NumberField source="roleId" label="Role ID" />
        <TextField source="roleCode" label="권한코드" />
        <TextField source="roleName" label="권한명" />
        <TextField source="description" label="설명" />
        <TextField source="systemRole" label="시스템권한" />
        <TextField source="createdAt" label="생성시각" />
        <FunctionField label="상세" render={() => <RecordDetailButton title="권한 상세" />} />
        <FunctionField label="액션" render={() => <RoleActionButtons />} />
      </Datagrid>
    </List>
  );
}
