package com.mihailTs.trading_bot.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class LivePrice extends Price{
    public LivePrice(UUID id, UUID tokenId, double price, LocalDateTime createdAt) {
        super(id, tokenId, price, createdAt);
    }
}
