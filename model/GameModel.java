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
    public int p1UpKey, p1DownKey, p1LeftKey, p1RightKey, p1SkillKey;
    public int p2UpKey, p2DownKey, p2LeftKey, p2RightKey, p2SkillKey;

    private int reverseTickCounter1 = 0, reverseTickCounter2 = 0;


    public GameModel(
        boolean isSinglePlayer,
        int p1UpKey, int p1DownKey, int p1LeftKey, int p1RightKey, int p1SkillKey,
        int p2UpKey, int p2DownKey, int p2LeftKey, int p2RightKey, int p2SkillKey,
        int player1Id, int player2Id, DatabaseManager db
    ) {
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

        // Set key mapping
        this.p1UpKey = p1UpKey;
        this.p1DownKey = p1DownKey;
        this.p1LeftKey = p1LeftKey;
        this.p1RightKey = p1RightKey;
        this.p1SkillKey = p1SkillKey;
        this.p2UpKey = p2UpKey;
        this.p2DownKey = p2DownKey;
        this.p2LeftKey = p2LeftKey;
        this.p2RightKey = p2RightKey;
        this.p2SkillKey = p2SkillKey;

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

        player1.showLaser = false;
        player1.laserStart = 0;
        player1.laserCooldownStart = -30000;

        player1.kingCrimsonActive = false;
        player1.kingCrimsonStart = 0;
        player1.kingCrimsonCooldownStart = -25000;
        player1.showCrimsonEffect = false;

        if (player1.summon != null) player1.summon.active = false;
        player1.summonLocked = false;
        player1.summonCooldownStart = -120000;
        player1.summonLastMove = 0;
        player1.summonHitPause = 0;

        player1.goldExpActive = false;
        player1.goldExpStart = 0;
        player1.goldExpCooldownStart = -120000;
        player1.showGoldEffect = false;
        player1.goldExpLocked = false;

        player2.showLaser = false;
        player2.laserStart = 0;
        player2.laserCooldownStart = -30000;

        player2.kingCrimsonActive = false;
        player2.kingCrimsonStart = 0;
        player2.kingCrimsonCooldownStart = -25000;
        player2.showCrimsonEffect = false;

        if (player2.summon != null) player2.summon.active = false;
        player2.summonLocked = false;
        player2.summonCooldownStart = -120000;
        player2.summonLastMove = 0;
        player2.summonHitPause = 0;

        player2.goldExpActive = false;
        player2.goldExpStart = 0;
        player2.goldExpCooldownStart = -120000;
        player2.showGoldEffect = false;
        player2.goldExpLocked = false;
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

        // --- The Hand (Laser) ---
        if (player1.showLaser) {
            for (int j = player1.y - 1; j <= player1.y + 1; j++) {
                if (j < 0 || j >= COLS) continue;
                for (int i = 0; i < ROWS; i++) {
                    if (arena[i][j] == '*') arena[i][j] = ' ';
                }
            }
            if (now - player1.laserStart >= 2000) {
                player1.showLaser = false;
                player1.laserCooldownStart = now;
                audio.resumeBGM();
            }
        }
        if (player2.showLaser) {
            for (int j = player2.y - 1; j <= player2.y + 1; j++) {
                if (j < 0 || j >= COLS) continue;
                for (int i = 0; i < ROWS; i++) {
                    if (arena[i][j] == '*') arena[i][j] = ' ';
                }
            }
            if (now - player2.laserStart >= 2000) {
                player2.showLaser = false;
                player2.laserCooldownStart = now;
                audio.resumeBGM();
            }
        }

        // --- King Crimson ---
        if (player1.kingCrimsonActive && now - player1.kingCrimsonStart >= 10000) {
            player1.kingCrimsonActive = false;
            player1.showCrimsonEffect = false;
            player1.kingCrimsonCooldownStart = now;
            audio.resumeBGM();
        }
        if (player2.kingCrimsonActive && now - player2.kingCrimsonStart >= 10000) {
            player2.kingCrimsonActive = false;
            player2.showCrimsonEffect = false;
            player2.kingCrimsonCooldownStart = now;
            audio.resumeBGM();
        }

        // --- Silver Chariot (Summon) ---
        if (player1.summon.active) {
            if (now - player1.summon.startTime < 10000) {
                if (now - player1.summonHitPause >= 2000) {
                    if (now - player1.summonLastMove >= 500) {
                        player1.summon.update(player2, ROWS, COLS);
                        player1.summonLastMove = now;
                        if (player1.summon.x == player2.x && player1.summon.y == player2.y && !player2.dead && !player2.shield) {
                            player2.lives--;
                            player1.summonHitPause = now;
                            if (player2.lives <= 0) {
                                player2.dead = true;
                                db.saveHighScore(player2Id, player2.score);
                                player2.highScore = db.getHighScore(player2Id);
                                JOptionPane.showMessageDialog(null, "\uD83D\uDC80 " + player2.username + " Kehabisan nyawa! Game Over.\nScore: " + player2.score + "\nHigh Score: " + player2.highScore);
                            }
                        }
                    }
                }
            }
            if (now - player1.summon.startTime >= 10000) {
                player1.summon.active = false;
                player1.summonLocked = true;
                player1.summonCooldownStart = now;
                audio.resumeBGM();
            }
        }
        if (player2.summon.active) {
            if (now - player2.summon.startTime < 10000) {
                if (now - player2.summonHitPause >= 2000) {
                    if (now - player2.summonLastMove >= 500) {
                        player2.summon.update(player1, ROWS, COLS);
                        player2.summonLastMove = now;
                        if (player2.summon.x == player1.x && player2.summon.y == player1.y && !player1.dead && !player1.shield) {
                            player1.lives--;
                            player2.summonHitPause = now;
                            if (player1.lives <= 0) {
                                player1.dead = true;
                                db.saveHighScore(player1Id, player1.score);
                                player1.highScore = db.getHighScore(player1Id);
                                JOptionPane.showMessageDialog(null, "\uD83D\uDC80 " + player1.username + " Kehabisan nyawa! Game Over.\nScore: " + player1.score + "\nHigh Score: " + player1.highScore);
                            }
                        }
                    }
                }
            }
            if (now - player2.summon.startTime >= 10000) {
                player2.summon.active = false;
                player2.summonLocked = true;
                player2.summonCooldownStart = now;
                audio.resumeBGM();
            }
        }

        // --- Gold Experience ---
        boolean goldExpFreeze1 = player1.goldExpActive;
        boolean goldExpFreeze2 = player2.goldExpActive;

        // --- PAUSE COOLDOWN LAWAN SAAT GOLD EXPERIENCE AKTIF ---
        if (player1.goldExpActive && !player2.dead && !isSinglePlayer) {
            if (player2.pauseStart == 0) player2.pauseStart = now;
        } else if (!player1.goldExpActive && player2.pauseStart != 0 && !player2.timeStopActive && !player2.timeReverseActive) {
            player2.pauseAccum += now - player2.pauseStart;
            player2.pauseStart = 0;
        }
        if (player2.goldExpActive && !player1.dead) {
            if (player1.pauseStart == 0) player1.pauseStart = now;
        } else if (!player2.goldExpActive && player1.pauseStart != 0 && !player1.timeStopActive && !player1.timeReverseActive) {
            player1.pauseAccum += now - player1.pauseStart;
            player1.pauseStart = 0;
        }

        if (player1.goldExpActive && now - player1.goldExpStart >= 10000) {
            player1.goldExpActive = false;
            player1.showGoldEffect = false;
            player1.goldExpCooldownStart = now;
            if (!player2.dead && !isSinglePlayer) {
                player2.lives = Math.min(1, player2.lives);
                if (player2.lives == 0) {
                    player2.dead = true;
                    db.saveHighScore(player2Id, player2.score);
                    player2.highScore = db.getHighScore(player2Id);
                    JOptionPane.showMessageDialog(null, "\uD83D\uDC80 " + player2.username + " Kehabisan nyawa! Game Over.\nScore: " + player2.score + "\nHigh Score: " + player2.highScore);
                }
            }
            player1.goldExpLocked = true;
            audio.resumeBGM();
        }
        if (player2.goldExpActive && now - player2.goldExpStart >= 10000) {
            player2.goldExpActive = false;
            player2.showGoldEffect = false;
            player2.goldExpCooldownStart = now;
            if (!player1.dead) {
                player1.lives = Math.min(1, player1.lives);
                if (player1.lives == 0) {
                    player1.dead = true;
                    db.saveHighScore(player1Id, player1.score);
                    player1.highScore = db.getHighScore(player1Id);
                    JOptionPane.showMessageDialog(null, "\uD83D\uDC80 " + player1.username + " Kehabisan nyawa! Game Over.\nScore: " + player1.score + "\nHigh Score: " + player1.highScore);
                }
            }
            player2.goldExpLocked = true;
            audio.resumeBGM();
        }

        // Damage loop Gold Experience + freeze peluru
        if (player1.goldExpActive && !player2.dead && !isSinglePlayer) {
            audio.playSound("sounds/damage.wav");
            player2.skillLock = true;
        } else if (!player1.goldExpActive) {
            player2.skillLock = false;
        }
        if (player2.goldExpActive && !player1.dead) {
            audio.playSound("sounds/damage.wav");
            player1.skillLock = true;
        } else if (!player2.goldExpActive) {
            player1.skillLock = false;
        }

        // --- END skill 5-8 ---

        // Freeze peluru jika Gold Experience aktif
        boolean freezeBullets = (goldExpFreeze1 || goldExpFreeze2);

        if (!(player1.timeStopActive || player2.timeStopActive || player1.timeReverseActive || player2.timeReverseActive || freezeBullets)) {
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
            audio.playSound("sounds/damage.wav");
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
        if (!isSinglePlayer && (player2.timeStopActive || player2.timeReverseActive || player2.goldExpActive)) return;
        int moveStep = player1.speed ? 2 : 1;
        if (player1.kingCrimsonActive) moveStep = 3;
        if (player2.kingCrimsonActive) moveStep = 1;
        int nx = player1.x + dx * moveStep, ny = player1.y + dy * moveStep;
        if (nx >= 0 && nx < ROWS && ny >= 0 && ny < COLS) {
            player1.x = nx; player1.y = ny;
        }
    }
    public void movePlayer2(int dx, int dy) {
        if (player1.timeStopActive || player1.timeReverseActive || player1.goldExpActive) return;
        int moveStep = player2.speed ? 2 : 1;
        if (player1.kingCrimsonActive) moveStep = 1;
        if (player2.kingCrimsonActive) moveStep = 3;
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
        if (player1.dead) return false;
        long now = System.currentTimeMillis();
        long pause = player1.pauseAccum;
        if (player2.timeStopActive || player2.timeReverseActive) pause += now - player1.pauseStart;
        return !player1.timeStopActive && (now - player1.timeStopCooldownStart - pause >= skill1Cooldown) && !player1.skillLock;
    }
    public boolean isTimeStopReady2() {
        if (player2.dead) return false;
        long now = System.currentTimeMillis();
        long pause = player2.pauseAccum;
        if (player1.timeStopActive || player1.timeReverseActive) pause += now - player2.pauseStart;
        return !player2.timeStopActive && (now - player2.timeStopCooldownStart - pause >= skill2Cooldown) && !player2.skillLock;
    }
    public boolean isAreaClearReady1() {
        if (player1.dead) return false;
        long now = System.currentTimeMillis();
        long pause = player1.pauseAccum;
        if (player2.timeStopActive || player2.timeReverseActive) pause += now - player1.pauseStart;
        return !player1.areaClearActive && (now - player1.areaClearCooldownStart - pause >= skill1Cooldown) && !player1.skillLock;
    }
    public boolean isAreaClearReady2() {
        if (player2.dead) return false;
        long now = System.currentTimeMillis();
        long pause = player2.pauseAccum;
        if (player1.timeStopActive || player1.timeReverseActive) pause += now - player2.pauseStart;
        return !player2.areaClearActive && (now - player2.areaClearCooldownStart - pause >= skill2Cooldown) && !player2.skillLock;
    }
    public boolean isTimeReverseReady1() {
        if (player1.dead) return false;
        long now = System.currentTimeMillis();
        long pause = player1.pauseAccum;
        if (player2.timeStopActive || player2.timeReverseActive) pause += now - player1.pauseStart;
        return !player1.timeReverseActive && (now - player1.timeReverseCooldownStart - pause >= skill1Cooldown) && !player1.skillLock;
    }
    public boolean isTimeReverseReady2() {
        if (player2.dead) return false;
        long now = System.currentTimeMillis();
        long pause = player2.pauseAccum;
        if (player1.timeStopActive || player1.timeReverseActive) pause += now - player2.pauseStart;
        return !player2.timeReverseActive && (now - player2.timeReverseCooldownStart - pause >= skill2Cooldown) && !player2.skillLock;
    }

    // --- Skill 5-8 ---
    public boolean isTheHandReady1() {
        if (player1.dead) return false;
        long now = System.currentTimeMillis();
        return skill1Id == 5 && !player1.showLaser && (now - player1.laserCooldownStart >= 30000) && !player1.skillLock;
    }
    public boolean isTheHandReady2() {
        if (player2.dead) return false;
        long now = System.currentTimeMillis();
        return skill2Id == 5 && !player2.showLaser && (now - player2.laserCooldownStart >= 30000) && !player2.skillLock;
    }
    public boolean isKingCrimsonReady1() {
        if (player1.dead) return false;
        long now = System.currentTimeMillis();
        return skill1Id == 6 && !player1.kingCrimsonActive && (now - player1.kingCrimsonCooldownStart >= 25000) && !player1.skillLock;
    }
    public boolean isKingCrimsonReady2() {
        if (player2.dead) return false;
        long now = System.currentTimeMillis();
        return skill2Id == 6 && !player2.kingCrimsonActive && (now - player2.kingCrimsonCooldownStart >= 25000) && !player2.skillLock;
    }
    public boolean isSilverChariotReady1() {
        if (player1.dead) return false;
        if (skill1Id != 7) return false;
        if (player1.score < 200) return false;
        long now = System.currentTimeMillis();
        return !player1.summon.active && !player1.summonLocked && (now - player1.summonCooldownStart >= skill1Cooldown) && !player1.skillLock;
    }
    public boolean isSilverChariotReady2() {
        if (player2.dead) return false;
        if (skill2Id != 7) return false;
        if (player2.score < 200) return false;
        long now = System.currentTimeMillis();
        return !player2.summon.active && !player2.summonLocked && (now - player2.summonCooldownStart >= skill2Cooldown) && !player2.skillLock;
    }
    public boolean isGoldExpReady1() {
        if (player1.dead) return false;
        if (skill1Id != 8) return false;
        if (player1.score < 100) return false;
        long now = System.currentTimeMillis();
        return !player1.goldExpActive && !player1.goldExpLocked && (now - player1.goldExpCooldownStart >= skill1Cooldown) && !player1.skillLock;
    }
    public boolean isGoldExpReady2() {
        if (player2.dead) return false;
        if (skill2Id != 8) return false;
        if (player2.score < 100) return false;
        long now = System.currentTimeMillis();
        return !player2.goldExpActive && !player2.goldExpLocked && (now - player2.goldExpCooldownStart >= skill2Cooldown) && !player2.skillLock;
    }

    // --- Skill 5-8 Activation ---
    public void activateTheHandForPlayer1() {
        if (!isTheHandReady1()) return;
        audio.pauseBGM();
        player1.showLaser = true;
        player1.laserStart = System.currentTimeMillis();
        audio.playSound("sounds/skill5.wav");
    }

    public void activateTheHandForPlayer2() {
        if (!isTheHandReady2()) return;
        audio.pauseBGM();
        player2.showLaser = true;
        player2.laserStart = System.currentTimeMillis();
        audio.playSound("sounds/skill5.wav");
    }

    public void activateKingCrimsonForPlayer1() {
        if (!isKingCrimsonReady1()) return;
        audio.pauseBGM();
        player1.kingCrimsonActive = true;
        player1.kingCrimsonStart = System.currentTimeMillis();
        player1.showCrimsonEffect = true;
        audio.playSound("sounds/skill6.wav");
    }

    public void activateKingCrimsonForPlayer2() {
        if (!isKingCrimsonReady2()) return;
        audio.pauseBGM();
        player2.kingCrimsonActive = true;
        player2.kingCrimsonStart = System.currentTimeMillis();
        player2.showCrimsonEffect = true;
        audio.playSound("sounds/skill6.wav");
    }

    public void activateSilverChariotForPlayer1() {
        if (!isSilverChariotReady1()) return;
        audio.pauseBGM();
        if (player1.summon != null) {
            player1.summon.summon(player1.x, player1.y);
            player1.summon.startTime = System.currentTimeMillis();
            player1.summonLastMove = player1.summon.startTime;
            player1.summonHitPause = player1.summon.startTime - 2000;
            audio.playSound("sounds/skill7.wav");
        }
    }

    public void activateSilverChariotForPlayer2() {
        if (!isSilverChariotReady2()) return;
        audio.pauseBGM();
        if (player2.summon != null) {
            player2.summon.summon(player2.x, player2.y);
            player2.summon.startTime = System.currentTimeMillis();
            player2.summonLastMove = player2.summon.startTime;
            player2.summonHitPause = player2.summon.startTime - 2000;
            audio.playSound("sounds/skill7.wav");
        }
    }

    public void activateGoldExpForPlayer1() {
        if (!isGoldExpReady1()) return;
        audio.pauseBGM();
        player1.goldExpActive = true;
        player1.goldExpStart = System.currentTimeMillis();
        player1.showGoldEffect = true;
        audio.playSound("sounds/skill8.wav");
    }

    public void activateGoldExpForPlayer2() {
        if (!isGoldExpReady2()) return;
        audio.pauseBGM();
        player2.goldExpActive = true;
        player2.goldExpStart = System.currentTimeMillis();
        player2.showGoldEffect = true;
        audio.playSound("sounds/skill8.wav");
    }


    // Skill 1-4 Activation (tidak berubah)
    public void activateTimeStopForPlayer1() {
        if (player2.timeStopActive || player2.timeReverseActive) return;
        if (isTimeStopReady1()) {
            audio.pauseBGM();
            player1.timeStopActive = true;
            player1.timeStopStart = System.currentTimeMillis();
            player2.pauseStart = player1.timeStopStart;
            audio.playSound("sounds/" + skill1Sound);
        }
    }
    public void activateTimeStopForPlayer2() {
        if (player1.timeStopActive || player1.timeReverseActive) return;
        if (isTimeStopReady2()) {
            audio.pauseBGM();
            player2.timeStopActive = true;
            player2.timeStopStart = System.currentTimeMillis();
            player1.pauseStart = player2.timeStopStart;
            audio.playSound("sounds/" + skill2Sound);
        }
    }
    public void activateAreaClearForPlayer1() {
        if (player2.timeStopActive || player2.timeReverseActive) return;
        if (isAreaClearReady1()) {
            audio.pauseBGM();
            player1.areaClearActive = true;
            player1.areaClearStart = System.currentTimeMillis();
            player1.showAreaClearEffect = true;
            audio.playSound("sounds/" + skill1Sound);
        }
    }
    public void activateAreaClearForPlayer2() {
        if (player1.timeStopActive || player1.timeReverseActive) return;
        if (isAreaClearReady2()) {
            audio.pauseBGM();
            player2.areaClearActive = true;
            player2.areaClearStart = System.currentTimeMillis();
            player2.showAreaClearEffect = true;
            audio.playSound("sounds/" + skill2Sound);
        }
    }
    public void activateTimeReverseForPlayer1() {
        if (player2.timeStopActive || player2.timeReverseActive) return;
        if (isTimeReverseReady1()) {
            audio.pauseBGM();
            player1.timeReverseActive = true;
            player1.timeReverseStart = System.currentTimeMillis();
            player2.pauseStart = player1.timeReverseStart;
            audio.playSound("sounds/" + skill1Sound);
        }
    }
    public void activateTimeReverseForPlayer2() {
        if (player1.timeStopActive || player1.timeReverseActive) return;
        if (isTimeReverseReady2()) {
            audio.pauseBGM();
            player2.timeReverseActive = true;
            player2.timeReverseStart = System.currentTimeMillis();
            player1.pauseStart = player2.timeReverseStart;
            audio.playSound("sounds/" + skill2Sound);
        }
    }
    public void activateExtraHealthForPlayer1() {
        if (player2.timeStopActive || player2.timeReverseActive) return;
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
        if (player1.timeStopActive || player1.timeReverseActive) return;
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
    GamePlayer enemy = (playerNum == 1) ? player2 : player1;
    int id = (playerNum == 1) ? player1Id : player2Id;
    powerup.applyEffect(p, enemy, type, id, isSinglePlayer);
}


    // Audio wrapper
    public void playBGM(String filePath) { audio.playBGM(filePath); }
    public void stopBGM() { audio.stopBGM(); }
}