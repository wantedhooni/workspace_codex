import { useMemo, type ReactNode } from "react";
import type { ColDef, ICellRendererParams } from "ag-grid-community";
import { AgGridReact } from "ag-grid-react";

type AppGridTableProps<RowType extends object> = {
  rowData: RowType[];
  columnDefs: ColDef<RowType>[];
  emptyMessage?: string;
  height?: number;
  className?: string;
  getRowId?: (row: RowType, index: number) => string;
  pagination?: false | {
    current: number;
    pageSize: number;
    total: number;
    onChange: (page: number, pageSize: number) => void;
  };
};

const DEFAULT_HEIGHT = 500;

export function AppGridTable<RowType extends object>({
  rowData,
  columnDefs,
  emptyMessage,
  height = DEFAULT_HEIGHT,
  className,
  getRowId,
  pagination,
}: AppGridTableProps<RowType>) {
  const rows = useMemo(
    () => rowData.map((row, index) => ({ ...row, __rowId: getRowId ? getRowId(row, index) : String(index) })),
    [getRowId, rowData],
  );

  const columns = useMemo(
    () => columnDefs.map((column) => ({
      minWidth: 140,
      resizable: true,
      sortable: true,
      wrapHeaderText: true,
      autoHeaderHeight: true,
      suppressHeaderMenuButton: true,
      suppressHeaderContextMenu: true,
      ...column,
    })),
    [columnDefs],
  );

  const totalPages = pagination ? Math.max(0, Math.ceil(pagination.total / pagination.pageSize)) : 0;

  return (
    <div className={["app-grid-card", className].filter(Boolean).join(" ")}>
      <div className="ag-theme-quartz app-ag-grid-shell" style={{ height }}>
        <AgGridReact
          rowData={rows}
          columnDefs={columns}
          getRowId={(params) => String((params.data as { __rowId: string }).__rowId)}
          suppressCellFocus
          animateRows
          defaultColDef={{
            flex: 1,
            minWidth: 140,
            resizable: true,
            wrapText: true,
            autoHeight: true,
            suppressHeaderMenuButton: true,
            suppressHeaderContextMenu: true,
          }}
          overlayNoRowsTemplate={`<span class="app-grid-empty">${emptyMessage ?? "표시할 데이터가 없습니다."}</span>`}
        />
      </div>
      {pagination ? (
        <div className="app-grid-footer">
          <div className="app-grid-footer-meta">
            <strong>{`${pagination.total.toLocaleString()}건`}</strong>
            <span>{`${Math.min(pagination.current, Math.max(totalPages, 1))} / ${Math.max(totalPages, 1)} 페이지`}</span>
          </div>
          <div className="app-grid-footer-actions">
            <select
              value={String(pagination.pageSize)}
              onChange={(event) => pagination.onChange(1, Number(event.target.value))}
            >
              {[10, 20, 50, 100].map((size) => (
                <option key={size} value={size}>
                  {size} / page
                </option>
              ))}
            </select>
            <button
              type="button"
              onClick={() => pagination.onChange(Math.max(1, pagination.current - 1), pagination.pageSize)}
              disabled={pagination.current <= 1}
            >
              Prev
            </button>
            <button
              type="button"
              onClick={() => pagination.onChange(Math.min(Math.max(totalPages, 1), pagination.current + 1), pagination.pageSize)}
              disabled={totalPages <= 1 || pagination.current >= totalPages}
            >
              Next
            </button>
          </div>
        </div>
      ) : null}
    </div>
  );
}

export function renderGridCell<RowType extends object>(
  renderer: (params: ICellRendererParams<RowType>) => ReactNode,
) {
  return renderer;
}
