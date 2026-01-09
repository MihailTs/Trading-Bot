package com.mihailTs.trading_bot.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class LiveTransaction extends Transaction{

    public LiveTransaction(UUID id, double quantity, UUID priceId, String type, LocalDateTime createdAt) {
        super(id, quantity, priceId, type, createdAt);
    }
}
