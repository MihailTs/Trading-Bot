package com.mihailTs.trading_bot.controllers;

import com.mihailTs.trading_bot.dto.TokenWithPriceDto;
import com.mihailTs.trading_bot.dto.WalletDto;
import com.mihailTs.trading_bot.model.LivePrice;
import com.mihailTs.trading_bot.service.LivePriceService;
import com.mihailTs.trading_bot.service.TokenService;
import com.mihailTs.trading_bot.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final LivePriceService priceService;

    @GetMapping
    public ResponseEntity<List<WalletDto>> getAllWallets() {
        List<WalletDto> wallets = walletService.getAll().stream()
                .map(wallet -> {
                    return new WalletDto(
                            wallet.getCurrency(),
                            wallet.getTotal()
                    );
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(wallets);
    }

}
