import type { DomainConfig } from "@/types/admin-api";

export const domainConfigs: DomainConfig[] = [
  {
    key: "admin",
    label: "관리자",
    description: "운영자 계정 생성, 검색, 수정, 삭제를 관리합니다.",
    searchFields: [
      { name: "email", label: "이메일", type: "email", placeholder: "admin@example.com" },
      { name: "name", label: "이름", placeholder: "관리자명" },
    ],
    formFields: [
      { name: "email", label: "이메일", type: "email", required: true },
      { name: "name", label: "이름", required: true },
      { name: "password", label: "비밀번호", type: "password", required: true },
    ],
    columnFields: ["id", "email", "name", "createdAt", "updatedAt"],
  },
  {
    key: "account",
    label: "계좌",
    description: "계좌 정보를 검색하고 등록 데이터를 운영합니다.",
    searchFields: [
      { name: "accountNumber", label: "계좌번호", placeholder: "계좌번호" },
      { name: "name", label: "예금주", placeholder: "예금주" },
      { name: "status", label: "상태", placeholder: "ACTIVE" },
    ],
    formFields: [
      { name: "accountNumber", label: "계좌번호", required: true },
      { name: "name", label: "예금주", required: true },
      { name: "balance", label: "잔액", type: "number" },
      { name: "status", label: "상태" },
    ],
    columnFields: ["id", "accountNumber", "name", "balance", "status", "createdAt"],
  },
  {
    key: "accounttransaction",
    label: "계좌 거래",
    description: "입출금 거래 이력을 검색하고 보정 데이터를 관리합니다.",
    searchFields: [
      { name: "accountId", label: "계좌 ID", type: "number", placeholder: "계좌 ID" },
      { name: "transactionType", label: "거래 유형", placeholder: "DEPOSIT" },
      { name: "fromDate", label: "시작일", type: "date" },
      { name: "toDate", label: "종료일", type: "date" },
    ],
    formFields: [
      { name: "accountId", label: "계좌 ID", type: "number", required: true },
      { name: "transactionType", label: "거래 유형", required: true },
      { name: "amount", label: "금액", type: "number", required: true },
      { name: "memo", label: "메모", type: "textarea" },
    ],
    columnFields: ["id", "accountId", "transactionType", "amount", "memo", "createdAt"],
  },
];
