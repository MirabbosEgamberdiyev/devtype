import React, { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { authApi } from '../api';
import { useAuthStore } from '../../../shared/stores/authStore';

export const OAuthCallbackPage: React.FC = () => {
  const { t } = useTranslation('auth');
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const setAuth = useAuthStore((state) => state.setAuth);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  useEffect(() => {
    const provider = searchParams.get('provider') || 'github';
    const email = searchParams.get('email');
    const uid = searchParams.get('uid') || searchParams.get('code');

    if (!email || !uid) {
      setErrorMsg('Missing OAuth callback parameters.');
      return;
    }

    let isMounted = true;
    authApi
      .oauthCallback(provider, {
        email,
        providerUid: uid,
        displayName: searchParams.get('displayName'),
      })
      .then((res) => {
        if (!isMounted) return;
        setAuth(res.user, res.accessToken);
        navigate('/dashboard', { replace: true });
      })
      .catch((err) => {
        if (!isMounted) return;
        setErrorMsg(err.message || 'OAuth authentication failed.');
      });

    return () => {
      isMounted = false;
    };
  }, [searchParams, setAuth, navigate]);

  return (
    <div className="space-y-4 text-center py-6">
      {errorMsg ? (
        <div className="space-y-3">
          <div className="mx-auto w-10 h-10 rounded-full bg-error/10 text-error flex items-center justify-center text-xl">
            ✕
          </div>
          <p className="text-sm text-error">{errorMsg}</p>
        </div>
      ) : (
        <div className="space-y-3">
          <div className="mx-auto w-8 h-8 border-2 border-accent border-t-transparent rounded-full animate-spin" />
          <p className="text-sm text-text-muted">{t('oauth.connecting')}</p>
        </div>
      )}
    </div>
  );
};
