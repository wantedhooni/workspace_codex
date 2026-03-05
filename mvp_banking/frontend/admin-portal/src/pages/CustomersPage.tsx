import { Button, Card, Col, Input, Row, Select, Space, Statistic, Tag, Typography } from "antd";
import type { Dispatch, SetStateAction } from "react";
import type { Customer, CustomerStatusSummary, PageResponse } from "../api";
import { OperationsGridTable } from "../components/OperationsGridTable";

type CustomerFilter = {
  query: string;
  status?: string;
  createdFrom?: string;
  createdTo?: string;
  sortBy: string;
  sortDir: string;
  page: number;
  size: number;
};

type CustomersPageProps = {
  summary: CustomerStatusSummary;
  data: PageResponse<Customer>;
  filter: CustomerFilter;
  setFilter: Dispatch<SetStateAction<CustomerFilter>>;
  onSearch: () => void;
  onReset: () => void;
  onPageChange: (page: number, size: number) => void;
};

export function CustomersPage({ summary, data, filter, setFilter, onSearch, onReset, onPageChange }: CustomersPageProps) {
  return (
    <Card title="Customers" extra={<Typography.Text type="secondary">총 {data.totalElements}건</Typography.Text>}>
      <Row gutter={[16, 16]} className="customer-summary-grid">
        <Col xs={24} md={6}>
          <Card bordered={false} className="customer-summary-card">
            <Statistic title="Visible Base" value={summary.total} />
          </Card>
        </Col>
        <Col xs={24} md={6}>
          <Card bordered={false} className="customer-summary-card">
            <Statistic title="ACTIVE" value={summary.active} valueStyle={{ color: "#157347" }} />
          </Card>
        </Col>
        <Col xs={24} md={6}>
          <Card bordered={false} className="customer-summary-card">
            <Statistic title="REVIEW_REQUIRED" value={summary.reviewRequired} valueStyle={{ color: "#9a6700" }} />
          </Card>
        </Col>
        <Col xs={24} md={6}>
          <Card bordered={false} className="customer-summary-card">
            <Statistic title="SUSPENDED" value={summary.suspended} valueStyle={{ color: "#b42318" }} />
          </Card>
        </Col>
      </Row>
      <Space wrap className="filter-row">
        <Input
          placeholder="고객번호 / 이름 / 이메일 검색"
          value={filter.query}
          onChange={(event) => setFilter((current) => ({ ...current, query: event.target.value, page: 0 }))}
          onPressEnter={onSearch}
        />
        <Select
          allowClear
          placeholder="상태"
          style={{ width: 160 }}
          value={filter.status}
          onChange={(value) => setFilter((current) => ({ ...current, status: value, page: 0 }))}
          options={[
            { label: "ACTIVE", value: "ACTIVE" },
            { label: "REVIEW_REQUIRED", value: "REVIEW_REQUIRED" },
            { label: "SUSPENDED", value: "SUSPENDED" },
          ]}
        />
        <Input
          type="date"
          placeholder="생성 시작일"
          value={filter.createdFrom}
          onChange={(event) => setFilter((current) => ({ ...current, createdFrom: event.target.value, page: 0 }))}
          style={{ width: 160 }}
        />
        <Input
          type="date"
          placeholder="생성 종료일"
          value={filter.createdTo}
          onChange={(event) => setFilter((current) => ({ ...current, createdTo: event.target.value, page: 0 }))}
          style={{ width: 160 }}
        />
        <Select
          placeholder="정렬 기준"
          style={{ width: 160 }}
          value={filter.sortBy}
          onChange={(value) => setFilter((current) => ({ ...current, sortBy: value, page: 0 }))}
          options={[
            { label: "최근 생성순", value: "createdAt" },
            { label: "고객번호", value: "customerNumber" },
            { label: "이름", value: "fullName" },
            { label: "이메일", value: "email" },
          ]}
        />
        <Select
          placeholder="정렬 방향"
          style={{ width: 140 }}
          value={filter.sortDir}
          onChange={(value) => setFilter((current) => ({ ...current, sortDir: value, page: 0 }))}
          options={[
            { label: "내림차순", value: "desc" },
            { label: "오름차순", value: "asc" },
          ]}
        />
        <Button type="primary" onClick={onSearch}>Search</Button>
        <Button onClick={onReset}>Reset</Button>
      </Space>
      <OperationsGridTable
        rowKey="id"
        dataSource={data.items}
        pagination={{
          current: data.page + 1,
          pageSize: data.size,
          total: data.totalElements,
          onChange: (page, size) => onPageChange(page, size),
        }}
        columns={[
          { title: "Customer No", dataIndex: "customerNumber" },
          { title: "Name", dataIndex: "fullName" },
          { title: "Email", dataIndex: "email" },
          { title: "Status", dataIndex: "status", render: (value: string) => <Tag color="blue">{value}</Tag> },
          { title: "Created At", render: (_, record: Customer) => new Date(record.createdAt).toLocaleDateString() },
        ]}
      />
    </Card>
  );
}
