package com.mihailTs.trading_bot.service;

import com.mihailTs.trading_bot.exception.ElementNotFoundException;
import com.mihailTs.trading_bot.model.LivePrice;
import com.mihailTs.trading_bot.model.LiveTransaction;
import com.mihailTs.trading_bot.model.Token;
import com.mihailTs.trading_bot.repository.LiveTransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class LiveTransactionService {

    private final TokenService tokenService;
    private final LiveTransactionRepository liveTransactionRepository;
    public final LivePriceService livePriceService;

    public LiveTransactionService(LiveTransactionRepository liveTransactionRepository,
                                  TokenService tokenService,
                                  LivePriceService livePriceService) {
        this.tokenService = tokenService;
        this.liveTransactionRepository = liveTransactionRepository;
        this.livePriceService = livePriceService;
    }

    public void saveNewTransaction(UUID id,
                                   String tokenId,
                                   BigDecimal quantity,
                                   UUID priceId,
                                   String type,
                                   LocalDateTime createdAt) {
        try {
            Token token = tokenService.getTokenById(tokenId);
            LivePrice price = livePriceService.getById(priceId);
            liveTransactionRepository.insert(new LiveTransaction(id, quantity, priceId, type, createdAt));
        } catch (ElementNotFoundException e) {
            // TODO: better exception handling
            System.err.println("Token not found: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to save new price", e);
        }
    }

    public List<LiveTransaction> getLastTransactions(int limit) {
        try {
            return liveTransactionRepository.findLast(limit);
        } catch (ElementNotFoundException e) {
            // TODO: better exception handling
            System.err.println("Token not found: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to save new price", e);
        }
    }

}
