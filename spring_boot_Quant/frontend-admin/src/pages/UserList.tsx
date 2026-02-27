import {
  Button,
  Datagrid,
  FunctionField,
  List,
  NumberField,
  SelectArrayInput,
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

type UserStatus = "ACTIVE" | "LOCKED" | "INACTIVE";

const ROLE_CHOICES = [
  { id: "ADMIN", name: "ADMIN" },
  { id: "QUANT", name: "QUANT" },
  { id: "VIEWER", name: "VIEWER" },
  { id: "RISK", name: "RISK" }
];

function UserListActions() {
  const { permissions } = usePermissions<AppPermissions>();
  const canCreate = Boolean((permissions as AppPermissions | undefined)?.menuPermissions?.users?.canCreate);

  return (
    <TopToolbar>
      {canCreate ? (
        <CreateDialogButton
          resource="users"
          title="사용자 등록"
          defaultValues={{ status: "ACTIVE", roleCodes: ["VIEWER"] }}
          successMessage="사용자가 등록되었습니다."
        >
          <TextInput source="email" label="이메일" fullWidth />
          <TextInput source="name" label="이름" />
          <SelectInput
            source="status"
            label="상태"
            choices={[
              { id: "ACTIVE", name: "ACTIVE" },
              { id: "LOCKED", name: "LOCKED" },
              { id: "INACTIVE", name: "INACTIVE" }
            ]}
          />
          <SelectArrayInput source="roleCodes" label="권한" choices={ROLE_CHOICES} />
        </CreateDialogButton>
      ) : null}
    </TopToolbar>
  );
}

function UserActionButtons() {
  const record = useRecordContext();
  const dp = useDataProvider();
  const notify = useNotify();
  const refresh = useRefresh();
  const { permissions } = usePermissions<AppPermissions>();

  if (!record) return null;

  const status = (record.status as UserStatus | undefined) ?? "ACTIVE";
  const canDelete = Boolean((permissions as AppPermissions | undefined)?.menuPermissions?.users?.canDelete);

  const changeStatus = async (nextStatus: UserStatus) => {
    try {
      await dp.update("users", {
        id: record.userId,
        data: { status: nextStatus },
        previousData: record,
        meta: { action: "status" }
      });
      notify(`상태 변경 완료: ${nextStatus}`, { type: "info" });
      refresh();
    } catch (error) {
      notify(error instanceof Error ? error.message : "상태 변경 실패", { type: "warning" });
    }
  };

  const resetPassword = async () => {
    try {
      const res = await dp.update("users", {
        id: record.userId,
        data: {},
        previousData: record,
        meta: { action: "resetPassword" }
      });
      const token = String(res.data.resetToken ?? "");
      const expiresAt = String(res.data.expiresAt ?? "");
      if (token) {
        notify(`비밀번호 재설정 토큰 발급 완료 (expiresAt=${expiresAt || "n/a"})`, { type: "info" });
      } else {
        notify("비밀번호 재설정 토큰 발급 완료", { type: "info" });
      }
      refresh();
    } catch (error) {
      notify(error instanceof Error ? error.message : "비밀번호 초기화 실패", { type: "warning" });
    }
  };

  return (
    <>
      <Button label="활성" disabled={status === "ACTIVE"} onClick={() => changeStatus("ACTIVE")} />
      <Button label="잠금" disabled={status === "LOCKED"} onClick={() => changeStatus("LOCKED")} />
      <Button label="비번초기화" onClick={resetPassword} />
      <Button
        label="삭제"
        disabled={!canDelete}
        onClick={async () => {
          try {
            await dp.delete("users", { id: record.userId, previousData: record });
            notify("사용자 삭제 완료", { type: "info" });
            refresh();
          } catch (error) {
            notify(error instanceof Error ? error.message : "사용자 삭제 실패", { type: "warning" });
          }
        }}
      />
    </>
  );
}

export function UserList() {
  return (
    <List
      title="사용자 관리"
      perPage={25}
      actions={<UserListActions />}
      filters={[
        <TextInput key="email" source="email" label="이메일" alwaysOn />,
        <TextInput key="name" source="name" label="이름" />,
        <TextInput key="status" source="status" label="상태" />,
        <TextInput key="roleCode" source="roleCode" label="권한코드" />
      ]}
    >
      <Datagrid bulkActionButtons={false} rowClick={false}>
        <NumberField source="userId" label="User ID" />
        <TextField source="email" label="이메일" />
        <TextField source="name" label="이름" />
        <TextField source="status" label="상태" />
        <FunctionField
          source="roleCodes"
          label="권한"
          render={(record) =>
            Array.isArray(record.roleCodes) ? (record.roleCodes as string[]).join(", ") : ""
          }
        />
        <TextField source="lastLoginAt" label="마지막 로그인" />
        <TextField source="updatedAt" label="수정시각" />
        <FunctionField label="상세" render={() => <RecordDetailButton title="사용자 상세" />} />
        <FunctionField label="액션" render={() => <UserActionButtons />} />
      </Datagrid>
    </List>
  );
}
