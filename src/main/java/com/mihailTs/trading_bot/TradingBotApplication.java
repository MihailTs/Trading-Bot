package com.mihailTs.trading_bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.sql.SQLException;

@SpringBootApplication
@EnableScheduling
public class TradingBotApplication {

	public static void main(String[] args) throws SQLException {
		SpringApplication.run(TradingBotApplication.class, args);
	}

}
