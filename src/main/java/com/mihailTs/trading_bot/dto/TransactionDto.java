package com.mihailTs.trading_bot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class TransactionDto {
    private UUID id;
    private String tokenName;
    private String tokenTicker;
    private String type;
    private BigDecimal quantity;
    private LocalDateTime timestamp;
}
