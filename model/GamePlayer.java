package model;

public class GamePlayer {
    public int x, y;
    public int score = 0, highScore = 0, lives = 3;
    public boolean dead = false;
    public String username = "";

    // Efek & status
    public boolean shield = false, speed = false, skillLock = false;
    public long shieldStart = 0, speedStart = 0, skillLockStart = 0;

    // Efek visual & skill
    public boolean showTimeStopEffect = false, showAreaClearEffect = false, showTimeReverseEffect = false;
    public boolean timeStopActive = false, areaClearActive = false, timeReverseActive = false;
    public long timeStopStart = 0, timeStopCooldownStart = -15000;
    public long areaClearStart = 0, areaClearCooldownStart = -20000;
    public long timeReverseStart = 0, timeReverseCooldownStart = -20000;

    public long pauseStart = 0, pauseAccum = 0;
    public boolean extraHealthActive = false;
    public long extraHealthStart = 0;

    public boolean showLaser = false;
    public long laserStart = 0;
    public long laserCooldownStart = -30000;

    public boolean kingCrimsonActive = false;
    public long kingCrimsonStart = 0;
    public long kingCrimsonCooldownStart = -25000;
    public boolean showCrimsonEffect = false;

    public GameSummon summon = new GameSummon();
    public boolean summonLocked = false;
    public long summonCooldownStart = -120000;
    public long summonLastMove = 0;
    public long summonHitPause = 0;

    public boolean goldExpActive = false;
    public long goldExpStart = 0;
    public long goldExpCooldownStart = -120000;
    public boolean showGoldEffect = false;
    public boolean goldExpLocked = false;

    public void resetSkillCooldowns(int cooldown) {
    this.timeStopCooldownStart = -cooldown;
    this.areaClearCooldownStart = -cooldown;
    this.timeReverseCooldownStart = -cooldown;
    this.laserCooldownStart = -cooldown;
    this.kingCrimsonCooldownStart = -25000;
    this.summonCooldownStart = -120000;
    this.goldExpCooldownStart = -120000;
}

    public GamePlayer(int startX, int startY) {
        this.x = startX;
        this.y = startY;
    }
}
