package com.devtype.gamification.infrastructure;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(schema = "gamification", name = "user_progress")
public class UserProgressEntity {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "total_xp", nullable = false)
    private long totalXp = 0L;

    @Column(name = "level", nullable = false)
    private int level = 1;

    @Column(name = "current_streak", nullable = false)
    private int currentStreak = 0;

    @Column(name = "longest_streak", nullable = false)
    private int longestStreak = 0;

    @Column(name = "streak_start_day")
    private LocalDate streakStartDay;

    @Column(name = "last_active_day")
    private LocalDate lastActiveDay;

    @Column(name = "freezes_left", nullable = false)
    private short freezesLeft = 2;

    @Column(name = "xp_today", nullable = false)
    private int xpToday = 0;

    @Column(name = "xp_today_day")
    private LocalDate xpTodayDay;

    @Version
    @Column(name = "version", nullable = false)
    private int version = 0;

    public UserProgressEntity() {}

    public UserProgressEntity(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public long getTotalXp() {
        return totalXp;
    }

    public void setTotalXp(long totalXp) {
        this.totalXp = totalXp;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
    }

    public int getLongestStreak() {
        return longestStreak;
    }

    public void setLongestStreak(int longestStreak) {
        this.longestStreak = longestStreak;
    }

    public LocalDate getStreakStartDay() {
        return streakStartDay;
    }

    public void setStreakStartDay(LocalDate streakStartDay) {
        this.streakStartDay = streakStartDay;
    }

    public LocalDate getLastActiveDay() {
        return lastActiveDay;
    }

    public void setLastActiveDay(LocalDate lastActiveDay) {
        this.lastActiveDay = lastActiveDay;
    }

    public short getFreezesLeft() {
        return freezesLeft;
    }

    public void setFreezesLeft(short freezesLeft) {
        this.freezesLeft = freezesLeft;
    }

    public int getXpToday() {
        return xpToday;
    }

    public void setXpToday(int xpToday) {
        this.xpToday = xpToday;
    }

    public LocalDate getXpTodayDay() {
        return xpTodayDay;
    }

    public void setXpTodayDay(LocalDate xpTodayDay) {
        this.xpTodayDay = xpTodayDay;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
