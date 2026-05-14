export type ApiResponse<T> = {
  success: boolean;
  data: T;
  message?: string;
  timestamp: number;
};

export type LoginRequest = {
  email: string;
  password: string;
};

export type UserAuthResponse = {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
};

export type RefreshTokenRequest = {
  refreshToken: string;
};

export type LogoutRequest = {
  refreshToken: string;
};

export type JwtPrincipal = {
  email?: string;
  sub?: string;
  userId?: string | number;
  roles?: string[];
  authorities?: string[];
  [key: string]: unknown;
};
