package com.mihailTs.trading_bot.repository;

import com.mihailTs.trading_bot.exception.ElementNotFoundException;
import com.mihailTs.trading_bot.model.TrainingTransaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.UUID;

public class TrainingTransactionRepository {
    private Connection connection;

    public TrainingTransactionRepository(Connection connection) {
        setConnection(connection);
    }

    public ArrayList<TrainingTransaction> findAll() {
        ArrayList<TrainingTransaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM \"training-transaction\"";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                TrainingTransaction transaction = new TrainingTransaction(
                        (UUID) rs.getObject("id"),
                        rs.getDouble("quantity"),
                        (UUID) rs.getObject("price_id"),
                        rs.getString("type"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                );
                transactions.add(transaction);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return transactions;
    }

    public TrainingTransaction findById(UUID id) {
        String sql = "SELECT * FROM \"training-transaction\" WHERE id = ?";
        TrainingTransaction transaction = null;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.isBeforeFirst()) {
                    throw new ElementNotFoundException(
                            String.format("No token with id %s was found", id)
                    );
                }

                if (rs.next()) {
                    transaction = new TrainingTransaction(
                            (UUID) rs.getObject("id"),
                            rs.getDouble("quantity"),
                            (UUID) rs.getObject("price_id"),
                            rs.getString("type"),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    );
                }
            }
        } catch (SQLException | ElementNotFoundException e) {
            e.printStackTrace();
        }

        return transaction;
    }

    public void insert(TrainingTransaction transaction) {
        String sql = "INSERT INTO \"training-transaction\" (id, quantity, price_id, type, created_at) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, transaction.getId());
            stmt.setDouble(2, transaction.getQuantity());
            stmt.setObject(3, transaction.getPriceId());
            stmt.setString(4, transaction.getType());
            stmt.setTimestamp(5, Timestamp.valueOf(transaction.getCreatedAt()));

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
