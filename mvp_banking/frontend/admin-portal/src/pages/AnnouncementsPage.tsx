import { Button, Card, Col, Form, Input, Row, Select, Space, Switch, Table, Tag, Typography } from "antd";
import type { Announcement, CreateAnnouncementPayload } from "../api";

type AnnouncementsPageProps = {
  announcements: Announcement[];
  creatingAnnouncement: boolean;
  actingAnnouncementId: string | null;
  onCreate: (payload: CreateAnnouncementPayload) => Promise<void>;
  onPublish: (announcementId: string) => Promise<void>;
  onArchive: (announcementId: string) => Promise<void>;
};

type AnnouncementFormValues = {
  title: string;
  summary: string;
  body: string;
  severity: string;
  audience: string;
  pinned: boolean;
  startsAt?: string;
  endsAt?: string;
};

export function AnnouncementsPage({
  announcements,
  creatingAnnouncement,
  actingAnnouncementId,
  onCreate,
  onPublish,
  onArchive,
}: AnnouncementsPageProps) {
  const [form] = Form.useForm<AnnouncementFormValues>();
  const publishedCount = announcements.filter((item) => item.status === "PUBLISHED").length;
  const draftCount = announcements.filter((item) => item.status === "DRAFT").length;

  async function handleSubmit(values: AnnouncementFormValues) {
    await onCreate({
      title: values.title,
      summary: values.summary,
      body: values.body,
      severity: values.severity,
      audience: values.audience,
      pinned: values.pinned ?? false,
      startsAt: toIsoOrNull(values.startsAt),
      endsAt: toIsoOrNull(values.endsAt),
    });
    form.resetFields();
    form.setFieldsValue({
      severity: "INFO",
      audience: "ALL",
      pinned: false,
    });
  }

  return (
    <Space direction="vertical" size={16} style={{ width: "100%" }}>
      <div>
        <Typography.Title level={2} style={{ marginBottom: 8 }}>
          Service Announcements
        </Typography.Title>
        <Typography.Paragraph type="secondary" style={{ margin: 0 }}>
          고객 공지, 서비스 배너, 운영자 전용 공지를 draft, published, archived 상태로 관리합니다.
        </Typography.Paragraph>
      </div>

      <Row gutter={[16, 16]}>
        <Col xs={24} md={8}>
          <Card>
            <Typography.Text type="secondary">Published</Typography.Text>
            <Typography.Title level={3} style={{ margin: "12px 0 0" }}>
              {publishedCount}건
            </Typography.Title>
          </Card>
        </Col>
        <Col xs={24} md={8}>
          <Card>
            <Typography.Text type="secondary">Draft</Typography.Text>
            <Typography.Title level={3} style={{ margin: "12px 0 0" }}>
              {draftCount}건
            </Typography.Title>
          </Card>
        </Col>
        <Col xs={24} md={8}>
          <Card>
            <Typography.Text type="secondary">Pinned</Typography.Text>
            <Typography.Title level={3} style={{ margin: "12px 0 0" }}>
              {announcements.filter((item) => item.pinned).length}건
            </Typography.Title>
          </Card>
        </Col>
      </Row>

      <Card title="Create Draft">
        <Form<AnnouncementFormValues>
          form={form}
          layout="vertical"
          initialValues={{
            severity: "INFO",
            audience: "ALL",
            pinned: false,
          }}
          onFinish={(values) => void handleSubmit(values)}
        >
          <Row gutter={16}>
            <Col xs={24} xl={12}>
              <Form.Item label="Title" name="title" rules={[{ required: true, message: "제목을 입력하세요." }]}>
                <Input placeholder="예: 해외주식 주문 점검 예정 안내" />
              </Form.Item>
            </Col>
            <Col xs={24} xl={12}>
              <Form.Item label="Summary" name="summary" rules={[{ required: true, message: "요약을 입력하세요." }]}>
                <Input placeholder="상단 배너와 목록에 노출될 짧은 설명" />
              </Form.Item>
            </Col>
            <Col xs={24} xl={8}>
              <Form.Item label="Severity" name="severity" rules={[{ required: true, message: "중요도를 선택하세요." }]}>
                <Select
                  options={[
                    { value: "INFO", label: "INFO" },
                    { value: "WARNING", label: "WARNING" },
                    { value: "CRITICAL", label: "CRITICAL" },
                  ]}
                />
              </Form.Item>
            </Col>
            <Col xs={24} xl={8}>
              <Form.Item label="Audience" name="audience" rules={[{ required: true, message: "대상을 선택하세요." }]}>
                <Select
                  options={[
                    { value: "ALL", label: "ALL" },
                    { value: "USER", label: "USER" },
                    { value: "ADMIN", label: "ADMIN" },
                  ]}
                />
              </Form.Item>
            </Col>
            <Col xs={24} xl={8}>
              <Form.Item label="Pinned" name="pinned" valuePropName="checked">
                <Switch checkedChildren="PIN" unCheckedChildren="NORMAL" />
              </Form.Item>
            </Col>
            <Col xs={24} xl={12}>
              <Form.Item label="Starts At" name="startsAt">
                <Input type="datetime-local" />
              </Form.Item>
            </Col>
            <Col xs={24} xl={12}>
              <Form.Item label="Ends At" name="endsAt">
                <Input type="datetime-local" />
              </Form.Item>
            </Col>
            <Col span={24}>
              <Form.Item label="Body" name="body" rules={[{ required: true, message: "본문을 입력하세요." }]}>
                <Input.TextArea rows={4} placeholder="서비스 점검 시간, 영향 범위, 고객 안내 문구를 입력하세요." />
              </Form.Item>
            </Col>
          </Row>
          <Button type="primary" htmlType="submit" loading={creatingAnnouncement}>
            Draft 생성
          </Button>
        </Form>
      </Card>

      <Card title="Announcement Queue">
        <Table
          rowKey="id"
          dataSource={announcements}
          pagination={false}
          columns={[
            {
              title: "Priority",
              width: 110,
              render: (_, record: Announcement) => (
                <Tag color={resolveSeverityColor(record.severity)}>{record.severity}</Tag>
              ),
            },
            {
              title: "Announcement",
              render: (_, record: Announcement) => (
                <div>
                  <Typography.Text strong>{record.title}</Typography.Text>
                  <div>
                    <Typography.Text type="secondary">{record.summary}</Typography.Text>
                  </div>
                </div>
              ),
            },
            { title: "Audience", dataIndex: "audience", width: 100 },
            {
              title: "Status",
              width: 120,
              render: (_, record: Announcement) => (
                <Tag color={resolveStatusColor(record.status)}>{record.status}</Tag>
              ),
            },
            {
              title: "Schedule",
              width: 220,
              render: (_, record: Announcement) => (
                <div>
                  <div>{record.startsAt ? new Date(record.startsAt).toLocaleString() : "즉시 노출"}</div>
                  <Typography.Text type="secondary">
                    {record.endsAt ? `~ ${new Date(record.endsAt).toLocaleString()}` : "~ 종료 시까지"}
                  </Typography.Text>
                </div>
              ),
            },
            {
              title: "Meta",
              width: 180,
              render: (_, record: Announcement) => (
                <div>
                  <div>{record.pinned ? "Pinned" : "Normal"}</div>
                  <Typography.Text type="secondary">{record.createdByEmail}</Typography.Text>
                </div>
              ),
            },
            {
              title: "Action",
              width: 160,
              render: (_, record: Announcement) => (
                <Space>
                  {record.status === "DRAFT" ? (
                    <Button
                      size="small"
                      type="primary"
                      loading={actingAnnouncementId === record.id}
                      onClick={() => void onPublish(record.id)}
                    >
                      Publish
                    </Button>
                  ) : null}
                  {record.status !== "ARCHIVED" ? (
                    <Button
                      size="small"
                      danger
                      loading={actingAnnouncementId === record.id}
                      onClick={() => void onArchive(record.id)}
                    >
                      Archive
                    </Button>
                  ) : null}
                </Space>
              ),
            },
          ]}
        />
      </Card>
    </Space>
  );
}

function resolveSeverityColor(severity: string) {
  if (severity === "CRITICAL") {
    return "red";
  }
  if (severity === "WARNING") {
    return "gold";
  }
  return "blue";
}

function resolveStatusColor(status: string) {
  if (status === "PUBLISHED") {
    return "green";
  }
  if (status === "ARCHIVED") {
    return "default";
  }
  return "processing";
}

function toIsoOrNull(value?: string) {
  if (!value) {
    return null;
  }
  return new Date(value).toISOString();
}
