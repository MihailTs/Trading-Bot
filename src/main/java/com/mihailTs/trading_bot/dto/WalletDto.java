package com.mihailTs.trading_bot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class WalletDto {
    private String currency;
    private BigDecimal total;
}
