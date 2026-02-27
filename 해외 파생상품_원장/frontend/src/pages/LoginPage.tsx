import { useState } from "react";
import { Box, Button, Card, CardContent, Stack, TextField, Typography } from "@mui/material";
import { useLogin } from "@refinedev/core";

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
            <Typography variant="h5" fontWeight={700}>
              Derivatives Ops Login
            </Typography>
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
              Demo: opsadmin / admin123!
            </Typography>
          </Stack>
        </CardContent>
      </Card>
    </Box>
  );
}
