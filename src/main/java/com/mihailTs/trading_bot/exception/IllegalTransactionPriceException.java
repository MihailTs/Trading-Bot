package com.mihailTs.trading_bot.exception;

public class IllegalTransactionPriceException extends RuntimeException{
    public IllegalTransactionPriceException(String message){
        super(message);
    }
}
