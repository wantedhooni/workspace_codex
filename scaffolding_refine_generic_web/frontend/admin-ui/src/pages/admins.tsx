import { Create, Edit, List, useForm, useSelect, EditButton, DeleteButton } from "@refinedev/antd";
import { Form, Input, Select, Table } from "antd";
import { useTable } from "@refinedev/antd";
import { useRef } from "react";
import { ListFieldFilters, ListFieldFiltersHandle, listFilterHeaderButtons } from "../components/ListFieldFilters";
import { toCrudFilters } from "../utils/filters";

export const AdminList = () => {
  const { tableProps, setFilters } = useTable();
  const filterRef = useRef<ListFieldFiltersHandle>(null);

  const onSearch = (values: Record<string, unknown>) => {
    setFilters(toCrudFilters(values), "replace");
  };

  return (
    <List title="Admin Users" headerButtons={listFilterHeaderButtons(filterRef)}>
      <ListFieldFilters
        ref={filterRef}
        fields={[{ name: "username", label: "Username", placeholder: "Enter username" }]}
        onSearch={onSearch}
      />
      <Table {...tableProps} rowKey="id">
        <Table.Column dataIndex="id" title="ID" />
        <Table.Column dataIndex="username" title="Username" />
        <Table.Column dataIndex="roleIds" title="Roles" render={(value: number[]) => value?.join(", ")} />
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

export const AdminCreate = () => {
  const { formProps, saveButtonProps } = useForm();
  const { selectProps } = useSelect({ resource: "roles", optionLabel: "name" });

  return (
    <Create title="Create Admin" saveButtonProps={saveButtonProps}>
      <Form {...formProps} layout="vertical">
        <Form.Item label="Username" name="username" rules={[{ required: true }]}> 
          <Input />
        </Form.Item>
        <Form.Item label="Password" name="passwordHash" rules={[{ required: true }]}> 
          <Input.Password />
        </Form.Item>
        <Form.Item label="Roles" name="roleIds">
          <Select mode="multiple" {...selectProps} />
        </Form.Item>
      </Form>
    </Create>
  );
};

export const AdminEdit = () => {
  const { formProps, saveButtonProps } = useForm();
  const { selectProps } = useSelect({ resource: "roles", optionLabel: "name" });

  return (
    <Edit title="Edit Admin" saveButtonProps={saveButtonProps}>
      <Form {...formProps} layout="vertical">
        <Form.Item label="Username" name="username" rules={[{ required: true }]}> 
          <Input />
        </Form.Item>
        <Form.Item label="Password" name="passwordHash">
          <Input.Password />
        </Form.Item>
        <Form.Item label="Roles" name="roleIds">
          <Select mode="multiple" {...selectProps} />
        </Form.Item>
      </Form>
    </Edit>
  );
};
