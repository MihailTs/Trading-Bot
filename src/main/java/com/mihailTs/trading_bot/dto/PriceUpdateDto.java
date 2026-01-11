package com.mihailTs.trading_bot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class PriceUpdateDto {
    private Long tokenId;
    private Double price;
    private LocalDateTime timestamp;
}
