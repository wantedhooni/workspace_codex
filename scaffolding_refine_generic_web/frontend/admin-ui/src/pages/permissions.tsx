import { Create, Edit, List, useForm, EditButton, DeleteButton } from "@refinedev/antd";
import { Form, Input, Table } from "antd";
import { useTable } from "@refinedev/antd";
import { useRef } from "react";
import { ListFieldFilters, ListFieldFiltersHandle, listFilterHeaderButtons } from "../components/ListFieldFilters";
import { toCrudFilters } from "../utils/filters";

export const PermissionList = () => {
  const { tableProps, setFilters } = useTable();
  const filterRef = useRef<ListFieldFiltersHandle>(null);

  const onSearch = (values: Record<string, unknown>) => {
    setFilters(toCrudFilters(values), "replace");
  };

  return (
    <List title="Permissions" headerButtons={listFilterHeaderButtons(filterRef)}>
      <ListFieldFilters
        ref={filterRef}
        fields={[
          { name: "code", label: "Permission Code", placeholder: "Enter permission code" },
          { name: "description", label: "Description", placeholder: "Enter description", width: 240 },
        ]}
        onSearch={onSearch}
      />
      <Table {...tableProps} rowKey="id">
        <Table.Column dataIndex="id" title="ID" />
        <Table.Column dataIndex="code" title="Code" />
        <Table.Column dataIndex="description" title="Description" />
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

export const PermissionCreate = () => {
  const { formProps, saveButtonProps } = useForm();

  return (
    <Create title="Create Permission" saveButtonProps={saveButtonProps}>
      <Form {...formProps} layout="vertical">
        <Form.Item label="Code" name="code" rules={[{ required: true }]}> 
          <Input />
        </Form.Item>
        <Form.Item label="Description" name="description" rules={[{ required: true }]}> 
          <Input />
        </Form.Item>
      </Form>
    </Create>
  );
};

export const PermissionEdit = () => {
  const { formProps, saveButtonProps } = useForm();

  return (
    <Edit title="Edit Permission" saveButtonProps={saveButtonProps}>
      <Form {...formProps} layout="vertical">
        <Form.Item label="Code" name="code" rules={[{ required: true }]}> 
          <Input />
        </Form.Item>
        <Form.Item label="Description" name="description" rules={[{ required: true }]}> 
          <Input />
        </Form.Item>
      </Form>
    </Edit>
  );
};
