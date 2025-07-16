package model;

public class GamePowerup {
    private final DatabaseManager db;

    public GamePowerup(DatabaseManager db) {
        this.db = db;
    }

    public void logPowerup(int playerId, String type) {
        db.logPowerup(playerId, type);
    }

    public void applyEffect(
        GamePlayer target,
        GamePlayer enemy,
        int type,
        int playerId,
        boolean isSinglePlayer
    ) {
        long now = System.currentTimeMillis();

        switch (type) {
            case 1: // Extra life
                if (target.lives < 5) {
                    target.lives++;
                }
                break;

            case 2: // Shield
                target.shield = true;
                target.shieldStart = now;
                break;

            case 3: // Speed
                target.speed = true;
                target.speedStart = now;
                break;

            case 4: // Damage enemy, gain life
                if (!isSinglePlayer && enemy != null && enemy.lives > 1) {
                    enemy.lives--;
                }
                target.lives++;
                break;

            case 5: // Lock enemy skill
                if (!isSinglePlayer && enemy != null) {
                    enemy.skillLock = true;
                    enemy.skillLockStart = now;
                }
                break;
        }

        logPowerup(playerId, String.valueOf(type));
    }
}
