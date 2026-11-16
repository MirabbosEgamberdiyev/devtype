import React, { useEffect, useRef, useState, useCallback } from 'react';
import { useTranslation } from 'react-i18next';
import { createEngine, Engine, EngineState } from '../../engine';

export interface TypingEditorProps {
  code: string;
  language: string;
  strictIndent?: boolean;
  onComplete: (keylogBase64: string, elapsedMs: number, wpm: number, accuracy: number) => void;
}

export const TypingEditor: React.FC<TypingEditorProps> = ({
  code,
  strictIndent = false,
  onComplete,
}) => {
  const { t } = useTranslation();
  const [engine, setEngine] = useState<Engine | null>(null);
  const [state, setState] = useState<EngineState | null>(null);
  const [liveWpm, setLiveWpm] = useState<number | null>(null);
  const containerRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLTextAreaElement>(null);

  // Initialize engine for current code
  useEffect(() => {
    const eng = createEngine({
      snippets: [code],
      mode: 'PRACTICE',
      errorMode: 'stop',
      strictIndent,
    });

    setEngine(eng);
    setState(eng.getState());
    setLiveWpm(null);

    const unsubscribe = eng.subscribe((newState, ev) => {
      setState({ ...newState });
      setLiveWpm(eng.liveWpm(ev.t));

      if (newState.phase === 'finished') {
        const keylog = eng.exportKeylog();
        const metrics = eng.metrics();
        onComplete(keylog, newState.activeMs, metrics.wpm, metrics.accuracy);
      }
    });

    return () => {
      unsubscribe();
    };
  }, [code, strictIndent, onComplete]);

  // Focus hidden input on click or mount
  const focusInput = useCallback(() => {
    inputRef.current?.focus();
  }, []);

  useEffect(() => {
    focusInput();
  }, [focusInput]);

  const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (!engine || !state || state.phase === 'finished') return;

    const now = Date.now();

    if (e.key === 'Backspace') {
      e.preventDefault();
      engine.dispatch({ k: 'backspace', t: now });
    } else if (e.key === 'Enter') {
      e.preventDefault();
      engine.dispatch({ k: 'enter', t: now });
    } else if (e.key === 'Tab') {
      e.preventDefault();
      engine.dispatch({ k: 'tab', t: now });
    } else if (e.key.length === 1) {
      e.preventDefault();
      engine.dispatch({ k: 'char', t: now, ch: e.key });
    }
  };

  if (!state) {
    return <div className="p-8 text-center text-text-muted">{t('typing.loading')}</div>;
  }

  const currentTargets = state.targets[state.seq] ?? [];
  const currentStates = state.states;
  const metrics = engine?.metrics();

  return (
    <div
      ref={containerRef}
      onClick={focusInput}
      className="relative flex flex-col gap-4 p-6 rounded-xl border border-border-default bg-bg-surface font-mono shadow-sm focus:outline-none"
    >
      {/* Live Metrics Header */}
      <div className="flex items-center justify-between pb-4 border-b border-border-muted text-sm select-none">
        <div className="flex items-center gap-6">
          <div>
            <span className="text-text-muted">{t('metrics.wpm')}: </span>
            <span className="font-bold text-accent-primary">{liveWpm ?? metrics?.wpm ?? 0}</span>
          </div>
          <div>
            <span className="text-text-muted">{t('metrics.accuracy')}: </span>
            <span className="font-bold text-accent-success">{metrics?.accuracy ?? 100}%</span>
          </div>
          <div>
            <span className="text-text-muted">{t('typing.elapsed')}: </span>
            <span className="font-bold text-text-primary">{(state.activeMs / 1000).toFixed(1)}s</span>
          </div>
        </div>
        <div className="text-xs text-text-subtle">
          {state.phase === 'idle' ? t('typing.pressAnyKey') : ''}
        </div>
      </div>

      {/* Code Display Area */}
      <div className="relative overflow-x-auto py-2 leading-relaxed text-base tracking-wide whitespace-pre select-none">
        {currentTargets.map((charTarget, idx) => {
          const isCursor = idx === state.cursor;
          const charState = currentStates[idx] ?? 'pending';
          let colorClass = 'text-text-muted'; // pending

          if (charState === 'correct') {
            colorClass = 'text-accent-success';
          } else if (charState === 'incorrect') {
            colorClass = 'text-accent-danger bg-accent-danger/20 rounded';
          }

          // Line break
          if (charTarget.ch === '\n') {
            return (
              <React.Fragment key={idx}>
                {isCursor && <span className="inline-block w-2 h-5 bg-accent-primary animate-pulse align-middle" />}
                <br />
              </React.Fragment>
            );
          }

          return (
            <span key={idx} className={`relative inline-block ${colorClass}`}>
              {isCursor && (
                <span className="absolute left-0 top-0 bottom-0 w-0.5 bg-accent-primary animate-pulse" />
              )}
              {charTarget.ch === ' ' ? ' ' : charTarget.ch}
            </span>
          );
        })}
      </div>

      {/* Hidden textarea to capture all keystrokes */}
      <textarea
        ref={inputRef}
        onKeyDown={handleKeyDown}
        aria-label="Typing input capture"
        className="absolute inset-0 opacity-0 pointer-events-none resize-none cursor-default"
        autoFocus
        tabIndex={0}
      />
    </div>
  );
};
