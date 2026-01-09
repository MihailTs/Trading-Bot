package com.mihailTs.trading_bot.model;

import java.time.LocalDateTime;
import java.util.UUID;

public abstract class Transaction {
    private UUID id;
    private UUID priceId;
    private String type;
    private LocalDateTime createdAt;
    private double quantity;

    public Transaction(UUID id, double quantity, UUID priceId, String type, LocalDateTime createdAt) {
        setId(id);
        setQuantity(quantity);
        setPriceId(priceId);
        setType(type);
        setCreatedAt(createdAt);
    }

    public UUID getId() {
        return id;
    }

    public UUID getPriceId() {
        return priceId;
    }

    public String getType() {
        return type;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setPriceId(UUID priceId) {
        this.priceId = priceId;
    }

    public void setType(String type) {
        this.type = type;
    }

    private void setQuantity(double quantity) {
        this.quantity = quantity;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
