import { Datagrid, FunctionField, List, NumberField, TextField } from "react-admin";
import { RecordDetailButton } from "../components/RecordDetailButton";

export function AccountProfileList() {
  return (
    <List title="내 계정" perPage={25} pagination={false} exporter={false} actions={false}>
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
        <FunctionField label="상세" render={() => <RecordDetailButton title="내 계정 상세" />} />
      </Datagrid>
    </List>
  );
}
