package com.mihailTs.trading_bot.model;

import java.time.LocalDateTime;
import java.util.UUID;

public abstract class Asset {

    private UUID tokenId;
    private double quantity;
    private LocalDateTime updatedAt;

    public Asset(UUID tokenId, double quantity, LocalDateTime updatedAt) {
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

    public void setTokenId(UUID tokenId) {
        this.tokenId = tokenId;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

}
