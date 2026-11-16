import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { authApi } from '../api';

export const ForgotPasswordPage: React.FC = () => {
  const { t } = useTranslation('auth');
  const [email, setEmail] = useState('');
  const [isSubmitted, setIsSubmitted] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email.trim()) return;

    setIsLoading(true);
    try {
      await authApi.forgotPassword({ email: email.trim() });
    } catch {
      // Intentionally suppress error for user anti-enumeration (AC-UM-5)
    } finally {
      setIsLoading(false);
      setIsSubmitted(true);
    }
  };

  if (isSubmitted) {
    return (
      <div className="space-y-6 text-center">
        <div className="space-y-2">
          <div className="mx-auto w-12 h-12 rounded-full bg-accent/10 text-accent flex items-center justify-center text-xl">
            ✉️
          </div>
          <h1 className="text-2xl font-bold tracking-tight text-text">{t('forgotPassword.successTitle')}</h1>
          <p className="text-sm text-text-muted leading-relaxed">{t('forgotPassword.successMessage')}</p>
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
        <h1 className="text-2xl font-bold tracking-tight text-text">{t('forgotPassword.title')}</h1>
        <p className="text-sm text-text-muted">{t('forgotPassword.subtitle')}</p>
      </div>

      <form onSubmit={handleSubmit} className="space-y-4">
        <div className="space-y-1">
          <label htmlFor="forgot-email" className="block text-sm font-medium text-text">
            {t('forgotPassword.emailField')}
          </label>
          <input
            id="forgot-email"
            type="email"
            required
            autoComplete="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            disabled={isLoading}
            className="w-full px-3 py-2 bg-surface-2 border border-border rounded-lg text-text text-sm focus:outline-none focus:ring-2 focus:ring-focus-ring disabled:opacity-50"
          />
        </div>

        <button
          type="submit"
          disabled={isLoading}
          className="w-full py-2.5 px-4 bg-accent hover:opacity-90 active:scale-[0.99] text-accent-fg font-medium rounded-lg text-sm transition-all focus:outline-none focus:ring-2 focus:ring-focus-ring disabled:opacity-50 disabled:pointer-events-none"
        >
          {isLoading ? '...' : t('forgotPassword.submit')}
        </button>
      </form>

      <div className="text-center text-sm">
        <Link to="/login" className="text-accent font-medium hover:underline focus:outline-none focus:underline">
          {t('forgotPassword.backToLogin')}
        </Link>
      </div>
    </div>
  );
};
