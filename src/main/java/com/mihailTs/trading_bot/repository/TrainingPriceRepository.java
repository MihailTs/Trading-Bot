package com.mihailTs.trading_bot.repository;

import com.mihailTs.trading_bot.exception.ElementNotFoundException;
import com.mihailTs.trading_bot.model.TrainingPrice;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.UUID;

public class TrainingPriceRepository {
    private Connection connection;

    public TrainingPriceRepository(Connection connection) {
        setConnection(connection);
    }

    public ArrayList<TrainingPrice> findAll() {
        ArrayList<TrainingPrice> prices = new ArrayList<>();
        String sql = "SELECT * FROM \"training-price\"";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                TrainingPrice price = new TrainingPrice(
                        (UUID) rs.getObject("id"),
                        rs.getInt("token_id"),
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

    public TrainingPrice findById(UUID id) {
        String sql = "SELECT * FROM \"training-price\" WHERE id = ?";
        TrainingPrice price = null;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, id);  // Safe UUID parameter

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.isBeforeFirst()) {
                    throw new ElementNotFoundException(
                            String.format("No token with id %s was found", id)
                    );
                }

                if (rs.next()) {
                    price = new TrainingPrice(
                            (UUID) rs.getObject("id"),
                            rs.getInt("token_id"),
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

    public void insert(TrainingPrice price) {
        String sql = "INSERT INTO \"training-price\" (id, token_id, price, created_at) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, price.getId());
            stmt.setObject(2, price.getTokenId());
            stmt.setBigDecimal(3, price.getPrice());
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
