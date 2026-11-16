import { create } from 'zustand';

export interface UserView {
  id: number;
  email: string;
  username: string;
  displayName: string | null;
  role: 'USER' | 'EDITOR' | 'ADMIN';
  emailVerified: boolean;
  avatarKey: string | null;
  locale: string;
  timezone: string;
  onboarded: boolean;
}

interface AuthState {
  user: UserView | null;
  accessToken: string | null;
  guestToken: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  setAuth: (user: UserView, accessToken: string) => void;
  setAccessToken: (accessToken: string) => void;
  setGuestToken: (guestToken: string | null) => void;
  logout: () => void;
  setLoading: (isLoading: boolean) => void;
}

const GUEST_TOKEN_KEY = 'devtype_guest_token';

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  accessToken: null,
  guestToken: typeof window !== 'undefined' ? localStorage.getItem(GUEST_TOKEN_KEY) : null,
  isAuthenticated: false,
  isLoading: true,

  setAuth: (user: UserView, accessToken: string) => {
    set({
      user,
      accessToken,
      isAuthenticated: true,
      isLoading: false,
    });
  },

  setAccessToken: (accessToken: string) => {
    set({ accessToken });
  },

  setGuestToken: (guestToken: string | null) => {
    if (guestToken) {
      localStorage.setItem(GUEST_TOKEN_KEY, guestToken);
    } else {
      localStorage.removeItem(GUEST_TOKEN_KEY);
    }
    set({ guestToken });
  },

  logout: () => {
    set({
      user: null,
      accessToken: null,
      isAuthenticated: false,
      isLoading: false,
    });
  },

  setLoading: (isLoading: boolean) => {
    set({ isLoading });
  },
}));
