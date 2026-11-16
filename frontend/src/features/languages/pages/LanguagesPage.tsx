import React, { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { languagesApi, LanguageResponse } from '../api';

export const LanguagesPage: React.FC = () => {
  const { t } = useTranslation('common');
  const [languages, setLanguages] = useState<LanguageResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [selectedSlug, setSelectedSlug] = useState<string>('java');

  useEffect(() => {
    let isMounted = true;
    languagesApi
      .getLanguages()
      .then((data) => {
        if (!isMounted) return;
        setLanguages(data);
        const firstPublished = data.find((l) => l.status === 'PUBLISHED');
        if (firstPublished) {
          setSelectedSlug(firstPublished.slug);
        }
      })
      .finally(() => {
        if (isMounted) setIsLoading(false);
      });

    return () => {
      isMounted = false;
    };
  }, []);

  if (isLoading) {
    return (
      <div className="flex items-center justify-center p-12">
        <div className="w-8 h-8 border-2 border-accent border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  const publishedLanguages = languages.filter((l) => l.status === 'PUBLISHED');
  const draftLanguages = languages.filter((l) => l.status === 'DRAFT');

  return (
    <div className="max-w-5xl mx-auto space-y-8 py-4">
      <div className="space-y-2">
        <h1 className="text-3xl font-extrabold tracking-tight text-text">
          {t('languages.title', 'Programming Languages')}
        </h1>
        <p className="text-sm text-text-muted">
          {t('languages.subtitle', 'Select a language to master keyboard fluency with real idioms')}
        </p>
      </div>

      <div className="space-y-4">
        <h2 className="text-lg font-semibold text-text">
          {t('languages.availableSection', 'Available in Release 1')}
        </h2>
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4">
          {publishedLanguages.map((lang) => {
            const isSelected = selectedSlug === lang.slug;
            return (
              <div
                key={lang.slug}
                onClick={() => setSelectedSlug(lang.slug)}
                className={`p-5 rounded-xl border transition-all cursor-pointer bg-surface hover:border-accent/60 ${
                  isSelected ? 'border-accent ring-1 ring-accent shadow-md' : 'border-border'
                }`}
              >
                <div className="flex items-center justify-between">
                  <div className="font-bold text-lg text-text">{lang.name}</div>
                  <span className="text-xs font-mono uppercase bg-accent/10 text-accent px-2 py-0.5 rounded">
                    {lang.slug}
                  </span>
                </div>
                <div className="mt-3 text-xs text-text-muted space-y-1">
                  <div>Indent: {lang.indentSize} {lang.indentStyle.toLowerCase()}s</div>
                  <div>Case: {lang.keywordCase.toLowerCase()}</div>
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {draftLanguages.length > 0 && (
        <div className="space-y-4 pt-4 border-t border-border">
          <h2 className="text-lg font-semibold text-text-muted">
            {t('languages.comingSoonSection', 'Coming Soon (Release 2 & 3)')}
          </h2>
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-3">
            {draftLanguages.map((lang) => (
              <div
                key={lang.slug}
                className="p-4 rounded-lg border border-border bg-surface/50 opacity-60 flex items-center justify-between"
              >
                <span className="text-sm font-medium text-text">{lang.name}</span>
                <span className="text-[10px] uppercase font-mono px-1.5 py-0.5 rounded bg-surface-2 text-text-muted border border-border">
                  R{lang.release}
                </span>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};
