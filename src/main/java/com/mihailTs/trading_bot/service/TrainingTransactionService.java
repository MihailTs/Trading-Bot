package com.mihailTs.trading_bot.service;

import com.mihailTs.trading_bot.exception.ElementNotFoundException;
import com.mihailTs.trading_bot.model.LiveTransaction;
import com.mihailTs.trading_bot.model.TrainingPrice;
import com.mihailTs.trading_bot.model.TrainingTransaction;
import com.mihailTs.trading_bot.model.Token;
import com.mihailTs.trading_bot.repository.TrainingTransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TrainingTransactionService {

    private final TokenService tokenService;
    private final TrainingTransactionRepository trainingTransactionRepository;
    public final TrainingPriceService trainingPriceService;

    public TrainingTransactionService(TrainingTransactionRepository trainingTransactionRepository,
                                  TokenService tokenService,
                                  TrainingPriceService trainingPriceService) {
        this.tokenService = tokenService;
        this.trainingTransactionRepository = trainingTransactionRepository;
        this.trainingPriceService = trainingPriceService;
    }

    public void saveNewTransaction(UUID id,
                                   String tokenId,
                                   BigDecimal quantity,
                                   UUID priceId,
                                   String type,
                                   LocalDateTime createdAt) {
        try {
            Token token = tokenService.getTokenById(tokenId);
            TrainingPrice price = trainingPriceService.getById(priceId);
            trainingTransactionRepository.insert(new TrainingTransaction(id, quantity, priceId, type, createdAt));
        } catch (ElementNotFoundException e) {
            // TODO: better exception handling
            System.err.println("Token not found: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to save new price", e);
        }
    }

    public List<TrainingTransaction> getLastTransactions(int limit) {
        try {
            return trainingTransactionRepository.findLast(limit);
        } catch (ElementNotFoundException e) {
            // TODO: better exception handling
            System.err.println("Token not found: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to save new price", e);
        }
    }

}
