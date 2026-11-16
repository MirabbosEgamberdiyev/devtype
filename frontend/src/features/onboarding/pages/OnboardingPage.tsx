import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { languagesApi, LanguageResponse } from '../../languages/api';
import { useAuthStore } from '../../../shared/stores/authStore';

export const OnboardingPage: React.FC = () => {
  const navigate = useNavigate();
  const { user, setAuth, accessToken } = useAuthStore();

  const [step, setStep] = useState<1 | 2 | 3>(1);
  const [languages, setLanguages] = useState<LanguageResponse[]>([]);
  const [selectedLangs, setSelectedLangs] = useState<string[]>(['java']);
  const [primaryLang, setPrimaryLang] = useState<string>('java');
  const [experience, setExperience] = useState<'NEW' | 'MID' | 'SENIOR'>('MID');
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    let isMounted = true;
    languagesApi
      .getLanguages()
      .then((data) => {
        if (!isMounted) return;
        const published = data.filter((l) => l.status === 'PUBLISHED');
        setLanguages(published);
        if (published.length > 0 && published[0]) {
          setSelectedLangs([published[0].slug]);
          setPrimaryLang(published[0].slug);
        }
      })
      .finally(() => {
        if (isMounted) setIsLoading(false);
      });

    return () => {
      isMounted = false;
    };
  }, []);

  const toggleLanguage = (slug: string) => {
    if (selectedLangs.includes(slug)) {
      if (selectedLangs.length === 1) return; // Cannot deselect last language (FR-PL-4)
      const next = selectedLangs.filter((s) => s !== slug);
      setSelectedLangs(next);
      if (primaryLang === slug && next[0]) {
        setPrimaryLang(next[0]);
      }
    } else {
      setSelectedLangs([...selectedLangs, slug]);
    }
  };

  const handleFinish = () => {
    if (user && accessToken) {
      setAuth({ ...user, onboarded: true }, accessToken);
    }
    navigate('/dashboard', { replace: true });
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center p-12">
        <div className="w-8 h-8 border-2 border-accent border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  return (
    <div className="max-w-2xl mx-auto space-y-8 py-8 px-4">
      {/* Step Indicators */}
      <div className="flex items-center justify-between border-b border-border pb-4">
        <div className="text-sm font-semibold text-text">
          <span>DevType Onboarding — </span>
          <span className="text-accent font-mono">Step {step} of 3</span>
        </div>
        <div className="flex items-center gap-2">
          {[1, 2, 3].map((s) => (
            <div
              key={s}
              className={`w-2.5 h-2.5 rounded-full ${
                s === step ? 'bg-accent' : s < step ? 'bg-success' : 'bg-surface-2 border border-border'
              }`}
            />
          ))}
        </div>
      </div>

      {/* Step 1: Language Selection (Mandatory) */}
      {step === 1 && (
        <div className="space-y-6">
          <div className="space-y-1">
            <h1 className="text-2xl font-bold text-text">Choose Your Programming Languages</h1>
            <p className="text-sm text-text-muted">
              Select at least one language to begin. Real-world snippets and idiom drills will be curated for you.
            </p>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            {languages.map((lang) => {
              const isSelected = selectedLangs.includes(lang.slug);
              const isPrimary = primaryLang === lang.slug;
              return (
                <div
                  key={lang.slug}
                  onClick={() => toggleLanguage(lang.slug)}
                  className={`p-4 rounded-xl border cursor-pointer transition-all ${
                    isSelected ? 'bg-surface border-accent shadow-sm' : 'bg-surface-2 border-border opacity-70'
                  }`}
                >
                  <div className="flex items-center justify-between">
                    <span className="font-bold text-text">{lang.name}</span>
                    <input
                      type="checkbox"
                      checked={isSelected}
                      readOnly
                      className="accent-accent w-4 h-4 rounded cursor-pointer"
                    />
                  </div>
                  {isSelected && (
                    <div className="mt-3 pt-2 border-t border-border flex items-center justify-between text-xs">
                      <span className="text-text-muted">Role:</span>
                      <button
                        type="button"
                        onClick={(e) => {
                          e.stopPropagation();
                          setPrimaryLang(lang.slug);
                        }}
                        className={`px-2 py-0.5 rounded font-medium ${
                          isPrimary
                            ? 'bg-accent text-accent-fg'
                            : 'bg-surface-2 border border-border text-text-muted hover:text-text'
                        }`}
                      >
                        {isPrimary ? 'Primary Language' : 'Set as Primary'}
                      </button>
                    </div>
                  )}
                </div>
              );
            })}
          </div>

          <div className="flex justify-end pt-4">
            <button
              type="button"
              disabled={selectedLangs.length === 0}
              onClick={() => setStep(2)}
              className="py-2.5 px-6 bg-accent hover:opacity-90 active:scale-[0.99] text-accent-fg font-medium rounded-lg text-sm transition-all disabled:opacity-50"
            >
              Continue to Preferences
            </button>
          </div>
        </div>
      )}

      {/* Step 2: Experience / Keyboard (Optional) */}
      {step === 2 && (
        <div className="space-y-6">
          <div className="space-y-1">
            <h1 className="text-2xl font-bold text-text">Your Development Experience</h1>
            <p className="text-sm text-text-muted">
              This helps calibrate your initial exercise recommendations and speed targets.
            </p>
          </div>

          <div className="space-y-3">
            {[
              { id: 'NEW', title: 'New to Coding (< 1 year)', desc: 'Focus on syntax fundamentals, brackets and symbols' },
              { id: 'MID', title: 'Mid-Level (1–3 years)', desc: 'Build steady keyboard rhythm and code idiom muscle memory' },
              { id: 'SENIOR', title: 'Senior Developer (3+ years)', desc: 'Rapid keyboard navigation, complex expressions, and speed sprints' },
            ].map((exp) => (
              <div
                key={exp.id}
                onClick={() => setExperience(exp.id as 'NEW' | 'MID' | 'SENIOR')}
                className={`p-4 rounded-xl border cursor-pointer transition-all ${
                  experience === exp.id ? 'bg-surface border-accent shadow-sm' : 'bg-surface-2 border-border opacity-70'
                }`}
              >
                <div className="font-bold text-text">{exp.title}</div>
                <div className="text-xs text-text-muted mt-1">{exp.desc}</div>
              </div>
            ))}
          </div>

          <div className="flex items-center justify-between pt-4">
            <button
              type="button"
              onClick={() => setStep(1)}
              className="py-2 px-4 text-sm font-medium text-text-muted hover:text-text"
            >
              Back
            </button>
            <div className="flex items-center gap-3">
              <button
                type="button"
                onClick={() => setStep(3)}
                className="py-2 px-4 text-sm font-medium text-text-muted hover:text-text"
              >
                Skip
              </button>
              <button
                type="button"
                onClick={() => setStep(3)}
                className="py-2.5 px-6 bg-accent hover:opacity-90 active:scale-[0.99] text-accent-fg font-medium rounded-lg text-sm transition-all"
              >
                Continue
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Step 3: Placement Test Prompt (Optional) */}
      {step === 3 && (
        <div className="space-y-6 text-center">
          <div className="space-y-2">
            <div className="mx-auto w-12 h-12 rounded-full bg-accent/10 text-accent flex items-center justify-center text-xl">
              🎯
            </div>
            <h1 className="text-2xl font-bold text-text">Placement Assessment</h1>
            <p className="text-sm text-text-muted max-w-md mx-auto leading-relaxed">
              Take a short 3-snippet typing evaluation in <strong className="text-text">{primaryLang.toUpperCase()}</strong> to unlock higher difficulty levels immediately, or start right away from Beginner.
            </p>
          </div>

          <div className="space-y-3 pt-4 max-w-sm mx-auto">
            <button
              type="button"
              onClick={() => {
                if (user && accessToken) {
                  setAuth({ ...user, onboarded: true }, accessToken);
                }
                navigate(`/practice?lang=${primaryLang}&mode=placement`, { replace: true });
              }}
              className="w-full py-2.5 px-4 bg-accent hover:opacity-90 active:scale-[0.99] text-accent-fg font-medium rounded-lg text-sm transition-all"
            >
              Start Placement Test (3 min)
            </button>
            <button
              type="button"
              onClick={handleFinish}
              className="w-full py-2.5 px-4 bg-surface-2 hover:bg-surface border border-border text-text font-medium rounded-lg text-sm transition-colors"
            >
              Start from Beginner (Skip)
            </button>
          </div>
        </div>
      )}
    </div>
  );
};
