package com.mihailTs.trading_bot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mihailTs.trading_bot.websocket.TokenPriceWebSocketHandler;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Iterator;
import java.util.List;

@Service
public class BotService {

    @Value("${crypto.api.key}")
    private String apiKey;
    @Value("${crypto.api.token-prices.url}")
    private String tokenPricesURL;
    @Value("${token.price.precision}")
    private String pricePrecision;
    private TokenService tokenService;
    private LivePriceService livePriceService;
    private LiveAssetService liveAssetService;
    private TokenPriceWebSocketHandler tokenPriceWebSocketHandler;
    private ObjectMapper objectMapper;

    public BotService(TokenService tokenService,
                      LivePriceService livePriceService,
                      LiveAssetService liveAssetService,
                      TokenPriceWebSocketHandler tokenPriceWebSocketHandler,
                      ObjectMapper objectMapper) throws IOException {
        this.tokenService = tokenService;
        this.livePriceService = livePriceService;
        this.liveAssetService = liveAssetService;
        this.tokenPriceWebSocketHandler = tokenPriceWebSocketHandler;
        this.objectMapper = objectMapper;

    }

    // run once every 24 hours
    @PostConstruct
    @Scheduled(fixedRate = 86400000)
    public void fetchHistoricPricesDays() throws IOException {
        List<String> tokenIDs = tokenService.getTokenIds();

        if (tokenIDs.isEmpty()) {
            return;
        }

        for(String tokenId : tokenIDs) {
            try {
                String jsonResponse = getJSONHistoricPricesDaysResponse(tokenId, 1);
                parseJSONHistoricPricesResponse(jsonResponse, tokenId);
            } catch (Exception e) {
                System.err.println("Error fetching historic prices for " + tokenId + ": " + e.getMessage());
            }
        }
    }

    // API data changes every ~1 minute
    @Scheduled(fixedRate = 30000)
    public void fetchNewestData() throws IOException, InterruptedException {
        List<String> tokenIDs = tokenService.getTokenIds();

        if (tokenIDs.isEmpty()) {
            return;
        }

        String jsonResponse = getJSONTokenPricesResponse(tokenIDs);
        parseJSONTokenPricesResponse(jsonResponse);

        for(String id : tokenIDs) {
            tokenPriceWebSocketHandler.broadcastToken(id);
        }

    }

    private void parseJSONTokenPricesResponse(String jsonResponse) throws JsonProcessingException {
        JsonNode root = objectMapper.readTree(jsonResponse);

        if (root.isArray()) {
            for (JsonNode tokenNode : root) {
                String id = tokenNode.path("id").asText();
                BigDecimal circulatingSupply = tokenNode.path("circulating_supply").decimalValue();
                BigDecimal price = tokenNode.path("current_price").decimalValue();

                tokenService.updateTokenCirculatingSupply(id, circulatingSupply);
                livePriceService.saveNewPrice(price, id, LocalDateTime.now());
            }
        }
    }

    private String getIdParams(List<String> ids) {
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0; i < ids.size(); i++) {
            stringBuilder.append(ids.get(i));
            if(i < ids.size() - 1) {
                stringBuilder.append(",");
            }
        }
        return stringBuilder.toString();
    }

    private String getJSONTokenPricesResponse(List<String> tokenIDs) throws IOException {
        String idParams = getIdParams(tokenIDs);
        String urlStr = tokenPricesURL + "markets?vs_currency=usd" + "&precision=" + pricePrecision + "&ids=" + idParams;

        URL url = new URL(urlStr);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        con.setRequestMethod("GET");
        con.setRequestProperty("Accept", "application/json");

        int status = con.getResponseCode();

        InputStream stream = (status >= 200 && status < 300) ? con.getInputStream() : con.getErrorStream();

        BufferedReader in = new BufferedReader(new InputStreamReader(stream));
        String inputLine;
        StringBuilder content = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        con.disconnect();

        return content.toString();
    }

    private String getJSONHistoricPricesDaysResponse(String tokenId, int daysCount) throws IOException {
        String urlStr = tokenPricesURL + tokenId + "/market_chart?vs_currency=usd&days=" + daysCount;
        System.out.println(urlStr);
        URL url = new URL(urlStr);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        con.setRequestMethod("GET");
        con.setRequestProperty("Accept", "application/json");

        int status = con.getResponseCode();

        InputStream stream = (status >= 200 && status < 300) ? con.getInputStream() : con.getErrorStream();

        BufferedReader in = new BufferedReader(new InputStreamReader(stream));
        String inputLine;
        StringBuilder content = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        con.disconnect();

        return content.toString();
    }

    private void parseJSONHistoricPricesResponse(String jsonResponse, String tokenId)
            throws JsonProcessingException {

        JsonNode root = objectMapper.readTree(jsonResponse);

        JsonNode pricesNode = root.path("prices");

        if (pricesNode.isArray()) {
            for (JsonNode priceEntry : pricesNode) {
                LocalDateTime timestamp = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(priceEntry.get(0).asLong()),
                        ZoneId.systemDefault()
                );
                BigDecimal price = priceEntry.get(1).decimalValue();

                livePriceService.saveNewPrice(price, tokenId, timestamp);
            }
        }
    }



    private static <T> Iterable<T> iterable(Iterator<T> it) {
        return () -> it;
    }

}
