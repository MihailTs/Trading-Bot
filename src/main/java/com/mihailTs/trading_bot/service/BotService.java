package com.mihailTs.trading_bot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mihailTs.trading_bot.model.TrainingPrice;
import com.mihailTs.trading_bot.websocket.AssetWebSocketHandler;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static com.mihailTs.trading_bot.service.ModeManager.Mode.LIVE;
import static com.mihailTs.trading_bot.service.ModeManager.Mode.TRAINING;

@Service
public class BotService {

    @Value("${crypto.api.key}")
    private String apiKey;
    @Value("${crypto.api.token-prices.url}")
    private String tokenPricesURL;
    @Value("${token.price.precision}")
    private String pricePrecision;
    private final TokenService tokenService;
    private final LivePriceService livePriceService;
    private LiveAssetService liveAssetService;
    private final TrainingPriceService trainingPriceService;
    private TrainingAssetService trainingAssetService;
    private TrainingTransactionService trainingTransactionService;
    private LiveTransactionService liveTransactionService;
    private LiveWalletService walletService;
    private final TokenPriceWebSocketHandler tokenPriceWebSocketHandler;
    private final AssetWebSocketHandler assetWebSocketHandler;
    private final ObjectMapper objectMapper;
    private final ModeManager modeManager;

    public BotService(TokenService tokenService,
                      LivePriceService livePriceService,
                      LiveAssetService liveAssetService,
                      LiveWalletService walletService,
                      TrainingTransactionService trainingTransactionService,
                      LiveTransactionService liveTransactionService,
                      TokenPriceWebSocketHandler tokenPriceWebSocketHandler,
                      AssetWebSocketHandler assetWebSocketHandler,
                      TrainingPriceService trainingPriceService,
                      ObjectMapper objectMapper,
                      ModeManager modeManager) throws IOException {
        this.tokenService = tokenService;
        this.livePriceService = livePriceService;
        this.liveAssetService = liveAssetService;
        this.walletService = walletService;
        this.trainingPriceService = trainingPriceService;
        this.tokenPriceWebSocketHandler = tokenPriceWebSocketHandler;
        this.assetWebSocketHandler = assetWebSocketHandler;
        this.liveTransactionService = liveTransactionService;
        this.trainingTransactionService = trainingTransactionService;
        this.objectMapper = objectMapper;
        this.modeManager = modeManager;

        // Register listener to fetch data immediately when switching to training mode
        modeManager.registerModeChangeListener(mode -> {
            if (mode == ModeManager.Mode.TRAINING) {
                try {
                    fetchAndLoadFirstDay();
                } catch (IOException e) {
                    System.err.println("Error fetching initial training data: " + e.getMessage());
                }
            }
        });
    }

