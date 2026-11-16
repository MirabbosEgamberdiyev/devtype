package com.devtype.coach.api;

import java.time.Instant;

public record GoalAchieved(
    Long userId,
    String goalType,
    double targetValue,
    Instant timestamp
) {}
