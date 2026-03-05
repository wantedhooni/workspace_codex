import { Button, Card, Col, Row, Space, Table, Tag, Typography } from "antd";
import { useMemo, useState } from "react";
import type { LinkedBankAccount } from "../api";

type LinkedBankAccountsPageProps = {
  linkedBankAccounts: LinkedBankAccount[];
  activatingId: string | null;
  blockingId: string | null;
  onActivate: (linkedBankAccountId: string) => Promise<void>;
  onBlock: (linkedBankAccountId: string) => Promise<void>;
};

export function LinkedBankAccountsPage({ linkedBankAccounts, activatingId, blockingId, onActivate, onBlock }: LinkedBankAccountsPageProps) {
  const [filter, setFilter] = useState<"ALL" | "PENDING" | "STALE" | "BLOCKED">("ALL");
  const activeCount = linkedBankAccounts.filter((item) => item.status === "ACTIVE").length;
  const pendingCount = linkedBankAccounts.filter((item) => item.status === "PENDING_VERIFICATION").length;
  const stalePendingCount = linkedBankAccounts.filter((item) => item.status === "PENDING_VERIFICATION" && item.verificationExpired).length;
  const primaryCount = linkedBankAccounts.filter((item) => item.status === "ACTIVE" && item.primaryWithdrawal).length;
  const blockedCount = linkedBankAccounts.filter((item) => item.status === "BLOCKED").length;
  const blockedByOpsCount = linkedBankAccounts.filter((item) => item.blockReasonCode === "OPS_BLOCKED").length;
  const blockedByVerificationCount = linkedBankAccounts.filter((item) => item.blockReasonCode === "VERIFICATION_ATTEMPTS_EXCEEDED").length;
  const filteredAccounts = useMemo(() => {
    if (filter === "PENDING") {
      return linkedBankAccounts.filter((item) => item.status === "PENDING_VERIFICATION");
    }
    if (filter === "STALE") {
      return linkedBankAccounts.filter((item) => item.status === "PENDING_VERIFICATION" && item.verificationExpired);
    }
    if (filter === "BLOCKED") {
      return linkedBankAccounts.filter((item) => item.status === "BLOCKED");
    }
    return linkedBankAccounts;
  }, [filter, linkedBankAccounts]);

  return (
    <>
      <div>
        <Typography.Title level={2} style={{ marginBottom: 8 }}>
          Linked Bank Accounts
        </Typography.Title>
        <Typography.Paragraph type="secondary" style={{ margin: 0 }}>
          사용자 출금 목적지로 연결된 외부 은행 계좌의 상태와 기본 출금 설정을 운영 관점에서 점검합니다.
        </Typography.Paragraph>
      </div>

      <Row gutter={[16, 16]} style={{ marginTop: 16, marginBottom: 16 }}>
        <Col xs={24} md={12}>
          <Card>
            <Typography.Text type="secondary">Active Linked Accounts</Typography.Text>
            <Typography.Title level={3} style={{ margin: "12px 0 0" }}>
              {activeCount}건
            </Typography.Title>
          </Card>
        </Col>
        <Col xs={24} md={12}>
          <Card>
            <Typography.Text type="secondary">Primary Withdrawal Accounts</Typography.Text>
            <Typography.Title level={3} style={{ margin: "12px 0 0" }}>
              {primaryCount}건
            </Typography.Title>
          </Card>
        </Col>
        <Col xs={24} md={12}>
          <Card>
            <Typography.Text type="secondary">Pending Verification</Typography.Text>
            <Typography.Title level={3} style={{ margin: "12px 0 0" }}>
              {pendingCount}건
            </Typography.Title>
          </Card>
        </Col>
        <Col xs={24} md={12}>
          <Card>
            <Typography.Text type="secondary">Stale Verification Queue</Typography.Text>
            <Typography.Title level={3} style={{ margin: "12px 0 0" }}>
              {stalePendingCount}건
            </Typography.Title>
          </Card>
        </Col>
        <Col xs={24} md={12}>
          <Card>
            <Typography.Text type="secondary">Blocked Linked Accounts</Typography.Text>
            <Typography.Title level={3} style={{ margin: "12px 0 0" }}>
              {blockedCount}건
            </Typography.Title>
            <Typography.Paragraph type="secondary" style={{ marginBottom: 0 }}>
              ops {blockedByOpsCount} / verification {blockedByVerificationCount}
            </Typography.Paragraph>
          </Card>
        </Col>
      </Row>

      <Card title="Linked Bank Account Registry">
        <Space wrap style={{ marginBottom: 16 }}>
          <Button type={filter === "ALL" ? "primary" : "default"} onClick={() => setFilter("ALL")}>All</Button>
          <Button type={filter === "PENDING" ? "primary" : "default"} onClick={() => setFilter("PENDING")}>Pending</Button>
          <Button type={filter === "STALE" ? "primary" : "default"} onClick={() => setFilter("STALE")}>Stale</Button>
          <Button type={filter === "BLOCKED" ? "primary" : "default"} onClick={() => setFilter("BLOCKED")}>Blocked</Button>
        </Space>
        <Table
          rowKey="id"
          dataSource={filteredAccounts}
          pagination={false}
          columns={[
            {
              title: "Customer",
              render: (_, record: LinkedBankAccount) => (
                <div>
                  <Typography.Text strong>{record.customerEmail}</Typography.Text>
                  <div>
                    <Typography.Text type="secondary">{record.customerId}</Typography.Text>
                  </div>
                </div>
              ),
            },
            {
              title: "Bank",
              render: (_, record: LinkedBankAccount) => (
                <div>
                  <div>{record.bankName}</div>
                  <Typography.Text type="secondary">{record.accountAlias}</Typography.Text>
                </div>
              ),
            },
            { title: "Holder", dataIndex: "accountHolderName" },
            { title: "Masked Account", dataIndex: "maskedAccountNumber" },
            {
              title: "Verification",
              render: (_, record: LinkedBankAccount) => (
                <div>
                  <div>{record.verificationReference ?? "ACTIVE / OVERRIDDEN"}</div>
                  <Typography.Text type="secondary">
                    {record.verificationRequestedAt ? new Date(record.verificationRequestedAt).toLocaleString() : "요청 없음"}
                  </Typography.Text>
                  <div>
                    <Typography.Text type="secondary">
                      expires {record.verificationExpiresAt ? new Date(record.verificationExpiresAt).toLocaleString() : "-"}
                    </Typography.Text>
                  </div>
                  <div>
                    <Typography.Text type="secondary">
                      resend {record.verificationResendAllowed ? "now" : record.verificationResendAvailableAt ? new Date(record.verificationResendAvailableAt).toLocaleTimeString() : "-"}
                    </Typography.Text>
                  </div>
                  <div>
                    <Typography.Text type="secondary">attempt {record.verificationAttemptCount} / 5</Typography.Text>
                  </div>
                </div>
              ),
            },
            {
              title: "Block Reason",
              render: (_, record: LinkedBankAccount) => (
                record.status === "BLOCKED"
                  ? (
                    <div>
                      <div>{renderBlockReason(record.blockReasonCode)}</div>
                      <Typography.Text type="secondary">
                        {record.blockedAt ? new Date(record.blockedAt).toLocaleString() : "-"}
                      </Typography.Text>
                    </div>
                  )
                  : "-"
              ),
            },
            {
              title: "Status",
              render: (_, record: LinkedBankAccount) => (
                <>
                  <Tag color={record.status === "ACTIVE" ? "green" : record.status === "BLOCKED" ? "red" : "gold"}>
                    {record.status}
                  </Tag>
                  {record.status === "PENDING_VERIFICATION" && record.verificationExpired ? (
                    <Tag color="volcano">EXPIRED</Tag>
                  ) : null}
                </>
              ),
            },
            {
              title: "Primary Withdrawal",
              render: (_, record: LinkedBankAccount) => (
                record.primaryWithdrawal
                  ? <Tag color={record.status === "ACTIVE" ? "blue" : "gold"}>{record.status === "ACTIVE" ? "PRIMARY" : "PRIMARY REQUEST"}</Tag>
                  : "-"
              ),
            },
            {
              title: "Verified At",
              render: (_, record: LinkedBankAccount) => record.verifiedAt ? new Date(record.verifiedAt).toLocaleString() : "-",
            },
            {
              title: "Action",
              render: (_, record: LinkedBankAccount) => {
                if (record.status === "PENDING_VERIFICATION") {
                  return (
                    <div style={{ display: "flex", gap: 8, flexWrap: "wrap" }}>
                      <Button
                        type="primary"
                        loading={activatingId === record.id}
                        onClick={() => void onActivate(record.id)}
                      >
                        Activate Override
                      </Button>
                      <Button
                        danger
                        loading={blockingId === record.id}
                        onClick={() => void onBlock(record.id)}
                      >
                        Block
                      </Button>
                    </div>
                  );
                }

                if (record.status === "ACTIVE") {
                  return (
                    <Button
                      danger
                      loading={blockingId === record.id}
                      onClick={() => void onBlock(record.id)}
                    >
                      Block
                    </Button>
                  );
                }

                return <Typography.Text type="secondary">Blocked</Typography.Text>;
              },
            },
          ]}
        />
      </Card>
    </>
  );
}

function renderBlockReason(blockReasonCode: string | null) {
  if (blockReasonCode === "VERIFICATION_ATTEMPTS_EXCEEDED") {
    return "Verification failures";
  }
  if (blockReasonCode === "OPS_BLOCKED") {
    return "Ops blocked";
  }
  return "Blocked";
}
