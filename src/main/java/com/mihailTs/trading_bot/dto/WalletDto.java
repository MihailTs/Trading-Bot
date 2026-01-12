package com.mihailTs.trading_bot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WalletDto {
    private String currency;
    private double total;
}
