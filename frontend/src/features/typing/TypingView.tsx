import React, { useState, useEffect, useCallback } from 'react';
import { useTranslation } from 'react-i18next';
import { TypingEditor } from './TypingEditor';

interface SessionExercise {
  seq: number;
  exerciseKey: string;
  title: string;
  note: string;
  difficulty: string;
  code: string;
  charCount: number;
}

interface StartSessionResponse {
  sessionId: string;
  startedAt: string;
  expiresAt: string;
  mode: string;
  errorMode: string;
  durationLimitS: number | null;
  language: {
    slug: string;
    grammarId: string;
    indent: { style: string; size: number };
    tokenPatterns: string[];
  };
  exercises: SessionExercise[];
  completeToken: string;
}

interface ServerMetrics {
  wpm: number;
  rawWpm: number;
  cpm: number;
  accuracy: number;
  consistency: number;
  errors: number;
  uncorrectedErrors: number;
  backspaces: number;
  correctChars: number;
  elapsedMs: number;
  completionPct: number;
  specialAccuracy: number;
}

interface CompleteSessionResponse {
  sessionId: string;
  status: string;
  ranked: boolean;
  rankedReason: string;
  metrics: ServerMetrics;
}

export const TypingView: React.FC = () => {
  const { t } = useTranslation();
  const [session, setSession] = useState<StartSessionResponse | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [submitting, setSubmitting] = useState<boolean>(false);
  const [result, setResult] = useState<CompleteSessionResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  const startNewSession = useCallback(async () => {
    setLoading(true);
    setResult(null);
    setError(null);

    try {
      const res = await fetch('/api/v1/sessions', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          language: 'java',
          mode: 'PRACTICE',
          difficulty: 'BEGINNER',
        }),
      });

      if (!res.ok) {
        throw new Error(`Session start failed with status ${res.status}`);
      }

      const data: StartSessionResponse = await res.json();
      setSession(data);
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Failed to start session');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    startNewSession();
  }, [startNewSession]);

  const handleComplete = async (
    keylog: string,
    elapsedMs: number,
    wpm: number,
    accuracy: number
  ) => {
    if (!session) return;
    setSubmitting(true);
    setError(null);

    try {
      const res = await fetch(`/api/v1/sessions/${session.sessionId}/complete`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          completeToken: session.completeToken,
          keylog,
          clientElapsedMs: elapsedMs,
          clientMetrics: { wpm, accuracy },
        }),
      });

      if (!res.ok) {
        throw new Error(`Session completion failed with status ${res.status}`);
      }

      const data: CompleteSessionResponse = await res.json();
      setResult(data);
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Failed to complete session');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center py-20 text-text-muted">
        {t('typing.loading')}
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex flex-col items-center justify-center py-20 gap-4">
        <div className="text-accent-danger font-semibold">{error}</div>
        <button
          onClick={startNewSession}
          className="px-4 py-2 rounded-lg bg-accent-primary text-white hover:bg-accent-primary/90 transition-colors"
        >
          {t('actions.retry')}
        </button>
      </div>
    );
  }

  if (result) {
    return (
      <div className="flex flex-col gap-6 max-w-2xl mx-auto p-8 rounded-2xl bg-bg-surface border border-border-default shadow-md">
        <div className="text-center">
          <h2 className="text-2xl font-bold text-text-primary mb-1">
            {t('typing.completed')}
          </h2>
          <p className="text-sm text-text-muted">{t('typing.serverResult')}</p>
        </div>

        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 py-4 border-y border-border-muted text-center">
          <div className="p-4 rounded-xl bg-bg-canvas/50">
            <span className="text-xs text-text-muted uppercase tracking-wider block mb-1">
              {t('metrics.wpm')}
            </span>
            <span className="text-3xl font-black text-accent-primary">
              {result.metrics.wpm.toFixed(1)}
            </span>
          </div>

          <div className="p-4 rounded-xl bg-bg-canvas/50">
            <span className="text-xs text-text-muted uppercase tracking-wider block mb-1">
              {t('metrics.accuracy')}
            </span>
            <span className="text-3xl font-black text-accent-success">
              {result.metrics.accuracy.toFixed(1)}%
            </span>
          </div>

          <div className="p-4 rounded-xl bg-bg-canvas/50">
            <span className="text-xs text-text-muted uppercase tracking-wider block mb-1">
              {t('metrics.cpm')}
            </span>
            <span className="text-3xl font-black text-text-primary">
              {result.metrics.cpm.toFixed(0)}
            </span>
          </div>

          <div className="p-4 rounded-xl bg-bg-canvas/50">
            <span className="text-xs text-text-muted uppercase tracking-wider block mb-1">
              {t('typing.elapsed')}
            </span>
            <span className="text-3xl font-black text-text-primary">
              {(result.metrics.elapsedMs / 1000).toFixed(1)}s
            </span>
          </div>
        </div>

        <div className="flex justify-between items-center text-sm text-text-muted">
          <span>{t('typing.status')}: <strong className="text-accent-success">{result.status}</strong></span>
          <span>{result.ranked ? t('typing.ranked') : t('typing.notRanked')}</span>
        </div>

        <div className="flex justify-center mt-2">
          <button
            onClick={startNewSession}
            className="px-6 py-2.5 rounded-xl font-medium bg-accent-primary text-white hover:bg-accent-primary/90 transition-colors shadow-sm"
          >
            {t('actions.restart')}
          </button>
        </div>
      </div>
    );
  }

  const currentExercise = session?.exercises[0];

  return (
    <div className="flex flex-col gap-6 max-w-4xl mx-auto">
      {/* Exercise metadata banner */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-bold text-text-primary">
            {currentExercise?.title}
          </h1>
          <p className="text-sm text-text-muted">{currentExercise?.note}</p>
        </div>
        <div className="flex items-center gap-3">
          <span className="px-2.5 py-1 rounded text-xs font-mono font-semibold uppercase bg-accent-primary/10 text-accent-primary">
            {session?.language.slug}
          </span>
          <span className="px-2.5 py-1 rounded text-xs font-semibold bg-bg-surface border border-border-default text-text-muted">
            {currentExercise?.difficulty}
          </span>
        </div>
      </div>

      {submitting ? (
        <div className="p-12 text-center text-text-muted bg-bg-surface rounded-xl border border-border-default">
          {t('typing.submitting')}
        </div>
      ) : currentExercise ? (
        <TypingEditor
          code={currentExercise.code}
          language={session?.language.slug ?? 'java'}
          onComplete={handleComplete}
        />
      ) : null}
    </div>
  );
};
