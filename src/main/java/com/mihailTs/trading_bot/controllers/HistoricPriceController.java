package com.mihailTs.trading_bot.controllers;

import com.mihailTs.trading_bot.model.LivePrice;
import com.mihailTs.trading_bot.service.LivePriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/historic")
@RequiredArgsConstructor
public class HistoricPriceController {

    private final LivePriceService priceService;

    @GetMapping("/{id}")
    public ResponseEntity<List<LivePrice>> getTokenHistory(
            @PathVariable String id,
            @RequestParam(defaultValue = "1") int days) {

        try {
            List<LivePrice> prices = priceService.getPriceHistory(id, days);
            if (prices.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(prices);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}