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

        // Gambar arena
        for (int i = 0; i < model.ROWS; i++) {
            for (int j = 0; j < model.COLS; j++) {
                char c = model.arena[i][j];
                g.setColor(c == '♥' ? Color.RED : c == '♦' ? Color.BLUE : c == '*' ? Color.YELLOW : c == 'X' ? Color.ORANGE : Color.WHITE);
                g.drawString(String.valueOf(c == '\0' ? ' ' : c), j * cw + cw / 4, i * ch + (3 * ch / 4));
            }
        }

        // Gambar drops
        for (GameModel.Drop d : model.drops) {
            g.setColor(d.type == 1 ? Color.GREEN : d.type == 2 ? Color.CYAN : d.type == 3 ? Color.MAGENTA : d.type == 4 ? Color.RED : Color.ORANGE);
            g.drawString(String.valueOf(d.icon), d.y * cw + cw / 4, d.x * ch + (3 * ch / 4));
        }

        // Efek Time Stop
        if (model.showTimeStopEffect1 || model.showTimeStopEffect2) {
            g.setColor(new Color(0, 255, 255, 80));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
        // Efek Time Reverse
        if (model.showTimeReverseEffect1 || model.showTimeReverseEffect2) {
            g.setColor(new Color(255, 0, 255, 80));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
        // Efek Area Clear Player 1
        if (model.showAreaClearEffect1) {
            int px = model.heartY1 * cw + cw / 2;
            int py = model.heartX1 * ch + ch / 2;
            g.setColor(new Color(255, 255, 0, 80));
            g.fillOval(px - 30, py - 30, 60, 60);
        }
        // Efek Area Clear Player 2
        if (model.showAreaClearEffect2 && !model.isSinglePlayer) {
            int px = model.heartY2 * cw + cw / 2;
            int py = model.heartX2 * ch + ch / 2;
            g.setColor(new Color(255, 255, 0, 80));
            g.fillOval(px - 30, py - 30, 60, 60);
        }

        // Skor, highscore, skill status, dan health
        g.setColor(Color.GREEN);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString(model.username1 + " Score: " + model.score1, 20, 30);
        g.drawString("High Score " + model.username1 + ": " + model.highScore1, 20, 60);

        // Status skill Player 1 (PAUSE hanya jika skill lawan aktif)
        String skillStatus1;
        int cd1 = model.getSkill1Cooldown();
        long now = System.currentTimeMillis();
        long pause1 = model.pauseAccumP1;
        if (model.timeStopActive2 || model.timeReverseActive2) pause1 += now - model.pauseStartP1;
        if (model.getSkill1Id() == 1) { // Time Stop
            long sisa = cd1 - (now - model.timeStopCooldownStart1 - pause1);
            skillStatus1 = model.isTimeStopReady1() ? "Ready" : "Cooldown: " + Math.max(0, sisa / 1000) + "s";
        } else if (model.getSkill1Id() == 2) { // Area Clear
            long sisa = cd1 - (now - model.areaClearCooldownStart1 - pause1);
            skillStatus1 = model.isAreaClearReady1() ? "Ready" : "Cooldown: " + Math.max(0, sisa / 1000) + "s";
        } else if (model.getSkill1Id() == 3) { // Time Reverse
            long sisa = cd1 - (now - model.timeReverseCooldownStart1 - pause1);
            skillStatus1 = model.isTimeReverseReady1() ? "Ready" : "Cooldown: " + Math.max(0, sisa / 1000) + "s";
        } else { // Extra Health
            long sisa = cd1 - (now - model.areaClearCooldownStart1 - pause1);
            skillStatus1 = (model.lives1 < 5) ? (sisa <= 0 ? "Ready" : "Cooldown: " + Math.max(0, sisa / 1000) + "s") : "Max Health";
        }
        g.drawString("Skill: " + skillStatus1, 20, 90);

        g.setColor(Color.WHITE);
        g.drawString("Lives P1 : ", 250, 30);
        for (int i = 0; i < model.lives1; i++) {
            g.setColor(Color.PINK);
            g.drawString("♥", 350 + (i * 20), 30);
        }

        if (!model.isSinglePlayer) {
            g.setColor(Color.GREEN);
            g.drawString(model.username2 + " Score: " + model.score2, 20, 120);
            g.drawString("High Score " + model.username2 + ": " + model.highScore2, 20, 150);

            // Status skill Player 2 (PAUSE hanya jika skill lawan aktif)
            String skillStatus2;
            int cd2 = model.getSkill2Cooldown();
            long pause2 = model.pauseAccumP2;
            if (model.timeStopActive1 || model.timeReverseActive1) pause2 += now - model.pauseStartP2;
            if (model.getSkill2Id() == 1) {
                long sisa = cd2 - (now - model.timeStopCooldownStart2 - pause2);
                skillStatus2 = model.isTimeStopReady2() ? "Ready" : "Cooldown: " + Math.max(0, sisa / 1000) + "s";
            } else if (model.getSkill2Id() == 2) {
                long sisa = cd2 - (now - model.areaClearCooldownStart2 - pause2);
                skillStatus2 = model.isAreaClearReady2() ? "Ready" : "Cooldown: " + Math.max(0, sisa / 1000) + "s";
            } else if (model.getSkill2Id() == 3) {
                long sisa = cd2 - (now - model.timeReverseCooldownStart2 - pause2);
                skillStatus2 = model.isTimeReverseReady2() ? "Ready" : "Cooldown: " + Math.max(0, sisa / 1000) + "s";
            } else {
                long sisa = cd2 - (now - model.areaClearCooldownStart2 - pause2);
                skillStatus2 = (model.lives2 < 5) ? (sisa <= 0 ? "Ready" : "Cooldown: " + Math.max(0, sisa / 1000) + "s") : "Max Health";
            }
            g.drawString("Skill: " + skillStatus2, 20, 180);

            g.setColor(Color.WHITE);
            g.drawString("Lives P2 : ", 250, 60);
            for (int i = 0; i < model.lives2; i++) {
                g.setColor(Color.CYAN);
                g.drawString("♦", 350 + (i * 20), 60);
            }
        }

        // Efek shield
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