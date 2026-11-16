import { describe, it, expect, beforeEach } from 'vitest';
import { useAuthStore, UserView } from '../authStore';

describe('useAuthStore', () => {
  beforeEach(() => {
    localStorage.clear();
    useAuthStore.getState().logout();
  });

  it('initializes with unauthenticated state', () => {
    const state = useAuthStore.getState();
    expect(state.isAuthenticated).toBe(false);
    expect(state.user).toBeNull();
    expect(state.accessToken).toBeNull();
  });

  it('sets authentication data and marks user as authenticated', () => {
    const mockUser: UserView = {
      id: 1,
      email: 'user@example.com',
      username: 'devuser',
      displayName: 'Dev User',
      role: 'USER',
      emailVerified: true,
      avatarKey: null,
      locale: 'en',
      timezone: 'UTC',
      onboarded: true,
    };

    useAuthStore.getState().setAuth(mockUser, 'test-access-token');

    const state = useAuthStore.getState();
    expect(state.isAuthenticated).toBe(true);
    expect(state.user).toEqual(mockUser);
    expect(state.accessToken).toBe('test-access-token');
    expect(state.isLoading).toBe(false);
  });

  it('clears credentials on logout', () => {
    const mockUser: UserView = {
      id: 1,
      email: 'user@example.com',
      username: 'devuser',
      displayName: null,
      role: 'USER',
      emailVerified: false,
      avatarKey: null,
      locale: 'en',
      timezone: 'UTC',
      onboarded: false,
    };

    useAuthStore.getState().setAuth(mockUser, 'test-token');
    useAuthStore.getState().logout();

    const state = useAuthStore.getState();
    expect(state.isAuthenticated).toBe(false);
    expect(state.user).toBeNull();
    expect(state.accessToken).toBeNull();
  });

  it('persists and clears guest token in localStorage', () => {
    useAuthStore.getState().setGuestToken('gst_test_123');
    expect(localStorage.getItem('devtype_guest_token')).toBe('gst_test_123');
    expect(useAuthStore.getState().guestToken).toBe('gst_test_123');

    useAuthStore.getState().setGuestToken(null);
    expect(localStorage.getItem('devtype_guest_token')).toBeNull();
    expect(useAuthStore.getState().guestToken).toBeNull();
  });
});
