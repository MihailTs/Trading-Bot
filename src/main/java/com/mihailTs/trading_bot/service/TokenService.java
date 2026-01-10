package com.mihailTs.trading_bot.service;

import com.mihailTs.trading_bot.exception.ElementNotFoundException;
import com.mihailTs.trading_bot.model.Token;
import com.mihailTs.trading_bot.repository.TokenRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TokenService {

    private TokenRepository tokenRepository;

    public TokenService(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    public List<Token> getAll() {
        try {
            return tokenRepository.findAll();
        } catch (ElementNotFoundException e) {
            return new ArrayList<>();
        }
    }

    public Token getTokenById(int id) {
        try {
            return tokenRepository.findById(id);
        } catch (ElementNotFoundException e) {
            return null;
        }
    }

    public ArrayList<Integer> getTokenIds() {
        try {
            return tokenRepository.findAllIDs();
        } catch (ElementNotFoundException e) {
            return new ArrayList<>();
        }
    }

    public Token updateTokenCirculatingSupply(int id, BigDecimal circulatingSupply) {
        try {
            Token token = tokenRepository.findById(id);

            token.setCirculatingSupply(circulatingSupply);
            token.setUpdatedAt(LocalDateTime.now());
            return tokenRepository.update(token);
        } catch (ElementNotFoundException e) {
            return null;
        }
    }

}
