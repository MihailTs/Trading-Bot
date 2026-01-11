package com.mihailTs.trading_bot.config;

import com.mihailTs.trading_bot.websocket.TokenPriceWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final TokenPriceWebSocketHandler tokenPriceWebSocketHandler;

    public WebSocketConfig(TokenPriceWebSocketHandler tokenPriceWebSocketHandler) {
        this.tokenPriceWebSocketHandler = tokenPriceWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(tokenPriceWebSocketHandler, "/ws/prices")
                .setAllowedOrigins("*");
    }

}
