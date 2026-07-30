import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { setToken, removeToken, getToken } from '../../../api/http';
import { AuthApi, LoginRequest, UserSummary, LoginResponse } from '../api/authApi';
import { getLandingRouteForRoles } from '../appMode';

const USER_KEY = 'bbms_user';

interface AuthContextType {
  user: UserSummary | null;
  token: string | null;
  permissions: string[];
  roles: string[];
  isAuthenticated: boolean;
  login: (credentials: LoginRequest) => Promise<LoginResponse>;
  logout: () => void;
  hasPermission: (permission: string) => boolean;
  hasRole: (role: string) => boolean;
  getLandingRoute: () => string;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [tokenState, setTokenState] = useState<string | null>(() => getToken());
  const [userState, setUserState] = useState<UserSummary | null>(() => {
    const savedUser = localStorage.getItem(USER_KEY);
    if (!savedUser) return null;
    try {
      return JSON.parse(savedUser);
    } catch {
      return null;
    }
  });

  const login = useCallback(async (credentials: LoginRequest): Promise<LoginResponse> => {
    const res = await AuthApi.login(credentials);
    if (res.accessToken) {
      setToken(res.accessToken);
      setTokenState(res.accessToken);
      if (res.user) {
        setUserState(res.user);
        localStorage.setItem(USER_KEY, JSON.stringify(res.user));
      }
    }
    return res;
  }, []);

  const logout = useCallback(() => {
    removeToken();
    localStorage.removeItem(USER_KEY);
    setTokenState(null);
    setUserState(null);
  }, []);

  const permissions = userState?.permissions ?? [];
  const roles = userState?.roles ?? [];

  const hasPermission = useCallback(
    (permission: string): boolean => {
      if (!userState) return false;
      if (roles.includes('ADMIN') || roles.includes('ROLE_ADMIN')) return true;
      return permissions.includes(permission);
    },
    [userState, roles, permissions]
  );

  const hasRole = useCallback(
    (role: string): boolean => {
      if (!userState) return false;
      const target = role.toUpperCase();
      return roles.some((r) => r.toUpperCase() === target || r.toUpperCase() === `ROLE_${target}`);
    },
    [userState, roles]
  );

  const getLandingRoute = useCallback(() => {
    return getLandingRouteForRoles(roles);
  }, [roles]);

  const value: AuthContextType = {
    user: userState,
    token: tokenState,
    permissions,
    roles,
    isAuthenticated: !!tokenState && !!userState,
    login,
    logout,
    hasPermission,
    hasRole,
    getLandingRoute,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
