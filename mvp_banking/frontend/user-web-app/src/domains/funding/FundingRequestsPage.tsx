import { useEffect, useMemo, useState, type FormEvent } from "react";
import type { Account } from "../accounts/types";
import type { CreateFundingRequestPayload, FundingRequest } from "./types";
import type { LinkedBankAccount } from "../linked-bank-accounts/types";
import { formatAmount } from "../../shared/utils/format";

const FUNDING_CUTOFF_HOUR = 16;
const PRIORITY_FUNDING_CUTOFF_HOUR = 18;
const SAME_DAY_DEPOSIT_SETTLEMENT_HOUR = 17;
const SAME_DAY_WITHDRAWAL_SETTLEMENT_HOUR = 18;
const SAME_DAY_PRIORITY_DEPOSIT_SETTLEMENT_HOUR = 16;
const SAME_DAY_PRIORITY_DEPOSIT_SETTLEMENT_MINUTE = 30;
const SAME_DAY_PRIORITY_WITHDRAWAL_SETTLEMENT_HOUR = 17;
const NEXT_DAY_DEPOSIT_SETTLEMENT_HOUR = 10;
const NEXT_DAY_WITHDRAWAL_SETTLEMENT_HOUR = 11;
const KRW_DEPOSIT_DAILY_LIMIT = 20_000_000;
const FX_DEPOSIT_DAILY_LIMIT = 15_000;
const KRW_WITHDRAWAL_DAILY_LIMIT = 3_000_000;
const FX_WITHDRAWAL_DAILY_LIMIT = 2_500;
const KRW_DEPOSIT_REVIEW_THRESHOLD = 7_500_000;
const FX_DEPOSIT_REVIEW_THRESHOLD = 5_000;
const KRW_WITHDRAWAL_REVIEW_THRESHOLD = 1_000_000;
const FX_WITHDRAWAL_REVIEW_THRESHOLD = 1_000;
const KRW_WITHDRAWAL_SERVICE_FEE = 1_000;
const FX_WITHDRAWAL_SERVICE_FEE = 2.5;
const KRW_PRIORITY_FEE = 2_000;
const FX_PRIORITY_FEE = 3;

type FundingRequestsPageProps = {
  loading: boolean;
  submitting: boolean;
  cancelingRequestId: string | null;
  accounts: Account[];
  linkedBankAccounts: LinkedBankAccount[];
  fundingRequests: FundingRequest[];
  onCreate: (payload: CreateFundingRequestPayload) => Promise<void>;
  onCancel: (requestId: string, reason?: string) => Promise<void>;
};

