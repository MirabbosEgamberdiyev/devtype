import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { devtypeApi, DashboardResponse, ActivityDay } from '../../shared/api/devtypeApi';
import { ActivityCalendar } from '../../shared/components/ActivityCalendar';

export const ProfilePage: React.FC = () => {
  const { t } = useTranslation('common');

  const [user, setUser] = useState<{
    id: number;
    username: string;
    email: string;
    role: string;
    country: string | null;
    timezone: string;
    createdAt: string;
  } | null>(null);

  const [dashboard, setDashboard] = useState<DashboardResponse | null>(null);
  const [activityDays, setActivityDays] = useState<ActivityDay[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      devtypeApi.getMe(),
      devtypeApi.getDashboard(),
      devtypeApi.getAnalyticsActivity(),
    ])
      .then(([u, d, a]) => {
        setUser(u);
        setDashboard(d);
        setActivityDays(a.days || []);
        setLoading(false);
      })
      .catch(() => {
        setLoading(false);
      });
  }, []);

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[400px] text-text-muted">
        <div className="w-8 h-8 border-2 border-accent border-t-transparent rounded-full animate-spin mb-3" />
        <p>{t('typing.loading', 'Loading profile...')}</p>
      </div>
    );
  }

  if (!user || !dashboard) {
    return null;
  }

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      {/* Profile Header */}
      <div className="p-6 rounded-2xl border border-border bg-surface flex flex-col sm:flex-row items-center sm:items-start justify-between gap-6">
        <div className="flex flex-col sm:flex-row items-center gap-5 text-center sm:text-left">
          <div className="w-20 h-20 rounded-2xl bg-accent/20 border-2 border-accent/40 flex items-center justify-center text-3xl font-bold text-accent font-mono">
            {user.username.substring(0, 2).toUpperCase()}
          </div>
          <div className="space-y-1">
            <h2 className="text-2xl font-bold text-text">{user.username}</h2>
            <div className="flex flex-wrap items-center justify-center sm:justify-start gap-2 text-xs text-text-muted">
              <span className="px-2 py-0.5 rounded bg-surface-2 border border-border/80 uppercase font-mono">
                {user.role}
              </span>
              <span>·</span>
              <span>{user.timezone}</span>
              <span>·</span>
              <span>
                {t('profile.joined', 'Joined')}{' '}
                {new Date(user.createdAt).toLocaleDateString()}
              </span>
            </div>
          </div>
        </div>

        <Link
          to="/settings"
          className="px-4 py-2 bg-surface-2 hover:bg-surface-3 border border-border rounded-xl text-xs font-semibold text-text transition-colors"
        >
          ⚙️ {t('profile.editSettings', 'Edit Settings')}
        </Link>
      </div>

      {/* Gamification High-Level Metrics */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
        <div className="p-4 rounded-xl border border-border bg-surface text-center">
          <div className="text-xs text-text-muted">{t('gamification.level', 'Level')}</div>
          <div className="text-2xl font-bold font-mono text-text mt-1">
            {dashboard.gamification.currentLevel}
          </div>
        </div>
        <div className="p-4 rounded-xl border border-border bg-surface text-center">
          <div className="text-xs text-text-muted">{t('metrics.xp', 'Total XP')}</div>
          <div className="text-2xl font-bold font-mono text-accent mt-1">
            {dashboard.gamification.totalXp.toLocaleString()}
          </div>
        </div>
        <div className="p-4 rounded-xl border border-border bg-surface text-center">
          <div className="text-xs text-text-muted">{t('gamification.streak', 'Streak')}</div>
          <div className="text-2xl font-bold font-mono text-amber-400 mt-1">
            🔥 {dashboard.gamification.streakDays}
          </div>
        </div>
        <div className="p-4 rounded-xl border border-border bg-surface text-center">
          <div className="text-xs text-text-muted">{t('dashboard.languages', 'Languages')}</div>
          <div className="text-2xl font-bold font-mono text-text mt-1">
            {dashboard.languages.length}
          </div>
        </div>
      </div>

      {/* Languages and Ranks */}
      <div className="p-6 rounded-2xl border border-border bg-surface space-y-4">
        <h3 className="text-base font-bold text-text">
          {t('profile.languageRanks', 'Language Ranks & Records')}
        </h3>
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-3">
          {dashboard.languages.map((l) => (
            <div
              key={l.language}
              className="p-3 rounded-xl border border-border/80 bg-surface-2 flex items-center justify-between"
            >
              <div>
                <div className="font-bold text-text text-sm">{l.languageName}</div>
                <span className="text-[10px] font-mono px-1.5 py-0.5 rounded bg-surface border border-border uppercase">
                  {l.rank || 'UNRANKED'}
                </span>
              </div>
              <div className="text-right">
                <div className="text-base font-bold font-mono text-accent">{l.bestWpm} WPM</div>
                <div className="text-[11px] text-text-muted">{l.accuracy}% acc</div>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Activity Heatmap */}
      <ActivityCalendar days={activityDays} />
    </div>
  );
};
