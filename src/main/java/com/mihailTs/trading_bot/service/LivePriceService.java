package com.mihailTs.trading_bot.service;

import com.mihailTs.trading_bot.exception.ElementNotFoundException;
import com.mihailTs.trading_bot.model.LivePrice;
import com.mihailTs.trading_bot.model.Token;
import com.mihailTs.trading_bot.repository.LivePriceRepository;
import com.mihailTs.trading_bot.repository.TokenRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class LivePriceService {

    private final TokenRepository tokenRepository;
    private final LivePriceRepository livePriceRepository;

    public LivePriceService(LivePriceRepository livePriceRepository, TokenRepository tokenRepository) {
        this.livePriceRepository = livePriceRepository;
        this.tokenRepository = tokenRepository;
    }

    public void saveNewPrice(BigDecimal price, String tokenId, LocalDateTime timestamp) {
        try {
            Token token = tokenRepository.findById(tokenId);
            LivePrice newPrice = new LivePrice(UUID.randomUUID(), tokenId, price, timestamp);
            livePriceRepository.insert(newPrice);
        } catch (ElementNotFoundException e) {
            // TODO: better exception handling
            System.err.println("Token not found: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to save new price", e);
        }
    }

    public LivePrice getLatestPrice(String tokenId) {
        try {
            return livePriceRepository.getPriceByTokenId(tokenId);
        } catch (ElementNotFoundException e) {
            // TODO: better exception handling
            System.err.println("Token not found: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to save new price", e);
        }
    }

    public List<LivePrice> getPriceHistory(String tokenId, int days) {
        try {
            return livePriceRepository.getPriceHistoryForDays(tokenId, days);
        } catch (ElementNotFoundException e) {
            // TODO: better exception handling
            System.err.println("Token not found: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to save new price", e);
        }
    }

}
