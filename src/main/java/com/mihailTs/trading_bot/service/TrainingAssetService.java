package com.mihailTs.trading_bot.service;

import com.mihailTs.trading_bot.dto.AssetValueDto;
import com.mihailTs.trading_bot.exception.ElementNotFoundException;
import com.mihailTs.trading_bot.model.LiveAsset;
import com.mihailTs.trading_bot.model.TrainingAsset;
import com.mihailTs.trading_bot.model.TrainingPrice;
import com.mihailTs.trading_bot.model.Token;
import com.mihailTs.trading_bot.repository.TrainingAssetRepository;
import com.mihailTs.trading_bot.repository.TrainingPriceRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TrainingAssetService {

    private final TrainingAssetRepository trainingAssetRepository;
    private final TrainingPriceService trainingPriceService;
    private final TokenService tokenService;

    public TrainingAssetService(TrainingAssetRepository trainingAssetRepository,
                            TrainingPriceService trainingPriceService,
                            TokenService tokenService) {
        this.trainingAssetRepository = trainingAssetRepository;
        this.trainingPriceService = trainingPriceService;
        this.tokenService = tokenService;
    }

    public List<TrainingAsset> getAllAssets() {
        try {
            return trainingAssetRepository.findAll();
        } catch (ElementNotFoundException e) {
            return new ArrayList<>();
        }
    }

    public TrainingAsset getAssetByTokenId(String tokenId) {
        try {
            return trainingAssetRepository.findByTokenId(tokenId);
        } catch (ElementNotFoundException e) {
            return null;
        }
    }

    public List<AssetValueDto> getAssetsWithLatestPrice() {
        try {
            List<Token> tokens = tokenService.getAll();
            List<AssetValueDto> assetValues = new ArrayList<>();

            for(Token token : tokens) {
                TrainingPrice trainingPrice = trainingPriceService.getLatestPrice(token.getId());
                TrainingAsset trainingAsset = getAssetByTokenId(token.getId());
                if(trainingPrice == null || trainingAsset == null) {
                    continue;
                }
                assetValues.add(new AssetValueDto(
                        token.getId(),
                        token.getName(),
                        token.getTicker(),
                        trainingAsset.getQuantity(),
                        trainingPrice.getPrice()));
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

            TrainingPrice trainingPrice = trainingPriceService.getLatestPrice(token.getId());
            TrainingAsset trainingAsset = getAssetByTokenId(token.getId());
            if(trainingAsset == null) {
                return null;
            }

            return new AssetValueDto(
                    token.getId(),
                    token.getName(),
                    token.getTicker(),
                    trainingAsset.getQuantity(),
                    trainingPrice.getPrice());

        } catch (ElementNotFoundException e) {
            return null;
        }
    }

    public void clearData() {
        this.trainingAssetRepository.deleteAll();
    }

    public void updateAssetQuantity(String tokenId, BigDecimal quantity) {
        try {
            trainingAssetRepository.updateQuantity(tokenId, quantity);
        } catch (ElementNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void addAsset(String tokenId) {
        try {
            trainingAssetRepository.insert(new TrainingAsset(tokenId, BigDecimal.valueOf(0), LocalDateTime.now(), LocalDateTime.now()));
        } catch (ElementNotFoundException e) {
            e.printStackTrace();
        }
    }

}
