package com.mihailTs.trading_bot.repository;

import com.mihailTs.trading_bot.config.DatabaseConfig;
import com.mihailTs.trading_bot.exception.ElementNotFoundException;
import com.mihailTs.trading_bot.model.LivePrice;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class LivePriceRepository {
    private final DatabaseConfig databaseConfig;
    public LivePriceRepository(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
    }

    public ArrayList<LivePrice> findAll() {
        ArrayList<LivePrice> prices = new ArrayList<>();
        String sql = "SELECT * FROM \"live-price\"";

        try (PreparedStatement stmt = databaseConfig.connection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                LivePrice price = new LivePrice(
                        (UUID) rs.getObject("id"),
                        rs.getString("token_id"),
                        rs.getBigDecimal("price"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                );
                prices.add(price);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return prices;
    }

    public LivePrice findById(UUID id) {
        String sql = "SELECT * FROM \"live-price\" WHERE id = ?";
        LivePrice price = null;

        try (PreparedStatement stmt = databaseConfig.connection().prepareStatement(sql)) {
            stmt.setObject(1, id);  // Safe UUID parameter

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.isBeforeFirst()) {
                    throw new ElementNotFoundException(
                            String.format("No token with id %s was found", id)
                    );
                }

                if (rs.next()) {
                    price = new LivePrice(
                            (UUID) rs.getObject("id"),
                            rs.getString("token_id"),
                            rs.getBigDecimal("price"),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    );
                }
            }
        } catch (SQLException | ElementNotFoundException e) {
            e.printStackTrace();
        }

        return price;
    }

    public LivePrice insert(LivePrice price) {
        String sql = "INSERT INTO \"live-price\" (id, token_id, price, created_at) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = databaseConfig.connection().prepareStatement(sql)) {
            stmt.setObject(1, price.getId());
            stmt.setString(2, price.getTokenId());
            stmt.setBigDecimal(3, price.getPrice());
            stmt.setTimestamp(4, Timestamp.valueOf(price.getCreatedAt()));

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return price;
    }

    public LivePrice getPriceByTokenId(String tokenId) {
        String sql = "SELECT * FROM \"live-price\" WHERE token_id = ? ORDER BY created_at DESC LIMIT 1";
        LivePrice price = null;

        try (PreparedStatement stmt = databaseConfig.connection().prepareStatement(sql)) {
            stmt.setString(1, tokenId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.isBeforeFirst()) {
                    throw new ElementNotFoundException(
                            String.format("No token with id %s was found", tokenId)
                    );
                }

                if (rs.next()) {
                    price = new LivePrice(
                            (UUID) rs.getObject("id"),
                            rs.getString("token_id"),
                            rs.getBigDecimal("price"),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return price;
    }

    public List<LivePrice> getPriceHistoryForDays(String tokenId, int days) {
        String sql = "SELECT * FROM \"live-price\" " +
                "WHERE token_id = ? AND created_at > NOW() - INTERVAL '1 day' * ? ORDER BY created_at ASC";
        List<LivePrice> prices = new ArrayList<>();

        try (PreparedStatement stmt = databaseConfig.connection().prepareStatement(sql)) {
            stmt.setString(1, tokenId);
            stmt.setInt(2, days);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LivePrice price = new LivePrice(
                            (UUID) rs.getObject("id"),
                            rs.getString("token_id"),
                            rs.getBigDecimal("price"),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    );
                    prices.add(price);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return prices;
    }

    public List<LivePrice> getLatest(String tokenId, int limit) {
        String sql = "SELECT * FROM \"live-price\" WHERE token_id = ? ORDER BY created_at DESC LIMIT ?";
        List<LivePrice> prices = new ArrayList<>();

        try (PreparedStatement stmt = databaseConfig.connection().prepareStatement(sql)) {
            stmt.setString(1, tokenId);
            stmt.setInt(2, limit);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    prices.add(new LivePrice(
                            (UUID) rs.getObject("id"),
                            rs.getString("token_id"),
                            rs.getBigDecimal("price"),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return prices;
    }

}
