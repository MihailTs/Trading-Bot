package com.mihailTs.trading_bot.repository;

import com.mihailTs.trading_bot.exception.ElementNotFoundException;
import com.mihailTs.trading_bot.model.LiveTransaction;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class LiveTransactionRepository {

    private Connection connection;

    public LiveTransactionRepository(Connection connection) {
        setConnection(connection);
    }

    public List<LiveTransaction> findAll() {
        ArrayList<LiveTransaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM \"live-transaction\"";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                LiveTransaction transaction = new LiveTransaction(
                        (UUID) rs.getObject("id"),
                        rs.getBigDecimal("quantity"),
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

    public List<LiveTransaction> findLast(int limit) {
        ArrayList<LiveTransaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM \"live-transaction\" ORDER BY created_at DESC LIMIT ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, limit);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LiveTransaction transaction = new LiveTransaction(
                            (UUID) rs.getObject("id"),
                            rs.getBigDecimal("quantity"),
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
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public LiveTransaction findById(UUID id) {
        String sql = "SELECT * FROM \"live-transaction\" WHERE id = ?";
        LiveTransaction transaction = null;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.isBeforeFirst()) {
                    throw new ElementNotFoundException(
                            String.format("No token with id %s was found", id)
                    );
                }

                if (rs.next()) {
                    transaction = new LiveTransaction(
                            (UUID) rs.getObject("id"),
                            rs.getBigDecimal("quantity"),
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

    public void insert(LiveTransaction transaction) {
        String sql = "INSERT INTO \"live-transaction\" (id, quantity, price_id, type, created_at) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, transaction.getId());
            stmt.setBigDecimal(2, transaction.getQuantity());
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
