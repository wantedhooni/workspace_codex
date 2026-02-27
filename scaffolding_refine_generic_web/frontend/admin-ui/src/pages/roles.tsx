import { Create, Edit, List, useForm, useSelect, EditButton, DeleteButton } from "@refinedev/antd";
import { Form, Input, Select, Table } from "antd";
import { useTable } from "@refinedev/antd";
import { useRef } from "react";
import { ListFieldFilters, ListFieldFiltersHandle, listFilterHeaderButtons } from "../components/ListFieldFilters";
import { toCrudFilters } from "../utils/filters";

export const RoleList = () => {
  const { tableProps, setFilters } = useTable();
  const filterRef = useRef<ListFieldFiltersHandle>(null);

  const onSearch = (values: Record<string, unknown>) => {
    setFilters(toCrudFilters(values), "replace");
  };

  return (
    <List title="Roles" headerButtons={listFilterHeaderButtons(filterRef)}>
      <ListFieldFilters
        ref={filterRef}
        fields={[
          { name: "name", label: "Role Name", placeholder: "Enter role name" },
          { name: "description", label: "Description", placeholder: "Enter description", width: 240 },
        ]}
        onSearch={onSearch}
      />
      <Table {...tableProps} rowKey="id">
        <Table.Column dataIndex="id" title="ID" />
        <Table.Column dataIndex="name" title="Name" />
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

export const RoleCreate = () => {
  const { formProps, saveButtonProps } = useForm();
  const { selectProps } = useSelect({ resource: "permissions", optionLabel: "code" });

  return (
    <Create title="Create Role" saveButtonProps={saveButtonProps}>
      <Form {...formProps} layout="vertical">
        <Form.Item label="Name" name="name" rules={[{ required: true }]}> 
          <Input />
        </Form.Item>
        <Form.Item label="Description" name="description" rules={[{ required: true }]}> 
          <Input />
        </Form.Item>
        <Form.Item label="Permissions" name="permissionIds">
          <Select mode="multiple" {...selectProps} />
        </Form.Item>
      </Form>
    </Create>
  );
};

export const RoleEdit = () => {
  const { formProps, saveButtonProps } = useForm();
  const { selectProps } = useSelect({ resource: "permissions", optionLabel: "code" });

  return (
    <Edit title="Edit Role" saveButtonProps={saveButtonProps}>
      <Form {...formProps} layout="vertical">
        <Form.Item label="Name" name="name" rules={[{ required: true }]}> 
          <Input />
        </Form.Item>
        <Form.Item label="Description" name="description" rules={[{ required: true }]}> 
          <Input />
        </Form.Item>
        <Form.Item label="Permissions" name="permissionIds">
          <Select mode="multiple" {...selectProps} />
        </Form.Item>
      </Form>
    </Edit>
  );
};
