import { request } from '../../../api/http';

export interface UserSummary {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  roles: string[];
  permissions: string[];
  active: boolean;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresInMs: number;
  mfaRequired: boolean;
  mfaToken?: string;
  user: UserSummary;
}

export interface LoginRequest {
  username: string;
  password?: string;
}

export interface MfaVerifyRequest {
  mfaToken: string;
  code: string;
}

export const AuthApi = {
  login: (body: LoginRequest) => request<LoginResponse>('POST', '/auth/login', body),
  verifyMfa: (body: MfaVerifyRequest) => request<LoginResponse>('POST', '/auth/verify-mfa', body),
  logout: (refreshToken: string) => request<void>('POST', '/auth/logout', { refreshToken }),
  refresh: (refreshToken: string) => request<LoginResponse>('POST', '/auth/refresh', { refreshToken }),
};
