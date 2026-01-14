package com.mihailTs.trading_bot.exception;

public class IllegalTransactionTimeException extends RuntimeException{
    public IllegalTransactionTimeException(String message){
        super(message);
    }
}
