import React, { useMemo } from 'react';
import { ANSI_US_LAYOUT } from './layouts/ansi-us';
import { KeyDef } from './types';

export interface VirtualKeyboardProps {
  nextChar?: string | null;
  lastPressedCode?: string | null;
  showFingers?: boolean;
}

// Finger zone subtle colors
const FINGER_BG_CLASSES: Record<number, string> = {
  0: 'border-b-2 border-b-rose-400/60',
  1: 'border-b-2 border-b-amber-400/60',
  2: 'border-b-2 border-b-emerald-400/60',
  3: 'border-b-2 border-b-sky-400/60',
  4: 'border-b-2 border-b-sky-400/60',
  5: 'border-b-2 border-b-emerald-400/60',
  6: 'border-b-2 border-b-amber-400/60',
  7: 'border-b-2 border-b-rose-400/60',
  8: 'border-b-2 border-b-purple-400/60',
};

export const VirtualKeyboard: React.FC<VirtualKeyboardProps> = ({
  nextChar,
  lastPressedCode,
  showFingers = true,
}) => {
  // Map char -> { code, needsShift, shiftSide }
  const charMap = useMemo(() => {
    const map = new Map<string, { code: string; needsShift: boolean; shiftSide: 'left' | 'right' }>();

    for (const row of ANSI_US_LAYOUT.rows) {
      for (const k of row) {
        if (k.base) {
          map.set(k.base, { code: k.code, needsShift: false, shiftSide: 'left' });
        }
        if (k.shift) {
          // If key is on left hand (finger 0..3), use right shift; otherwise left shift
          const shiftSide = k.finger <= 3 ? 'right' : 'left';
          map.set(k.shift, { code: k.code, needsShift: true, shiftSide });
        }
      }
    }
    // Special mappings
    map.set('\n', { code: 'Enter', needsShift: false, shiftSide: 'left' });
    map.set('\t', { code: 'Tab', needsShift: false, shiftSide: 'left' });
    map.set(' ', { code: 'Space', needsShift: false, shiftSide: 'left' });

    return map;
  }, []);

  const activeTarget = useMemo(() => {
    if (!nextChar) return null;
    return charMap.get(nextChar) ?? null;
  }, [nextChar, charMap]);

  return (
    <div
      className="select-none pointer-events-none p-3 rounded-xl bg-bg-surface border border-border-default shadow-sm max-w-4xl mx-auto flex flex-col gap-1.5 font-mono text-xs"
      data-testid="virtual-keyboard"
    >
      {ANSI_US_LAYOUT.rows.map((row, rIdx) => (
        <div key={rIdx} className="flex gap-1.5 justify-center">
          {row.map((k: KeyDef) => {
            const isTarget = activeTarget?.code === k.code;
            const isTargetShift =
              activeTarget?.needsShift &&
              ((activeTarget.shiftSide === 'left' && k.code === 'ShiftLeft') ||
                (activeTarget.shiftSide === 'right' && k.code === 'ShiftRight'));

            const isPressed = lastPressedCode === k.code;

            const fingerStyle = showFingers ? FINGER_BG_CLASSES[k.finger] ?? '' : '';

            let stateStyle = 'bg-bg-subtle text-text-primary border-border-default';
            if (isPressed) {
              stateStyle = 'bg-accent-primary text-white border-accent-primary scale-95 transition-transform duration-75';
            } else if (isTarget || isTargetShift) {
              stateStyle = 'bg-accent-subtle/40 border-accent-primary text-accent-primary font-bold animate-pulse shadow-sm ring-1 ring-accent-primary';
            }

            return (
              <div
                key={k.code}
                style={{ flexGrow: k.w * 10, flexBasis: `${k.w * 36}px`, minWidth: `${k.w * 28}px` }}
                className={`h-10 flex flex-col items-center justify-center rounded-md border text-center transition-colors duration-100 ${fingerStyle} ${stateStyle}`}
              >
                {k.label ? (
                  <span>{k.label}</span>
                ) : (
                  <>
                    {k.shift && <span className="text-[10px] opacity-75">{k.shift}</span>}
                    <span>{k.base}</span>
                  </>
                )}
              </div>
            );
          })}
        </div>
      ))}
    </div>
  );
};
