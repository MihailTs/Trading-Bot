package com.mihailTs.trading_bot.service;

import com.mihailTs.trading_bot.repository.LiveAssetRepository;
import com.mihailTs.trading_bot.repository.LivePriceRepository;
import org.springframework.stereotype.Service;

@Service
public class LiveAssetService {

    private LiveAssetRepository liveAssetRepository;

    public LiveAssetService(LiveAssetRepository liveAssetRepository) {
        this.liveAssetRepository = liveAssetRepository;
    }


}
