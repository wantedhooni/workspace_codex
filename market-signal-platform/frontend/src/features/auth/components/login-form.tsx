"use client";

import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { ApiError } from "@/lib/api-client";
import { useAuth } from "@/features/auth/context/auth-context";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";

const schema = z.object({
  email: z.email("이메일 형식이 올바르지 않습니다."),
  password: z.string().min(8, "비밀번호는 8자 이상이어야 합니다."),
});

type FormValues = z.infer<typeof schema>;

export function LoginForm({ nextPath }: { nextPath?: string | null }) {
  const { login } = useAuth();
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
    setError,
    setValue,
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
  });

  const onSubmit = async (values: FormValues) => {
    try {
      await login(values);
      if (nextPath) {
        window.location.href = nextPath;
      }
    } catch (error) {
      setError("root", {
        message: error instanceof ApiError ? error.message : "로그인에 실패했습니다.",
      });
    }
  };

  return (
    <Card className="mx-auto max-w-xl overflow-hidden">
      <CardHeader className="border-b border-white/70 bg-[linear-gradient(135deg,rgba(255,255,255,0.96),rgba(247,240,231,0.92))]">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <div>
            <CardTitle>로그인</CardTitle>
            <CardDescription>대시보드, 관심 종목, 리포트, 뉴스 분석 화면으로 바로 진입합니다.</CardDescription>
          </div>
          <Badge className="bg-primary/10 text-primary">Protected Workspace</Badge>
        </div>
      </CardHeader>
      <CardContent className="space-y-6 pt-6">
        <div className="rounded-[1.75rem] border border-border/70 bg-muted/70 p-4">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div>
              <p className="text-xs font-semibold uppercase tracking-[0.2em] text-accent">Quick Access</p>
              <p className="mt-2 text-sm text-foreground/75">데모 계정을 즉시 채워 로그인 흐름과 데이터 화면을 바로 확인할 수 있습니다.</p>
            </div>
            <Button
              type="button"
              variant="outline"
              onClick={() => {
                setValue("email", "demo@marketsignal.dev", { shouldValidate: true });
                setValue("password", "Demo1234!", { shouldValidate: true });
              }}
            >
              데모 계정 채우기
            </Button>
          </div>
          <div className="mt-4 grid gap-2 text-sm text-foreground/70 sm:grid-cols-3">
            <div className="rounded-2xl border border-border/70 bg-white/80 px-3 py-2">시장 레짐과 섹터 강도</div>
            <div className="rounded-2xl border border-border/70 bg-white/80 px-3 py-2">관심 종목 커버리지</div>
            <div className="rounded-2xl border border-border/70 bg-white/80 px-3 py-2">뉴스 분석 이력</div>
          </div>
        </div>
        <form className="space-y-5" onSubmit={handleSubmit(onSubmit)}>
          <div>
            <Label htmlFor="email">이메일</Label>
            <Input autoComplete="email" id="email" placeholder="demo@marketsignal.dev" {...register("email")} />
            {errors.email && <p className="mt-2 text-sm text-danger">{errors.email.message}</p>}
          </div>
          <div>
            <Label htmlFor="password">비밀번호</Label>
            <Input autoComplete="current-password" id="password" type="password" placeholder="Demo1234!" {...register("password")} />
            {errors.password && <p className="mt-2 text-sm text-danger">{errors.password.message}</p>}
          </div>
          {errors.root && (
            <div className="rounded-2xl border border-danger/20 bg-danger/5 px-4 py-3 text-sm text-danger">
              {errors.root.message}
            </div>
          )}
          <Button className="w-full" disabled={isSubmitting} size="lg" type="submit">
            {isSubmitting ? "로그인 중..." : "로그인"}
          </Button>
          <p className="text-xs leading-6 text-foreground/55">
            로그인 후 access token은 앱 상태에 저장되고, refresh token 쿠키를 사용해 보호된 화면에서 자동으로 세션이 갱신됩니다.
          </p>
        </form>
      </CardContent>
    </Card>
  );
}
