import { useState, type FormEvent } from "react";
import type { CreateLinkedBankAccountPayload, LinkedBankAccount } from "./types";

type LinkedBankAccountsPageProps = {
  loading: boolean;
  submitting: boolean;
  verifyingId: string | null;
  resendingId: string | null;
  linkedBankAccounts: LinkedBankAccount[];
  onCreate: (payload: CreateLinkedBankAccountPayload) => Promise<void>;
  onMarkPrimary: (linkedBankAccountId: string) => Promise<void>;
  onVerify: (linkedBankAccountId: string, verificationReference: string) => Promise<void>;
  onResend: (linkedBankAccountId: string) => Promise<void>;
};

export function LinkedBankAccountsPage({
  loading,
  submitting,
  verifyingId,
  resendingId,
  linkedBankAccounts,
  onCreate,
  onMarkPrimary,
  onVerify,
  onResend,
}: LinkedBankAccountsPageProps) {
  const [form, setForm] = useState({
    bankName: "Shinhan Bank",
    accountAlias: "주거래 출금 계좌",
    accountHolderName: "MVP User",
    accountNumber: "110-222-333333",
    primaryWithdrawal: true,
  });
  const [verificationInputs, setVerificationInputs] = useState<Record<string, string>>({});

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    await onCreate(form);
    setForm((current) => ({
      ...current,
      accountAlias: "",
      accountNumber: "",
      primaryWithdrawal: false,
    }));
  }

  if (loading) {
    return (
      <section className="timeline-panel">
        <p>연결 계좌 정보를 불러오는 중입니다...</p>
      </section>
    );
  }

  const activeCount = linkedBankAccounts.filter((item) => item.status === "ACTIVE").length;
  const pendingAccounts = linkedBankAccounts
    .filter((item) => item.status === "PENDING_VERIFICATION")
    .sort((left, right) => Number(right.verificationExpired) - Number(left.verificationExpired));
  const pendingCount = pendingAccounts.length;
  const expiredPendingCount = pendingAccounts.filter((item) => item.verificationExpired).length;
  const blockedAccounts = linkedBankAccounts.filter((item) => item.status === "BLOCKED");
  const primaryCount = linkedBankAccounts.filter((item) => item.status === "ACTIVE" && item.primaryWithdrawal).length;
  const blockedCount = linkedBankAccounts.filter((item) => item.status === "BLOCKED").length;
  const primaryAccount = linkedBankAccounts.find((item) => item.status === "ACTIVE" && item.primaryWithdrawal);

  async function handleVerify(linkedBankAccount: LinkedBankAccount) {
    const verificationReference = (verificationInputs[linkedBankAccount.id] ?? linkedBankAccount.verificationReference ?? "").trim();
    await onVerify(linkedBankAccount.id, verificationReference);
    setVerificationInputs((current) => ({
      ...current,
      [linkedBankAccount.id]: "",
    }));
  }

  return (
    <>
      <section className="summary-grid">
        <article className="summary-card">
          <span className="eyebrow">Active Linked Accounts</span>
          <strong>{activeCount}</strong>
          <p>출금 목적지로 사용할 수 있는 활성 외부 계좌 수</p>
        </article>
        <article className="summary-card">
          <span className="eyebrow">Primary Withdrawal</span>
          <strong>{primaryCount}</strong>
          <p>기본 출금 계좌로 지정된 연결 계좌 수</p>
        </article>
        <article className="summary-card">
          <span className="eyebrow">Pending Verification</span>
          <strong>{pendingCount}</strong>
          <p>운영 검증 후 출금 목적지로 활성화될 대기 계좌 수</p>
        </article>
        <article className={`summary-card ${blockedCount ? "negative" : ""}`}>
          <span className="eyebrow">Blocked Accounts</span>
          <strong>{blockedCount}</strong>
          <p>출금 목적지에서 제외된 차단 계좌 수</p>
        </article>
      </section>

      <section className="timeline-panel">
        <div className="linked-bank-hero">
          <div>
            <p className="eyebrow">Primary Withdrawal Route</p>
            <h2>현재 기본 출금 계좌</h2>
            <p className="section-copy">
              출금 요청 티켓에서는 활성 상태의 기본 계좌가 우선 선택됩니다. 새로 등록한 계좌는 운영 검증 후 목적지로 사용할 수 있습니다.
            </p>
          </div>
          <div className="linked-bank-highlight">
            {primaryAccount ? (
              <>
                <strong>{primaryAccount.bankName}</strong>
                <span>{primaryAccount.accountAlias}</span>
                <code>{primaryAccount.maskedAccountNumber}</code>
              </>
            ) : (
              <>
                <strong>활성 기본 출금 계좌 없음</strong>
                <span>활성 연결 계좌를 등록하거나 검증 완료 후 기본 계좌로 지정하세요.</span>
              </>
            )}
          </div>
        </div>
      </section>

      <section className="timeline-panel asset-panel">
        <div className="section-header compact">
          <div>
            <p className="eyebrow">Linked Bank Accounts</p>
            <h2>출금 연결 계좌 관리</h2>
          </div>
        </div>
        <form className="trade-ticket linked-bank-ticket" onSubmit={handleSubmit}>
          <div className="trade-ticket-grid">
            <div className="trade-ticket-main">
              <div className="trade-form-grid">
                <label className="form-field">
                  <span>은행명</span>
                  <input value={form.bankName} onChange={(event) => setForm((current) => ({ ...current, bankName: event.target.value }))} required />
                </label>
                <label className="form-field">
                  <span>계좌 별칭</span>
                  <input value={form.accountAlias} onChange={(event) => setForm((current) => ({ ...current, accountAlias: event.target.value }))} required />
                </label>
              </div>
              <div className="trade-form-grid">
                <label className="form-field">
                  <span>예금주명</span>
                  <input value={form.accountHolderName} onChange={(event) => setForm((current) => ({ ...current, accountHolderName: event.target.value }))} required />
                </label>
                <label className="form-field">
                  <span>계좌번호</span>
                  <input value={form.accountNumber} onChange={(event) => setForm((current) => ({ ...current, accountNumber: event.target.value }))} required />
                </label>
              </div>
              <div className="form-field">
                <span>기본 출금 계좌 여부</span>
                <div className="trade-toggle-row single">
                  <button
                    type="button"
                    className={form.primaryWithdrawal ? "active" : ""}
                    onClick={() => setForm((current) => ({ ...current, primaryWithdrawal: !current.primaryWithdrawal }))}
                  >
                    {form.primaryWithdrawal ? "PRIMARY ON" : "PRIMARY OFF"}
                  </button>
                </div>
              </div>
            </div>

            <aside className="trade-summary-card">
              <span className="trade-kicker">External Settlement</span>
              <h3>등록 요약</h3>
              <div className="trade-summary-list">
                <div>
                  <span>은행명</span>
                  <strong>{form.bankName || "-"}</strong>
                </div>
                <div>
                  <span>별칭</span>
                  <strong>{form.accountAlias || "-"}</strong>
                </div>
                <div>
                  <span>예금주명</span>
                  <strong>{form.accountHolderName || "-"}</strong>
                </div>
                <div>
                  <span>기본 출금</span>
                  <strong>{form.primaryWithdrawal ? "예" : "아니오"}</strong>
                </div>
              </div>
              <button type="submit" className="primary-button trade-submit-button" disabled={submitting}>
                {submitting ? "등록 중..." : "연결 계좌 등록"}
              </button>
              <p className="trade-summary-note">등록 후 운영 검증 대기 상태로 저장되며, 활성화 완료 후 출금 티켓의 목적지 후보로 표시됩니다.</p>
            </aside>
          </div>
        </form>
      </section>

      {pendingCount ? (
        <section className="timeline-panel">
          <div className="pending-verification-banner">
            <div>
              <p className="eyebrow">Verification Queue</p>
              <strong>운영 검증 대기 계좌 {pendingCount}건</strong>
              <p>대기 상태 계좌는 출금 요청 목적지에서 제외됩니다. 데모 환경에서는 소액이체 인증 문구를 제출하면 바로 활성화되고, 만료된 계좌는 새 인증 문구를 다시 받아야 합니다.</p>
            </div>
            <div className="pending-verification-meta">
              <div>
                <span>활성 대기</span>
                <strong>{pendingCount - expiredPendingCount}건</strong>
              </div>
              <div>
                <span>만료 대기</span>
                <strong>{expiredPendingCount}건</strong>
              </div>
            </div>
          </div>
        </section>
      ) : null}

      {blockedAccounts.length ? (
        <section className="timeline-panel">
          <div className="section-header">
            <div>
              <p className="eyebrow">Blocked Accounts</p>
              <h2>출금 목적지에서 제외된 계좌</h2>
            </div>
          </div>
          <div className="verification-ticket-grid">
            {blockedAccounts.map((item) => (
              <article key={item.id} className="verification-ticket-card blocked-ticket-card">
                <div className="verification-ticket-header">
                  <div>
                    <p className="eyebrow">Blocked Route</p>
                    <strong>{item.bankName} / {item.accountAlias}</strong>
                    <p>{item.maskedAccountNumber} · {item.accountHolderName}</p>
                  </div>
                  <b className="status-pill rejected">BLOCKED</b>
                </div>
                <div className="verification-ticket-summary">
                  <div>
                    <span>차단 사유</span>
                    <strong>{renderBlockReason(item.blockReasonCode)}</strong>
                  </div>
                  <div>
                    <span>차단 시각</span>
                    <strong>{item.blockedAt ? new Date(item.blockedAt).toLocaleString() : "기록 없음"}</strong>
                  </div>
                  <div>
                    <span>기본 출금 지정</span>
                    <strong>{item.primaryWithdrawal ? "이전 기본 계좌" : "일반 목적지"}</strong>
                  </div>
                  <div>
                    <span>다음 조치</span>
                    <strong>{renderBlockedNextAction(item.blockReasonCode)}</strong>
                  </div>
                </div>
                <p className="trade-summary-note warning">
                  {item.blockReasonCode === "VERIFICATION_ATTEMPTS_EXCEEDED"
                    ? "인증 실패 횟수가 기준을 초과해 차단되었습니다. 운영팀에 문의하거나 새 계좌를 등록하세요."
                    : "운영자가 출금 목적지에서 제외한 계좌입니다. 필요하면 운영팀에 해제 요청을 남기세요."}
                </p>
              </article>
            ))}
          </div>
        </section>
      ) : null}

      {pendingAccounts.length ? (
        <section className="timeline-panel">
          <div className="section-header">
            <div>
              <p className="eyebrow">Verification Ticket</p>
              <h2>검증 대기 계좌 활성화</h2>
            </div>
          </div>
          <div className="verification-ticket-grid">
            {pendingAccounts.map((item) => {
              const verificationValue = verificationInputs[item.id] ?? item.verificationReference ?? "";
              const isExpired = item.verificationExpired;
              return (
                <article key={item.id} className="verification-ticket-card">
                  <div className="verification-ticket-header">
                    <div>
                      <p className="eyebrow">Pending Verification</p>
                      <strong>{item.bankName} / {item.accountAlias}</strong>
                      <p>{item.maskedAccountNumber} · {item.accountHolderName}</p>
                    </div>
                    <b className="status-pill pending_verification">{item.status}</b>
                  </div>

                  <div className="verification-ticket-summary">
                    <div>
                      <span>검증 요청 시각</span>
                      <strong>{item.verificationRequestedAt ? new Date(item.verificationRequestedAt).toLocaleString() : "방금 등록됨"}</strong>
                    </div>
                    <div>
                      <span>만료 시각</span>
                      <strong>{item.verificationExpiresAt ? new Date(item.verificationExpiresAt).toLocaleString() : "계산 불가"}</strong>
                    </div>
                    <div>
                      <span>시도 횟수</span>
                      <strong>{item.verificationAttemptCount} / 5</strong>
                    </div>
                    <div>
                      <span>재발송 가능</span>
                      <strong>
                        {item.verificationResendAllowed
                          ? "지금 가능"
                          : item.verificationResendAvailableAt
                            ? new Date(item.verificationResendAvailableAt).toLocaleTimeString()
                            : "잠시 후"}
                      </strong>
                    </div>
                    <div>
                      <span>기본 출금 지정</span>
                      <strong>{item.primaryWithdrawal ? "활성화 후 기본 반영" : "일반 목적지"}</strong>
                    </div>
                    <div>
                      <span>데모 인증 문구</span>
                      <strong>{item.verificationReference ?? "MVP-0000"}</strong>
                    </div>
                    <div>
                      <span>현재 상태</span>
                      <strong>{isExpired ? "만료됨" : "입력 가능"}</strong>
                    </div>
                  </div>

                  <div className="verification-ticket-form">
                    <label className="form-field">
                      <span>소액이체 인증 문구</span>
                      <input
                        value={verificationValue}
                        placeholder="MVP-0000"
                        onChange={(event) => setVerificationInputs((current) => ({
                          ...current,
                          [item.id]: event.target.value,
                        }))}
                      />
                    </label>
                    <button
                      type="button"
                      className="primary-button"
                      disabled={verifyingId === item.id || !verificationValue.trim() || isExpired}
                      onClick={() => void handleVerify(item)}
                    >
                      {verifyingId === item.id ? "인증 처리 중..." : isExpired ? "재발송 필요" : "인증 완료"}
                    </button>
                    <button
                      type="button"
                      className="secondary-button"
                      disabled={resendingId === item.id || !item.verificationResendAllowed}
                      onClick={() => void onResend(item.id)}
                    >
                      {resendingId === item.id ? "재발송 중..." : item.verificationResendAllowed ? "인증 문구 재발송" : "재발송 대기"}
                    </button>
                  </div>

                  <p className={`trade-summary-note ${isExpired ? "warning" : ""}`}>
                    {isExpired
                      ? "인증 문구가 만료되었습니다. 새 문구를 재발송한 뒤 10분 안에 입력하세요."
                      : item.verificationResendAllowed
                        ? "실제 서비스에서는 은행 거래 메모의 인증 문구를 입력합니다. 현재 데모에서는 카드에 표시된 문구로 바로 검증할 수 있습니다."
                        : `방금 인증 문구를 재발송했습니다. ${item.verificationResendAvailableAt ? new Date(item.verificationResendAvailableAt).toLocaleTimeString() : "잠시 후"} 이후 다시 요청할 수 있습니다.`}
                  </p>
                </article>
              );
            })}
          </div>
        </section>
      ) : null}

      <section className="timeline-panel">
        <div className="section-header">
          <div>
            <p className="eyebrow">Linked Bank Accounts</p>
            <h2>내 연결 계좌 목록</h2>
          </div>
        </div>
        <div className="table-shell">
          <table className="data-table">
            <thead>
              <tr>
                <th>은행</th>
                <th>별칭</th>
                <th>예금주</th>
                <th>계좌번호</th>
                <th>상태</th>
                <th>기본 출금</th>
                <th>검증시각</th>
                <th>액션</th>
              </tr>
            </thead>
            <tbody>
              {linkedBankAccounts.length ? (
                linkedBankAccounts.map((item) => (
                  <tr key={item.id}>
                    <td>{item.bankName}</td>
                    <td>{item.accountAlias}</td>
                    <td>{item.accountHolderName}</td>
                    <td className="table-mono">
                      <div>{item.maskedAccountNumber}</div>
                      {item.status === "PENDING_VERIFICATION" && item.verificationReference ? (
                        <span className="table-muted">
                          memo {item.verificationReference}{item.verificationExpired ? " · expired" : ""}
                        </span>
                      ) : null}
                    </td>
                    <td><b className={`status-pill ${item.status.toLowerCase()}`}>{item.status}</b></td>
                    <td>
                      {item.status === "ACTIVE"
                        ? item.primaryWithdrawal ? "기본" : "-"
                        : item.primaryWithdrawal ? "기본 예정" : "-"}
                    </td>
                    <td>{item.verifiedAt ? new Date(item.verifiedAt).toLocaleString() : "검증 대기"}</td>
                    <td>
                      {item.status === "ACTIVE" ? (
                        item.primaryWithdrawal ? (
                          <span className="table-muted">현재 기본 계좌</span>
                        ) : (
                          <button
                            type="button"
                            className="table-inline-button"
                            disabled={submitting}
                            onClick={() => void onMarkPrimary(item.id)}
                          >
                            기본 출금 설정
                          </button>
                        )
                      ) : item.status === "PENDING_VERIFICATION" ? (
                        <span className="table-muted">인증 대기</span>
                      ) : (
                        <span className="table-muted">{renderBlockReason(item.blockReasonCode)}</span>
                      )}
                    </td>
                  </tr>
                ))
              ) : (
                <tr className="table-empty-row">
                  <td colSpan={8}>
                    <strong>연결 계좌가 없습니다.</strong>
                    <p>외부 출금 목적지를 먼저 등록하세요.</p>
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

function renderBlockReason(blockReasonCode: string | null) {
  if (blockReasonCode === "VERIFICATION_ATTEMPTS_EXCEEDED") {
    return "인증 실패 누적";
  }
  if (blockReasonCode === "OPS_BLOCKED") {
    return "운영자 차단";
  }
  return "차단";
}

function renderBlockedNextAction(blockReasonCode: string | null) {
  if (blockReasonCode === "VERIFICATION_ATTEMPTS_EXCEEDED") {
    return "운영 확인 또는 신규 등록";
  }
  if (blockReasonCode === "OPS_BLOCKED") {
    return "운영팀 해제 요청";
  }
  return "운영팀 문의";
}
