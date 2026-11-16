import React, { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { devtypeApi, SessionHistoryItem, ReplayResponse } from '../../shared/api/devtypeApi';
import { ReplayViewer } from './ReplayViewer';

export const HistoryPage: React.FC = () => {
  const { t } = useTranslation('common');

  const [sessions, setSessions] = useState<SessionHistoryItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Filters
  const [selectedLanguage, setSelectedLanguage] = useState<string>('');
  const [selectedMode, setSelectedMode] = useState<string>('');
  const [selectedStatus, setSelectedStatus] = useState<string>('');

  // Replay modal state
  const [activeReplay, setActiveReplay] = useState<ReplayResponse | null>(null);
  const [loadingReplayId, setLoadingReplayId] = useState<string | null>(null);

  useEffect(() => {
    let mounted = true;
    setLoading(true);
    devtypeApi
      .getSessions({
        language: selectedLanguage || undefined,
        mode: selectedMode || undefined,
        status: selectedStatus || undefined,
        limit: 50,
      })
      .then((res) => {
        if (mounted) {
          setSessions(res.sessions || []);
          setLoading(false);
        }
      })
      .catch((err: unknown) => {
        if (mounted) {
          const e = err as Error;
          setError(e.message || 'Failed to load session history');
          setLoading(false);
        }
      });

    return () => {
      mounted = false;
    };
  }, [selectedLanguage, selectedMode, selectedStatus]);

  const handleLaunchReplay = async (sessionId: string) => {
    try {
      setLoadingReplayId(sessionId);
      const replayData = await devtypeApi.getSessionReplay(sessionId);
      setActiveReplay(replayData);
    } catch (err: unknown) {
      const e = err as Error;
      alert(e.message || 'Could not load replay');
    } finally {
      setLoadingReplayId(null);
    }
  };

  return (
    <div className="space-y-6 max-w-6xl mx-auto">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-text">{t('history.title', 'Session History')}</h2>
          <p className="text-sm text-text-muted mt-1">
            {t('history.subtitle', 'Review your completed practice runs, accuracy, and playback keystrokes.')}
          </p>
        </div>
      </div>

      {/* Filter Bar */}
      <div className="p-4 rounded-xl border border-border bg-surface flex flex-wrap items-center gap-4">
        <div>
          <label className="block text-xs font-semibold text-text-muted mb-1">
            {t('metrics.language', 'Language')}
          </label>
          <select
            value={selectedLanguage}
            onChange={(e) => setSelectedLanguage(e.target.value)}
            className="bg-surface-2 border border-border text-sm rounded-lg px-3 py-1.5 text-text outline-none focus:ring-2 focus:ring-accent"
          >
            <option value="">{t('history.allLanguages', 'All Languages')}</option>
            <option value="sql">SQL</option>
            <option value="plsql">PL/SQL</option>
            <option value="java">Java</option>
            <option value="python">Python</option>
            <option value="javascript">JavaScript</option>
          </select>
        </div>

        <div>
          <label className="block text-xs font-semibold text-text-muted mb-1">
            {t('metrics.mode', 'Mode')}
          </label>
          <select
            value={selectedMode}
            onChange={(e) => setSelectedMode(e.target.value)}
            className="bg-surface-2 border border-border text-sm rounded-lg px-3 py-1.5 text-text outline-none focus:ring-2 focus:ring-accent"
          >
            <option value="">{t('history.allModes', 'All Modes')}</option>
            <option value="PRACTICE">Practice</option>
            <option value="SPEED_TEST">Speed Test</option>
            <option value="ACCURACY">Accuracy</option>
            <option value="SPECIAL_CHARS">Special Characters</option>
          </select>
        </div>

        <div>
          <label className="block text-xs font-semibold text-text-muted mb-1">
            {t('typing.status', 'Status')}
          </label>
          <select
            value={selectedStatus}
            onChange={(e) => setSelectedStatus(e.target.value)}
            className="bg-surface-2 border border-border text-sm rounded-lg px-3 py-1.5 text-text outline-none focus:ring-2 focus:ring-accent"
          >
            <option value="">{t('history.allStatuses', 'All Statuses')}</option>
            <option value="COMPLETED">Completed</option>
            <option value="ABANDONED">Abandoned</option>
            <option value="TIMED_OUT">Timed Out</option>
            <option value="FLAGGED">Flagged</option>
          </select>
        </div>
      </div>

      {/* Replay Modal Overlay */}
      {activeReplay && (
        <div className="fixed inset-0 bg-black/70 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="w-full max-w-4xl max-h-[90vh] overflow-y-auto">
            <ReplayViewer
              code={activeReplay.code}
              keylogBase64={activeReplay.keylog}
              language={activeReplay.language}
              finalMetrics={{
                wpm: activeReplay.metrics.wpm,
                accuracy: activeReplay.metrics.accuracy,
                durationMs: activeReplay.metrics.durationMs,
                errorCount: activeReplay.metrics.errorCount,
              }}
              onClose={() => setActiveReplay(null)}
            />
          </div>
        </div>
      )}

      {/* History Table */}
      {loading ? (
        <div className="flex flex-col items-center justify-center min-h-[300px] text-text-muted">
          <div className="w-8 h-8 border-2 border-accent border-t-transparent rounded-full animate-spin mb-3" />
          <p>{t('typing.loading', 'Loading sessions...')}</p>
        </div>
      ) : error ? (
        <div className="p-6 rounded-xl border border-red-500/30 bg-red-500/10 text-center text-red-400">
          {error}
        </div>
      ) : sessions.length === 0 ? (
        <div className="p-12 rounded-2xl border border-dashed border-border bg-surface/50 text-center text-text-muted">
          <span className="text-3xl block mb-2">📜</span>
          <p className="font-medium text-text">{t('history.emptyTitle', 'No training sessions found')}</p>
          <p className="text-xs text-text-muted mt-1">
            {t('history.emptyDesc', 'Sessions you complete will appear here with full replay and metrics.')}
          </p>
        </div>
      ) : (
        <div className="rounded-xl border border-border bg-surface overflow-x-auto shadow-sm">
          <table className="w-full text-left text-sm">
            <thead className="border-b border-border bg-surface-2 text-xs text-text-muted uppercase font-mono">
              <tr>
                <th className="p-3 pl-4">{t('metrics.language', 'Language')}</th>
                <th className="p-3">{t('metrics.mode', 'Mode')}</th>
                <th className="p-3">{t('history.topicExercise', 'Topic / Exercise')}</th>
                <th className="p-3">{t('metrics.wpm', 'WPM')}</th>
                <th className="p-3">{t('metrics.accuracy', 'Accuracy')}</th>
                <th className="p-3">{t('metrics.time', 'Duration')}</th>
                <th className="p-3">{t('metrics.xp', 'XP')}</th>
                <th className="p-3">{t('typing.status', 'Status')}</th>
                <th className="p-3 pr-4 text-right">{t('history.action', 'Action')}</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border/60">
              {sessions.map((session) => (
                <tr key={session.sessionId} className="hover:bg-surface-2/60 transition-colors">
                  <td className="p-3 pl-4 font-mono font-medium text-accent">
                    {session.language.toUpperCase()}
                  </td>
                  <td className="p-3 text-xs">
                    <span className="px-2 py-0.5 rounded bg-surface-2 border border-border/60">
                      {session.mode}
                    </span>
                  </td>
                  <td className="p-3 text-xs text-text-muted truncate max-w-[180px]">
                    {session.exerciseTitle || session.topicTitle || '—'}
                  </td>
                  <td className="p-3 font-mono font-bold">{session.wpm}</td>
                  <td className="p-3 font-mono text-text-muted">{session.accuracy}%</td>
                  <td className="p-3 font-mono text-xs text-text-muted">
                    {session.durationSeconds}s
                  </td>
                  <td className="p-3 font-mono text-emerald-400">+{session.earnedXp}</td>
                  <td className="p-3 text-xs">
                    <span
                      className={`px-2 py-0.5 rounded text-[11px] font-semibold ${
                        session.status === 'COMPLETED'
                          ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                          : 'bg-surface-3 text-text-muted border border-border'
                      }`}
                    >
                      {session.status}
                    </span>
                  </td>
                  <td className="p-3 pr-4 text-right">
                    {session.status === 'COMPLETED' ? (
                      <button
                        onClick={() => handleLaunchReplay(session.sessionId)}
                        disabled={loadingReplayId === session.sessionId}
                        className="px-3 py-1 bg-surface-2 hover:bg-surface-3 border border-border rounded text-xs font-semibold text-text hover:text-accent transition-colors disabled:opacity-50"
                      >
                        {loadingReplayId === session.sessionId
                          ? '...'
                          : `▶ ${t('history.replay', 'Replay')}`}
                      </button>
                    ) : (
                      <span className="text-xs text-text-muted">—</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};
