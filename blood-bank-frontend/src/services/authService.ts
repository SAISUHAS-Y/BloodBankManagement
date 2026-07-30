import { request } from '../api/http';
import { UserSummary } from '../features/auth/api/authApi';

export interface LoginRequest {
  identifier: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresInMs: number;
  mfaRequired?: boolean;
  mfaToken?: string;
  user: UserSummary;
}

export interface DonorRegisterRequest {
  fullName: string;
  dateOfBirth: string;
  gender: string;
  bloodGroup?: string;
  email: string;
  phone: string;
  address: string;
  city: string;
  username: string;
  password: string;
  confirmPassword: string;
  privacyConsent: boolean;
  dataConsent: boolean;
}

export interface DonorRegisterResponse {
  id: number;
  username: string;
  email: string;
  verificationToken?: string;
  message: string;
}

export interface OtpVerifyRequest {
  email: string;
  otpCode: string;
}

export interface ForgotPasswordRequest {
  identifier: string;
}

export interface ResetPasswordRequest {
  token: string;
  newPassword: string;
}

export const authService = {
  login: (body: LoginRequest) =>
    request<LoginResponse>('POST', '/auth/login', { username: body.identifier, password: body.password }),

  registerDonor: (body: DonorRegisterRequest) =>
    request<DonorRegisterResponse>('POST', '/auth/register/donor', body),

  verifyOtp: (body: OtpVerifyRequest) =>
    request<{ success: boolean; message: string }>('POST', '/auth/verify-otp', body),

  forgotPassword: (emailOrUsername: string) =>
    request<{ message: string }>('POST', '/auth/forgot-password', { identifier: emailOrUsername }),

  resetPassword: (body: ResetPasswordRequest) =>
    request<{ message: string }>('POST', '/auth/reset-password', body),
};
