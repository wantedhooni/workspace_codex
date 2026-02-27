import { Create, DeleteButton, Edit, EditButton, List, useForm, useSelect, useTable } from "@refinedev/antd";
import { Form, Input, InputNumber, Select, Table } from "antd";
import { useRef } from "react";
import { ListFieldFilters, ListFieldFiltersHandle, listFilterHeaderButtons } from "../components/ListFieldFilters";
import { toCrudFilters } from "../utils/filters";

const { TextArea } = Input;

const yesNoOptions = [
  { label: "Enabled", value: true },
  { label: "Disabled", value: false },
];

const httpMethodOptions = ["GET", "POST", "PUT", "PATCH", "DELETE"].map((method) => ({
  label: method,
  value: method,
}));

const ActionButtons = ({ id }: { id: number }) => (
  <>
    <EditButton size="small" recordItemId={id} />
    <DeleteButton size="small" recordItemId={id} />
  </>
);

export const CommonCodeList = () => {
  const { tableProps, setFilters } = useTable();
  const filterRef = useRef<ListFieldFiltersHandle>(null);
  const onSearch = (values: Record<string, unknown>) => {
    setFilters(toCrudFilters(values), "replace");
  };

  return (
    <List title="Common Codes" headerButtons={listFilterHeaderButtons(filterRef)}>
      <ListFieldFilters
        ref={filterRef}
        fields={[
          { name: "groupCode", label: "Group Code", placeholder: "Group Code" },
          { name: "code", label: "Code", placeholder: "Code" },
          { name: "name", label: "Name", placeholder: "Name" },
          { name: "enabled", label: "Enabled", placeholder: "Enabled", type: "select", options: yesNoOptions },
        ]}
        onSearch={onSearch}
      />
      <Table {...tableProps} rowKey="id">
        <Table.Column dataIndex="id" title="ID" />
        <Table.Column dataIndex="groupCode" title="Group" />
        <Table.Column dataIndex="code" title="Code" />
        <Table.Column dataIndex="name" title="Name" />
        <Table.Column dataIndex="sortOrder" title="Order" />
        <Table.Column dataIndex="enabled" title="Enabled" render={(value: boolean) => (value ? "Y" : "N")} />
        <Table.Column title="Actions" render={(_, record: { id: number }) => <ActionButtons id={record.id} />} />
      </Table>
    </List>
  );
};

export const CommonCodeCreate = () => {
  const { formProps, saveButtonProps } = useForm();
  return (
    <Create title="Create Common Code" saveButtonProps={saveButtonProps}>
      <Form {...formProps} layout="vertical">
        <Form.Item label="Group Code" name="groupCode" rules={[{ required: true }]}> 
          <Input />
        </Form.Item>
        <Form.Item label="Code" name="code" rules={[{ required: true }]}> 
          <Input />
        </Form.Item>
        <Form.Item label="Name" name="name" rules={[{ required: true }]}> 
          <Input />
        </Form.Item>
        <Form.Item label="Description" name="description">
          <Input />
        </Form.Item>
        <Form.Item label="Sort Order" name="sortOrder">
          <InputNumber min={0} style={{ width: "100%" }} />
        </Form.Item>
        <Form.Item label="Enabled" name="enabled">
          <Select options={yesNoOptions} />
        </Form.Item>
      </Form>
    </Create>
  );
};

export const CommonCodeEdit = () => {
  const { formProps, saveButtonProps } = useForm();
  return (
    <Edit title="Edit Common Code" saveButtonProps={saveButtonProps}>
      <Form {...formProps} layout="vertical">
        <Form.Item label="Group Code" name="groupCode">
          <Input />
        </Form.Item>
        <Form.Item label="Code" name="code">
          <Input />
        </Form.Item>
        <Form.Item label="Name" name="name">
          <Input />
        </Form.Item>
        <Form.Item label="Description" name="description">
          <Input />
        </Form.Item>
        <Form.Item label="Sort Order" name="sortOrder">
          <InputNumber min={0} style={{ width: "100%" }} />
        </Form.Item>
        <Form.Item label="Enabled" name="enabled">
          <Select options={yesNoOptions} />
        </Form.Item>
      </Form>
    </Edit>
  );
};

