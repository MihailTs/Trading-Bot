package com.mihailTs.trading_bot.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class TrainingTransaction extends Transaction{
    public TrainingTransaction(UUID id, double quantity, UUID priceId, String type, LocalDateTime createdAt) {
        super(id, quantity, priceId, type, createdAt);
    }
}
