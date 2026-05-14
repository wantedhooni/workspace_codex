"use client";

import { useMemo } from "react";
import { AgGridReact } from "ag-grid-react";
import { AllCommunityModule, ModuleRegistry } from "ag-grid-community";
import type { ColDef, RowClickedEvent } from "ag-grid-community";

import type { JsonRecord } from "@/types/admin-api";

ModuleRegistry.registerModules([AllCommunityModule]);

function toDisplayValue(value: unknown): string {
  if (value === null || value === undefined) {
    return "";
  }

  if (typeof value === "object") {
    return JSON.stringify(value);
  }

  return String(value);
}

/**
 * REST API 목록 응답을 표현하는 공통 AG Grid 템플릿입니다.
 * 도메인별 컬럼 후보와 실제 응답 필드를 합쳐 운영 테이블을 구성합니다.
 */
export function RestAgGridTemplate({
  rows,
  columnFields,
  selectedId,
  onSelect,
}: {
  rows: JsonRecord[];
  columnFields: string[];
  selectedId?: string;
  onSelect: (row: JsonRecord) => void;
}) {
  const columns = useMemo<ColDef<JsonRecord>[]>(() => {
    const responseFields = Array.from(new Set(rows.flatMap((row) => Object.keys(row))));
    const mergedFields = Array.from(new Set([...columnFields, ...responseFields]));
    const fields = mergedFields.length > 0 ? mergedFields : columnFields;

    return fields.map((field) => ({
      field,
      headerName: field,
      sortable: true,
      filter: true,
      resizable: true,
      minWidth: field === "id" ? 90 : 140,
      valueFormatter: ({ value }) => toDisplayValue(value),
    }));
  }, [columnFields, rows]);

  return (
    <div className="admin-grid ag-theme-alpine h-[520px] w-full">
      <AgGridReact
        theme="legacy"
        rowData={rows}
        columnDefs={columns}
        rowSelection={{ mode: "singleRow" }}
        animateRows
        suppressCellFocus
        getRowId={({ data }) => String(data.id ?? data._id ?? JSON.stringify(data))}
        rowClassRules={{
          "ag-row-selected": ({ data }) => String(data?.id ?? data?._id ?? "") === selectedId,
        }}
        onRowClicked={(event: RowClickedEvent<JsonRecord>) => {
          if (event.data) {
            onSelect(event.data);
          }
        }}
        overlayNoRowsTemplate="<span>조회된 데이터가 없습니다.</span>"
      />
    </div>
  );
}
