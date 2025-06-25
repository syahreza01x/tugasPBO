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

        // ESC: kembali ke homepage dan stop semua sound
        if (key == KeyEvent.VK_ESCAPE) {
            timer.stop();
            model.audio.stopAllSounds();
            frame.setContentPane(homePage);
            frame.revalidate();
            homePage.refreshHighScore();
            return;
        }

        // Game over check
        if (model.player1.dead && (model.player2.dead || model.isSinglePlayer)) return;

        // Player 1 movement & skill
        if (!model.player1.dead) {
            if (key == model.p1UpKey) model.movePlayer1(-1, 0);
            else if (key == model.p1DownKey) model.movePlayer1(1, 0);
            else if (key == model.p1LeftKey) model.movePlayer1(0, -1);
            else if (key == model.p1RightKey) model.movePlayer1(0, 1);
            else if (key == model.p1SkillKey) {
                if (model.getSkill1Id() == 1) model.activateTimeStopForPlayer1();
                else if (model.getSkill1Id() == 2) model.activateAreaClearForPlayer1();
                else if (model.getSkill1Id() == 3) model.activateTimeReverseForPlayer1();
                else if (model.getSkill1Id() == 4) model.activateExtraHealthForPlayer1();
            }
        }

        // Player 2 movement & skill (jika multiplayer)
        if (!model.player2.dead && !model.isSinglePlayer) {
            if (key == model.p2UpKey) model.movePlayer2(-1, 0);
            else if (key == model.p2DownKey) model.movePlayer2(1, 0);
            else if (key == model.p2LeftKey) model.movePlayer2(0, -1);
            else if (key == model.p2RightKey) model.movePlayer2(0, 1);
            else if (key == model.p2SkillKey) {
                if (model.getSkill2Id() == 1) model.activateTimeStopForPlayer2();
                else if (model.getSkill2Id() == 2) model.activateAreaClearForPlayer2();
                else if (model.getSkill2Id() == 3) model.activateTimeReverseForPlayer2();
                else if (model.getSkill2Id() == 4) model.activateExtraHealthForPlayer2();
            }
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}