export const ProgramList = () => {
  const { tableProps, setFilters } = useTable();
  const filterRef = useRef<ListFieldFiltersHandle>(null);
  const onSearch = (values: Record<string, unknown>) => {
    setFilters(toCrudFilters(values), "replace");
  };

  return (
    <List title="Programs" headerButtons={listFilterHeaderButtons(filterRef)}>
      <ListFieldFilters
        ref={filterRef}
        fields={[
          { name: "name", label: "Name", placeholder: "Name" },
          { name: "url", label: "URL", placeholder: "URL", width: 220 },
          { name: "httpMethod", label: "HTTP Method", placeholder: "HTTP Method", type: "select", options: httpMethodOptions },
          { name: "enabled", label: "Enabled", placeholder: "Enabled", type: "select", options: yesNoOptions },
        ]}
        onSearch={onSearch}
      />
      <Table {...tableProps} rowKey="id">
        <Table.Column dataIndex="id" title="ID" />
        <Table.Column dataIndex="name" title="Name" />
        <Table.Column dataIndex="url" title="URL" />
        <Table.Column dataIndex="httpMethod" title="Method" />
        <Table.Column dataIndex="enabled" title="Enabled" render={(value: boolean) => (value ? "Y" : "N")} />
        <Table.Column title="Actions" render={(_, record: { id: number }) => <ActionButtons id={record.id} />} />
      </Table>
    </List>
  );
};

export const ProgramCreate = () => {
  const { formProps, saveButtonProps } = useForm();
  return (
    <Create title="Create Program" saveButtonProps={saveButtonProps}>
      <Form {...formProps} layout="vertical">
        <Form.Item label="Name" name="name" rules={[{ required: true }]}> 
          <Input />
        </Form.Item>
        <Form.Item label="URL" name="url" rules={[{ required: true }]}> 
          <Input />
        </Form.Item>
        <Form.Item label="HTTP Method" name="httpMethod" rules={[{ required: true }]}> 
          <Select options={httpMethodOptions} />
        </Form.Item>
        <Form.Item label="Description" name="description">
          <Input />
        </Form.Item>
        <Form.Item label="Enabled" name="enabled">
          <Select options={yesNoOptions} />
        </Form.Item>
      </Form>
    </Create>
  );
};

export const ProgramEdit = () => {
  const { formProps, saveButtonProps } = useForm();
  return (
    <Edit title="Edit Program" saveButtonProps={saveButtonProps}>
      <Form {...formProps} layout="vertical">
        <Form.Item label="Name" name="name">
          <Input />
        </Form.Item>
        <Form.Item label="URL" name="url">
          <Input />
        </Form.Item>
        <Form.Item label="HTTP Method" name="httpMethod">
          <Select options={httpMethodOptions} />
        </Form.Item>
        <Form.Item label="Description" name="description">
          <Input />
        </Form.Item>
        <Form.Item label="Enabled" name="enabled">
          <Select options={yesNoOptions} />
        </Form.Item>
      </Form>
    </Edit>
  );
};

export const RoleRouteList = () => {
  const { tableProps, setFilters } = useTable();
  const filterRef = useRef<ListFieldFiltersHandle>(null);
  const onSearch = (values: Record<string, unknown>) => {
    setFilters(toCrudFilters(values), "replace");
  };

  return (
    <List title="Role Routes" headerButtons={listFilterHeaderButtons(filterRef)}>
      <ListFieldFilters
        ref={filterRef}
        fields={[
          { name: "roleId", label: "Role ID", placeholder: "Role ID", type: "number", width: 140 },
          { name: "pattern", label: "Pattern", placeholder: "Pattern", width: 220 },
          { name: "httpMethod", label: "HTTP Method", placeholder: "HTTP Method", type: "select", options: httpMethodOptions },
          { name: "enabled", label: "Enabled", placeholder: "Enabled", type: "select", options: yesNoOptions },
        ]}
        onSearch={onSearch}
      />
      <Table {...tableProps} rowKey="id">
        <Table.Column dataIndex="id" title="ID" />
        <Table.Column dataIndex="roleId" title="Role ID" />
        <Table.Column dataIndex="pattern" title="Pattern" />
        <Table.Column dataIndex="httpMethod" title="Method" />
        <Table.Column dataIndex="enabled" title="Enabled" render={(value: boolean) => (value ? "Y" : "N")} />
        <Table.Column title="Actions" render={(_, record: { id: number }) => <ActionButtons id={record.id} />} />
      </Table>
    </List>
  );
};

