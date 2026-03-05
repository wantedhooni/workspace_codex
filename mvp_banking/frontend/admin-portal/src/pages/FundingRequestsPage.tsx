import { Card, Col, Row, Table, Tag, Typography } from "antd";
import type { FundingRequest } from "../api";

type FundingRequestsPageProps = {
  fundingRequests: FundingRequest[];
};

export function FundingRequestsPage({ fundingRequests }: FundingRequestsPageProps) {
  const pendingRequests = fundingRequests.filter((item) => item.status === "PENDING_APPROVAL");
  const canceledToday = fundingRequests.filter(
    (item) => item.status === "CANCELED" && item.canceledAt && new Date(item.canceledAt).toDateString() === new Date().toDateString(),
  ).length;
  const approvedToday = fundingRequests.filter(
    (item) => item.status === "APPROVED" && item.settledAt && new Date(item.settledAt).toDateString() === new Date().toDateString(),
  ).length;
  const pendingDepositCount = pendingRequests.filter((item) => item.requestType === "DEPOSIT").length;
  const pendingWithdrawalCount = pendingRequests.filter((item) => item.requestType === "WITHDRAWAL").length;
  const pendingWithdrawalFeeVolume = pendingRequests
    .filter((item) => item.requestType === "WITHDRAWAL")
    .reduce((sum, item) => sum + Number(item.serviceFeeAmount), 0);
  const pendingPriorityCount = pendingRequests.filter((item) => item.priorityProcessing).length;
  const pendingPriorityFeeVolume = pendingRequests
    .filter((item) => item.requestType === "WITHDRAWAL" && item.priorityProcessing)
    .reduce((sum, item) => sum + Number(item.priorityFeeAmount), 0);
  const pendingVolume = pendingRequests.reduce((sum, item) => sum + Number(item.amount), 0);
  const manualReviewCount = pendingRequests.filter((item) => item.manualReviewRequired).length;
  const nextWindowCount = pendingRequests.filter((item) => !item.sameDaySettlementEligible).length;

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
            <Typography.Paragraph type="secondary" style={{ marginBottom: 0 }}>
              기본 수수료 {pendingWithdrawalFeeVolume.toLocaleString()} / 우선 수수료 {pendingPriorityFeeVolume.toLocaleString()}
            </Typography.Paragraph>
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
        <Col xs={24} md={8}>
          <Card>
            <Typography.Text type="secondary">Manual Review Flags</Typography.Text>
            <Typography.Title level={3} style={{ margin: "12px 0 0" }}>
              {manualReviewCount}건
            </Typography.Title>
            <Typography.Paragraph type="secondary" style={{ marginBottom: 0 }}>
              고액 요청 또는 일일 한도 초과로 추가 심사가 필요한 요청
            </Typography.Paragraph>
          </Card>
        </Col>
        <Col xs={24} md={8}>
          <Card>
            <Typography.Text type="secondary">Next Settlement Window</Typography.Text>
            <Typography.Title level={3} style={{ margin: "12px 0 0" }}>
              {nextWindowCount}건
            </Typography.Title>
            <Typography.Paragraph type="secondary" style={{ marginBottom: 0 }}>
              컷오프 이후 또는 영업일 외 요청으로 다음 영업일 정산 예정
            </Typography.Paragraph>
          </Card>
        </Col>
        <Col xs={24} md={8}>
          <Card>
            <Typography.Text type="secondary">Canceled Today</Typography.Text>
            <Typography.Title level={3} style={{ margin: "12px 0 0" }}>
              {canceledToday}건
            </Typography.Title>
            <Typography.Paragraph type="secondary" style={{ marginBottom: 0 }}>
              사용자 직접 취소로 승인 큐에서 제외된 요청
            </Typography.Paragraph>
          </Card>
        </Col>
        <Col xs={24} md={8}>
          <Card>
            <Typography.Text type="secondary">Priority Requests</Typography.Text>
            <Typography.Title level={3} style={{ margin: "12px 0 0" }}>
              {pendingPriorityCount}건
            </Typography.Title>
            <Typography.Paragraph type="secondary" style={{ marginBottom: 0 }}>
              우선처리 플래그가 설정된 승인 대기 요청
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
                    <Typography.Text type="secondary">
                      base fee {Number(record.serviceFeeAmount).toLocaleString()} / priority fee {Number(record.priorityFeeAmount).toLocaleString()} / total debit {Number(record.totalDebitAmount).toLocaleString()} {record.currency}
                    </Typography.Text>
                  </div>
                  <div>
                    <Typography.Text type="secondary">snapshot {Number(record.balanceSnapshot).toLocaleString()} {record.currency}</Typography.Text>
                  </div>
                </div>
              ),
            },
            {
              title: "Destination",
              render: (_, record: FundingRequest) => (
                record.requestType === "WITHDRAWAL" ? (
                  <div>
                    <div>{record.linkedBankName ?? "미지정"}</div>
                    <Typography.Text type="secondary">
                      {record.linkedBankAccountAlias ?? record.linkedBankAccountNumberMasked ?? "-"}
                    </Typography.Text>
                  </div>
                ) : (
                  "-"
                )
              ),
            },
            {
              title: "Policy",
              render: (_, record: FundingRequest) => (
                <div>
                  <div style={{ display: "flex", gap: 8, flexWrap: "wrap", marginBottom: 8 }}>
                    {record.manualReviewRequired ? <Tag color="volcano">MANUAL REVIEW</Tag> : <Tag color="green">STANDARD</Tag>}
                    {record.dailyLimitExceeded ? <Tag color="red">LIMIT</Tag> : null}
                    {record.sameDaySettlementEligible ? <Tag color="blue">SAME DAY</Tag> : <Tag color="gold">NEXT WINDOW</Tag>}
                    {record.priorityProcessing ? <Tag color="orange">PRIORITY</Tag> : null}
                  </div>
                  <Typography.Text type="secondary">
                    {record.status === "CANCELED"
                      ? record.cancellationReason ?? "사용자 요청 취소"
                      : record.manualReviewRequired ? record.manualReviewReason ?? "추가 심사" : "일반 승인 큐"}
                  </Typography.Text>
                  <div>
                    <Typography.Text type="secondary">
                      {record.expectedSettlementAt ? new Date(record.expectedSettlementAt).toLocaleString() : "-"}
                    </Typography.Text>
                  </div>
                </div>
              ),
            },
            {
              title: "Status",
              width: 140,
              render: (_, record: FundingRequest) => (
                <Tag color={
                  record.status === "PENDING_APPROVAL"
                    ? "gold"
                    : record.status === "APPROVED"
                      ? "green"
                      : record.status === "CANCELED"
                        ? "default"
                        : "red"
                }>
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
                    {record.status === "CANCELED" && record.canceledAt
                      ? `취소 ${new Date(record.canceledAt).toLocaleString()}`
                      : record.settledAt
                        ? new Date(record.settledAt).toLocaleString()
                        : "-"}
                  </Typography.Text>
                  {record.dailyLimitAmount !== null && record.dailyAccumulatedAmount !== null ? (
                    <div>
                      <Typography.Text type="secondary">
                        누적 {Number(record.dailyAccumulatedAmount).toLocaleString()} / 한도 {Number(record.dailyLimitAmount).toLocaleString()} {record.currency}
                      </Typography.Text>
                    </div>
                  ) : null}
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
