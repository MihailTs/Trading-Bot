package com.mihailTs.trading_bot.service;

import com.mihailTs.trading_bot.exception.ElementNotFoundException;
import com.mihailTs.trading_bot.exception.IllegalTransactionPriceException;
import com.mihailTs.trading_bot.exception.IllegalTransactionTimeException;
import com.mihailTs.trading_bot.exception.NotEnoughAssetException;
import com.mihailTs.trading_bot.exception.NotEnoughMoneyException;
import com.mihailTs.trading_bot.model.TrainingAsset;
import com.mihailTs.trading_bot.model.TrainingPrice;
import com.mihailTs.trading_bot.model.TrainingTransaction;
import com.mihailTs.trading_bot.model.Token;
import com.mihailTs.trading_bot.model.Wallet;
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
    public final TrainingAssetService trainingAssetService;

    public final TrainingWalletService trainingWalletService;

    public TrainingTransactionService(TrainingTransactionRepository trainingTransactionRepository,
                                      TokenService tokenService,
                                      TrainingPriceService trainingPriceService,
                                      TrainingWalletService trainingWalletService,
                                      TrainingAssetService trainingAssetService) {
        this.tokenService = tokenService;
        this.trainingTransactionRepository = trainingTransactionRepository;
        this.trainingPriceService = trainingPriceService;
        this.trainingWalletService = trainingWalletService;
        this.trainingAssetService = trainingAssetService;
    }

    public void saveNewTransaction(UUID id,
                                   String tokenId,
                                   BigDecimal quantity,
                                   UUID priceId,
                                   String type,
                                   LocalDateTime createdAt) {
        try {
            Token token = tokenService.getTokenById(tokenId);
            TrainingPrice savePrice = trainingPriceService.getById(priceId);
            TrainingPrice latestPrice = trainingPriceService.getLatestPrice(tokenId);
            TrainingAsset asset = trainingAssetService.getAssetByTokenId(tokenId);
            if(savePrice.getId() != latestPrice.getId()) {
                throw new IllegalTransactionPriceException("A transaction cannot be registered with earlier price");
            }
            if(savePrice.getId() != latestPrice.getId()) {
                throw new IllegalTransactionTimeException("A transaction cannot be registered with earlier timestamp");
            }
            if(type.equals("BUY")) {
                Wallet wallet = trainingWalletService.getWalletByCurrency("USD");
                if(quantity.multiply(latestPrice.getPrice()).compareTo(wallet.getTotal()) > 0) {
                    throw new NotEnoughMoneyException("Value of bought crypto exceeds wallet");
                }
                trainingWalletService.addMoneyToWallet("USD", quantity.multiply(latestPrice.getPrice()).multiply(BigDecimal.valueOf(-1)));
            } else {
                Wallet wallet = trainingWalletService.getWalletByCurrency("USD");
                if(asset.getQuantity().compareTo(quantity) < 0) {
                    throw new NotEnoughAssetException("Transacted quantity is bigger than available asset");
                }
                trainingWalletService.addMoneyToWallet("USD", quantity.multiply(latestPrice.getPrice()));
            }
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

    public List<TrainingTransaction> getPagedTransactions(int page, int pageSize) {
        try {
            return trainingTransactionRepository.findPageByDate(page, pageSize);
        } catch (ElementNotFoundException e) {
            // TODO: better exception handling
            System.err.println("Token not found: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to save new price", e);
        }
    }
}
