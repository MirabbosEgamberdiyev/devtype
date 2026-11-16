import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import { I18nextProvider } from 'react-i18next';
import i18n from '../../../shared/i18n';
import { LoginPage } from '../pages/LoginPage';
import { authApi } from '../api';
import { useAuthStore } from '../../../shared/stores/authStore';
import { ProblemError } from '../../../shared/api/problem';

vi.mock('../api', () => ({
  authApi: {
    login: vi.fn(),
    oauthCallback: vi.fn(),
  },
}));

describe('LoginPage Component', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    useAuthStore.getState().logout();
  });

  const renderWithProviders = (component: React.ReactElement) => {
    return render(
      <I18nextProvider i18n={i18n}>
        <MemoryRouter>{component}</MemoryRouter>
      </I18nextProvider>
    );
  };

  it('renders login form elements properly', () => {
    renderWithProviders(<LoginPage />);

    expect(screen.getByLabelText(/Email or Username|loginField/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Password|passwordField/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Sign in|Kirish|Войти/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /GitHub/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Google/i })).toBeInTheDocument();
  });

  it('displays error alert on invalid credentials', async () => {
    vi.mocked(authApi.login).mockRejectedValueOnce(
      new ProblemError({
        status: 401,
        code: 'INVALID_CREDENTIALS',
        title: 'Unauthorized',
        detail: 'Invalid credentials',
      })
    );

    renderWithProviders(<LoginPage />);

    fireEvent.change(screen.getByLabelText(/Email or Username|loginField/i), {
      target: { value: 'wronguser' },
    });
    fireEvent.change(screen.getByLabelText(/Password|passwordField/i), {
      target: { value: 'wrongpass' },
    });
    fireEvent.click(screen.getByRole('button', { name: /Sign in|Kirish|Войти/i }));

    await waitFor(() => {
      expect(screen.getByRole('alert')).toBeInTheDocument();
    });
  });

  it('successfully logs in and stores auth tokens', async () => {
    const mockResponse = {
      accessToken: 'jwt_mock_token',
      expiresIn: 900,
      user: {
        id: 1,
        email: 'dev@devtype.com',
        username: 'devtype_user',
        displayName: 'Dev Developer',
        role: 'USER' as const,
        emailVerified: true,
        avatarKey: null,
        locale: 'en',
        timezone: 'UTC',
        onboarded: true,
      },
    };

    vi.mocked(authApi.login).mockResolvedValueOnce(mockResponse);

    renderWithProviders(<LoginPage />);

    fireEvent.change(screen.getByLabelText(/Email or Username|loginField/i), {
      target: { value: 'dev@devtype.com' },
    });
    fireEvent.change(screen.getByLabelText(/Password|passwordField/i), {
      target: { value: 'ValidPass123!' },
    });
    fireEvent.click(screen.getByRole('button', { name: /Sign in|Kirish|Войти/i }));

    await waitFor(() => {
      expect(useAuthStore.getState().isAuthenticated).toBe(true);
      expect(useAuthStore.getState().accessToken).toBe('jwt_mock_token');
    });
  });
});
