import { describe, it, expect } from 'vitest';
import { prepareSingleTarget, prepareTargets } from '../prepare';

describe('prepareTargets — docs/07 §1', () => {
  it('strips carriage returns and trailing newlines', () => {
    const raw = 'const a = 1;\r\nconst b = 2;\r\n\n';
    const targets = prepareSingleTarget(raw);

    // Characters should not contain \r
    for (const t of targets) {
      expect(t.ch).not.toBe('\r');
    }
    // Should end at ';' of the second line
    expect(targets[targets.length - 1]?.ch).toBe(';');
  });

  it('marks leading indentation as auto when strictIndent=false', () => {
    const code = 'function foo() {\n  return 42;\n}';
    const targets = prepareSingleTarget(
      code,
      { indentStyle: 'SPACE', indentSize: 2 },
      false
    );

    // '  return' has 2 leading spaces
    const line1Chars = targets.filter((t) => t.line === 1);
    expect(line1Chars[0]?.ch).toBe(' ');
    expect(line1Chars[0]?.auto).toBe(true);
    expect(line1Chars[1]?.ch).toBe(' ');
    expect(line1Chars[1]?.auto).toBe(true);
    expect(line1Chars[2]?.ch).toBe('r');
    expect(line1Chars[2]?.auto).toBe(false);
  });

  it('marks leading indentation as auto=false when strictIndent=true', () => {
    const code = 'function foo() {\n  return 42;\n}';
    const targets = prepareSingleTarget(
      code,
      { indentStyle: 'SPACE', indentSize: 2 },
      true
    );

    const line1Chars = targets.filter((t) => t.line === 1);
    expect(line1Chars[0]?.ch).toBe(' ');
    expect(line1Chars[0]?.auto).toBe(false);
    expect(line1Chars[1]?.ch).toBe(' ');
    expect(line1Chars[1]?.auto).toBe(false);
  });

  it('matches token patterns greedily and non-overlapping', () => {
    const code = 'a === b && c == d';
    const targets = prepareSingleTarget(code, {
      tokenPatterns: ['==', '===', '&&'],
    });

    // '===' should match '===' (length 3), not '=='
    const eq1 = targets.filter((t) => t.col >= 2 && t.col <= 4);
    expect(eq1.map((t) => t.ch).join('')).toBe('===');
    expect(eq1[0]?.patternId).toBeGreaterThanOrEqual(0);
    expect(eq1[0]?.patternId).toBe(eq1[1]?.patternId);
    expect(eq1[1]?.patternId).toBe(eq1[2]?.patternId);

    // '&&' should match
    const and = targets.filter((t) => t.ch === '&');
    expect(and.length).toBe(2);
    expect(and[0]?.patternId).toBeGreaterThanOrEqual(0);
    expect(and[0]?.patternId).toBe(and[1]?.patternId);
    expect(and[0]?.patternId).not.toBe(eq1[0]?.patternId);

    // '==' should match
    const eq2 = targets.filter((t) => t.col >= 13 && t.col <= 14);
    expect(eq2.map((t) => t.ch).join('')).toBe('==');
    expect(eq2[0]?.patternId).toBeGreaterThanOrEqual(0);
    expect(eq2[0]?.patternId).toBe(eq2[1]?.patternId);
  });

  it('correctly tracks word boundaries for wordId according to Rule 6', () => {
    const code = 't_ids.COUNT';
    const targets = prepareSingleTarget(code);

    const tIds = targets.filter((t) => 't_ids'.includes(t.ch));
    const dot = targets.find((t) => t.ch === '.');
    const count = targets.filter((t) => 'COUNT'.includes(t.ch));

    const wordId1 = tIds[0]?.wordId ?? 0;
    const wordIdDot = dot!.wordId;
    const wordId2 = count[0]?.wordId ?? 0;

    expect(wordIdDot).toBe(wordId1 + 1);
    expect(wordId2).toBe(wordIdDot + 1);
  });

  it('handles multi-snippet targets with exerciseSeq', () => {
    const snippets = ['x = 1;', 'y = 2;'];
    const all = prepareTargets(snippets, {}, false);

    expect(all.length).toBe(2);
    expect(all[0]?.every((t) => t.exerciseSeq === 0)).toBe(true);
    expect(all[1]?.every((t) => t.exerciseSeq === 1)).toBe(true);
  });
});
