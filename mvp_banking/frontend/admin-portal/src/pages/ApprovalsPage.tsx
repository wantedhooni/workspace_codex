import { Button, Card, Modal, Space, Table, Tag, Typography, Input } from "antd";
import type { Approval } from "../api";

type ApprovalsPageProps = {
  approvals: Approval[];
  approvalModal: { id: string; action: "approve" | "reject" } | null;
  approvalReason: string;
  approvalSubmitting: boolean;
  onOpenModal: (modal: { id: string; action: "approve" | "reject" }) => void;
  onCloseModal: () => void;
  onReasonChange: (value: string) => void;
  onSubmit: () => void;
};

export function ApprovalsPage({
  approvals,
  approvalModal,
  approvalReason,
  approvalSubmitting,
  onOpenModal,
  onCloseModal,
  onReasonChange,
  onSubmit,
}: ApprovalsPageProps) {
  return (
    <>
      <Card title="Approval Queue">
        <Table
          rowKey="id"
          dataSource={approvals}
          pagination={false}
          columns={[
            { title: "Title", dataIndex: "title" },
            { title: "Target", dataIndex: "targetType" },
            {
              title: "Status",
              dataIndex: "status",
              render: (value: string) => (
                <Tag color={value === "PENDING" ? "gold" : value === "APPROVED" ? "green" : "red"}>{value}</Tag>
              ),
            },
            { title: "Requested By", dataIndex: "requestedByEmail" },
            {
              title: "Action",
              render: (_, record: Approval) =>
                record.status === "PENDING" ? (
                  <Space>
                    <Button size="small" type="primary" onClick={() => onOpenModal({ id: record.id, action: "approve" })}>
                      승인
                    </Button>
                    <Button size="small" danger onClick={() => onOpenModal({ id: record.id, action: "reject" })}>
                      반려
                    </Button>
                  </Space>
                ) : (
                  <Typography.Text type="secondary">{record.decisionReason ?? "-"}</Typography.Text>
                ),
            },
          ]}
        />
      </Card>
      <Modal
        open={approvalModal !== null}
        onCancel={onCloseModal}
        confirmLoading={approvalSubmitting}
        onOk={onSubmit}
        okText={approvalModal?.action === "approve" ? "승인 실행" : "반려 실행"}
        title={approvalModal?.action === "approve" ? "승인 처리" : "반려 처리"}
      >
        <Input.TextArea
          rows={4}
          value={approvalReason}
          onChange={(event) => onReasonChange(event.target.value)}
          placeholder="처리 사유를 입력하세요."
        />
      </Modal>
    </>
  );
}
