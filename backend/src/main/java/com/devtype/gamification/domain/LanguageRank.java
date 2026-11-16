package com.devtype.gamification.domain;

public enum LanguageRank {
    UNRANKED(0.0),
    BRONZE(0.0),
    SILVER(30.0),
    GOLD(45.0),
    PLATINUM(60.0),
    DIAMOND(80.0);

    private final double minWpm;

    LanguageRank(double minWpm) {
        this.minWpm = minWpm;
    }

    public double getMinWpm() {
        return minWpm;
    }
}
