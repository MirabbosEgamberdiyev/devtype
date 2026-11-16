import React from 'react';
import { useTranslation } from 'react-i18next';
import { CompleteSessionResponse } from './api';

export interface ResultViewProps {
  result: CompleteSessionResponse;
  onRetry: () => void;
  onNext?: () => void;
  onBackToTopics?: () => void;
}

export const ResultView: React.FC<ResultViewProps> = ({
  result,
  onRetry,
  onNext,
  onBackToTopics,
}) => {
  const { t } = useTranslation();
  const { metrics, wpmSeries, weakTokens, rewards, progress, coachTip } = result;

  // SVG grafik koordinatalarini hisoblash
  const graphWidth = 600;
  const graphHeight = 150;
  const padding = 20;

  const maxWpm = Math.max(10, ...wpmSeries.map((p) => Math.max(p.wpm, p.raw)));
  const maxT = Math.max(1, ...wpmSeries.map((p) => p.t));

  const pointsWpm = wpmSeries
    .map((p) => {
      const x = padding + (p.t / maxT) * (graphWidth - 2 * padding);
      const y = graphHeight - padding - (p.wpm / maxWpm) * (graphHeight - 2 * padding);
      return `${x},${y}`;
    })
    .join(' ');

  const pointsRaw = wpmSeries
    .map((p) => {
      const x = padding + (p.t / maxT) * (graphWidth - 2 * padding);
      const y = graphHeight - padding - (p.raw / maxWpm) * (graphHeight - 2 * padding);
      return `${x},${y}`;
    })
    .join(' ');

  return (
    <div className="flex flex-col gap-8 max-w-4xl mx-auto w-full p-6 animate-fade-in" data-testid="result-view">
      {/* Title & Status */}
      <div className="flex items-center justify-between pb-4 border-b border-border-default">
        <div>
          <h1 className="text-2xl font-bold text-text-primary">{t('result.title')}</h1>
          <p className="text-sm text-text-muted">{t('result.subtitle')}</p>
        </div>
        {result.ranked && (
          <span className="px-3 py-1 rounded-full text-xs font-semibold bg-emerald-500/10 text-emerald-500 border border-emerald-500/30">
            {t('result.rankedSession')}
          </span>
        )}
      </div>

      {/* Main KPI Grid */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
        <div className="p-5 rounded-xl bg-bg-surface border border-border-default flex flex-col gap-1 shadow-sm">
          <span className="text-xs text-text-muted uppercase font-mono">{t('metrics.wpm')}</span>
          <span className="text-4xl font-extrabold text-accent-primary font-mono" data-testid="result-wpm">
            {metrics.wpm.toFixed(1)}
          </span>
          <span className="text-xs text-text-muted font-mono">
            {t('metrics.raw')}: {metrics.rawWpm.toFixed(1)}
          </span>
        </div>

        <div className="p-5 rounded-xl bg-bg-surface border border-border-default flex flex-col gap-1 shadow-sm">
          <span className="text-xs text-text-muted uppercase font-mono">{t('metrics.accuracy')}</span>
          <span className="text-4xl font-extrabold text-emerald-500 font-mono" data-testid="result-accuracy">
            {metrics.accuracy.toFixed(1)}%
          </span>
          <span className="text-xs text-text-muted font-mono">
            {t('metrics.errors')}: {metrics.errors}
          </span>
        </div>

        <div className="p-5 rounded-xl bg-bg-surface border border-border-default flex flex-col gap-1 shadow-sm">
          <span className="text-xs text-text-muted uppercase font-mono">{t('metrics.consistency')}</span>
          <span className="text-4xl font-extrabold text-sky-500 font-mono">
            {metrics.consistency.toFixed(1)}%
          </span>
          <span className="text-xs text-text-muted font-mono">
            CPM: {metrics.cpm.toFixed(0)}
          </span>
        </div>

        <div className="p-5 rounded-xl bg-bg-surface border border-border-default flex flex-col gap-1 shadow-sm">
          <span className="text-xs text-text-muted uppercase font-mono">{t('metrics.time')}</span>
          <span className="text-4xl font-extrabold text-text-primary font-mono">
            {(metrics.elapsedMs / 1000).toFixed(1)}s
          </span>
          <span className="text-xs text-text-muted font-mono">
            {t('metrics.chars')}: {metrics.correctChars}
          </span>
        </div>
      </div>

      {/* WPM Chart */}
      {wpmSeries.length > 1 && (
        <div className="p-5 rounded-xl bg-bg-surface border border-border-default shadow-sm flex flex-col gap-3">
          <div className="flex items-center justify-between text-xs text-text-muted">
            <span className="font-semibold uppercase tracking-wider">{t('result.wpmOverTime')}</span>
            <div className="flex items-center gap-4">
              <span className="flex items-center gap-1.5">
                <span className="w-2.5 h-0.5 bg-accent-primary inline-block"></span>
                <span>{t('metrics.wpm')}</span>
              </span>
              <span className="flex items-center gap-1.5">
                <span className="w-2.5 h-0.5 bg-text-muted/60 inline-block border-t border-dashed"></span>
                <span>{t('metrics.raw')}</span>
              </span>
            </div>
          </div>

          <div className="w-full overflow-hidden">
            <svg viewBox={`0 0 ${graphWidth} ${graphHeight}`} className="w-full h-36">
              {/* Grid lines */}
              <line
                x1={padding}
                y1={graphHeight - padding}
                x2={graphWidth - padding}
                y2={graphHeight - padding}
                stroke="currentColor"
                className="text-border-muted"
                strokeWidth="1"
              />
              <line
                x1={padding}
                y1={padding}
                x2={graphWidth - padding}
                y2={padding}
                stroke="currentColor"
                className="text-border-muted"
                strokeWidth="1"
                strokeDasharray="4"
              />

              {/* Raw WPM line */}
              {pointsRaw && (
                <polyline
                  fill="none"
                  stroke="currentColor"
                  className="text-text-muted opacity-40"
                  strokeWidth="2"
                  strokeDasharray="4"
                  points={pointsRaw}
                />
              )}

              {/* Net WPM line */}
              {pointsWpm && (
                <polyline
                  fill="none"
                  stroke="currentColor"
                  className="text-accent-primary"
                  strokeWidth="3"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  points={pointsWpm}
                />
              )}
            </svg>
          </div>
        </div>
      )}

      {/* Weak Tokens & Rewards Section */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Weak Tokens */}
        <div className="p-5 rounded-xl bg-bg-surface border border-border-default shadow-sm flex flex-col gap-3">
          <h2 className="text-sm font-semibold text-text-primary uppercase tracking-wider">
            {t('result.troubleKeys')}
          </h2>

          {weakTokens && weakTokens.length > 0 ? (
            <div className="flex flex-col divide-y divide-border-subtle">
              {weakTokens.map((wt, idx) => (
                <div key={idx} className="py-2 flex items-center justify-between text-xs font-mono">
                  <div className="flex items-center gap-2">
                    <span className="px-2 py-0.5 rounded bg-bg-subtle border border-border-default text-accent-primary font-bold">
                      {wt.token === ' ' ? 'Space' : wt.token}
                    </span>
                    <span className="text-text-muted">
                      {wt.errors} {t('result.mistakes')}
                    </span>
                  </div>
                  <span className="text-text-muted">
                    {wt.avgMs > 0 ? `${wt.avgMs} ms avg` : ''}
                  </span>
                </div>
              ))}
            </div>
          ) : (
            <p className="text-xs text-text-muted py-4 text-center">
              {t('result.noTroubleKeys')}
            </p>
          )}
        </div>

        {/* Progress & Rewards */}
        <div className="p-5 rounded-xl bg-bg-surface border border-border-default shadow-sm flex flex-col gap-3">
          <h2 className="text-sm font-semibold text-text-primary uppercase tracking-wider">
            {t('result.progressAndRewards')}
          </h2>

          <div className="flex flex-col gap-3 text-xs">
            {rewards && (
              <div className="p-3 rounded-lg bg-bg-subtle flex items-center justify-between">
                <div>
                  <div className="font-bold text-accent-primary text-sm">+{rewards.xp} XP</div>
                  <div className="text-text-muted">
                    {t('gamification.level')} {rewards.level} · Total: {rewards.totalXp} XP
                  </div>
                </div>
                {rewards.streak && (
                  <div className="text-right">
                    <div className="font-bold text-amber-500">🔥 {rewards.streak.current} {t('gamification.days')}</div>
                    <div className="text-[10px] text-text-muted">{t('gamification.streak')}</div>
                  </div>
                )}
              </div>
            )}

            {progress && (
              <div className="p-3 rounded-lg bg-bg-subtle flex flex-col gap-1.5">
                <div className="flex items-center justify-between font-semibold">
                  <span>{progress.topic} ({progress.difficulty})</span>
                  <span className="text-accent-primary">{progress.state}</span>
                </div>
                <div className="w-full bg-border-muted h-1.5 rounded-full overflow-hidden">
                  <div
                    style={{ width: `${Math.min(100, (progress.completed / Math.max(1, progress.required)) * 100)}%` }}
                    className="bg-accent-primary h-full rounded-full transition-all duration-300"
                  />
                </div>
                <div className="text-[10px] text-text-muted text-right">
                  {progress.completed} / {progress.required} {t('result.exercisesCompleted')}
                </div>
              </div>
            )}

            {coachTip && (
              <div className="p-3 rounded-lg bg-accent-subtle/30 border border-accent-primary/20 text-text-primary">
                <span className="font-bold">💡 Coach: </span>
                <span>{coachTip.text}</span>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Action Buttons */}
      <div className="flex items-center justify-end gap-3 pt-4 border-t border-border-default">
        {onBackToTopics && (
          <button
            onClick={onBackToTopics}
            className="px-5 py-2.5 rounded-xl border border-border-default hover:bg-bg-subtle text-text-primary text-sm font-medium transition-colors"
          >
            {t('result.backToTopics')}
          </button>
        )}
        <button
          onClick={onRetry}
          className="px-5 py-2.5 rounded-xl border border-border-default hover:bg-bg-subtle text-text-primary text-sm font-medium transition-colors"
        >
          {t('result.retry')}
        </button>
        {onNext && (
          <button
            onClick={onNext}
            className="px-6 py-2.5 rounded-xl bg-accent-primary hover:bg-accent-hover text-white text-sm font-medium shadow-sm transition-colors"
          >
            {t('result.nextExercise')}
          </button>
        )}
      </div>
    </div>
  );
};
