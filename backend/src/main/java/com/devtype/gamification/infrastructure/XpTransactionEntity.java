package com.devtype.gamification.infrastructure;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(
        schema = "gamification",
        name = "xp_transactions",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "source_type", "source_id"})
        }
)
public class XpTransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "amount", nullable = false)
    private int amount;

    @Column(name = "source_type", nullable = false, length = 24)
    private String sourceType;

    @Column(name = "source_id", nullable = false, length = 80)
    private String sourceId;

    @Column(name = "details")
    @JdbcTypeCode(SqlTypes.JSON)
    private String details;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public XpTransactionEntity() {}

    public XpTransactionEntity(Long userId, int amount, String sourceType, String sourceId, String details) {
        this.userId = userId;
        this.amount = amount;
        this.sourceType = sourceType;
        this.sourceId = sourceId;
        this.details = details;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
