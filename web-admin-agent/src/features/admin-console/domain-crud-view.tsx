"use client";

import { useMemo, useState } from "react";
import { Check, Pencil, Plus, RefreshCw, Search, Trash2, X } from "lucide-react";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { AdminApiService } from "@/services/admin-api-service";
import type { AuthSession, DomainConfig, FieldConfig, JsonRecord } from "@/types/admin-api";
import { RestAgGridTemplate } from "./rest-ag-grid-template";

function initialValues(fields: FieldConfig[], source?: JsonRecord): JsonRecord {
  return Object.fromEntries(
    fields.map((field) => [field.name, source?.[field.name] ?? ""])
  );
}

function normalizePayload(values: JsonRecord): JsonRecord {
  return Object.fromEntries(
    Object.entries(values)
      .filter(([, value]) => value !== "")
      .map(([key, value]) => {
        if (typeof value === "string" && value.trim() !== "" && !Number.isNaN(Number(value))) {
          return [key, Number(value)];
        }

        return [key, value];
      })
  );
}

function getRowId(row: JsonRecord | null): string | undefined {
  if (!row) {
    return undefined;
  }

  const id = row.id ?? row._id;
  return id === undefined || id === null ? undefined : String(id);
}

/**
 * 도메인별 검색 필터와 CRUD 액션을 제공하는 공통 운영 화면입니다.
 * DomainConfig만 교체하면 관리자, 계좌, 계좌 거래 화면으로 재사용됩니다.
 */
