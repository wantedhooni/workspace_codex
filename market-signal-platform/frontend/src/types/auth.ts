export type UserProfile = {
  id: number;
  email: string;
  name: string;
  bio: string | null;
  createdAt: string;
};

export type AuthResponse = {
  accessToken: string;
  tokenType: string;
  user: UserProfile;
};
