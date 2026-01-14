package com.mihailTs.trading_bot.exception;

public class NotEnoughMoneyException extends RuntimeException{
    public NotEnoughMoneyException(String message){
        super(message);
    }
}
