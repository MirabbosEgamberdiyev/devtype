import { ProblemDetail, ProblemError } from './problem';
import { useAuthStore } from '../stores/authStore';

const BASE_URL = '/api/v1';

let isRefreshing = false;
let refreshPromise: Promise<string | null> | null = null;

async function executeRefreshToken(): Promise<string | null> {
  try {
    const res = await fetch(`${BASE_URL}/auth/refresh`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include',
    });

    if (!res.ok) {
      useAuthStore.getState().logout();
      return null;
    }

    const data = await res.json();
    if (data.accessToken) {
      useAuthStore.getState().setAccessToken(data.accessToken);
      if (data.user) {
        useAuthStore.getState().setAuth(data.user, data.accessToken);
      }
      return data.accessToken as string;
    }
    useAuthStore.getState().logout();
    return null;
  } catch {
    useAuthStore.getState().logout();
    return null;
  }
}

export interface RequestOptions extends RequestInit {
  skipAuth?: boolean;
}

export async function apiClient<T>(endpoint: string, options: RequestOptions = {}): Promise<T> {
  const url = endpoint.startsWith('http') ? endpoint : `${BASE_URL}${endpoint.startsWith('/') ? endpoint : `/${endpoint}`}`;

  const { skipAuth, headers: customHeaders, ...restOptions } = options;
  const store = useAuthStore.getState();

  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
    'X-Engine-Version': '0.1.0',
    ...(customHeaders as Record<string, string>),
  };

  if (!skipAuth && store.accessToken) {
    headers['Authorization'] = `Bearer ${store.accessToken}`;
  }

  if (store.guestToken) {
    headers['X-Guest-Token'] = store.guestToken;
  }

  let res = await fetch(url, {
    ...restOptions,
    headers,
    credentials: 'include',
  });

  // Handle 401: Token refresh logic (docs/15 §5)
  const isAuthEndpoint = endpoint.includes('/auth/login') || endpoint.includes('/auth/refresh') || endpoint.includes('/auth/register');
  if (res.status === 401 && !skipAuth && !isAuthEndpoint) {
    if (!isRefreshing) {
      isRefreshing = true;
      refreshPromise = executeRefreshToken().finally(() => {
        isRefreshing = false;
        refreshPromise = null;
      });
    }

    const newAccessToken = await refreshPromise;
    if (newAccessToken) {
      headers['Authorization'] = `Bearer ${newAccessToken}`;
      res = await fetch(url, {
        ...restOptions,
        headers,
        credentials: 'include',
      });
    } else {
      throw new ProblemError({
        status: 401,
        code: 'UNAUTHORIZED',
        title: 'Session expired',
        detail: 'Your session has expired. Please log in again.',
      });
    }
  }

  if (res.status === 204) {
    return null as unknown as T;
  }

  const contentType = res.headers?.get ? res.headers.get('content-type') : null;
  const isJson = (contentType && contentType.includes('application/json')) || (!contentType && typeof res.json === 'function');

  if (!res.ok) {
    if (isJson) {
      const problem = (await res.json()) as ProblemDetail;
      throw new ProblemError(problem);
    }
    throw new ProblemError({
      status: res.status,
      code: 'HTTP_ERROR',
      title: res.statusText,
      detail: await res.text(),
    });
  }

  if (isJson) {
    return (await res.json()) as T;
  }

  return (await res.text()) as unknown as T;
}