export const RoleRouteCreate = () => {
  const { formProps, saveButtonProps } = useForm();
  const { selectProps } = useSelect({ resource: "roles", optionLabel: "name" });
  return (
    <Create title="Create Role Route" saveButtonProps={saveButtonProps}>
      <Form {...formProps} layout="vertical">
        <Form.Item label="Role" name="roleId" rules={[{ required: true }]}> 
          <Select {...selectProps} />
        </Form.Item>
        <Form.Item label="Pattern" name="pattern" rules={[{ required: true }]}> 
          <Input />
        </Form.Item>
        <Form.Item label="HTTP Method" name="httpMethod" rules={[{ required: true }]}> 
          <Select options={httpMethodOptions} />
        </Form.Item>
        <Form.Item label="Description" name="description">
          <Input />
        </Form.Item>
        <Form.Item label="Enabled" name="enabled">
          <Select options={yesNoOptions} />
        </Form.Item>
      </Form>
    </Create>
  );
};

export const RoleRouteEdit = () => {
  const { formProps, saveButtonProps } = useForm();
  const { selectProps } = useSelect({ resource: "roles", optionLabel: "name" });
  return (
    <Edit title="Edit Role Route" saveButtonProps={saveButtonProps}>
      <Form {...formProps} layout="vertical">
        <Form.Item label="Role" name="roleId">
          <Select {...selectProps} />
        </Form.Item>
        <Form.Item label="Pattern" name="pattern">
          <Input />
        </Form.Item>
        <Form.Item label="HTTP Method" name="httpMethod">
          <Select options={httpMethodOptions} />
        </Form.Item>
        <Form.Item label="Description" name="description">
          <Input />
        </Form.Item>
        <Form.Item label="Enabled" name="enabled">
          <Select options={yesNoOptions} />
        </Form.Item>
      </Form>
    </Edit>
  );
};

export const AccessLogList = () => {
  const { tableProps, setFilters } = useTable({
    sorters: {
      initial: [{ field: "id", order: "desc" }],
    },
  });
  const filterRef = useRef<ListFieldFiltersHandle>(null);
  const onSearch = (values: Record<string, unknown>) => {
    setFilters(toCrudFilters(values), "replace");
  };

  return (
    <List title="Access Logs" headerButtons={listFilterHeaderButtons(filterRef)}>
      <ListFieldFilters
        ref={filterRef}
        fields={[
          { name: "username", label: "Username", placeholder: "Username" },
          { name: "ipAddress", label: "IP Address", placeholder: "IP Address" },
          { name: "action", label: "Action", placeholder: "Action" },
          { name: "path", label: "Path", placeholder: "Path", width: 220 },
          { name: "success", label: "Success", placeholder: "Success", type: "select", options: yesNoOptions },
        ]}
        onSearch={onSearch}
      />
      <Table {...tableProps} rowKey="id">
        <Table.Column dataIndex="id" title="ID" />
        <Table.Column dataIndex="username" title="Username" />
        <Table.Column dataIndex="ipAddress" title="IP" />
        <Table.Column dataIndex="action" title="Action" />
        <Table.Column dataIndex="path" title="Path" ellipsis />
        <Table.Column dataIndex="success" title="Success" render={(value: boolean) => (value ? "Y" : "N")} />
        <Table.Column dataIndex="loggedAt" title="Logged At" />
      </Table>
    </List>
  );
};

export const AccessLogCreate = () => {
  const { formProps, saveButtonProps } = useForm();
  return (
    <Create title="Create Access Log" saveButtonProps={saveButtonProps}>
      <Form {...formProps} layout="vertical">
        <Form.Item label="Username" name="username" rules={[{ required: true }]}> 
          <Input />
        </Form.Item>
        <Form.Item label="IP Address" name="ipAddress" rules={[{ required: true }]}> 
          <Input />
        </Form.Item>
        <Form.Item label="Action" name="action" rules={[{ required: true }]}> 
          <Input />
        </Form.Item>
        <Form.Item label="Path" name="path" rules={[{ required: true }]}> 
          <Input />
        </Form.Item>
        <Form.Item label="Success" name="success" rules={[{ required: true }]}> 
          <Select options={yesNoOptions} />
        </Form.Item>
      </Form>
    </Create>
  );
};

