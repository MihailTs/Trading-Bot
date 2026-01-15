package com.mihailTs.trading_bot.service;

import com.mihailTs.trading_bot.dto.AssetValueDto;
import com.mihailTs.trading_bot.exception.ElementNotFoundException;
import com.mihailTs.trading_bot.model.LiveAsset;
import com.mihailTs.trading_bot.model.LivePrice;
import com.mihailTs.trading_bot.model.Token;
import com.mihailTs.trading_bot.repository.LiveAssetRepository;
import com.mihailTs.trading_bot.repository.LivePriceRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class LiveAssetService {

    private final LiveAssetRepository liveAssetRepository;
    private final LivePriceService livePriceService;
    private final TokenService tokenService;

    public LiveAssetService(LiveAssetRepository liveAssetRepository,
                            LivePriceService livePriceService,
                            TokenService tokenService) {
        this.liveAssetRepository = liveAssetRepository;
        this.livePriceService = livePriceService;
        this.tokenService = tokenService;
    }

    public List<LiveAsset> getAllAssets() {
        try {
            return liveAssetRepository.findAll();
        } catch (ElementNotFoundException e) {
            return new ArrayList<>();
        }
    }

    public LiveAsset getAssetByTokenId(String tokenId) {
        try {
            return liveAssetRepository.findByTokenId(tokenId);
        } catch (ElementNotFoundException e) {
            return null;
        }
    }

    public List<AssetValueDto> getAssetsWithLatestPrice() {
        try {
            List<Token> tokens = tokenService.getAll();
            List<AssetValueDto> assetValues = new ArrayList<>();

            for(Token token : tokens) {
                LivePrice livePrice = livePriceService.getLatestPrice(token.getId());
                LiveAsset liveAsset = getAssetByTokenId(token.getId());
                if(livePrice == null || liveAsset == null) {
                    continue;
                }
                assetValues.add(new AssetValueDto(
                        token.getId(),
                        token.getName(),
                        token.getTicker(),
                        liveAsset.getQuantity(),
                        livePrice.getPrice()));
            }
            return assetValues;
        } catch (ElementNotFoundException e) {
            return new ArrayList<>();
        }
    }

    public AssetValueDto getAssetWIthLatestPrice(String tokenId) {
        try {
            Token token = tokenService.getTokenById(tokenId);
            if(token == null) {
                return null;
            }

            LivePrice livePrice = livePriceService.getLatestPrice(token.getId());
            LiveAsset liveAsset = getAssetByTokenId(token.getId());
            if(liveAsset == null) {
                return null;
            }

            return new AssetValueDto(
                    token.getId(),
                    token.getName(),
                    token.getTicker(),
                    liveAsset.getQuantity(),
                    livePrice.getPrice());

        } catch (ElementNotFoundException e) {
            return null;
        }
    }

    public void updateAssetQuantity(String tokenId, BigDecimal quantity) {
        try {
            liveAssetRepository.updateQuantity(tokenId, quantity);
        } catch (ElementNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void addAsset(String tokenId) {
        try {
            liveAssetRepository.insert(new LiveAsset(tokenId, BigDecimal.valueOf(0), LocalDateTime.now(), LocalDateTime.now()));
        } catch (ElementNotFoundException e) {
            e.printStackTrace();
        }
    }

}
