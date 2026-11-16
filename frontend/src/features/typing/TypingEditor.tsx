import React, { useEffect, useRef, useState, useCallback } from 'react';
import { useTranslation } from 'react-i18next';
import { createEngine, Engine, EngineState, TargetChar } from '../../engine';
import { Mode } from '../../engine/constants';
import { VirtualKeyboard } from '../keyboard/VirtualKeyboard';

export interface TypingEditorProps {
  code: string;
  language: string;
  mode?: 'PRACTICE' | 'SPEED_TEST' | 'ACCURACY' | 'SPECIAL_CHARS';
  strictIndent?: boolean;
  stopOnError?: boolean;
  durationLimitS?: number | null;
  showKeyboard?: boolean;
  onComplete: (keylogBase64: string, elapsedMs: number, wpm: number, accuracy: number) => void;
}

export const TypingEditor: React.FC<TypingEditorProps> = ({
  code,
  language,
  mode = 'PRACTICE',
  strictIndent = false,
  stopOnError = true,
  durationLimitS = null,
  showKeyboard = true,
  onComplete,
}) => {
  const { t } = useTranslation();
  const [engine, setEngine] = useState<Engine | null>(null);
  const [state, setState] = useState<EngineState | null>(null);
  const [liveWpm, setLiveWpm] = useState<number | null>(null);
  const [lastPressedCode, setLastPressedCode] = useState<string | null>(null);
  const [capsLockActive, setCapsLockActive] = useState<boolean>(false);
  const [showPasteWarning, setShowPasteWarning] = useState<boolean>(false);
  const [isPaused, setIsPaused] = useState<boolean>(false);

  const containerRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLTextAreaElement>(null);
  const lastActiveRef = useRef<number>(Date.now());
  const sessionStartRef = useRef<number>(Date.now());
  const pressedTimeoutRef = useRef<number | null>(null);

  // Dvigatelni ishga tushirish
  useEffect(() => {
    sessionStartRef.current = Date.now();
    const eng = createEngine({
      snippets: [code],
      mode: mode as Mode,
      errorMode: stopOnError ? 'stop' : 'free',
      strictIndent,
      limitSeconds: durationLimitS ?? undefined,
    });

    setEngine(eng);
    setState(eng.getState());
    setLiveWpm(null);
    setIsPaused(false);

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
      if (pressedTimeoutRef.current) {
        clearTimeout(pressedTimeoutRef.current);
      }
    };
  }, [code, mode, strictIndent, stopOnError, durationLimitS, onComplete]);

  // Hidden textarea fokusini saqlash
  const focusInput = useCallback(() => {
    inputRef.current?.focus();
  }, []);

  useEffect(() => {
    focusInput();
  }, [focusInput]);

  // 15 soniya harakatsizlikda avtomatik pauza (AC-TM-1, docs/07 §7)
  useEffect(() => {
    const interval = window.setInterval(() => {
      if (state && state.phase === 'running' && !isPaused) {
        const now = Date.now();
        if (now - lastActiveRef.current >= 15000) {
          setIsPaused(true);
        }
      }
    }, 1000);

    return () => window.clearInterval(interval);
  }, [state, isPaused]);

  const resumeFromPause = useCallback(() => {
    lastActiveRef.current = Date.now();
    setIsPaused(false);
    focusInput();
  }, [focusInput]);

  const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (!engine || !state || state.phase === 'finished') return;

    setCapsLockActive(typeof e.getModifierState === 'function' ? e.getModifierState('CapsLock') : false);
    setLastPressedCode(e.code);

    // 120ms dan keyin pressed effekti o'chadi
    if (pressedTimeoutRef.current) {
      clearTimeout(pressedTimeoutRef.current);
    }
    pressedTimeoutRef.current = window.setTimeout(() => setLastPressedCode(null), 120);

    if (isPaused) {
      resumeFromPause();
    }

    lastActiveRef.current = Date.now();
    const now = Math.max(0, Date.now() - sessionStartRef.current);

    if (e.key === 'Backspace') {
      e.preventDefault();
      engine.dispatch({ k: 'backspace', t: now });
    } else if (e.key === 'Enter') {
      e.preventDefault();
      engine.dispatch({ k: 'enter', t: now });
    } else if (e.key === 'Tab') {
      e.preventDefault();
      engine.dispatch({ k: 'tab', t: now });
    } else if (e.key.length === 1 && !e.ctrlKey && !e.metaKey && !e.altKey) {
      e.preventDefault();
      engine.dispatch({ k: 'char', t: now, ch: e.key });
    }
  };

  const handlePaste = (e: React.ClipboardEvent<HTMLTextAreaElement>) => {
    e.preventDefault(); // Paste taqiqlanadi (docs/09 §1)
    setShowPasteWarning(true);
    window.setTimeout(() => setShowPasteWarning(false), 3000);
  };

  const handleDrop = (e: React.DragEvent<HTMLTextAreaElement>) => {
    e.preventDefault(); // Drop taqiqlanadi
  };

  if (!state) {
    return <div className="p-8 text-center text-text-muted">{t('typing.loading')}</div>;
  }

  const currentTargets: TargetChar[] = state.targets[state.seq] ?? [];
  const currentStates = state.states;
  const metrics = engine?.metrics();

  // Qatorlar bo'yicha guruhlash (docs/09 §2)
  const lines: { lineIndex: number; chars: { target: TargetChar; state: string; charIndex: number }[] }[] = [];
  let currentLineIndex = 0;
  let lineChars: { target: TargetChar; state: string; charIndex: number }[] = [];

  for (let i = 0; i < currentTargets.length; i++) {
    const tChar = currentTargets[i];
    if (!tChar) continue;
    const s = currentStates[i] ?? 'pending';
    lineChars.push({ target: tChar, state: s, charIndex: i });

    if (tChar.ch === '\n' || i === currentTargets.length - 1) {
      lines.push({ lineIndex: currentLineIndex, chars: lineChars });
      currentLineIndex++;
      lineChars = [];
    }
  }

  // Joriy qator va kursor ustuni
  let activeLineIdx = 0;
  let nextExpectedChar: string | null = null;

  for (let l = 0; l < lines.length; l++) {
    const row = lines[l];
    if (!row) continue;
    const hasCursor = row.chars.some((c) => c.charIndex === state.cursor);
    if (hasCursor) {
      activeLineIdx = l;
      const found = row.chars.find((c) => c.charIndex === state.cursor);
      if (found) {
        nextExpectedChar = found.target.ch;
      }
      break;
    }
  }

  // 9 qatorli oyna: joriy qator 3-o'rinda turadi (docs/09 §6)
  const LINE_HEIGHT_PX = 28;
  const maxTop = Math.max(0, lines.length - 9);
  const targetTop = Math.min(Math.max(0, activeLineIdx - 2), maxTop);
  const scrollOffsetY = targetTop * LINE_HEIGHT_PX;

  return (
    <div className="flex flex-col gap-6 max-w-4xl mx-auto w-full select-none">
      {/* Metrics Bar */}
      <div className="flex items-center justify-between px-6 py-3 rounded-xl bg-bg-surface border border-border-default shadow-sm font-mono text-sm">
        <div className="flex items-center gap-6">
          <div>
            <span className="text-text-muted">{t('metrics.wpm')}: </span>
            <span className="font-bold text-accent-primary text-base">
              {liveWpm !== null ? liveWpm.toFixed(1) : (metrics?.wpm.toFixed(1) ?? '0.0')}
            </span>
          </div>
          <div>
            <span className="text-text-muted">{t('metrics.accuracy')}: </span>
            <span className="font-bold text-text-primary">
              {metrics ? `${metrics.accuracy.toFixed(1)}%` : '100%'}
            </span>
          </div>
          {durationLimitS && (
            <div>
              <span className="text-text-muted">{t('metrics.time')}: </span>
              <span className="font-bold text-amber-500">
                {Math.max(0, durationLimitS - Math.floor(state.activeMs / 1000))}s
              </span>
            </div>
          )}
        </div>

        <div className="flex items-center gap-3">
          {capsLockActive && (
            <span className="px-2 py-0.5 rounded text-xs bg-amber-500/10 text-amber-500 border border-amber-500/20 font-semibold animate-pulse">
              CAPS LOCK
            </span>
          )}
          <span className="px-2.5 py-1 rounded bg-bg-subtle text-xs text-text-muted font-mono uppercase">
            {language} · {mode}
          </span>
        </div>
      </div>

      {/* Editor Surface */}
      <div
        ref={containerRef}
        onClick={focusInput}
        className="relative p-6 rounded-xl border border-border-default bg-bg-surface font-mono shadow-sm overflow-hidden min-h-[300px] cursor-text"
        data-testid="typing-editor"
      >
        {/* Hidden Input for Keyboard capture */}
        <textarea
          ref={inputRef}
          onKeyDown={handleKeyDown}
          onPaste={handlePaste}
          onDrop={handleDrop}
          autoFocus
          rows={1}
          className="absolute -left-[9999px] top-0 opacity-0 pointer-events-none"
          aria-label="Code Typing Input"
        />

        {/* Paste Warning Toast */}
        {showPasteWarning && (
          <div className="absolute top-4 right-4 z-20 px-3 py-1.5 rounded-lg bg-red-500/10 border border-red-500/30 text-red-500 text-xs font-sans animate-fade-in">
            {t('typing.pasteDisabled')}
          </div>
        )}

        {/* Pause Overlay */}
        {isPaused && (
          <div
            onClick={resumeFromPause}
            className="absolute inset-0 z-10 bg-bg-surface/80 backdrop-blur-sm flex flex-col items-center justify-center gap-3 cursor-pointer"
          >
            <div className="text-lg font-bold text-text-primary">{t('typing.paused')}</div>
            <div className="text-xs text-text-muted">{t('typing.clickToResume')}</div>
          </div>
        )}

        {/* Code Viewport with Smooth Vertical Scroll */}
        <div
          style={{
            transform: `translateY(-${scrollOffsetY}px)`,
            transition: 'transform 150ms ease-out',
          }}
          className="flex flex-col"
        >
          {lines.map((line) => {
            const isCurrentLine = line.lineIndex === activeLineIdx;

            return (
              <div
                key={line.lineIndex}
                style={{ height: `${LINE_HEIGHT_PX}px` }}
                className={`flex items-center text-sm leading-[28px] px-2 rounded ${
                  isCurrentLine ? 'bg-bg-subtle/70' : ''
                }`}
              >
                {/* Gutter (Line Number) */}
                <span data-testid="gutter-line" className="w-10 select-none text-right pr-4 text-xs text-text-muted opacity-50">
                  {line.lineIndex + 1}
                </span>

                {/* Line Characters */}
                <div className="flex whitespace-pre">
                  {line.chars.map(({ target, state: charState, charIndex }) => {
                    const isCursorHere = charIndex === state.cursor;

                    const isError = charState === 'incorrect' || (charState === 'pending' && state.everWrong[charIndex]);

                    let colorClass = 'opacity-35 text-text-muted'; // pending
                    let underlineClass = '';

                    if (charState === 'correct') {
                      colorClass = 'opacity-100 text-text-primary';
                    } else if (isError) {
                      colorClass = 'text-red-500 font-bold';
                      underlineClass = 'underline decoration-red-500 decoration-2 underline-offset-4 bg-red-500/10';
                    } else if (charState === 'corrected') {
                      colorClass = 'text-amber-500';
                      underlineClass = 'underline decoration-amber-500 decoration-2 underline-offset-4';
                    } else if (charState === 'auto') {
                      colorClass = 'opacity-25 text-text-muted';
                    }

                    return (
                      <span key={charIndex} className="relative inline-block">
                        {/* Caret */}
                        {isCursorHere && !isPaused && (
                          <span
                            data-testid="typing-caret"
                            className="absolute left-0 top-1 bottom-1 w-[2px] bg-accent-primary animate-pulse shadow-sm"
                          />
                        )}
                        <span className={`${colorClass} ${underlineClass}`}>
                          {target.ch === '\n' ? '↵' : target.ch}
                        </span>
                      </span>
                    );
                  })}
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Virtual Keyboard */}
      {showKeyboard && (
        <VirtualKeyboard
          nextChar={nextExpectedChar}
          lastPressedCode={lastPressedCode}
          showFingers={true}
        />
      )}
    </div>
  );
};
