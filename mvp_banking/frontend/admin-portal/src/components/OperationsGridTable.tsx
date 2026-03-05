import { Table } from "antd";
import type { TablePaginationConfig, TableProps } from "antd";
import { useMemo } from "react";

const DEFAULT_PAGE_SIZE_OPTIONS = ["10", "20", "50", "100"];
const DEFAULT_SCROLL_HEIGHT = 500;

type OperationsGridTableProps<RecordType extends object> = TableProps<RecordType>;

function resolvePagination<RecordType extends object>(
  pagination: TableProps<RecordType>["pagination"],
): TableProps<RecordType>["pagination"] {
  const mergedPagination: TablePaginationConfig = {
    showSizeChanger: true,
    showQuickJumper: true,
    pageSizeOptions: DEFAULT_PAGE_SIZE_OPTIONS,
    hideOnSinglePage: false,
    showTotal: (total, range) => `${range[0]}-${range[1]} / ${total}`,
    ...(pagination === false ? {} : (pagination ?? {})),
  };
  return mergedPagination;
}

function extractDataIndexValue(record: unknown, dataIndex: unknown): unknown {
  if (!dataIndex) {
    return undefined;
  }
  const keyPath = Array.isArray(dataIndex) ? dataIndex : [dataIndex];
  let current: unknown = record;
  for (const key of keyPath) {
    if (current === null || current === undefined || typeof current !== "object") {
      return undefined;
    }
    current = (current as Record<string | number, unknown>)[key as string | number];
  }
  return current;
}

function compareValues(left: unknown, right: unknown): number {
  if (left === right) {
    return 0;
  }
  if (left === null || left === undefined || left === "") {
    return 1;
  }
  if (right === null || right === undefined || right === "") {
    return -1;
  }

  const leftNumber = Number(left);
  const rightNumber = Number(right);
  if (Number.isFinite(leftNumber) && Number.isFinite(rightNumber)) {
    return leftNumber - rightNumber;
  }

  const leftDate = Date.parse(String(left));
  const rightDate = Date.parse(String(right));
  if (Number.isFinite(leftDate) && Number.isFinite(rightDate)) {
    return leftDate - rightDate;
  }

  return String(left).localeCompare(String(right));
}

function resolveColumns<RecordType extends object>(
  columns: TableProps<RecordType>["columns"],
): TableProps<RecordType>["columns"] {
  if (!columns) {
    return columns;
  }
  return columns.map((column) => {
    if (!column) {
      return column;
    }
    if ("children" in column && column.children) {
      return {
        ...column,
        children: resolveColumns(column.children),
      };
    }
    if (!("dataIndex" in column) || column.sorter) {
      return column;
    }
    if (typeof column.dataIndex === "undefined") {
      return column;
    }
    return {
      ...column,
      sorter: (a: RecordType, b: RecordType) => compareValues(
        extractDataIndexValue(a, column.dataIndex),
        extractDataIndexValue(b, column.dataIndex),
      ),
      sortDirections: ["descend", "ascend"],
    };
  });
}

export function OperationsGridTable<RecordType extends object>({
  className,
  size,
  bordered,
  sticky,
  scroll,
  pagination,
  dataSource,
  columns,
  ...props
}: OperationsGridTableProps<RecordType>) {
  const resolvedColumns = useMemo(() => resolveColumns(columns), [columns]);
  const resolvedDataSource = dataSource ?? [];

  return (
    <Table<RecordType>
      {...props}
      dataSource={resolvedDataSource}
      columns={resolvedColumns}
      className={["ops-grid-table", className].filter(Boolean).join(" ")}
      size={size ?? "small"}
      bordered={bordered ?? true}
      sticky={sticky ?? true}
      scroll={{ x: "max-content", y: DEFAULT_SCROLL_HEIGHT, ...(scroll ?? {}) }}
      pagination={resolvePagination(pagination)}
    />
  );
}
