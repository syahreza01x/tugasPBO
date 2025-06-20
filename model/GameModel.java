package model;

import java.util.*;
import javax.swing.JOptionPane;

public class GameModel {
    public final int ROWS = 20, COLS = 60;
    public char[][] arena = new char[ROWS][COLS];

    public GamePlayer player1, player2;
    public boolean isSinglePlayer = true;

    private int player1Id, player2Id;
    private DatabaseManager db;

    public GameAudio audio = new GameAudio();
    public GameDrop drop = new GameDrop();
    public GameSkill skill;
    public GamePowerup powerup;
    public GameArena arenaHelper;

    // Skill id, cooldown, sound
    private int skill1Id, skill2Id;
    private int skill1Cooldown, skill2Cooldown;
    private String skill1Sound, skill2Sound;

    private int reverseTickCounter1 = 0, reverseTickCounter2 = 0;

    // Untuk key binding
    public static final int DEFAULT_TIME_STOP_KEY = java.awt.event.KeyEvent.VK_E;
    public static final int DEFAULT_AREA_CLEAR_KEY = java.awt.event.KeyEvent.VK_END;
    public int timeStopKey = DEFAULT_TIME_STOP_KEY;
    public int areaClearKey = DEFAULT_AREA_CLEAR_KEY;

    public GameModel(boolean isSinglePlayer, int timeStopKey, int areaClearKey, int player1Id, int player2Id, DatabaseManager db) {
        this.isSinglePlayer = isSinglePlayer;
        this.player1Id = player1Id;
        this.player2Id = player2Id;
        this.db = db;

        player1 = new GamePlayer(ROWS - 2, COLS / 4);
        player2 = new GamePlayer(ROWS - 2, COLS - COLS / 4);

        this.skill = new GameSkill(this, db, player1Id, player2Id);
        this.powerup = new GamePowerup(db);

        this.skill1Id = skill.skill1Id;
        this.skill2Id = skill.skill2Id;
        this.skill1Cooldown = skill.skill1Cooldown;
        this.skill2Cooldown = skill.skill2Cooldown;
        this.skill1Sound = skill.skill1Sound;
        this.skill2Sound = skill.skill2Sound;

        this.timeStopKey = timeStopKey;
        this.areaClearKey = areaClearKey;

        this.arenaHelper = new GameArena(ROWS, COLS, arena);

        resetGame();
        player1.highScore = db.getHighScore(player1Id);
        if (!isSinglePlayer) player2.highScore = db.getHighScore(player2Id);
    }

    public void resetGame() {
    for (int i = 0; i < ROWS; i++) Arrays.fill(arena[i], ' ');
    player1.x = ROWS - 2; player1.y = COLS / 4;
    player2.x = ROWS - 2; player2.y = COLS - COLS / 4;
    player1.score = 0; player2.score = 0;
    player1.lives = 3; player2.lives = 3;
    player1.dead = false; player2.dead = false;
    player1.shield = player2.shield = false;
    player1.speed = player2.speed = false;
    player1.skillLock = player2.skillLock = false;
    player1.showTimeStopEffect = player2.showTimeStopEffect = false;
    player1.showTimeReverseEffect = player2.showTimeReverseEffect = false;
    player1.showAreaClearEffect = player2.showAreaClearEffect = false;
    player1.timeStopActive = player2.timeStopActive = false;
    player1.timeReverseActive = player2.timeReverseActive = false;
    player1.areaClearActive = player2.areaClearActive = false;
    player1.pauseStart = player2.pauseStart = 0;
    player1.pauseAccum = player2.pauseAccum = 0;
    player1.extraHealthActive = player2.extraHealthActive = false;
    player1.extraHealthStart = player2.extraHealthStart = 0;
    reverseTickCounter1 = reverseTickCounter2 = 0;
    drop.clear();

    player1.timeStopCooldownStart = -skill1Cooldown;
    player1.areaClearCooldownStart = -skill1Cooldown;
    player1.timeReverseCooldownStart = -skill1Cooldown;
    player2.timeStopCooldownStart = -skill2Cooldown;
    player2.areaClearCooldownStart = -skill2Cooldown;
    player2.timeReverseCooldownStart = -skill2Cooldown;
}

