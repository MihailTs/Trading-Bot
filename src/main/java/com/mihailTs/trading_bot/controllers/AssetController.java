package com.mihailTs.trading_bot.controllers;

import com.mihailTs.trading_bot.dto.AssetValueDto;
import com.mihailTs.trading_bot.dto.TokenWithPriceDto;
import com.mihailTs.trading_bot.model.LiveAsset;
import com.mihailTs.trading_bot.model.LivePrice;
import com.mihailTs.trading_bot.service.LiveAssetService;
import com.mihailTs.trading_bot.service.LivePriceService;
import com.mihailTs.trading_bot.service.TokenService;
import com.mihailTs.trading_bot.service.TrainingAssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/assets")
@RequiredArgsConstructor
public class AssetController {

    private final LiveAssetService liveAssetService;
    private final TrainingAssetService trainingAssetService;

    @GetMapping("/live")
    public ResponseEntity<List<AssetValueDto>> getAllLiveAssets() {
        List<AssetValueDto> assets = liveAssetService.getAssetsWithLatestPrice();
        return ResponseEntity.ok(assets);
    }

    @GetMapping("/training")
    public ResponseEntity<List<AssetValueDto>> getAllTrainingAssets() {
        List<AssetValueDto> assets = trainingAssetService.getAssetsWithLatestPrice();
        return ResponseEntity.ok(assets);
    }

}
