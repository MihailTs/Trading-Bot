package com.mihailTs.trading_bot.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class TrainingPrice extends Price{
    public TrainingPrice(UUID id, int tokenId, BigDecimal price, LocalDateTime createdAt) {
        super(id, tokenId, price, createdAt);
    }
}
