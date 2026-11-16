package com.devtype.content.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.hibernate.type.descriptor.jdbc.CharJdbcType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "exercises", schema = "content", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"exercise_key", "version"}),
        @UniqueConstraint(columnNames = {"language_id", "code_hash"})
})
public class ExerciseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "exercise_key", nullable = false)
    private UUID exerciseKey;

    @Column(nullable = false)
    private int version = 1;

    @Column(name = "language_id", nullable = false)
    private Short languageId;

    @Column(name = "topic_id")
    private Integer topicId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private DifficultyLevel difficulty;

    @Enumerated(EnumType.STRING)
    @Column(name = "exercise_type", nullable = false, length = 16)
    private ExerciseType exerciseType = ExerciseType.SNIPPET;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 400)
    private String note;

    @Column(name = "note_i18n", nullable = false, columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String noteI18n = "{}";

    @Column(nullable = false, columnDefinition = "text")
    private String code;

    @Column(name = "code_hash", nullable = false, columnDefinition = "char(64)")
    @JdbcType(CharJdbcType.class)
    private String codeHash;

    @Column(name = "char_count", nullable = false)
    private int charCount;

    @Column(name = "line_count", nullable = false)
    private int lineCount;

    @Column(name = "special_ratio", nullable = false, precision = 4, scale = 3)
    private BigDecimal specialRatio = BigDecimal.ZERO;

    @Column(name = "nesting_depth", nullable = false)
    private short nestingDepth = 0;

    @Column(name = "char_freq", nullable = false, columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String charFreq = "{}";

    @Column(name = "bigram_freq", nullable = false, columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String bigramFreq = "{}";

    @Column(name = "expected_seconds", nullable = false)
    private int expectedSeconds = 30;

    @Column(name = "ranked_pool", nullable = false)
    private boolean rankedPool = false;

    @Column(nullable = false, length = 300)
    private String source = "original";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ExerciseStatus status = ExerciseStatus.DRAFT;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "reviewed_by")
    private Long reviewedBy;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    protected ExerciseEntity() {
    }

    public ExerciseEntity(
            UUID exerciseKey,
            int version,
            Short languageId,
            Integer topicId,
            DifficultyLevel difficulty,
            ExerciseType exerciseType,
            String title,
            String code,
            String codeHash,
            int charCount,
            int lineCount,
            BigDecimal specialRatio,
            short nestingDepth,
            Long createdBy
    ) {
        this.exerciseKey = exerciseKey != null ? exerciseKey : UUID.randomUUID();
        this.version = version;
        this.languageId = languageId;
        this.topicId = topicId;
        this.difficulty = difficulty;
        this.exerciseType = exerciseType;
        this.title = title;
        this.code = code;
        this.codeHash = codeHash;
        this.charCount = charCount;
        this.lineCount = lineCount;
        this.specialRatio = specialRatio;
        this.nestingDepth = nestingDepth;
        this.createdBy = createdBy;
        this.status = ExerciseStatus.DRAFT;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public UUID getExerciseKey() {
        return exerciseKey;
    }

    public int getVersion() {
        return version;
    }

    public Short getLanguageId() {
        return languageId;
    }

    public Integer getTopicId() {
        return topicId;
    }

    public DifficultyLevel getDifficulty() {
        return difficulty;
    }

    public ExerciseType getExerciseType() {
        return exerciseType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getNoteI18n() {
        return noteI18n;
    }

    public void setNoteI18n(String noteI18n) {
        this.noteI18n = noteI18n;
    }

    public String getCode() {
        return code;
    }

    public String getCodeHash() {
        return codeHash;
    }

    public int getCharCount() {
        return charCount;
    }

    public int getLineCount() {
        return lineCount;
    }

    public BigDecimal getSpecialRatio() {
        return specialRatio;
    }

    public short getNestingDepth() {
        return nestingDepth;
    }

    public String getCharFreq() {
        return charFreq;
    }

    public void setCharFreq(String charFreq) {
        this.charFreq = charFreq;
    }

    public String getBigramFreq() {
        return bigramFreq;
    }

    public void setBigramFreq(String bigramFreq) {
        this.bigramFreq = bigramFreq;
    }

    public int getExpectedSeconds() {
        return expectedSeconds;
    }

    public void setExpectedSeconds(int expectedSeconds) {
        this.expectedSeconds = expectedSeconds;
    }

    public boolean isRankedPool() {
        return rankedPool;
    }

    public void setRankedPool(boolean rankedPool) {
        this.rankedPool = rankedPool;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public ExerciseStatus getStatus() {
        return status;
    }

    public void setStatus(ExerciseStatus status) {
        this.status = status;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public Long getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(Long reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
