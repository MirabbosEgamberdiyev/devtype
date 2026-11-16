import { describe, it, expect } from 'vitest';
import { validateSnippet, calculateCharCount } from '../validate-content';

describe('Content Validator (BL-EM-1..4 & docs/06)', () => {
  it('passes a completely valid BEGINNER snippet', () => {
    const yamlStr = `language: java
topic: variables
difficulty: BEGINNER
title: Primitive Data Types
note:
  en: Standard variable declarations in Java.
  uzl: Java tilida standart oʻzgaruvchilar eʻloni.
  ru: Стандартное объявление переменных в Java.
tags: [basics, types]
ranked_pool: false
source: original
code: |
  int currentYear = 2026;
  double temperature = 24.5;
  boolean isActive = true;
  String serviceName = "DevType";`;

    const seen = new Map<string, string>();
    const { errors } = validateSnippet('test.yaml', yamlStr, seen);
    expect(errors).toHaveLength(0);
  });

  it('calculates effective char count excluding automatic leading indent', () => {
    const code = `if (isActive) {\n    int count = 10;\n}`;
    // line 0: "if (isActive) {" -> 15 chars + 1 newline = 16
    // line 1: "    int count = 10;" -> 4 spaces ignored, "int count = 10;" (15) + 1 newline = 16
    // line 2: "}" -> 1
    // total = 16 + 16 + 1 = 33
    expect(calculateCharCount(code)).toBe(33);
  });

  it('Rule 2: catches CRLF line endings', () => {
    const yamlStr = "language: java\r\ntopic: variables\r\ndifficulty: BEGINNER\r\ntitle: Test\r\ncode: |\r\n  int x = 1;\r\n  int y = 2;";
    const { errors } = validateSnippet('test.yaml', yamlStr, new Map());
    expect(errors.some((e) => e.rule === 'CRLF_NOT_ALLOWED')).toBe(true);
  });

  it('Rule 3: catches trailing whitespace', () => {
    const yamlStr = `language: java
topic: variables
difficulty: BEGINNER
title: Test
code: |
  int x = 1;   
  int y = 2;`;
    const { errors } = validateSnippet('test.yaml', yamlStr, new Map());
    expect(errors.some((e) => e.rule === 'TRAILING_WHITESPACE')).toBe(true);
  });

  it('Rule 4: catches blank line at end of file', () => {
    const yamlStr = `language: java
topic: variables
difficulty: BEGINNER
title: Test
code: |
  int x = 1;
  int y = 2;

`;
    const { errors } = validateSnippet('test.yaml', yamlStr, new Map());
    expect(errors.some((e) => e.rule === 'BLANK_LINES')).toBe(true);
  });

  it('Rule 6: catches line longer than 80 chars', () => {
    const longLine = 'String message = "' + 'a'.repeat(70) + '";';
    const yamlStr = `language: java
topic: variables
difficulty: BEGINNER
title: Test
code: |
  ${longLine}
  int y = 2;`;
    const { errors } = validateSnippet('test.yaml', yamlStr, new Map());
    expect(errors.some((e) => e.rule === 'LINE_TOO_LONG')).toBe(true);
  });

  it('Rule 7: catches length out of difficulty range', () => {
    const yamlStr = `language: java
topic: variables
difficulty: BEGINNER
title: Test
code: |
  int x = 1;
  int y = 2;`;
    const { errors } = validateSnippet('test.yaml', yamlStr, new Map());
    expect(errors.some((e) => e.rule === 'LENGTH_OUT_OF_RANGE')).toBe(true);
  });

  it('Rule 8: catches duplicate snippet hash within same language', () => {
    const yamlStr1 = `language: java
topic: variables
difficulty: BEGINNER
title: Snippet One
code: |
  int initialCounter = 100;
  int secondaryCounter = 200;
  int totalSumResult = initialCounter + secondaryCounter;
  String formattedMessage = "Result is: " + totalSumResult;`;

    const yamlStr2 = `language: java
topic: variables
difficulty: BEGINNER
title: Snippet Two
code: |
  int initialCounter = 100;
  int secondaryCounter = 200;
  int totalSumResult = initialCounter + secondaryCounter;
  String formattedMessage = "Result is: " + totalSumResult;`;

    const seen = new Map<string, string>();
    const res1 = validateSnippet('f1.yaml', yamlStr1, seen);
    expect(res1.errors).toHaveLength(0);

    const res2 = validateSnippet('f2.yaml', yamlStr2, seen);
    expect(res2.errors.some((e) => e.rule === 'DUPLICATE_EXERCISE')).toBe(true);
  });

  it('Rule 9: catches forbidden constructs (TODO, foo, PL/SQL $$)', () => {
    const yamlStr = `language: plsql
topic: procedures
difficulty: BEGINNER
title: Test
code: |
  CREATE OR REPLACE PROCEDURE test_proc AS $$
  BEGIN
    NULL;
  END;
  /`;
    const { errors } = validateSnippet('test.yaml', yamlStr, new Map());
    expect(errors.some((e) => e.rule === 'FORBIDDEN_CONSTRUCT')).toBe(true);
  });

  it('Rule 10: catches smart quotes', () => {
    const yamlStr = `language: java
topic: variables
difficulty: BEGINNER
title: Test
code: |
  String text = “DevType”;
  int y = 2;`;
    const { errors } = validateSnippet('test.yaml', yamlStr, new Map());
    expect(errors.some((e) => e.rule === 'SMART_PUNCTUATION')).toBe(true);
  });

  it('Rule 11: catches too few lines', () => {
    const yamlStr = `language: java
topic: variables
difficulty: BEGINNER
title: Test
code: |
  int x = 1;`;
    const { errors } = validateSnippet('test.yaml', yamlStr, new Map());
    expect(errors.some((e) => e.rule === 'TOO_FEW_LINES')).toBe(true);
  });

  it('Rule 12: catches missing topic for SNIPPET', () => {
    const yamlStr = `language: java
difficulty: BEGINNER
title: Test
code: |
  int x = 1;
  int y = 2;`;
    const { errors } = validateSnippet('test.yaml', yamlStr, new Map());
    expect(errors.some((e) => e.rule === 'TOPIC_REQUIRED')).toBe(true);
  });
});
