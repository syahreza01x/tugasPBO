package view;

import javax.swing.*;
import java.awt.*;
import model.GameModel;
import model.GameDrop;

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
        for (GameDrop.Drop d : model.drop.drops) {
            g.setColor(d.type == 1 ? Color.GREEN : d.type == 2 ? Color.CYAN : d.type == 3 ? Color.MAGENTA : d.type == 4 ? Color.RED : Color.ORANGE);
            g.drawString(String.valueOf(d.icon), d.y * cw + cw / 4, d.x * ch + (3 * ch / 4));
        }

        // Efek Time Stop
        if (model.player1.showTimeStopEffect || model.player2.showTimeStopEffect) {
            g.setColor(new Color(0, 255, 255, 80));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
        // Efek Time Reverse
        if (model.player1.showTimeReverseEffect || model.player2.showTimeReverseEffect) {
            g.setColor(new Color(255, 0, 255, 80));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
        // Efek Area Clear Player 1
        if (model.player1.showAreaClearEffect) {
            int px = model.player1.y * cw + cw / 2;
            int py = model.player1.x * ch + ch / 2;
            g.setColor(new Color(255, 255, 0, 80));
            g.fillOval(px - 30, py - 30, 60, 60);
        }
        // Efek Area Clear Player 2
        if (model.player2.showAreaClearEffect && !model.isSinglePlayer) {
            int px = model.player2.y * cw + cw / 2;
            int py = model.player2.x * ch + ch / 2;
            g.setColor(new Color(255, 255, 0, 80));
            g.fillOval(px - 30, py - 30, 60, 60);
        }

        // Skor, highscore, skill status, dan health
        g.setColor(Color.GREEN);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString(model.player1.username + " Score: " + model.player1.score, 20, 30);
        g.drawString("High Score " + model.player1.username + ": " + model.player1.highScore, 20, 60);

        // Status skill Player 1
        String skillStatus1;
        int cd1 = model.getSkill1Cooldown();
        long now = System.currentTimeMillis();
        long pause1 = model.player1.pauseAccum;
        if (model.player2.timeStopActive || model.player2.timeReverseActive) pause1 += now - model.player1.pauseStart;
        if (model.getSkill1Id() == 1) { // Time Stop
            long sisa = cd1 - (now - model.player1.timeStopCooldownStart - pause1);
            skillStatus1 = model.isTimeStopReady1() ? "Ready" : "Cooldown: " + Math.max(0, sisa / 1000) + "s";
        } else if (model.getSkill1Id() == 2) { // Area Clear
            long sisa = cd1 - (now - model.player1.areaClearCooldownStart - pause1);
            skillStatus1 = model.isAreaClearReady1() ? "Ready" : "Cooldown: " + Math.max(0, sisa / 1000) + "s";
        } else if (model.getSkill1Id() == 3) { // Time Reverse
            long sisa = cd1 - (now - model.player1.timeReverseCooldownStart - pause1);
            skillStatus1 = model.isTimeReverseReady1() ? "Ready" : "Cooldown: " + Math.max(0, sisa / 1000) + "s";
        } else { // Extra Health
            long sisa = cd1 - (now - model.player1.areaClearCooldownStart - pause1);
            skillStatus1 = (model.player1.lives < 5) ? (sisa <= 0 ? "Ready" : "Cooldown: " + Math.max(0, sisa / 1000) + "s") : "Max Health";
        }
        g.drawString("Skill: " + skillStatus1, 20, 90);

        g.setColor(Color.WHITE);
        g.drawString("Lives P1 : ", 250, 30);
        for (int i = 0; i < model.player1.lives; i++) {
            g.setColor(Color.PINK);
            g.drawString("♥", 350 + (i * 20), 30);
        }

        if (!model.isSinglePlayer) {
            g.setColor(Color.GREEN);
            g.drawString(model.player2.username + " Score: " + model.player2.score, 20, 120);
            g.drawString("High Score " + model.player2.username + ": " + model.player2.highScore, 20, 150);

            // Status skill Player 2
            String skillStatus2;
            int cd2 = model.getSkill2Cooldown();
            long pause2 = model.player2.pauseAccum;
            if (model.player1.timeStopActive || model.player1.timeReverseActive) pause2 += now - model.player2.pauseStart;
            if (model.getSkill2Id() == 1) {
                long sisa = cd2 - (now - model.player2.timeStopCooldownStart - pause2);
                skillStatus2 = model.isTimeStopReady2() ? "Ready" : "Cooldown: " + Math.max(0, sisa / 1000) + "s";
            } else if (model.getSkill2Id() == 2) {
                long sisa = cd2 - (now - model.player2.areaClearCooldownStart - pause2);
                skillStatus2 = model.isAreaClearReady2() ? "Ready" : "Cooldown: " + Math.max(0, sisa / 1000) + "s";
            } else if (model.getSkill2Id() == 3) {
                long sisa = cd2 - (now - model.player2.timeReverseCooldownStart - pause2);
                skillStatus2 = model.isTimeReverseReady2() ? "Ready" : "Cooldown: " + Math.max(0, sisa / 1000) + "s";
            } else {
                long sisa = cd2 - (now - model.player2.areaClearCooldownStart - pause2);
                skillStatus2 = (model.player2.lives < 5) ? (sisa <= 0 ? "Ready" : "Cooldown: " + Math.max(0, sisa / 1000) + "s") : "Max Health";
            }
            g.drawString("Skill: " + skillStatus2, 20, 180);

            g.setColor(Color.WHITE);
            g.drawString("Lives P2 : ", 250, 60);
            for (int i = 0; i < model.player2.lives; i++) {
                g.setColor(Color.CYAN);
                g.drawString("♦", 350 + (i * 20), 60);
            }
        }

        // Efek shield
        if (model.player1.shield) {
            g.setColor(new Color(0, 255, 255, 100));
            g.fillOval(model.player1.y * cw - 5, model.player1.x * ch - 5, fontSize + 10, fontSize + 10);
        }
        if (model.player2.shield && !model.isSinglePlayer) {
            g.setColor(new Color(0, 255, 255, 100));
            g.fillOval(model.player2.y * cw - 5, model.player2.x * ch - 5, fontSize + 10, fontSize + 10);
        }
    }
}