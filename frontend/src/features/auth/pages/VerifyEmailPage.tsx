import React, { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { authApi } from '../api';
import { ProblemError } from '../../../shared/api/problem';

export const VerifyEmailPage: React.FC = () => {
  const { t } = useTranslation('auth');
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');

  const [state, setState] = useState<'VERIFYING' | 'SUCCESS' | 'EXPIRED' | 'USED' | 'ERROR'>('VERIFYING');
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    if (!token) {
      setState('ERROR');
      setErrorMessage('Verification token missing from URL.');
      return;
    }

    let isMounted = true;

    authApi
      .verifyEmail({ token })
      .then(() => {
        if (isMounted) setState('SUCCESS');
      })
      .catch((err) => {
        if (!isMounted) return;
        if (err instanceof ProblemError) {
          if (err.code === 'TOKEN_EXPIRED') {
            setState('EXPIRED');
          } else if (err.code === 'TOKEN_USED') {
            setState('USED');
          } else {
            setState('ERROR');
            setErrorMessage(err.message);
          }
        } else {
          setState('ERROR');
          setErrorMessage('Verification failed.');
        }
      });

    return () => {
      isMounted = false;
    };
  }, [token]);

  return (
    <div className="space-y-6 text-center">
      <div className="space-y-2">
        {state === 'VERIFYING' && (
          <>
            <div className="mx-auto w-10 h-10 border-2 border-accent border-t-transparent rounded-full animate-spin" />
            <h1 className="text-xl font-bold tracking-tight text-text">{t('verifyEmail.title')}</h1>
            <p className="text-sm text-text-muted">{t('verifyEmail.verifying')}</p>
          </>
        )}

        {state === 'SUCCESS' && (
          <>
            <div className="mx-auto w-12 h-12 rounded-full bg-success/10 text-success flex items-center justify-center text-xl">
              ✓
            </div>
            <h1 className="text-2xl font-bold tracking-tight text-text">{t('verifyEmail.successTitle')}</h1>
            <p className="text-sm text-text-muted leading-relaxed">{t('verifyEmail.successMessage')}</p>
            <div className="pt-4">
              <Link
                to="/dashboard"
                className="inline-block w-full py-2.5 px-4 bg-accent hover:opacity-90 active:scale-[0.99] text-accent-fg font-medium rounded-lg text-sm text-center transition-all"
              >
                {t('verifyEmail.goToDashboard')}
              </Link>
            </div>
          </>
        )}

        {state === 'EXPIRED' && (
          <>
            <div className="mx-auto w-12 h-12 rounded-full bg-warning/10 text-warning flex items-center justify-center text-xl">
              ⚠️
            </div>
            <h1 className="text-xl font-bold tracking-tight text-text">{t('verifyEmail.title')}</h1>
            <p className="text-sm text-text-muted leading-relaxed">{t('verifyEmail.errorExpired')}</p>
            <div className="pt-4">
              <Link
                to="/login"
                className="inline-block w-full py-2.5 px-4 bg-surface-2 hover:bg-surface border border-border text-text font-medium rounded-lg text-sm text-center transition-colors"
              >
                {t('verifyEmail.goToLogin')}
              </Link>
            </div>
          </>
        )}

        {(state === 'USED' || state === 'ERROR') && (
          <>
            <div className="mx-auto w-12 h-12 rounded-full bg-error/10 text-error flex items-center justify-center text-xl">
              ✕
            </div>
            <h1 className="text-xl font-bold tracking-tight text-text">{t('verifyEmail.title')}</h1>
            <p className="text-sm text-text-muted leading-relaxed">
              {state === 'USED' ? t('verifyEmail.errorUsed') : errorMessage}
            </p>
            <div className="pt-4">
              <Link
                to="/login"
                className="inline-block w-full py-2.5 px-4 bg-accent hover:opacity-90 active:scale-[0.99] text-accent-fg font-medium rounded-lg text-sm text-center transition-all"
              >
                {t('verifyEmail.goToLogin')}
              </Link>
            </div>
          </>
        )}
      </div>
    </div>
  );
};
