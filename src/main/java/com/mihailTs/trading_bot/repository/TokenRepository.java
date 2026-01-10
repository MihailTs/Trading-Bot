package com.mihailTs.trading_bot.repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
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
                        rs.getInt("id"),
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

    public ArrayList<Integer> findAllIDs() {
        ArrayList<Integer> ids = new ArrayList<>();
        String sql = "SELECT * FROM token";

        try (PreparedStatement stmt = databaseConfig.connection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ids.add(rs.getInt("id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ids;
    }

    public Token findById(int id) throws ElementNotFoundException {
        String sql = "SELECT * FROM token WHERE id = ?";
        try (PreparedStatement stmt = databaseConfig.connection().prepareStatement(sql)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new ElementNotFoundException(
                            String.format("No token with id %d was found", id)
                    );
                }

                return new Token(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("ticker"),
                        rs.getBigDecimal("circulating_supply"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getTimestamp("updated_at").toLocalDateTime()
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error while fetching token", e);
        }
    }

    public void insert(Token token) {
        String sql = "INSERT INTO token (id, name, circulating_supply, ticker, created_at, updated_at) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = databaseConfig.connection().prepareStatement(sql)) {
            stmt.setObject(1, token.getId());
            stmt.setString(2, token.getName());
            stmt.setBigDecimal(3, token.getCirculatingSupply());
            stmt.setString(4, token.getTicker());
            stmt.setTimestamp(5, Timestamp.valueOf(token.getCreatedAt()));
            stmt.setTimestamp(6, Timestamp.valueOf(token.getCreatedAt()));
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Token update(Token token) {
        String sql = "UPDATE token SET name = ?, ticker = ?, circulating_supply = ?, updated_at = ? WHERE id = ?";

        try (PreparedStatement stmt = databaseConfig.connection().prepareStatement(sql)) {
            stmt.setString(1, token.getName());
            stmt.setString(2, token.getTicker());
            stmt.setBigDecimal(3, token.getCirculatingSupply());
            stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setObject(5, token.getId());

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated == 0) {
                throw new ElementNotFoundException(
                        String.format("No token with id %s was found to update.", token.getId())
                );
            }
            return token;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
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
