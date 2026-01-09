package com.mihailTs.trading_bot.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Token {
    private UUID id;
    private String name;
    private String ticker;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;

    public Token(UUID id, String name, String ticker, LocalDateTime createdAt, LocalDateTime updatedAt) {
        setId(id);
        setName(name);
        setTicker(ticker);
        setCreatedAt(createdAt);
        setUpdatedAt(updatedAt);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTicker() {
        return ticker;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

}