    // run once every 24 hours - for LIVE mode ONLY
    @PostConstruct
    @Scheduled(fixedRate = 86400000)
    public void fetchHistoricPricesDays() throws IOException {
        if (!modeManager.isLiveMode()) {
            return;
        }

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

    // LIVE mode - fetch from the API every 50 seconds
    @Scheduled(fixedRate = 50000)
    public void fetchLiveData() throws IOException, InterruptedException {
        if (!modeManager.isLiveMode()) {
            return;
        }

        List<String> tokenIDs = tokenService.getTokenIds();

        if (tokenIDs.isEmpty()) {
            return;
        }

        String jsonResponse = getJSONTokenPricesResponse(tokenIDs);
        parseJSONTokenPricesResponse(jsonResponse);

        broadcastLiveTokenUpdates(tokenIDs);
    }

    // every minute fetch the next part of the training data
    @Scheduled(fixedRate = 60000)
    public void fetchHistoricDataInBatches() throws IOException {
        if (!modeManager.isTrainingMode()) {
            return;
        }
        fetchNextDay();
    }

    // TRAINING mode - fetch from the database every 2 seconds
    @Scheduled(fixedRate = 2000)
    public void fetchTrainingData() throws IOException {
        if (!modeManager.isTrainingMode()) {
            return;
        }

        if (modeManager.hasReachedTrainingEnd()) {
            System.out.println("Training period ended");
//            modeManager.setMode(ModeManager.Mode.LIVE);
            return;
        }

        if (modeManager.isBatchExhausted()) {
            modeManager.markDayComplete();
            System.out.println("Day completed. Moving to next day: " + modeManager.getCurrentTrainingDate());
            return;
        }

        TrainingPrice price = modeManager.getNextTrainingPrice();

        if (price == null) {
            return;
        }

        String tokenId = price.getTokenId();
        tokenPriceWebSocketHandler.broadcastTrainingToken(tokenId, price);

        if (liveAssetService.getAssetByTokenId(tokenId) != null) {
            assetWebSocketHandler.broadcastAsset(tokenId, TRAINING);
        }
    }

    private void broadcastLiveTokenUpdates(List<String> tokenIDs) {
        for(String id : tokenIDs) {
            tokenPriceWebSocketHandler.broadcastLiveToken(id);
            if (liveAssetService.getAssetByTokenId(id) != null) {
                assetWebSocketHandler.broadcastAsset(id, LIVE);
            }
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

                if(modeManager.getCurrentMode() == ModeManager.Mode.LIVE) {
                    livePriceService.saveNewPrice(price, tokenId, timestamp);
                } else {
                    trainingPriceService.saveNewPrice(price, tokenId, timestamp);
                }
            }
        }
    }

    private String getJSONHistoricDayResponse(String tokenId, LocalDate dayToFetch) throws IOException {
        StringBuilder urlString = new StringBuilder(tokenPricesURL)
                .append(tokenId)
                .append("/market_chart/range?vs_currency=usd&from=")
                .append(dayToFetch.atStartOfDay(ZoneId.systemDefault()).toEpochSecond())
                .append("&to=")
                .append(dayToFetch.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toEpochSecond());

        System.out.println("Fetching: " + urlString);
        URL url = new URL(urlString.toString());
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

    private void fetchAndLoadFirstDay() throws IOException {
        if (!modeManager.isTrainingMode()) {
            return;
        }

        LocalDate dayToFetch = modeManager.getTrainingStartDate();

        List<String> tokenIDs = tokenService.getTokenIds();

        if (tokenIDs.isEmpty()) {
            return;
        }

        for (String tokenId : tokenIDs) {
            try {
                String jsonResponse = getJSONHistoricDayResponse(tokenId, dayToFetch);
                parseJSONHistoricPricesResponse(jsonResponse, tokenId);
            } catch (Exception e) {
                System.err.println("Error fetching historic data for " + tokenId + ": " + e.getMessage());
            }
        }

        modeManager.markDayFetched();

        loadBatchForDay(dayToFetch);
    }

    private void fetchNextDay() {
        if (!modeManager.isTrainingMode()) {
            return;
        }

        LocalDate dayToFetch = modeManager.getLastFetchedDay().plusDays(1);

        if (dayToFetch.isAfter(modeManager.getTrainingEndDate())) {
            System.out.println("All training data has been fetched");
            return;
        }

        List<String> tokenIDs = tokenService.getTokenIds();

        if (tokenIDs.isEmpty()) {
            return;
        }

        // Fetch data for the next day
        for (String tokenId : tokenIDs) {
            try {
                String jsonResponse = getJSONHistoricDayResponse(tokenId, dayToFetch);
                parseJSONHistoricPricesResponse(jsonResponse, tokenId);
            } catch (Exception e) {
                System.err.println("Error fetching historic data for " + tokenId + ": " + e.getMessage());
            }
        }

        modeManager.markDayFetched();

        System.out.println("Fetched and stored data for: " + dayToFetch);
    }

    private void loadBatchForDay(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        // fetch all prices for this day from the database, sorted by timestamp
        List<TrainingPrice> dayPrices = trainingPriceService.getPricesBetween(startOfDay, endOfDay);

        // initialize the batch in ModeManager
        modeManager.initializeDayBatch(dayPrices);

        System.out.println("Loaded batch for " + date + " with " + dayPrices.size() + " prices");
    }
}