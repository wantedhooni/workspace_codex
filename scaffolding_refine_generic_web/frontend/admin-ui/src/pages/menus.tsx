import { Create, Edit, EditButton, List, useForm } from "@refinedev/antd";
import { Form, Input, InputNumber, Popconfirm, Select, Table } from "antd";
import { useApiUrl, useDelete } from "@refinedev/core";
import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { ListFieldFilters, ListFieldFiltersHandle, listFilterHeaderButtons } from "../components/ListFieldFilters";

type MenuNode = {
  id: number;
  title: string;
  path: string;
  parentId: number | null;
  sortOrder: number;
  children?: MenuNode[];
  parentTitle?: string;
};

type ParentOption = {
  label: string;
  value: number;
};

const ActionButtons = ({ id, onDeleted }: { id: number; onDeleted: () => Promise<void> }) => {
  const { mutate, isLoading } = useDelete();

  const remove = () =>
    new Promise<void>((resolve, reject) => {
      mutate(
        {
          resource: "menus",
          id,
        },
        {
          onSuccess: async () => {
            await onDeleted();
            resolve();
          },
          onError: () => reject(),
        },
      );
    });

  return (
    <>
      <EditButton size="small" recordItemId={id} />
      <Popconfirm title="Delete this menu?" onConfirm={remove} okButtonProps={{ loading: isLoading }}>
        <a style={{ marginInlineStart: 8 }}>Delete</a>
      </Popconfirm>
    </>
  );
};

const buildParentOptions = (nodes: MenuNode[], depth = 0, excludeId?: number): ParentOption[] => {
  return nodes.flatMap((node) => {
    const current = node.id === excludeId ? [] : [{ label: `${"  ".repeat(depth)}${node.title}`, value: node.id }];
    const children = buildParentOptions(node.children ?? [], depth + 1, excludeId);
    return [...current, ...children];
  });
};

const attachParentTitle = (nodes: MenuNode[]): MenuNode[] => {
  const titleMap = new Map<number, string>();
  const collect = (list: MenuNode[]) => {
    list.forEach((node) => {
      titleMap.set(node.id, node.title);
      collect(node.children ?? []);
    });
  };
  collect(nodes);

  const mapNodes = (list: MenuNode[]): MenuNode[] =>
    list.map((node) => ({
      ...node,
      parentTitle: node.parentId ? titleMap.get(node.parentId) ?? "-" : "-",
      children: mapNodes(node.children ?? []),
    }));

  return mapNodes(nodes);
};

const useMenuTree = () => {
  const apiUrl = useApiUrl();
  const [menuTree, setMenuTree] = useState<MenuNode[]>([]);
  const [loading, setLoading] = useState(false);

  const load = useCallback(
    async (filters?: Record<string, unknown>) => {
      setLoading(true);
      try {
        const url = new URL(`${apiUrl}/menus/tree`);
        Object.entries(filters ?? {}).forEach(([field, value]) => {
          if (value === null || value === undefined) {
            return;
          }
          const raw = String(value).trim();
          if (raw.length > 0) {
            url.searchParams.set(field, raw);
          }
        });

        const response = await fetch(url.toString(), { credentials: "include" });
        if (!response.ok) {
          throw new Error(await response.text());
        }
        const data = (await response.json()) as MenuNode[];
        setMenuTree(attachParentTitle(data));
      } finally {
        setLoading(false);
      }
    },
    [apiUrl],
  );

  useEffect(() => {
    load();
  }, [load]);

  return { menuTree, loading, reload: load };
};

export const MenuList = () => {
  const { menuTree, loading, reload } = useMenuTree();
  const filterRef = useRef<ListFieldFiltersHandle>(null);

  const onSearch = async (values: Record<string, unknown>) => {
    await reload(values);
  };

  return (
    <List title="Menus" headerButtons={listFilterHeaderButtons(filterRef)}>
      <ListFieldFilters
        ref={filterRef}
        fields={[
          { name: "title", label: "Title", placeholder: "Enter title" },
          { name: "path", label: "Path", placeholder: "Enter path" },
          { name: "parentId", label: "Parent ID", placeholder: "Enter parent ID", type: "number", width: 140 },
          { name: "sortOrder", label: "Sort Order", placeholder: "Enter sort order", type: "number", width: 140 },
        ]}
        onSearch={onSearch}
      />
      <Table dataSource={menuTree} loading={loading} rowKey="id" pagination={false}>
        <Table.Column dataIndex="id" title="ID" width={80} />
        <Table.Column dataIndex="title" title="Title" />
        <Table.Column dataIndex="path" title="Path" />
        <Table.Column dataIndex="sortOrder" title="Order" width={100} />
        <Table.Column dataIndex="parentTitle" title="Parent" />
        <Table.Column title="Actions" render={(_, record: { id: number }) => <ActionButtons id={record.id} onDeleted={() => reload()} />} />
      </Table>
    </List>
  );
};

export const MenuCreate = () => {
  const { formProps, saveButtonProps } = useForm();
  const { menuTree } = useMenuTree();
  const parentOptions = useMemo(() => buildParentOptions(menuTree), [menuTree]);

  return (
    <Create title="Create Menu" saveButtonProps={saveButtonProps}>
      <Form {...formProps} layout="vertical">
        <Form.Item label="Title" name="title" rules={[{ required: true }]}> 
          <Input />
        </Form.Item>
        <Form.Item label="Path" name="path" rules={[{ required: true }]}> 
          <Input />
        </Form.Item>
        <Form.Item label="Order" name="sortOrder" rules={[{ required: true }]}> 
          <InputNumber min={0} style={{ width: "100%" }} />
        </Form.Item>
        <Form.Item label="Parent" name="parentId">
          <Select allowClear options={parentOptions} />
        </Form.Item>
      </Form>
    </Create>
  );
};

export const MenuEdit = () => {
  const { formProps, saveButtonProps, queryResult } = useForm();
  const { menuTree } = useMenuTree();
  const currentId = queryResult?.data?.data?.id as number | undefined;
  const parentOptions = useMemo(() => buildParentOptions(menuTree, 0, currentId), [menuTree, currentId]);

  return (
    <Edit title="Edit Menu" saveButtonProps={saveButtonProps}>
      <Form {...formProps} layout="vertical">
        <Form.Item label="Title" name="title" rules={[{ required: true }]}> 
          <Input />
        </Form.Item>
        <Form.Item label="Path" name="path" rules={[{ required: true }]}> 
          <Input />
        </Form.Item>
        <Form.Item label="Order" name="sortOrder" rules={[{ required: true }]}> 
          <InputNumber min={0} style={{ width: "100%" }} />
        </Form.Item>
        <Form.Item label="Parent" name="parentId">
          <Select allowClear options={parentOptions} />
        </Form.Item>
      </Form>
    </Edit>
  );
};
