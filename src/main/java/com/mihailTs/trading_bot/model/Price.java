package com.mihailTs.trading_bot.model;

import java.time.LocalDateTime;
import java.util.UUID;

public abstract class Price {
    private UUID id;
    private UUID tokenId;
    private double price;
    private LocalDateTime createdAt;

    public Price(UUID id, UUID tokenId, double price, LocalDateTime createdAt) {
        setId(id);
        setTokenId(tokenId);
        setPrice(price);
        setCreatedAt(createdAt);
    }

    public UUID getId() {
        return id;
    }

    public UUID getTokenId() {
        return tokenId;
    }

    public double getPrice() {
        return price;
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

    public void setPrice(double price) {
        this.price = price;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

}
