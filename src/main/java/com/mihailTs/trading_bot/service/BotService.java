package com.mihailTs.trading_bot.service;


import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class BotService {

    private TokenService tokenService;
    private LivePriceService livePriceService;
    private LiveAssetService liveAssetService;

    public BotService(TokenService tokenService,
                      LivePriceService livePriceService,
                      LiveAssetService liveAssetService) {
        this.tokenService = tokenService;
        this.livePriceService = livePriceService;
        this.liveAssetService = liveAssetService;
    }

    @Scheduled(fixedRate = 20000)
    @Order(1)
    public void fetchNewestData() {

    }


}
