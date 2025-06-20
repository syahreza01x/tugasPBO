package controller;

import model.GameModel;
import view.GameView;
import view.HomePage;
import javax.swing.*;
import java.awt.event.*;

public class GameController implements KeyListener, ActionListener {
    private final GameModel model;
    private final GameView view;
    private final Timer timer;
    private final JFrame frame;
    private final HomePage homePage;

    public GameController(GameModel model, GameView view, int delay, JFrame frame, HomePage homePage) {
        this.model = model;
        this.view = view;
        this.frame = frame;
        this.homePage = homePage;
        this.timer = new Timer(delay, this);
        this.timer.start();
        view.addKeyListener(this);
        view.setFocusable(true);
        view.requestFocusInWindow();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        model.updateGame();
        view.repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        // ESC: kembali ke homepage
        if (key == KeyEvent.VK_ESCAPE) {
            timer.stop();
            model.stopBGM();
            frame.setContentPane(homePage);
            frame.revalidate();
            homePage.refreshHighScore();
            return;
        }

        // Game over check
        if (model.player1.dead && (model.player2.dead || model.isSinglePlayer)) return;

        // Player 1 movement & skill
        if (!model.player1.dead) {
            switch (key) {
                case KeyEvent.VK_W -> model.movePlayer1(-1, 0);
                case KeyEvent.VK_S -> model.movePlayer1(1, 0);
                case KeyEvent.VK_A -> model.movePlayer1(0, -1);
                case KeyEvent.VK_D -> model.movePlayer1(0, 1);
            }
            if (key == model.timeStopKey) {
                if (model.getSkill1Id() == 1) {
                    model.activateTimeStopForPlayer1();
                } else if (model.getSkill1Id() == 2) {
                    model.activateAreaClearForPlayer1();
                } else if (model.getSkill1Id() == 3) {
                    model.activateTimeReverseForPlayer1();
                } else if (model.getSkill1Id() == 4) {
                    model.activateExtraHealthForPlayer1();
                }
            }
        }

        // Player 2 movement & skill (jika multiplayer)
        if (!model.player2.dead && !model.isSinglePlayer && !model.player2.timeStopActive) {
            switch (key) {
                case KeyEvent.VK_UP -> model.movePlayer2(-1, 0);
                case KeyEvent.VK_DOWN -> model.movePlayer2(1, 0);
                case KeyEvent.VK_LEFT -> model.movePlayer2(0, -1);
                case KeyEvent.VK_RIGHT -> model.movePlayer2(0, 1);
            }
            if (key == model.areaClearKey) {
                if (model.getSkill2Id() == 1) {
                    model.activateTimeStopForPlayer2();
                } else if (model.getSkill2Id() == 2) {
                    model.activateAreaClearForPlayer2();
                } else if (model.getSkill2Id() == 3) {
                    model.activateTimeReverseForPlayer2();
                } else if (model.getSkill2Id() == 4) {
                    model.activateExtraHealthForPlayer2();
                }
            }
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}