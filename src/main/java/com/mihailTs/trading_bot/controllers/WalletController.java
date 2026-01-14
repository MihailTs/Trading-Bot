package com.mihailTs.trading_bot.controllers;

import com.mihailTs.trading_bot.dto.WalletDto;
import com.mihailTs.trading_bot.service.LivePriceService;
import com.mihailTs.trading_bot.service.LiveWalletService;
import com.mihailTs.trading_bot.service.TrainingPriceService;
import com.mihailTs.trading_bot.service.TrainingWalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final LiveWalletService liveWalletService;
    private final TrainingWalletService trainingWalletService;

    @GetMapping("/live")
    public ResponseEntity<List<WalletDto>> getAllLiveWallets() {
        List<WalletDto> wallets = liveWalletService.getAll().stream()
                .map(wallet -> {
                    return new WalletDto(
                            wallet.getCurrency(),
                            wallet.getTotal()
                    );
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(wallets);
    }

    @GetMapping("/training")
    public ResponseEntity<List<WalletDto>> getAllTrainingWallets() {
        List<WalletDto> wallets = trainingWalletService.getAll().stream()
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
