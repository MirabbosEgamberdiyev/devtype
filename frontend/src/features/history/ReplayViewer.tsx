import React, { useEffect, useRef, useState, useMemo } from 'react';
import { useTranslation } from 'react-i18next';
import { decodeKeylog } from '../../engine/keylog';
import { createInitialState, reduce } from '../../engine/reducer';
import { calculateMetrics } from '../../engine/metrics';
import { EngineState, KeyEvent } from '../../engine/types';

interface ReplayViewerProps {
  code: string;
  keylogBase64: string;
  language: string;
  finalMetrics?: {
    wpm: number;
    accuracy: number;
    durationMs: number;
    errorCount: number;
  };
  onClose?: () => void;
}

export const ReplayViewer: React.FC<ReplayViewerProps> = ({
  code,
  keylogBase64,
  language,
  finalMetrics,
  onClose,
}) => {
  const { t } = useTranslation('common');

  // Decode keylog
  const decoded = useMemo(() => {
    try {
      return decodeKeylog(keylogBase64);
    } catch {
      return null;
    }
  }, [keylogBase64]);

  const events: KeyEvent[] = useMemo(() => decoded?.events || [], [decoded]);

  const totalDurationMs = events.length > 0 ? events[events.length - 1]?.t ?? 0 : 0;

  // Precompute states at every event for instant scrub/seek performance
  const precomputedStates: EngineState[] = useMemo(() => {
    if (!events.length) return [];
    const states: EngineState[] = [];
    let state = createInitialState({
      snippets: [code],
      strictIndent: decoded?.header.strictIndent ?? false,
    });
    for (const ev of events) {
      state = reduce(state, ev);
      states.push(state);
    }
    return states;
  }, [events, code, decoded]);

  // Playback state
  const [currentEventIndex, setCurrentEventIndex] = useState(0);
  const [isPlaying, setIsPlaying] = useState(false);
  const [speed, setSpeed] = useState<1 | 2 | 4>(1);

  const requestRef = useRef<number | null>(null);
  const lastTimeRef = useRef<number | null>(null);
  const playheadMsRef = useRef<number>(0);

  const currentState = precomputedStates[currentEventIndex] || null;

  // Calculate live metrics at current state
  const liveMetrics = useMemo(() => {
    if (!currentState) {
      return finalMetrics || { wpm: 0, accuracy: 100, durationMs: 0, errorCount: 0 };
    }
    const computed = calculateMetrics(currentState);
    const duration = events[currentEventIndex]?.t ?? 0;
    return {
      wpm: computed.wpm,
      accuracy: computed.accuracy,
      durationMs: duration,
      errorCount: currentState.incorrectKeys,
    };
  }, [currentState, currentEventIndex, events, finalMetrics]);

  // Playback loop
  useEffect(() => {
    if (!isPlaying) {
      if (requestRef.current) cancelAnimationFrame(requestRef.current);
      lastTimeRef.current = null;
      return;
    }

    const animate = (time: number) => {
      if (lastTimeRef.current !== null) {
        const delta = (time - lastTimeRef.current) * speed;
        playheadMsRef.current += delta;

        // Find index corresponding to playheadMs
        let nextIdx = currentEventIndex;
        while (
          nextIdx < events.length &&
          (events[nextIdx]?.t ?? 0) <= playheadMsRef.current
        ) {
          nextIdx++;
        }

        if (nextIdx >= events.length) {
          // Reached end of replay
          setCurrentEventIndex(Math.max(0, events.length - 1));
          setIsPlaying(false);
          return;
        } else {
          setCurrentEventIndex(nextIdx);
        }
      }
      lastTimeRef.current = time;
      requestRef.current = requestAnimationFrame(animate);
    };

    requestRef.current = requestAnimationFrame(animate);

    return () => {
      if (requestRef.current) cancelAnimationFrame(requestRef.current);
    };
  }, [isPlaying, speed, currentEventIndex, events]);

  const togglePlay = () => {
    if (currentEventIndex >= events.length - 1) {
      // If at end, restart
      playheadMsRef.current = 0;
      setCurrentEventIndex(0);
      setIsPlaying(true);
    } else {
      playheadMsRef.current = events[currentEventIndex]?.t ?? 0;
      setIsPlaying(!isPlaying);
    }
  };

  const handleSeek = (newIndex: number) => {
    const clamped = Math.max(0, Math.min(newIndex, events.length - 1));
    setCurrentEventIndex(clamped);
    playheadMsRef.current = events[clamped]?.t ?? 0;
  };

  const skipTime = (deltaMs: number) => {
    const targetMs = Math.max(0, Math.min(playheadMsRef.current + deltaMs, totalDurationMs));
    playheadMsRef.current = targetMs;
    let nextIdx = 0;
    while (nextIdx < events.length && (events[nextIdx]?.t ?? 0) <= targetMs) {
      nextIdx++;
    }
    setCurrentEventIndex(Math.min(nextIdx, events.length - 1));
  };

  if (!decoded) {
    return (
      <div className="p-6 rounded-xl border border-red-500/30 bg-red-500/10 text-center">
        <p className="text-red-400 font-medium">
          {t('history.invalidKeylog', 'Unable to decode session keylog for replay.')}
        </p>
        {onClose && (
          <button
            onClick={onClose}
            className="mt-4 px-4 py-2 bg-surface-2 border border-border rounded-lg text-sm text-text"
          >
            {t('actions.cancel', 'Close')}
          </button>
        )}
      </div>
    );
  }

  // Format code display with current states
  const targetChars = currentState?.targets[currentState?.seq || 0] || [];
  const charStates = currentState?.states || [];
  const cursor = currentState?.cursor || 0;

  return (
    <div className="rounded-2xl border border-border bg-surface overflow-hidden shadow-xl flex flex-col max-w-4xl mx-auto">
      {/* Replay Header */}
      <div className="p-4 border-b border-border bg-surface-2 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <span className="font-bold text-accent text-sm uppercase tracking-wider font-mono">
            {language} REPLAY
          </span>
          <span className="text-xs text-text-muted">
            {events.length} {t('history.events', 'keystrokes')}
          </span>
        </div>
        <div className="flex items-center gap-4">
          <div className="flex items-center gap-2 font-mono text-sm">
            <span className="text-text font-bold">{liveMetrics.wpm} WPM</span>
            <span className="text-text-muted">·</span>
            <span className="text-text-muted">{liveMetrics.accuracy}% acc</span>
            <span className="text-text-muted">·</span>
            <span className="text-text-muted">{(liveMetrics.durationMs / 1000).toFixed(1)}s</span>
          </div>
          {onClose && (
            <button
              onClick={onClose}
              className="text-text-muted hover:text-text text-lg p-1 rounded transition-colors"
              aria-label="Close replay"
            >
              ✕
            </button>
          )}
        </div>
      </div>

      {/* Code Display Area */}
      <div className="p-6 font-mono text-sm leading-relaxed overflow-x-auto min-h-[220px] bg-bg/70 select-none">
        <pre className="inline-block whitespace-pre">
          {targetChars.map((tChar, idx) => {
            const state = charStates[idx] || 'pending';
            const isCursor = idx === cursor;

            let colorClass = 'text-text-muted/50';
            if (state === 'correct') colorClass = 'text-emerald-400';
            if (state === 'incorrect') colorClass = 'text-red-400 bg-red-500/20 underline';
            if (state === 'corrected') colorClass = 'text-amber-400';
            if (tChar.auto) colorClass = 'text-text-muted/30';

            return (
              <span
                key={idx}
                className={`relative ${colorClass} ${
                  isCursor ? 'border-l-2 border-accent animate-pulse font-bold' : ''
                }`}
              >
                {tChar.ch === '\n' ? '↵\n' : tChar.ch}
              </span>
            );
          })}
        </pre>
      </div>

      {/* Controls Bar (AC-CE-8) */}
      <div className="p-4 border-t border-border bg-surface-2 flex flex-col gap-3">
        {/* Timeline Slider */}
        <div className="flex items-center gap-3">
          <span className="text-xs font-mono text-text-muted min-w-[36px]">
            {(liveMetrics.durationMs / 1000).toFixed(1)}s
          </span>
          <input
            type="range"
            min={0}
            max={Math.max(0, events.length - 1)}
            value={currentEventIndex}
            onChange={(e) => handleSeek(Number(e.target.value))}
            className="flex-1 accent-accent cursor-pointer h-1.5 bg-surface-3 rounded-lg"
            aria-label="Replay seek slider"
          />
          <span className="text-xs font-mono text-text-muted min-w-[36px]">
            {(totalDurationMs / 1000).toFixed(1)}s
          </span>
        </div>

        {/* Playback Controls & Speed Buttons */}
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <button
              onClick={() => skipTime(-5000)}
              className="p-2 rounded-lg bg-surface hover:bg-surface-3 border border-border text-xs text-text font-mono transition-colors"
              title="-5 seconds"
              aria-label="Seek back 5 seconds"
            >
              -5s
            </button>
            <button
              onClick={togglePlay}
              className="px-5 py-2 rounded-lg bg-accent text-accent-fg font-bold text-sm hover:opacity-90 transition-opacity flex items-center gap-2"
              aria-label={isPlaying ? 'Pause replay' : 'Play replay'}
            >
              <span>{isPlaying ? '⏸' : '▶'}</span>
              <span>{isPlaying ? t('actions.pause', 'Pause') : t('actions.play', 'Play')}</span>
            </button>
            <button
              onClick={() => skipTime(5000)}
              className="p-2 rounded-lg bg-surface hover:bg-surface-3 border border-border text-xs text-text font-mono transition-colors"
              title="+5 seconds"
              aria-label="Seek forward 5 seconds"
            >
              +5s
            </button>
          </div>

          {/* Speed Switcher: 1x, 2x, 4x */}
          <div className="flex items-center gap-1 bg-surface p-1 rounded-lg border border-border text-xs font-mono">
            {([1, 2, 4] as const).map((spd) => (
              <button
                key={spd}
                onClick={() => setSpeed(spd)}
                className={`px-2.5 py-1 rounded font-bold transition-colors ${
                  speed === spd
                    ? 'bg-accent text-accent-fg'
                    : 'text-text-muted hover:text-text hover:bg-surface-2'
                }`}
              >
                {spd}x
              </button>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};
