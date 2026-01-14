package com.mihailTs.trading_bot.config;

import com.mihailTs.trading_bot.websocket.AssetWebSocketHandler;
import com.mihailTs.trading_bot.websocket.TokenPriceWebSocketHandler;
import com.mihailTs.trading_bot.websocket.TransactionWebSocketHandler;
import com.mihailTs.trading_bot.websocket.WalletWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final TokenPriceWebSocketHandler tokenPriceWebSocketHandler;
    private final AssetWebSocketHandler assetWebSocketHandler;
    private final WalletWebSocketHandler walletWebSocketHandler;
//    private final TransactionWebSocketHandler transactionWebSocketHandler;

    public WebSocketConfig(TokenPriceWebSocketHandler tokenPriceWebSocketHandler,
                           AssetWebSocketHandler assetWebSocketHandler,
                           WalletWebSocketHandler walletWebSocketHandler) {
        this.tokenPriceWebSocketHandler = tokenPriceWebSocketHandler;
        this.assetWebSocketHandler = assetWebSocketHandler;
        this.walletWebSocketHandler = walletWebSocketHandler;
//        this.transactionWebSocketHandler = transactionWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(tokenPriceWebSocketHandler, "/ws/prices")
                .addHandler(assetWebSocketHandler, "/ws/assets")
                .addHandler(walletWebSocketHandler, "/ws/wallets")
//                .addHandler(transactionWebSocketHandler, "/ws/transactions")
                .setAllowedOrigins("*");
    }

}
