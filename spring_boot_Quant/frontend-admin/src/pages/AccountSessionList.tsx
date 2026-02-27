import {
  Button,
  Datagrid,
  FunctionField,
  List,
  NumberField,
  TextField,
  useDataProvider,
  useNotify,
  useRecordContext,
  useRefresh
} from "react-admin";
import { RecordDetailButton } from "../components/RecordDetailButton";

function SessionActionButtons() {
  const record = useRecordContext();
  const dp = useDataProvider();
  const notify = useNotify();
  const refresh = useRefresh();

  if (!record) return null;

  const active = Boolean(record.active);

  const revoke = async () => {
    try {
      await dp.update("accountSessions", {
        id: record.sessionId,
        data: {},
        previousData: record,
        meta: { action: "revoke" }
      });
      notify("세션 해지 완료", { type: "info" });
      refresh();
    } catch (error) {
      notify(error instanceof Error ? error.message : "세션 해지 실패", { type: "warning" });
    }
  };

  return <Button label="세션해지" disabled={!active} onClick={revoke} />;
}

export function AccountSessionList() {
  return (
    <List title="내 세션" perPage={25}>
      <Datagrid bulkActionButtons={false} rowClick={false}>
        <NumberField source="sessionId" label="Session ID" />
        <TextField source="ipAddress" label="IP" />
        <TextField source="userAgent" label="User Agent" />
        <TextField source="active" label="활성" />
        <TextField source="createdAt" label="생성시각" />
        <TextField source="lastAccessAt" label="마지막 접근" />
        <FunctionField label="상세" render={() => <RecordDetailButton title="세션 상세" />} />
        <FunctionField label="액션" render={() => <SessionActionButtons />} />
      </Datagrid>
    </List>
  );
}
