package controller;

import model.GameModel;
import view.GameView;
import javax.swing.*;
import java.awt.event.*;

public class GameController implements KeyListener, ActionListener {
    private final GameModel model;
    private final GameView view;
    private final Timer timer;

    public GameController(GameModel model, GameView view, int delay) {
        this.model = model;
        this.view = view;
        this.timer = new Timer(delay, this);
        this.timer.start();
        view.addKeyListener(this);
        view.setFocusable(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        model.updateGame();
        view.repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (model.player1Dead && (model.player2Dead || model.isSinglePlayer)) return;

        if (!model.player1Dead) {
            switch (key) {
                case KeyEvent.VK_W -> model.movePlayer1(-1, 0);
                case KeyEvent.VK_S -> model.movePlayer1(1, 0);
                case KeyEvent.VK_A -> model.movePlayer1(0, -1);
                case KeyEvent.VK_D -> model.movePlayer1(0, 1);
            }
            if (key == model.timeStopKey) model.activateTimeStop();
        }

        if (!model.player2Dead && !model.isSinglePlayer && !model.timeStopActive) {
            switch (key) {
                case KeyEvent.VK_UP -> model.movePlayer2(-1, 0);
                case KeyEvent.VK_DOWN -> model.movePlayer2(1, 0);
                case KeyEvent.VK_LEFT -> model.movePlayer2(0, -1);
                case KeyEvent.VK_RIGHT -> model.movePlayer2(0, 1);
            }
            if (key == model.areaClearKey) model.activateAreaClear();
        }
    }
    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}