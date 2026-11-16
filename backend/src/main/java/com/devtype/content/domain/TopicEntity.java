package com.devtype.content.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(name = "topics", schema = "content", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"language_id", "slug"})
})
public class TopicEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "language_id", nullable = false)
    private Short languageId;

    @Column(nullable = false, length = 64)
    private String slug;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "name_i18n", nullable = false, columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String nameI18n = "{}";

    @Column(name = "description_i18n", nullable = false, columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String descriptionI18n = "{}";

    @Column(name = "sort_order", nullable = false)
    private short sortOrder;

    @Column(nullable = false)
    private short release = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TopicStatus status = TopicStatus.DRAFT;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    protected TopicEntity() {
    }

    public TopicEntity(Short languageId, String slug, String name, String description, short sortOrder) {
        this.languageId = languageId;
        this.slug = slug.toLowerCase().trim();
        this.name = name;
        this.description = description;
        this.sortOrder = sortOrder;
        this.status = TopicStatus.DRAFT;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Integer getId() {
        return id;
    }

    public Short getLanguageId() {
        return languageId;
    }

    public String getSlug() {
        return slug;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNameI18n() {
        return nameI18n;
    }

    public void setNameI18n(String nameI18n) {
        this.nameI18n = nameI18n;
    }

    public String getDescriptionI18n() {
        return descriptionI18n;
    }

    public void setDescriptionI18n(String descriptionI18n) {
        this.descriptionI18n = descriptionI18n;
    }

    public short getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(short sortOrder) {
        this.sortOrder = sortOrder;
    }

    public short getRelease() {
        return release;
    }

    public void setRelease(short release) {
        this.release = release;
    }

    public TopicStatus getStatus() {
        return status;
    }

    public void setStatus(TopicStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
