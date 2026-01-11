package com.mihailTs.trading_bot.dto;

import jdk.jfr.DataAmount;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class TokenWithPriceDto {
    private String id;
    private String name;
    private String ticker;
    private BigDecimal currentPrice;
    private LocalDateTime lastUpdated;
}
