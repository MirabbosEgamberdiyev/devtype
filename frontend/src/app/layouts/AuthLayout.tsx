import React from 'react';
import { Outlet, Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { SupportedLocale, SUPPORTED_LOCALES, LOCALE_LABELS } from '../../shared/i18n';

export const AuthLayout: React.FC = () => {
  const { t, i18n } = useTranslation('common');

  const handleLocaleChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const nextLocale = e.target.value as SupportedLocale;
    i18n.changeLanguage(nextLocale);
    localStorage.setItem('dt_locale', nextLocale);
    document.documentElement.setAttribute('lang', nextLocale);
  };

  const toggleTheme = () => {
    const current = document.documentElement.getAttribute('data-theme') || 'dark';
    const next = current === 'dark' ? 'light' : 'dark';
    document.documentElement.setAttribute('data-theme', next);
    localStorage.setItem('dt_theme', next);
  };

  return (
    <div className="min-h-screen bg-bg text-text flex flex-col justify-between">
      <header className="h-16 border-b border-border bg-surface px-6 flex items-center justify-between">
        <Link to="/" className="flex items-center gap-2 font-bold text-lg text-accent tracking-tight">
          <span>DevType</span>
        </Link>
        <div className="flex items-center gap-3">
          <select
            value={i18n.language}
            onChange={handleLocaleChange}
            aria-label="Language selector"
            className="bg-surface-2 border border-border text-text text-sm rounded px-2.5 py-1 outline-none focus:ring-2 focus:ring-focus-ring cursor-pointer"
          >
            {SUPPORTED_LOCALES.map((loc) => (
              <option key={loc} value={loc}>
                {LOCALE_LABELS[loc]}
              </option>
            ))}
          </select>
          <button
            onClick={toggleTheme}
            type="button"
            aria-label="Toggle theme"
            className="p-1.5 rounded bg-surface-2 border border-border text-sm hover:border-border-strong transition-colors"
          >
            🌓
          </button>
        </div>
      </header>

      <main className="flex-1 flex items-center justify-center p-4 sm:p-6 my-8">
        <div className="w-full max-w-md bg-surface border border-border rounded-xl p-6 sm:p-8 shadow-xl">
          <Outlet />
        </div>
      </main>

      <footer className="h-14 border-t border-border bg-surface px-6 flex items-center justify-center text-xs text-text-muted">
        <div>DevType © 2026. {t('app.tagline')}.</div>
      </footer>
    </div>
  );
};
