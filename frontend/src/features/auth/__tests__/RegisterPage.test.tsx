import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import { I18nextProvider } from 'react-i18next';
import i18n from '../../../shared/i18n';
import { RegisterPage } from '../pages/RegisterPage';
import { authApi } from '../api';
import { useAuthStore } from '../../../shared/stores/authStore';
import { ProblemError } from '../../../shared/api/problem';

vi.mock('../api', () => ({
  authApi: {
    register: vi.fn(),
    oauthCallback: vi.fn(),
  },
}));

describe('RegisterPage Component', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    useAuthStore.getState().logout();
    useAuthStore.getState().setGuestToken(null);
  });

  const renderWithProviders = (component: React.ReactElement) => {
    return render(
      <I18nextProvider i18n={i18n}>
        <MemoryRouter>{component}</MemoryRouter>
      </I18nextProvider>
    );
  };

  it('renders registration form inputs', () => {
    renderWithProviders(<RegisterPage />);

    expect(screen.getByLabelText(/Email/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Username|Foydalanuvchi/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Password|Parol/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Create account|Akkaunt yaratish/i })).toBeInTheDocument();
  });

  it('shows guest sessions notice when guest token is active', () => {
    useAuthStore.getState().setGuestToken('gst_active_123');
    renderWithProviders(<RegisterPage />);

    expect(screen.getByText(/guest sessions will be automatically claimed|Mehmon holatidagi mashqlaringiz/i)).toBeInTheDocument();
  });

  it('validates password length on client before submission', async () => {
    renderWithProviders(<RegisterPage />);

    fireEvent.change(screen.getByLabelText(/Email/i), {
      target: { value: 'dev@devtype.com' },
    });
    fireEvent.change(screen.getByLabelText(/Username|Foydalanuvchi/i), {
      target: { value: 'validuser' },
    });
    fireEvent.change(screen.getByLabelText(/Password|Parol/i), {
      target: { value: 'short' },
    });
    fireEvent.click(screen.getByRole('button', { name: /Create account|Akkaunt yaratish/i }));

    await waitFor(() => {
      expect(screen.getByRole('alert')).toBeInTheDocument();
      expect(authApi.register).not.toHaveBeenCalled();
    });
  });

  it('displays error when email is already taken', async () => {
    vi.mocked(authApi.register).mockRejectedValueOnce(
      new ProblemError({
        status: 409,
        code: 'EMAIL_TAKEN',
        title: 'Conflict',
        detail: 'Email is already registered',
      })
    );

    renderWithProviders(<RegisterPage />);

    fireEvent.change(screen.getByLabelText(/Email/i), {
      target: { value: 'taken@devtype.com' },
    });
    fireEvent.change(screen.getByLabelText(/Username|Foydalanuvchi/i), {
      target: { value: 'validuser' },
    });
    fireEvent.change(screen.getByLabelText(/Password|Parol/i), {
      target: { value: 'ValidPassword123!' },
    });
    fireEvent.click(screen.getByRole('button', { name: /Create account|Akkaunt yaratish/i }));

    await waitFor(() => {
      expect(screen.getByRole('alert')).toBeInTheDocument();
    });
  });
});
