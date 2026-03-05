import { Button, Card, Col, Row, Space, Tag, Typography } from "antd";
import type { Notification } from "../api";
import { OperationsGridTable } from "../components/OperationsGridTable";

type NotificationsPageProps = {
  notifications: Notification[];
  unreadCount: number;
  readingNotificationId: string | null;
  onRead: (notificationId: string) => void;
};

export function NotificationsPage({ notifications, unreadCount, readingNotificationId, onRead }: NotificationsPageProps) {
  const actionRequiredCount = notifications.filter((item) => item.severity === "ACTION_REQUIRED" && !item.read).length;
  const todayCount = notifications.filter((item) => isSameBusinessDay(item.createdAt)).length;

  return (
    <Space direction="vertical" size={16} style={{ width: "100%" }}>
      <div>
        <Typography.Title level={2} style={{ marginBottom: 8 }}>
          Operations Inbox
        </Typography.Title>
        <Typography.Paragraph type="secondary" style={{ margin: 0 }}>
          승인 대기, 시세 경보, 사용자 지시 이벤트를 읽음 상태까지 포함해 운영자 기준으로 정리한 알림 대기함입니다.
        </Typography.Paragraph>
      </div>

      <Row gutter={[16, 16]}>
        <Col xs={24} md={8}>
          <Card>
            <Typography.Text type="secondary">Unread</Typography.Text>
            <Typography.Title level={3} style={{ margin: "12px 0 0" }}>
              {unreadCount}건
            </Typography.Title>
          </Card>
        </Col>
        <Col xs={24} md={8}>
          <Card>
            <Typography.Text type="secondary">Action Required</Typography.Text>
            <Typography.Title level={3} style={{ margin: "12px 0 0", color: actionRequiredCount > 0 ? "#cf1322" : undefined }}>
              {actionRequiredCount}건
            </Typography.Title>
          </Card>
        </Col>
        <Col xs={24} md={8}>
          <Card>
            <Typography.Text type="secondary">Today</Typography.Text>
            <Typography.Title level={3} style={{ margin: "12px 0 0" }}>
              {todayCount}건
            </Typography.Title>
          </Card>
        </Col>
      </Row>

      <Card title="Notification Queue">
        <OperationsGridTable
          rowKey="id"
          dataSource={notifications}
          pagination={false}
          columns={[
            {
              title: "Severity",
              width: 120,
              render: (_, record: Notification) => (
                <Tag color={resolveSeverityColor(record.severity)}>{record.severity}</Tag>
              ),
            },
            { title: "Category", dataIndex: "category", width: 140 },
            {
              title: "Notification",
              render: (_, record: Notification) => (
                <div>
                  <Typography.Text strong>{record.title}</Typography.Text>
                  <div>
                    <Typography.Text type="secondary">{record.message}</Typography.Text>
                  </div>
                </div>
              ),
            },
            {
              title: "Route",
              dataIndex: "actionPath",
              width: 160,
              render: (value: string) => <Typography.Text code>{value}</Typography.Text>,
            },
            {
              title: "Status",
              width: 110,
              render: (_, record: Notification) => (
                <Tag color={record.read ? "default" : "blue"}>{record.read ? "READ" : "UNREAD"}</Tag>
              ),
            },
            {
              title: "Created At",
              width: 180,
              render: (_, record: Notification) => new Date(record.createdAt).toLocaleString(),
            },
            {
              title: "Read At",
              width: 180,
              render: (_, record: Notification) => (record.readAt ? new Date(record.readAt).toLocaleString() : "-"),
            },
            {
              title: "Action",
              width: 130,
              render: (_, record: Notification) =>
                record.read ? (
                  <Typography.Text type="secondary">완료</Typography.Text>
                ) : (
                  <Button
                    size="small"
                    type="primary"
                    loading={readingNotificationId === record.id}
                    onClick={() => onRead(record.id)}
                  >
                    읽음 처리
                  </Button>
                ),
            },
          ]}
        />
      </Card>
    </Space>
  );
}

function resolveSeverityColor(severity: string) {
  if (severity === "ACTION_REQUIRED") {
    return "red";
  }
  if (severity === "WARNING") {
    return "gold";
  }
  if (severity === "SUCCESS") {
    return "green";
  }
  return "blue";
}

function isSameBusinessDay(dateTime: string) {
  const today = new Date();
  const target = new Date(dateTime);
  return today.getFullYear() === target.getFullYear()
    && today.getMonth() === target.getMonth()
    && today.getDate() === target.getDate();
}
