package com.mihailTs.trading_bot.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class LivePrice extends Price{
    public LivePrice(UUID id, String tokenId, BigDecimal price, LocalDateTime createdAt) {
        super(id, tokenId, price, createdAt);
    }
}
