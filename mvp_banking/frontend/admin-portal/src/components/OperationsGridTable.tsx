import type { ReactNode } from "react";
import { useMemo } from "react";
import type { ColDef, ICellRendererParams, ValueGetterParams } from "ag-grid-community";
import { AgGridReact } from "ag-grid-react";

const DEFAULT_SCROLL_HEIGHT = 500;
const DEFAULT_COLUMN_WIDTH = 160;

type GridColumn<RecordType> = {
  title: ReactNode;
  dataIndex?: keyof RecordType | string | Array<string | number>;
  width?: number;
  render?: (value: any, record: RecordType) => ReactNode;
  sorter?: boolean | ((left: RecordType, right: RecordType) => number);
};

type GridPagination = false | {
  current?: number;
  pageSize?: number;
  total?: number;
  onChange?: (page: number, pageSize: number) => void;
};

type OperationsGridTableProps<RecordType extends object> = {
  className?: string;
  rowKey?: keyof RecordType | string | ((record: RecordType) => string);
  dataSource?: RecordType[];
  columns?: GridColumn<RecordType>[];
  pagination?: GridPagination;
  scroll?: { x?: number | string; y?: number };
  locale?: { emptyText?: string };
};

function extractDataIndexValue(record: unknown, dataIndex: any): unknown {
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

function resolveHeaderName(title: ReactNode) {
  return typeof title === "string" ? title : "";
}

function resolveRowId<RecordType extends object>(
  rowKey: OperationsGridTableProps<RecordType>["rowKey"],
  record: RecordType,
  index: number,
) {
  if (typeof rowKey === "function") {
    return rowKey(record);
  }
  if (typeof rowKey === "string") {
    const directValue = (record as Record<string, unknown>)[rowKey];
    if (directValue !== undefined && directValue !== null) {
      return String(directValue);
    }
  }
  return String(index);
}

function resolveColumns<RecordType extends object>(columns: GridColumn<RecordType>[] | undefined): ColDef<RecordType>[] {
  if (!columns) {
    return [];
  }

  return columns.map((column) => {
    const valueGetter = (params: ValueGetterParams<RecordType>) => extractDataIndexValue(params.data, column.dataIndex);
    const customSorter = typeof column.sorter === "function" ? column.sorter : null;
    const comparator: ColDef<RecordType>["comparator"] = customSorter
      ? ((left, right, nodeA, nodeB) => customSorter(nodeA.data as RecordType, nodeB.data as RecordType))
      : ((left, right) => compareValues(left, right));

    return {
      headerName: resolveHeaderName(column.title),
      width: column.width ?? DEFAULT_COLUMN_WIDTH,
      minWidth: Math.min(column.width ?? DEFAULT_COLUMN_WIDTH, 220),
      sortable: column.sorter !== false,
      resizable: true,
      suppressMovable: false,
      wrapHeaderText: true,
      autoHeaderHeight: true,
      valueGetter,
      comparator,
      cellRenderer: column.render
        ? (params: ICellRendererParams<RecordType>) => column.render?.(params.value, params.data as RecordType)
        : undefined,
    };
  });
}

export function OperationsGridTable<RecordType extends object>({
  className,
  rowKey,
  dataSource,
  columns,
  pagination,
  scroll,
  locale,
}: OperationsGridTableProps<RecordType>) {
  const rowData = useMemo(
    () => (dataSource ?? []).map((record, index) => ({ ...record, __rowId: resolveRowId(rowKey, record, index) })),
    [dataSource, rowKey],
  );
  const colDefs = useMemo(() => resolveColumns(columns), [columns]);
  const height = scroll?.y ?? DEFAULT_SCROLL_HEIGHT;
  const resolvedPagination = pagination || null;
  const pageSize = resolvedPagination?.pageSize ?? 20;
  const currentPage = resolvedPagination?.current ?? 1;
  const total = resolvedPagination?.total ?? rowData.length;
  const totalPages = Math.max(1, Math.ceil(total / pageSize));

  return (
    <div className={["ops-grid-card", className].filter(Boolean).join(" ")}>
      <div className="ag-theme-quartz ops-ag-grid-shell" style={{ height }}>
        <AgGridReact
          rowData={rowData}
          columnDefs={colDefs}
          getRowId={(params) => String((params.data as { __rowId: string }).__rowId)}
          suppressCellFocus
          animateRows
          suppressDragLeaveHidesColumns
          defaultColDef={{
            flex: 1,
            minWidth: 140,
            wrapText: true,
            autoHeight: true,
            suppressHeaderMenuButton: true,
            suppressHeaderContextMenu: true,
          }}
          overlayNoRowsTemplate={`<span class="ops-grid-empty">${locale?.emptyText ?? "표시할 데이터가 없습니다."}</span>`}
        />
      </div>
      {resolvedPagination ? (
        <div className="ops-grid-footer">
          <div className="ops-grid-footer-meta">
            <strong>{`${total.toLocaleString()}건`}</strong>
            <span>{`${currentPage} / ${totalPages} 페이지`}</span>
          </div>
          <div className="ops-grid-footer-actions">
            <select
              value={String(pageSize)}
              onChange={(event) => resolvedPagination.onChange?.(1, Number(event.target.value))}
            >
              {[10, 20, 50, 100].map((size) => (
                <option key={size} value={size}>
                  {size} / page
                </option>
              ))}
            </select>
            <button type="button" onClick={() => resolvedPagination.onChange?.(Math.max(1, currentPage - 1), pageSize)} disabled={currentPage <= 1}>
              Prev
            </button>
            <button type="button" onClick={() => resolvedPagination.onChange?.(Math.min(totalPages, currentPage + 1), pageSize)} disabled={currentPage >= totalPages}>
              Next
            </button>
          </div>
        </div>
      ) : null}
    </div>
  );
}
