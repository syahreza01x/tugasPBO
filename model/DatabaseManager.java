package model;

import java.sql.*;
import java.util.*;

public class DatabaseManager {
    private Connection conn;

    public void connect() {
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/game_db", "root", "");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static class Player {
        public int id;
        public String username;
        public int skillId;
        public Player(int id, String username, int skillId) {
            this.id = id;
            this.username = username;
            this.skillId = skillId;
        }
    }

    public boolean isPasswordValid(String username, String password) {
    try (PreparedStatement ps = conn.prepareStatement("SELECT password FROM players WHERE username=?")) {
        ps.setString(1, username);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            String dbPass = rs.getString("password");
            return dbPass.equals(password);
        }
        } catch (SQLException e) {
        e.printStackTrace();
        }
        return false;
    }

    public static class Skill {
        public int id;
        public String namaSkill;
        public Skill(int id, String namaSkill) {
            this.id = id;
            this.namaSkill = namaSkill;
        }
    }

    public boolean isUsernameExist(String username) {
        try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM players WHERE username=?")) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public List<Map<String, Object>> getTop5HighScores() {
    List<Map<String, Object>> list = new ArrayList<>();
    String sql = """
        SELECT p.username, MAX(s.score) AS score
        FROM scores s
        JOIN players p ON s.player_id = p.id
        GROUP BY s.player_id
        ORDER BY score DESC
        LIMIT 5
        """;
    try (Statement st = conn.createStatement();
         ResultSet rs = st.executeQuery(sql)) {
        while (rs.next()) {
            Map<String, Object> row = new HashMap<>();
            row.put("username", rs.getString("username"));
            row.put("score", rs.getInt("score"));
            list.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void updatePlayer(int playerId, String username, int skillId) {
    try (PreparedStatement ps = conn.prepareStatement(
            "UPDATE players SET username=?, skill=? WHERE id=?")) {
        ps.setString(1, username);
        ps.setInt(2, skillId);
        ps.setInt(3, playerId);
        ps.executeUpdate();
        } catch (SQLException e) {
        e.printStackTrace();
        }
    }

    public void deletePlayer(int playerId) {
    try (PreparedStatement ps = conn.prepareStatement(
            "DELETE FROM players WHERE id=?")) {
        ps.setInt(1, playerId);
        ps.executeUpdate();
        } catch (SQLException e) {
        e.printStackTrace();
        }
    }

    public void registerPlayer(String username, String password, int skillId) {
        try (PreparedStatement ps = conn.prepareStatement(
            "INSERT INTO players(username, password, skill) VALUES (?, ?, ?)")) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setInt(3, skillId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public List<Player> getAllPlayers() {
        List<Player> list = new ArrayList<>();
        try (Statement st = conn.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT id, username, skill FROM players");
            while (rs.next()) {
                list.add(new Player(rs.getInt("id"), rs.getString("username"), rs.getInt("skill")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Skill> getAllSkills() {
        List<Skill> list = new ArrayList<>();
        try (Statement st = conn.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT id, nama_skill FROM skills");
            while (rs.next()) {
                list.add(new Skill(rs.getInt("id"), rs.getString("nama_skill")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
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