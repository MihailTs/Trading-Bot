package com.mihailTs.trading_bot.service;

import com.mihailTs.trading_bot.exception.ElementNotFoundException;
import com.mihailTs.trading_bot.exception.IllegalTransactionPriceException;
import com.mihailTs.trading_bot.exception.IllegalTransactionTimeException;
import com.mihailTs.trading_bot.exception.NotEnoughAssetException;
import com.mihailTs.trading_bot.exception.NotEnoughMoneyException;
import com.mihailTs.trading_bot.model.LiveAsset;
import com.mihailTs.trading_bot.model.LivePrice;
import com.mihailTs.trading_bot.model.LiveTransaction;
import com.mihailTs.trading_bot.model.Token;
import com.mihailTs.trading_bot.model.TrainingTransaction;
import com.mihailTs.trading_bot.model.Wallet;
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
    private final LivePriceService livePriceService;
    private final LiveAssetService liveAssetService;
    private final LiveWalletService liveWalletService;

    public LiveTransactionService(LiveTransactionRepository liveTransactionRepository,
                                  TokenService tokenService,
                                  LivePriceService livePriceService,
                                  LiveAssetService liveAssetService,
                                  LiveWalletService liveWalletService) {
        this.tokenService = tokenService;
        this.liveTransactionRepository = liveTransactionRepository;
        this.livePriceService = livePriceService;
        this.liveWalletService = liveWalletService;
        this.liveAssetService = liveAssetService;
    }

    public void saveNewTransaction(UUID id,
                                   String tokenId,
                                   BigDecimal quantity,
                                   UUID priceId,
                                   String type,
                                   LocalDateTime createdAt) {
        try {
            Token token = tokenService.getTokenById(tokenId);
            LivePrice savePrice = livePriceService.getById(priceId);
            LivePrice latestPrice = livePriceService.getLatestPrice(tokenId);
            LiveAsset asset = liveAssetService.getAssetByTokenId(tokenId);
//            if(savePrice.getId() != latestPrice.getId()) {
//                throw new IllegalTransactionPriceException("A transaction cannot be registered with earlier price");
//            }
            if(type.equals("BUY")) {
                Wallet wallet = liveWalletService.getWalletByCurrency("USD");
                System.out.println();
                System.out.println(quantity.multiply(savePrice.getPrice()) + "   " + wallet.getTotal());
                System.out.println();
                if(quantity.multiply(savePrice.getPrice()).compareTo(wallet.getTotal()) > 0) {
                    throw new NotEnoughMoneyException("Value of bought crypto exceeds wallet");
                }
                liveWalletService.addMoneyToWallet("USD", quantity.multiply(latestPrice.getPrice()).multiply(BigDecimal.valueOf(-1)));
            } else {
                Wallet wallet = liveWalletService.getWalletByCurrency("USD");
                if(asset.getQuantity().compareTo(quantity) < 0) {
                    throw new NotEnoughAssetException("Transacted quantity is bigger than available asset");
                }
                liveWalletService.addMoneyToWallet("USD", quantity.multiply(latestPrice.getPrice()));
            }
            liveTransactionRepository.insert(new LiveTransaction(id, quantity, priceId, type, createdAt));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to save new transaction", e);
        }
    }

    public List<LiveTransaction> getLastTransactions(int limit) {
        try {
            return liveTransactionRepository.findLast(limit);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch transactions", e);
        }
    }

    public LiveTransaction getById(UUID id) {
        try {
            return liveTransactionRepository.findById(id);
        } catch (ElementNotFoundException e) {
            // TODO: better exception handling
            System.err.println("Transaction not found: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to save new transaction", e);
        }
    }

    public List<LiveTransaction> getPagedTransactions(int page, int pageSize) {
        try {
            return liveTransactionRepository.findPageByDate(page, pageSize);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save new transaction", e);
        }
    }
}
