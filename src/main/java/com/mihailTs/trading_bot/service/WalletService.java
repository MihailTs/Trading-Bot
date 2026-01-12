package com.mihailTs.trading_bot.service;

import com.mihailTs.trading_bot.exception.ElementNotFoundException;
import com.mihailTs.trading_bot.model.Wallet;
import com.mihailTs.trading_bot.repository.WalletRepository;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class WalletService {

    private final WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    public List<Wallet> getAll() {
        try {
            return walletRepository.findAll();
        } catch (ElementNotFoundException e) {
            return new ArrayList<>();
        }
    }

    public Wallet getWalletByCurrency(String currency) {
        try {
            return walletRepository.findByCurrency(currency);
        } catch (ElementNotFoundException e) {
            return null;
        }
    }

    public void saveWallet(String currency, double total, LocalDateTime created_at) {
        try {
            walletRepository.insert(new Wallet(currency, total, created_at, created_at));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addMoneyToWallet(String currency, double amount) {
        try {
            Wallet wallet = walletRepository.findByCurrency(currency);
            walletRepository.updateWallet(wallet, amount);
        } catch (ElementNotFoundException e) {
            // TODO: fix exception handling here
            throw e;
        }
    }


}
