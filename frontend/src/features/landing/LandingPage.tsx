import React from 'react';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';

export const LandingPage: React.FC = () => {
  const { t } = useTranslation('common');

  return (
    <div className="flex flex-col items-center justify-center py-20 text-center gap-6">
      <h1 className="text-4xl sm:text-5xl font-extrabold tracking-tight">
        <span className="text-accent">{t('app.name')}</span> — {t('app.tagline')}
      </h1>
      <p className="max-w-2xl text-text-muted text-lg">
        {t('modes.practice')}, {t('modes.speedTest')}, {t('modes.specialChars')}.
      </p>
      <div className="flex gap-4 mt-4">
        <Link
          to="/practice"
          className="px-6 py-3 rounded-lg font-semibold bg-accent text-accent-fg hover:opacity-90 shadow"
        >
          {t('actions.start')}
        </Link>
        <Link
          to="/register"
          className="px-6 py-3 rounded-lg font-semibold bg-surface-2 border border-border text-text hover:border-border-strong"
        >
          {t('nav.register')}
        </Link>
      </div>
    </div>
  );
};
