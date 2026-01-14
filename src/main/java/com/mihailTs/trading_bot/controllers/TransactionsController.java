package com.mihailTs.trading_bot.controllers;

import com.mihailTs.trading_bot.dto.TokenWithPriceDto;
import com.mihailTs.trading_bot.dto.TransactionDto;
import com.mihailTs.trading_bot.model.LivePrice;
import com.mihailTs.trading_bot.model.Token;
import com.mihailTs.trading_bot.model.TrainingPrice;
import com.mihailTs.trading_bot.service.LivePriceService;
import com.mihailTs.trading_bot.service.LiveTransactionService;
import com.mihailTs.trading_bot.service.TokenService;
import com.mihailTs.trading_bot.service.TrainingPriceService;
import com.mihailTs.trading_bot.service.TrainingTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionsController {

    private final TokenService tokenService;
    private final LiveTransactionService liveTransactionService;
    private final LivePriceService livePriceService;
    private final TrainingTransactionService trainingTransactionService;
    private final TrainingPriceService trainingPriceService;

    @GetMapping("/live")
    public ResponseEntity<List<TransactionDto>> getLiveTtransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int pageSize) {
        List<TransactionDto> transactions = liveTransactionService.getPagedTransactions(page, pageSize).stream()
                .map(transaction -> {
                    LivePrice latestPrice = livePriceService.getById(transaction.getPriceId());
                    Token transactedToken = tokenService.getTokenById(latestPrice.getTokenId());
                    return new TransactionDto(
                            transaction.getId(),
                            transactedToken.getName(),
                            transactedToken.getTicker(),
                            transaction.getType(),
                            transaction.getQuantity(),
                            transaction.getCreatedAt()
                    );
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/training")
    public ResponseEntity<List<TransactionDto>> getTrainingTtransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int pageSize) {
        List<TransactionDto> transactions = trainingTransactionService.getPagedTransactions(page, pageSize).stream()
                .map(transaction -> {
                    TrainingPrice latestPrice = trainingPriceService.getById(transaction.getPriceId());
                    Token transactedToken = tokenService.getTokenById(latestPrice.getTokenId());
                    return new TransactionDto(
                            transaction.getId(),
                            transactedToken.getName(),
                            transactedToken.getTicker(),
                            transaction.getType(),
                            transaction.getQuantity(),
                            transaction.getCreatedAt()
                    );
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(transactions);
    }

}
