import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';

import enCommon from './locales/en/common.json';
import uzlCommon from './locales/uzl/common.json';
import uzcCommon from './locales/uzc/common.json';
import ruCommon from './locales/ru/common.json';

export const SUPPORTED_LOCALES = ['uzl', 'uzc', 'ru', 'en'] as const;
export type SupportedLocale = (typeof SUPPORTED_LOCALES)[number];

export const LOCALE_LABELS: Record<SupportedLocale, string> = {
  uzl: 'Oʻzbekcha',
  uzc: 'Ўзбекча',
  ru: 'Русский',
  en: 'English',
};

const resources = {
  en: { common: enCommon },
  uzl: { common: uzlCommon },
  uzc: { common: uzcCommon },
  ru: { common: ruCommon },
};

const initialLocale = (typeof window !== 'undefined' && localStorage.getItem('dt_locale')) || 'en';

i18n
  .use(initReactI18next)
  .init({
    resources,
    lng: initialLocale,
    fallbackLng: {
      uzc: ['uzl', 'en'],
      uzl: ['en'],
      ru: ['en'],
      default: ['en'],
    },
    ns: ['common'],
    defaultNS: 'common',
    interpolation: {
      escapeValue: false,
    },
  });

export default i18n;
