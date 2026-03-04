import { Card, Col, Row, Table, Tag, Typography } from "antd";
import type { FundingRequest } from "../api";

type FundingRequestsPageProps = {
  fundingRequests: FundingRequest[];
};

export function FundingRequestsPage({ fundingRequests }: FundingRequestsPageProps) {
  const pendingRequests = fundingRequests.filter((item) => item.status === "PENDING_APPROVAL");
  const approvedToday = fundingRequests.filter(
    (item) => item.status === "APPROVED" && item.settledAt && new Date(item.settledAt).toDateString() === new Date().toDateString(),
  ).length;
  const pendingDepositCount = pendingRequests.filter((item) => item.requestType === "DEPOSIT").length;
  const pendingWithdrawalCount = pendingRequests.filter((item) => item.requestType === "WITHDRAWAL").length;
  const pendingVolume = pendingRequests.reduce((sum, item) => sum + Number(item.amount), 0);

  return (
    <>
      <div>
        <Typography.Title level={2} style={{ marginBottom: 8 }}>
          Funding Operations
        </Typography.Title>
        <Typography.Paragraph type="secondary" style={{ margin: 0 }}>
          고객 입금/출금 요청의 큐 볼륨, 계좌 스냅샷, 정산 거래번호를 한 화면에서 점검하는 운영 페이지입니다.
        </Typography.Paragraph>
      </div>

      <Row gutter={[16, 16]} style={{ marginTop: 16, marginBottom: 16 }}>
        <Col xs={24} md={8}>
          <Card>
            <Typography.Text type="secondary">Pending Deposits</Typography.Text>
            <Typography.Title level={3} style={{ margin: "12px 0 0" }}>
              {pendingDepositCount}건
            </Typography.Title>
          </Card>
        </Col>
        <Col xs={24} md={8}>
          <Card>
            <Typography.Text type="secondary">Pending Withdrawals</Typography.Text>
            <Typography.Title level={3} style={{ margin: "12px 0 0" }}>
              {pendingWithdrawalCount}건
            </Typography.Title>
          </Card>
        </Col>
        <Col xs={24} md={8}>
          <Card>
            <Typography.Text type="secondary">Approved Today</Typography.Text>
            <Typography.Title level={3} style={{ margin: "12px 0 0" }}>
              {approvedToday}건
            </Typography.Title>
            <Typography.Paragraph type="secondary" style={{ marginBottom: 0 }}>
              대기 물량 {pendingVolume.toLocaleString()} {pendingRequests[0]?.currency ?? "KRW"} 규모
            </Typography.Paragraph>
          </Card>
        </Col>
      </Row>

      <Card title="Funding Request Queue">
        <Table
          rowKey="id"
          dataSource={fundingRequests}
          pagination={false}
          columns={[
            {
              title: "Type",
              width: 120,
              render: (_, record: FundingRequest) => (
                <Tag color={record.requestType === "WITHDRAWAL" ? "volcano" : "green"}>{record.requestType}</Tag>
              ),
            },
            {
              title: "Request",
              render: (_, record: FundingRequest) => (
                <div>
                  <Typography.Text strong>{record.requestNumber}</Typography.Text>
                  <div>
                    <Typography.Text type="secondary">{record.customerEmail}</Typography.Text>
                  </div>
                </div>
              ),
            },
            {
              title: "Account Snapshot",
              render: (_, record: FundingRequest) => (
                <div>
                  <div>{record.accountNumber}</div>
                  <Typography.Text type="secondary">{record.accountType}</Typography.Text>
                </div>
              ),
            },
            {
              title: "Amount",
              render: (_, record: FundingRequest) => (
                <div>
                  <Typography.Text strong>{Number(record.amount).toLocaleString()} {record.currency}</Typography.Text>
                  <div>
                    <Typography.Text type="secondary">snapshot {Number(record.balanceSnapshot).toLocaleString()} {record.currency}</Typography.Text>
                  </div>
                </div>
              ),
            },
            {
              title: "Status",
              width: 140,
              render: (_, record: FundingRequest) => (
                <Tag color={record.status === "PENDING_APPROVAL" ? "gold" : record.status === "APPROVED" ? "green" : "red"}>
                  {record.status}
                </Tag>
              ),
            },
            {
              title: "Settlement",
              render: (_, record: FundingRequest) => (
                <div>
                  <div>{record.settlementTransactionNumber ?? "정산 대기"}</div>
                  <Typography.Text type="secondary">
                    {record.settledAt ? new Date(record.settledAt).toLocaleString() : "-"}
                  </Typography.Text>
                </div>
              ),
            },
            {
              title: "Note",
              render: (_, record: FundingRequest) => record.note ?? "-",
            },
          ]}
        />
      </Card>
    </>
  );
}