export function DomainCrudView({
  apiService,
  config,
  session,
}: {
  apiService: AdminApiService;
  config: DomainConfig;
  session: AuthSession;
}) {
  const [searchValues, setSearchValues] = useState<JsonRecord>(() =>
    initialValues(config.searchFields)
  );
  const [formValues, setFormValues] = useState<JsonRecord>(() =>
    initialValues(config.formFields)
  );
  const [rows, setRows] = useState<JsonRecord[]>([]);
  const [selectedRow, setSelectedRow] = useState<JsonRecord | null>(null);
  const [mode, setMode] = useState<"create" | "edit">("create");
  const [pageInfo, setPageInfo] = useState({ page: 0, size: 20, totalElements: 0 });
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  const selectedId = getRowId(selectedRow);

  const formTitle = useMemo(
    () => (mode === "create" ? `${config.label} 생성` : `${config.label} 수정`),
    [config.label, mode]
  );

  const handleSearch = async (page = 0) => {
    setIsLoading(true);
    setError("");
    setMessage("");

    try {
      const response = await apiService.search(
        config.endpoint,
        session.accessToken || "",
        normalizePayload(searchValues),
        page,
        pageInfo.size
      );
      setRows(response.content ?? []);
      setPageInfo({
        page: response.page ?? page,
        size: response.size ?? pageInfo.size,
        totalElements: response.totalElements ?? 0,
      });
      setSelectedRow(null);
    } catch (errorValue) {
      setError(errorValue instanceof Error ? errorValue.message : "목록 조회에 실패했습니다.");
    } finally {
      setIsLoading(false);
    }
  };

  const handleSelect = (row: JsonRecord) => {
    setSelectedRow(row);
    setMode("edit");
    setFormValues(initialValues(config.formFields, row));
  };

  const handleNew = () => {
    setMode("create");
    setSelectedRow(null);
    setFormValues(initialValues(config.formFields));
    setMessage("");
    setError("");
  };

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setIsLoading(true);
    setError("");
    setMessage("");

    try {
      const payload = normalizePayload(formValues);

      if (mode === "edit") {
        if (!selectedId) {
          throw new Error("수정할 행의 id가 없습니다.");
        }

        await apiService.update(config.endpoint, session.accessToken || "", selectedId, payload);
        setMessage("수정이 완료되었습니다.");
      } else {
        await apiService.create(config.endpoint, session.accessToken || "", payload);
        setMessage("생성이 완료되었습니다.");
      }

      await handleSearch(pageInfo.page);
      handleNew();
    } catch (errorValue) {
      setError(errorValue instanceof Error ? errorValue.message : "저장에 실패했습니다.");
    } finally {
      setIsLoading(false);
    }
  };

  const handleDelete = async () => {
    if (!selectedId) {
      setError("삭제할 행을 먼저 선택하세요.");
      return;
    }

    if (!window.confirm(`${config.label} ID ${selectedId} 항목을 삭제할까요?`)) {
      return;
    }

    setIsLoading(true);
    setError("");
    setMessage("");

    try {
      await apiService.delete(config.endpoint, session.accessToken || "", selectedId);
      setMessage("삭제가 완료되었습니다.");
      await handleSearch(pageInfo.page);
      handleNew();
    } catch (errorValue) {
      setError(errorValue instanceof Error ? errorValue.message : "삭제에 실패했습니다.");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="space-y-4">
      <section className="rounded-lg border border-slate-200 bg-white">
        <div className="border-b border-slate-200 px-5 py-4">
          <h1 className="text-base font-semibold">{config.label}</h1>
          <p className="mt-1 text-sm text-slate-500">{config.description}</p>
        </div>

        <form
          className="grid gap-3 px-5 py-4 md:grid-cols-2 xl:grid-cols-4"
          onSubmit={(event) => {
            event.preventDefault();
            void handleSearch(0);
          }}
        >
          {config.searchFields.map((field) => (
            <FieldControl
              key={field.name}
              field={field}
              value={searchValues[field.name]}
              onChange={(value) =>
                setSearchValues((current) => ({ ...current, [field.name]: value }))
              }
            />
          ))}
          <div className="flex items-end gap-2">
            <Button type="submit" disabled={isLoading}>
              <Search className="size-4" />
              검색
            </Button>
            <Button
              type="button"
              variant="outline"
              onClick={() => setSearchValues(initialValues(config.searchFields))}
            >
              <X className="size-4" />
              초기화
            </Button>
          </div>
        </form>
      </section>

      <section className="grid gap-4 xl:grid-cols-[minmax(0,1fr)_360px]">
        <div className="rounded-lg border border-slate-200 bg-white">
          <div className="flex flex-wrap items-center justify-between gap-2 border-b border-slate-200 px-5 py-3">
            <div className="text-sm text-slate-500">
              총 <span className="font-semibold text-slate-950">{pageInfo.totalElements}</span>건
            </div>
            <div className="flex gap-2">
              <Button variant="outline" size="sm" onClick={() => void handleSearch(pageInfo.page)}>
                <RefreshCw className="size-4" />
                새로고침
              </Button>
              <Button variant="outline" size="sm" onClick={handleNew}>
                <Plus className="size-4" />
                신규
              </Button>
              <Button variant="outline" size="sm" onClick={handleDelete} disabled={!selectedId}>
                <Trash2 className="size-4" />
                삭제
              </Button>
            </div>
          </div>
          <RestAgGridTemplate
            rows={rows}
            columnFields={config.columnFields}
            selectedId={selectedId}
            onSelect={handleSelect}
          />
          <div className="flex items-center justify-between border-t border-slate-200 px-5 py-3">
            <Button
              variant="outline"
              size="sm"
              disabled={pageInfo.page <= 0 || isLoading}
              onClick={() => void handleSearch(pageInfo.page - 1)}
            >
              이전
            </Button>
            <span className="text-sm text-slate-500">{pageInfo.page + 1} 페이지</span>
            <Button
              variant="outline"
              size="sm"
              disabled={rows.length < pageInfo.size || isLoading}
              onClick={() => void handleSearch(pageInfo.page + 1)}
            >
              다음
            </Button>
          </div>
        </div>

        <form className="rounded-lg border border-slate-200 bg-white" onSubmit={handleSubmit}>
          <div className="flex items-center justify-between border-b border-slate-200 px-5 py-4">
            <div>
              <h2 className="text-base font-semibold">{formTitle}</h2>
              <p className="mt-1 text-xs text-slate-500">
                선택 행 ID: {selectedId ?? "신규 입력"}
              </p>
            </div>
            <Pencil className="size-4 text-blue-600" />
          </div>
          <div className="space-y-3 p-5">
            {config.formFields.map((field) => (
              <FieldControl
                key={field.name}
                field={field}
                value={formValues[field.name]}
                onChange={(value) =>
                  setFormValues((current) => ({ ...current, [field.name]: value }))
                }
              />
            ))}

            {message && (
              <div className="flex items-center gap-2 rounded-lg border border-emerald-200 bg-emerald-50 px-3 py-2 text-sm text-emerald-700">
                <Check className="size-4" />
                {message}
              </div>
            )}
            {error && (
              <div className="rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
                {error}
              </div>
            )}

            <div className="flex gap-2 pt-2">
              <Button type="submit" disabled={isLoading}>
                {mode === "create" ? "생성" : "수정"}
              </Button>
              <Button type="button" variant="outline" onClick={handleNew}>
                취소
              </Button>
            </div>
          </div>
        </form>
      </section>
    </div>
  );
}

function FieldControl({
  field,
  value,
  onChange,
}: {
  field: FieldConfig;
  value: unknown;
  onChange: (value: string) => void;
}) {
  const fieldValue = value === undefined || value === null ? "" : String(value);

  return (
    <label className="block space-y-1.5">
      <span className="text-sm font-medium text-slate-700">{field.label}</span>
      {field.type === "textarea" ? (
        <textarea
          className="min-h-20 w-full rounded-lg border border-input bg-transparent px-2.5 py-2 text-sm outline-none transition-colors placeholder:text-muted-foreground focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50"
          value={fieldValue}
          onChange={(event) => onChange(event.target.value)}
          placeholder={field.placeholder}
          required={field.required}
        />
      ) : (
        <Input
          type={field.type ?? "text"}
          value={fieldValue}
          onChange={(event) => onChange(event.target.value)}
          placeholder={field.placeholder}
          autoComplete={
            field.type === "password"
              ? "new-password"
              : field.type === "email"
                ? "email"
                : "off"
          }
          required={field.required}
        />
      )}
    </label>
  );
}
