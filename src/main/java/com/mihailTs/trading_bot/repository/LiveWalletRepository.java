package com.mihailTs.trading_bot.repository;

import com.mihailTs.trading_bot.config.DatabaseConfig;
import com.mihailTs.trading_bot.model.Wallet;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class LiveWalletRepository {

    private DatabaseConfig databaseConfig;

    public LiveWalletRepository(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
    }

    public List<Wallet> findAll() {
        ArrayList<Wallet> wallets = new ArrayList<>();
        String sql = "SELECT * FROM \"live-wallet\"";

        try (PreparedStatement stmt = databaseConfig.connection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Wallet wallet = new Wallet(
                        rs.getString("currency"),
                        rs.getBigDecimal("total"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getTimestamp("updated_at").toLocalDateTime()
                );
                wallets.add(wallet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return wallets;
    }

    public Wallet findByCurrency(String currency) {
        String sql = "SELECT * FROM \"live-wallet\" WHERE currency = ?";

        try (PreparedStatement stmt = databaseConfig.connection().prepareStatement(sql)) {
            stmt.setString(1, currency);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                return new Wallet(
                        rs.getString("currency"),
                        rs.getBigDecimal("total"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getTimestamp("updated_at").toLocalDateTime()
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Database error while fetching wallet", e);
        }
    }

    public void insert(Wallet wallet) throws SQLException {
        String sql = "INSERT INTO \"live-wallet\" (currency, total, created_at, updated_at) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = databaseConfig.connection().prepareStatement(sql)) {
            stmt.setString(1, wallet.getCurrency());
            stmt.setDouble(2, 0);
            stmt.setTimestamp(3, Timestamp.valueOf(wallet.getCreatedAt()));
            stmt.setTimestamp(4, Timestamp.valueOf(wallet.getUpdatedAt()));
            stmt.executeUpdate();
        }
    }

    public void updateWallet(Wallet wallet, BigDecimal amount) {
        String sql = "UPDATE \"live-wallet\" SET total = ?, updated_at = ? WHERE currency = ?";

        try (PreparedStatement stmt = databaseConfig.connection().prepareStatement(sql)) {
            stmt.setBigDecimal(1, wallet.getTotal().add(amount));
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(3, wallet.getCurrency());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
