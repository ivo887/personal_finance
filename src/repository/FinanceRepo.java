package repository;

import config.DatabaseConfig;






import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FinanceRepo {

    public void deleteTransaction(int transactionId, int userId) {
        String hideSql = "UPDATE transactions SET is_deleted = TRUE WHERE transaction_id = ? AND user_id = ?";

        // Recalculate exact user balance
        // Keep users balance synced
        String syncBalanceSql = """
            UPDATE users SET balance = (
                SELECT 
                    COALESCE(SUM(CASE WHEN tt.type_name = 'INCOME' THEN t.amount ELSE 0 END), 0) - 
                    COALESCE(SUM(CASE WHEN tt.type_name = 'EXPENSE' THEN t.amount ELSE 0 END), 0)
                FROM transactions t
                JOIN transaction_types tt ON t.type_id = tt.type_id
                WHERE t.user_id = ? AND t.is_deleted = FALSE
            ) WHERE user_id = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false); // Atomic DB update

            try (PreparedStatement hideStmt = conn.prepareStatement(hideSql);
                 PreparedStatement syncStmt = conn.prepareStatement(syncBalanceSql)) {

                // Hide selected transaction
                hideStmt.setInt(1, transactionId);
                hideStmt.setInt(2, userId);
                hideStmt.executeUpdate();

                // Sync cached balance
                syncStmt.setInt(1, userId);
                syncStmt.setInt(2, userId);
                syncStmt.executeUpdate();

                conn.commit();
                System.out.println("Transaction soft-deleted and balance synced.");
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Delete failed: " + e.getMessage());
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("DB Connection Error: " + e.getMessage());
        }
    }

    /**
     * Return user ID
     */
    public int loginUser(String username, String password) {
        String sql = "SELECT user_id FROM users WHERE username = ? AND password_hash = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password); // In a real app, hash this first!

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt("user_id");
            }
        } catch (SQLException e) {
            System.err.println("Login Error: " + e.getMessage());
        }
        return -1; // Invalid credentials
    }

    /**
     * Build table history
     */
    public Object[][] getTransactionHistory(int userId) {
        String sql = """
            SELECT t.transaction_id, t.transaction_date, tt.type_name, t.amount, t.description 
            FROM transactions t
            JOIN transaction_types tt ON t.type_id = tt.type_id
            WHERE t.user_id = ? AND t.is_deleted = FALSE
            ORDER BY t.transaction_date DESC
            LIMIT 50
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                rs.last();
                int rowCount = rs.getRow();
                rs.beforeFirst();

                // Five table columns
                Object[][] data = new Object[rowCount][5];
                int i = 0;
                while (rs.next()) {
                    data[i][0] = rs.getInt("transaction_id"); // Internal transaction ID
                    data[i][1] = rs.getTimestamp("transaction_date").toString().substring(0, 16);
                    data[i][2] = rs.getString("type_name");
                    data[i][3] = "$" + rs.getBigDecimal("amount");
                    data[i][4] = rs.getString("description");
                    i++;
                }
                return data;
            }
        } catch (SQLException e) {
            System.err.println(" History Error: " + e.getMessage());
            return new Object[0][0];
        }
    }


    /**
     * Create new user
     */
    public boolean registerUser(String username, String email, String hashedPassword) {
        String sql = "INSERT INTO users (username, email, password_hash) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, email);
            pstmt.setString(3, hashedPassword);
            pstmt.executeUpdate();
            return true; // Insert succeeded

        } catch (SQLException e) {
            System.err.println("Registration Error: " + e.getMessage());
            return false; // Insert failed
        }
    }
    /**
     * Compute net balance
     */
    public BigDecimal getUserBalance(int userId) {
        String sql = """
            SELECT 
                COALESCE(SUM(CASE WHEN tt.type_name = 'INCOME' THEN t.amount ELSE 0 END), 0) - 
                COALESCE(SUM(CASE WHEN tt.type_name = 'EXPENSE' THEN t.amount ELSE 0 END), 0) AS net_balance
            FROM transactions t
            JOIN transaction_types tt ON t.type_id = tt.type_id
            WHERE t.user_id = ? AND t.is_deleted = FALSE
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getBigDecimal("net_balance");
            }
        } catch (SQLException e) {
            System.err.println(" Balance Error: " + e.getMessage());
        }
        return BigDecimal.ZERO;
    }



    public void recordTransaction(int userId, BigDecimal amount, String typeName, String description) {
        String typeQuery = "SELECT type_id FROM transaction_types WHERE type_name = ?";
        String insertQuery = "INSERT INTO transactions (user_id, type_id, amount, description) VALUES (?, ?, ?, ?)";

        // Update cached balance
        String updateBalanceQuery = "UPDATE users SET balance = balance + ? WHERE user_id = ?";

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false); // Atomic balance update

            try {
                // Resolve type ID
                int typeId = -1;
                try (PreparedStatement typeStmt = conn.prepareStatement(typeQuery)) {
                    typeStmt.setString(1, typeName);
                    try (ResultSet rs = typeStmt.executeQuery()) {
                        if (rs.next()) typeId = rs.getInt("type_id");
                        else throw new SQLException("Unknown type: " + typeName);
                    }
                }

                // Insert ledger row
                try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                    insertStmt.setInt(1, userId);
                    insertStmt.setInt(2, typeId);
                    insertStmt.setBigDecimal(3, amount);
                    insertStmt.setString(4, description);
                    insertStmt.executeUpdate();
                }

                // Update total balance
                // Negate expense amounts
                BigDecimal balanceChange = typeName.equals("EXPENSE") ? amount.negate() : amount;

                try (PreparedStatement updateStmt = conn.prepareStatement(updateBalanceQuery)) {
                    updateStmt.setBigDecimal(1, balanceChange);
                    updateStmt.setInt(2, userId);
                    updateStmt.executeUpdate();
                }

                // Commit all changes
                conn.commit();
                System.out.println("Transaction recorded and user balance updated: $" + amount + " (" + typeName + ")");

            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Process failed and rolled back: " + e.getMessage());
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
        }
    }
}