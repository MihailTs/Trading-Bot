package com.mihailTs.trading_bot.exception;

public class NotEnoughAssetException extends RuntimeException{
    public NotEnoughAssetException(String message){
        super(message);
    }
}