export const AccessLogEdit = () => {
  const { formProps, saveButtonProps } = useForm();
  return (
    <Edit title="Edit Access Log" saveButtonProps={saveButtonProps}>
      <Form {...formProps} layout="vertical">
        <Form.Item label="Username" name="username">
          <Input />
        </Form.Item>
        <Form.Item label="IP Address" name="ipAddress">
          <Input />
        </Form.Item>
        <Form.Item label="Action" name="action">
          <Input />
        </Form.Item>
        <Form.Item label="Path" name="path">
          <Input />
        </Form.Item>
        <Form.Item label="Success" name="success">
          <Select options={yesNoOptions} />
        </Form.Item>
      </Form>
    </Edit>
  );
};

export const ServiceAuditLogList = () => {
  const { tableProps, setFilters } = useTable({
    sorters: {
      initial: [{ field: "id", order: "desc" }],
    },
  });
  const filterRef = useRef<ListFieldFiltersHandle>(null);
  const onSearch = (values: Record<string, unknown>) => {
    setFilters(toCrudFilters(values), "replace");
  };

  return (
    <List title="Service Audit Logs" headerButtons={listFilterHeaderButtons(filterRef)}>
      <ListFieldFilters
        ref={filterRef}
        fields={[
          { name: "domainType", label: "Domain Type", placeholder: "Domain Type" },
          { name: "domainId", label: "Domain ID", placeholder: "Domain ID", type: "number", width: 140 },
          { name: "action", label: "Action", placeholder: "Action" },
          { name: "username", label: "Username", placeholder: "Username" },
          { name: "detail", label: "Detail", placeholder: "Detail", width: 220 },
        ]}
        onSearch={onSearch}
      />
      <Table {...tableProps} rowKey="id">
        <Table.Column dataIndex="id" title="ID" />
        <Table.Column dataIndex="domainType" title="Domain" />
        <Table.Column dataIndex="domainId" title="Domain ID" />
        <Table.Column dataIndex="action" title="Action" />
        <Table.Column dataIndex="username" title="Username" />
        <Table.Column dataIndex="detail" title="Detail" ellipsis />
        <Table.Column dataIndex="loggedAt" title="Logged At" />
      </Table>
    </List>
  );
};

export const ServiceAuditLogCreate = () => {
  const { formProps, saveButtonProps } = useForm();
  return (
    <Create title="Create Service Audit Log" saveButtonProps={saveButtonProps}>
      <Form {...formProps} layout="vertical">
        <Form.Item label="Domain Type" name="domainType" rules={[{ required: true }]}> 
          <Input />
        </Form.Item>
        <Form.Item label="Domain ID" name="domainId" rules={[{ required: true }]}> 
          <InputNumber min={1} style={{ width: "100%" }} />
        </Form.Item>
        <Form.Item label="Action" name="action" rules={[{ required: true }]}> 
          <Input />
        </Form.Item>
        <Form.Item label="Username" name="username" rules={[{ required: true }]}> 
          <Input />
        </Form.Item>
        <Form.Item label="Detail" name="detail">
          <TextArea rows={4} />
        </Form.Item>
      </Form>
    </Create>
  );
};

export const ServiceAuditLogEdit = () => {
  const { formProps, saveButtonProps } = useForm();
  return (
    <Edit title="Edit Service Audit Log" saveButtonProps={saveButtonProps}>
      <Form {...formProps} layout="vertical">
        <Form.Item label="Domain Type" name="domainType">
          <Input />
        </Form.Item>
        <Form.Item label="Domain ID" name="domainId">
          <InputNumber min={1} style={{ width: "100%" }} />
        </Form.Item>
        <Form.Item label="Action" name="action">
          <Input />
        </Form.Item>
        <Form.Item label="Username" name="username">
          <Input />
        </Form.Item>
        <Form.Item label="Detail" name="detail">
          <TextArea rows={4} />
        </Form.Item>
      </Form>
    </Edit>
  );
};

