import { Button, Card, Col, Form, Input, Modal, Row, Select, Space, Switch, Tag, Typography } from "antd";
import { useState } from "react";
import type { Announcement, CreateAnnouncementPayload } from "../api";
import { OperationsGridTable } from "../components/OperationsGridTable";

type AnnouncementsPageProps = {
  announcements: Announcement[];
  creatingAnnouncement: boolean;
  actingAnnouncementId: string | null;
  onCreate: (payload: CreateAnnouncementPayload) => Promise<void>;
  onUpdate: (announcementId: string, payload: CreateAnnouncementPayload) => Promise<void>;
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
  onUpdate,
  onPublish,
  onArchive,
}: AnnouncementsPageProps) {
  const [form] = Form.useForm<AnnouncementFormValues>();
  const [editorMode, setEditorMode] = useState<"create" | "update" | null>(null);
  const [editingAnnouncement, setEditingAnnouncement] = useState<Announcement | null>(null);
  const publishedCount = announcements.filter((item) => item.status === "PUBLISHED").length;
  const draftCount = announcements.filter((item) => item.status === "DRAFT").length;
  const criticalCount = announcements.filter((item) => item.severity === "CRITICAL" && item.status !== "ARCHIVED").length;
  const modalOpen = editorMode !== null;
  const modalSubmitting = editorMode === "create"
    ? creatingAnnouncement
    : editingAnnouncement !== null && actingAnnouncementId === editingAnnouncement.id;

  async function handleSubmit(values: AnnouncementFormValues) {
    const payload = {
      title: values.title,
      summary: values.summary,
      body: values.body,
      severity: values.severity,
      audience: values.audience,
      pinned: values.pinned ?? false,
      startsAt: toIsoOrNull(values.startsAt),
      endsAt: toIsoOrNull(values.endsAt),
    };

    if (editorMode === "update" && editingAnnouncement) {
      await onUpdate(editingAnnouncement.id, payload);
    } else {
      await onCreate(payload);
    }
    closeEditor();
  }

  function openCreateModal() {
    setEditorMode("create");
    setEditingAnnouncement(null);
    form.resetFields();
    form.setFieldsValue({
      severity: "INFO",
      audience: "ALL",
      pinned: false,
    });
  }

  function openUpdateModal(announcement: Announcement) {
    setEditorMode("update");
    setEditingAnnouncement(announcement);
    form.setFieldsValue({
      title: announcement.title,
      summary: announcement.summary,
      body: announcement.body,
      severity: announcement.severity,
      audience: announcement.audience,
      pinned: announcement.pinned,
      startsAt: toDateTimeLocal(announcement.startsAt),
      endsAt: toDateTimeLocal(announcement.endsAt),
    });
  }

  function closeEditor() {
    setEditorMode(null);
    setEditingAnnouncement(null);
    form.resetFields();
  }

  return (
    <Space direction="vertical" size={16} style={{ width: "100%" }}>
      <Card className="ops-hero-card" bordered={false}>
        <div className="ops-page-header">
          <div>
            <p className="eyebrow">Operations Communication</p>
            <Typography.Title level={2} style={{ marginBottom: 8 }}>
              Service Announcements
            </Typography.Title>
            <Typography.Paragraph type="secondary" style={{ margin: 0, maxWidth: 720 }}>
              고객 공지, 운영 배너, 내부 알림을 게시 상태와 노출 일정을 기준으로 통제합니다. 작성은 모달에서 처리하고,
              운영 큐는 테이블에서 바로 게시/종료합니다.
            </Typography.Paragraph>
          </div>
          <Space>
            <Button type="primary" size="large" onClick={openCreateModal}>
              Create Draft
            </Button>
          </Space>
        </div>
      </Card>

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
            <Typography.Text type="secondary">Critical Live</Typography.Text>
            <Typography.Title level={3} style={{ margin: "12px 0 0" }}>
              {criticalCount}건
            </Typography.Title>
            <Typography.Paragraph type="secondary" style={{ marginBottom: 0 }}>
              중요 공지와 서비스 영향 배너
            </Typography.Paragraph>
          </Card>
        </Col>
      </Row>

      <Card
        title="Announcement Queue"
        extra={<Typography.Text type="secondary">초안 작성과 수정은 모달에서 처리합니다.</Typography.Text>}
      >
        <OperationsGridTable
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
                <div className="ops-primary-cell">
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
              width: 260,
              render: (_, record: Announcement) => (
                <Space>
                  {record.status === "DRAFT" ? (
                    <Button size="small" onClick={() => openUpdateModal(record)}>
                      Edit
                    </Button>
                  ) : null}
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

      <Modal
        open={modalOpen}
        destroyOnHidden
        width={820}
        onCancel={closeEditor}
        onOk={() => void form.submit()}
        confirmLoading={modalSubmitting}
        okText={editorMode === "update" ? "Update Draft" : "Create Draft"}
        title={editorMode === "update" ? "Edit Announcement Draft" : "Create Announcement Draft"}
      >
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
                <Input.TextArea rows={8} placeholder="서비스 점검 시간, 영향 범위, 고객 안내 문구를 입력하세요." />
              </Form.Item>
            </Col>
          </Row>
        </Form>
      </Modal>
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

function toDateTimeLocal(value: string | null) {
  if (!value) {
    return undefined;
  }
  const date = new Date(value);
  const timezoneOffset = date.getTimezoneOffset() * 60000;
  return new Date(date.getTime() - timezoneOffset).toISOString().slice(0, 16);
}
