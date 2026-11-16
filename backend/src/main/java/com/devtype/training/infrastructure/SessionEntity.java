package com.devtype.training.infrastructure;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sessions", schema = "training")
@IdClass(SessionKey.class)
public class SessionEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Id
    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "guest_id")
    private UUID guestId;

    @Column(name = "language_id", nullable = false)
    private short languageId;

    @Column(name = "mode", nullable = false, length = 16)
    private String mode;

    @Column(name = "topic_id")
    private Integer topicId;

    @Column(name = "difficulty", length = 16)
    private String difficulty;

    @Column(name = "duration_limit_s")
    private Short durationLimitS;

    @Column(name = "accuracy_master", nullable = false)
    private boolean accuracyMaster;

    @Column(name = "is_placement", nullable = false)
    private boolean isPlacement;

    @Column(name = "challenge_id")
    private Long challengeId;

    @Column(name = "competition_id")
    private Long competitionId;

    @Column(name = "special_category", length = 16)
    private String specialCategory;

    @Column(name = "gen_seed")
    private Long genSeed;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    @Column(name = "input_device", nullable = false, length = 16)
    private String inputDevice;

    @Column(name = "strict_indent", nullable = false)
    private boolean strictIndent;

    @Column(name = "engine_version", nullable = false, length = 16)
    private String engineVersion;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @Column(name = "elapsed_ms")
    private Integer elapsedMs;

    @Column(name = "wpm", precision = 6, scale = 2)
    private BigDecimal wpm;

    @Column(name = "raw_wpm", precision = 6, scale = 2)
    private BigDecimal rawWpm;

    @Column(name = "cpm", precision = 7, scale = 2)
    private BigDecimal cpm;

    @Column(name = "accuracy", precision = 5, scale = 2)
    private BigDecimal accuracy;

    @Column(name = "consistency", precision = 5, scale = 2)
    private BigDecimal consistency;

    @Column(name = "correct_chars")
    private Integer correctChars;

    @Column(name = "uncorrected_errors")
    private Integer uncorrectedErrors;

    @Column(name = "correct_keys")
    private Integer correctKeys;

    @Column(name = "incorrect_keys")
    private Integer incorrectKeys;

    @Column(name = "total_typed")
    private Integer totalTyped;

    @Column(name = "backspace_count")
    private Integer backspaceCount;

    @Column(name = "special_correct")
    private Integer specialCorrect;

    @Column(name = "special_total")
    private Integer specialTotal;

    @Column(name = "completion_pct", precision = 5, scale = 2)
    private BigDecimal completionPct;

    @Column(name = "client_wpm", precision = 6, scale = 2)
    private BigDecimal clientWpm;

    @Column(name = "client_accuracy", precision = 5, scale = 2)
    private BigDecimal clientAccuracy;

    @Column(name = "xp_awarded", nullable = false)
    private int xpAwarded;

    @Column(name = "ranked", nullable = false)
    private boolean ranked;

    @Column(name = "ranked_reason", length = 32)
    private String rankedReason;

    @Column(name = "reject_reason", length = 32)
    private String rejectReason;

    @Column(name = "suspicion_score", nullable = false)
    private short suspicionScore;

    @Column(name = "suspicion_reasons", columnDefinition = "text[]")
    private String[] suspicionReasons = new String[0];

    @Column(name = "review_status", length = 16)
    private String reviewStatus;

    @Column(name = "reviewed_by")
    private Long reviewedBy;

    @Column(name = "result_json")
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    private String resultJson;

    public SessionEntity() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public UUID getGuestId() {
        return guestId;
    }

    public void setGuestId(UUID guestId) {
        this.guestId = guestId;
    }

    public short getLanguageId() {
        return languageId;
    }

    public void setLanguageId(short languageId) {
        this.languageId = languageId;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public Integer getTopicId() {
        return topicId;
    }

    public void setTopicId(Integer topicId) {
        this.topicId = topicId;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public Short getDurationLimitS() {
        return durationLimitS;
    }

    public void setDurationLimitS(Short durationLimitS) {
        this.durationLimitS = durationLimitS;
    }

    public boolean isAccuracyMaster() {
        return accuracyMaster;
    }

    public void setAccuracyMaster(boolean accuracyMaster) {
        this.accuracyMaster = accuracyMaster;
    }

    public boolean isPlacement() {
        return isPlacement;
    }

    public void setPlacement(boolean placement) {
        isPlacement = placement;
    }

    public Long getChallengeId() {
        return challengeId;
    }

    public void setChallengeId(Long challengeId) {
        this.challengeId = challengeId;
    }

    public Long getCompetitionId() {
        return competitionId;
    }

    public void setCompetitionId(Long competitionId) {
        this.competitionId = competitionId;
    }

    public String getSpecialCategory() {
        return specialCategory;
    }

    public void setSpecialCategory(String specialCategory) {
        this.specialCategory = specialCategory;
    }

    public Long getGenSeed() {
        return genSeed;
    }

    public void setGenSeed(Long genSeed) {
        this.genSeed = genSeed;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getInputDevice() {
        return inputDevice;
    }

    public void setInputDevice(String inputDevice) {
        this.inputDevice = inputDevice;
    }

    public boolean isStrictIndent() {
        return strictIndent;
    }

    public void setStrictIndent(boolean strictIndent) {
        this.strictIndent = strictIndent;
    }

    public String getEngineVersion() {
        return engineVersion;
    }

    public void setEngineVersion(String engineVersion) {
        this.engineVersion = engineVersion;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(Instant finishedAt) {
        this.finishedAt = finishedAt;
    }

    public Integer getElapsedMs() {
        return elapsedMs;
    }

    public void setElapsedMs(Integer elapsedMs) {
        this.elapsedMs = elapsedMs;
    }

    public BigDecimal getWpm() {
        return wpm;
    }

    public void setWpm(BigDecimal wpm) {
        this.wpm = wpm;
    }

    public BigDecimal getRawWpm() {
        return rawWpm;
    }

    public void setRawWpm(BigDecimal rawWpm) {
        this.rawWpm = rawWpm;
    }

    public BigDecimal getCpm() {
        return cpm;
    }

    public void setCpm(BigDecimal cpm) {
        this.cpm = cpm;
    }

    public BigDecimal getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(BigDecimal accuracy) {
        this.accuracy = accuracy;
    }

    public BigDecimal getConsistency() {
        return consistency;
    }

    public void setConsistency(BigDecimal consistency) {
        this.consistency = consistency;
    }

    public Integer getCorrectChars() {
        return correctChars;
    }

    public void setCorrectChars(Integer correctChars) {
        this.correctChars = correctChars;
    }

    public Integer getUncorrectedErrors() {
        return uncorrectedErrors;
    }

    public void setUncorrectedErrors(Integer uncorrectedErrors) {
        this.uncorrectedErrors = uncorrectedErrors;
    }

    public Integer getCorrectKeys() {
        return correctKeys;
    }

    public void setCorrectKeys(Integer correctKeys) {
        this.correctKeys = correctKeys;
    }

    public Integer getIncorrectKeys() {
        return incorrectKeys;
    }

    public void setIncorrectKeys(Integer incorrectKeys) {
        this.incorrectKeys = incorrectKeys;
    }

    public Integer getTotalTyped() {
        return totalTyped;
    }

    public void setTotalTyped(Integer totalTyped) {
        this.totalTyped = totalTyped;
    }

    public Integer getBackspaceCount() {
        return backspaceCount;
    }

    public void setBackspaceCount(Integer backspaceCount) {
        this.backspaceCount = backspaceCount;
    }

    public Integer getSpecialCorrect() {
        return specialCorrect;
    }

    public void setSpecialCorrect(Integer specialCorrect) {
        this.specialCorrect = specialCorrect;
    }

    public Integer getSpecialTotal() {
        return specialTotal;
    }

    public void setSpecialTotal(Integer specialTotal) {
        this.specialTotal = specialTotal;
    }

    public BigDecimal getCompletionPct() {
        return completionPct;
    }

    public void setCompletionPct(BigDecimal completionPct) {
        this.completionPct = completionPct;
    }

    public BigDecimal getClientWpm() {
        return clientWpm;
    }

    public void setClientWpm(BigDecimal clientWpm) {
        this.clientWpm = clientWpm;
    }

    public BigDecimal getClientAccuracy() {
        return clientAccuracy;
    }

    public void setClientAccuracy(BigDecimal clientAccuracy) {
        this.clientAccuracy = clientAccuracy;
    }

    public int getXpAwarded() {
        return xpAwarded;
    }

    public void setXpAwarded(int xpAwarded) {
        this.xpAwarded = xpAwarded;
    }

    public boolean isRanked() {
        return ranked;
    }

    public void setRanked(boolean ranked) {
        this.ranked = ranked;
    }

    public String getRankedReason() {
        return rankedReason;
    }

    public void setRankedReason(String rankedReason) {
        this.rankedReason = rankedReason;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public short getSuspicionScore() {
        return suspicionScore;
    }

    public void setSuspicionScore(short suspicionScore) {
        this.suspicionScore = suspicionScore;
    }

    public String[] getSuspicionReasons() {
        return suspicionReasons;
    }

    public void setSuspicionReasons(String[] suspicionReasons) {
        this.suspicionReasons = suspicionReasons;
    }

    public String getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(String reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    public Long getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(Long reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public String getResultJson() {
        return resultJson;
    }

    public void setResultJson(String resultJson) {
        this.resultJson = resultJson;
    }
}
