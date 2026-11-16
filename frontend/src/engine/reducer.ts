/**
 * Typing Engine Reducer — docs/07 §2, §3.
 * Pure function: reduce(state, event) -> EngineState
 * Completely deterministic and decoupled from browser APIs.
 */

import { EngineState, KeyEvent, CharState, TargetChar, EngineConfig } from './types';
import { prepareTargets, LanguageConfig } from './prepare';
import { FREE_MODE_CONSECUTIVE_ERRORS_LIMIT } from './constants';

/**
 * Finds the first typable (non-auto) position in the snippet.
 */
export function getFirstTypablePosition(targets: TargetChar[]): number {
  let pos = 0;
  while (pos < targets.length && targets[pos]?.auto) {
    pos++;
  }
  return pos;
}

/**
 * Creates the initial engine state.
 */
export function createInitialState(config: EngineConfig): EngineState {
  const mode = config.mode || 'PRACTICE';
  const errorMode = config.errorMode || (mode === 'PRACTICE' || mode === 'ACCURACY' || mode === 'SPECIAL_CHARS' || mode === 'WEAKNESS' ? 'stop' : 'free');
  const strictIndent = config.strictIndent || false;

  const langConfig: LanguageConfig = {
    indentStyle: config.indentStyle || 'SPACE',
    indentSize: config.indentSize || 2,
    tokenPatterns: config.tokenPatterns || [],
  };

  const targets = prepareTargets(config.snippets, langConfig, strictIndent);
  const currentTargets = targets[0] || [];
  const currentLen = currentTargets.length;

  const states: CharState[] = currentTargets.map((t) => (t.auto ? 'correct' : 'pending'));
  const typed: (string | null)[] = new Array(currentLen).fill(null);
  const everWrong: boolean[] = new Array(currentLen).fill(false);
  const cursor = getFirstTypablePosition(currentTargets);

  return {
    phase: 'idle',
    mode,
    errorMode,
    targets,
    seq: 0,
    states,
    typed,
    everWrong,
    cursor,
    consecutiveErrors: 0,
    activeMs: 0,
    lastEventAt: 0,
    limitMs: config.limitSeconds ? config.limitSeconds * 1000 : null,
    correctKeys: 0,
    incorrectKeys: 0,
    backspaces: 0,
    totalTyped: 0,
    finishedCorrect: 0,
    perSecond: [],
    log: [],
  };
}

/**
 * Advances cursor past auto-indent characters.
 */
function advanceCursor(cursor: number, targets: TargetChar[]): number {
  let next = cursor + 1;
  while (next < targets.length && targets[next]?.auto) {
    next++;
  }
  return next;
}

/**
 * Updates per-second keystroke counts for consistency calculation.
 */
function recordPerSecondKeystroke(perSecond: number[], eventTimeMs: number): number[] {
  const sec = Math.floor(eventTimeMs / 1000);
  const updated = [...perSecond];
  while (updated.length <= sec) {
    updated.push(0);
  }
  const curr = updated[sec] ?? 0;
  updated[sec] = curr + 1;
  return updated;
}

/**
 * Pure engine reducer function.
 */
