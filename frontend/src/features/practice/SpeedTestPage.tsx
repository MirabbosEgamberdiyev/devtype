import React, { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { TypingView } from '../typing/TypingView';

export const SpeedTestPage: React.FC = () => {
  const { lang } = useParams<{ lang?: string }>();
  const navigate = useNavigate();
  const { t } = useTranslation();
  const [duration, setDuration] = useState<30 | 60 | 120>(60);

  return (
    <div className="flex flex-col gap-6 w-full max-w-4xl mx-auto py-4">
      {/* Duration selector */}
      <div className="flex items-center justify-between px-6 py-3 rounded-xl bg-bg-surface border border-border-default shadow-sm text-sm">
        <span className="font-semibold text-text-primary">{t('speedTest.selectDuration')}:</span>
        <div className="flex items-center gap-2 font-mono">
          {([30, 60, 120] as const).map((d) => (
            <button
              key={d}
              onClick={() => setDuration(d)}
              className={`px-3 py-1 rounded-lg text-xs font-semibold transition-colors ${
                duration === d
                  ? 'bg-accent-primary text-white shadow-sm'
                  : 'bg-bg-subtle text-text-muted hover:text-text-primary'
              }`}
            >
              {d}s
            </button>
          ))}
        </div>
      </div>

      <TypingView
        key={`${lang}-${duration}`}
        language={lang || 'java'}
        mode="SPEED_TEST"
        duration={duration}
        onBackToTopics={() => navigate('/languages')}
      />
    </div>
  );
};
