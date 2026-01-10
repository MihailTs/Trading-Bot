package com.mihailTs.trading_bot.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

@Service
public class BotService {

    @Value("${crypto.api.key}")
    private String apiKey;
    private TokenService tokenService;
    private LivePriceService livePriceService;
    private LiveAssetService liveAssetService;
    private static final ObjectMapper mapper = new ObjectMapper();

    public BotService(TokenService tokenService,
                      LivePriceService livePriceService,
                      LiveAssetService liveAssetService) {
        this.tokenService = tokenService;
        this.livePriceService = livePriceService;
        this.liveAssetService = liveAssetService;
    }

    @Scheduled(fixedRate = 20000)
    @Order(1)
    public void fetchNewestData() throws IOException {
        ArrayList<Integer> tokenIDs = tokenService.getTokenIds();

        if (tokenIDs.isEmpty()) {
            return;
        }

        String jsonResponse = getJSONResponse(tokenIDs);
        parseJSONResponse(jsonResponse);

    }

    private void parseJSONResponse(String jsonResponse) throws JsonProcessingException {
        JsonNode root = mapper.readTree(jsonResponse);
        JsonNode tokensNode = root.path("data");

        if (tokensNode.isObject()) {
            for (String fieldName : iterable(tokensNode.fieldNames())) {
                JsonNode tokenNode = tokensNode.get(fieldName);

                // TODO: error handling for missing data
                int id = tokenNode.path("id").asInt();
                String name = tokenNode.path("name").asText();
                String symbol = tokenNode.path("symbol").asText();
                BigDecimal circulatingSupply = tokenNode.path("circulating_supply").decimalValue();
                BigDecimal price = tokenNode.path("quote").path("USD").path("price").decimalValue();

                tokenService.updateTokenCirculatingSupply(id, circulatingSupply);
                livePriceService.saveNewPrice(price, id);
            }
        }
    }

    private String getIdParams(ArrayList<Integer> ids) {
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0; i < ids.size(); i++) {
            stringBuilder.append(ids.get(i));
            if(i < ids.size() - 1) {
                stringBuilder.append(",");
            }
        }
        return stringBuilder.toString();
    }

    private String getJSONResponse(ArrayList<Integer> tokenIDs) throws IOException {
        String idParams = getIdParams(tokenIDs);
        String urlStr =
                "https://pro-api.coinmarketcap.com/v2/cryptocurrency/quotes/latest?id=" + idParams;

        System.out.println(urlStr);

        URL url = new URL(urlStr);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        con.setRequestMethod("GET");
        con.setRequestProperty("Accept", "application/json");
        con.setRequestProperty("X-CMC_PRO_API_KEY", apiKey);

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

    private static <T> Iterable<T> iterable(Iterator<T> it) {
        return () -> it;
    }

}
