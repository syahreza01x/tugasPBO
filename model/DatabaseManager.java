package model;

import java.sql.*;

public class DatabaseManager {
    private Connection conn;

    public void connect() {
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/game_db", "root", "");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public int getPlayerSkill(int playerId) {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT skill FROM players WHERE id=?")) {
            ps.setInt(1, playerId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("skill");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getSkillCooldown(int skillId) {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT cooldown FROM skills WHERE id=?")) {
            ps.setInt(1, skillId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("cooldown");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 15000;
    }

    public String getSkillSound(int skillId) {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT sounds FROM skills WHERE id=?")) {
            ps.setInt(1, skillId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("sounds");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "skill1.wav";
    }

    public void createTables() {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS players (id INT PRIMARY KEY AUTO_INCREMENT, username VARCHAR(50), password VARCHAR(255), created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            stmt.execute("CREATE TABLE IF NOT EXISTS scores (id INT PRIMARY KEY AUTO_INCREMENT, player_id INT, score INT, match_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            stmt.execute("CREATE TABLE IF NOT EXISTS powerups_log (id INT PRIMARY KEY AUTO_INCREMENT, player_id INT, powerup_type VARCHAR(50), collected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            stmt.execute("CREATE TABLE IF NOT EXISTS skills (id INT PRIMARY KEY AUTO_INCREMENT, nama_skill VARCHAR(50), penjelasan VARCHAR(255), cooldown INT, sounds VARCHAR(100))");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void insertScore(int playerId, int score) {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO scores(player_id, score, match_date) VALUES (?, ?, NOW())")) {
            ps.setInt(1, playerId);
            ps.setInt(2, score);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveHighScore(int playerId, int score) {
        int currentHigh = getHighScore(playerId);
        if (score > currentHigh) {
            deleteScoresByPlayer(playerId);
            insertScore(playerId, score);
        }
    }

    public int login(String username, String password) {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id FROM players WHERE username=? AND password=?")) {
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int getHighScore(int playerId) {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT MAX(score) as hs FROM scores WHERE player_id=?")) {
            ps.setInt(1, playerId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("hs");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void deleteScoresByPlayer(int playerId) {
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM scores WHERE player_id=?")) {
            ps.setInt(1, playerId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void logPowerup(int playerId, String type) {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO powerups_log(player_id, powerup_type, collected_at) VALUES (?, ?, NOW())")) {
            ps.setInt(1, playerId);
            ps.setString(2, type);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}