import { Card, Table } from "antd";
import type { AuditLog } from "../api";

type AuditLogsPageProps = {
  auditLogs: AuditLog[];
};

export function AuditLogsPage({ auditLogs }: AuditLogsPageProps) {
  return (
    <Card title="Audit Logs">
      <Table
        rowKey="id"
        dataSource={auditLogs}
        pagination={{ pageSize: 10 }}
        columns={[
          { title: "Actor", dataIndex: "actorEmail" },
          { title: "Action", dataIndex: "actionType" },
          { title: "Target", dataIndex: "targetType" },
          { title: "At", render: (_, record: AuditLog) => new Date(record.loggedAt).toLocaleString() },
        ]}
      />
    </Card>
  );
}
