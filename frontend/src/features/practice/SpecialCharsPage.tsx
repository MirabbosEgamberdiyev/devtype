import React, { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { TypingView } from '../typing/TypingView';

const CATEGORIES = ['BRACKETS', 'ASSIGN_CMP', 'PUNCT', 'LOGICAL', 'ARITH', 'ARROWS', 'QUOTES'];

export const SpecialCharsPage: React.FC = () => {
  const { lang } = useParams<{ lang?: string }>();
  const navigate = useNavigate();
  const { t } = useTranslation();
  const [category, setCategory] = useState<string>('BRACKETS');

  return (
    <div className="flex flex-col gap-6 w-full max-w-4xl mx-auto py-4">
      {/* Category selector */}
      <div className="flex flex-wrap items-center justify-between gap-3 px-6 py-3 rounded-xl bg-bg-surface border border-border-default shadow-sm text-sm">
        <span className="font-semibold text-text-primary">{t('specialChars.selectCategory')}:</span>
        <div className="flex flex-wrap items-center gap-2 font-mono">
          {CATEGORIES.map((cat) => (
            <button
              key={cat}
              onClick={() => setCategory(cat)}
              className={`px-3 py-1 rounded-lg text-xs font-semibold transition-colors ${
                category === cat
                  ? 'bg-accent-primary text-white shadow-sm'
                  : 'bg-bg-subtle text-text-muted hover:text-text-primary'
              }`}
            >
              {cat}
            </button>
          ))}
        </div>
      </div>

      <TypingView
        key={`${lang}-${category}`}
        language={lang || 'java'}
        mode="SPECIAL_CHARS"
        specialCategory={category}
        onBackToTopics={() => navigate('/languages')}
      />
    </div>
  );
};