export const BatchJobList = () => {
  const { tableProps, setFilters } = useTable();
  const filterRef = useRef<ListFieldFiltersHandle>(null);
  const onSearch = (values: Record<string, unknown>) => {
    setFilters(toCrudFilters(values), "replace");
  };

  return (
    <List title="Batch Jobs" headerButtons={listFilterHeaderButtons(filterRef)}>
      <ListFieldFilters
        ref={filterRef}
        fields={[
          { name: "name", label: "Name", placeholder: "Name" },
          { name: "jobKey", label: "Job Key", placeholder: "Job Key" },
          { name: "enabled", label: "Enabled", placeholder: "Enabled", type: "select", options: yesNoOptions },
        ]}
        onSearch={onSearch}
      />
      <Table {...tableProps} rowKey="id">
        <Table.Column dataIndex="id" title="ID" />
        <Table.Column dataIndex="name" title="Name" />
        <Table.Column dataIndex="jobKey" title="Job Key" />
        <Table.Column dataIndex="enabled" title="Enabled" render={(value: boolean) => (value ? "Y" : "N")} />
        <Table.Column title="Actions" render={(_, record: { id: number }) => <ActionButtons id={record.id} />} />
      </Table>
    </List>
  );
};

export const BatchJobCreate = () => {
  const { formProps, saveButtonProps } = useForm();
  return (
    <Create title="Create Batch Job" saveButtonProps={saveButtonProps}>
      <Form {...formProps} layout="vertical">
        <Form.Item label="Name" name="name" rules={[{ required: true }]}> 
          <Input />
        </Form.Item>
        <Form.Item label="Job Key" name="jobKey" rules={[{ required: true }]}> 
          <Input />
        </Form.Item>
        <Form.Item label="Description" name="description">
          <Input />
        </Form.Item>
        <Form.Item label="Enabled" name="enabled">
          <Select options={yesNoOptions} />
        </Form.Item>
      </Form>
    </Create>
  );
};

export const BatchJobEdit = () => {
  const { formProps, saveButtonProps } = useForm();
  return (
    <Edit title="Edit Batch Job" saveButtonProps={saveButtonProps}>
      <Form {...formProps} layout="vertical">
        <Form.Item label="Name" name="name">
          <Input />
        </Form.Item>
        <Form.Item label="Job Key" name="jobKey">
          <Input />
        </Form.Item>
        <Form.Item label="Description" name="description">
          <Input />
        </Form.Item>
        <Form.Item label="Enabled" name="enabled">
          <Select options={yesNoOptions} />
        </Form.Item>
      </Form>
    </Edit>
  );
};

export const BatchScheduleList = () => {
  const { tableProps, setFilters } = useTable();
  const filterRef = useRef<ListFieldFiltersHandle>(null);
  const onSearch = (values: Record<string, unknown>) => {
    setFilters(toCrudFilters(values), "replace");
  };

  return (
    <List title="Batch Schedules" headerButtons={listFilterHeaderButtons(filterRef)}>
      <ListFieldFilters
        ref={filterRef}
        fields={[
          { name: "batchJobId", label: "Batch Job ID", placeholder: "Batch Job ID", type: "number", width: 140 },
          { name: "cronExpression", label: "Cron Expression", placeholder: "Cron Expression", width: 220 },
          { name: "timezone", label: "Timezone", placeholder: "Timezone" },
          { name: "lastStatus", label: "Last Status", placeholder: "Last Status" },
          { name: "enabled", label: "Enabled", placeholder: "Enabled", type: "select", options: yesNoOptions },
        ]}
        onSearch={onSearch}
      />
      <Table {...tableProps} rowKey="id">
        <Table.Column dataIndex="id" title="ID" />
        <Table.Column dataIndex="batchJobId" title="Batch Job ID" />
        <Table.Column dataIndex="cronExpression" title="Cron" />
        <Table.Column dataIndex="timezone" title="Timezone" />
        <Table.Column dataIndex="lastStatus" title="Last Status" />
        <Table.Column dataIndex="enabled" title="Enabled" render={(value: boolean) => (value ? "Y" : "N")} />
        <Table.Column title="Actions" render={(_, record: { id: number }) => <ActionButtons id={record.id} />} />
      </Table>
    </List>
  );
};

