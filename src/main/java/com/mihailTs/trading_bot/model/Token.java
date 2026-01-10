package com.mihailTs.trading_bot.model;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.UUID;

public class Token {
    private int id;
    private String name;
    private String ticker;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
    public BigDecimal circulatingSupply;

    public Token(int id, String name, String ticker, BigDecimal circulatingSupply, LocalDateTime createdAt, LocalDateTime updatedAt) {
        setId(id);
        setName(name);
        setTicker(ticker);
        setCreatedAt(createdAt);
        setUpdatedAt(updatedAt);
        setCirculatingSupply(circulatingSupply);
    }

    public int getId() {
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

    public BigDecimal getCirculatingSupply() {
        return circulatingSupply;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setId(int id) {
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

    public void setCirculatingSupply(BigDecimal circulatingSupply) {
        this.circulatingSupply = circulatingSupply;
    }

}
