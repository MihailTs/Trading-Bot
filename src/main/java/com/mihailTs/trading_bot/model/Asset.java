package com.mihailTs.trading_bot.model;

import java.time.LocalDateTime;
import java.util.UUID;

public abstract class Asset {

    private int tokenId;
    private double quantity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Asset(int tokenId, double quantity, LocalDateTime createdAt, LocalDateTime updatedAt) {
        setTokenId(tokenId);
        setQuantity(quantity);
        setUpdatedAt(updatedAt);
    }

    public int getTokenId() {
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

    public void setTokenId(int tokenId) {
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
