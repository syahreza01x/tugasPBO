package model;

public class GamePowerup {
    private final DatabaseManager db;

    public GamePowerup(DatabaseManager db) {
        this.db = db;
    }

    public void logPowerup(int playerId, String type) {
        db.logPowerup(playerId, type);
    }
}