package com.mihailTs.trading_bot.service;

import com.mihailTs.trading_bot.exception.ElementNotFoundException;
import com.mihailTs.trading_bot.model.LivePrice;
import com.mihailTs.trading_bot.model.TrainingPrice;
import com.mihailTs.trading_bot.model.Token;
import com.mihailTs.trading_bot.repository.TrainingPriceRepository;
import com.mihailTs.trading_bot.repository.TokenRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TrainingPriceService {

    private final TokenRepository tokenRepository;
    private final TrainingPriceRepository trainingPriceRepository;

    public TrainingPriceService(TrainingPriceRepository trainingPriceRepository, TokenRepository tokenRepository) {
        this.trainingPriceRepository = trainingPriceRepository;
        this.tokenRepository = tokenRepository;
    }

    public TrainingPrice getById(UUID id) {
        try {
            return trainingPriceRepository.findById(id);
        } catch (ElementNotFoundException e) {
            // TODO: better exception handling
            System.err.println("Price not found: " + e.getMessage());
            throw e;
        }
    }

    public void saveNewPrice(BigDecimal price, String tokenId, LocalDateTime timestamp) {
        try {
            Token token = tokenRepository.findById(tokenId);
            TrainingPrice newPrice = new TrainingPrice(UUID.randomUUID(), tokenId, price, timestamp);
            trainingPriceRepository.insert(newPrice);
        } catch (ElementNotFoundException e) {
            // TODO: better exception handling
            System.err.println("Token not found: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to save new price", e);
        }
    }

    public TrainingPrice getLatestPrice(String tokenId) {
        try {
            return trainingPriceRepository.getPriceByTokenId(tokenId);
        } catch (ElementNotFoundException e) {
            // TODO: better exception handling
            System.err.println("Token not found: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to save new price", e);
        }
    }

    public List<TrainingPrice> getPriceHistory(String tokenId, int days) {
        try {
            return trainingPriceRepository.getPriceHistoryForDays(tokenId, days);
        } catch (ElementNotFoundException e) {
            // TODO: better exception handling
            System.err.println("Token not found: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to save new price", e);
        }
    }

    public List<TrainingPrice> getPricesByTokenIdAndDate(String tokenId, LocalDate date) {
        try {
            return trainingPriceRepository.getPriceHistoryForDate(tokenId, date);
        } catch (ElementNotFoundException e) {
            // TODO: better exception handling
            System.err.println("Token not found: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to save new price", e);
        }
    }

    public List<TrainingPrice> getPricesBetween(LocalDateTime start, LocalDateTime end) {
        return trainingPriceRepository.findByTimestampBetween(start, end);
    }
}
