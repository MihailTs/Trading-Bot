package com.mihailTs.trading_bot.service;

import com.mihailTs.trading_bot.model.Token;
import com.mihailTs.trading_bot.repository.TokenRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TokenService {

    private TokenRepository tokenRepository;

    public TokenService(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    public List<Token> getAll() {
        return tokenRepository.findAll();
    }



}
