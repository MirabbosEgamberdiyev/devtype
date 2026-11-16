export type FingerZone = 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8;

export interface KeyDef {
  code: string;
  base?: string;
  shift?: string;
  label?: string;
  finger: FingerZone;
  w: number; // width factor (1 = standard 1u, 1.5 = tab, 2.25 = enter, etc.)
}

export interface KeyboardLayout {
  id: string;
  rows: KeyDef[][];
}
