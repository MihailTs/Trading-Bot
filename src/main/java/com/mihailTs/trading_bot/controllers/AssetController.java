package com.mihailTs.trading_bot.controllers;

import com.mihailTs.trading_bot.dto.AssetValueDto;
import com.mihailTs.trading_bot.dto.TokenWithPriceDto;
import com.mihailTs.trading_bot.model.LiveAsset;
import com.mihailTs.trading_bot.model.LivePrice;
import com.mihailTs.trading_bot.service.LiveAssetService;
import com.mihailTs.trading_bot.service.LivePriceService;
import com.mihailTs.trading_bot.service.TokenService;
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

    private final LiveAssetService assetService;

    // Get all assets with price
    @GetMapping
    public ResponseEntity<List<AssetValueDto>> getAllAssets() {
        List<AssetValueDto> assets = assetService.getAssetsWithLatestPrice();
        return ResponseEntity.ok(assets);
    }

}