export const BatchScheduleCreate = () => {
  const { formProps, saveButtonProps } = useForm();
  const { selectProps } = useSelect({ resource: "batch-jobs", optionLabel: "jobKey" });
  return (
    <Create title="Create Batch Schedule" saveButtonProps={saveButtonProps}>
      <Form {...formProps} layout="vertical">
        <Form.Item label="Batch Job" name="batchJobId" rules={[{ required: true }]}> 
          <Select {...selectProps} />
        </Form.Item>
        <Form.Item label="Cron Expression" name="cronExpression" rules={[{ required: true }]}> 
          <Input />
        </Form.Item>
        <Form.Item label="Timezone" name="timezone" rules={[{ required: true }]}> 
          <Input />
        </Form.Item>
        <Form.Item label="Enabled" name="enabled">
          <Select options={yesNoOptions} />
        </Form.Item>
        <Form.Item label="Last Status" name="lastStatus">
          <Input />
        </Form.Item>
        <Form.Item label="Last Run At" name="lastRunAt">
          <Input />
        </Form.Item>
      </Form>
    </Create>
  );
};

export const BatchScheduleEdit = () => {
  const { formProps, saveButtonProps } = useForm();
  const { selectProps } = useSelect({ resource: "batch-jobs", optionLabel: "jobKey" });
  return (
    <Edit title="Edit Batch Schedule" saveButtonProps={saveButtonProps}>
      <Form {...formProps} layout="vertical">
        <Form.Item label="Batch Job" name="batchJobId">
          <Select {...selectProps} />
        </Form.Item>
        <Form.Item label="Cron Expression" name="cronExpression">
          <Input />
        </Form.Item>
        <Form.Item label="Timezone" name="timezone">
          <Input />
        </Form.Item>
        <Form.Item label="Enabled" name="enabled">
          <Select options={yesNoOptions} />
        </Form.Item>
        <Form.Item label="Last Status" name="lastStatus">
          <Input />
        </Form.Item>
        <Form.Item label="Last Run At" name="lastRunAt">
          <Input />
        </Form.Item>
      </Form>
    </Edit>
  );
};

export const BannerList = () => {
  const { tableProps, setFilters } = useTable();
  const filterRef = useRef<ListFieldFiltersHandle>(null);
  const onSearch = (values: Record<string, unknown>) => {
    setFilters(toCrudFilters(values), "replace");
  };

  return (
    <List title="Banners" headerButtons={listFilterHeaderButtons(filterRef)}>
      <ListFieldFilters
        ref={filterRef}
        fields={[
          { name: "title", label: "Title", placeholder: "Title" },
          { name: "imageUrl", label: "Image URL", placeholder: "Image URL", width: 220 },
          { name: "linkUrl", label: "Link URL", placeholder: "Link URL", width: 220 },
          { name: "enabled", label: "Enabled", placeholder: "Enabled", type: "select", options: yesNoOptions },
        ]}
        onSearch={onSearch}
      />
      <Table {...tableProps} rowKey="id">
        <Table.Column dataIndex="id" title="ID" />
        <Table.Column dataIndex="title" title="Title" />
        <Table.Column dataIndex="imageUrl" title="Image URL" />
        <Table.Column dataIndex="enabled" title="Enabled" render={(value: boolean) => (value ? "Y" : "N")} />
        <Table.Column dataIndex="sortOrder" title="Order" />
        <Table.Column title="Actions" render={(_, record: { id: number }) => <ActionButtons id={record.id} />} />
      </Table>
    </List>
  );
};

export const BannerCreate = () => {
  const { formProps, saveButtonProps } = useForm();
  return (
    <Create title="Create Banner" saveButtonProps={saveButtonProps}>
      <Form {...formProps} layout="vertical">
        <Form.Item label="Title" name="title" rules={[{ required: true }]}> 
          <Input />
        </Form.Item>
        <Form.Item label="Image URL" name="imageUrl" rules={[{ required: true }]}> 
          <Input />
        </Form.Item>
        <Form.Item label="Link URL" name="linkUrl">
          <Input />
        </Form.Item>
        <Form.Item label="Start At" name="startAt">
          <Input />
        </Form.Item>
        <Form.Item label="End At" name="endAt">
          <Input />
        </Form.Item>
        <Form.Item label="Enabled" name="enabled">
          <Select options={yesNoOptions} />
        </Form.Item>
        <Form.Item label="Sort Order" name="sortOrder">
          <InputNumber min={0} style={{ width: "100%" }} />
        </Form.Item>
      </Form>
    </Create>
  );
};