export function FundingRequestsPage({
  loading,
  submitting,
  cancelingRequestId,
  accounts,
  linkedBankAccounts,
  fundingRequests,
  onCreate,
  onCancel,
}: FundingRequestsPageProps) {
  const activeAccounts = useMemo(
    () => accounts
      .filter((account) => account.status === "ACTIVE")
      .sort((left, right) => left.accountType.localeCompare(right.accountType) || left.accountNumber.localeCompare(right.accountNumber)),
    [accounts],
  );
  const [form, setForm] = useState({
    accountId: "",
    requestType: "DEPOSIT",
    priorityProcessing: false,
    linkedBankAccountId: "",
    amount: "1000000",
    note: "",
  });
  const [statusFilter, setStatusFilter] = useState("ALL");

  useEffect(() => {
    if (activeAccounts.length && !form.accountId) {
      setForm((current) => ({ ...current, accountId: activeAccounts[0].id }));
    }
  }, [activeAccounts, form.accountId]);

  const selectedAccount = activeAccounts.find((account) => account.id === form.accountId);
  const activeLinkedBankAccounts = linkedBankAccounts.filter((account) => account.status === "ACTIVE");
  const pendingLinkedBankAccounts = linkedBankAccounts.filter((account) => account.status === "PENDING_VERIFICATION");
  const expiredPendingLinkedBankAccounts = pendingLinkedBankAccounts.filter((account) => account.verificationExpired);
  const selectedLinkedBankAccount = activeLinkedBankAccounts.find((account) => account.id === form.linkedBankAccountId);
  const parsedAmount = Number(form.amount || 0);
  const isWithdrawal = form.requestType === "WITHDRAWAL";
  const priorityProcessing = form.priorityProcessing;
  const effectiveCurrency = selectedAccount?.currency ?? "KRW";
  const previewServiceFeeAmount = isWithdrawal ? resolveWithdrawalServiceFee(effectiveCurrency) : 0;
  const previewPriorityFeeAmount = isWithdrawal && priorityProcessing ? resolvePriorityFee(effectiveCurrency) : 0;
  const previewTotalFeeAmount = previewServiceFeeAmount + previewPriorityFeeAmount;
  const previewTotalDebitAmount = isWithdrawal ? parsedAmount + previewTotalFeeAmount : parsedAmount;
  const effectiveCutoffHour = priorityProcessing ? PRIORITY_FUNDING_CUTOFF_HOUR : FUNDING_CUTOFF_HOUR;
  const dailyLimit = resolveDailyLimit(isWithdrawal, effectiveCurrency);
  const reviewThreshold = resolveReviewThreshold(isWithdrawal, effectiveCurrency);
  const projectedBalance = selectedAccount
    ? isWithdrawal
      ? Number(selectedAccount.balance) - previewTotalDebitAmount
      : Number(selectedAccount.balance) + parsedAmount
    : null;
  const hasEnoughBalance = !selectedAccount || !isWithdrawal || Number(selectedAccount.balance) >= previewTotalDebitAmount;
  const canSubmit = Boolean(
    form.accountId
    && parsedAmount > 0
    && hasEnoughBalance
    && (!isWithdrawal || form.linkedBankAccountId),
  );
  const pendingCount = fundingRequests.filter((item) => item.status === "PENDING_APPROVAL").length;
  const canceledCount = fundingRequests.filter((item) => item.status === "CANCELED").length;
  const statusCounts = useMemo(
    () => fundingRequests.reduce<Record<string, number>>((accumulator, request) => {
      accumulator[request.status] = (accumulator[request.status] ?? 0) + 1;
      return accumulator;
    }, {}),
    [fundingRequests],
  );
  const filteredFundingRequests = useMemo(
    () => (statusFilter === "ALL"
      ? fundingRequests
      : fundingRequests.filter((item) => item.status === statusFilter)),
    [fundingRequests, statusFilter],
  );
  const sameTypeTodayAmount = useMemo(
    () => fundingRequests
      .filter((item) => item.requestType === form.requestType)
      .filter((item) => item.status === "PENDING_APPROVAL" || item.status === "APPROVED")
      .filter((item) => isSameLocalDay(item.createdAt))
      .reduce((sum, item) => sum + Number(item.amount), 0),
    [fundingRequests, form.requestType],
  );
  const projectedDailyTotal = sameTypeTodayAmount + (parsedAmount > 0 ? parsedAmount : 0);
  const dailyLimitExceededPreview = projectedDailyTotal > dailyLimit;
  const sameDaySettlementEligiblePreview = isSameDaySettlementEligibleNow(priorityProcessing);
  const expectedSettlementPreview = buildExpectedSettlementPreview(isWithdrawal, sameDaySettlementEligiblePreview, priorityProcessing);
  const previewReviewReasons = useMemo(() => {
    const reasons: string[] = [];
    if (parsedAmount >= reviewThreshold) {
      reasons.push(isWithdrawal ? "고액 출금 심사" : "고액 입금 확인");
    }
    if (dailyLimitExceededPreview) {
      reasons.push("일일 한도 초과");
    }
    if (priorityProcessing) {
      reasons.push("우선 처리 요청");
    }
    return reasons;
  }, [dailyLimitExceededPreview, isWithdrawal, parsedAmount, priorityProcessing, reviewThreshold]);
  const previewRequiresManualReview = previewReviewReasons.length > 0;

  useEffect(() => {
    if (isWithdrawal && !form.linkedBankAccountId && activeLinkedBankAccounts.length) {
      const primary = activeLinkedBankAccounts.find((account) => account.primaryWithdrawal) ?? activeLinkedBankAccounts[0];
      setForm((current) => ({ ...current, linkedBankAccountId: primary.id }));
    }
    if (!isWithdrawal && form.linkedBankAccountId) {
      setForm((current) => ({ ...current, linkedBankAccountId: "" }));
    }
  }, [isWithdrawal, activeLinkedBankAccounts, form.linkedBankAccountId]);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    await onCreate({
      accountId: form.accountId,
      requestType: form.requestType,
      amount: Number(form.amount),
      linkedBankAccountId: isWithdrawal ? form.linkedBankAccountId : undefined,
      priorityProcessing,
      note: form.note.trim() || undefined,
    });
    setForm((current) => ({
      ...current,
      linkedBankAccountId: isWithdrawal ? current.linkedBankAccountId : "",
      amount: "1000000",
      note: "",
    }));
  }

  async function requestCancel(request: FundingRequest) {
    if (!canCancelFundingRequest(request.status)) {
      return;
    }
    const reasonInput = window.prompt("취소 사유를 입력하세요. (선택)", "요청 계획 변경");
    if (reasonInput === null) {
      return;
    }
    await onCancel(request.id, reasonInput.trim() || undefined);
  }

  if (loading) {
    return (
      <section className="timeline-panel">
        <p>입출금 요청 정보를 불러오는 중입니다...</p>
      </section>
    );
  }

  return (
    <>
      <section className="summary-grid">
        <article className="summary-card">
          <span className="eyebrow">Active Cash Accounts</span>
          <strong>{activeAccounts.length}</strong>
          <p>입출금 요청 가능한 활성 계좌 수</p>
        </article>
        <article className="summary-card">
          <span className="eyebrow">Pending Requests</span>
          <strong>{pendingCount}</strong>
          <p>{`운영 승인 대기 ${pendingCount}건 / 사용자 취소 ${canceledCount}건`}</p>
        </article>
        <article className={`summary-card ${isWithdrawal && !hasEnoughBalance ? "negative" : ""}`}>
          <span className="eyebrow">Projected Balance</span>
          <strong>{selectedAccount && projectedBalance !== null ? formatAmount(projectedBalance, selectedAccount.currency) : "-"}</strong>
          <p>{isWithdrawal ? "출금 승인 후 예상 잔액" : "입금 승인 후 예상 잔액"}</p>
        </article>
        <article className={`summary-card ${previewRequiresManualReview ? "negative" : ""}`}>
          <span className="eyebrow">Policy Review</span>
          <strong>{previewRequiresManualReview ? "수동 심사 예상" : "자동 큐 진입"}</strong>
          <p>{previewRequiresManualReview ? previewReviewReasons.join(" / ") : "현재 입력 기준 추가 심사 사유 없음"}</p>
        </article>
      </section>

      <div className="page-convenience-strip">
        <div>
          <span>입출금 안내</span>
          <strong>{isWithdrawal ? "출금은 목적지 계좌와 총 차감액을 먼저 확인하세요." : "입금은 승인 후 계좌 잔액과 거래내역에 반영됩니다."}</strong>
        </div>
        <div>
          <span>오늘 누적 / 한도</span>
          <strong>{`${formatAmount(projectedDailyTotal, effectiveCurrency)} / ${formatAmount(dailyLimit, effectiveCurrency)}`}</strong>
        </div>
      </div>

      <section className="timeline-panel asset-panel">
        <div className="section-header compact">
          <div>
            <p className="eyebrow">Funding Desk</p>
            <h2>입출금 요청</h2>
            <p className="section-copy">
              계좌 선택, 정산 경로, 정책 플래그를 먼저 확인한 뒤 요청을 제출하세요. 실제 운영 환경과 같은 승인형 플로우로 동작합니다.
            </p>
          </div>
        </div>
        <div className="trade-route-strip funding-route-strip">
          <div>
            <span className="trade-kicker">Source</span>
            <strong>{selectedAccount ? `${selectedAccount.accountNumber} / ${selectedAccount.currency}` : "정산 계좌 선택"}</strong>
            <p>{isWithdrawal ? "내 계좌에서 외부 계좌로 정산됩니다." : "운영 승인 후 선택 계좌로 입금 반영됩니다."}</p>
          </div>
          <div className={`trade-rate-chip ${isWithdrawal ? "" : "muted"}`}>
            <span>{isWithdrawal ? "Withdrawal Route" : "Deposit Route"}</span>
            <strong>
              {isWithdrawal
                ? selectedLinkedBankAccount
                  ? `${selectedLinkedBankAccount.bankName} / ${selectedLinkedBankAccount.maskedAccountNumber}`
                  : "외부 목적지 계좌 선택"
                : "외부 입금 확인 후 계좌 반영"}
            </strong>
          </div>
        </div>
        <form className="trade-ticket" onSubmit={handleSubmit}>
          <div className="trade-ticket-grid">
            <div className="trade-ticket-main">
              <article className="trade-leg-card">
                <div className="trade-leg-header">
                  <div>
                    <span className="trade-kicker">Account</span>
                    <strong>정산 계좌</strong>
                  </div>
                  {selectedAccount ? <b className={`status-pill ${selectedAccount.status.toLowerCase()}`}>{selectedAccount.status}</b> : null}
                </div>
                <label className="form-field">
                  <span>어느 계좌로 요청할까요?</span>
                  <select value={form.accountId} onChange={(event) => setForm((current) => ({ ...current, accountId: event.target.value }))} required>
                    <option value="">계좌 선택</option>
                    {activeAccounts.map((account) => (
                      <option key={account.id} value={account.id}>
                        {account.accountNumber} / {account.accountType} / {account.currency}
                      </option>
                    ))}
                  </select>
                </label>
                <div className="trade-leg-meta">
                  <div>
                    <span>계좌번호</span>
                    <strong>{selectedAccount?.accountNumber ?? "선택 필요"}</strong>
                  </div>
                  <div>
                    <span>현재 잔액</span>
                    <strong>{selectedAccount ? formatAmount(selectedAccount.balance, selectedAccount.currency) : "-"}</strong>
                  </div>
                </div>
              </article>

              <div className="trade-form-grid">
                <div className="form-field">
                  <span>요청 유형</span>
                  <div className="trade-toggle-row">
                    <button
                      type="button"
                      className={form.requestType === "DEPOSIT" ? "active" : ""}
                      onClick={() => setForm((current) => ({ ...current, requestType: "DEPOSIT" }))}
                    >
                      입금
                    </button>
                    <button
                      type="button"
                      className={form.requestType === "WITHDRAWAL" ? "active" : ""}
                      onClick={() => setForm((current) => ({ ...current, requestType: "WITHDRAWAL" }))}
                    >
                      출금
                    </button>
                  </div>
                </div>
                <label className="form-field">
                  <span>요청 금액</span>
                  <input
                    type="number"
                    min="0.0001"
                    step="0.0001"
                    value={form.amount}
                    onChange={(event) => setForm((current) => ({ ...current, amount: event.target.value }))}
                    placeholder="예: 1000000"
                    required
                  />
                  <small className="form-helper">
                    {selectedAccount
                      ? isWithdrawal
                        ? `${selectedAccount.currency} 기준 출금 금액입니다. 수수료 ${formatAmount(previewServiceFeeAmount, selectedAccount.currency)}가 별도 차감됩니다.`
                        : `${selectedAccount.currency} 기준 금액입니다.`
                      : "계좌 통화 기준으로 입력하세요."}
                  </small>
                </label>
              </div>

              <article className={`priority-toggle-card ${priorityProcessing ? "enabled" : ""}`}>
                <div className="priority-toggle-header">
                  <div>
                    <span className="trade-kicker">Priority Processing</span>
                    <strong>{priorityProcessing ? "우선 처리 ON" : "기본 처리"}</strong>
                  </div>
                  <button
                    type="button"
                    className={`priority-toggle-button ${priorityProcessing ? "enabled" : ""}`}
                    onClick={() => setForm((current) => ({ ...current, priorityProcessing: !current.priorityProcessing }))}
                  >
                    {priorityProcessing ? "ON" : "OFF"}
                  </button>
                </div>
                <p>
                  {priorityProcessing
                    ? `컷오프를 ${PRIORITY_FUNDING_CUTOFF_HOUR}:00까지 확장하여 당일 정산 가능성을 높입니다.`
                    : `기본 컷오프 ${FUNDING_CUTOFF_HOUR}:00 기준으로 정산됩니다.`}
                </p>
                <div className="priority-toggle-meta">
                  <div>
                    <span>컷오프</span>
                    <strong>{`${effectiveCutoffHour}:00`}</strong>
                  </div>
                  <div>
                    <span>우선 수수료</span>
                    <strong>{isWithdrawal ? formatAmount(previewPriorityFeeAmount, effectiveCurrency) : formatAmount(0, effectiveCurrency)}</strong>
                  </div>
                  <div>
                    <span>심사 플래그</span>
                    <strong>{priorityProcessing ? "우선 처리 요청" : "일반 처리"}</strong>
                  </div>
                </div>
              </article>

              <div className="funding-policy-strip">
                <article className={`policy-chip ${sameDaySettlementEligiblePreview ? "positive" : "warning"}`}>
                  <span>정산 윈도우</span>
                  <strong>{sameDaySettlementEligiblePreview ? "당일 정산 가능" : "다음 영업일 정산"}</strong>
                  <p>{expectedSettlementPreview}</p>
                </article>
                <article className={`policy-chip ${priorityProcessing ? "neutral" : "positive"}`}>
                  <span>처리 모드</span>
                  <strong>{priorityProcessing ? "우선 처리" : "기본 처리"}</strong>
                  <p>{`당일 정산 컷오프 ${effectiveCutoffHour}:00`}</p>
                </article>
                <article className={`policy-chip ${dailyLimitExceededPreview ? "negative" : "neutral"}`}>
                  <span>일일 한도</span>
                  <strong>{formatAmount(projectedDailyTotal, selectedAccount?.currency ?? "KRW")}</strong>
                  <p>
                    현재 누적 {formatAmount(sameTypeTodayAmount, effectiveCurrency)} / 한도 {formatAmount(dailyLimit, effectiveCurrency)}
                  </p>
                </article>
                <article className={`policy-chip ${previewRequiresManualReview ? "warning" : "positive"}`}>
                  <span>심사 플래그</span>
                  <strong>{previewRequiresManualReview ? "추가 심사 필요" : "기본 심사"}</strong>
                  <p>{previewRequiresManualReview ? previewReviewReasons.join(" / ") : "일반 승인 큐로 전달됩니다."}</p>
                </article>
                <article className={`policy-chip ${isWithdrawal && previewTotalFeeAmount > 0 ? "neutral" : "positive"}`}>
                  <span>수수료 스냅샷</span>
                  <strong>{formatAmount(previewTotalFeeAmount, effectiveCurrency)}</strong>
                  <p>
                    {isWithdrawal
                      ? `기본 ${formatAmount(previewServiceFeeAmount, effectiveCurrency)} + 우선 ${formatAmount(previewPriorityFeeAmount, effectiveCurrency)}`
                      : "입금 요청은 수수료가 없습니다."}
                  </p>
                </article>
              </div>

              {isWithdrawal ? (
                <article className="trade-leg-card">
                  <div className="trade-leg-header">
                    <div>
                      <span className="trade-kicker">Destination</span>
                      <strong>출금 목적지 계좌</strong>
                    </div>
                    {selectedLinkedBankAccount?.primaryWithdrawal ? <b className="status-pill active">PRIMARY</b> : null}
                  </div>
                  <label className="form-field">
                    <span>어느 외부 계좌로 보낼까요?</span>
                    <select
                      value={form.linkedBankAccountId}
                      onChange={(event) => setForm((current) => ({ ...current, linkedBankAccountId: event.target.value }))}
                      required={isWithdrawal}
                    >
                      <option value="">출금 목적지 선택</option>
                      {activeLinkedBankAccounts.map((account) => (
                        <option key={account.id} value={account.id}>
                          {account.bankName} / {account.accountAlias} / {account.maskedAccountNumber}
                        </option>
                      ))}
                    </select>
                  </label>
                  <div className="trade-leg-meta">
                    <div>
                      <span>예금주</span>
                      <strong>{selectedLinkedBankAccount?.accountHolderName ?? "선택 필요"}</strong>
                    </div>
                    <div>
                      <span>목적지</span>
                      <strong>
                        {selectedLinkedBankAccount
                          ? `${selectedLinkedBankAccount.bankName} / ${selectedLinkedBankAccount.maskedAccountNumber}`
                          : "등록된 활성 연결 계좌 선택"}
                      </strong>
                    </div>
                  </div>
                </article>
              ) : null}

              <label className="form-field">
                <span>메모 / 요청 배경</span>
                <input
                  value={form.note}
                  onChange={(event) => setForm((current) => ({ ...current, note: event.target.value }))}
                  placeholder="예: 생활비 출금, 추가 투자 예탁금 입금"
                  maxLength={255}
                />
              </label>
            </div>

            <aside className="trade-summary-card">
              <span className="trade-kicker">Funding Ticket</span>
              <h3>요청 요약</h3>
              <div className="trade-summary-list">
                <div>
                  <span>요청 유형</span>
                  <strong>{form.requestType === "WITHDRAWAL" ? "출금" : "입금"}</strong>
                </div>
                <div>
                  <span>정산 계좌</span>
                  <strong>{selectedAccount ? `${selectedAccount.accountNumber} / ${selectedAccount.accountType}` : "선택 필요"}</strong>
                </div>
                {isWithdrawal ? (
                  <div>
                    <span>출금 목적지</span>
                    <strong>
                      {selectedLinkedBankAccount
                        ? `${selectedLinkedBankAccount.bankName} / ${selectedLinkedBankAccount.maskedAccountNumber}`
                        : "선택 필요"}
                    </strong>
                  </div>
                ) : null}
                <div>
                  <span>현재 잔액</span>
                  <strong>{selectedAccount ? formatAmount(selectedAccount.balance, selectedAccount.currency) : "-"}</strong>
                </div>
                <div>
                  <span>{isWithdrawal ? "출금 예정" : "입금 예정"}</span>
                  <strong>{selectedAccount ? formatAmount(parsedAmount || 0, selectedAccount.currency) : "-"}</strong>
                </div>
                <div>
                  <span>예상 수수료</span>
                  <strong>{selectedAccount ? formatAmount(previewTotalFeeAmount, selectedAccount.currency) : "-"}</strong>
                </div>
                <div>
                  <span>우선처리 수수료</span>
                  <strong>{selectedAccount ? formatAmount(previewPriorityFeeAmount, selectedAccount.currency) : "-"}</strong>
                </div>
                <div>
                  <span>처리 모드</span>
                  <strong>{priorityProcessing ? "우선 처리" : "기본 처리"}</strong>
                </div>
                <div>
                  <span>{isWithdrawal ? "총 차감 예정" : "총 반영 예정"}</span>
                  <strong>{selectedAccount ? formatAmount(previewTotalDebitAmount || 0, selectedAccount.currency) : "-"}</strong>
                </div>
                <div>
                  <span>예상 정산</span>
                  <strong>{expectedSettlementPreview}</strong>
                </div>
                <div>
                  <span>승인 후 예상 잔액</span>
                  <strong>{selectedAccount && projectedBalance !== null ? formatAmount(projectedBalance, selectedAccount.currency) : "-"}</strong>
                </div>
                <div>
                  <span>운영 처리</span>
                  <strong>승인 후 거래 원장 자동 반영</strong>
                </div>
              </div>
              {!hasEnoughBalance ? <p className="trade-summary-note warning">현재 잔액보다 큰 출금 요청입니다. (수수료 포함 차감 기준)</p> : null}
              {isWithdrawal && !activeLinkedBankAccounts.length ? <p className="trade-summary-note warning">출금 전용 연결 계좌를 먼저 등록해야 합니다.</p> : null}
              {isWithdrawal && pendingLinkedBankAccounts.length ? (
                <p className="trade-summary-note">
                  검증 대기 계좌 {pendingLinkedBankAccounts.length}건은 아직 출금 목적지로 선택할 수 없습니다. {pendingLinkedBankAccounts.map((account) => account.accountAlias).join(", ")} 계좌부터 인증을 완료해 주세요.
                  {expiredPendingLinkedBankAccounts.length ? ` 현재 ${expiredPendingLinkedBankAccounts.length}건은 인증 문구가 만료되어 재발송이 필요합니다.` : ""}
                </p>
              ) : null}
              {previewRequiresManualReview ? <p className="trade-summary-note warning">현재 입력 기준으로 추가 심사 사유가 감지되었습니다.</p> : null}
              <button type="submit" className="primary-button trade-submit-button" disabled={submitting || !canSubmit}>
                {submitting ? "요청 중..." : "입출금 요청"}
              </button>
              <p className="trade-summary-note">요청 후 운영 승인 절차를 거쳐 계좌와 거래내역에 반영됩니다.</p>
            </aside>
          </div>
        </form>
        <div className="funding-guidance-grid">
          <article className="funding-guidance-card">
            <span className="trade-kicker">Routing Rules</span>
            <strong>출금 목적지는 활성 연결 계좌만 사용</strong>
            <p>차단된 외부 계좌는 티켓에서 자동 제외되며, 기본 출금 계좌가 있으면 우선 선택됩니다.</p>
          </article>
          <article className="funding-guidance-card">
            <span className="trade-kicker">Ops Settlement</span>
            <strong>요청 후 운영 승인과 원장 반영</strong>
            <p>승인 완료 시 정산 거래번호가 생성되고, 계좌 잔액과 입출금 내역에 동시에 반영됩니다.</p>
          </article>
        </div>
      </section>

      <section className="timeline-panel">
        <div className="section-header">
          <div>
            <p className="eyebrow">Funding Requests</p>
            <h2>내 입출금 요청</h2>
            <p className="section-copy">상태, 심사 사유, 정산 거래번호를 한 번에 볼 수 있도록 표 중심으로 정리했습니다.</p>
          </div>
          <div className="filter-cluster">
            <select
              className="inline-filter"
              value={statusFilter}
              onChange={(event) => setStatusFilter(event.target.value)}
              aria-label="입출금 상태 필터"
            >
              <option value="ALL">{`전체 (${fundingRequests.length})`}</option>
              <option value="PENDING_APPROVAL">{`승인대기 (${statusCounts.PENDING_APPROVAL ?? 0})`}</option>
              <option value="APPROVED">{`승인완료 (${statusCounts.APPROVED ?? 0})`}</option>
              <option value="REJECTED">{`반려 (${statusCounts.REJECTED ?? 0})`}</option>
              <option value="CANCELED">{`취소 (${statusCounts.CANCELED ?? 0})`}</option>
            </select>
          </div>
        </div>
        <div className="table-shell">
          <table className="data-table">
            <colgroup>
              <col style={{ width: 180 }} />
              <col style={{ width: 190 }} />
              <col style={{ width: 210 }} />
              <col style={{ width: 240 }} />
              <col style={{ width: 190 }} />
              <col style={{ width: 210 }} />
              <col style={{ width: 190 }} />
              <col style={{ width: 220 }} />
              <col style={{ width: 180 }} />
            </colgroup>
            <thead>
              <tr>
                <th>요청번호</th>
                <th>계좌</th>
                <th>유형</th>
                <th>금액 / 스냅샷</th>
                <th>외부 목적지</th>
                <th>정책 / 심사</th>
                <th>상태</th>
                <th>정산 거래</th>
                <th>요청 메모</th>
                <th>액션</th>
              </tr>
            </thead>
            <tbody>
              {filteredFundingRequests.length ? (
                filteredFundingRequests.map((item) => (
                  <tr key={item.id}>
                    <td className="table-mono">{item.requestNumber}</td>
                    <td>
                      <div className="table-cell-stack">
                        <strong>{item.accountNumber}</strong>
                        <p>{`${item.accountType} / ${item.currency}`}</p>
                      </div>
                    </td>
                    <td>
                      <b className={`status-pill ${item.requestType === "WITHDRAWAL" ? "rejected" : "approved"}`}>
                        {item.requestType}
                      </b>
                      {item.priorityProcessing ? <p className="table-priority-text">PRIORITY</p> : null}
                    </td>
                    <td>
                      <div className="table-cell-stack">
                        <strong>{formatAmount(item.amount, item.currency)}</strong>
                        <p>{`base fee ${formatAmount(item.serviceFeeAmount, item.currency)} / priority fee ${formatAmount(item.priorityFeeAmount, item.currency)}`}</p>
                        <p>{`total debit ${formatAmount(item.totalDebitAmount, item.currency)}`}</p>
                        <p>{`snapshot ${formatAmount(item.balanceSnapshot, item.currency)}`}</p>
                      </div>
                    </td>
                    <td>
                      {item.requestType === "WITHDRAWAL" ? (
                        <div className="table-cell-stack">
                          <strong>{item.linkedBankName ?? "미지정"}</strong>
                          <p>{item.linkedBankAccountAlias ?? item.linkedBankAccountNumberMasked ?? "-"}</p>
                        </div>
                      ) : (
                        "-"
                      )}
                    </td>
                    <td>
                      <div className="table-cell-stack">
                        <strong>{item.expectedSettlementAt ? new Date(item.expectedSettlementAt).toLocaleString() : "-"}</strong>
                        <p>
                          {item.status === "CANCELED"
                            ? item.cancellationReason ?? "사용자 요청 취소"
                            : item.manualReviewRequired
                            ? item.manualReviewReason ?? "추가 심사"
                            : item.sameDaySettlementEligible
                              ? "당일 정산 대상"
                              : "다음 영업일 정산"}
                        </p>
                        {item.priorityProcessing ? <p>우선 처리 요청</p> : null}
                        {item.dailyLimitAmount !== null && item.dailyAccumulatedAmount !== null ? (
                          <p>
                            {`누적 ${formatAmount(item.dailyAccumulatedAmount, item.currency)} / 한도 ${formatAmount(item.dailyLimitAmount, item.currency)}`}
                          </p>
                        ) : null}
                      </div>
                    </td>
                    <td>
                      <b className={`status-pill ${item.status.toLowerCase()}`}>{item.status}</b>
                    </td>
                    <td>
                      <div className="table-cell-stack">
                        <strong className="table-mono">{item.settlementTransactionNumber ?? "정산 대기"}</strong>
                        <p>
                          {item.status === "CANCELED" && item.canceledAt
                            ? `취소 ${new Date(item.canceledAt).toLocaleString()}`
                            : item.settledAt
                              ? new Date(item.settledAt).toLocaleString()
                              : new Date(item.createdAt).toLocaleString()}
                        </p>
                      </div>
                    </td>
                    <td>{item.note ?? "-"}</td>
                    <td>
                      {canCancelFundingRequest(item.status) ? (
                        <button
                          type="button"
                          className="secondary-button compact"
                          disabled={cancelingRequestId === item.id}
                          onClick={() => void requestCancel(item)}
                        >
                          {cancelingRequestId === item.id ? "취소 중..." : "요청 취소"}
                        </button>
                      ) : (
                        <span className="table-muted">-</span>
                      )}
                    </td>
                  </tr>
                ))
              ) : (
                <tr className="table-empty-row">
                  <td colSpan={10}>
                    <strong>{statusFilter === "ALL" ? "입출금 요청 내역이 없습니다." : "선택한 상태의 요청이 없습니다."}</strong>
                    <p>{statusFilter === "ALL" ? "첫 요청을 등록하면 운영 승인 상태를 여기서 추적할 수 있습니다." : "다른 상태 필터로 전환해 요청 이력을 확인하세요."}</p>
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </section>
    </>
  );
}

function isSameDaySettlementEligibleNow(priorityProcessing: boolean) {
  const now = new Date();
  const day = now.getDay();
  if (day === 0 || day === 6) {
    return false;
  }
  const cutoffHour = priorityProcessing ? PRIORITY_FUNDING_CUTOFF_HOUR : FUNDING_CUTOFF_HOUR;
  return now.getHours() < cutoffHour;
}

function buildExpectedSettlementPreview(
  isWithdrawal: boolean,
  sameDaySettlementEligible: boolean,
  priorityProcessing: boolean,
) {
  const base = new Date();
  const settlement = sameDaySettlementEligible
    ? setTime(
      base,
      resolveSameDaySettlementHour(isWithdrawal, priorityProcessing),
      resolveSameDaySettlementMinute(isWithdrawal, priorityProcessing),
    )
    : setTime(
      nextBusinessDay(base),
      isWithdrawal ? NEXT_DAY_WITHDRAWAL_SETTLEMENT_HOUR : NEXT_DAY_DEPOSIT_SETTLEMENT_HOUR,
    );
  return settlement.toLocaleString();
}

function nextBusinessDay(base: Date) {
  const next = new Date(base);
  next.setDate(next.getDate() + 1);
  while (next.getDay() === 0 || next.getDay() === 6) {
    next.setDate(next.getDate() + 1);
  }
  return next;
}

function setTime(base: Date, hour: number, minute = 0) {
  const next = new Date(base);
  next.setHours(hour, minute, 0, 0);
  return next;
}

function isSameLocalDay(isoDateTime: string) {
  const target = new Date(isoDateTime);
  const now = new Date();
  return target.getFullYear() === now.getFullYear()
    && target.getMonth() === now.getMonth()
    && target.getDate() === now.getDate();
}

function resolveDailyLimit(isWithdrawal: boolean, currency: string) {
  const krw = currency.toUpperCase() === "KRW";
  if (isWithdrawal) {
    return krw ? KRW_WITHDRAWAL_DAILY_LIMIT : FX_WITHDRAWAL_DAILY_LIMIT;
  }
  return krw ? KRW_DEPOSIT_DAILY_LIMIT : FX_DEPOSIT_DAILY_LIMIT;
}

function resolveReviewThreshold(isWithdrawal: boolean, currency: string) {
  const krw = currency.toUpperCase() === "KRW";
  if (isWithdrawal) {
    return krw ? KRW_WITHDRAWAL_REVIEW_THRESHOLD : FX_WITHDRAWAL_REVIEW_THRESHOLD;
  }
  return krw ? KRW_DEPOSIT_REVIEW_THRESHOLD : FX_DEPOSIT_REVIEW_THRESHOLD;
}

function resolveWithdrawalServiceFee(currency: string) {
  return currency.toUpperCase() === "KRW" ? KRW_WITHDRAWAL_SERVICE_FEE : FX_WITHDRAWAL_SERVICE_FEE;
}

function resolvePriorityFee(currency: string) {
  return currency.toUpperCase() === "KRW" ? KRW_PRIORITY_FEE : FX_PRIORITY_FEE;
}

function resolveSameDaySettlementHour(isWithdrawal: boolean, priorityProcessing: boolean) {
  if (!priorityProcessing) {
    return isWithdrawal ? SAME_DAY_WITHDRAWAL_SETTLEMENT_HOUR : SAME_DAY_DEPOSIT_SETTLEMENT_HOUR;
  }
  return isWithdrawal ? SAME_DAY_PRIORITY_WITHDRAWAL_SETTLEMENT_HOUR : SAME_DAY_PRIORITY_DEPOSIT_SETTLEMENT_HOUR;
}

function resolveSameDaySettlementMinute(isWithdrawal: boolean, priorityProcessing: boolean) {
  if (!priorityProcessing) {
    return 0;
  }
  return isWithdrawal ? 0 : SAME_DAY_PRIORITY_DEPOSIT_SETTLEMENT_MINUTE;
}

function canCancelFundingRequest(status: string) {
  return status === "PENDING_APPROVAL";
}
