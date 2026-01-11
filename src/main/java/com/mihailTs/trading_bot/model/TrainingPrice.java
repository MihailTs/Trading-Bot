package com.mihailTs.trading_bot.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class TrainingPrice extends Price{
    public TrainingPrice(UUID id, int tokenId, BigDecimal price, LocalDateTime createdAt) {
        super(id, tokenId, price, createdAt);
    }
}
