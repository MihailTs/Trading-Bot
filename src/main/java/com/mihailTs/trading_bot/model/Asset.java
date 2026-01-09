package com.mihailTs.trading_bot.model;

import java.time.LocalDateTime;
import java.util.UUID;

public abstract class Asset {

    private UUID tokenId;
    private double quantity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Asset(UUID tokenId, double quantity, LocalDateTime createdAt, LocalDateTime updatedAt) {
        setTokenId(tokenId);
        setQuantity(quantity);
        setUpdatedAt(updatedAt);
    }

    public UUID getTokenId() {
        return tokenId;
    }

    public double getQuantity() {
        return quantity;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setTokenId(UUID tokenId) {
        this.tokenId = tokenId;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

}
