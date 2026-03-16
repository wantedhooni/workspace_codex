"use client";

import {
  createContext,
  type ReactNode,
  startTransition,
  useContext,
  useEffect,
  useState,
} from "react";
import { useRouter } from "next/navigation";
import { apiClient } from "@/lib/api-client";
import type { AuthResponse, UserProfile } from "@/types/auth";

type LoginInput = {
  email: string;
  password: string;
};

type SignupInput = LoginInput & {
  name: string;
};

type AuthContextValue = {
  user: UserProfile | null;
  isAuthenticated: boolean;
  isInitializing: boolean;
  login: (input: LoginInput) => Promise<void>;
  signup: (input: SignupInput) => Promise<void>;
  logout: () => Promise<void>;
  refreshSession: () => Promise<void>;
  updateUser: (user: UserProfile) => void;
};

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserProfile | null>(null);
  const [isInitializing, setIsInitializing] = useState(true);
  const router = useRouter();

  useEffect(() => {
    void refreshSession().finally(() => setIsInitializing(false));
  }, []);

  const applyAuthResponse = (response: AuthResponse) => {
    apiClient.setAccessToken(response.accessToken);
    setUser(response.user);
  };

  const login = async (input: LoginInput) => {
    const response = await apiClient.request<AuthResponse>("/auth/login", {
      method: "POST",
      body: JSON.stringify(input),
    });
    startTransition(() => {
      applyAuthResponse(response);
      router.push("/dashboard");
    });
  };

  const signup = async (input: SignupInput) => {
    const response = await apiClient.request<AuthResponse>("/auth/signup", {
      method: "POST",
      body: JSON.stringify(input),
    });
    startTransition(() => {
      applyAuthResponse(response);
      router.push("/dashboard");
    });
  };

  const logout = async () => {
    try {
      await apiClient.request<void>("/auth/logout", {
        method: "POST",
      });
    } finally {
      apiClient.setAccessToken(null);
      setUser(null);
      startTransition(() => {
        router.push("/login");
      });
    }
  };

  const refreshSession = async () => {
    const token = await apiClient.refreshAccessToken();
    if (!token) {
      apiClient.setAccessToken(null);
      setUser(null);
      return;
    }

    try {
      const me = await apiClient.request<UserProfile>("/users/me");
      setUser(me);
    } catch {
      apiClient.setAccessToken(null);
      setUser(null);
    }
  };

  const updateUser = (nextUser: UserProfile) => {
    setUser(nextUser);
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated: Boolean(user),
        isInitializing,
        login,
        signup,
        logout,
        refreshSession,
        updateUser,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("AuthProvider 내부에서만 사용할 수 있습니다.");
  }
  return context;
}
