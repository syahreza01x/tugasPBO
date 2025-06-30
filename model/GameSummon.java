package model;

public class GameSummon {
    public int x, y;
    public boolean active = false;
    public long startTime = 0;
    public int speed = 2; // Fast speed

    public void summon(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        this.active = true;
        this.startTime = System.currentTimeMillis();
    }

    public void update(GamePlayer target, int rows, int cols) {
        if (!active) return;
        // Move towards target
        if (x < target.x) x += speed;
        else if (x > target.x) x -= speed;
        if (y < target.y) y += speed;
        else if (y > target.y) y -= speed;
        // Clamp
        if (x < 0) x = 0; if (x >= rows) x = rows - 1;
        if (y < 0) y = 0; if (y >= cols) y = cols - 1;
    }
}