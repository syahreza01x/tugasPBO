package view;

import model.DatabaseManager;
import model.DatabaseManager.Player;
import model.DatabaseManager.Skill;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.*;

public class HomePage extends JPanel {
    private final DatabaseManager db;
    private final JButton registerButton = new JButton("Register");
    private final JButton editButton = new JButton("Edit");
    private final JButton deleteButton = new JButton("Delete");
    private final JButton playButton = new JButton("Play");
    private final JFrame parentFrame;

    private PlayerTableModel tableModel;
    private JTable table;

    private HighScoreTableModel highScoreModel;
    private JTable highScoreTable;

    public interface PlayListener {
        void onPlay(List<Integer> playerIds, List<String> usernames);
    }

    public HomePage(DatabaseManager db, JFrame parentFrame, PlayListener playListener) {
        this.db = db;
        this.parentFrame = parentFrame;

        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Game Kelompok 5 PBO", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 30));
        add(title, BorderLayout.NORTH);

        // --- KIRI: Data Player ---
        tableModel = new PlayerTableModel();
        table = new JTable(tableModel);

        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(3).setMaxWidth(60);
        table.setRowHeight(32);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));

        // Pilih (checkbox) renderer
        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                JCheckBox checkBox = new JCheckBox();
                checkBox.setSelected((Boolean) value);
                checkBox.setHorizontalAlignment(SwingConstants.CENTER);
                checkBox.setEnabled(true);
                return checkBox;
            }
        });

        // Agar klik checkbox langsung aktif
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point point = e.getPoint();
                int row = table.rowAtPoint(point);
                int column = table.columnAtPoint(point);
                if (column == 3) { // kolom checkbox
                    boolean current = (boolean) tableModel.getValueAt(row, column);
                    tableModel.setSelected(row, !current);
                    table.repaint();
                    updateButtonState();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(400, 350));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        registerButton.setPreferredSize(new Dimension(100, 30));
        editButton.setPreferredSize(new Dimension(100, 30));
        deleteButton.setPreferredSize(new Dimension(100, 30));
        playButton.setPreferredSize(new Dimension(100, 30));
        playButton.setEnabled(false);
        editButton.setEnabled(false);
        deleteButton.setEnabled(false);

        buttonPanel.add(registerButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(playButton);

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout(10, 10));
        leftPanel.add(scrollPane, BorderLayout.CENTER);
        leftPanel.add(buttonPanel, BorderLayout.SOUTH);

        // --- KANAN: Top 5 High Score ---
        highScoreModel = new HighScoreTableModel();
        highScoreTable = new JTable(highScoreModel);
        highScoreTable.setRowHeight(28);
        highScoreTable.setFont(new Font("Arial", Font.PLAIN, 14));
        highScoreTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        highScoreTable.getColumnModel().getColumn(0).setMaxWidth(50);

        JScrollPane highScoreScroll = new JScrollPane(highScoreTable);
        highScoreScroll.setPreferredSize(new Dimension(300, 200));

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout(10, 10));
        JLabel hsTitle = new JLabel("Top 5 High Score", SwingConstants.CENTER);
        hsTitle.setFont(new Font("Arial", Font.BOLD, 18));
        rightPanel.add(hsTitle, BorderLayout.NORTH);
        rightPanel.add(highScoreScroll, BorderLayout.CENTER);

        // --- Gabung Kiri & Kanan ---
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.X_AXIS));
        centerPanel.add(leftPanel);

        // Spacer (jarak antar panel)
        centerPanel.add(Box.createRigidArea(new Dimension(40, 0)));

        centerPanel.add(rightPanel);

        add(centerPanel, BorderLayout.CENTER);

        // --- Button Action ---
        registerButton.addActionListener(e -> {
            showRegisterDialog();
            tableModel.loadPlayers();
            updateButtonState();
            highScoreModel.loadHighScores();
        });

        editButton.addActionListener(e -> {
            int idx = tableModel.getFirstSelectedIndex();
            if (idx != -1) {
                int playerId = tableModel.getPlayerId(idx);
                String username = tableModel.getPlayerName(idx);
                int skillId = tableModel.getPlayerSkillId(idx);
                showEditDialog(playerId, username, skillId);
                tableModel.loadPlayers();
                updateButtonState();
                highScoreModel.loadHighScores();
            }
        });

        deleteButton.addActionListener(e -> {
            List<Integer> selectedIdx = tableModel.getSelectedIndexes();
            if (!selectedIdx.isEmpty()) {
                int confirm = JOptionPane.showConfirmDialog(this, "Yakin ingin menghapus player terpilih?", "Hapus Player", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    for (int idx : selectedIdx) {
                        int playerId = tableModel.getPlayerId(idx);
                        db.deletePlayer(playerId);
                    }
                    tableModel.loadPlayers();
                    updateButtonState();
                    highScoreModel.loadHighScores();
                }
            }
        });

        playButton.addActionListener(e -> {
            List<Integer> ids = new ArrayList<>();
            List<String> names = new ArrayList<>();
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                if (tableModel.isSelected(i)) {
                    ids.add(tableModel.getPlayerId(i));
                    names.add(tableModel.getPlayerName(i));
                }
            }
            playListener.onPlay(ids, names);
        });

        tableModel.loadPlayers();
        highScoreModel.loadHighScores();
    }

    private void updateButtonState() {
        int selected = tableModel.getSelectedCount();
        playButton.setEnabled(selected >= 1 && selected <= 2);
        editButton.setEnabled(selected == 1);
        deleteButton.setEnabled(selected >= 1);
    }

    private void showRegisterDialog() {
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JComboBox<String> skillBox = new JComboBox<>();
        var skills = db.getAllSkills();
        for (var s : skills) skillBox.addItem(s.namaSkill);

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(new JLabel("Skill:"));
        panel.add(skillBox);

        int result = JOptionPane.showConfirmDialog(this, panel, "Register Player", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username dan password wajib diisi!");
                return;
            }
            if (db.isUsernameExist(username)) {
                JOptionPane.showMessageDialog(this, "Username sudah terdaftar!");
                return;
            }
            int skillId = db.getAllSkills().get(skillBox.getSelectedIndex()).id;
            db.registerPlayer(username, password, skillId);
        }
    }

    private void showEditDialog(int playerId, String oldUsername, int oldSkillId) {
        JTextField usernameField = new JTextField(oldUsername);
        JComboBox<String> skillBox = new JComboBox<>();
        var skills = db.getAllSkills();
        int selectedSkillIdx = 0;
        for (int i = 0; i < skills.size(); i++) {
            skillBox.addItem(skills.get(i).namaSkill);
            if (skills.get(i).id == oldSkillId) selectedSkillIdx = i;
        }
        skillBox.setSelectedIndex(selectedSkillIdx);

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Skill:"));
        panel.add(skillBox);

        int result = JOptionPane.showConfirmDialog(this, panel, "Edit Player", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String username = usernameField.getText().trim();
            int skillId = skills.get(skillBox.getSelectedIndex()).id;
            if (username.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username wajib diisi!");
                return;
            }
            if (!username.equals(oldUsername) && db.isUsernameExist(username)) {
                JOptionPane.showMessageDialog(this, "Username sudah terdaftar!");
                return;
            }
            db.updatePlayer(playerId, username, skillId);
        }
    }

    // --- Table Model ---
    class PlayerTableModel extends AbstractTableModel {
        private final String[] columns = {"No", "Player", "Skill", "Pilih"};
        private final List<Player> playerList = new ArrayList<>();
        private final List<String> skillNames = new ArrayList<>();
        private final List<Boolean> selected = new ArrayList<>();

        public void loadPlayers() {
            playerList.clear();
            skillNames.clear();
            selected.clear();

            List<Player> fetched = db.getAllPlayers();
            List<Skill> skills = db.getAllSkills();
            Map<Integer, String> skillMap = new HashMap<>();
            for (Skill s : skills) skillMap.put(s.id, s.namaSkill);

            for (Player p : fetched) {
                playerList.add(p);
                skillNames.add(skillMap.getOrDefault(p.skillId, "Unknown"));
                selected.add(false);
            }
            fireTableDataChanged();
        }

        public int getRowCount() {
            return playerList.size();
        }

        public int getColumnCount() {
            return columns.length;
        }

        public Object getValueAt(int row, int col) {
            switch (col) {
                case 0: return row + 1;
                case 1: return playerList.get(row).username;
                case 2: return skillNames.get(row);
                case 3: return selected.get(row);
            }
            return null;
        }

        public String getColumnName(int col) {
            return columns[col];
        }

        public boolean isSelected(int row) {
            return selected.get(row);
        }

        public void setSelected(int row, boolean value) {
            int count = getSelectedCount();
            if (value && count >= 2) return;
            selected.set(row, value);
            fireTableCellUpdated(row, 3);
        }

        public int getSelectedCount() {
            return (int) selected.stream().filter(Boolean::booleanValue).count();
        }

        public int getFirstSelectedIndex() {
            for (int i = 0; i < selected.size(); i++) {
                if (selected.get(i)) return i;
            }
            return -1;
        }

        public List<Integer> getSelectedIndexes() {
            List<Integer> idxs = new ArrayList<>();
            for (int i = 0; i < selected.size(); i++) {
                if (selected.get(i)) idxs.add(i);
            }
            return idxs;
        }

        public int getPlayerId(int row) {
            return playerList.get(row).id;
        }

        public String getPlayerName(int row) {
            return playerList.get(row).username;
        }

        public int getPlayerSkillId(int row) {
            return playerList.get(row).skillId;
        }

        public Class<?> getColumnClass(int col) {
            return switch (col) {
                case 0 -> Integer.class;
                case 3 -> Boolean.class;
                default -> String.class;
            };
        }
    }

    // --- High Score Table Model ---
    class HighScoreTableModel extends AbstractTableModel {
        private final String[] columns = {"No", "Player", "Score"};
        private final List<Object[]> data = new ArrayList<>();

        public void loadHighScores() {
            data.clear();
            // Ambil top 5 high score dari database
            List<Map<String, Object>> hs = db.getTop5HighScores();
            int no = 1;
            for (Map<String, Object> row : hs) {
                data.add(new Object[]{
                        no++,
                        row.get("username"),
                        row.get("score")
                });
            }
            fireTableDataChanged();
        }

        public int getRowCount() {
            return data.size();
        }

        public int getColumnCount() {
            return columns.length;
        }

        public Object getValueAt(int row, int col) {
            return data.get(row)[col];
        }

        public String getColumnName(int col) {
            return columns[col];
        }

        public Class<?> getColumnClass(int col) {
            return switch (col) {
                case 0, 2 -> Integer.class;
                default -> String.class;
            };
        }
    }
}