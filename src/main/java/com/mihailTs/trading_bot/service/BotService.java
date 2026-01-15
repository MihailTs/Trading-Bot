package com.mihailTs.trading_bot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mihailTs.trading_bot.entity.ActionEnum;
import com.mihailTs.trading_bot.model.LiveAsset;
import com.mihailTs.trading_bot.model.LivePrice;
import com.mihailTs.trading_bot.model.TrainingAsset;
import com.mihailTs.trading_bot.model.TrainingPrice;
import com.mihailTs.trading_bot.model.Transaction;
import com.mihailTs.trading_bot.model.Wallet;
import com.mihailTs.trading_bot.websocket.AssetWebSocketHandler;
import com.mihailTs.trading_bot.websocket.TokenPriceWebSocketHandler;
import com.mihailTs.trading_bot.websocket.TransactionWebSocketHandler;
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
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
    private final TrainingPriceService trainingPriceService;
    private LiveAssetService liveAssetService;
    private TrainingAssetService trainingAssetService;
    private TrainingTransactionService trainingTransactionService;
    private LiveTransactionService liveTransactionService;
    private LiveWalletService liveWalletService;
    private TrainingWalletService trainingWalletService;
    private final TokenPriceWebSocketHandler tokenPriceWebSocketHandler;
    private final AssetWebSocketHandler assetWebSocketHandler;
    private final TransactionWebSocketHandler transactionWebSocketHandler;
    private final ObjectMapper objectMapper;
    private final ModeManager modeManager;
    private final StrategyService strategyService;

    public BotService(TokenService tokenService,
                      LivePriceService livePriceService,
                      LiveAssetService liveAssetService,
                      LiveWalletService walletService,
                      TrainingTransactionService trainingTransactionService,
                      LiveTransactionService liveTransactionService,
                      TokenPriceWebSocketHandler tokenPriceWebSocketHandler,
                      AssetWebSocketHandler assetWebSocketHandler,
                      TrainingPriceService trainingPriceService,
                      LiveWalletService liveWalletService,
                      TrainingWalletService trainingWalletService,
                      ObjectMapper objectMapper,
                      ModeManager modeManager,
                      StrategyService strategyService,
                      TransactionWebSocketHandler transactionWebSocketHandler) throws IOException {
        this.tokenService = tokenService;
        this.livePriceService = livePriceService;
        this.liveAssetService = liveAssetService;
        this.liveWalletService = liveWalletService;
        this.trainingPriceService = trainingPriceService;
        this.trainingWalletService = trainingWalletService;
        this.tokenPriceWebSocketHandler = tokenPriceWebSocketHandler;
        this.assetWebSocketHandler = assetWebSocketHandler;
        this.liveTransactionService = liveTransactionService;
        this.trainingTransactionService = trainingTransactionService;
        this.objectMapper = objectMapper;
        this.modeManager = modeManager;
        this.strategyService = strategyService;
        this.transactionWebSocketHandler = transactionWebSocketHandler;
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
    @Scheduled(fixedRate = 20000)
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

        for (String tokenId : tokenIDs) {
            List<Double> lastPrices = livePriceService.getLatestPrices(tokenId, 100)
                                            .stream()
                                            .map(price -> price.getPrice().doubleValue())
                                            .toList();
            UUID newTransactionId = null;
            if (strategyService.nextAction(lastPrices) == ActionEnum.BUY) {
                newTransactionId = buyToken(tokenId);
            } else if (strategyService.nextAction(lastPrices) == ActionEnum.SELL) {
                LiveAsset liveAsset = liveAssetService.getAssetByTokenId(tokenId);
                if(liveAsset == null) {
                    continue;
                }
                newTransactionId = sellToken(tokenId);
            }
            if(newTransactionId != null) {
                transactionWebSocketHandler.broadcastLiveTransaction(newTransactionId);
            }
        }
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


    // TODO: move the logic below in the services; also should return the new transaction
    // always buying with half the available money
    private UUID buyToken(String tokenId) {
        UUID transactionId = UUID.randomUUID();
        if(modeManager.isLiveMode()) {
            Wallet wallet = liveWalletService.getWalletByCurrency("USD");
            if(wallet.getTotal().compareTo(BigDecimal.valueOf(0)) <= 0) {
                return null;
            }
            LiveAsset asset = liveAssetService.getAssetByTokenId(tokenId);
            if(asset == null) {
                // TODO: add should return the added asset
                liveAssetService.addAsset(tokenId);
            }
            asset = liveAssetService.getAssetByTokenId(tokenId);
            LivePrice livePrice = livePriceService.getLatestPrice(tokenId);
            liveTransactionService.saveNewTransaction(
                    transactionId,
                    tokenId,
                    wallet.getTotal()
                            .divide(livePrice.getPrice(), RoundingMode.DOWN)
                            .divide(BigDecimal.valueOf(2), RoundingMode.DOWN),
                    livePrice.getId(),
                    "BUY",
                    LocalDateTime.now()
            );
            liveWalletService.addMoneyToWallet(
                    wallet.getCurrency(),
                    livePrice.getPrice()
                            .multiply(wallet.getTotal()
                                    .divide(livePrice.getPrice(), RoundingMode.DOWN))
                            .multiply(BigDecimal.valueOf(-1)));
            liveAssetService.updateAssetQuantity(
                    tokenId,
                    asset.getQuantity().add(wallet.getTotal()
                            .divide(livePrice.getPrice(), RoundingMode.DOWN)));
        } else {
            Wallet wallet = trainingWalletService.getWalletByCurrency("USD");
            if(wallet.getTotal().compareTo(BigDecimal.valueOf(0)) <= 0) {
                return null;
            }
            TrainingAsset asset = trainingAssetService.getAssetByTokenId(tokenId);
            if(asset == null) {
                // TODO: add should return the added asset
                trainingAssetService.addAsset(tokenId);
            }
            asset = trainingAssetService.getAssetByTokenId(tokenId);
            TrainingPrice trainingPrice = trainingPriceService.getLatestPrice(tokenId);
            trainingTransactionService.saveNewTransaction(
                    transactionId,
                    tokenId,
                    wallet.getTotal()
                            .divide(trainingPrice.getPrice(), RoundingMode.DOWN)
                            .divide(BigDecimal.valueOf(2), RoundingMode.DOWN),
                    trainingPrice.getId(),
                    "BUY",
                    LocalDateTime.now()
            );
            trainingWalletService.addMoneyToWallet(
                    wallet.getCurrency(),
                    trainingPrice.getPrice()
                            .multiply(wallet.getTotal()
                                    .divide(trainingPrice.getPrice(), RoundingMode.DOWN))
                            .multiply(BigDecimal.valueOf(-1)));
            trainingAssetService.updateAssetQuantity(
                    tokenId,
                    asset.getQuantity().add(wallet.getTotal()
                            .divide(trainingPrice.getPrice(), RoundingMode.DOWN)));

        }
        return transactionId;
    }

    // selling half the token asset if available
    private UUID sellToken(String tokenId) {
        UUID transactionId = UUID.randomUUID();
        if(modeManager.isLiveMode()) {
            Wallet wallet = liveWalletService.getWalletByCurrency("USD");
            LiveAsset asset = liveAssetService.getAssetByTokenId(tokenId);
            LivePrice livePrice = livePriceService.getLatestPrice(tokenId);
            liveTransactionService.saveNewTransaction(
                    transactionId,
                    tokenId,
                    asset.getQuantity().divide(BigDecimal.valueOf(2), RoundingMode.DOWN),
                    livePrice.getId(),
                    "SELL",
                    LocalDateTime.now()
            );
            liveWalletService.addMoneyToWallet(
                    wallet.getCurrency(),
                    livePrice.getPrice()
                            .multiply(wallet.getTotal()
                                    .divide(livePrice.getPrice(), RoundingMode.DOWN))
                            .multiply(BigDecimal.valueOf(-1)));
            liveAssetService.updateAssetQuantity(
                    tokenId,
                    asset.getQuantity().subtract(asset.getQuantity()
                            .divide(BigDecimal.valueOf(2), RoundingMode.DOWN)));
        } else {
            Wallet wallet = trainingWalletService.getWalletByCurrency("USD");
            TrainingAsset asset = trainingAssetService.getAssetByTokenId(tokenId);
            TrainingPrice trainingPrice = trainingPriceService.getLatestPrice(tokenId);
            trainingTransactionService.saveNewTransaction(
                    transactionId,
                    tokenId,
                    asset.getQuantity().divide(BigDecimal.valueOf(2), RoundingMode.DOWN),
                    trainingPrice.getId(),
                    "SELL",
                    LocalDateTime.now()
            );
            trainingWalletService.addMoneyToWallet(
                    wallet.getCurrency(),
                    trainingPrice.getPrice()
                            .multiply(wallet.getTotal()
                                    .divide(trainingPrice.getPrice(), RoundingMode.DOWN)).multiply(BigDecimal.valueOf(-1)));
            trainingAssetService.updateAssetQuantity(
                    tokenId,
                    asset.getQuantity().subtract(asset.getQuantity().divide(BigDecimal.valueOf(2), RoundingMode.DOWN)));
        }
        return transactionId;
    }

}