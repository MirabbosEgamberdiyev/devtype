package com.devtype.gamification.domain;

import java.util.Map;

public record XpBreakdown(
        double base,
        double modeCoef,
        double diffCoef,
        double accFactor,
        double repeatFactor,
        Map<String, Integer> bonuses,
        boolean capped
) {}
