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

    // --- Tambahan untuk skill 5-8 ---
    // The Hand (Laser)
    public boolean showLaser1 = false, showLaser2 = false;
    public long laserStart1 = 0, laserStart2 = 0;
    public long laserCooldownStart1 = -30000, laserCooldownStart2 = -30000;

    // King Crimson
    public boolean kingCrimsonActive1 = false, kingCrimsonActive2 = false;
    public long kingCrimsonStart1 = 0, kingCrimsonStart2 = 0;
    public long kingCrimsonCooldownStart1 = -25000, kingCrimsonCooldownStart2 = -25000;
    public boolean showCrimsonEffect1 = false, showCrimsonEffect2 = false;

    // Silver Chariot (Summon)
    public GameSummon summon1 = new GameSummon();
    public GameSummon summon2 = new GameSummon();
    public boolean summonLocked1 = false, summonLocked2 = false;
    public long summonCooldownStart1 = -120000, summonCooldownStart2 = -120000;
    public long summonLastMove1 = 0, summonLastMove2 = 0;
    public long summonHitPause1 = 0, summonHitPause2 = 0;

    // Gold Experience
    public boolean goldExpActive1 = false, goldExpActive2 = false;
    public long goldExpStart1 = 0, goldExpStart2 = 0;
    public long goldExpCooldownStart1 = -120000, goldExpCooldownStart2 = -120000;
    public boolean showGoldEffect1 = false, showGoldEffect2 = false;
    public boolean goldExpLocked1 = false, goldExpLocked2 = false;

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

        // Reset skill 5-8
        showLaser1 = showLaser2 = false;
        laserStart1 = laserStart2 = 0;
        laserCooldownStart1 = laserCooldownStart2 = -30000;
        kingCrimsonActive1 = kingCrimsonActive2 = false;
        kingCrimsonStart1 = kingCrimsonStart2 = 0;
        kingCrimsonCooldownStart1 = kingCrimsonCooldownStart2 = -25000;
        showCrimsonEffect1 = showCrimsonEffect2 = false;
        summon1.active = summon2.active = false;
        summonLocked1 = summonLocked2 = false;
        summonCooldownStart1 = summonCooldownStart2 = -120000;
        summonLastMove1 = summonLastMove2 = 0;
        summonHitPause1 = summonHitPause2 = 0;
        goldExpActive1 = goldExpActive2 = false;
        goldExpStart1 = goldExpStart2 = 0;
        goldExpCooldownStart1 = goldExpCooldownStart2 = -120000;
        showGoldEffect1 = showGoldEffect2 = false;
        goldExpLocked1 = goldExpLocked2 = false;
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
        if (showLaser1) {
            for (int j = player1.y - 1; j <= player1.y + 1; j++) {
                if (j < 0 || j >= COLS) continue;
                for (int i = 0; i < ROWS; i++) {
                    if (arena[i][j] == '*') arena[i][j] = ' ';
                }
            }
            if (now - laserStart1 >= 2000) {
                showLaser1 = false;
                laserCooldownStart1 = now;
                audio.resumeBGM();
            }
        }
        if (showLaser2) {
            for (int j = player2.y - 1; j <= player2.y + 1; j++) {
                if (j < 0 || j >= COLS) continue;
                for (int i = 0; i < ROWS; i++) {
                    if (arena[i][j] == '*') arena[i][j] = ' ';
                }
            }
            if (now - laserStart2 >= 2000) {
                showLaser2 = false;
                laserCooldownStart2 = now;
                audio.resumeBGM();
            }
        }

        // --- King Crimson ---
        if (kingCrimsonActive1 && now - kingCrimsonStart1 >= 10000) {
            kingCrimsonActive1 = false;
            showCrimsonEffect1 = false;
            kingCrimsonCooldownStart1 = now;
            audio.resumeBGM();
        }
        if (kingCrimsonActive2 && now - kingCrimsonStart2 >= 10000) {
            kingCrimsonActive2 = false;
            showCrimsonEffect2 = false;
            kingCrimsonCooldownStart2 = now;
            audio.resumeBGM();
        }

        // --- Silver Chariot (Summon) ---
        if (summon1.active) {
            if (now - summon1.startTime < 10000) {
                if (now - summonHitPause1 >= 2000) {
                    if (now - summonLastMove1 >= 500) {
                        summon1.update(player2, ROWS, COLS);
                        summonLastMove1 = now;
                        if (summon1.x == player2.x && summon1.y == player2.y && !player2.dead && !player2.shield) {
                            player2.lives--;
                            summonHitPause1 = now;
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
            if (now - summon1.startTime >= 10000) {
                summon1.active = false;
                summonLocked1 = true;
                summonCooldownStart1 = now;
                audio.resumeBGM();
            }
        }
        if (summon2.active) {
            if (now - summon2.startTime < 10000) {
                if (now - summonHitPause2 >= 2000) {
                    if (now - summonLastMove2 >= 500) {
                        summon2.update(player1, ROWS, COLS);
                        summonLastMove2 = now;
                        if (summon2.x == player1.x && summon2.y == player1.y && !player1.dead && !player1.shield) {
                            player1.lives--;
                            summonHitPause2 = now;
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
            if (now - summon2.startTime >= 10000) {
                summon2.active = false;
                summonLocked2 = true;
                summonCooldownStart2 = now;
                audio.resumeBGM();
            }
        }

        // --- Gold Experience ---
        boolean goldExpFreeze1 = goldExpActive1;
        boolean goldExpFreeze2 = goldExpActive2;

        // Pause cooldown lawan saat Gold Experience aktif
        if (goldExpActive1 && !player2.dead && !isSinglePlayer) {
            if (player2.pauseStart == 0) player2.pauseStart = now;
        } else if (!goldExpActive1 && player2.pauseStart != 0) {
            player2.pauseAccum += now - player2.pauseStart;
            player2.pauseStart = 0;
        }
        if (goldExpActive2 && !player1.dead) {
            if (player1.pauseStart == 0) player1.pauseStart = now;
        } else if (!goldExpActive2 && player1.pauseStart != 0) {
            player1.pauseAccum += now - player1.pauseStart;
            player1.pauseStart = 0;
        }

        if (goldExpActive1 && now - goldExpStart1 >= 10000) {
            goldExpActive1 = false;
            showGoldEffect1 = false;
            goldExpCooldownStart1 = now;
            if (!player2.dead && !isSinglePlayer) {
                if (player2.lives > 1) player2.lives = 1;
                else player2.lives = 0;
                if (player2.lives == 0) {
                    player2.dead = true;
                    db.saveHighScore(player2Id, player2.score);
                    player2.highScore = db.getHighScore(player2Id);
                    JOptionPane.showMessageDialog(null, "\uD83D\uDC80 " + player2.username + " Kehabisan nyawa! Game Over.\nScore: " + player2.score + "\nHigh Score: " + player2.highScore);
                }
            }
            goldExpLocked1 = true;
            audio.resumeBGM();
        }
        if (goldExpActive2 && now - goldExpStart2 >= 10000) {
            goldExpActive2 = false;
            showGoldEffect2 = false;
            goldExpCooldownStart2 = now;
            if (!player1.dead) {
                if (player1.lives > 1) player1.lives = 1;
                else player1.lives = 0;
                if (player1.lives == 0) {
                    player1.dead = true;
                    db.saveHighScore(player1Id, player1.score);
                    player1.highScore = db.getHighScore(player1Id);
                    JOptionPane.showMessageDialog(null, "\uD83D\uDC80 " + player1.username + " Kehabisan nyawa! Game Over.\nScore: " + player1.score + "\nHigh Score: " + player1.highScore);
                }
            }
            goldExpLocked2 = true;
            audio.resumeBGM();
        }

        // Damage loop Gold Experience + freeze peluru
        if (goldExpActive1 && !player2.dead && !isSinglePlayer) {
            audio.playSound("sounds/damage.wav");
            player2.skillLock = true;
        } else if (!goldExpActive1) {
            player2.skillLock = false;
        }
        if (goldExpActive2 && !player1.dead) {
            audio.playSound("sounds/damage.wav");
            player1.skillLock = true;
        } else if (!goldExpActive2) {
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
        if (!isSinglePlayer && (player2.timeStopActive || player2.timeReverseActive || goldExpActive2)) return;
        int moveStep = player1.speed ? 2 : 1;
        if (kingCrimsonActive1) moveStep = 3;
        if (kingCrimsonActive2) moveStep = 1;
        int nx = player1.x + dx * moveStep, ny = player1.y + dy * moveStep;
        if (nx >= 0 && nx < ROWS && ny >= 0 && ny < COLS) {
            player1.x = nx; player1.y = ny;
        }
    }
    public void movePlayer2(int dx, int dy) {
        if (player1.timeStopActive || player1.timeReverseActive || goldExpActive1) return;
        int moveStep = player2.speed ? 2 : 1;
        if (kingCrimsonActive2) moveStep = 3;
        if (kingCrimsonActive1) moveStep = 1;
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

    // --- Skill 5-8 ---
    public boolean isTheHandReady1() {
        long now = System.currentTimeMillis();
        return skill1Id == 5 && !showLaser1 && (now - laserCooldownStart1 >= 30000) && !player1.skillLock;
    }
    public boolean isTheHandReady2() {
        long now = System.currentTimeMillis();
        return skill2Id == 5 && !showLaser2 && (now - laserCooldownStart2 >= 30000) && !player2.skillLock;
    }
    public boolean isKingCrimsonReady1() {
        long now = System.currentTimeMillis();
        return skill1Id == 6 && !kingCrimsonActive1 && (now - kingCrimsonCooldownStart1 >= 25000) && !player1.skillLock;
    }
    public boolean isKingCrimsonReady2() {
        long now = System.currentTimeMillis();
        return skill2Id == 6 && !kingCrimsonActive2 && (now - kingCrimsonCooldownStart2 >= 25000) && !player2.skillLock;
    }
    public boolean isSilverChariotReady1() {
        if (skill1Id != 7) return false;
        if (player1.score < 200) return false;
        long now = System.currentTimeMillis();
        return !summon1.active && !summonLocked1 && (now - summonCooldownStart1 >= skill1Cooldown) && !player1.skillLock;
    }
    public boolean isSilverChariotReady2() {
        if (skill2Id != 7) return false;
        if (player2.score < 200) return false;
        long now = System.currentTimeMillis();
        return !summon2.active && !summonLocked2 && (now - summonCooldownStart2 >= skill2Cooldown) && !player2.skillLock;
    }
    public boolean isGoldExpReady1() {
        if (skill1Id != 8) return false;
        if (player1.score < 100) return false;
        long now = System.currentTimeMillis();
        return !goldExpActive1 && !goldExpLocked1 && (now - goldExpCooldownStart1 >= skill1Cooldown) && !player1.skillLock;
    }
    public boolean isGoldExpReady2() {
        if (skill2Id != 8) return false;
        if (player2.score < 100) return false;
        long now = System.currentTimeMillis();
        return !goldExpActive2 && !goldExpLocked2 && (now - goldExpCooldownStart2 >= skill2Cooldown) && !player2.skillLock;
    }

    // --- Skill 5-8 Activation ---
    public void activateTheHandForPlayer1() {
        if (!isTheHandReady1()) return;
        audio.pauseBGM();
        showLaser1 = true;
        laserStart1 = System.currentTimeMillis();
        audio.playSound("sounds/skill5.wav");
    }
    public void activateTheHandForPlayer2() {
        if (!isTheHandReady2()) return;
        audio.pauseBGM();
        showLaser2 = true;
        laserStart2 = System.currentTimeMillis();
        audio.playSound("sounds/skill5.wav");
    }
    public void activateKingCrimsonForPlayer1() {
        if (!isKingCrimsonReady1()) return;
        audio.pauseBGM();
        kingCrimsonActive1 = true;
        kingCrimsonStart1 = System.currentTimeMillis();
        showCrimsonEffect1 = true;
        audio.playSound("sounds/skill6.wav");
    }
    public void activateKingCrimsonForPlayer2() {
        if (!isKingCrimsonReady2()) return;
        audio.pauseBGM();
        kingCrimsonActive2 = true;
        kingCrimsonStart2 = System.currentTimeMillis();
        showCrimsonEffect2 = true;
        audio.playSound("sounds/skill6.wav");
    }
    public void activateSilverChariotForPlayer1() {
        if (!isSilverChariotReady1()) return;
        audio.pauseBGM();
        summon1.summon(player1.x, player1.y);
        summon1.startTime = System.currentTimeMillis();
        summonLastMove1 = summon1.startTime;
        summonHitPause1 = summon1.startTime - 2000;
        audio.playSound("sounds/skill7.wav");
    }
    public void activateSilverChariotForPlayer2() {
        if (!isSilverChariotReady2()) return;
        audio.pauseBGM();
        summon2.summon(player2.x, player2.y);
        summon2.startTime = System.currentTimeMillis();
        summonLastMove2 = summon2.startTime;
        summonHitPause2 = summon2.startTime - 2000;
        audio.playSound("sounds/skill7.wav");
    }
    public void activateGoldExpForPlayer1() {
        if (!isGoldExpReady1()) return;
        audio.pauseBGM();
        goldExpActive1 = true;
        goldExpStart1 = System.currentTimeMillis();
        showGoldEffect1 = true;
        audio.playSound("sounds/skill8.wav");
    }
    public void activateGoldExpForPlayer2() {
        if (!isGoldExpReady2()) return;
        audio.pauseBGM();
        goldExpActive2 = true;
        goldExpStart2 = System.currentTimeMillis();
        showGoldEffect2 = true;
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