package com.mihailTs.trading_bot.repository;

import com.mihailTs.trading_bot.config.DatabaseConfig;
import com.mihailTs.trading_bot.exception.ElementNotFoundException;
import com.mihailTs.trading_bot.model.LiveAsset;
import com.mihailTs.trading_bot.model.Token;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

@Repository
public class LiveAssetRepository {

    private DatabaseConfig databaseConfig;
    public LiveAssetRepository(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
    }

    public ArrayList<LiveAsset> findAll() {
        ArrayList<LiveAsset> assets = new ArrayList<>();
        String sql = "SELECT * FROM \"live-asset\"";

        try (PreparedStatement stmt = databaseConfig.connection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                LiveAsset asset = new LiveAsset(
                        rs.getString("token_id"),
                        rs.getBigDecimal("quantity"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getTimestamp("updated_at").toLocalDateTime()
                        );
                assets.add(asset);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return assets;
    }

    public LiveAsset findByTokenId(String tokenId) throws ElementNotFoundException{
        String sql = "SELECT * FROM \"live-asset\" WHERE token_id = ?";
        LiveAsset asset = null;

        try (PreparedStatement stmt = databaseConfig.connection().prepareStatement(sql)) {
            stmt.setObject(1, tokenId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.isBeforeFirst()) {
                    throw new ElementNotFoundException(
                            String.format("No asset with tokenId %s was found", tokenId)
                    );
                }

                if (rs.next()) {
                    asset = new LiveAsset(
                            rs.getString("token_id"),
                            rs.getBigDecimal("quantity"),
                            rs.getTimestamp("created_at").toLocalDateTime(),
                            rs.getTimestamp("updated_at").toLocalDateTime()
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return asset;
    }

    public void insert(LiveAsset asset) {
        String sql = "INSERT INTO \"live-asset\" (token_id, quantity, created_at, updated_at) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = databaseConfig.connection().prepareStatement(sql)) {
            stmt.setString(1, asset.getTokenId());
            stmt.setBigDecimal(2, asset.getQuantity());
            stmt.setTimestamp(3, Timestamp.valueOf(asset.getCreatedAt()));
            stmt.setTimestamp(4, Timestamp.valueOf(asset.getUpdatedAt()));

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void update(String tokenId, double quantity, Timestamp updatedAt) {
        String sql = "UPDATE \"live-asset\" SET quantity = ?, updated_at = ? WHERE token_id = ?";

        try (PreparedStatement stmt = databaseConfig.connection().prepareStatement(sql)) {
            stmt.setDouble(1, quantity);
            stmt.setTimestamp(2, updatedAt);
            stmt.setObject(3, tokenId);

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated == 0) {
                throw new ElementNotFoundException(String.format("No asset with token_id %s was found to update.", tokenId));
            }
        } catch (SQLException | ElementNotFoundException e) {
            e.printStackTrace();
        }
    }

    public int delete(String tokenId) {
        String sql = "DELETE FROM \"live-asset\" WHERE token_id = ?";
        int rowsDeleted = 0;

        try (PreparedStatement stmt = databaseConfig.connection().prepareStatement(sql)) {
            stmt.setObject(1, tokenId);

            rowsDeleted = stmt.executeUpdate();
            if (rowsDeleted == 0) {
                throw new ElementNotFoundException(String.format("No token with id %s was found to delete.", tokenId));
            }
        } catch (SQLException | ElementNotFoundException e) {
            e.printStackTrace();
        }

        return rowsDeleted;
    }

    public void updateQuantity(String tokenId, BigDecimal quantity) {
        String sql = "UPDATE \"live-asset\" SET quantity = ?, updated_at = ? WHERE token_id = ?";

        try (PreparedStatement stmt = databaseConfig.connection().prepareStatement(sql)) {
            stmt.setBigDecimal(1, quantity);
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(3, tokenId);

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated == 0) {
                throw new ElementNotFoundException(
                        String.format("No asset with token_id %s was found to update.", tokenId)
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
