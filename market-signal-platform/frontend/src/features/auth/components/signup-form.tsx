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
  name: z.string().min(2, "이름은 2자 이상이어야 합니다."),
  email: z.email("이메일 형식이 올바르지 않습니다."),
  password: z.string().min(8, "비밀번호는 8자 이상이어야 합니다."),
});

type FormValues = z.infer<typeof schema>;

export function SignupForm() {
  const { signup } = useAuth();
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
    setError,
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
  });

  const onSubmit = async (values: FormValues) => {
    try {
      await signup(values);
    } catch (error) {
      setError("root", {
        message: error instanceof ApiError ? error.message : "회원가입에 실패했습니다.",
      });
    }
  };

  return (
    <Card className="mx-auto max-w-xl overflow-hidden">
      <CardHeader className="border-b border-white/70 bg-[linear-gradient(135deg,rgba(255,255,255,0.96),rgba(247,240,231,0.92))]">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <div>
            <CardTitle>회원가입</CardTitle>
            <CardDescription>개인 분석 워크스페이스를 만들고 바로 투자 시그널 운영 흐름을 시작합니다.</CardDescription>
          </div>
          <Badge className="bg-accent/10 text-accent">New Workspace</Badge>
        </div>
      </CardHeader>
      <CardContent className="space-y-6 pt-6">
        <div className="grid gap-3 sm:grid-cols-3">
          <div className="rounded-[1.5rem] border border-border/70 bg-muted/70 p-4">
            <p className="text-xs font-semibold uppercase tracking-[0.2em] text-accent">1</p>
            <p className="mt-3 text-sm font-semibold">계정 생성</p>
            <p className="mt-2 text-sm leading-6 text-foreground/65">이름, 이메일, 비밀번호로 개인 워크스페이스를 만듭니다.</p>
          </div>
          <div className="rounded-[1.5rem] border border-border/70 bg-muted/70 p-4">
            <p className="text-xs font-semibold uppercase tracking-[0.2em] text-accent">2</p>
            <p className="mt-3 text-sm font-semibold">보호 화면 진입</p>
            <p className="mt-2 text-sm leading-6 text-foreground/65">대시보드, 리포트, 관심 종목, 뉴스 분석을 바로 사용할 수 있습니다.</p>
          </div>
          <div className="rounded-[1.5rem] border border-border/70 bg-muted/70 p-4">
            <p className="text-xs font-semibold uppercase tracking-[0.2em] text-accent">3</p>
            <p className="mt-3 text-sm font-semibold">프로필 설정</p>
            <p className="mt-2 text-sm leading-6 text-foreground/65">투자 성향과 소개를 추가해 운영 컨텍스트를 정리합니다.</p>
          </div>
        </div>
        <form className="space-y-5" onSubmit={handleSubmit(onSubmit)}>
          <div>
            <Label htmlFor="name">이름</Label>
            <Input autoComplete="name" id="name" placeholder="Alex Kim" {...register("name")} />
            {errors.name && <p className="mt-2 text-sm text-danger">{errors.name.message}</p>}
          </div>
          <div>
            <Label htmlFor="email">이메일</Label>
            <Input autoComplete="email" id="email" placeholder="alex@example.com" {...register("email")} />
            {errors.email && <p className="mt-2 text-sm text-danger">{errors.email.message}</p>}
          </div>
          <div>
            <Label htmlFor="password">비밀번호</Label>
            <Input autoComplete="new-password" id="password" type="password" placeholder="8자 이상 입력" {...register("password")} />
            {errors.password && <p className="mt-2 text-sm text-danger">{errors.password.message}</p>}
            <p className="mt-2 text-xs leading-5 text-foreground/55">비밀번호는 8자 이상으로 입력하고, 실제 운영 계정이라면 숫자와 특수문자를 함께 사용하는 편이 안전합니다.</p>
          </div>
          {errors.root && (
            <div className="rounded-2xl border border-danger/20 bg-danger/5 px-4 py-3 text-sm text-danger">
              {errors.root.message}
            </div>
          )}
          <Button className="w-full" size="lg" type="submit" disabled={isSubmitting}>
            {isSubmitting ? "가입 중..." : "회원가입"}
          </Button>
          <p className="text-xs leading-6 text-foreground/55">
            가입이 완료되면 자동 로그인 후 대시보드로 이동합니다. 이후 프로필과 관심 종목을 바로 설정할 수 있습니다.
          </p>
        </form>
      </CardContent>
    </Card>
  );
}
