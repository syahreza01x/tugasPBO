package model;

public class GameSkill {
    public int skill1Id, skill2Id;
    public int skill1Cooldown, skill2Cooldown;
    public String skill1Sound, skill2Sound;

    private final GameModel model;
    private final DatabaseManager db;
    private final GamePlayer p1, p2;
    private final int p1Id, p2Id;

    public GameSkill(GameModel model, DatabaseManager db, int player1Id, int player2Id) {
        this.model = model;
        this.db = db;
        this.p1 = model.player1;
        this.p2 = model.player2;
        this.p1Id = player1Id;
        this.p2Id = player2Id;

        this.skill1Id = db.getPlayerSkill(player1Id);
        if (!model.isSinglePlayer) this.skill2Id = db.getPlayerSkill(player2Id);

        this.skill1Cooldown = db.getSkillCooldown(skill1Id);
        if (!model.isSinglePlayer) this.skill2Cooldown = db.getSkillCooldown(skill2Id);

        this.skill1Sound = db.getSkillSound(skill1Id);
        if (!model.isSinglePlayer) this.skill2Sound = db.getSkillSound(skill2Id);
    }

    // ======= Skill Ready Check =======

    public boolean isTimeStopReady1() {
        if (p1.dead) return false;
        long now = System.currentTimeMillis();
        long pause = p1.pauseAccum;
        if (p2.timeStopActive || p2.timeReverseActive) pause += now - p1.pauseStart;
        return !p1.timeStopActive && (now - p1.timeStopCooldownStart - pause >= skill1Cooldown) && !p1.skillLock;
    }

    public boolean isTimeStopReady2() {
        if (p2.dead) return false;
        long now = System.currentTimeMillis();
        long pause = p2.pauseAccum;
        if (p1.timeStopActive || p1.timeReverseActive) pause += now - p2.pauseStart;
        return !p2.timeStopActive && (now - p2.timeStopCooldownStart - pause >= skill2Cooldown) && !p2.skillLock;
    }

    public boolean isAreaClearReady1() {
        if (p1.dead) return false;
        long now = System.currentTimeMillis();
        long pause = p1.pauseAccum;
        if (p2.timeStopActive || p2.timeReverseActive) pause += now - p1.pauseStart;
        return !p1.areaClearActive && (now - p1.areaClearCooldownStart - pause >= skill1Cooldown) && !p1.skillLock;
    }

    public boolean isAreaClearReady2() {
        if (p2.dead) return false;
        long now = System.currentTimeMillis();
        long pause = p2.pauseAccum;
        if (p1.timeStopActive || p1.timeReverseActive) pause += now - p2.pauseStart;
        return !p2.areaClearActive && (now - p2.areaClearCooldownStart - pause >= skill2Cooldown) && !p2.skillLock;
    }

    public boolean isTimeReverseReady1() {
        if (p1.dead) return false;
        long now = System.currentTimeMillis();
        long pause = p1.pauseAccum;
        if (p2.timeStopActive || p2.timeReverseActive) pause += now - p1.pauseStart;
        return !p1.timeReverseActive && (now - p1.timeReverseCooldownStart - pause >= skill1Cooldown) && !p1.skillLock;
    }

    public boolean isTimeReverseReady2() {
        if (p2.dead) return false;
        long now = System.currentTimeMillis();
        long pause = p2.pauseAccum;
        if (p1.timeStopActive || p1.timeReverseActive) pause += now - p2.pauseStart;
        return !p2.timeReverseActive && (now - p2.timeReverseCooldownStart - pause >= skill2Cooldown) && !p2.skillLock;
    }

    public boolean isTheHandReady1() {
        if (p1.dead) return false;
        long now = System.currentTimeMillis();
        return skill1Id == 5 && !p1.showLaser && (now - p1.laserCooldownStart >= 30000) && !p1.skillLock;
    }

    public boolean isTheHandReady2() {
        if (p2.dead) return false;
        long now = System.currentTimeMillis();
        return skill2Id == 5 && !p2.showLaser && (now - p2.laserCooldownStart >= 30000) && !p2.skillLock;
    }

