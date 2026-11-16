package com.devtype.coach.api;

import java.util.List;

public record CoachPlanView(
    Long userId,
    List<TaskItem> tasks,
    int completedCount,
    boolean allCompleted
) {
    public record TaskItem(
        String taskId,
        String description,
        boolean completed
    ) {}
}
