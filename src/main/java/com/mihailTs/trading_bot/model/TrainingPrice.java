package com.mihailTs.trading_bot.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class TrainingPrice extends Price{
    public TrainingPrice(UUID id, int tokenId, double price, LocalDateTime createdAt) {
        super(id, tokenId, price, createdAt);
    }
}
