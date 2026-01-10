package com.mihailTs.trading_bot.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public abstract class Price {
    private UUID id;
    private int tokenId;
    private BigDecimal price;
    private LocalDateTime createdAt;

    public Price(UUID id, int tokenId, BigDecimal price, LocalDateTime createdAt) {
        setId(id);
        setTokenId(tokenId);
        setPrice(price);
        setCreatedAt(createdAt);
    }

    public UUID getId() {
        return id;
    }

    public int getTokenId() {
        return tokenId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setTokenId(int tokenId) {
        this.tokenId = tokenId;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

}
