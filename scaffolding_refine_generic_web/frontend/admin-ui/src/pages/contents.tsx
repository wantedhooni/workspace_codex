import { Create, Edit, List, useForm, EditButton, DeleteButton } from "@refinedev/antd";
import { Form, Input, Select, Table } from "antd";
import { useTable } from "@refinedev/antd";
import { useRef } from "react";
import { ListFieldFilters, ListFieldFiltersHandle, listFilterHeaderButtons } from "../components/ListFieldFilters";
import { toCrudFilters } from "../utils/filters";

const { TextArea } = Input;

const contentStatusOptions = [
  { label: "Draft", value: "DRAFT" },
  { label: "Published", value: "PUBLISHED" },
  { label: "Archived", value: "ARCHIVED" },
];

export const ContentList = () => {
  const { tableProps, setFilters } = useTable();
  const filterRef = useRef<ListFieldFiltersHandle>(null);

  const onSearch = (values: Record<string, unknown>) => {
    setFilters(toCrudFilters(values), "replace");
  };

  return (
    <List title="Contents" headerButtons={listFilterHeaderButtons(filterRef)}>
      <ListFieldFilters
        ref={filterRef}
        fields={[
          { name: "title", label: "Title", placeholder: "Enter title" },
          { name: "slug", label: "Slug", placeholder: "Enter slug" },
          { name: "status", label: "Status", placeholder: "Select status", type: "select", options: contentStatusOptions },
        ]}
        onSearch={onSearch}
      />
      <Table {...tableProps} rowKey="id">
        <Table.Column dataIndex="id" title="ID" />
        <Table.Column dataIndex="title" title="Title" />
        <Table.Column dataIndex="slug" title="Slug" />
        <Table.Column dataIndex="status" title="Status" />
        <Table.Column dataIndex="updatedAt" title="Updated At" />
        <Table.Column
          title="Actions"
          render={(_, record: { id: number }) => (
            <>
              <EditButton size="small" recordItemId={record.id} />
              <DeleteButton size="small" recordItemId={record.id} />
            </>
          )}
        />
      </Table>
    </List>
  );
};

export const ContentCreate = () => {
  const { formProps, saveButtonProps } = useForm();

  return (
    <Create title="Create Content" saveButtonProps={saveButtonProps}>
      <Form {...formProps} layout="vertical">
        <Form.Item label="Title" name="title" rules={[{ required: true }]}> 
          <Input />
        </Form.Item>
        <Form.Item label="Slug" name="slug" rules={[{ required: true }]}> 
          <Input />
        </Form.Item>
        <Form.Item label="Body" name="body" rules={[{ required: true }]}> 
          <TextArea rows={6} />
        </Form.Item>
        <Form.Item label="Status" name="status" rules={[{ required: true }]}> 
          <Select options={contentStatusOptions} />
        </Form.Item>
      </Form>
    </Create>
  );
};

export const ContentEdit = () => {
  const { formProps, saveButtonProps } = useForm();

  return (
    <Edit title="Edit Content" saveButtonProps={saveButtonProps}>
      <Form {...formProps} layout="vertical">
        <Form.Item label="Title" name="title" rules={[{ required: true }]}> 
          <Input />
        </Form.Item>
        <Form.Item label="Slug" name="slug" rules={[{ required: true }]}> 
          <Input />
        </Form.Item>
        <Form.Item label="Body" name="body" rules={[{ required: true }]}> 
          <TextArea rows={6} />
        </Form.Item>
        <Form.Item label="Status" name="status" rules={[{ required: true }]}> 
          <Select options={contentStatusOptions} />
        </Form.Item>
      </Form>
    </Edit>
  );
};
