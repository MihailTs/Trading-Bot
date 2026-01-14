package com.mihailTs.trading_bot.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mihailTs.trading_bot.dto.AssetValueDto;
import com.mihailTs.trading_bot.service.LiveAssetService;
import com.mihailTs.trading_bot.service.LivePriceService;
import com.mihailTs.trading_bot.service.ModeManager;
import com.mihailTs.trading_bot.service.TokenService;
import com.mihailTs.trading_bot.service.TrainingAssetService;
import com.mihailTs.trading_bot.service.LiveWalletService;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class AssetWebSocketHandler extends TextWebSocketHandler {

    private TokenService tokenService;
    private LivePriceService priceService;
    private LiveWalletService walletService;
    private LiveAssetService liveAssetService;
    private TrainingAssetService trainingAssetService;
    private final ObjectMapper objectMapper;
    // thread-safe
    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    public AssetWebSocketHandler(TokenService tokenService,
                                      LivePriceService livePriceService,
                                      LiveWalletService walletService,
                                      LiveAssetService liveAssetService,
                                      TrainingAssetService trainingAssetService,
                                      ObjectMapper objectMapper) {
        this.tokenService = tokenService;
        this.priceService = livePriceService;
        this.walletService = walletService;
        this.liveAssetService = liveAssetService;
        this.trainingAssetService = trainingAssetService;
        this.objectMapper = objectMapper;
    }

    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
    }

    public void afterConnectionClosed(WebSocketSession session, CloseStatus status)
            throws Exception {
        sessions.remove(session);
    }

    public void broadcastAsset(String tokenId, ModeManager.Mode mode) {
        if (sessions.isEmpty()) {
            return;
        }

        try {
            AssetValueDto assetValueDto = null;
            if(mode == ModeManager.Mode.LIVE) {
                assetValueDto = liveAssetService.getAssetWIthLatestPrice(tokenId);
            } else {
                assetValueDto = trainingAssetService.getAssetWIthLatestPrice(tokenId);
            }
            sendToSessions(objectMapper.writeValueAsString(assetValueDto));
        } catch (Exception e) {
            System.out.println("Error broadcasting asset: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void sendToSessions(String payload) {
        TextMessage message = new TextMessage(payload);

        for (WebSocketSession session : sessions) {
            try {
                if (session.isOpen()) {
                    session.sendMessage(message);
                }
            } catch (IOException e) {
                System.out.println("Failed to send to session " + session.getId());
            }
        }
    }

    public int getConnectedSessionCount() {
        return sessions.size();
    }

}
