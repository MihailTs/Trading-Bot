package com.mihailTs.trading_bot.model;

import java.util.UUID;

public class Token {
    private UUID id;
    private String name;
    private String ticker;

    public Token(UUID id, String name, String ticker) {
        setId(id);
        setName(name);
        setTicker(ticker);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTicker() {
        return ticker;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

}