    public void updateGame() {
        long now = System.currentTimeMillis();

        for (int i = 0; i < ROWS; i++)
            for (int j = 0; j < COLS; j++)
                if (arena[i][j] == '♥' || arena[i][j] == '♦')
                    arena[i][j] = ' ';

        updatePlayerEffects(player1, now);
        if (!isSinglePlayer) updatePlayerEffects(player2, now);

        // Skor dan efek reverse
        updatePlayerScore(player1, player2, true);
        if (!isSinglePlayer) updatePlayerScore(player2, player1, false);

        if (!(player1.timeStopActive || player2.timeStopActive || player1.timeReverseActive || player2.timeReverseActive)) {
            arenaHelper.updateBullets(player1, player2);
            arenaHelper.spawnBullets();
            drop.spawnDrops(ROWS, COLS);
            drop.updateDrops(this);
        }

        // Cek collision
        checkPlayerCollision(player1, player1Id);
        if (!isSinglePlayer) checkPlayerCollision(player2, player2Id);

        // Game over
        if (player1.dead && (player2.dead || isSinglePlayer)) {
            JOptionPane.showMessageDialog(null, "\uD83C\uDFAE Game Over. Player mati.");
            audio.stopBGM();
            resetGame();
            audio.playBGM("sounds/bgm.wav");
        }

        if (!player1.dead) arena[player1.x][player1.y] = '♥'; else arena[player1.x][player1.y] = ' ';
        if (!player2.dead && !isSinglePlayer) arena[player2.x][player2.y] = '♦'; else arena[player2.x][player2.y] = ' ';
    }

    private void updatePlayerEffects(GamePlayer p, long now) {
        if (p.extraHealthActive && now - p.extraHealthStart >= 4000) {
            audio.resumeBGM();
            p.extraHealthActive = false;
        }
        if (p.shield && now - p.shieldStart > 5000) p.shield = false;
        if (p.speed && now - p.speedStart > 5000) p.speed = false;
        if (p.skillLock && now - p.skillLockStart > 20000) p.skillLock = false;

        if (p.timeStopActive && (now - p.timeStopStart >= 5000)) {
            p.timeStopActive = false;
            p.timeStopCooldownStart = now;
            audio.resumeBGM();
        }
        p.showTimeStopEffect = p.timeStopActive && (now - p.timeStopStart <= 5000);

        if (p.timeReverseActive && (now - p.timeReverseStart >= 10000)) {
            p.timeReverseActive = false;
            p.timeReverseCooldownStart = now;
            audio.resumeBGM();
        }
        p.showTimeReverseEffect = p.timeReverseActive && (now - p.timeReverseStart <= 10000);

        if (p.areaClearActive && (now - p.areaClearStart <= 5000)) {
            arenaHelper.clearBulletsAroundPlayer(p);
            p.showAreaClearEffect = true;
        } else {
            if (p.areaClearActive) {
                p.areaClearActive = false;
                p.areaClearCooldownStart = now;
                audio.resumeBGM();
            }
            p.showAreaClearEffect = false;
        }
    }

    private void updatePlayerScore(GamePlayer p, GamePlayer enemy, boolean isP1) {
        if (!p.dead) {
            if (enemy.timeReverseActive) {
                if (isP1) reverseTickCounter1++; else reverseTickCounter2++;
                if ((isP1 ? reverseTickCounter1 : reverseTickCounter2) % 2 == 0 && p.score > 0) p.score--;
            } else if (enemy.timeStopActive) {
                if (isP1) reverseTickCounter1 = 0; else reverseTickCounter2 = 0;
            } else if (p.timeReverseActive) {
                if (isP1) reverseTickCounter1 = 0; else reverseTickCounter2 = 0;
            } else {
                p.score++;
                if (isP1) reverseTickCounter1 = 0; else reverseTickCounter2 = 0;
            }
        } else {
            if (isP1) reverseTickCounter1 = 0; else reverseTickCounter2 = 0;
        }
    }

