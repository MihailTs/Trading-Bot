package com.mihailTs.trading_bot.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class LiveTransaction extends Transaction{
    public LiveTransaction(UUID id, UUID tokenId, UUID priceId, String type, LocalDateTime createdAt) {
        super(id, tokenId, priceId, type, createdAt);
    }
}
