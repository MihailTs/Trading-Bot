package com.mihailTs.trading_bot.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class LiveTransaction extends Transaction{
    public LiveTransaction(UUID id, double quantity, UUID priceId, String type, LocalDateTime createdAt) {
        super(id, quantity, priceId, type, createdAt);
    }
}
