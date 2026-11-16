import React, { useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { authApi } from '../api';
import { useAuthStore } from '../../../shared/stores/authStore';
import { ProblemError } from '../../../shared/api/problem';

export const LoginPage: React.FC = () => {
  const { t } = useTranslation('auth');
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const setAuth = useAuthStore((state) => state.setAuth);

  const [login, setLogin] = useState('');
  const [password, setPassword] = useState('');
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMsg(null);

    if (!login.trim() || !password) {
      return;
    }

    setIsLoading(true);
    try {
      const response = await authApi.login({ login: login.trim(), password });
      setAuth(response.user, response.accessToken);

      const nextUrl = searchParams.get('next');
      if (nextUrl && nextUrl.startsWith('/')) {
        navigate(nextUrl, { replace: true });
      } else {
        navigate('/dashboard', { replace: true });
      }
    } catch (err) {
      if (err instanceof ProblemError) {
        if (err.code === 'INVALID_CREDENTIALS') {
          setErrorMsg(t('errors.invalidCredentials'));
        } else if (err.code === 'USER_BLOCKED') {
          setErrorMsg(t('errors.userBlocked'));
        } else {
          setErrorMsg(err.message);
        }
      } else {
        setErrorMsg(t('errors.invalidCredentials'));
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
        <h1 className="text-2xl font-bold tracking-tight text-text">{t('login.title')}</h1>
        <p className="text-sm text-text-muted">{t('login.subtitle')}</p>
      </div>

      {errorMsg && (
        <div role="alert" className="p-3 text-sm rounded bg-error/10 border border-error/20 text-error">
          {errorMsg}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-4">
        <div className="space-y-1">
          <label htmlFor="login-input" className="block text-sm font-medium text-text">
            {t('login.loginField')}
          </label>
          <input
            id="login-input"
            type="text"
            required
            autoComplete="username"
            value={login}
            onChange={(e) => setLogin(e.target.value)}
            disabled={isLoading}
            className="w-full px-3 py-2 bg-surface-2 border border-border rounded-lg text-text text-sm focus:outline-none focus:ring-2 focus:ring-focus-ring disabled:opacity-50"
          />
        </div>

        <div className="space-y-1">
          <div className="flex items-center justify-between">
            <label htmlFor="password-input" className="block text-sm font-medium text-text">
              {t('login.passwordField')}
            </label>
            <Link
              to="/forgot-password"
              className="text-xs text-accent hover:underline focus:outline-none focus:underline"
            >
              {t('login.forgotPassword')}
            </Link>
          </div>
          <input
            id="password-input"
            type="password"
            required
            autoComplete="current-password"
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
          {isLoading ? '...' : t('login.submit')}
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
        <span>{t('login.noAccount')} </span>
        <Link to="/register" className="text-accent font-medium hover:underline focus:outline-none focus:underline">
          {t('login.signUpLink')}
        </Link>
      </div>
    </div>
  );
};
