package com.mihailTs.trading_bot.repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.UUID;

import com.mihailTs.trading_bot.config.DatabaseConfig;
import com.mihailTs.trading_bot.exception.ElementNotFoundException;
import com.mihailTs.trading_bot.model.Token;
import org.springframework.stereotype.Repository;

@Repository
public class TokenRepository {

    private DatabaseConfig databaseConfig;

    public TokenRepository(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
    }

    public ArrayList<Token> findAll() {
        ArrayList<Token> tokens = new ArrayList<>();
        String sql = "SELECT * FROM token";

        try (PreparedStatement stmt = databaseConfig.connection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Token token = new Token(
                        (UUID) rs.getObject("id"),
                        rs.getString("name"),
                        rs.getString("ticker"),
                        rs.getBigDecimal("circulatingSupply"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getTimestamp("updated_at").toLocalDateTime()
                );
                tokens.add(token);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tokens;
    }

    public Token findById(UUID id) {
        String sql = "SELECT * FROM token WHERE id = ?";
        Token token = null;

        try (PreparedStatement stmt = databaseConfig.connection().prepareStatement(sql)) {
            stmt.setObject(1, id);  // Safe UUID parameter

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.isBeforeFirst()) {
                    throw new ElementNotFoundException(
                            String.format("No token with id %s was found", id)
                    );
                }

                if (rs.next()) {
                    token = new Token(
                            (UUID) rs.getObject("id"),
                            rs.getString("name"),
                            rs.getString("ticker"),
                            rs.getBigDecimal("circulatingSupply"),
                            rs.getTimestamp("created_at").toLocalDateTime(),
                            rs.getTimestamp("updated_at").toLocalDateTime()
                    );
                }
            }
        } catch (SQLException | ElementNotFoundException e) {
            e.printStackTrace();
        }

        return token;
    }

    public void insert(Token token) {
        String sql = "INSERT INTO token (id, name, ticker, created_at) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = databaseConfig.connection().prepareStatement(sql)) {
            stmt.setObject(1, token.getId());
            stmt.setString(2, token.getName());
            stmt.setString(3, token.getTicker());
            stmt.setTimestamp(4, Timestamp.valueOf(token.getCreatedAt()));

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void update(Token token) {
        String sql = "UPDATE token SET name = ?, ticker = ? WHERE id = ?";

        try (PreparedStatement stmt = databaseConfig.connection().prepareStatement(sql)) {
            stmt.setString(1, token.getName());
            stmt.setString(2, token.getTicker());
            stmt.setObject(3, token.getId());

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated == 0) {
                throw new ElementNotFoundException("No token with id " + token.getId() + " was found to update.");
            }
        } catch (SQLException | ElementNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void delete(UUID id) {
        String sql = "DELETE FROM token WHERE id = ?";

        try (PreparedStatement stmt = databaseConfig.connection().prepareStatement(sql)) {
            stmt.setObject(1, id);

            int rowsDeleted = stmt.executeUpdate();
            if (rowsDeleted == 0) {
                throw new ElementNotFoundException(String.format("No token with id %s was found to delete.", id));
            }
        } catch (SQLException | ElementNotFoundException e) {
            e.printStackTrace();
        }
    }

}
