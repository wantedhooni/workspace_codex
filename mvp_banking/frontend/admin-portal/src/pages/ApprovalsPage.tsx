import { Button, Card, Col, Input, Modal, Row, Space, Tag, Typography } from "antd";
import type { Approval } from "../api";
import { OperationsGridTable } from "../components/OperationsGridTable";

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
  const pendingCount = approvals.filter((item) => item.status === "PENDING").length;
  const approvedCount = approvals.filter((item) => item.status === "APPROVED").length;
  const rejectedCount = approvals.filter((item) => item.status === "REJECTED").length;
  const topPendingItem = approvals.find((item) => item.status === "PENDING");

  return (
    <>
      <Card className="ops-hero-card" bordered={false}>
        <div className="ops-page-header">
          <div>
            <p className="eyebrow">Approval Control</p>
            <Typography.Title level={2} style={{ marginBottom: 8 }}>
              Approval Queue
            </Typography.Title>
            <Typography.Paragraph type="secondary" style={{ margin: 0, maxWidth: 760 }}>
              승인, 반려, 사유 입력이 모두 한 흐름에서 끝나도록 정리한 운영 심사 대기함입니다. 처리 우선순위는 대기 건수와 미처리 타깃을 먼저 확인한 뒤 액션 컬럼에서 바로 수행합니다.
            </Typography.Paragraph>
          </div>
          <div className="ops-header-meta">
            <span>Pending Queue</span>
            <strong>{pendingCount}건</strong>
            <small>{topPendingItem ? `${topPendingItem.targetType} / ${topPendingItem.title}` : "현재 즉시 처리 대상 없음"}</small>
          </div>
        </div>
      </Card>

      <Row gutter={[16, 16]}>
        <Col xs={24} md={8}>
          <Card className="ops-stat-card">
            <Typography.Text type="secondary">Pending</Typography.Text>
            <Typography.Title level={3} style={{ margin: "12px 0 0", color: pendingCount > 0 ? "#d46b08" : undefined }}>
              {pendingCount}건
            </Typography.Title>
          </Card>
        </Col>
        <Col xs={24} md={8}>
          <Card className="ops-stat-card">
            <Typography.Text type="secondary">Approved</Typography.Text>
            <Typography.Title level={3} style={{ margin: "12px 0 0" }}>
              {approvedCount}건
            </Typography.Title>
          </Card>
        </Col>
        <Col xs={24} md={8}>
          <Card className="ops-stat-card">
            <Typography.Text type="secondary">Rejected</Typography.Text>
            <Typography.Title level={3} style={{ margin: "12px 0 0", color: rejectedCount > 0 ? "#cf1322" : undefined }}>
              {rejectedCount}건
            </Typography.Title>
          </Card>
        </Col>
      </Row>

      <Card className="ops-panel-card" title="Approval Queue">
        <OperationsGridTable
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
                <Tag color={value === "PENDING" ? "gold" : value === "APPROVED" ? "green" : value === "CANCELED" ? "default" : "red"}>{value}</Tag>
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
