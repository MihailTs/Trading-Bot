package com.mihailTs.trading_bot.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class Transaction {
    private UUID id;
    private BigDecimal quantity;
    private UUID priceId;
    private String type;
    private LocalDateTime createdAt;
}
