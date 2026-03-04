export type AuthResponse = {
  accessToken: string;
  refreshToken: string;
  accessTokenExpiresIn: number;
  refreshTokenExpiresIn: number;
  principalType: "USER" | "ADMIN";
  email: string;
  displayName: string;
};

export type Profile = {
  id: string;
  email: string;
  displayName: string;
  principalType: string;
  roles: string[];
};
