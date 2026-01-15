package com.mihailTs.trading_bot.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mihailTs.trading_bot.dto.TokenWithPriceDto;
import com.mihailTs.trading_bot.dto.TransactionDto;
import com.mihailTs.trading_bot.model.LivePrice;
import com.mihailTs.trading_bot.model.LiveTransaction;
import com.mihailTs.trading_bot.model.Token;
import com.mihailTs.trading_bot.model.TrainingPrice;
import com.mihailTs.trading_bot.model.TrainingTransaction;
import com.mihailTs.trading_bot.service.LivePriceService;
import com.mihailTs.trading_bot.service.LiveTransactionService;
import com.mihailTs.trading_bot.service.TokenService;
import com.mihailTs.trading_bot.service.TrainingPriceService;
import com.mihailTs.trading_bot.service.TrainingTransactionService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class TransactionWebSocketHandler extends TextWebSocketHandler {

    private LivePriceService livePriceService;
    private final TrainingPriceService trainingPriceService;
    private final LiveTransactionService liveTransactionService;
    private final TrainingTransactionService trainingTransactionService;
    private final TokenService tokenService;
    private final ObjectMapper objectMapper;
    // thread-safe
    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    public TransactionWebSocketHandler(LivePriceService livePriceService,
                                      TrainingPriceService trainingPriceService,
                                      TrainingTransactionService trainingTransactionService,
                                      LiveTransactionService liveTransactionService,
                                      ObjectMapper objectMapper,
                                      TokenService tokenService) {
        this.livePriceService = livePriceService;
        this.trainingPriceService = trainingPriceService;
        this.trainingTransactionService = trainingTransactionService;
        this.liveTransactionService = liveTransactionService;
        this.objectMapper = objectMapper;
        this.tokenService = tokenService;
    }

    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
    }

    public void afterConnectionClosed(WebSocketSession session, CloseStatus status)
            throws Exception {
        sessions.remove(session);
    }

    public void broadcastLiveTransaction(UUID transactionId) {
        if (sessions.isEmpty()) {
            return;
        }

        try {
            LiveTransaction liveTransaction = liveTransactionService.getById(transactionId);
            LivePrice livePrice = livePriceService.getById(liveTransaction.getPriceId());
            Token token = tokenService.getTokenById(livePrice.getTokenId());
            TransactionDto transactionData = new TransactionDto(
                    liveTransaction.getId(),
                    token.getName(),
                    token.getTicker(),
                    liveTransaction.getType(),
                    liveTransaction.getQuantity(),
                    liveTransaction.getCreatedAt()
            );

            sendToSessions(objectMapper.writeValueAsString(liveTransaction));
        } catch (Exception e) {
            System.out.println("Error broadcasting transaction: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void broadcastTrainingTransaction(UUID transactionId) {
        if (sessions.isEmpty()) {
            return;
        }

        try {
            TrainingTransaction trainingTransaction = trainingTransactionService.getById(transactionId);
            TrainingPrice trainingPrice = trainingPriceService.getById(trainingTransaction.getPriceId());
            Token token = tokenService.getTokenById(trainingPrice.getTokenId());
            TransactionDto transactionData = new TransactionDto(
                    trainingTransaction.getId(),
                    token.getName(),
                    token.getTicker(),
                    trainingTransaction.getType(),
                    trainingTransaction.getQuantity(),
                    trainingTransaction.getCreatedAt()
            );

            sendToSessions(objectMapper.writeValueAsString(trainingTransaction));
        } catch (Exception e) {
            System.out.println("Error broadcasting transaction: " + e.getMessage());
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