    private void checkPlayerCollision(GamePlayer p, int playerId) {
        if (!p.dead && arena[p.x][p.y] == '*' && !p.shield) {
            p.lives--;
            if (p.lives <= 0) {
                p.dead = true;
                db.saveHighScore(playerId, p.score);
                p.highScore = db.getHighScore(playerId);
                JOptionPane.showMessageDialog(null, "\uD83D\uDC80 " + p.username + " Kehabisan nyawa! Game Over.\nScore: " + p.score + "\nHigh Score: " + p.highScore);
            }
        }
    }

    public void movePlayer1(int dx, int dy) {
    // Hanya batasi jika lawan sedang time stop/reverse
    if (!isSinglePlayer && (player2.timeStopActive || player2.timeReverseActive)) return;
    int moveStep = player1.speed ? 2 : 1;
    int nx = player1.x + dx * moveStep, ny = player1.y + dy * moveStep;
    if (nx >= 0 && nx < ROWS && ny >= 0 && ny < COLS) {
        player1.x = nx; player1.y = ny;
        }   
    }
    public void movePlayer2(int dx, int dy) {
    // Hanya batasi jika lawan sedang time stop/reverse
    if (player1.timeStopActive || player1.timeReverseActive) return;
    int moveStep = player2.speed ? 2 : 1;
    int nx = player2.x + dx * moveStep, ny = player2.y + dy * moveStep;
    if (nx >= 0 && nx < ROWS && ny >= 0 && ny < COLS) {
        player2.x = nx; player2.y = ny;
        }
    }
    // Skill getter/setter
    public int getSkill1Id() { return skill1Id; }
    public int getSkill2Id() { return skill2Id; }
    public int getSkill1Cooldown() { return skill1Cooldown; }
    public int getSkill2Cooldown() { return skill2Cooldown; }
    public String getSkill1Sound() { return skill1Sound; }
    public String getSkill2Sound() { return skill2Sound; }

    // Skill ready
    public boolean isTimeStopReady1() {
        long now = System.currentTimeMillis();
        long pause = player1.pauseAccum;
        if (player2.timeStopActive || player2.timeReverseActive) pause += now - player1.pauseStart;
        return !player1.timeStopActive && (now - player1.timeStopCooldownStart - pause >= skill1Cooldown) && !player1.skillLock;
    }
    public boolean isTimeStopReady2() {
        long now = System.currentTimeMillis();
        long pause = player2.pauseAccum;
        if (player1.timeStopActive || player1.timeReverseActive) pause += now - player2.pauseStart;
        return !player2.timeStopActive && (now - player2.timeStopCooldownStart - pause >= skill2Cooldown) && !player2.skillLock;
    }
    public boolean isAreaClearReady1() {
        long now = System.currentTimeMillis();
        long pause = player1.pauseAccum;
        if (player2.timeStopActive || player2.timeReverseActive) pause += now - player1.pauseStart;
        return !player1.areaClearActive && (now - player1.areaClearCooldownStart - pause >= skill1Cooldown) && !player1.skillLock;
    }
    public boolean isAreaClearReady2() {
        long now = System.currentTimeMillis();
        long pause = player2.pauseAccum;
        if (player1.timeStopActive || player1.timeReverseActive) pause += now - player2.pauseStart;
        return !player2.areaClearActive && (now - player2.areaClearCooldownStart - pause >= skill2Cooldown) && !player2.skillLock;
    }
    public boolean isTimeReverseReady1() {
        long now = System.currentTimeMillis();
        long pause = player1.pauseAccum;
        if (player2.timeStopActive || player2.timeReverseActive) pause += now - player1.pauseStart;
        return !player1.timeReverseActive && (now - player1.timeReverseCooldownStart - pause >= skill1Cooldown) && !player1.skillLock;
    }
    public boolean isTimeReverseReady2() {
        long now = System.currentTimeMillis();
        long pause = player2.pauseAccum;
        if (player1.timeStopActive || player1.timeReverseActive) pause += now - player2.pauseStart;
        return !player2.timeReverseActive && (now - player2.timeReverseCooldownStart - pause >= skill2Cooldown) && !player2.skillLock;
    }

