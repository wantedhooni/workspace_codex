import { Button, Form, Input, InputNumber, Select, Space } from "antd";
import React, { forwardRef, useImperativeHandle } from "react";

type OptionItem = {
  label: string;
  value: string | number | boolean;
};

export type FieldConfig = {
  name: string;
  label: string;
  placeholder?: string;
  type?: "text" | "number" | "select";
  options?: OptionItem[];
  width?: number;
};

type ListFieldFiltersProps = {
  fields: FieldConfig[];
  onSearch: (values: Record<string, unknown>) => void;
  showActions?: boolean;
  subtitle?: string;
};

const renderField = (field: FieldConfig, onSubmit: () => void) => {
  const width = field.width ?? 180;
  const inputId = `list-filter-${field.name}`;

  if (field.type === "number") {
    return (
      <InputNumber
        id={inputId}
        style={{ width }}
        placeholder={field.placeholder ?? field.label}
        onPressEnter={() => onSubmit()}
      />
    );
  }

  if (field.type === "select") {
    return <Select id={inputId} allowClear options={field.options ?? []} style={{ width }} placeholder={field.placeholder ?? field.label} />;
  }

  return (
    <Input
      id={inputId}
      allowClear
      style={{ width }}
      placeholder={field.placeholder ?? field.label}
      onPressEnter={() => onSubmit()}
    />
  );
};

export type ListFieldFiltersHandle = {
  submit: () => void;
  reset: () => void;
};

type HeaderButtonsContext = {
  defaultButtons: React.ReactNode;
};

export const listFilterHeaderButtons = (filterRef: React.RefObject<ListFieldFiltersHandle | null>) => ({ defaultButtons }: HeaderButtonsContext) => (
  <Space>
    {defaultButtons}
    <Button type="primary" onClick={() => filterRef.current?.submit()}>
      Search
    </Button>
    <Button onClick={() => filterRef.current?.reset()}>Reset</Button>
  </Space>
);

export const ListFieldFilters = forwardRef<ListFieldFiltersHandle, ListFieldFiltersProps>(function ListFieldFilters(
  { fields, onSearch, showActions = false, subtitle = "Integrated Search Filters" },
  ref,
) {
  const [form] = Form.useForm();

  const onFinish = (values: Record<string, unknown>) => {
    onSearch(values);
  };

  const onReset = () => {
    form.resetFields();
    onSearch({});
  };

  useImperativeHandle(ref, () => ({
    submit: () => {
      form.submit();
    },
    reset: onReset,
  }));

  return (
    <div className="list-filter-panel">
      <div className="list-filter-panel-subtitle">{subtitle}</div>
      <Form form={form} layout="inline" onFinish={onFinish} className="list-filter-form">
        {fields.map((field) => (
          <Form.Item key={field.name} name={field.name} className="list-filter-field-item">
            <div className="list-filter-field">
              <label className="list-filter-field-label" htmlFor={`list-filter-${field.name}`}>
                {field.label}
              </label>
              {renderField(field, () => form.submit())}
            </div>
          </Form.Item>
        ))}
        {showActions && (
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit">
                Search
              </Button>
              <Button onClick={onReset}>Reset</Button>
            </Space>
          </Form.Item>
        )}
      </Form>
    </div>
  );
});
