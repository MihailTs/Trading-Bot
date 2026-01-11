package com.mihailTs.trading_bot.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class Token {
    private int id;
    private String name;
    private String ticker;
    public BigDecimal circulatingSupply;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
}
