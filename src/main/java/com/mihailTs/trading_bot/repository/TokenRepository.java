package com.mihailTs.trading_bot.repository;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import com.mihailTs.trading_bot.model.Token;

public class TokenRepository {

    private Connection connection;

    public TokenRepository(Connection connection) {
        setConnection(connection);
    }

    public ArrayList<Token> findAll() {
        ArrayList<Token> tokens = new ArrayList<>();
        String sql = "SELECT * FROM token";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
          while (rs.next()) {
            Token token = new Token(
                    (java.util.UUID) rs.getObject("id"),
                    rs.getString("name"),
                    rs.getString("ticker"),
                    rs.getTimestamp("created_at").toLocalDateTime()
            );
            tokens.add(token);
          }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tokens;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public Connection getConnection() {
        return connection;
    }

}
