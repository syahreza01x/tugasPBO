package model;

public class GameSkill {
    public int skill1Id, skill2Id;
    public int skill1Cooldown, skill2Cooldown;
    public String skill1Sound, skill2Sound;

    public GameSkill(GameModel model, DatabaseManager db, int player1Id, int player2Id) {
        this.skill1Id = db.getPlayerSkill(player1Id);
        if (!model.isSinglePlayer) this.skill2Id = db.getPlayerSkill(player2Id);

        this.skill1Cooldown = db.getSkillCooldown(skill1Id);
        if (!model.isSinglePlayer) this.skill2Cooldown = db.getSkillCooldown(skill2Id);

        this.skill1Sound = db.getSkillSound(skill1Id);
        if (!model.isSinglePlayer) this.skill2Sound = db.getSkillSound(skill2Id);
    }
}