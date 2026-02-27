import { Button, Input, Space } from "antd";

type ListSearchBarProps = {
  value: string;
  onChange: (value: string) => void;
  onSearch: () => void;
  placeholder?: string;
};

export const ListSearchBar = ({ value, onChange, onSearch, placeholder = "Search..." }: ListSearchBarProps) => {
  return (
    <Space style={{ marginBottom: 12 }}>
      <Input
        value={value}
        onChange={(event) => onChange(event.target.value)}
        onPressEnter={onSearch}
        placeholder={placeholder}
        allowClear
        style={{ width: 320 }}
      />
      <Button type="primary" onClick={onSearch}>
        Search
      </Button>
    </Space>
  );
};
