package com.mihailTs.trading_bot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class AssetValueDto {
    private String tokenId;
    private String name;
    private String ticker;
    private BigDecimal quantity;
    private BigDecimal price;
}
