package model;

import java.util.*;

public class GameDrop {
    public static class Drop {
        public int x, y, type;
        public char icon;
        public Drop(int x, int y, char icon, int type) {
            this.x = x; this.y = y; this.icon = icon; this.type = type;
        }
    }

    public List<Drop> drops = new ArrayList<>();
    private final Random rand = new Random();

    public void spawnDrops(int ROWS, int COLS) {
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

    public void updateDrops(GameModel model) {
        List<Drop> toRemove = new ArrayList<>();
        for (Drop d : drops) {
            if (rand.nextInt(2) == 0) {
                if (d.x < model.ROWS - 1) d.x++;
                else toRemove.add(d);
            }
            if (d.x == model.player1.x && d.y == model.player1.y && !model.player1.dead) {
                model.applyDropEffect(model.player1, d.type, 1);
                toRemove.add(d);
            } else if (!model.isSinglePlayer && d.x == model.player2.x && d.y == model.player2.y && !model.player2.dead) {
                model.applyDropEffect(model.player2, d.type, 2);
                toRemove.add(d);
            }
        }
        drops.removeAll(toRemove);
    }

    public void clear() {
        drops.clear();
    }
}