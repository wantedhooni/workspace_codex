import { useState } from "react";
import type { FormEvent } from "react";
import { Alert, Box, Button, Card, CardContent, Chip, Stack, TextField, Typography } from "@mui/material";
import { Login, useLogin, useNotify } from "react-admin";
import { DEMO_ADMIN_CREDENTIALS, DEMO_LOCAL_ACCOUNTS, isLocalRuntime } from "../providers/authProvider";

type LoginForm = {
  username: string;
  password: string;
};

const createInitialForm = (): LoginForm => {
  if (isLocalRuntime()) {
    return {
      username: DEMO_ADMIN_CREDENTIALS.username,
      password: DEMO_ADMIN_CREDENTIALS.password
    };
  }

  return {
    username: "",
    password: ""
  };
};

export const LoginPage = () => {
  const login = useLogin();
  const notify = useNotify();
  const localMode = isLocalRuntime();
  const [form, setForm] = useState<LoginForm>(() => createInitialForm());
  const [submitting, setSubmitting] = useState(false);

  const onSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setSubmitting(true);
    try {
      await login({ username: form.username, password: form.password });
    } catch (error) {
      notify(error instanceof Error ? error.message : "로그인에 실패했습니다.", { type: "error" });
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Login>
      <Card elevation={4} sx={{ minWidth: 360, maxWidth: 420, mt: 1, borderRadius: 3 }}>
        <CardContent>
          <Stack spacing={2.2}>
            <Box>
              <Typography variant="h6">Quant Admin Console</Typography>
              <Typography variant="body2" color="text.secondary">
                미국 증시 퀀트 운용/리스크/원장 통합 운영 콘솔
              </Typography>
            </Box>

            {localMode ? (
              <Alert severity="info">
                로컬 데모 계정이 지원됩니다. 아래 계정을 누르면 입력값이 변경됩니다.
              </Alert>
            ) : null}

            {localMode ? (
              <Stack direction="row" useFlexGap flexWrap="wrap" gap={1}>
                {DEMO_LOCAL_ACCOUNTS.map((account) => (
                  <Chip
                    key={account.username}
                    label={account.username}
                    size="small"
                    onClick={() => {
                      setForm({
                        username: account.username,
                        password: account.password
                      });
                    }}
                    variant={form.username === account.username ? "filled" : "outlined"}
                    color={form.username === account.username ? "primary" : "default"}
                  />
                ))}
              </Stack>
            ) : null}

            <Box component="form" onSubmit={onSubmit}>
              <Stack spacing={2}>
                <TextField
                  autoComplete="username"
                  fullWidth
                  label="이메일"
                  name="username"
                  onChange={(event) => setForm((prev) => ({ ...prev, username: event.target.value }))}
                  required
                  value={form.username}
                />

                <TextField
                  autoComplete="current-password"
                  fullWidth
                  label="비밀번호"
                  name="password"
                  onChange={(event) => setForm((prev) => ({ ...prev, password: event.target.value }))}
                  required
                  type="password"
                  value={form.password}
                />

                <Button
                  disabled={!form.username || !form.password || submitting}
                  fullWidth
                  size="large"
                  type="submit"
                  variant="contained"
                >
                  로그인
                </Button>
              </Stack>
            </Box>
          </Stack>
        </CardContent>
      </Card>
    </Login>
  );
};
