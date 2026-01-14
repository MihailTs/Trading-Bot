package com.mihailTs.trading_bot.controllers;

import com.mihailTs.trading_bot.model.LivePrice;
import com.mihailTs.trading_bot.model.TrainingPrice;
import com.mihailTs.trading_bot.service.LivePriceService;
import com.mihailTs.trading_bot.service.TrainingPriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/historic")
@RequiredArgsConstructor
public class HistoricPriceController {

    private final LivePriceService livePriceService;
    private final TrainingPriceService trainingPriceService;

    @GetMapping("/live/{id}")
    public ResponseEntity<List<LivePrice>> getLiveTokenHistory(
            @PathVariable String id,
            @RequestParam(defaultValue = "1") int days) {
        try {
            List<LivePrice> prices = livePriceService.getPriceHistory(id, days);
            if (prices.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(prices);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/training/{id}")
    public ResponseEntity<List<TrainingPrice>> getTrainingTokenHistory(
            @PathVariable String id,
            @RequestParam(defaultValue = "1") int days) {
        try {
            List<TrainingPrice> prices = trainingPriceService.getPriceHistory(id, days);
            if (prices.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(prices);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

}