export function reduce(state: EngineState, event: KeyEvent): EngineState {
  // If already finished, ignore all subsequent events
  if (state.phase === 'finished') {
    return state;
  }

  // FSM Phase Transitions (docs/07 §3.1)
  if (state.phase === 'idle') {
    // Only char, enter, tab transitions from idle to running
    if (event.k !== 'char' && event.k !== 'enter' && event.k !== 'tab') {
      return state;
    }
    // Start running at t=0 of this event
    const startedState: EngineState = {
      ...state,
      phase: 'running',
      lastEventAt: event.t,
      activeMs: 0,
    };
    return reduce(startedState, event);
  }

  if (state.phase === 'paused') {
    if (event.k === 'resume') {
      return {
        ...state,
        phase: 'running',
        lastEventAt: event.t,
        log: [...state.log, event],
      };
    }
    // Any typing input event automatically resumes and processes
    if (event.k === 'char' || event.k === 'enter' || event.k === 'tab' || event.k === 'backspace' || event.k === 'wordDelete') {
      const resumedState: EngineState = {
        ...state,
        phase: 'running',
        lastEventAt: event.t,
        log: [...state.log, { t: event.t, k: 'resume' }],
      };
      return reduce(resumedState, event);
    }
    return state;
  }

  // Now state.phase is 'running'
  const currentTargets = state.targets[state.seq] || [];
  const currentLen = currentTargets.length;
  const newLog = [...state.log, event];

  // Active time calculation
  let newActiveMs = state.activeMs;
  if (event.t >= state.lastEventAt) {
    newActiveMs += event.t - state.lastEventAt;
  }
  const newLastEventAt = event.t;

  // Handle pause
  if (event.k === 'pause') {
    if (state.errorMode === 'stop') {
      return {
        ...state,
        phase: 'paused',
        activeMs: newActiveMs,
        lastEventAt: newLastEventAt,
        log: newLog,
      };
    }
    return state; // In free mode, pause is ignored
  }

  // Handle timeout
  if (event.k === 'timeout') {
    return {
      ...state,
      phase: 'finished',
      activeMs: state.limitMs !== null ? state.limitMs : newActiveMs,
      lastEventAt: newLastEventAt,
      log: newLog,
    };
  }

  // Handle finishEarly (PRACTICE or CHALLENGE when at end of snippet)
  if (event.k === 'finishEarly') {
    return {
      ...state,
      phase: 'finished',
      activeMs: newActiveMs,
      lastEventAt: newLastEventAt,
      log: newLog,
    };
  }

  // Handle next (docs/07 §3.6)
  if (event.k === 'next') {
    // Only allowed if current snippet is completed (cursor reached end)
    if (state.cursor < currentLen) {
      return state;
    }
    const nextSeq = state.seq + 1;
    if (nextSeq >= state.targets.length) {
      // No more snippets
      return {
        ...state,
        phase: 'finished',
        activeMs: newActiveMs,
        lastEventAt: newLastEventAt,
        log: newLog,
      };
    }

    // Count correct chars in completed snippet
    let snippetCorrect = 0;
    for (let i = 0; i < currentLen; i++) {
      const tc = currentTargets[i];
      if (tc && !tc.auto && (state.states[i] === 'correct' || state.states[i] === 'corrected')) {
        snippetCorrect++;
      }
    }

    const nextTargets = state.targets[nextSeq];
    if (!nextTargets) {
      return {
        ...state,
        phase: 'finished',
        activeMs: newActiveMs,
        lastEventAt: newLastEventAt,
        log: newLog,
      };
    }
    const nextLen = nextTargets.length;
    const nextStates: CharState[] = nextTargets.map((t) => (t.auto ? 'correct' : 'pending'));
    const nextTyped: (string | null)[] = new Array(nextLen).fill(null);
    const nextEverWrong: boolean[] = new Array(nextLen).fill(false);
    const nextCursor = getFirstTypablePosition(nextTargets);

    return {
      ...state,
      seq: nextSeq,
      targets: state.targets,
      states: nextStates,
      typed: nextTyped,
      everWrong: nextEverWrong,
      cursor: nextCursor,
      consecutiveErrors: 0,
      finishedCorrect: state.finishedCorrect + snippetCorrect,
      activeMs: newActiveMs,
      lastEventAt: newLastEventAt,
      log: newLog,
    };
  }

  // Handle backspace (docs/07 §3.4)
  if (event.k === 'backspace') {
    const firstPos = getFirstTypablePosition(currentTargets);
    if (state.errorMode === 'stop') {
      return {
        ...state,
        backspaces: state.backspaces + 1,
        activeMs: newActiveMs,
        lastEventAt: newLastEventAt,
        log: newLog,
      };
    }

    // Free mode
    if (state.cursor <= firstPos) {
      return {
        ...state,
        activeMs: newActiveMs,
        lastEventAt: newLastEventAt,
        log: newLog,
      };
    }

    let prevPos = state.cursor - 1;
    while (prevPos >= 0 && currentTargets[prevPos]?.auto) {
      prevPos--;
    }

    if (prevPos < 0) {
      return {
        ...state,
        activeMs: newActiveMs,
        lastEventAt: newLastEventAt,
        log: newLog,
      };
    }

    const newStates = [...state.states];
    const newTyped = [...state.typed];
    let newConsecutiveErrors = state.consecutiveErrors;

    if (newStates[prevPos] === 'incorrect') {
      newConsecutiveErrors = Math.max(0, newConsecutiveErrors - 1);
    }
    newStates[prevPos] = 'pending';
    newTyped[prevPos] = null;

    return {
      ...state,
      cursor: prevPos,
      states: newStates,
      typed: newTyped,
      consecutiveErrors: newConsecutiveErrors,
      backspaces: state.backspaces + 1,
      activeMs: newActiveMs,
      lastEventAt: newLastEventAt,
      log: newLog,
    };
  }

  // Handle wordDelete (Ctrl/Option+Backspace) (docs/07 §3.4)
  if (event.k === 'wordDelete') {
    const firstPos = getFirstTypablePosition(currentTargets);
    if (state.errorMode === 'stop') {
      return {
        ...state,
        backspaces: state.backspaces + 1,
        activeMs: newActiveMs,
        lastEventAt: newLastEventAt,
        log: newLog,
      };
    }

    if (state.cursor <= firstPos) {
      return {
        ...state,
        activeMs: newActiveMs,
        lastEventAt: newLastEventAt,
        log: newLog,
      };
    }

    const newStates = [...state.states];
    const newTyped = [...state.typed];

    let pos = state.cursor - 1;
    while (pos >= 0 && currentTargets[pos]?.auto) {
      pos--;
    }

    if (pos < 0) {
      return {
        ...state,
        activeMs: newActiveMs,
        lastEventAt: newLastEventAt,
        log: newLog,
      };
    }

    const currentPosChar = currentTargets[pos];
    if (!currentPosChar) {
      return {
        ...state,
        activeMs: newActiveMs,
        lastEventAt: newLastEventAt,
        log: newLog,
      };
    }

    // If at newline, only delete newline
    if (currentPosChar.ch === '\n') {
      newStates[pos] = 'pending';
      newTyped[pos] = null;
      let newCursor = pos;
      while (newCursor > 0 && currentTargets[newCursor - 1]?.auto) {
        newCursor--;
      }
      return {
        ...state,
        cursor: pos,
        states: newStates,
        typed: newTyped,
        backspaces: state.backspaces + 1,
        activeMs: newActiveMs,
        lastEventAt: newLastEventAt,
        log: newLog,
      };
    }

    const targetWordId = currentPosChar.wordId;
    while (pos >= firstPos && currentTargets[pos]?.wordId === targetWordId && currentTargets[pos]?.ch !== '\n') {
      const tc = currentTargets[pos];
      if (tc && !tc.auto) {
        newStates[pos] = 'pending';
        newTyped[pos] = null;
      }
      pos--;
    }

    // Step back to the next typable position after the deleted word
    const nextCursor = Math.max(firstPos, pos + 1);

    // Calculate remaining consecutive errors to the left of nextCursor
    let leftErrors = 0;
    let scan = nextCursor - 1;
    while (scan >= 0 && newStates[scan] === 'incorrect') {
      leftErrors++;
      scan--;
    }

    return {
      ...state,
      cursor: nextCursor,
      states: newStates,
      typed: newTyped,
      consecutiveErrors: leftErrors,
      backspaces: state.backspaces + 1,
      activeMs: newActiveMs,
      lastEventAt: newLastEventAt,
      log: newLog,
    };
  }

  // Handle Tab key (docs/07 §3.3)
  if (event.k === 'tab') {
    // Check if cursor is at end
    if (state.cursor >= currentLen) {
      return state;
    }

    const exp = currentTargets[state.cursor];
    if (!exp) {
      return state;
    }

    if (exp.ch === '\t') {
      // Strict tab: ≡ char('\t')
      return reduce(state, { t: event.t, k: 'char', ch: '\t' });
    }

    // Strict, indentStyle=SPACE, cursor is in indent zone with expected space:
    // next min(indentSize, remaining spaces in indent) positions become correct with 1 keystroke
    if (exp.ch === ' ') {
      // Find remaining contiguous leading spaces in this line
      let spaceCount = 0;
      let checkPos = state.cursor;
      while (
        checkPos < currentLen &&
        currentTargets[checkPos]?.line === exp.line &&
        currentTargets[checkPos]?.ch === ' '
      ) {
        spaceCount++;
        checkPos++;
      }

      if (spaceCount > 0) {
        const fillCount = Math.min(spaceCount, 2); // default indentSize or min remaining
        const newStates = [...state.states];
        for (let i = 0; i < fillCount; i++) {
          newStates[state.cursor + i] = 'correct';
        }

        let newCursor = state.cursor + fillCount;
        while (newCursor < currentLen && currentTargets[newCursor]?.auto) {
          newCursor++;
        }

        const newPerSec = recordPerSecondKeystroke(state.perSecond, event.t);
        const isFinished = newCursor >= currentLen && state.seq === state.targets.length - 1;

        return {
          ...state,
          phase: isFinished ? 'finished' : state.phase,
          states: newStates,
          cursor: newCursor,
          correctKeys: state.correctKeys + 1,
          totalTyped: state.totalTyped + 1,
          perSecond: newPerSec,
          activeMs: newActiveMs,
          lastEventAt: newLastEventAt,
          log: newLog,
        };
      }
    }

    // Otherwise treated as error char('\t')
    return reduce(state, { t: event.t, k: 'char', ch: '\t' });
  }

  // Handle char and enter (enter ≡ char('\n'))
  const inputChar = event.k === 'enter' ? '\n' : event.k === 'char' ? event.ch : '';

  // In CHALLENGE single-snippet mode, Enter at the end of snippet triggers finishEarly (docs/07 §3.1)
  if (event.k === 'enter' && state.cursor >= currentLen && state.mode === 'CHALLENGE') {
    return {
      ...state,
      phase: 'finished',
      activeMs: newActiveMs,
      lastEventAt: newLastEventAt,
      log: [...state.log, { t: event.t, k: 'finishEarly' }],
    };
  }

  if (state.cursor >= currentLen) {
    return state;
  }

  // Free mode consecutive errors check (docs/07 §3.2)
  if (state.errorMode === 'free' && state.consecutiveErrors >= FREE_MODE_CONSECUTIVE_ERRORS_LIMIT) {
    // Keystroke is recorded in log, but ignored from counters and state
    return {
      ...state,
      activeMs: newActiveMs,
      lastEventAt: newLastEventAt,
      log: newLog,
    };
  }

  const expChar = currentTargets[state.cursor];
  if (!expChar) {
    return state;
  }
  const isMatch = inputChar === expChar.ch;

  const newTotalTyped = state.totalTyped + 1;
  const newPerSec = recordPerSecondKeystroke(state.perSecond, event.t);

  if (isMatch) {
    const newStates = [...state.states];
    newStates[state.cursor] = state.everWrong[state.cursor] ? 'corrected' : 'correct';
    const nextCursor = advanceCursor(state.cursor, currentTargets);
    const isFinished = nextCursor >= currentLen && state.seq === state.targets.length - 1;

    return {
      ...state,
      phase: isFinished ? 'finished' : state.phase,
      states: newStates,
      cursor: nextCursor,
      consecutiveErrors: 0,
      correctKeys: state.correctKeys + 1,
      totalTyped: newTotalTyped,
      perSecond: newPerSec,
      activeMs: newActiveMs,
      lastEventAt: newLastEventAt,
      log: newLog,
    };
  } else {
    // Incorrect character
    const newEverWrong = [...state.everWrong];
    newEverWrong[state.cursor] = true;

    if (state.errorMode === 'stop') {
      // In stop mode, cursor does not move
      return {
        ...state,
        everWrong: newEverWrong,
        incorrectKeys: state.incorrectKeys + 1,
        totalTyped: newTotalTyped,
        perSecond: newPerSec,
        activeMs: newActiveMs,
        lastEventAt: newLastEventAt,
        log: newLog,
      };
    } else {
      // Free mode
      const newStates = [...state.states];
      const newTyped = [...state.typed];
      newStates[state.cursor] = 'incorrect';
      newTyped[state.cursor] = inputChar;

      const nextCursor = advanceCursor(state.cursor, currentTargets);

      return {
        ...state,
        cursor: nextCursor,
        states: newStates,
        typed: newTyped,
        everWrong: newEverWrong,
        consecutiveErrors: state.consecutiveErrors + 1,
        incorrectKeys: state.incorrectKeys + 1,
        totalTyped: newTotalTyped,
        perSecond: newPerSec,
        activeMs: newActiveMs,
        lastEventAt: newLastEventAt,
        log: newLog,
      };
    }
  }
}
