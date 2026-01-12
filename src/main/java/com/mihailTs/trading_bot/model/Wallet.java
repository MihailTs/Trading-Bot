package com.mihailTs.trading_bot.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class Wallet {
    private String currency;
    private double total;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
