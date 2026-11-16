import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import {
  devtypeApi,
  AnalyticsOverview,
  ActivityDay,
  CharErrorStat,
} from '../../shared/api/devtypeApi';
import { ActivityCalendar } from '../../shared/components/ActivityCalendar';

export const AnalyticsPage: React.FC = () => {
  const { t } = useTranslation('common');

  const [selectedLanguage, setSelectedLanguage] = useState<string>('');
  const [overview, setOverview] = useState<AnalyticsOverview | null>(null);
  const [activityDays, setActivityDays] = useState<ActivityDay[]>([]);
  const [chars, setChars] = useState<CharErrorStat[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let mounted = true;
    setLoading(true);

    Promise.all([
      devtypeApi.getAnalyticsOverview(selectedLanguage || undefined),
      devtypeApi.getAnalyticsActivity(),
      selectedLanguage ? devtypeApi.getAnalyticsChars(selectedLanguage) : Promise.resolve({ chars: [] }),
    ])
      .then(([ov, act, ch]) => {
        if (mounted) {
          setOverview(ov);
          setActivityDays(act.days || []);
          setChars(ch.chars || []);
          setLoading(false);
        }
      })
      .catch((err) => {
        if (mounted) {
          setError(err.message || 'Failed to load analytics data');
          setLoading(false);
        }
      });

    return () => {
      mounted = false;
    };
  }, [selectedLanguage]);

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[400px] text-text-muted">
        <div className="w-8 h-8 border-2 border-accent border-t-transparent rounded-full animate-spin mb-3" />
        <p>{t('typing.loading', 'Loading analytics...')}</p>
      </div>
    );
  }

  if (error || !overview) {
    return (
      <div className="p-8 rounded-xl border border-red-500/30 bg-red-500/10 text-center max-w-xl mx-auto my-12">
        <h2 className="text-lg font-bold text-red-400 mb-2">{t('common.error', 'Error')}</h2>
        <p className="text-sm text-text-muted mb-4">{error}</p>
        <button
          onClick={() => window.location.reload()}
          className="px-4 py-2 bg-accent text-accent-fg rounded-lg font-medium hover:opacity-90"
        >
          {t('actions.retry', 'Try Again')}
        </button>
      </div>
    );
  }

  // AC-AN-8: Empty State when < 5 sessions
  if (!overview.hasEnoughData) {
    return (
      <div className="p-12 rounded-2xl border border-dashed border-border bg-surface text-center max-w-2xl mx-auto my-8 space-y-4">
        <span className="text-4xl block">📊</span>
        <h2 className="text-xl font-bold text-text">
          {t('analytics.notEnoughDataTitle', 'Not Enough Training Data')}
        </h2>
        <p className="text-sm text-text-muted">
          {t(
            'analytics.notEnoughDataDesc',
            'Complete at least 5 sessions to unlock comprehensive analytics, error clustering, WPM trendlines, and weakness profiling.'
          )}
        </p>
        <div className="pt-2">
          <Link
            to="/practice"
            className="inline-block px-6 py-2.5 bg-accent text-accent-fg font-semibold rounded-xl hover:opacity-90 transition-opacity"
          >
            {t('dashboard.resumeCta', 'Start Practice')}
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6 max-w-6xl mx-auto">
      {/* Header & Filter */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-text">{t('analytics.title', 'Analytics & Insights')}</h2>
          <p className="text-sm text-text-muted mt-1">
            {t('analytics.subtitle', 'Deep diagnostic telemetry into your syntax typing rhythm and accuracy.')}
          </p>
        </div>

        <div>
          <select
            value={selectedLanguage}
            onChange={(e) => setSelectedLanguage(e.target.value)}
            className="bg-surface-2 border border-border text-sm rounded-lg px-3 py-2 text-text outline-none focus:ring-2 focus:ring-accent"
          >
            <option value="">{t('history.allLanguages', 'All Languages')}</option>
            <option value="sql">SQL</option>
            <option value="plsql">PL/SQL</option>
            <option value="java">Java</option>
            <option value="python">Python</option>
            <option value="javascript">JavaScript</option>
          </select>
        </div>
      </div>

      {/* Weakest Language Warning (AC-AN-6) */}
      {overview.weakestLanguage && (
        <div className="p-4 rounded-xl border border-amber-500/40 bg-amber-500/10 flex items-start justify-between gap-4">
          <div className="flex items-start gap-3">
            <span className="text-2xl">💡</span>
            <div>
              <p className="font-semibold text-amber-300">
                {t('analytics.weakestLangTitle', 'Focus Opportunity Identified')}
              </p>
              <p className="text-xs text-amber-200/90 mt-0.5">
                {t(
                  'analytics.weakestLangDesc',
                  'Your performance in {{lang}} ({{ratio}}% of your median speed) is lagging behind your other languages. Dedicated drills will bring it up to par.',
                  {
                    lang: overview.weakestLanguage.language.toUpperCase(),
                    ratio: Math.round(overview.weakestLanguage.ratio * 100),
                  }
                )}
              </p>
            </div>
          </div>
          <Link
            to={`/practice/${overview.weakestLanguage.language}`}
            className="px-3.5 py-1.5 bg-amber-500 text-amber-950 font-bold rounded-lg text-xs hover:bg-amber-400 whitespace-nowrap"
          >
            {t('analytics.practiceWeakest', 'Practice')} {overview.weakestLanguage.language.toUpperCase()}
          </Link>
        </div>
      )}

      {/* KPI Cards Grid (AC-AN-1) */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div className="p-4 rounded-xl border border-border bg-surface">
          <div className="text-xs text-text-muted">{t('metrics.wpm', 'Average WPM')}</div>
          <div className="text-2xl font-bold font-mono text-accent mt-1">{overview.avgWpm}</div>
        </div>
        <div className="p-4 rounded-xl border border-border bg-surface">
          <div className="text-xs text-text-muted">{t('metrics.bestWpm', 'Best WPM')}</div>
          <div className="text-2xl font-bold font-mono text-text mt-1">{overview.bestWpm}</div>
        </div>
        <div className="p-4 rounded-xl border border-border bg-surface">
          <div className="text-xs text-text-muted">{t('metrics.accuracy', 'Accuracy')}</div>
          <div className="text-2xl font-bold font-mono text-text mt-1">{overview.avgAccuracy}%</div>
        </div>
        <div className="p-4 rounded-xl border border-border bg-surface">
          <div className="text-xs text-text-muted">{t('analytics.devScore', 'DevScore')}</div>
          <div className="text-2xl font-bold font-mono text-emerald-400 mt-1">
            {overview.devScore}
          </div>
        </div>
        <div className="p-4 rounded-xl border border-border bg-surface">
          <div className="text-xs text-text-muted">{t('dashboard.sessions', 'Sessions')}</div>
          <div className="text-xl font-bold font-mono text-text mt-1">{overview.totalSessions}</div>
        </div>
        <div className="p-4 rounded-xl border border-border bg-surface">
          <div className="text-xs text-text-muted">{t('analytics.totalTime', 'Time Trained')}</div>
          <div className="text-xl font-bold font-mono text-text mt-1">
            {overview.totalTimeMinutes}m
          </div>
        </div>
        <div className="p-4 rounded-xl border border-border bg-surface">
          <div className="text-xs text-text-muted">{t('metrics.chars', 'Chars Typed')}</div>
          <div className="text-xl font-bold font-mono text-text mt-1">
            {overview.totalChars.toLocaleString()}
          </div>
        </div>
        <div className="p-4 rounded-xl border border-border bg-surface">
          <div className="text-xs text-text-muted">{t('analytics.status', 'Data Status')}</div>
          <div className="text-sm font-semibold text-emerald-400 mt-2">
            ✓ {t('analytics.active', 'Sufficient Telemetry')}
          </div>
        </div>
      </div>

      {/* 365-day Activity Calendar Heatmap (AC-AN-7) */}
      <ActivityCalendar days={activityDays} />

      {/* WPM Trend Chart */}
      {overview.wpmTrend && overview.wpmTrend.length > 0 && (
        <div className="p-5 rounded-xl border border-border bg-surface">
          <h3 className="text-sm font-bold text-text mb-4">
            📈 {t('analytics.wpmTrend', 'WPM & Accuracy Progression')}
          </h3>
          <div className="h-44 w-full flex items-end gap-1 pt-6 pb-2 border-b border-border">
            {overview.wpmTrend.map((pt, idx) => {
              const heightPercent = Math.min(100, Math.max(10, (pt.wpm / 150) * 100));
              return (
                <div
                  key={idx}
                  className="flex-1 flex flex-col items-center gap-1 group relative h-full justify-end"
                >
                  <div
                    className="w-full bg-accent/80 hover:bg-accent rounded-t transition-all"
                    style={{ height: `${heightPercent}%` }}
                  />
                  <div className="absolute -top-7 hidden group-hover:flex flex-col items-center bg-surface-3 px-2 py-1 rounded text-[10px] font-mono whitespace-nowrap shadow-md z-10">
                    <span>{pt.wpm} WPM</span>
                    <span className="text-text-muted">{pt.accuracy}% acc</span>
                  </div>
                </div>
              );
            })}
          </div>
          <div className="flex justify-between text-[11px] font-mono text-text-muted mt-2">
            <span>{overview.wpmTrend[0]?.date}</span>
            <span>{overview.wpmTrend[overview.wpmTrend.length - 1]?.date}</span>
          </div>
        </div>
      )}

      {/* Character & Bigram Weakness Breakdown (AC-AN-5) */}
      {chars.length > 0 && (
        <div className="p-5 rounded-xl border border-border bg-surface space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="text-sm font-bold text-text">
              🎯 {t('analytics.weakCharsTitle', 'High-Penalty Error Characters (BL-AN-2)')}
            </h3>
            <span className="text-xs text-text-muted">
              {t('analytics.observationsThreshold', 'Min. 30 observations')}
            </span>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="border-b border-border bg-surface-2 text-xs text-text-muted uppercase font-mono">
                <tr>
                  <th className="p-2.5 pl-3">{t('metrics.chars', 'Char')}</th>
                  <th className="p-2.5">{t('analytics.observations', 'Observations')}</th>
                  <th className="p-2.5">{t('metrics.errors', 'Errors')}</th>
                  <th className="p-2.5">{t('analytics.errorRate', 'Error Rate')}</th>
                  <th className="p-2.5 pr-3">{t('analytics.zScore', 'Z-Score')}</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border/60 font-mono text-xs">
                {chars.map((stat, i) => (
                  <tr key={i} className="hover:bg-surface-2/60 transition-colors">
                    <td className="p-2.5 pl-3 font-bold text-accent">
                      <span className="px-2 py-0.5 rounded bg-surface-2 border border-border">
                        {stat.character === ' ' ? '␣ Space' : stat.character}
                      </span>
                    </td>
                    <td className="p-2.5 text-text-muted">{stat.totalCount}</td>
                    <td className="p-2.5 text-red-400 font-bold">{stat.errorCount}</td>
                    <td className="p-2.5 text-red-400">
                      {(stat.errorRate * 100).toFixed(1)}%
                    </td>
                    <td className="p-2.5 pr-3 text-amber-300 font-bold">
                      {stat.zScore.toFixed(2)}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
};
