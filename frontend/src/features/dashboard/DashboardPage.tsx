import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { devtypeApi, DashboardResponse } from '../../shared/api/devtypeApi';
import { Sparkline } from '../../shared/components/Sparkline';

export const DashboardPage: React.FC = () => {
  const { t } = useTranslation('common');
  const navigate = useNavigate();
  const [data, setData] = useState<DashboardResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let mounted = true;
    devtypeApi
      .getDashboard()
      .then((res) => {
        if (mounted) {
          setData(res);
          setLoading(false);
        }
      })
      .catch((err) => {
        if (mounted) {
          setError(err.message || 'Failed to load dashboard');
          setLoading(false);
        }
      });

    return () => {
      mounted = false;
    };
  }, []);

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[400px] text-text-muted">
        <div className="w-8 h-8 border-2 border-accent border-t-transparent rounded-full animate-spin mb-3" />
        <p>{t('typing.loading', 'Loading dashboard...')}</p>
      </div>
    );
  }

  if (error || !data) {
    return (
      <div className="p-8 rounded-xl border border-red-500/30 bg-red-500/10 text-center max-w-xl mx-auto my-12">
        <h2 className="text-lg font-bold text-red-400 mb-2">{t('common.error', 'Error')}</h2>
        <p className="text-sm text-text-muted mb-4">{error}</p>
        <button
          onClick={() => window.location.reload()}
          className="px-4 py-2 bg-accent text-accent-fg rounded-lg font-medium hover:opacity-90 transition-opacity"
        >
          {t('actions.retry', 'Try Again')}
        </button>
      </div>
    );
  }

  const { isNewUser, resume, gamification, languages, recentSessions } = data;

  const xpPercent =
    gamification.xpToNext > 0
      ? Math.min(100, Math.round((gamification.xpIntoLevel / gamification.xpToNext) * 100))
      : 100;

  return (
    <div className="space-y-6">
      {/* New User Welcome Banner (AC-UD-1) */}
      {isNewUser && (
        <div className="p-5 rounded-2xl border border-accent/40 bg-accent/10 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
          <div>
            <h2 className="text-lg font-bold text-accent">
              {t('dashboard.welcomeTitle', 'Welcome to DevType!')}
            </h2>
            <p className="text-sm text-text-muted mt-1">
              {t(
                'dashboard.welcomeDesc',
                'Your developer typing journey starts here. Build muscle memory on real code syntax, operators, and production patterns.'
              )}
            </p>
          </div>
          <Link
            to="/languages"
            className="px-5 py-2.5 rounded-xl bg-accent text-accent-fg font-semibold hover:opacity-90 whitespace-nowrap transition-opacity shadow-sm"
          >
            {t('dashboard.pickLanguages', 'Choose Languages')}
          </Link>
        </div>
      )}

      {/* Streak atRisk Banner (AC-UD-3) */}
      {gamification.streakAtRisk && (
        <div className="p-4 rounded-xl border border-amber-500/40 bg-amber-500/10 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <span className="text-2xl">⚠️</span>
            <div>
              <p className="font-semibold text-amber-300">
                {t('dashboard.streakAtRiskTitle', 'Streak at Risk!')}
              </p>
              <p className="text-xs text-amber-200/80">
                {t(
                  'dashboard.streakAtRiskDesc',
                  'Train today before midnight to maintain your streak and avoid consuming a streak freeze.'
                )}
              </p>
            </div>
          </div>
          <Link
            to="/practice"
            className="px-4 py-2 bg-amber-500 text-amber-950 font-bold rounded-lg text-sm hover:bg-amber-400 transition-colors"
          >
            {t('dashboard.trainNow', 'Train Now')}
          </Link>
        </div>
      )}

      {/* Top Grid: Resume CTA & Gamification Stats */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Resume Card (BL-UD-1) */}
        <div className="lg:col-span-2 p-6 rounded-2xl border border-border bg-surface flex flex-col justify-between relative overflow-hidden">
          <div className="space-y-2">
            <div className="flex items-center gap-2">
              <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-accent/20 text-accent uppercase tracking-wider">
                {resume ? resume.type.replace('_', ' ') : 'PRACTICE'}
              </span>
              {resume?.language && (
                <span className="text-xs font-mono text-text-muted uppercase">
                  {resume.language}
                </span>
              )}
            </div>
            <h3 className="text-2xl font-bold text-text">
              {resume?.exerciseTitle || t('dashboard.continueTraining', 'Continue Training')}
            </h3>
            <p className="text-sm text-text-muted">
              {resume?.message ||
                t(
                  'dashboard.defaultResumeDesc',
                  'Sharpen your syntax typing speed and eliminate high-penalty error patterns.'
                )}
            </p>
          </div>

          <div className="mt-6 flex flex-wrap items-center gap-3">
            <button
              onClick={() => {
                if (resume?.language) {
                  navigate(
                    resume.topicId
                      ? `/practice/${resume.language}/${resume.topicId}`
                      : `/practice/${resume.language}`
                  );
                } else {
                  navigate('/practice');
                }
              }}
              className="px-6 py-3 rounded-xl bg-accent text-accent-fg font-semibold hover:opacity-90 transition-opacity shadow-sm flex items-center gap-2"
            >
              <span>▶</span>
              <span>{t('dashboard.resumeCta', 'Start Next Exercise')}</span>
            </button>
            <Link
              to="/speed-test"
              className="px-4 py-3 rounded-xl bg-surface-2 border border-border text-text font-medium hover:bg-surface-3 transition-colors text-sm"
            >
              ⚡ {t('nav.speedTest', 'Speed Test')}
            </Link>
          </div>
        </div>

        {/* Gamification Card (AC-UD-5) */}
        <div className="p-6 rounded-2xl border border-border bg-surface flex flex-col justify-between space-y-6">
          <div>
            <div className="flex items-center justify-between mb-4">
              <div className="flex items-center gap-2">
                <span className="text-2xl">🔥</span>
                <div>
                  <div className="text-xl font-bold font-mono">
                    {gamification.streakDays} {t('gamification.days', 'days')}
                  </div>
                  <div className="text-xs text-text-muted">{t('gamification.streak', 'Streak')}</div>
                </div>
              </div>
              <div className="text-right">
                <div className="text-xl font-bold text-accent font-mono">
                  {gamification.totalXp.toLocaleString()}
                </div>
                <div className="text-xs text-text-muted">{t('metrics.xp', 'Total XP')}</div>
              </div>
            </div>

            {/* Level Progress */}
            <div className="space-y-2">
              <div className="flex items-center justify-between text-xs">
                <span className="font-semibold text-text">
                  {t('gamification.level', 'Level')} {gamification.currentLevel}
                </span>
                <span className="text-text-muted font-mono">
                  {gamification.xpIntoLevel} / {gamification.xpToNext} XP
                </span>
              </div>
              <div className="h-2 w-full bg-surface-2 rounded-full overflow-hidden">
                <div
                  className="h-full bg-accent rounded-full transition-all duration-500"
                  style={{ width: `${xpPercent}%` }}
                />
              </div>
            </div>
          </div>

          <div className="pt-4 border-t border-border flex items-center justify-between text-xs text-text-muted">
            <span>{t('dashboard.freezesAvailable', 'Streak Freezes:')}</span>
            <span className="font-bold text-text">
              {gamification.streakFreezesRemaining} {t('dashboard.available', 'available')}
            </span>
          </div>
        </div>
      </div>

      {/* Languages Section with Sparklines (AC-UD-4) */}
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <h3 className="text-lg font-bold text-text">{t('dashboard.myLanguages', 'My Languages')}</h3>
          <Link
            to="/languages"
            className="text-xs text-accent hover:underline font-medium"
          >
            {t('dashboard.manageLanguages', 'Manage Languages')} →
          </Link>
        </div>

        {languages.length === 0 ? (
          <div className="p-8 rounded-xl border border-dashed border-border text-center text-text-muted bg-surface/50">
            <p className="mb-3">{t('dashboard.noLanguages', 'No languages selected yet.')}</p>
            <Link
              to="/languages"
              className="px-4 py-2 bg-accent text-accent-fg rounded-lg text-sm font-semibold hover:opacity-90"
            >
              {t('dashboard.pickLanguages', 'Choose Languages')}
            </Link>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {languages.map((lang) => (
              <div
                key={lang.language}
                className="p-5 rounded-xl border border-border bg-surface hover:border-border-strong transition-colors flex flex-col justify-between"
              >
                <div className="flex items-start justify-between">
                  <div>
                    <h4 className="font-bold text-text">{lang.languageName}</h4>
                    <span className="text-xs px-2 py-0.5 rounded bg-surface-2 text-text-muted uppercase font-mono">
                      {lang.rank || 'UNRANKED'}
                    </span>
                  </div>
                  <div className="text-right">
                    <div className="text-xl font-bold font-mono text-accent">
                      {lang.bestWpm} <span className="text-xs font-normal text-text-muted">WPM</span>
                    </div>
                    <div className="text-xs text-text-muted font-mono">{lang.accuracy}% acc</div>
                  </div>
                </div>

                <div className="mt-4 pt-3 border-t border-border/50 flex items-center justify-between">
                  <div className="text-xs text-text-muted">
                    {lang.sessionsCount} {t('dashboard.sessions', 'sessions')}
                  </div>
                  <Sparkline data={lang.sparkline} width={110} height={32} />
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Recent Sessions Table (AC-UD-6) */}
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <h3 className="text-lg font-bold text-text">{t('dashboard.recentSessions', 'Recent Sessions')}</h3>
          <Link to="/history" className="text-xs text-accent hover:underline font-medium">
            {t('dashboard.viewAllHistory', 'Full History')} →
          </Link>
        </div>

        {recentSessions.length === 0 ? (
          <div className="p-8 rounded-xl border border-dashed border-border text-center text-text-muted bg-surface/50">
            {t('dashboard.noRecentSessions', 'No completed sessions yet. Start your first exercise!')}
          </div>
        ) : (
          <div className="overflow-x-auto rounded-xl border border-border bg-surface">
            <table className="w-full text-left text-sm">
              <thead className="border-b border-border bg-surface-2 text-xs text-text-muted uppercase font-mono">
                <tr>
                  <th className="p-3 pl-4">{t('metrics.language', 'Language')}</th>
                  <th className="p-3">{t('metrics.mode', 'Mode')}</th>
                  <th className="p-3">{t('metrics.wpm', 'WPM')}</th>
                  <th className="p-3">{t('metrics.accuracy', 'Accuracy')}</th>
                  <th className="p-3">{t('metrics.xp', 'XP')}</th>
                  <th className="p-3 pr-4 text-right">{t('metrics.time', 'Date')}</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border/60">
                {recentSessions.map((session) => (
                  <tr key={session.sessionId} className="hover:bg-surface-2/60 transition-colors">
                    <td className="p-3 pl-4 font-mono font-medium text-accent">
                      {session.language.toUpperCase()}
                    </td>
                    <td className="p-3 text-xs">
                      <span className="px-2 py-0.5 rounded bg-surface-2 border border-border/60">
                        {session.mode}
                      </span>
                    </td>
                    <td className="p-3 font-mono font-bold">{session.wpm}</td>
                    <td className="p-3 font-mono text-text-muted">{session.accuracy}%</td>
                    <td className="p-3 font-mono text-emerald-400">+{session.earnedXp}</td>
                    <td className="p-3 pr-4 text-right text-xs text-text-muted">
                      {new Date(session.completedAt).toLocaleDateString()}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};
