import React, { useState, useEffect, useCallback } from 'react';
import { useTranslation } from 'react-i18next';
import {
  startSession,
  completeSession,
  StartSessionResponse,
  CompleteSessionResponse,
  StartSessionRequest,
} from './api';
import { enqueueOfflineSession } from '../offline/offlineQueue';
import { TypingEditor } from './TypingEditor';
import { ResultView } from './ResultView';

export interface TypingViewProps {
  language?: string;
  mode?: 'PRACTICE' | 'SPEED_TEST' | 'ACCURACY' | 'SPECIAL_CHARS';
  topic?: string;
  difficulty?: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED' | 'EXPERT';
  specialCategory?: string;
  duration?: 30 | 60 | 120;
  onBackToTopics?: () => void;
}

export const TypingView: React.FC<TypingViewProps> = ({
  language = 'java',
  mode = 'PRACTICE',
  topic,
  difficulty = 'BEGINNER',
  specialCategory,
  duration,
  onBackToTopics,
}) => {
  const { t } = useTranslation();
  const [session, setSession] = useState<StartSessionResponse | null>(null);
  const [currentExerciseIndex, setCurrentExerciseIndex] = useState<number>(0);
  const [loading, setLoading] = useState<boolean>(true);
  const [submitting, setSubmitting] = useState<boolean>(false);
  const [result, setResult] = useState<CompleteSessionResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  const startNewSession = useCallback(async () => {
    setLoading(true);
    setResult(null);
    setError(null);
    setCurrentExerciseIndex(0);

    try {
      const req: StartSessionRequest = {
        language,
        mode,
        topic,
        difficulty,
        specialCategory,
        duration,
      };

      const data = await startSession(req);
      setSession(data);
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Failed to start session');
    } finally {
      setLoading(false);
    }
  }, [language, mode, topic, difficulty, specialCategory, duration]);

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
      const res = await completeSession(session.sessionId, {
        completeToken: session.completeToken,
        keylog,
        clientElapsedMs: elapsedMs,
        clientMetrics: { wpm, accuracy },
      });
      setResult(res);
    } catch (err: unknown) {
      if (typeof navigator !== 'undefined' && !navigator.onLine) {
        await enqueueOfflineSession(session.sessionId, {
          completeToken: session.completeToken,
          keylog,
          clientElapsedMs: elapsedMs,
          clientMetrics: { wpm, accuracy },
        });
        setError(t('typing.offlineSaved'));
      } else {
        setError(err instanceof Error ? err.message : 'Failed to submit session result');
      }
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center p-16 gap-3 text-text-muted">
        <div className="w-8 h-8 rounded-full border-2 border-accent-primary border-t-transparent animate-spin" />
        <span className="text-sm font-mono">{t('typing.loading')}</span>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex flex-col items-center justify-center p-12 gap-4 max-w-md mx-auto text-center">
        <div className="p-3 rounded-full bg-red-500/10 text-red-500 text-2xl">⚠️</div>
        <div className="text-base font-semibold text-text-primary">{t('typing.sessionError')}</div>
        <p className="text-xs text-text-muted font-mono">{error}</p>
        <button
          onClick={startNewSession}
          className="px-5 py-2 rounded-xl bg-accent-primary hover:bg-accent-hover text-white text-xs font-semibold transition-colors"
        >
          {t('common.retry')}
        </button>
      </div>
    );
  }

  if (result) {
    return (
      <ResultView
        result={result}
        onRetry={startNewSession}
        onNext={result.next ? startNewSession : undefined}
        onBackToTopics={onBackToTopics}
      />
    );
  }

  if (!session || !session.exercises || session.exercises.length === 0) {
    return (
      <div className="text-center p-12 text-text-muted text-sm font-mono">
        {t('typing.noExercises')}
      </div>
    );
  }

  const currentExercise = session.exercises[currentExerciseIndex] ?? session.exercises[0];
  if (!currentExercise) {
    return (
      <div className="text-center p-12 text-text-muted text-sm font-mono">
        {t('typing.noExercises')}
      </div>
    );
  }

  return (
    <div className="relative w-full py-6 flex flex-col gap-4">
      {submitting && (
        <div className="absolute inset-0 z-30 bg-bg-surface/70 backdrop-blur-sm flex flex-col items-center justify-center gap-2">
          <div className="w-6 h-6 rounded-full border-2 border-accent-primary border-t-transparent animate-spin" />
          <span className="text-xs font-mono text-text-muted">{t('typing.analyzing')}</span>
        </div>
      )}

      {currentExercise.title && (
        <div className="max-w-4xl mx-auto w-full px-2 flex items-center justify-between">
          <div>
            <h2 className="text-lg font-bold text-text-primary">{currentExercise.title}</h2>
            {currentExercise.note && (
              <p className="text-xs text-text-muted mt-0.5">{currentExercise.note}</p>
            )}
          </div>
          <span className="px-2.5 py-1 rounded bg-bg-subtle text-xs text-text-muted font-mono uppercase">
            {currentExercise.difficulty}
          </span>
        </div>
      )}

      <TypingEditor
        code={currentExercise.code}
        language={session.language.slug}
        mode={session.mode as 'PRACTICE' | 'SPEED_TEST' | 'ACCURACY' | 'SPECIAL_CHARS'}
        durationLimitS={session.durationLimitS}
        strictIndent={false}
        stopOnError={true}
        showKeyboard={true}
        onComplete={handleComplete}
      />
    </div>
  );
};
