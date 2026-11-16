package com.devtype.shared.i18n;

import com.devtype.shared.Constants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;

@Configuration
public class LocaleResolverConfig {

    @Bean
    public LocaleResolver localeResolver() {
        return new CustomLocaleResolver();
    }

    private static class CustomLocaleResolver implements LocaleResolver {

        @Override
        public Locale resolveLocale(HttpServletRequest request) {
            String acceptLanguage = request.getHeader("Accept-Language");
            if (acceptLanguage != null && !acceptLanguage.isBlank()) {
                String normalized = acceptLanguage.toLowerCase();
                if (normalized.startsWith("uz-cyrl") || normalized.startsWith("uzc")) {
                    return Locale.forLanguageTag("uz-Cyrl");
                }
                if (normalized.startsWith("uz") || normalized.startsWith("uzl")) {
                    return Locale.forLanguageTag("uz-Latn");
                }
                if (normalized.startsWith("ru")) {
                    return Locale.forLanguageTag("ru");
                }
            }
            return Locale.ENGLISH;
        }

        @Override
        public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
            // Holatsiz API — header asosida ishlaydi
        }
    }
}
