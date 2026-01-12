package com.mihailTs.trading_bot.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
public class TrainingAsset extends Asset{
    public TrainingAsset(String tokenId, BigDecimal quantity, LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(tokenId, quantity, createdAt, updatedAt);
    }
}
