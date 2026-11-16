import React, { useEffect } from 'react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import '../shared/i18n';
import { authApi } from '../features/auth/api';
import { useAuthStore } from '../shared/stores/authStore';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      refetchOnWindowFocus: false,
    },
  },
});

export const Providers: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { setAuth, setGuestToken, setLoading } = useAuthStore();

  useEffect(() => {
    let isMounted = true;

    // Try restoring authenticated session via refresh cookie
    authApi
      .refresh()
      .then((res) => {
        if (!isMounted) return;
        setAuth(res.user, res.accessToken);
      })
      .catch(() => {
        if (!isMounted) return;
        // Not logged in; ensure guest token exists if not authenticated
        const currentGuestToken = useAuthStore.getState().guestToken;
        if (!currentGuestToken) {
          authApi
            .getGuestToken()
            .then((guestRes) => {
              if (isMounted) setGuestToken(guestRes.guestToken);
            })
            .catch(() => {
              // Silently ignore guest token fetch error on initial mount
            })
            .finally(() => {
              if (isMounted) setLoading(false);
            });
        } else {
          setLoading(false);
        }
      });

    return () => {
      isMounted = false;
    };
  }, [setAuth, setGuestToken, setLoading]);

  return (
    <QueryClientProvider client={queryClient}>
      {children}
    </QueryClientProvider>
  );
};
