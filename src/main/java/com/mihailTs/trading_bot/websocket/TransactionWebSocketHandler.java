package com.mihailTs.trading_bot.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mihailTs.trading_bot.dto.TokenWithPriceDto;
import com.mihailTs.trading_bot.dto.WalletDto;
import com.mihailTs.trading_bot.model.LivePrice;
import com.mihailTs.trading_bot.model.LiveTransaction;
import com.mihailTs.trading_bot.model.Token;
import com.mihailTs.trading_bot.model.Wallet;
import com.mihailTs.trading_bot.service.LiveAssetService;
import com.mihailTs.trading_bot.service.LivePriceService;
import com.mihailTs.trading_bot.service.LiveTransactionService;
import com.mihailTs.trading_bot.service.TokenService;
import com.mihailTs.trading_bot.service.TrainingPriceService;
import com.mihailTs.trading_bot.service.TrainingTransactionService;
import com.mihailTs.trading_bot.service.WalletService;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class TransactionWebSocketHandler extends TextWebSocketHandler {

    private LivePriceService livePriceService;
    private TrainingPriceService trainingPriceService;
    private LiveTransactionService liveTransactionService;
    private TrainingTransactionService trainingTransactionService;
    private final ObjectMapper objectMapper;
    // thread-safe
    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    public TransactionWebSocketHandler(LivePriceService livePriceService,
                                      TrainingPriceService trainingPriceService,
                                      TrainingTransactionService trainingTransactionService,
                                      LiveTransactionService liveTransactionService,
                                      ObjectMapper objectMapper) {
        this.livePriceService = livePriceService;
        this.trainingPriceService = trainingPriceService;
        this.trainingTransactionService = trainingTransactionService;
        this.liveTransactionService = liveTransactionService;
        this.objectMapper = objectMapper;
    }

    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
    }

    public void afterConnectionClosed(WebSocketSession session, CloseStatus status)
            throws Exception {
        sessions.remove(session);
    }

    public void broadcastTransaction(UUID transactionId) {
        if (sessions.isEmpty()) {
            return;
        }

        try {


            LivePrice latestPrice = priceService.getLatestPrice(tokenId);
            TokenWithPriceDto tokenData = new TokenWithPriceDto(
                    token.getId(),
                    token.getName(),
                    token.getTicker(),
                    latestPrice != null ? latestPrice.getPrice() : BigDecimal.ZERO,
                    latestPrice != null ? latestPrice.getCreatedAt() : null
            );

            sendToSessions(objectMapper.writeValueAsString(tokenData));
        } catch (Exception e) {
            System.out.println("Error broadcasting token: " + e.getMessage());
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
