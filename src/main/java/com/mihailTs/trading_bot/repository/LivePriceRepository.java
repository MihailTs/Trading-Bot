package com.mihailTs.trading_bot.repository;

import com.mihailTs.trading_bot.exception.ElementNotFoundException;
import com.mihailTs.trading_bot.model.LiveAsset;
import com.mihailTs.trading_bot.model.LivePrice;
import com.mihailTs.trading_bot.model.Token;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.UUID;

public class LivePriceRepository {
    private Connection connection;

    public LivePriceRepository(Connection connection) {
        setConnection(connection);
    }

    public ArrayList<LivePrice> findAll() {
        ArrayList<LivePrice> prices = new ArrayList<>();
        String sql = "SELECT * FROM \"live-price\"";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                LivePrice price = new LivePrice(
                        (UUID) rs.getObject("id"),
                        (UUID) rs.getObject("token_id"),
                        rs.getDouble("price"),
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

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
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
                            (UUID) rs.getObject("token_id"),
                            rs.getDouble("price"),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    );
                }
            }
        } catch (SQLException | ElementNotFoundException e) {
            e.printStackTrace();
        }

        return price;
    }

    public void insert(LivePrice price) {
        String sql = "INSERT INTO \"live-price\" (id, token_id, price, created_at) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, price.getId());
            stmt.setObject(2, price.getTokenId());
            stmt.setDouble(3, price.getPrice());
            stmt.setTimestamp(4, Timestamp.valueOf(price.getCreatedAt()));

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public Connection getConnection() {
        return connection;
    }

}