    public boolean isKingCrimsonReady1() {
        if (p1.dead) return false;
        long now = System.currentTimeMillis();
        return skill1Id == 6 && !p1.kingCrimsonActive && (now - p1.kingCrimsonCooldownStart >= 25000) && !p1.skillLock;
    }

    public boolean isKingCrimsonReady2() {
        if (p2.dead) return false;
        long now = System.currentTimeMillis();
        return skill2Id == 6 && !p2.kingCrimsonActive && (now - p2.kingCrimsonCooldownStart >= 25000) && !p2.skillLock;
    }

    public boolean isSilverChariotReady1() {
        if (p1.dead || skill1Id != 7 || p1.score < 200) return false;
        long now = System.currentTimeMillis();
        return !p1.summon.active && !p1.summonLocked && (now - p1.summonCooldownStart >= skill1Cooldown) && !p1.skillLock;
    }

    public boolean isSilverChariotReady2() {
        if (p2.dead || skill2Id != 7 || p2.score < 200) return false;
        long now = System.currentTimeMillis();
        return !p2.summon.active && !p2.summonLocked && (now - p2.summonCooldownStart >= skill2Cooldown) && !p2.skillLock;
    }

    public boolean isGoldExpReady1() {
        if (p1.dead || skill1Id != 8 || p1.score < 100) return false;
        long now = System.currentTimeMillis();
        return !p1.goldExpActive && !p1.goldExpLocked && (now - p1.goldExpCooldownStart >= skill1Cooldown) && !p1.skillLock;
    }

    public boolean isGoldExpReady2() {
        if (p2.dead || skill2Id != 8 || p2.score < 100) return false;
        long now = System.currentTimeMillis();
        return !p2.goldExpActive && !p2.goldExpLocked && (now - p2.goldExpCooldownStart >= skill2Cooldown) && !p2.skillLock;
    }

    // ======= Skill Activation =======

    public void activateTimeStopForPlayer1() {
        if (p2.timeStopActive || p2.timeReverseActive) return;
        if (isTimeStopReady1()) {
            model.audio.pauseBGM();
            p1.timeStopActive = true;
            p1.timeStopStart = System.currentTimeMillis();
            p2.pauseStart = p1.timeStopStart;
            model.audio.playSound("sounds/" + skill1Sound);
        }
    }

    public void activateTimeStopForPlayer2() {
        if (p1.timeStopActive || p1.timeReverseActive) return;
        if (isTimeStopReady2()) {
            model.audio.pauseBGM();
            p2.timeStopActive = true;
            p2.timeStopStart = System.currentTimeMillis();
            p1.pauseStart = p2.timeStopStart;
            model.audio.playSound("sounds/" + skill2Sound);
        }
    }

    public void activateAreaClearForPlayer1() {
        if (p2.timeStopActive || p2.timeReverseActive) return;
        if (isAreaClearReady1()) {
            model.audio.pauseBGM();
            p1.areaClearActive = true;
            p1.areaClearStart = System.currentTimeMillis();
            p1.showAreaClearEffect = true;
            model.audio.playSound("sounds/" + skill1Sound);
        }
    }

    public void activateAreaClearForPlayer2() {
        if (p1.timeStopActive || p1.timeReverseActive) return;
        if (isAreaClearReady2()) {
            model.audio.pauseBGM();
            p2.areaClearActive = true;
            p2.areaClearStart = System.currentTimeMillis();
            p2.showAreaClearEffect = true;
            model.audio.playSound("sounds/" + skill2Sound);
        }
    }

    public void activateTimeReverseForPlayer1() {
        if (p2.timeStopActive || p2.timeReverseActive) return;
        if (isTimeReverseReady1()) {
            model.audio.pauseBGM();
            p1.timeReverseActive = true;
            p1.timeReverseStart = System.currentTimeMillis();
            p2.pauseStart = p1.timeReverseStart;
            model.audio.playSound("sounds/" + skill1Sound);
        }
    }