    // Skill activation
    public void activateTimeStopForPlayer1() {
        if (isTimeStopReady1()) {
            audio.pauseBGM();
            player1.timeStopActive = true;
            player1.timeStopStart = System.currentTimeMillis();
            player2.pauseStart = player1.timeStopStart;
            audio.playSound("sounds/" + skill1Sound);
        }
    }
    public void activateTimeStopForPlayer2() {
        if (isTimeStopReady2()) {
            audio.pauseBGM();
            player2.timeStopActive = true;
            player2.timeStopStart = System.currentTimeMillis();
            player1.pauseStart = player2.timeStopStart;
            audio.playSound("sounds/" + skill2Sound);
        }
    }
    public void activateAreaClearForPlayer1() {
        if (isAreaClearReady1()) {
            audio.pauseBGM();
            player1.areaClearActive = true;
            player1.areaClearStart = System.currentTimeMillis();
            player1.showAreaClearEffect = true;
            audio.playSound("sounds/" + skill1Sound);
        }
    }
    public void activateAreaClearForPlayer2() {
        if (isAreaClearReady2()) {
            audio.pauseBGM();
            player2.areaClearActive = true;
            player2.areaClearStart = System.currentTimeMillis();
            player2.showAreaClearEffect = true;
            audio.playSound("sounds/" + skill2Sound);
        }
    }
    public void activateTimeReverseForPlayer1() {
        if (isTimeReverseReady1()) {
            audio.pauseBGM();
            player1.timeReverseActive = true;
            player1.timeReverseStart = System.currentTimeMillis();
            player2.pauseStart = player1.timeReverseStart;
            audio.playSound("sounds/" + skill1Sound);
        }
    }
    public void activateTimeReverseForPlayer2() {
        if (isTimeReverseReady2()) {
            audio.pauseBGM();
            player2.timeReverseActive = true;
            player2.timeReverseStart = System.currentTimeMillis();
            player1.pauseStart = player2.timeReverseStart;
            audio.playSound("sounds/" + skill2Sound);
        }
    }
    public void activateExtraHealthForPlayer1() {
        if (player1.lives < 5 && !player1.extraHealthActive) {
            audio.pauseBGM();
            player1.lives++;
            audio.playSound("sounds/" + skill1Sound);
            player1.areaClearCooldownStart = System.currentTimeMillis();
            player1.extraHealthActive = true;
            player1.extraHealthStart = System.currentTimeMillis();
        }
    }
    public void activateExtraHealthForPlayer2() {
        if (player2.lives < 5 && !player2.extraHealthActive) {
            audio.pauseBGM();
            player2.lives++;
            audio.playSound("sounds/" + skill2Sound);
            player2.areaClearCooldownStart = System.currentTimeMillis();
            player2.extraHealthActive = true;
            player2.extraHealthStart = System.currentTimeMillis();
        }
    }

    // Drop effect
    public void applyDropEffect(GamePlayer p, int type, int playerNum) {
        if (type == 1) { // Extra life
            if (p.lives < 5) p.lives++;
        } else if (type == 2) { // Shield
            p.shield = true; p.shieldStart = System.currentTimeMillis();
        } else if (type == 3) { // Speed
            p.speed = true; p.speedStart = System.currentTimeMillis();
        } else if (type == 4) { // Damage enemy
            if (playerNum == 1 && !isSinglePlayer && player2.lives > 1) player2.lives--;
            else if (playerNum == 2 && player1.lives > 1) player1.lives--;
            p.lives++;
        } else if (type == 5) { // Lock enemy skill
            if (playerNum == 1) { player2.skillLock = true; player2.skillLockStart = System.currentTimeMillis(); }
            else { player1.skillLock = true; player1.skillLockStart = System.currentTimeMillis(); }
        }
        powerup.logPowerup(playerNum == 1 ? player1Id : player2Id, String.valueOf(type));
    }

    // Audio wrapper
    public void playBGM(String filePath) { audio.playBGM(filePath); }
    public void stopBGM() { audio.stopBGM(); }
}