package model;

import java.util.*;
import java.awt.event.KeyEvent;
import javax.swing.JOptionPane;
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class GameModel {
    private Clip bgmClip;
    private long bgmPosition = 0;

    public final int ROWS = 20, COLS = 60;
    public char[][] arena = new char[ROWS][COLS];
    public int heartX1 = ROWS - 2, heartY1 = COLS / 4;
    public int heartX2 = ROWS - 2, heartY2 = COLS - COLS / 4;

    public int score1 = 0, score2 = 0;
    public int highScore1 = 0, highScore2 = 0;
    public int lives1 = 3, lives2 = 3;
    public boolean player1Dead = false, player2Dead = false, isSinglePlayer = true;

    private int skill1Id, skill2Id;
    private int skill1Cooldown, skill2Cooldown;
    private String skill1Sound, skill2Sound;

    public boolean timeStopActive1 = false, timeStopActive2 = false;
    public long timeStopStart1 = 0, timeStopStart2 = 0;
    public long timeStopCooldownStart1 = -15000, timeStopCooldownStart2 = -15000;
    public boolean areaClearActive1 = false, areaClearActive2 = false;
    public long areaClearStart1 = 0, areaClearStart2 = 0;
    public long areaClearCooldownStart1 = -20000, areaClearCooldownStart2 = -20000;

    public boolean timeReverseActive1 = false, timeReverseActive2 = false;
    public long timeReverseStart1 = 0, timeReverseStart2 = 0;
    public long timeReverseCooldownStart1 = -20000, timeReverseCooldownStart2 = -20000;

    public boolean showTimeStopEffect1 = false, showTimeStopEffect2 = false;
    public boolean showAreaClearEffect1 = false, showAreaClearEffect2 = false;
    public boolean showTimeReverseEffect1 = false, showTimeReverseEffect2 = false;

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

    public String username1, username2;

    private int player1Id, player2Id;
    private DatabaseManager db;

    // Pause hanya untuk cooldown lawan!
    public long pauseStartP1 = 0, pauseAccumP1 = 0; // Untuk cooldown P1 (dipause saat skill 1/3 P2 aktif)
    public long pauseStartP2 = 0, pauseAccumP2 = 0; // Untuk cooldown P2 (dipause saat skill 1/3 P1 aktif)

    public static class Drop {
        public int x, y, type;
        public char icon;
        public Drop(int x, int y, char icon, int type) {
            this.x = x; this.y = y; this.icon = icon; this.type = type;
        }
    }
    public List<Drop> drops = new ArrayList<>();
    private final Random rand = new Random();

    public int getSkill1Id() { return skill1Id; }
    public int getSkill2Id() { return skill2Id; }
    public int getSkill1Cooldown() { return skill1Cooldown; }
    public int getSkill2Cooldown() { return skill2Cooldown; }
    public String getSkill1Sound() { return skill1Sound; }
    public String getSkill2Sound() { return skill2Sound; }

    public GameModel(boolean isSinglePlayer, int timeStopKey, int areaClearKey, int player1Id, int player2Id, DatabaseManager db) {
        this.isSinglePlayer = isSinglePlayer;
        this.timeStopKey = timeStopKey;
        this.areaClearKey = areaClearKey;
        this.player1Id = player1Id;
        this.player2Id = player2Id;
        this.db = db;

        this.skill1Id = db.getPlayerSkill(player1Id);
        if (!isSinglePlayer) this.skill2Id = db.getPlayerSkill(player2Id);

        this.skill1Cooldown = db.getSkillCooldown(skill1Id);
        if (!isSinglePlayer) this.skill2Cooldown = db.getSkillCooldown(skill2Id);

        this.skill1Sound = db.getSkillSound(skill1Id);
        if (!isSinglePlayer) this.skill2Sound = db.getSkillSound(skill2Id);

        resetGame();
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
        timeStopActive1 = false; timeStopActive2 = false;
        areaClearActive1 = false; areaClearActive2 = false;
        timeReverseActive1 = false; timeReverseActive2 = false;
        timeStopCooldownStart1 = -skill1Cooldown;
        timeStopCooldownStart2 = -skill2Cooldown;
        areaClearCooldownStart1 = -skill1Cooldown;
        areaClearCooldownStart2 = -skill2Cooldown;
        timeReverseCooldownStart1 = -skill1Cooldown;
        timeReverseCooldownStart2 = -skill2Cooldown;
        shield1 = false; shield2 = false;
        speed1 = false; speed2 = false;
        skillLock1 = false; skillLock2 = false;
        drops.clear();
        bgmPosition = 0;
        pauseStartP1 = 0; pauseAccumP1 = 0;
        pauseStartP2 = 0; pauseAccumP2 = 0;
    }

    public void updateGame() {
        long now = System.currentTimeMillis();

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

        // Efek Time Stop selesai: resume BGM dan akumulasi pause lawan
        if (timeStopActive1 && (now - timeStopStart1 >= 5000)) {
            timeStopActive1 = false;
            timeStopCooldownStart1 = now;
            pauseAccumP2 += now - pauseStartP2;
            resumeBGM();
        }
        if (timeStopActive2 && (now - timeStopStart2 >= 5000)) {
            timeStopActive2 = false;
            timeStopCooldownStart2 = now;
            pauseAccumP1 += now - pauseStartP1;
            resumeBGM();
        }
        showTimeStopEffect1 = timeStopActive1 && (now - timeStopStart1 <= 5000);
        showTimeStopEffect2 = timeStopActive2 && (now - timeStopStart2 <= 5000);

        // Efek Time Reverse selesai: resume BGM dan akumulasi pause lawan
        if (timeReverseActive1 && (now - timeReverseStart1 >= 10000)) {
            timeReverseActive1 = false;
            timeReverseCooldownStart1 = now;
            pauseAccumP2 += now - pauseStartP2;
            resumeBGM();
        }
        if (timeReverseActive2 && (now - timeReverseStart2 >= 10000)) {
            timeReverseActive2 = false;
            timeReverseCooldownStart2 = now;
            pauseAccumP1 += now - pauseStartP1;
            resumeBGM();
        }
        showTimeReverseEffect1 = timeReverseActive1 && (now - timeReverseStart1 <= 10000);
        showTimeReverseEffect2 = timeReverseActive2 && (now - timeReverseStart2 <= 10000);

        // Efek Area Clear
        if (areaClearActive1 && (now - areaClearStart1 <= 5000)) {
            clearBulletsAroundPlayer(1);
            showAreaClearEffect1 = true;
        } else {
            if (areaClearActive1) {
                areaClearActive1 = false;
                areaClearCooldownStart1 = now;
            }
            showAreaClearEffect1 = false;
        }
        if (areaClearActive2 && (now - areaClearStart2 <= 5000)) {
            clearBulletsAroundPlayer(2);
            showAreaClearEffect2 = true;
        } else {
            if (areaClearActive2) {
                areaClearActive2 = false;
                areaClearCooldownStart2 = now;
            }
            showAreaClearEffect2 = false;
        }

        // Skor: hanya skor pengguna time stop yang tetap bertambah, time reverse lawan turun
        if (!player1Dead && (!timeStopActive2) && (!timeReverseActive2)) score1++;
        if (!player2Dead && !isSinglePlayer && (!timeStopActive1) && (!timeReverseActive1)) score2++;
        // Skor lawan mundur lebih cepat (setiap 200ms)
        if (timeReverseActive1 && !player2Dead && !isSinglePlayer && now % 200 < 50 && score2 > 0) score2--;
        if (timeReverseActive2 && !player1Dead && now % 200 < 50 && score1 > 0) score1--;

        // Bullets & drops
        if (!(timeStopActive1 || timeStopActive2 || timeReverseActive1 || timeReverseActive2)) {
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
                    db.deleteScoresByPlayer(player1Id);
                    highScore1 = score1;
                    db.insertScore(player1Id, score1);
                } else {
                    db.insertScore(player1Id, score1);
                }
                JOptionPane.showMessageDialog(null, "\uD83D\uDC80 " + username1 + " Kehabisan nyawa! Game Over.\nScore: " + score1 + "\nHigh Score: " + highScore1);
            }
        }
        if (!player2Dead && !isSinglePlayer && arena[heartX2][heartY2] == '*' && !shield2) {
            lives2--;
            if (lives2 <= 0) {
                player2Dead = true;
                if (score2 > highScore2) {
                    db.deleteScoresByPlayer(player2Id);
                    highScore2 = score2;
                    db.insertScore(player2Id, score2);
                } else {
                    db.insertScore(player2Id, score2);
                }
                JOptionPane.showMessageDialog(null, "\uD83D\uDC80 " + username2 + " Kehabisan nyawa! Game Over.\nScore: " + score2 + "\nHigh Score: " + highScore2);
            }
        }
        if ((player1Dead && (player2Dead || isSinglePlayer))) {
            JOptionPane.showMessageDialog(null, "\uD83C\uDFAE Game Over. Player mati.");
            stopBGM();
            resetGame();
            playBGM("sounds/bgm.wav");
        }

        if (!player1Dead) arena[heartX1][heartY1] = '♥'; else arena[heartX1][heartY1] = ' ';
        if (!player2Dead && !isSinglePlayer) arena[heartX2][heartY2] = '♦'; else arena[heartX2][heartY2] = ' ';
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

    void applyDropEffect(int player, int type) {
        if (player == 1) {
            switch (type) {
                case 1 -> { if (lives1 < 5) lives1++; }
                case 2 -> { shield1 = true; shield1Start = System.currentTimeMillis(); }
                case 3 -> { speed1 = true; speed1Start = System.currentTimeMillis(); }
                case 4 -> { if (!isSinglePlayer && lives2 > 1) lives2--; else player2Dead = true; lives1++; }
                case 5 -> { skillLock2 = true; skillLock2Start = System.currentTimeMillis(); }
            }
            db.logPowerup(player1Id, String.valueOf(type));
        } else {
            switch (type) {
                case 1 -> { if (lives2 < 5) lives2++; }
                case 2 -> { shield2 = true; shield2Start = System.currentTimeMillis(); }
                case 3 -> { speed2 = true; speed2Start = System.currentTimeMillis(); }
                case 4 -> { if (lives1 > 1) lives1--; else player1Dead = true; lives2++; }
                case 5 -> { skillLock1 = true; skillLock1Start = System.currentTimeMillis(); }
            }
            db.logPowerup(player2Id, String.valueOf(type));
        }
    }

    // Cooldown dengan pause lawan SAJA
    public boolean isTimeStopReady1() {
        long now = System.currentTimeMillis();
        long pause = pauseAccumP1;
        if (timeStopActive2 || timeReverseActive2) pause += now - pauseStartP1;
        return !timeStopActive1 && (now - timeStopCooldownStart1 - pause >= skill1Cooldown) && !skillLock1;
    }
    public boolean isTimeStopReady2() {
        long now = System.currentTimeMillis();
        long pause = pauseAccumP2;
        if (timeStopActive1 || timeReverseActive1) pause += now - pauseStartP2;
        return !timeStopActive2 && (now - timeStopCooldownStart2 - pause >= skill2Cooldown) && !skillLock2;
    }
    public boolean isAreaClearReady1() {
        long now = System.currentTimeMillis();
        long pause = pauseAccumP1;
        if (timeStopActive2 || timeReverseActive2) pause += now - pauseStartP1;
        return !areaClearActive1 && (now - areaClearCooldownStart1 - pause >= skill1Cooldown) && !skillLock1;
    }
    public boolean isAreaClearReady2() {
        long now = System.currentTimeMillis();
        long pause = pauseAccumP2;
        if (timeStopActive1 || timeReverseActive1) pause += now - pauseStartP2;
        return !areaClearActive2 && (now - areaClearCooldownStart2 - pause >= skill2Cooldown) && !skillLock2;
    }
    public boolean isTimeReverseReady1() {
        long now = System.currentTimeMillis();
        long pause = pauseAccumP1;
        if (timeStopActive2 || timeReverseActive2) pause += now - pauseStartP1;
        return !timeReverseActive1 && (now - timeReverseCooldownStart1 - pause >= skill1Cooldown) && !skillLock1;
    }
    public boolean isTimeReverseReady2() {
        long now = System.currentTimeMillis();
        long pause = pauseAccumP2;
        if (timeStopActive1 || timeReverseActive1) pause += now - pauseStartP2;
        return !timeReverseActive2 && (now - timeReverseCooldownStart2 - pause >= skill2Cooldown) && !skillLock2;
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

    void clearBulletsAroundPlayer(int player) {
        int cx = (player == 1) ? heartX1 : heartX2;
        int cy = (player == 1) ? heartY1 : heartY2;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int x = cx + i, y = cy + j;
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

    // Skill activation: hanya pause cooldown lawan!
    public void activateTimeStopForPlayer1() {
        if (isTimeStopReady1()) {
            pauseBGM();
            timeStopActive1 = true;
            timeStopStart1 = System.currentTimeMillis();
            pauseStartP2 = timeStopStart1;
            playSound("sounds/" + skill1Sound);
        }
    }
    public void activateTimeStopForPlayer2() {
        if (isTimeStopReady2()) {
            pauseBGM();
            timeStopActive2 = true;
            timeStopStart2 = System.currentTimeMillis();
            pauseStartP1 = timeStopStart2;
            playSound("sounds/" + skill2Sound);
        }
    }
    public void activateAreaClearForPlayer1() {
        if (isAreaClearReady1()) {
            areaClearActive1 = true;
            areaClearStart1 = System.currentTimeMillis();
            showAreaClearEffect1 = true;
            playSound("sounds/" + skill1Sound);
        }
    }
    public void activateAreaClearForPlayer2() {
        if (isAreaClearReady2()) {
            areaClearActive2 = true;
            areaClearStart2 = System.currentTimeMillis();
            showAreaClearEffect2 = true;
            playSound("sounds/" + skill2Sound);
        }
    }
    public void activateTimeReverseForPlayer1() {
        if (isTimeReverseReady1()) {
            pauseBGM();
            timeReverseActive1 = true;
            timeReverseStart1 = System.currentTimeMillis();
            pauseStartP2 = timeReverseStart1;
            playSound("sounds/" + skill1Sound);
        }
    }
    public void activateTimeReverseForPlayer2() {
        if (isTimeReverseReady2()) {
            pauseBGM();
            timeReverseActive2 = true;
            timeReverseStart2 = System.currentTimeMillis();
            pauseStartP1 = timeReverseStart2;
            playSound("sounds/" + skill2Sound);
        }
    }
    public void activateExtraHealthForPlayer1() {
        if (lives1 < 5) {
            lives1++;
            playSound("sounds/" + skill1Sound);
            areaClearCooldownStart1 = System.currentTimeMillis();
        }
    }
    public void activateExtraHealthForPlayer2() {
        if (lives2 < 5) {
            lives2++;
            playSound("sounds/" + skill2Sound);
            areaClearCooldownStart2 = System.currentTimeMillis();
        }
    }

    // BGM controls
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
            bgmPosition = 0;
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public void stopBGM() {
        if (bgmClip != null) {
            bgmClip.stop();
            bgmClip.close();
            bgmClip = null;
            bgmPosition = 0;
        }
    }

    public void pauseBGM() {
        if (bgmClip != null && bgmClip.isRunning()) {
            bgmPosition = bgmClip.getMicrosecondPosition();
            bgmClip.stop();
        }
    }

    public void resumeBGM() {
        if (bgmClip != null) {
            bgmClip.setMicrosecondPosition(bgmPosition);
            bgmClip.start();
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}