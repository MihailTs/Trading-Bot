package com.mihailTs.trading_bot.service;

import com.mihailTs.trading_bot.repository.LivePriceRepository;
import org.springframework.stereotype.Service;

@Service
public class LivePriceService {

    private LivePriceRepository livePriceRepository;

    public LivePriceService(LivePriceRepository livePriceRepository) {
        this.livePriceRepository = livePriceRepository;
    }

}
