import { useState } from "react";
import { Box, Button, Card, CardContent, Stack, TextField, Typography } from "@mui/material";
import { useLogin } from "@refinedev/core";
import { PageTitle } from "../components/PageTitle";
import { pageDescriptions } from "../utils/pageDescriptions";

export function LoginPage() {
  const { mutate: login, isLoading } = useLogin();
  const [username, setUsername] = useState("opsadmin");
  const [password, setPassword] = useState("admin123!");

  return (
    <Box
      sx={{
        minHeight: "100vh",
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        background: "linear-gradient(135deg, #f3f7ff 0%, #e6f6ef 100%)",
      }}
    >
      <Card sx={{ width: 420 }}>
        <CardContent>
          <Stack spacing={2}>
            <PageTitle title="Derivatives Ops Login" description={pageDescriptions.login} />
            <TextField
              label="Username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              fullWidth
            />
            <TextField
              label="Password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              fullWidth
            />
            <Button
              variant="contained"
              disabled={isLoading}
              onClick={() => login({ username, password })}
            >
              Login
            </Button>
            <Typography variant="caption" color="text.secondary">
              Demo: opsadmin / admin123! (requester), opsadmin2 / admin234! (approver)
            </Typography>
          </Stack>
        </CardContent>
      </Card>
    </Box>
  );
}
