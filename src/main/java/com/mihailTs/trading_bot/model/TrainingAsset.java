package com.mihailTs.trading_bot.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class TrainingAsset extends Asset{
    public TrainingAsset(int tokenId, double quantity, LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(tokenId, quantity, createdAt, updatedAt);
    }
}
