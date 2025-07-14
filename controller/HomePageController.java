package controller;

import view.HomePage;
import model.DatabaseManager;

import javax.swing.*;
import java.awt.event.*;
import java.util.List;

public class HomePageController {
    private final HomePage homePage;
    private final DatabaseManager db;

    public HomePageController(HomePage homePage, DatabaseManager db) {
        this.homePage = homePage;
        this.db = db;
        initListeners();
    }

    private void initListeners() {
        homePage.getRegisterButton().addActionListener(e -> {
            homePage.showRegisterDialog();
            homePage.getTableModel().loadPlayers();
            homePage.updateButtonState();
            homePage.getHighScoreModel().loadHighScores();
        });

        homePage.getEditButton().addActionListener(e -> {
            int idx = homePage.getTableModel().getFirstSelectedIndex();
            if (idx != -1) {
                int playerId = homePage.getTableModel().getPlayerId(idx);
                String username = homePage.getTableModel().getPlayerName(idx);
                int skillId = homePage.getTableModel().getPlayerSkillId(idx);
                if (!homePage.verifyPassword(username)) {
                    JOptionPane.showMessageDialog(homePage, "Password salah atau aksi dibatalkan!");
                    return;
                }
                homePage.showEditDialog(playerId, username, skillId);
                homePage.getTableModel().loadPlayers();
                homePage.updateButtonState();
                homePage.getHighScoreModel().loadHighScores();
            }
        });

        homePage.getDeleteButton().addActionListener(e -> {
            List<Integer> selectedIdx = homePage.getTableModel().getSelectedIndexes();
            if (!selectedIdx.isEmpty()) {
                String username = homePage.getTableModel().getPlayerName(selectedIdx.get(0));
                if (!homePage.verifyPassword(username)) {
                    JOptionPane.showMessageDialog(homePage, "Password salah atau aksi dibatalkan!");
                    return;
                }
                int confirm = JOptionPane.showConfirmDialog(homePage, "Yakin ingin menghapus player terpilih?", "Hapus Player", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    for (int idx : selectedIdx) {
                        int playerId = homePage.getTableModel().getPlayerId(idx);
                        db.deletePlayer(playerId);
                    }
                    homePage.getTableModel().loadPlayers();
                    homePage.updateButtonState();
                    homePage.getHighScoreModel().loadHighScores();
                }
            }
        });

        homePage.getPlayButton().addActionListener(e -> homePage.handlePlayAction());

        homePage.getControlButton().addActionListener(e -> homePage.showControlSettingDialog());
    }
}