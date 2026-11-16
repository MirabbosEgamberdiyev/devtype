import React, { useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { authApi } from '../api';
import { ProblemError } from '../../../shared/api/problem';

export const ResetPasswordPage: React.FC = () => {
  const { t } = useTranslation('auth');
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token') || '';

  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [isSuccess, setIsSuccess] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMsg(null);

    if (!token) {
      setErrorMsg('Invalid or missing password reset token.');
      return;
    }

    if (newPassword.length < 8) {
      setErrorMsg(t('errors.passwordTooShort'));
      return;
    }

    if (newPassword !== confirmPassword) {
      setErrorMsg(t('errors.passwordsDoNotMatch'));
      return;
    }

    setIsLoading(true);
    try {
      await authApi.resetPassword({ token, newPassword });
      setIsSuccess(true);
    } catch (err) {
      if (err instanceof ProblemError) {
        if (err.code === 'TOKEN_EXPIRED') {
          setErrorMsg(t('verifyEmail.errorExpired'));
        } else if (err.code === 'TOKEN_USED') {
          setErrorMsg(t('verifyEmail.errorUsed'));
        } else {
          setErrorMsg(err.message);
        }
      } else {
        setErrorMsg('Password reset failed. Please request a new link.');
      }
    } finally {
      setIsLoading(false);
    }
  };

  if (isSuccess) {
    return (
      <div className="space-y-6 text-center">
        <div className="space-y-2">
          <div className="mx-auto w-12 h-12 rounded-full bg-success/10 text-success flex items-center justify-center text-xl">
            ✓
          </div>
          <h1 className="text-2xl font-bold tracking-tight text-text">{t('resetPassword.title')}</h1>
          <p className="text-sm text-text-muted leading-relaxed">{t('resetPassword.successMessage')}</p>
        </div>

        <Link
          to="/login"
          className="inline-block w-full py-2.5 px-4 bg-accent hover:opacity-90 active:scale-[0.99] text-accent-fg font-medium rounded-lg text-sm text-center transition-all"
        >
          {t('forgotPassword.backToLogin')}
        </Link>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="space-y-1.5 text-center">
        <h1 className="text-2xl font-bold tracking-tight text-text">{t('resetPassword.title')}</h1>
        <p className="text-sm text-text-muted">{t('resetPassword.subtitle')}</p>
      </div>

      {errorMsg && (
        <div role="alert" className="p-3 text-sm rounded bg-error/10 border border-error/20 text-error">
          {errorMsg}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-4">
        <div className="space-y-1">
          <label htmlFor="new-password" className="block text-sm font-medium text-text">
            {t('resetPassword.newPasswordField')}
          </label>
          <input
            id="new-password"
            type="password"
            required
            autoComplete="new-password"
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
            disabled={isLoading}
            className="w-full px-3 py-2 bg-surface-2 border border-border rounded-lg text-text text-sm focus:outline-none focus:ring-2 focus:ring-focus-ring disabled:opacity-50"
          />
        </div>

        <div className="space-y-1">
          <label htmlFor="confirm-password" className="block text-sm font-medium text-text">
            {t('resetPassword.confirmPasswordField')}
          </label>
          <input
            id="confirm-password"
            type="password"
            required
            autoComplete="new-password"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            disabled={isLoading}
            className="w-full px-3 py-2 bg-surface-2 border border-border rounded-lg text-text text-sm focus:outline-none focus:ring-2 focus:ring-focus-ring disabled:opacity-50"
          />
        </div>

        <button
          type="submit"
          disabled={isLoading}
          className="w-full py-2.5 px-4 bg-accent hover:opacity-90 active:scale-[0.99] text-accent-fg font-medium rounded-lg text-sm transition-all focus:outline-none focus:ring-2 focus:ring-focus-ring disabled:opacity-50 disabled:pointer-events-none"
        >
          {isLoading ? '...' : t('resetPassword.submit')}
        </button>
      </form>
    </div>
  );
};
