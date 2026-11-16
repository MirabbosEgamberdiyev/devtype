import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { authApi } from '../api';
import { useAuthStore } from '../../../shared/stores/authStore';
import { ProblemError } from '../../../shared/api/problem';

const USERNAME_REGEX = /^[A-Za-z][A-Za-z0-9_]{2,19}$/;

export const RegisterPage: React.FC = () => {
  const { t, i18n } = useTranslation('auth');
  const navigate = useNavigate();
  const { guestToken, setAuth } = useAuthStore();

  const [email, setEmail] = useState('');
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMsg(null);

    const cleanUsername = username.trim();
    const cleanEmail = email.trim();

    if (!cleanEmail || !cleanUsername || !password) {
      return;
    }

    if (!USERNAME_REGEX.test(cleanUsername)) {
      setErrorMsg(t('errors.usernameInvalid'));
      return;
    }

    if (password.length < 8) {
      setErrorMsg(t('errors.passwordTooShort'));
      return;
    }

    setIsLoading(true);
    try {
      const response = await authApi.register({
        email: cleanEmail,
        username: cleanUsername,
        password,
        locale: i18n.language || 'en',
        timezone: Intl.DateTimeFormat().resolvedOptions().timeZone || 'UTC',
        guestToken,
      });

      setAuth(response.user, response.accessToken);
      navigate(response.user.onboarded ? '/dashboard' : '/onboarding', { replace: true });
    } catch (err) {
      if (err instanceof ProblemError) {
        if (err.code === 'EMAIL_TAKEN') {
          setErrorMsg(t('errors.emailTaken'));
        } else if (err.code === 'USERNAME_TAKEN') {
          setErrorMsg(t('errors.usernameTaken'));
        } else if (err.code === 'VALIDATION_ERROR' && err.invalidParams?.length) {
          setErrorMsg(err.invalidParams[0]?.reason || err.message);
        } else {
          setErrorMsg(err.message);
        }
      } else {
        setErrorMsg('Registration failed. Please try again.');
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleOAuthLogin = async (provider: 'github' | 'google') => {
    setIsLoading(true);
    setErrorMsg(null);
    try {
      const dummyUid = `${provider}_user_${Date.now()}`;
      const response = await authApi.oauthCallback(provider, {
        email: `${dummyUid}@developer.dev`,
        providerUid: dummyUid,
        displayName: `${provider.toUpperCase()} Developer`,
      });
      setAuth(response.user, response.accessToken);
      navigate('/dashboard', { replace: true });
    } catch (err) {
      if (err instanceof ProblemError) {
        setErrorMsg(err.message);
      } else {
        setErrorMsg('OAuth authentication failed');
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="space-y-1.5 text-center">
        <h1 className="text-2xl font-bold tracking-tight text-text">{t('register.title')}</h1>
        <p className="text-sm text-text-muted">{t('register.subtitle')}</p>
      </div>

      {guestToken && (
        <div className="p-3 text-xs rounded bg-accent/10 border border-accent/20 text-accent">
          {t('register.guestSessionsNotice')}
        </div>
      )}

      {errorMsg && (
        <div role="alert" className="p-3 text-sm rounded bg-error/10 border border-error/20 text-error">
          {errorMsg}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-4">
        <div className="space-y-1">
          <label htmlFor="reg-email" className="block text-sm font-medium text-text">
            {t('register.emailField')}
          </label>
          <input
            id="reg-email"
            type="email"
            required
            autoComplete="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            disabled={isLoading}
            className="w-full px-3 py-2 bg-surface-2 border border-border rounded-lg text-text text-sm focus:outline-none focus:ring-2 focus:ring-focus-ring disabled:opacity-50"
          />
        </div>

        <div className="space-y-1">
          <label htmlFor="reg-username" className="block text-sm font-medium text-text">
            {t('register.usernameField')}
          </label>
          <input
            id="reg-username"
            type="text"
            required
            autoComplete="username"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            disabled={isLoading}
            className="w-full px-3 py-2 bg-surface-2 border border-border rounded-lg text-text text-sm focus:outline-none focus:ring-2 focus:ring-focus-ring disabled:opacity-50"
          />
        </div>

        <div className="space-y-1">
          <label htmlFor="reg-password" className="block text-sm font-medium text-text">
            {t('register.passwordField')}
          </label>
          <input
            id="reg-password"
            type="password"
            required
            autoComplete="new-password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            disabled={isLoading}
            className="w-full px-3 py-2 bg-surface-2 border border-border rounded-lg text-text text-sm focus:outline-none focus:ring-2 focus:ring-focus-ring disabled:opacity-50"
          />
        </div>

        <button
          type="submit"
          disabled={isLoading}
          className="w-full py-2.5 px-4 bg-accent hover:opacity-90 active:scale-[0.99] text-accent-fg font-medium rounded-lg text-sm transition-all focus:outline-none focus:ring-2 focus:ring-focus-ring disabled:opacity-50 disabled:pointer-events-none"
        >
          {isLoading ? '...' : t('register.submit')}
        </button>
      </form>

      <div className="relative">
        <div className="absolute inset-0 flex items-center">
          <div className="w-full border-t border-border" />
        </div>
        <div className="relative flex justify-center text-xs uppercase">
          <span className="bg-surface px-2 text-text-muted">{t('login.orOAuth')}</span>
        </div>
      </div>

      <div className="grid grid-cols-2 gap-3">
        <button
          type="button"
          onClick={() => handleOAuthLogin('github')}
          disabled={isLoading}
          className="flex items-center justify-center gap-2 py-2 px-3 bg-surface-2 hover:bg-surface border border-border rounded-lg text-sm font-medium text-text transition-colors disabled:opacity-50"
        >
          <span>GitHub</span>
        </button>
        <button
          type="button"
          onClick={() => handleOAuthLogin('google')}
          disabled={isLoading}
          className="flex items-center justify-center gap-2 py-2 px-3 bg-surface-2 hover:bg-surface border border-border rounded-lg text-sm font-medium text-text transition-colors disabled:opacity-50"
        >
          <span>Google</span>
        </button>
      </div>

      <div className="text-center text-sm text-text-muted">
        <span>{t('register.hasAccount')} </span>
        <Link to="/login" className="text-accent font-medium hover:underline focus:outline-none focus:underline">
          {t('register.signInLink')}
        </Link>
      </div>
    </div>
  );
};
