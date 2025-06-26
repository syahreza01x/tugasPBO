package view;

import model.DatabaseManager;
import model.DatabaseManager.Player;
import model.DatabaseManager.Skill;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.*;
import java.util.Properties;
import java.io.*;

public class HomePage extends JPanel {
    private final DatabaseManager db;
    private final JButton registerButton = new JButton("Register");
    private final JButton editButton = new JButton("Edit");
    private final JButton deleteButton = new JButton("Delete");
    private final JButton playButton = new JButton("Play");
    private final JButton controlButton = new JButton("Setting");
    private final JFrame parentFrame;

    private PlayerTableModel tableModel;
    private JTable table;

    private HighScoreTableModel highScoreModel;
    private JTable highScoreTable;

    // Tema warna
    private final Color bgDark = new Color(30, 34, 45);
    private final Color bgPanel = new Color(40, 44, 60);
    private final Color blueAccent = new Color(0, 120, 215);
    private final Color textColor = new Color(220, 220, 230);
    private final Color tableHeader = new Color(20, 24, 34);
    private final Color tableRow = new Color(38, 42, 56);

    public interface PlayListener {
        void onPlay(List<Integer> playerIds, List<String> usernames, boolean specialMode);
}

    public HomePage(DatabaseManager db, JFrame parentFrame, PlayListener playListener) {
        this.db = db;
        this.parentFrame = parentFrame;

        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(bgDark);

        JLabel title = new JLabel("Game Kelompok 5 PBO", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title.setForeground(blueAccent);
        title.setBorder(new EmptyBorder(10, 0, 20, 0));
        add(title, BorderLayout.NORTH);

        // --- KIRI: Data Player ---
        tableModel = new PlayerTableModel();
        table = new JTable(tableModel);

        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(3).setMaxWidth(60);
        table.setRowHeight(32);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.setBackground(tableRow);
        table.setForeground(textColor);
        table.setSelectionBackground(blueAccent.darker());
        table.setSelectionForeground(Color.WHITE);
        table.setGridColor(bgDark);

        JTableHeader th = table.getTableHeader();
        th.setBackground(tableHeader);
        th.setForeground(blueAccent);
        th.setReorderingAllowed(false);

        // Pilih (checkbox) renderer
        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                JCheckBox checkBox = new JCheckBox();
                checkBox.setSelected((Boolean) value);
                checkBox.setHorizontalAlignment(SwingConstants.CENTER);
                checkBox.setEnabled(true);
                checkBox.setBackground(isSelected ? blueAccent.darker() : tableRow);
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
        scrollPane.getViewport().setBackground(bgPanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(blueAccent, 1));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(bgPanel);

        styleButton(registerButton);
        styleButton(editButton);
        styleButton(deleteButton);
        styleButton(playButton);
        styleButton(controlButton);

        playButton.setEnabled(false);
        editButton.setEnabled(false);
        deleteButton.setEnabled(false);

        buttonPanel.add(registerButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(playButton);
        buttonPanel.add(controlButton);

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout(10, 10));
        leftPanel.setBackground(bgPanel);
        leftPanel.setBorder(BorderFactory.createLineBorder(blueAccent, 1));
        leftPanel.add(scrollPane, BorderLayout.CENTER);
        leftPanel.add(buttonPanel, BorderLayout.SOUTH);

        // --- KANAN: Top 5 High Score ---
        highScoreModel = new HighScoreTableModel();
        highScoreTable = new JTable(highScoreModel);
        highScoreTable.setRowHeight(28);
        highScoreTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        highScoreTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        highScoreTable.getColumnModel().getColumn(0).setMaxWidth(50);
        highScoreTable.setBackground(tableRow);
        highScoreTable.setForeground(textColor);
        highScoreTable.setSelectionBackground(blueAccent.darker());
        highScoreTable.setSelectionForeground(Color.WHITE);
        highScoreTable.setGridColor(bgDark);

        JTableHeader hsTh = highScoreTable.getTableHeader();
        hsTh.setBackground(tableHeader);
        hsTh.setForeground(blueAccent);
        hsTh.setReorderingAllowed(false);

        JScrollPane highScoreScroll = new JScrollPane(highScoreTable);
        highScoreScroll.setPreferredSize(new Dimension(300, 200));
        highScoreScroll.getViewport().setBackground(bgPanel);
        highScoreScroll.setBorder(BorderFactory.createLineBorder(blueAccent, 1));

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout(10, 10));
        rightPanel.setBackground(bgPanel);
        rightPanel.setBorder(BorderFactory.createLineBorder(blueAccent, 1));
        JLabel hsTitle = new JLabel("Top 5 High Score", SwingConstants.CENTER);
        hsTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        hsTitle.setForeground(blueAccent);
        hsTitle.setBorder(new EmptyBorder(10, 0, 10, 0));
        rightPanel.add(hsTitle, BorderLayout.NORTH);
        rightPanel.add(highScoreScroll, BorderLayout.CENTER);

        // --- Gabung Kiri & Kanan ---
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.X_AXIS));
        centerPanel.setBackground(bgDark);
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
                // Konfirmasi password sebelum edit
                if (!verifyPassword(username)) {
                    JOptionPane.showMessageDialog(this, "Password salah atau aksi dibatalkan!");
                    return;
                }
                showEditDialog(playerId, username, skillId);
                tableModel.loadPlayers();
                updateButtonState();
                highScoreModel.loadHighScores();
            }
        });

        deleteButton.addActionListener(e -> {
            List<Integer> selectedIdx = tableModel.getSelectedIndexes();
            if (!selectedIdx.isEmpty()) {
                // Konfirmasi password untuk username pertama yang dipilih
                String username = tableModel.getPlayerName(selectedIdx.get(0));
                if (!verifyPassword(username)) {
                    JOptionPane.showMessageDialog(this, "Password salah atau aksi dibatalkan!");
                    return;
                }
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
    // Jika solo, tampilkan pilihan mode
    if (ids.size() == 1) {
        String[] mode = {"Normal Mode", "Special Mode"};
        int modeChoice = JOptionPane.showOptionDialog(this, "Pilih Mode:", "Mode",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, mode, mode[0]);
        playListener.onPlay(ids, names, modeChoice == 1); // true jika special mode
    } else {
        playListener.onPlay(ids, names, false);
    }
});

        // --- Setting Kontrol ---
        controlButton.addActionListener(e -> showControlSettingDialog());

        tableModel.loadPlayers();
        highScoreModel.loadHighScores();
    }

    private void styleButton(JButton btn) {
        btn.setPreferredSize(new Dimension(100, 30));
        btn.setBackground(blueAccent);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void updateButtonState() {
        int selected = tableModel.getSelectedCount();
        playButton.setEnabled(selected >= 1 && selected <= 2);
        editButton.setEnabled(selected == 1);
        deleteButton.setEnabled(selected == 1);
    }

private void showControlSettingDialog() {
    Properties props = new Properties();
    try (FileInputStream in = new FileInputStream("controls.properties")) {
        props.load(in);
    } catch (Exception ignored) {}

    // Default values
    String p1Up = props.getProperty("p1.up", "W");
    String p1Down = props.getProperty("p1.down", "S");
    String p1Left = props.getProperty("p1.left", "A");
    String p1Right = props.getProperty("p1.right", "D");
    String p1Skill = props.getProperty("p1.skill", "E");

    String p2Up = props.getProperty("p2.up", "UP");
    String p2Down = props.getProperty("p2.down", "DOWN");
    String p2Left = props.getProperty("p2.left", "LEFT");
    String p2Right = props.getProperty("p2.right", "RIGHT");
    String p2Skill = props.getProperty("p2.skill", "END");

    // Field setup
    JTextField p1UpField = new JTextField(p1Up);
    JTextField p1DownField = new JTextField(p1Down);
    JTextField p1LeftField = new JTextField(p1Left);
    JTextField p1RightField = new JTextField(p1Right);
    JTextField p1SkillField = new JTextField(p1Skill);

    JTextField p2UpField = new JTextField(p2Up);
    JTextField p2DownField = new JTextField(p2Down);
    JTextField p2LeftField = new JTextField(p2Left);
    JTextField p2RightField = new JTextField(p2Right);
    JTextField p2SkillField = new JTextField(p2Skill);

    JTextField[] fields = {p1UpField, p1DownField, p1LeftField, p1RightField, p1SkillField,
                           p2UpField, p2DownField, p2LeftField, p2RightField, p2SkillField};
    for (JTextField f : fields) {
        f.setBackground(bgPanel);
        f.setForeground(textColor);
        f.setCaretColor(textColor);
        f.setBorder(BorderFactory.createLineBorder(blueAccent, 1));
    }

    // Panel Player 1
    JPanel panelP1 = new JPanel(new GridLayout(6, 2, 6, 4));
    panelP1.setBackground(bgPanel);
    panelP1.add(new JLabel("Player 1", SwingConstants.CENTER) {{
        setForeground(blueAccent);
        setFont(getFont().deriveFont(Font.BOLD));
        setHorizontalAlignment(SwingConstants.CENTER);
    }});
    panelP1.add(new JLabel()); // Kosong biar rapi
    panelP1.add(new JLabel("Up:", SwingConstants.RIGHT) {{ setForeground(textColor); }});
    panelP1.add(p1UpField);
    panelP1.add(new JLabel("Down:", SwingConstants.RIGHT) {{ setForeground(textColor); }});
    panelP1.add(p1DownField);
    panelP1.add(new JLabel("Left:", SwingConstants.RIGHT) {{ setForeground(textColor); }});
    panelP1.add(p1LeftField);
    panelP1.add(new JLabel("Right:", SwingConstants.RIGHT) {{ setForeground(textColor); }});
    panelP1.add(p1RightField);
    panelP1.add(new JLabel("Skill:", SwingConstants.RIGHT) {{ setForeground(textColor); }});
    panelP1.add(p1SkillField);

    // Panel Player 2
    JPanel panelP2 = new JPanel(new GridLayout(6, 2, 6, 4));
    panelP2.setBackground(bgPanel);
    panelP2.add(new JLabel("Player 2", SwingConstants.CENTER) {{
        setForeground(blueAccent);
        setFont(getFont().deriveFont(Font.BOLD));
        setHorizontalAlignment(SwingConstants.CENTER);
    }});
    panelP2.add(new JLabel());
    panelP2.add(new JLabel("Up:", SwingConstants.RIGHT) {{ setForeground(textColor); }});
    panelP2.add(p2UpField);
    panelP2.add(new JLabel("Down:", SwingConstants.RIGHT) {{ setForeground(textColor); }});
    panelP2.add(p2DownField);
    panelP2.add(new JLabel("Left:", SwingConstants.RIGHT) {{ setForeground(textColor); }});
    panelP2.add(p2LeftField);
    panelP2.add(new JLabel("Right:", SwingConstants.RIGHT) {{ setForeground(textColor); }});
    panelP2.add(p2RightField);
    panelP2.add(new JLabel("Skill:", SwingConstants.RIGHT) {{ setForeground(textColor); }});
    panelP2.add(p2SkillField);

    // Panel utama 2 kolom
    JPanel panelUtama = new JPanel(new GridLayout(1, 2, 20, 0));
    panelUtama.setBackground(bgPanel);
    panelUtama.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    panelUtama.add(panelP1);
    panelUtama.add(panelP2);

    UIManager.put("OptionPane.background", bgPanel);
    UIManager.put("Panel.background", bgPanel);
    UIManager.put("OptionPane.messageForeground", textColor);

    int result = JOptionPane.showConfirmDialog(this, panelUtama, "Setting Kontrol", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    if (result == JOptionPane.OK_OPTION) {
        props.setProperty("p1.up", p1UpField.getText().trim().toUpperCase());
        props.setProperty("p1.down", p1DownField.getText().trim().toUpperCase());
        props.setProperty("p1.left", p1LeftField.getText().trim().toUpperCase());
        props.setProperty("p1.right", p1RightField.getText().trim().toUpperCase());
        props.setProperty("p1.skill", p1SkillField.getText().trim().toUpperCase());

        props.setProperty("p2.up", p2UpField.getText().trim().toUpperCase());
        props.setProperty("p2.down", p2DownField.getText().trim().toUpperCase());
        props.setProperty("p2.left", p2LeftField.getText().trim().toUpperCase());
        props.setProperty("p2.right", p2RightField.getText().trim().toUpperCase());
        props.setProperty("p2.skill", p2SkillField.getText().trim().toUpperCase());

        try (FileOutputStream out = new FileOutputStream("controls.properties")) {
            props.store(out, "Player Controls");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan setting kontrol!");
        }
    }
}



    private void showRegisterDialog() {
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JComboBox<String> skillBox = new JComboBox<>();
        var skills = db.getAllSkills();
        for (var s : skills) skillBox.addItem(s.namaSkill);

        // Set warna dark untuk semua komponen
        usernameField.setBackground(bgPanel);
        usernameField.setForeground(textColor);
        usernameField.setCaretColor(textColor);
        usernameField.setBorder(BorderFactory.createLineBorder(blueAccent, 1));

        passwordField.setBackground(bgPanel);
        passwordField.setForeground(textColor);
        passwordField.setCaretColor(textColor);
        passwordField.setBorder(BorderFactory.createLineBorder(blueAccent, 1));

        skillBox.setBackground(bgPanel);
        skillBox.setForeground(textColor);

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.setBackground(bgPanel);
        JLabel l1 = new JLabel("Username:");
        JLabel l2 = new JLabel("Password:");
        JLabel l3 = new JLabel("Skill:");
        l1.setForeground(textColor);
        l2.setForeground(textColor);
        l3.setForeground(textColor);
        panel.add(l1);
        panel.add(usernameField);
        panel.add(l2);
        panel.add(passwordField);
        panel.add(l3);
        panel.add(skillBox);

        // Set JOptionPane dark
        UIManager.put("OptionPane.background", bgPanel);
        UIManager.put("Panel.background", bgPanel);
        UIManager.put("OptionPane.messageForeground", textColor);

        int result = JOptionPane.showConfirmDialog(this, panel, "Register Player", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
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

        // Set warna dark untuk semua komponen
        usernameField.setBackground(bgPanel);
        usernameField.setForeground(textColor);
        usernameField.setCaretColor(textColor);
        usernameField.setBorder(BorderFactory.createLineBorder(blueAccent, 1));

        skillBox.setBackground(bgPanel);
        skillBox.setForeground(textColor);

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.setBackground(bgPanel);
        JLabel l1 = new JLabel("Username:");
        JLabel l2 = new JLabel("Skill:");
        l1.setForeground(textColor);
        l2.setForeground(textColor);
        panel.add(l1);
        panel.add(usernameField);
        panel.add(l2);
        panel.add(skillBox);

        // Set JOptionPane dark
        UIManager.put("OptionPane.background", bgPanel);
        UIManager.put("Panel.background", bgPanel);
        UIManager.put("OptionPane.messageForeground", textColor);

        int result = JOptionPane.showConfirmDialog(this, panel, "Edit Player", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
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

    // Konfirmasi password sebelum edit/delete
    private boolean verifyPassword(String username) {
        JPasswordField passwordField = new JPasswordField();
        passwordField.setBackground(bgPanel);
        passwordField.setForeground(textColor);
        passwordField.setCaretColor(textColor);
        passwordField.setBorder(BorderFactory.createLineBorder(blueAccent, 1));
        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.setBackground(bgPanel);
        JLabel l1 = new JLabel("Masukkan password untuk " + username + ":");
        l1.setForeground(textColor);
        panel.add(l1);
        panel.add(passwordField);

        UIManager.put("OptionPane.background", bgPanel);
        UIManager.put("Panel.background", bgPanel);
        UIManager.put("OptionPane.messageForeground", textColor);

        int result = JOptionPane.showConfirmDialog(this, panel, "Konfirmasi Password", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String password = new String(passwordField.getPassword());
            return db.isPasswordValid(username, password);
        }
        return false;
    }

    // Method untuk refresh high score dari luar (misal setelah game selesai)
    public void refreshHighScore() {
        highScoreModel.loadHighScores();
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