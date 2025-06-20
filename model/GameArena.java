package model;

import java.util.Random;

public class GameArena {
    private final int ROWS, COLS;
    private final char[][] arena;

    public GameArena(int rows, int cols, char[][] arena) {
        this.ROWS = rows;
        this.COLS = cols;
        this.arena = arena;
    }

    public void updateBullets(GamePlayer player1, GamePlayer player2) {
        for (int i = ROWS - 2; i >= 0; i--) {
            for (int j = 0; j < COLS; j++) {
                if (arena[i][j] == '*') {
                    if (arena[i + 1][j] == '♥' || arena[i + 1][j] == '♦') {
                        arena[i + 1][j] = 'X';
                    } else {
                        arena[i + 1][j] = '*';
                    }
                    arena[i][j] = ' ';
                }
            }
        }
    }

    public void spawnBullets() {
        Random rand = new Random();
        for (int i = 0; i < COLS; i++) {
            if (rand.nextInt(300) < 10) arena[0][i] = '*';
        }
    }

    public void clearBulletsAroundPlayer(GamePlayer p) {
        int cx = p.x, cy = p.y;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int x = cx + i, y = cy + j;
                if (x >= 0 && x < ROWS && y >= 0 && y < COLS && arena[x][y] == '*') {
                    arena[x][y] = ' ';
                }
            }
        }
    }
}