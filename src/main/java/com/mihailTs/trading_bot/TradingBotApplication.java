package com.mihailTs.trading_bot;

import com.mihailTs.trading_bot.repository.TokenRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@SpringBootApplication
public class TradingBotApplication {

	public static void main(String[] args) throws SQLException {
		SpringApplication.run(TradingBotApplication.class, args);
	}

}
