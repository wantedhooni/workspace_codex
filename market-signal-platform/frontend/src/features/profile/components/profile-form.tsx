"use client";

import { useForm } from "react-hook-form";
import { useMutation } from "@tanstack/react-query";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { ApiError, apiClient } from "@/lib/api-client";
import { useAuth } from "@/features/auth/context/auth-context";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import type { UserProfile } from "@/features/auth/types/auth";

const schema = z.object({
  name: z.string().min(2, "이름은 2자 이상이어야 합니다."),
  bio: z.string().max(500, "소개는 500자 이하여야 합니다.").optional(),
});

type FormValues = z.infer<typeof schema>;

export function ProfileForm() {
  const { user, updateUser } = useAuth();
  const {
    register,
    handleSubmit,
    formState: { errors },
    setError,
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    values: {
      name: user?.name ?? "",
      bio: user?.bio ?? "",
    },
  });

  const mutation = useMutation({
    mutationFn: (values: FormValues) =>
      apiClient.request<UserProfile>("/users/me", {
        method: "PATCH",
        body: JSON.stringify(values),
      }),
    onSuccess: (nextUser) => {
      updateUser(nextUser);
    },
  });

  const onSubmit = async (values: FormValues) => {
    try {
      await mutation.mutateAsync(values);
    } catch (error) {
      setError("root", {
        message: error instanceof ApiError ? error.message : "프로필 수정에 실패했습니다.",
      });
    }
  };

  if (!user) {
    return null;
  }

  const joinedDate = new Date(user.createdAt);
  const joinedDays = Math.max(1, Math.ceil((Date.now() - joinedDate.getTime()) / (1000 * 60 * 60 * 24)));

  return (
    <div className="grid gap-6 xl:grid-cols-[minmax(0,1.5fr)_340px]">
      <Card>
        <CardHeader>
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div>
              <CardTitle>내 프로필</CardTitle>
              <CardDescription>이름과 소개를 정리해 대시보드와 협업 화면에서 일관된 사용자 컨텍스트를 유지합니다.</CardDescription>
            </div>
            <Badge className="bg-primary/10 text-primary">Profile Settings</Badge>
          </div>
        </CardHeader>
        <CardContent>
          <form className="space-y-6" onSubmit={handleSubmit(onSubmit)}>
            <div className="grid gap-4 md:grid-cols-2">
              <div>
                <Label htmlFor="name">이름</Label>
                <Input id="name" {...register("name")} />
                {errors.name && <p className="mt-2 text-sm text-danger">{errors.name.message}</p>}
              </div>
              <div>
                <Label htmlFor="email">이메일</Label>
                <Input id="email" disabled value={user.email} />
                <p className="mt-2 text-xs text-foreground/55">이메일은 로그인 식별자로 사용되며 현재 화면에서는 수정하지 않습니다.</p>
              </div>
            </div>
            <div>
              <Label htmlFor="bio">소개</Label>
              <Textarea
                id="bio"
                placeholder="예: 대형 기술주와 반도체 중심으로 스윙 시그널을 검토합니다. 아침 리포트와 뉴스 해석을 주로 확인합니다."
                {...register("bio")}
              />
              {errors.bio && <p className="mt-2 text-sm text-danger">{errors.bio.message}</p>}
            </div>
            {errors.root && (
              <div className="rounded-2xl border border-danger/20 bg-danger/5 px-4 py-3 text-sm text-danger">
                {errors.root.message}
              </div>
            )}
            <div className="flex flex-wrap items-center justify-between gap-3 rounded-[1.75rem] border border-border/70 bg-muted/70 px-4 py-4">
              <div>
                <p className="text-sm font-semibold">프로필 저장</p>
                <p className="mt-1 text-sm text-foreground/65">프로필 정보는 상단 계정 영역과 운영 화면 소개 문구에 즉시 반영됩니다.</p>
              </div>
              <Button type="submit" disabled={mutation.isPending}>
                {mutation.isPending ? "저장 중..." : "프로필 저장"}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>

      <div className="space-y-6">
        <Card>
          <CardHeader>
            <CardTitle>계정 상태</CardTitle>
            <CardDescription>현재 세션과 계정 정보를 한 화면에서 확인합니다.</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="rounded-[1.5rem] border border-border/70 bg-muted/70 p-4">
              <p className="text-xs font-semibold uppercase tracking-[0.2em] text-accent">Account</p>
              <p className="mt-3 text-base font-semibold text-foreground">{user.email}</p>
              <p className="mt-1 text-sm text-foreground/65">가입일 {joinedDate.toLocaleDateString("ko-KR")}</p>
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div className="rounded-[1.5rem] border border-border/70 bg-white/80 p-4">
                <p className="text-xs font-semibold uppercase tracking-[0.18em] text-accent">Status</p>
                <p className="mt-3 text-lg font-semibold">Active</p>
              </div>
              <div className="rounded-[1.5rem] border border-border/70 bg-white/80 p-4">
                <p className="text-xs font-semibold uppercase tracking-[0.18em] text-accent">Joined</p>
                <p className="mt-3 text-lg font-semibold">{joinedDays}일</p>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>운영 팁</CardTitle>
            <CardDescription>프로필을 실제 사용 흐름과 맞추면 화면 가독성이 좋아집니다.</CardDescription>
          </CardHeader>
          <CardContent className="space-y-3 text-sm leading-6 text-foreground/68">
            <div className="rounded-[1.5rem] border border-border/70 bg-muted/65 p-4">
              관심 종목 관리 페이지와 리포트 화면에서 일관된 운영 맥락을 유지하려면 소개에 현재 추적하는 섹터나 스타일을 짧게 남기는 편이 좋습니다.
            </div>
            <div className="rounded-[1.5rem] border border-border/70 bg-muted/65 p-4">
              프로필을 수정한 뒤 대시보드 상단 계정 영역과 모바일 내비게이션의 사용자 문맥도 즉시 갱신됩니다.
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
