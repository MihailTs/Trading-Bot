package com.mihailTs.trading_bot.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mihailTs.trading_bot.dto.TokenWithPriceDto;
import com.mihailTs.trading_bot.model.LivePrice;
import com.mihailTs.trading_bot.model.Token;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import com.mihailTs.trading_bot.service.LivePriceService;
import com.mihailTs.trading_bot.service.TokenService;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class TokenPriceWebSocketHandler extends TextWebSocketHandler {

    private TokenService tokenService;
    private LivePriceService priceService;
    private final ObjectMapper objectMapper;
    // thread-safe
    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    public TokenPriceWebSocketHandler(TokenService tokenService,
                                      LivePriceService livePriceService,
                                      ObjectMapper objectMapper) {
        this.tokenService = tokenService;
        this.priceService = livePriceService;
        this.objectMapper = objectMapper;
    }

    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        System.out.println("Client connected: " + session.getId());
        System.out.println("Total connections: " + sessions.size());
    }

    public void afterConnectionClosed(WebSocketSession session, CloseStatus status)
            throws Exception {
        sessions.remove(session);
        System.out.println("Client disconnected: " + session.getId());
        System.out.println("Total connections: " + sessions.size());
    }

    public void broadcastToken(int tokenId) {
        if (sessions.isEmpty()) {
            System.out.println("No clients connected");
            return;
        }

        try {
            Token token = tokenService.getTokenById(tokenId);
            LivePrice latestPrice = priceService.getLatestPrice(tokenId);

            TokenWithPriceDto tokenData = new TokenWithPriceDto(
                    token.getId(),
                    token.getName(),
                    token.getTicker(),
                    latestPrice != null ? latestPrice.getPrice() : BigDecimal.ZERO,
                    latestPrice != null ? latestPrice.getCreatedAt() : null
            );

            // Convert DTO to JSON
            String messagePayload = objectMapper.writeValueAsString(tokenData);
            TextMessage message = new TextMessage(messagePayload);

            for (WebSocketSession session : sessions) {
                try {
                    if (session.isOpen()) {
                        session.sendMessage(message);
                    }
                } catch (IOException e) {
                    System.out.println("Failed to send to session " + session.getId());
                }
            }
        } catch (Exception e) {
            System.out.println("Error broadcasting token: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public int getConnectedSessionCount() {
        return sessions.size();
    }

}
