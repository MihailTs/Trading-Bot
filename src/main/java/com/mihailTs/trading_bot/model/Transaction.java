package com.mihailTs.trading_bot.model;

import java.time.LocalDateTime;
import java.util.UUID;

public abstract class Transaction {
    private UUID id;
    private UUID tokenId;
    private UUID priceId;
    private String type;
    private LocalDateTime createdAt;

    public Transaction(UUID id, UUID tokenId, UUID priceId, String type, LocalDateTime createdAt) {
        setId(id);
        setTokenId(tokenId);
        setPriceId(priceId);
        setType(type);
        setCreatedAt(createdAt);
    }

    public UUID getId() {
        return id;
    }

    public UUID getTokenId() {
        return tokenId;
    }

    public UUID getPriceId() {
        return priceId;
    }

    private String getType() {
        return type;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setTokenId(UUID tokenId) {
        this.tokenId = tokenId;
    }

    public void setPriceId(UUID priceId) {
        this.priceId = priceId;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
