package model;

import java.util.*;
import java.awt.event.KeyEvent;
import javax.swing.JOptionPane;
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class GameModel {
    private Clip bgmClip;

    public final int ROWS = 20, COLS = 60;
    public char[][] arena = new char[ROWS][COLS];
    public int heartX1 = ROWS - 2, heartY1 = COLS / 4;
    public int heartX2 = ROWS - 2, heartY2 = COLS - COLS / 4;

    public int score1 = 0, score2 = 0;
    public int highScore1 = 0, highScore2 = 0;
    public int lives1 = 3, lives2 = 3;
    public boolean player1Dead = false, player2Dead = false, isSinglePlayer = true;

    public boolean timeStopActive = false, areaClearActive = false;
    public long timeStopStart = 0, areaClearStart = 0;
    public long timeStopCooldownStart = -15000, areaClearCooldownStart = -20000;
    public boolean showTimeStopEffect = false, showAreaClearEffect = false;

    public boolean shield1 = false, shield2 = false;
    public long shield1Start = 0, shield2Start = 0;
    public boolean speed1 = false, speed2 = false;
    public long speed1Start = 0, speed2Start = 0;
    public boolean skillLock1 = false, skillLock2 = false;
    public long skillLock1Start = 0, skillLock2Start = 0;

    public int timeStopKey;
    public int areaClearKey;
    public static final int DEFAULT_TIME_STOP_KEY = KeyEvent.VK_E;
    public static final int DEFAULT_AREA_CLEAR_KEY = KeyEvent.VK_END;

    // === Tambahan untuk database ===
    private int player1Id, player2Id;
    private DatabaseManager db;

    public static class Drop {
        public int x, y, type;
        public char icon;
        public Drop(int x, int y, char icon, int type) {
            this.x = x; this.y = y; this.icon = icon; this.type = type;
        }
    }
    public List<Drop> drops = new ArrayList<>();
    private final Random rand = new Random();

    // === Konstruktor baru dengan database dan playerId ===
    public GameModel(boolean isSinglePlayer, int timeStopKey, int areaClearKey, int player1Id, int player2Id, DatabaseManager db) {
        this.isSinglePlayer = isSinglePlayer;
        this.timeStopKey = timeStopKey;
        this.areaClearKey = areaClearKey;
        this.player1Id = player1Id;
        this.player2Id = player2Id;
        this.db = db;
        resetGame();
        // Ambil high score dari DB
        highScore1 = db.getHighScore(player1Id);
        if (!isSinglePlayer) highScore2 = db.getHighScore(player2Id);
    }

    public void resetGame() {
        for (int i = 0; i < ROWS; i++) Arrays.fill(arena[i], ' ');
        heartX1 = ROWS - 2; heartY1 = COLS / 4;
        heartX2 = ROWS - 2; heartY2 = COLS - COLS / 4;
        score1 = 0; score2 = 0;
        lives1 = 3; lives2 = 3;
        player1Dead = false; player2Dead = false;
        timeStopActive = false; areaClearActive = false;
        timeStopCooldownStart = -15000; areaClearCooldownStart = -20000;
        shield1 = false; shield2 = false;
        speed1 = false; speed2 = false;
        skillLock1 = false; skillLock2 = false;
        drops.clear();
    }

    public void updateGame() {
        long now = System.currentTimeMillis();
        boolean timeStopWindow = timeStopActive && (now - timeStopStart < 5000);

        // Hapus karakter lama
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                if (arena[i][j] == '♥' || arena[i][j] == '♦') {
                    arena[i][j] = ' ';
                }
            }
        }

        // Power-up durations
        if (shield1 && now - shield1Start > 5000) shield1 = false;
        if (shield2 && now - shield2Start > 5000) shield2 = false;
        if (speed1 && now - speed1Start > 5000) speed1 = false;
        if (speed2 && now - speed2Start > 5000) speed2 = false;
        if (skillLock1 && now - skillLock1Start > 20000) skillLock1 = false;
        if (skillLock2 && now - skillLock2Start > 20000) skillLock2 = false;

        if (!player1Dead) score1++;
        if (!player2Dead && !isSinglePlayer && !timeStopWindow) score2++;

        showTimeStopEffect = timeStopActive && (now - timeStopStart <= 5000);
        showAreaClearEffect = areaClearActive && (now - areaClearStart <= 5000);

        if (timeStopWindow) {
            // Player 2 frozen
        } else {
            if (timeStopActive) {
                timeStopActive = false;
                timeStopCooldownStart = System.currentTimeMillis();
            }
            updateBullets();
            spawnBullets();
            spawnDrops();
            updateDrops();
        }

        // Collision & game over
        if (!player1Dead && arena[heartX1][heartY1] == '*' && !shield1) {
            lives1--;
            if (lives1 <= 0) {
                player1Dead = true;
                if (score1 > highScore1) {
                    db.deleteScoresByPlayer(player1Id); // Hapus skor lama
                    highScore1 = score1;
                    db.insertScore(player1Id, score1);
                } else {
                    db.insertScore(player1Id, score1);
                }
                JOptionPane.showMessageDialog(null, "\uD83D\uDC80 Player 1 Kehabisan nyawa! Game Over.\nScore: " + score1 + "\nHigh Score: " + highScore1);
            }
        }
        if (!player2Dead && !isSinglePlayer && arena[heartX2][heartY2] == '*' && !shield2) {
            lives2--;
            if (lives2 <= 0) {
                player2Dead = true;
                if (score2 > highScore2) {
                    db.deleteScoresByPlayer(player2Id); // Hapus skor lama
                    highScore2 = score2;
                    db.insertScore(player2Id, score2);
                } else {
                    db.insertScore(player2Id, score2);
                }
                JOptionPane.showMessageDialog(null, "\uD83D\uDC80 Player 2 Kehabisan nyawa! Game Over.\nScore: " + score2 + "\nHigh Score: " + highScore2);
            }
        }
        if ((player1Dead && (player2Dead || isSinglePlayer))) {
            JOptionPane.showMessageDialog(null, "\uD83C\uDFAE Game Over. Player mati.");
            resetGame();
        }

        if (!player1Dead) arena[heartX1][heartY1] = '♥'; else arena[heartX1][heartY1] = ' ';
        if (!player2Dead && !isSinglePlayer) arena[heartX2][heartY2] = '♦'; else arena[heartX2][heartY2] = ' ';

        if (areaClearActive && (now - areaClearStart <= 5000)) {
            clearBulletsAroundPlayer2();
        } else {
            if (areaClearActive) {
                areaClearActive = false;
                areaClearCooldownStart = System.currentTimeMillis();
            }
        }
    }

    void spawnDrops() {
        if (rand.nextInt(200) < 3) {
            int col = rand.nextInt(COLS);
            int type;
            char icon;
            int r = rand.nextInt(100);
            if (r < 30) { type = 1; icon = '+'; }
            else if (r < 55) { type = 2; icon = '⛨'; }
            else if (r < 80) { type = 3; icon = '⇶'; }
            else if (r < 90) { type = 4; icon = '⇄'; }
            else { type = 5; icon = '✖'; }
            drops.add(new Drop(0, col, icon, type));
        }
    }

    void updateDrops() {
        List<Drop> toRemove = new ArrayList<>();
        for (Drop d : drops) {
            if (rand.nextInt(2) == 0) {
                if (d.x < ROWS - 1) d.x++;
                else toRemove.add(d);
            }
            if (d.x == heartX1 && d.y == heartY1 && !player1Dead) {
                applyDropEffect(1, d.type);
                toRemove.add(d);
            } else if (!isSinglePlayer && d.x == heartX2 && d.y == heartY2 && !player2Dead) {
                applyDropEffect(2, d.type);
                toRemove.add(d);
            }
        }
        drops.removeAll(toRemove);
    }

    // === Log power-up ke database ===
    void applyDropEffect(int player, int type) {
        if (player == 1) {
            switch (type) {
                case 1 -> { if (lives1 < 5) lives1++; }
                case 2 -> { shield1 = true; shield1Start = System.currentTimeMillis(); }
                case 3 -> { speed1 = true; speed1Start = System.currentTimeMillis(); }
                case 4 -> { if (!isSinglePlayer && lives2 > 1) lives2--; else player2Dead = true; lives1++; }
                case 5 -> { skillLock2 = true; skillLock2Start = System.currentTimeMillis(); }
            }
            db.logPowerup(player1Id, String.valueOf(type)); // Ganti ke powerup_id jika sudah migrasi DB
        } else {
            switch (type) {
                case 1 -> { if (lives2 < 5) lives2++; }
                case 2 -> { shield2 = true; shield2Start = System.currentTimeMillis(); }
                case 3 -> { speed2 = true; speed2Start = System.currentTimeMillis(); }
                case 4 -> { if (lives1 > 1) lives1--; else player1Dead = true; lives2++; }
                case 5 -> { skillLock1 = true; skillLock1Start = System.currentTimeMillis(); }
            }
            db.logPowerup(player2Id, String.valueOf(type)); // Ganti ke powerup_id jika sudah migrasi DB
        }
    }

    public boolean isTimeStopReady() {
        return !timeStopActive && (System.currentTimeMillis() - timeStopCooldownStart >= 15000) && !skillLock1;
    }

    public boolean isAreaClearReady() {
        return !areaClearActive && (System.currentTimeMillis() - areaClearCooldownStart >= 20000) && !skillLock2;
    }

    void updateBullets() {
        for (int i = ROWS - 2; i >= 0; i--) {
            for (int j = 0; j < COLS; j++) {
                if (arena[i][j] == '*') {
                    if (arena[i + 1][j] == '♥' || arena[i + 1][j] == '♦') {
                        arena[i + 1][j] = 'X';
                    } else {
                        arena[i + 1][j] = '*';
                    }
                    arena[i][j] = ' ';
                }
            }
        }
    }

    void spawnBullets() {
        for (int i = 0; i < COLS; i++) {
            if (rand.nextInt(300) < 10) arena[0][i] = '*';
        }
    }

    void clearBulletsAroundPlayer2() {
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int x = heartX2 + i, y = heartY2 + j;
                if (x >= 0 && x < ROWS && y >= 0 && y < COLS && arena[x][y] == '*') {
                    arena[x][y] = ' ';
                }
            }
        }
    }

    public void movePlayer1(int dx, int dy) {
        int moveStep = speed1 ? 2 : 1;
        int nx = heartX1 + dx * moveStep, ny = heartY1 + dy * moveStep;
        if (nx >= 0 && nx < ROWS && ny >= 0 && ny < COLS) {
            heartX1 = nx; heartY1 = ny;
        }
    }
    public void movePlayer2(int dx, int dy) {
        int moveStep = speed2 ? 2 : 1;
        int nx = heartX2 + dx * moveStep, ny = heartY2 + dy * moveStep;
        if (nx >= 0 && nx < ROWS && ny >= 0 && ny < COLS) {
            heartX2 = nx; heartY2 = ny;
        }
    }

    public void activateTimeStop() {
        if (isTimeStopReady()) {
            timeStopActive = true;
            timeStopStart = System.currentTimeMillis();
            playSound("sounds/player1.wav");
        }
    }

    public void activateAreaClear() {
        if (isAreaClearReady()) {
            areaClearActive = true;
            areaClearStart = System.currentTimeMillis();
            playSound("sounds/player2.wav");
        }
    }

    public void playBGM(String filePath) {
        try {
            File soundFile = new File(filePath);
            if (!soundFile.exists()) {
                System.out.println("File tidak ditemukan: " + filePath);
                return;
            }
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
            bgmClip = AudioSystem.getClip();
            bgmClip.open(audioStream);
            bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
            bgmClip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public void stopBGM() {
        if (bgmClip != null && bgmClip.isRunning()) {
            bgmClip.stop();
            bgmClip.close();
        }
    }

    public void playSound(String filePath) {
        try {
            File soundFile = new File(filePath);
            if (!soundFile.exists()) {
                System.out.println("File tidak ditemukan: " + filePath);
                return;
            }
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }
}