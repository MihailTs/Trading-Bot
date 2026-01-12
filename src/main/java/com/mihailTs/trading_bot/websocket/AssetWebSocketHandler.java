package com.mihailTs.trading_bot.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mihailTs.trading_bot.dto.AssetValueDto;
import com.mihailTs.trading_bot.dto.TokenWithPriceDto;
import com.mihailTs.trading_bot.dto.WalletDto;
import com.mihailTs.trading_bot.model.LivePrice;
import com.mihailTs.trading_bot.model.Token;
import com.mihailTs.trading_bot.model.Wallet;
import com.mihailTs.trading_bot.service.LiveAssetService;
import com.mihailTs.trading_bot.service.LivePriceService;
import com.mihailTs.trading_bot.service.TokenService;
import com.mihailTs.trading_bot.service.WalletService;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class AssetWebSocketHandler extends TextWebSocketHandler {

    private TokenService tokenService;
    private LivePriceService priceService;
    private WalletService walletService;
    private LiveAssetService liveAssetService;
    private final ObjectMapper objectMapper;
    // thread-safe
    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    public AssetWebSocketHandler(TokenService tokenService,
                                      LivePriceService livePriceService,
                                      WalletService walletService,
                                      LiveAssetService liveAssetService,
                                      ObjectMapper objectMapper) {
        this.tokenService = tokenService;
        this.priceService = livePriceService;
        this.walletService = walletService;
        this.liveAssetService = liveAssetService;
        this.objectMapper = objectMapper;
    }

    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
    }

    public void afterConnectionClosed(WebSocketSession session, CloseStatus status)
            throws Exception {
        sessions.remove(session);
    }

    public void broadcastAsset(String tokenId) {
        if (sessions.isEmpty()) {
            return;
        }

        try {
            AssetValueDto assetValueDto = liveAssetService.getAssetWIthLatestPrice(tokenId);
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
