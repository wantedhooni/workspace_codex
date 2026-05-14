import { LogoutClient } from "./logout-client";

export default function LogoutPage() {
  return (
    <main className="grid min-h-screen place-items-center bg-[#f7f9fc] px-6 text-slate-950">
      <div className="rounded-2xl border border-slate-200 bg-white p-8 text-center shadow-sm">
        <h1 className="text-2xl font-semibold">세션 정리</h1>
        <div className="mt-3">
          <LogoutClient />
        </div>
      </div>
    </main>
  );
}
