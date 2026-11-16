package com.devtype.content.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(name = "languages", schema = "content")
public class LanguageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short id;

    @Column(nullable = false, unique = true, length = 32)
    private String slug;

    @Column(nullable = false, length = 64)
    private String name;

    @Column(name = "grammar_id", nullable = false, length = 32)
    private String grammarId;

    @Enumerated(EnumType.STRING)
    @Column(name = "indent_style", nullable = false, length = 8)
    private IndentStyle indentStyle = IndentStyle.SPACE;

    @Column(name = "indent_size", nullable = false)
    private short indentSize = 4;

    @Enumerated(EnumType.STRING)
    @Column(name = "keyword_case", nullable = false, length = 8)
    private KeywordCase keywordCase = KeywordCase.LOWER;

    @Column(name = "token_patterns", nullable = false, columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String tokenPatterns = "[]";

    @Column(name = "special_templates", nullable = false, columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String specialTemplates = "{}";

    @Column(name = "forbidden_patterns", nullable = false, columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String forbiddenPatterns = "[]";

    @Column(nullable = false)
    private short release = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private LanguageStatus status = LanguageStatus.DRAFT;

    @Column(name = "sort_order", nullable = false)
    private short sortOrder = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    protected LanguageEntity() {
    }

    public LanguageEntity(String slug, String name, String grammarId, IndentStyle indentStyle, short indentSize, KeywordCase keywordCase) {
        this.slug = slug.toLowerCase().trim();
        this.name = name;
        this.grammarId = grammarId;
        this.indentStyle = indentStyle;
        this.indentSize = indentSize;
        this.keywordCase = keywordCase;
        this.status = LanguageStatus.DRAFT;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Short getId() {
        return id;
    }

    public String getSlug() {
        return slug;
    }

    public String getName() {
        return name;
    }

    public String getGrammarId() {
        return grammarId;
    }

    public IndentStyle getIndentStyle() {
        return indentStyle;
    }

    public short getIndentSize() {
        return indentSize;
    }

    public KeywordCase getKeywordCase() {
        return keywordCase;
    }

    public String getTokenPatterns() {
        return tokenPatterns;
    }

    public void setTokenPatterns(String tokenPatterns) {
        this.tokenPatterns = tokenPatterns;
    }

    public String getSpecialTemplates() {
        return specialTemplates;
    }

    public void setSpecialTemplates(String specialTemplates) {
        this.specialTemplates = specialTemplates;
    }

    public String getForbiddenPatterns() {
        return forbiddenPatterns;
    }

    public void setForbiddenPatterns(String forbiddenPatterns) {
        this.forbiddenPatterns = forbiddenPatterns;
    }

    public short getRelease() {
        return release;
    }

    public void setRelease(short release) {
        this.release = release;
    }

    public LanguageStatus getStatus() {
        return status;
    }

    public void setStatus(LanguageStatus status) {
        this.status = status;
    }

    public short getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(short sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
