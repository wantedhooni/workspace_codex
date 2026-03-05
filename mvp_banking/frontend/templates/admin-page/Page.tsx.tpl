import { Card, Table, Tag, Typography } from "antd";

type __DOMAIN_PASCAL__Row = {
  id: string;
  code: string;
  name: string;
  status: string;
  createdAt: string;
};

type __DOMAIN_PASCAL__PageProps = {
  items?: __DOMAIN_PASCAL__Row[];
};

export function __DOMAIN_PASCAL__Page({ items = [] }: __DOMAIN_PASCAL__PageProps) {
  return (
    <>
      <div>
        <Typography.Title level={2} style={{ marginBottom: 8 }}>
          __DOMAIN_TITLE__ Operations
        </Typography.Title>
        <Typography.Paragraph type="secondary" style={{ margin: 0 }}>
          __DOMAIN_TITLE__ 도메인의 운영 상태와 주요 레코드를 확인하는 템플릿 페이지입니다.
        </Typography.Paragraph>
      </div>

      <Card title="__DOMAIN_TITLE__ Queue" style={{ marginTop: 16 }}>
        <Table
          rowKey="id"
          dataSource={items}
          pagination={false}
          columns={[
            {
              title: "Code",
              dataIndex: "code",
              key: "code",
            },
            {
              title: "Name",
              dataIndex: "name",
              key: "name",
            },
            {
              title: "Status",
              key: "status",
              render: (_, record: __DOMAIN_PASCAL__Row) => (
                <Tag color={record.status === "ACTIVE" ? "green" : "default"}>{record.status}</Tag>
              ),
            },
            {
              title: "Created At",
              key: "createdAt",
              render: (_, record: __DOMAIN_PASCAL__Row) => new Date(record.createdAt).toLocaleString(),
            },
          ]}
        />
      </Card>
    </>
  );
}
