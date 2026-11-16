/**
 * Typing Engine Constants — docs/00-conventions.md §4 va docs/07.
 */

export const FREE_MODE_CONSECUTIVE_ERRORS_LIMIT = 8;
export const INACTIVITY_PAUSE_SECONDS = 15;
export const HIDDEN_PAGE_ABANDON_SECONDS = 2;
export const SPECIAL_CHARS_TARGET_LENGTH = 150;
export const TIMING_TOLERANCE_MS = 2000;
export const SUSPICIOUS_WPM_THRESHOLD = 220;
export const SUSPICION_FLAG_THRESHOLD = 50;

export const COMPLETION_ACCURACY_THRESHOLD = 95.0;
export const MASTERED_ACCURACY_THRESHOLD = 98.0;
export const LEVEL_UNLOCK_COMPLETION_PERCENT = 80.0;
export const ACCURACY_MODE_MIN_ACCURACY = 97.0;

export const METRIC_SCALE = 2;

export const SUPPORTED_MODES = [
  'PRACTICE',
  'SPEED_TEST',
  'ACCURACY',
  'CHALLENGE',
  'SPECIAL_CHARS',
  'WEAKNESS',
  'CODE_SPRINT',
] as const;

export type Mode = (typeof SUPPORTED_MODES)[number];

export const SPECIAL_CATEGORIES = [
  'BRACKETS',
  'OPERATORS',
  'PUNCTUATION',
  'QUOTES',
  'ARROWS',
  'LOGICAL',
  'BITWISE',
  'CASE',
] as const;

export type SpecialCategory = (typeof SPECIAL_CATEGORIES)[number];
