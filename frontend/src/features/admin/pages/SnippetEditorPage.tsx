import React, { useState } from 'react';
import { apiClient } from '../../../shared/api/client';
import { ProblemError } from '../../../shared/api/problem';

const FORBIDDEN_SQL = [/(\$\$)/i, /(LANGUAGE\s+plpgsql)/i, /(RAISE\s+NOTICE)/i, /(RETURNS)/i, /(PERFORM)/i];

export const SnippetEditorPage: React.FC = () => {
  const [languageSlug, setLanguageSlug] = useState('java');
  const [difficulty, setDifficulty] = useState<'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED' | 'EXPERT'>('BEGINNER');
  const [title, setTitle] = useState('');
  const [note, setNote] = useState('');
  const [code, setCode] = useState('');
  const [rankedPool, setRankedPool] = useState(false);

  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  // Live lint metrics
  const lines = code.split('\n');
  const charCount = code.length;
  const longLines = lines
    .map((line, idx) => ({ lineNum: idx + 1, length: line.length }))
    .filter((l) => l.length > 80);

  const hasForbiddenDialect =
    (languageSlug === 'sql' || languageSlug === 'plsql') &&
    FORBIDDEN_SQL.some((pattern) => pattern.test(code));

  let minChars = 30;
  let maxChars = 120;
  if (difficulty === 'INTERMEDIATE') { minChars = 80; maxChars = 300; }
  else if (difficulty === 'ADVANCED') { minChars = 200; maxChars = 600; }
  else if (difficulty === 'EXPERT') { minChars = 400; maxChars = 1000; }

  const isLengthValid = charCount >= minChars && charCount <= maxChars;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMsg(null);
    setSuccessMsg(null);

    if (longLines.length > 0) {
      setErrorMsg(`Line ${longLines[0]?.lineNum} exceeds 80 characters (${longLines[0]?.length}).`);
      return;
    }

    if (!isLengthValid) {
      setErrorMsg(`Code length (${charCount}) must be between ${minChars} and ${maxChars} characters.`);
      return;
    }

    if (hasForbiddenDialect) {
      setErrorMsg('Code contains PostgreSQL/non-standard syntax forbidden in Oracle PL/SQL.');
      return;
    }

    setIsLoading(true);
    try {
      const response = await apiClient<{ exerciseKey: string; status: string }>('/admin/exercises', {
        method: 'POST',
        body: JSON.stringify({
          languageSlug,
          difficulty,
          title: title.trim(),
          note: note.trim() || null,
          code,
          rankedPool,
        }),
      });

      setSuccessMsg(`Exercise created successfully! Key: ${response.exerciseKey} (Status: ${response.status})`);
      setTitle('');
      setNote('');
      setCode('');
    } catch (err) {
      if (err instanceof ProblemError) {
        setErrorMsg(err.message);
      } else {
        setErrorMsg('Failed to save exercise.');
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto space-y-6 py-6 px-4">
      <div className="space-y-1">
        <h1 className="text-2xl font-bold text-text">Admin Snippet Editor</h1>
        <p className="text-sm text-text-muted">
          Author and lint code training snippets following the strict pedagogy guidelines (BL-EM-1..4).
        </p>
      </div>

      {errorMsg && (
        <div role="alert" className="p-3 text-sm rounded bg-error/10 border border-error/20 text-error">
          {errorMsg}
        </div>
      )}

      {successMsg && (
        <div role="status" className="p-3 text-sm rounded bg-success/10 border border-success/20 text-success">
          {successMsg}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-4 bg-surface p-6 rounded-xl border border-border shadow-sm">
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
          <div className="space-y-1">
            <label htmlFor="lang-select" className="block text-xs font-semibold text-text uppercase">Language</label>
            <select
              id="lang-select"
              value={languageSlug}
              onChange={(e) => setLanguageSlug(e.target.value)}
              className="w-full px-3 py-2 bg-surface-2 border border-border rounded-lg text-text text-sm"
            >
              <option value="java">Java</option>
              <option value="python">Python</option>
              <option value="javascript">JavaScript</option>
              <option value="sql">SQL</option>
              <option value="plsql">PL/SQL</option>
            </select>
          </div>

          <div className="space-y-1">
            <label htmlFor="diff-select" className="block text-xs font-semibold text-text uppercase">Difficulty</label>
            <select
              id="diff-select"
              value={difficulty}
              onChange={(e) => setDifficulty(e.target.value as 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED' | 'EXPERT')}
              className="w-full px-3 py-2 bg-surface-2 border border-border rounded-lg text-text text-sm"
            >
              <option value="BEGINNER">Beginner (30–120)</option>
              <option value="INTERMEDIATE">Intermediate (80–300)</option>
              <option value="ADVANCED">Advanced (200–600)</option>
              <option value="EXPERT">Expert (400–1000)</option>
            </select>
          </div>

          <div className="space-y-1 flex flex-col justify-end">
            <label className="flex items-center gap-2 cursor-pointer pb-2">
              <input
                type="checkbox"
                checked={rankedPool}
                onChange={(e) => setRankedPool(e.target.checked)}
                className="accent-accent w-4 h-4 rounded"
              />
              <span className="text-sm font-medium text-text">Ranked Speed Pool</span>
            </label>
          </div>
        </div>

        <div className="space-y-1">
          <label htmlFor="title-input" className="block text-xs font-semibold text-text uppercase">Snippet Title</label>
          <input
            id="title-input"
            type="text"
            required
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="e.g. Stream Map and Filter Pipeline"
            className="w-full px-3 py-2 bg-surface-2 border border-border rounded-lg text-text text-sm"
          />
        </div>

        <div className="space-y-1">
          <label htmlFor="note-input" className="block text-xs font-semibold text-text uppercase">Pedagogical Note (Optional)</label>
          <input
            id="note-input"
            type="text"
            value={note}
            onChange={(e) => setNote(e.target.value)}
            placeholder="Key concept or operator practice objective"
            className="w-full px-3 py-2 bg-surface-2 border border-border rounded-lg text-text text-sm"
          />
        </div>

        <div className="space-y-1">
          <div className="flex items-center justify-between text-xs">
            <label htmlFor="code-textarea" className="font-semibold text-text uppercase">Source Code (LF only)</label>
            <div className="flex items-center gap-3">
              <span className={isLengthValid ? 'text-success' : 'text-error font-medium'}>
                {charCount} / {minChars}–{maxChars} chars
              </span>
              <span className={longLines.length === 0 ? 'text-text-muted' : 'text-error font-medium'}>
                {longLines.length === 0 ? 'Max 80 cols: OK' : `${longLines.length} line(s) > 80 cols`}
              </span>
            </div>
          </div>
          <textarea
            id="code-textarea"
            required
            rows={10}
            value={code}
            onChange={(e) => setCode(e.target.value)}
            placeholder="Enter idiomatic code snippet without trailing spaces..."
            className="w-full p-3 font-mono text-xs bg-surface-2 border border-border rounded-lg text-text focus:outline-none focus:ring-2 focus:ring-focus-ring"
          />
        </div>

        {hasForbiddenDialect && (
          <div className="p-3 text-xs rounded bg-error/10 border border-error/20 text-error">
            ⚠️ Disallowed syntax detected: Oracle PL/SQL snippets must not use PostgreSQL syntax ($$, LANGUAGE plpgsql, RAISE NOTICE).
          </div>
        )}

        <div className="flex justify-end pt-2">
          <button
            type="submit"
            disabled={isLoading || !isLengthValid || longLines.length > 0 || hasForbiddenDialect}
            className="py-2.5 px-6 bg-accent hover:opacity-90 active:scale-[0.99] text-accent-fg font-medium rounded-lg text-sm transition-all disabled:opacity-50"
          >
            {isLoading ? 'Saving...' : 'Save Exercise (Draft)'}
          </button>
        </div>
      </form>
    </div>
  );
};
