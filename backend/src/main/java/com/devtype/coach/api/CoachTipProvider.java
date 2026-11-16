package com.devtype.coach.api;

public interface CoachTipProvider {
    String getLatestTip(Long userId, String locale);
}
