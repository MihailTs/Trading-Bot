package com.mihailTs.trading_bot.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class LiveAsset extends Asset{
    public LiveAsset(UUID tokenId, double quantity, LocalDateTime updatedAt) {
        super(tokenId, quantity, updatedAt);
    }
}