export const BannerEdit = () => {
  const { formProps, saveButtonProps } = useForm();
  return (
    <Edit title="Edit Banner" saveButtonProps={saveButtonProps}>
      <Form {...formProps} layout="vertical">
        <Form.Item label="Title" name="title">
          <Input />
        </Form.Item>
        <Form.Item label="Image URL" name="imageUrl">
          <Input />
        </Form.Item>
        <Form.Item label="Link URL" name="linkUrl">
          <Input />
        </Form.Item>
        <Form.Item label="Start At" name="startAt">
          <Input />
        </Form.Item>
        <Form.Item label="End At" name="endAt">
          <Input />
        </Form.Item>
        <Form.Item label="Enabled" name="enabled">
          <Select options={yesNoOptions} />
        </Form.Item>
        <Form.Item label="Sort Order" name="sortOrder">
          <InputNumber min={0} style={{ width: "100%" }} />
        </Form.Item>
      </Form>
    </Edit>
  );
};

export const LoginPolicyList = () => {
  const { tableProps, setFilters } = useTable();
  const filterRef = useRef<ListFieldFiltersHandle>(null);
  const onSearch = (values: Record<string, unknown>) => {
    setFilters(toCrudFilters(values), "replace");
  };

  return (
    <List title="Login Policies" headerButtons={listFilterHeaderButtons(filterRef)}>
      <ListFieldFilters
        ref={filterRef}
        fields={[
          { name: "name", label: "Policy Name", placeholder: "Policy Name" },
          { name: "allowedIpCidr", label: "Allowed CIDR", placeholder: "Allowed CIDR", width: 220 },
          { name: "maxFailCount", label: "Max Fail Count", placeholder: "Max Fail Count", type: "number", width: 150 },
          { name: "lockMinutes", label: "Lock Minutes", placeholder: "Lock Minutes", type: "number", width: 150 },
          { name: "enabled", label: "Enabled", placeholder: "Enabled", type: "select", options: yesNoOptions },
        ]}
        onSearch={onSearch}
      />
      <Table {...tableProps} rowKey="id">
        <Table.Column dataIndex="id" title="ID" />
        <Table.Column dataIndex="name" title="Name" />
        <Table.Column dataIndex="maxFailCount" title="Max Fail" />
        <Table.Column dataIndex="lockMinutes" title="Lock Minutes" />
        <Table.Column dataIndex="allowedIpCidr" title="Allowed CIDR" />
        <Table.Column dataIndex="enabled" title="Enabled" render={(value: boolean) => (value ? "Y" : "N")} />
        <Table.Column title="Actions" render={(_, record: { id: number }) => <ActionButtons id={record.id} />} />
      </Table>
    </List>
  );
};

export const LoginPolicyCreate = () => {
  const { formProps, saveButtonProps } = useForm();
  return (
    <Create title="Create Login Policy" saveButtonProps={saveButtonProps}>
      <Form {...formProps} layout="vertical">
        <Form.Item label="Name" name="name" rules={[{ required: true }]}> 
          <Input />
        </Form.Item>
        <Form.Item label="Max Fail Count" name="maxFailCount" rules={[{ required: true }]}> 
          <InputNumber min={1} style={{ width: "100%" }} />
        </Form.Item>
        <Form.Item label="Lock Minutes" name="lockMinutes" rules={[{ required: true }]}> 
          <InputNumber min={1} style={{ width: "100%" }} />
        </Form.Item>
        <Form.Item label="Allowed IP CIDR" name="allowedIpCidr">
          <Input />
        </Form.Item>
        <Form.Item label="Enabled" name="enabled">
          <Select options={yesNoOptions} />
        </Form.Item>
      </Form>
    </Create>
  );
};

export const LoginPolicyEdit = () => {
  const { formProps, saveButtonProps } = useForm();
  return (
    <Edit title="Edit Login Policy" saveButtonProps={saveButtonProps}>
      <Form {...formProps} layout="vertical">
        <Form.Item label="Name" name="name">
          <Input />
        </Form.Item>
        <Form.Item label="Max Fail Count" name="maxFailCount">
          <InputNumber min={1} style={{ width: "100%" }} />
        </Form.Item>
        <Form.Item label="Lock Minutes" name="lockMinutes">
          <InputNumber min={1} style={{ width: "100%" }} />
        </Form.Item>
        <Form.Item label="Allowed IP CIDR" name="allowedIpCidr">
          <Input />
        </Form.Item>
        <Form.Item label="Enabled" name="enabled">
          <Select options={yesNoOptions} />
        </Form.Item>
      </Form>
    </Edit>
  );
};
