import { Button, Card, Form, Input, Typography } from "antd";

const ADMIN_DEMO_ACCOUNT = {
  email: "admin@mvpbanking.local",
  password: "Admin1234!",
};

type LoginScreenProps = {
  submitting: boolean;
  onLogin: (values: { email: string; password: string }) => Promise<void>;
};

export function LoginScreen({ submitting, onLogin }: LoginScreenProps) {
  const [form] = Form.useForm<{ email: string; password: string }>();

  function fillDemoAccount() {
    form.setFieldsValue(ADMIN_DEMO_ACCOUNT);
  }

  return (
    <main className="admin-auth-shell">
      <Card className="admin-login-card" bordered={false}>
        <p className="eyebrow">MVP Banking</p>
        <Typography.Title level={2}>Admin Portal Login</Typography.Title>
        <Typography.Paragraph type="secondary">
          운영자 인증 후 고객, 계좌, 거래, 승인, 감사 로그를 검색과 페이지 단위로 조회합니다.
        </Typography.Paragraph>
        <section className="demo-account-panel">
          <div>
            <p className="eyebrow">Demo Account</p>
            <Typography.Text strong>{ADMIN_DEMO_ACCOUNT.email}</Typography.Text>
            <Typography.Paragraph className="demo-account-password">
              {ADMIN_DEMO_ACCOUNT.password}
            </Typography.Paragraph>
          </div>
          <Button onClick={fillDemoAccount}>데모 계정 채우기</Button>
        </section>
        <Form form={form} layout="vertical" onFinish={onLogin} initialValues={ADMIN_DEMO_ACCOUNT}>
          <Form.Item label="Email" name="email" rules={[{ required: true }]}>
            <Input size="large" />
          </Form.Item>
          <Form.Item label="Password" name="password" rules={[{ required: true }]}>
            <Input.Password size="large" />
          </Form.Item>
          <Button htmlType="submit" type="primary" size="large" block loading={submitting}>
            로그인
          </Button>
        </Form>
      </Card>
    </main>
  );
}
