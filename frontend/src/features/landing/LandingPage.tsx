import React, { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';
import { useAuthStore } from '../../shared/stores/authStore';
import { authApi } from '../auth/api';
import { TypingView } from '../typing/TypingView';

const DEMO_LANGUAGES = [
  { slug: 'java', label: 'Java' },
  { slug: 'python', label: 'Python' },
  { slug: 'javascript', label: 'JavaScript' },
  { slug: 'sql', label: 'SQL' },
  { slug: 'plsql', label: 'PL/SQL' },
];

export const LandingPage: React.FC = () => {
  const { t } = useTranslation();
  const { guestToken, setGuestToken, isAuthenticated } = useAuthStore();
  const [selectedLang, setSelectedLang] = useState<string>('java');
  const [guestLoading, setGuestLoading] = useState<boolean>(false);

  // Mehmon tokenini olish (docs/04 §1.1, AC-UM-7)
  useEffect(() => {
    if (!isAuthenticated && !guestToken) {
      setGuestLoading(true);
      authApi
        .getGuestToken()
        .then((res) => {
          setGuestToken(res.guestToken);
        })
        .catch((err) => {
          console.warn('Guest token fetch error:', err);
        })
        .finally(() => {
          setGuestLoading(false);
        });
    }
  }, [isAuthenticated, guestToken, setGuestToken]);

  return (
    <div className="flex flex-col items-center justify-center gap-16 py-8 max-w-6xl mx-auto w-full px-4">
      {/* Hero Section */}
      <div className="flex flex-col items-center text-center gap-5 max-w-3xl mx-auto">
        <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-accent-primary/10 border border-accent-primary/20 text-accent-primary text-xs font-mono font-medium">
          <span className="w-2 h-2 rounded-full bg-accent-primary animate-pulse" />
          <span>DevType v0.1.0 · Developer Typing Trainer</span>
        </div>

        <h1 className="text-4xl sm:text-5xl lg:text-6xl font-extrabold tracking-tight text-text-primary leading-[1.15]">
          {t('landing.heroTitle')}
        </h1>

        <p className="text-base sm:text-lg text-text-muted leading-relaxed max-w-2xl">
          {t('landing.heroSubtitle')}
        </p>

        <div className="flex flex-wrap items-center justify-center gap-4 mt-2">
          <Link
            to="/practice"
            className="px-6 py-3 rounded-xl font-semibold bg-accent-primary text-white hover:bg-accent-hover transition-colors shadow-sm text-sm"
          >
            {t('actions.start')}
          </Link>
          <Link
            to="/register"
            className="px-6 py-3 rounded-xl font-semibold bg-bg-surface border border-border-default text-text-primary hover:bg-bg-subtle transition-colors shadow-sm text-sm"
          >
            {t('nav.register')}
          </Link>
        </div>
      </div>

      {/* Interactive 30-second Speed Test Demo (docs/04 §1.1, docs/05 §5.1) */}
      <div className="w-full max-w-4xl flex flex-col gap-4">
        <div className="flex flex-wrap items-center justify-between gap-3 px-6 py-3 rounded-xl bg-bg-surface border border-border-default shadow-sm">
          <div className="flex items-center gap-2">
            <span className="text-sm font-semibold text-text-primary">{t('landing.tryDemo')}</span>
            <span className="px-2 py-0.5 rounded text-[10px] font-mono font-bold bg-amber-500/10 text-amber-500 border border-amber-500/20">
              30s
            </span>
          </div>

          {/* Language Selector */}
          <div className="flex items-center gap-1.5 font-mono text-xs">
            <span className="text-text-muted mr-1 hidden sm:inline">{t('landing.selectLanguage')}:</span>
            {DEMO_LANGUAGES.map((lang) => (
              <button
                key={lang.slug}
                onClick={() => setSelectedLang(lang.slug)}
                className={`px-2.5 py-1 rounded-lg font-medium transition-colors ${
                  selectedLang === lang.slug
                    ? 'bg-accent-primary text-white shadow-sm'
                    : 'bg-bg-subtle text-text-muted hover:text-text-primary'
                }`}
              >
                {lang.label}
              </button>
            ))}
          </div>
        </div>

        {/* Live Typing Demo Container */}
        <div className="rounded-2xl border border-border-default bg-bg-surface/50 p-4 shadow-md backdrop-blur-sm">
          {guestLoading ? (
            <div className="flex flex-col items-center justify-center p-16 gap-3 text-text-muted">
              <div className="w-7 h-7 rounded-full border-2 border-accent-primary border-t-transparent animate-spin" />
              <span className="text-xs font-mono">{t('typing.loading')}</span>
            </div>
          ) : (
            <TypingView
              key={`${selectedLang}-demo`}
              language={selectedLang}
              mode="SPEED_TEST"
              duration={30}
            />
          )}
        </div>
      </div>

      {/* Feature Highlights Grid */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 w-full max-w-5xl">
        <div className="p-6 rounded-2xl bg-bg-surface border border-border-default flex flex-col gap-3 shadow-sm hover:border-accent-primary/40 transition-colors">
          <div className="w-10 h-10 rounded-xl bg-accent-primary/10 text-accent-primary flex items-center justify-center font-mono text-lg font-bold">
            {'</>'}
          </div>
          <h3 className="text-base font-bold text-text-primary">{t('landing.featureCodeTitle')}</h3>
          <p className="text-sm text-text-muted leading-relaxed">{t('landing.featureCodeDesc')}</p>
        </div>

        <div className="p-6 rounded-2xl bg-bg-surface border border-border-default flex flex-col gap-3 shadow-sm hover:border-accent-primary/40 transition-colors">
          <div className="w-10 h-10 rounded-xl bg-emerald-500/10 text-emerald-500 flex items-center justify-center font-mono text-lg font-bold">
            {'{}'}
          </div>
          <h3 className="text-base font-bold text-text-primary">{t('landing.featureSymbolsTitle')}</h3>
          <p className="text-sm text-text-muted leading-relaxed">{t('landing.featureSymbolsDesc')}</p>
        </div>

        <div className="p-6 rounded-2xl bg-bg-surface border border-border-default flex flex-col gap-3 shadow-sm hover:border-accent-primary/40 transition-colors">
          <div className="w-10 h-10 rounded-xl bg-sky-500/10 text-sky-500 flex items-center justify-center font-mono text-lg font-bold">
            {'⚡'}
          </div>
          <h3 className="text-base font-bold text-text-primary">{t('landing.featureAnalyticsTitle')}</h3>
          <p className="text-sm text-text-muted leading-relaxed">{t('landing.featureAnalyticsDesc')}</p>
        </div>
      </div>
    </div>
  );
};
