import { apiClient } from '../../shared/api/client';
import { UserView } from '../../shared/stores/authStore';

export interface RegisterRequest {
  email: string;
  username: string;
  password: string;
  locale?: string;
  timezone?: string;
  guestToken?: string | null;
}

export interface LoginRequest {
  login: string;
  password: string;
}

export interface AuthResponse {
  accessToken: string;
  expiresIn: number;
  user: UserView;
  claimedSessions?: number | null;
}

export interface GuestResponse {
  guestToken: string;
  expiresAt: string;
}

export interface VerifyEmailRequest {
  token: string;
}

export interface ForgotPasswordRequest {
  email: string;
}

export interface ResetPasswordRequest {
  token: string;
  newPassword: string;
}

export interface OAuthProviderRequest {
  email: string;
  providerUid: string;
  displayName?: string | null;
}

export const authApi = {
  register: (data: RegisterRequest): Promise<AuthResponse> =>
    apiClient<AuthResponse>('/auth/register', {
      method: 'POST',
      body: JSON.stringify(data),
      skipAuth: true,
    }),

  login: (data: LoginRequest): Promise<AuthResponse> =>
    apiClient<AuthResponse>('/auth/login', {
      method: 'POST',
      body: JSON.stringify(data),
      skipAuth: true,
    }),

  refresh: (): Promise<AuthResponse> =>
    apiClient<AuthResponse>('/auth/refresh', {
      method: 'POST',
      skipAuth: true,
    }),

  logout: (): Promise<void> =>
    apiClient<void>('/auth/logout', {
      method: 'POST',
    }),

  verifyEmail: (data: VerifyEmailRequest): Promise<{ status: string }> =>
    apiClient<{ status: string }>('/auth/verify-email', {
      method: 'POST',
      body: JSON.stringify(data),
      skipAuth: true,
    }),

  forgotPassword: (data: ForgotPasswordRequest): Promise<{ status: string }> =>
    apiClient<{ status: string }>('/auth/forgot-password', {
      method: 'POST',
      body: JSON.stringify(data),
      skipAuth: true,
    }),

  resetPassword: (data: ResetPasswordRequest): Promise<{ status: string }> =>
    apiClient<{ status: string }>('/auth/reset-password', {
      method: 'POST',
      body: JSON.stringify(data),
      skipAuth: true,
    }),

  getGuestToken: (): Promise<GuestResponse> =>
    apiClient<GuestResponse>('/auth/guest', {
      method: 'POST',
      skipAuth: true,
    }),

  oauthCallback: (provider: string, data: OAuthProviderRequest): Promise<AuthResponse> =>
    apiClient<AuthResponse>(`/auth/oauth/${provider}/callback`, {
      method: 'POST',
      body: JSON.stringify(data),
      skipAuth: true,
    }),
};
