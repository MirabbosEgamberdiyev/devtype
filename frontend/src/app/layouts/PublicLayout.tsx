import React from 'react';
import { Outlet, Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { SupportedLocale, SUPPORTED_LOCALES, LOCALE_LABELS } from '../../shared/i18n';

export const PublicLayout: React.FC = () => {
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
    <div className="min-h-screen bg-bg text-text flex flex-col">
      <header className="h-16 border-b border-border bg-surface px-6 flex items-center justify-between">
        <Link to="/" className="flex items-center gap-2 font-bold text-lg text-accent">
          <span>DevType</span>
        </Link>
        <div className="flex items-center gap-4">
          <select
            value={i18n.language}
            onChange={handleLocaleChange}
            aria-label="Language selector"
            className="bg-surface-2 border border-border text-text text-sm rounded px-2 py-1 outline-none focus:ring-2 focus:ring-focus-ring"
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
            className="p-1.5 rounded bg-surface-2 border border-border text-sm hover:border-border-strong"
          >
            🌓
          </button>
          <Link
            to="/login"
            className="text-sm font-medium px-3 py-1.5 rounded bg-accent text-accent-fg hover:opacity-90"
          >
            {t('nav.login')}
          </Link>
        </div>
      </header>
      <main className="flex-1 max-w-7xl w-full mx-auto p-6">
        <Outlet />
      </main>
      <footer className="h-14 border-t border-border bg-surface px-6 flex items-center justify-between text-xs text-text-muted">
        <div>DevType © 2026. {t('app.tagline')}.</div>
      </footer>
    </div>
  );
};
