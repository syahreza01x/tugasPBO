package model;

import java.sql.*;
import java.util.*;

public class DatabaseManager {
    private Connection conn;

    public void connect() {
        try {
            // Koneksi tanpa database dulu, agar bisa CREATE DATABASE jika belum ada
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/?serverTimezone=UTC", "root", "");
            initDatabase();
            // Setelah database pasti ada, konek ulang ke game_db
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/game_db?serverTimezone=UTC", "root", "");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // Inisialisasi database dan tabel jika belum ada
    public void initDatabase() {
        try (Statement st = conn.createStatement()) {
            // 1. Buat database jika belum ada
            st.executeUpdate("CREATE DATABASE IF NOT EXISTS game_db DEFAULT CHARACTER SET utf8mb4 COLLATE=utf8mb4_0900_ai_ci");
            st.executeUpdate("USE game_db");

            // 2. Buat tabel skills
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS skills (
                    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                    nama_skill VARCHAR(50) DEFAULT NULL,
                    penjelasan TEXT,
                    cooldown INT DEFAULT NULL,
                    sounds VARCHAR(100) DEFAULT NULL
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """);

            // 3. Buat tabel players
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS players (
                    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                    username VARCHAR(50) NOT NULL UNIQUE,
                    password VARCHAR(255) DEFAULT '',
                    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                    skill INT DEFAULT NULL,
                    KEY fk_skill (skill),
                    CONSTRAINT fk_skill FOREIGN KEY (skill) REFERENCES skills(id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """);

            // 4. Buat tabel scores
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS scores (
                    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                    player_id INT DEFAULT NULL,
                    score INT NOT NULL,
                    match_date TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                    KEY player_id (player_id),
                    CONSTRAINT scores_ibfk_1 FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """);

            // 5. Buat tabel powerup
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS powerup (
                    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                    namapowerup VARCHAR(50) DEFAULT NULL,
                    keterangan TEXT
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """);

            // 6. Buat tabel powerups_log
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS powerups_log (
                    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                    player_id INT DEFAULT NULL,
                    powerup_type VARCHAR(50) DEFAULT NULL,
                    collected_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                    powerup_id INT DEFAULT NULL,
                    KEY player_id (player_id),
                    KEY fk_powerup (powerup_id),
                    CONSTRAINT fk_powerup FOREIGN KEY (powerup_id) REFERENCES powerup(id),
                    CONSTRAINT powerups_log_ibfk_1 FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """);

            // 7. Isi data awal skills jika kosong
            ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM skills");
            if (rs.next() && rs.getInt(1) == 0) {
                st.executeUpdate("""
                    INSERT INTO skills (id, nama_skill, penjelasan, cooldown, sounds) VALUES
                    (1, 'The World', 'Menghentikan waktu sementara', 15000, 'skill1.wav'),
                    (2, 'Star Platinum', 'Menghapus peluru di sekitar karakter', 20000, 'skill2.wav'),
                    (3, 'Made in Heaven', 'Menghentikan waktu 10 detik, skor lawan perlahan turun', 30000, 'skill3.wav'),
                    (4, 'Crazy Diamond', 'Menambah 1 health ke player', 35000, 'skill4.wav'),
                    (5, 'The Hand', 'Menembakkan laser ke arah depan, menghancurkan peluru dalam area selebar 3 kotak', 30000, 'skill5.wav'),
                    (6, 'King Crimson', 'Meningkatkan kecepatan gerak pengguna dan memperlambat lawan selama beberapa detik', 25000, 'skill6.wav'),
                    (7, 'Silver Chariot (Requiem)', 'Memanggil makhluk summon yang secara otomatis mengejar dan menyerang lawan selama 10 detik', 120000, 'skill7.wav'),
                    (8, 'Gold Experience (Requiem)', 'Mengaktifkan time loop: lawan menerima damage terus menerus selama 10 detik dan HP tersisa 1 setelah selesai', 120000, 'skill8.wav')
                """);
            }

            // 8. Isi data awal powerup jika kosong
            rs = st.executeQuery("SELECT COUNT(*) FROM powerup");
            if (rs.next() && rs.getInt(1) == 0) {
                st.executeUpdate("""
                    INSERT INTO powerup (id, namapowerup, keterangan) VALUES
                    (1, 'Heart', 'Menambah nyawa'),
                    (2, 'Shield', 'Kebal sementara dari peluru'),
                    (3, 'Speed', 'Gerak cepat sementara'),
                    (4, 'Swap', 'Tukar nyawa lawan dan tambah nyawa sendiri'),
                    (5, 'Lock', 'Mengunci skill lawan sementara')
                """);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
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