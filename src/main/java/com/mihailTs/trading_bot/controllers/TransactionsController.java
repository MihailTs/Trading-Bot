package com.mihailTs.trading_bot.controllers;

import com.mihailTs.trading_bot.dto.TokenWithPriceDto;
import com.mihailTs.trading_bot.model.LivePrice;
import com.mihailTs.trading_bot.service.LivePriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionsController {

//    private  tokenService;
    private final LivePriceService priceService;

//    @GetMapping
//    public ResponseEntity<List<TokenWithPriceDto>> getAllTokens() {
//        List<TokenWithPriceDto> tokens = tokenService.getAll().stream()
//                .map(token -> {
//                    LivePrice latestPrice = priceService.getLatestPrice(token.getId());
//                    return new TokenWithPriceDto(
//                            token.getId(),
//                            token.getName(),
//                            token.getTicker(),
//                            latestPrice != null ? latestPrice.getPrice() : BigDecimal.ZERO,
//                            latestPrice != null ? latestPrice.getCreatedAt() : LocalDateTime.MIN
//                    );
//                })
//                .collect(Collectors.toList());
//        return ResponseEntity.ok(tokens);
//    }

}
