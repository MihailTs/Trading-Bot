package com.mihailTs.trading_bot.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mihailTs.trading_bot.dto.WalletDto;
import com.mihailTs.trading_bot.model.Wallet;
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
public class WalletWebSocketHandler extends TextWebSocketHandler {
    private LiveWalletService walletService;
    private final ObjectMapper objectMapper;
    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    public WalletWebSocketHandler(LiveWalletService walletService,
                                  ObjectMapper objectMapper) {
        this.walletService = walletService;
        this.objectMapper = objectMapper;
    }

    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
    }

    public void afterConnectionClosed(WebSocketSession session, CloseStatus status)
            throws Exception {
        sessions.remove(session);
    }

    public void broadcastWallet(String currency) {
        if (sessions.isEmpty()) {
            return;
        }

        try {
            Wallet wallet = walletService.getWalletByCurrency(currency);
            WalletDto walletDto = new WalletDto(wallet.getCurrency(), wallet.getTotal());
            sendToSessions(objectMapper.writeValueAsString(walletDto));
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