    public void activateTimeReverseForPlayer2() {
        if (p1.timeStopActive || p1.timeReverseActive) return;
        if (isTimeReverseReady2()) {
            model.audio.pauseBGM();
            p2.timeReverseActive = true;
            p2.timeReverseStart = System.currentTimeMillis();
            p1.pauseStart = p2.timeReverseStart;
            model.audio.playSound("sounds/" + skill2Sound);
        }
    }

    public void activateExtraHealthForPlayer1() {
        if (p2.timeStopActive || p2.timeReverseActive) return;
        if (p1.lives < 5 && !p1.extraHealthActive) {
            model.audio.pauseBGM();
            p1.lives++;
            p1.extraHealthActive = true;
            p1.extraHealthStart = System.currentTimeMillis();
            p1.areaClearCooldownStart = p1.extraHealthStart;
            model.audio.playSound("sounds/" + skill1Sound);
        }
    }

    public void activateExtraHealthForPlayer2() {
        if (p1.timeStopActive || p1.timeReverseActive) return;
        if (p2.lives < 5 && !p2.extraHealthActive) {
            model.audio.pauseBGM();
            p2.lives++;
            p2.extraHealthActive = true;
            p2.extraHealthStart = System.currentTimeMillis();
            p2.areaClearCooldownStart = p2.extraHealthStart;
            model.audio.playSound("sounds/" + skill2Sound);
        }
    }

    public void activateTheHandForPlayer1() {
        if (!isTheHandReady1()) return;
        model.audio.pauseBGM();
        p1.showLaser = true;
        p1.laserStart = System.currentTimeMillis();
        model.audio.playSound("sounds/skill5.wav");
    }

    public void activateTheHandForPlayer2() {
        if (!isTheHandReady2()) return;
        model.audio.pauseBGM();
        p2.showLaser = true;
        p2.laserStart = System.currentTimeMillis();
        model.audio.playSound("sounds/skill5.wav");
    }

    public void activateKingCrimsonForPlayer1() {
        if (!isKingCrimsonReady1()) return;
        model.audio.pauseBGM();
        p1.kingCrimsonActive = true;
        p1.kingCrimsonStart = System.currentTimeMillis();
        p1.showCrimsonEffect = true;
        model.audio.playSound("sounds/skill6.wav");
    }

    public void activateKingCrimsonForPlayer2() {
        if (!isKingCrimsonReady2()) return;
        model.audio.pauseBGM();
        p2.kingCrimsonActive = true;
        p2.kingCrimsonStart = System.currentTimeMillis();
        p2.showCrimsonEffect = true;
        model.audio.playSound("sounds/skill6.wav");
    }

    public void activateSilverChariotForPlayer1() {
        if (!isSilverChariotReady1()) return;
        model.audio.pauseBGM();
        if (p1.summon != null) {
            long now = System.currentTimeMillis();
            p1.summon.summon(p1.x, p1.y);
            p1.summon.startTime = now;
            p1.summonLastMove = now;
            p1.summonHitPause = now - 2000;
            model.audio.playSound("sounds/skill7.wav");
        }
    }

    public void activateSilverChariotForPlayer2() {
        if (!isSilverChariotReady2()) return;
        model.audio.pauseBGM();
        if (p2.summon != null) {
            long now = System.currentTimeMillis();
            p2.summon.summon(p2.x, p2.y);
            p2.summon.startTime = now;
            p2.summonLastMove = now;
            p2.summonHitPause = now - 2000;
            model.audio.playSound("sounds/skill7.wav");
        }
    }

    public void activateGoldExpForPlayer1() {
        if (!isGoldExpReady1()) return;
        model.audio.pauseBGM();
        p1.goldExpActive = true;
        p1.goldExpStart = System.currentTimeMillis();
        p1.showGoldEffect = true;
        model.audio.playSound("sounds/skill8.wav");
    }

    public void activateGoldExpForPlayer2() {
        if (!isGoldExpReady2()) return;
        model.audio.pauseBGM();
        p2.goldExpActive = true;
        p2.goldExpStart = System.currentTimeMillis();
        p2.showGoldEffect = true;
        model.audio.playSound("sounds/skill8.wav");
    }
}
