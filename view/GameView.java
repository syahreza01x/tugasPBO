package view;

import javax.swing.*;
import java.awt.*;
import model.GameModel;

public class GameView extends JPanel {
    private final GameModel model;

    public GameView(GameModel model) {
        this.model = model;
        setBackground(Color.BLACK);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int cw = getWidth() / model.COLS;
        int ch = getHeight() / model.ROWS;
        int fontSize = Math.min(cw, ch) - 2;
        g.setFont(new Font("Monospaced", Font.BOLD, fontSize));

        // Draw arena
        for (int i = 0; i < model.ROWS; i++) {
            for (int j = 0; j < model.COLS; j++) {
                char c = model.arena[i][j];
                g.setColor(c == '♥' ? Color.RED : c == '♦' ? Color.BLUE : c == '*' ? Color.YELLOW : c == 'X' ? Color.ORANGE : Color.WHITE);
                g.drawString(String.valueOf(c == '\0' ? ' ' : c), j * cw + cw / 4, i * ch + (3 * ch / 4));
            }
        }

        // Draw drops
        for (GameModel.Drop d : model.drops) {
            g.setColor(d.type == 1 ? Color.GREEN : d.type == 2 ? Color.CYAN : d.type == 3 ? Color.MAGENTA : d.type == 4 ? Color.RED : Color.ORANGE);
            g.drawString(String.valueOf(d.icon), d.y * cw + cw / 4, d.x * ch + (3 * ch / 4));
        }

        // Draw effects
        if (model.showTimeStopEffect) {
            g.setColor(new Color(0, 255, 255, 80));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
        if (model.showAreaClearEffect && !model.isSinglePlayer) {
            int px = model.heartY2 * getWidth() / model.COLS;
            int py = model.heartX2 * getHeight() / model.ROWS;
            g.setColor(new Color(255, 255, 0, 80));
            g.fillOval(px - 30, py - 30, 60, 60);
        }

        // Draw player 1 info
        g.setColor(Color.GREEN);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("Player 1 Score: " + model.score1, 20, 30);
        g.drawString("High Score P1: " + model.highScore1, 20, 60);
        g.drawString("Skill: " + (model.isTimeStopReady() ? "Ready" : "Cooldown"), 20, 90);

        g.setColor(Color.WHITE);
        g.drawString("Lives P1 : ", 250, 30);
        for (int i = 0; i < model.lives1; i++) {
            g.setColor(Color.PINK);
            g.drawString("♥", 350 + (i * 20), 30);
        }

        // Draw player 2 info if multiplayer
        if (!model.isSinglePlayer) {
            g.setColor(Color.GREEN);
            g.drawString("Player 2 Score: " + model.score2, 20, 120);
            g.drawString("High Score P2: " + model.highScore2, 20, 150);
            g.drawString("Skill: " + (model.isAreaClearReady() ? "Ready" : "Cooldown"), 20, 180);

            g.setColor(Color.WHITE);
            g.drawString("Lives P2 : ", 250, 60);
            for (int i = 0; i < model.lives2; i++) {
                g.setColor(Color.CYAN);
                g.drawString("♦", 350 + (i * 20), 60);
            }
        }

        // Draw active power-ups
        if (model.shield1) {
            g.setColor(new Color(0, 255, 255, 100));
            g.fillOval(model.heartY1 * cw - 5, model.heartX1 * ch - 5, fontSize + 10, fontSize + 10);
        }
        if (model.shield2 && !model.isSinglePlayer) {
            g.setColor(new Color(0, 255, 255, 100));
            g.fillOval(model.heartY2 * cw - 5, model.heartX2 * ch - 5, fontSize + 10, fontSize + 10);
        }
    }
}