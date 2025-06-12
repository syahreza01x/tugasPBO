import model.GameModel;
import model.DatabaseManager;
import view.GameView;
import controller.GameController;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        String[] difficulties = {"Easy", "Normal", "Hard"};
        int difficultyChoice = JOptionPane.showOptionDialog(null, "Pilih Difficulty:", "Difficulty",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, difficulties, difficulties[0]);

        int delay = switch (difficultyChoice) {
            case 0 -> 1000 / 5;
            case 2 -> 1000 / 15;
            default -> 1000 / 10;
        };

        String[] options = {"Single Player", "Multiplayer", "Settings"};
        int choice = JOptionPane.showOptionDialog(null, "Pilih Mode:", "Mode Game",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        int timeStopKey = GameModel.DEFAULT_TIME_STOP_KEY;
        int areaClearKey = GameModel.DEFAULT_AREA_CLEAR_KEY;

        if (choice == 2) {
            String input1 = JOptionPane.showInputDialog("Tombol skill Player 1 (E.g., E, Q, R):");
            String input2 = JOptionPane.showInputDialog("Tombol skill Player 2 (E.g., END, L, O):");
            if (input1 != null && input1.length() == 1) {
                timeStopKey = java.awt.event.KeyEvent.getExtendedKeyCodeForChar(input1.toUpperCase().charAt(0));
            }
            if (input2 != null && input2.length() == 1) {
                areaClearKey = java.awt.event.KeyEvent.getExtendedKeyCodeForChar(input2.toUpperCase().charAt(0));
            }
            main(null);
            return;
        }

        boolean isSinglePlayer = (choice == 0);

        // === Tambahan: Login dan koneksi database ===
        DatabaseManager db = new DatabaseManager();
        db.connect();

        int player1Id = -1, player2Id = -1;

        if (isSinglePlayer) {
            while (player1Id == -1) {
                String user = JOptionPane.showInputDialog("Username Player 1:");
                String pass = JOptionPane.showInputDialog("Password Player 1:");
                player1Id = db.login(user, pass);
                if (player1Id == -1) JOptionPane.showMessageDialog(null, "Login gagal!");
            }
        } else {
            while (player1Id == -1) {
                String user = JOptionPane.showInputDialog("Username Player 1:");
                String pass = JOptionPane.showInputDialog("Password Player 1:");
                player1Id = db.login(user, pass);
                if (player1Id == -1) JOptionPane.showMessageDialog(null, "Login gagal!");
            }
            while (player2Id == -1) {
                String user = JOptionPane.showInputDialog("Username Player 2:");
                String pass = JOptionPane.showInputDialog("Password Player 2:");
                player2Id = db.login(user, pass);
                if (player2Id == -1) JOptionPane.showMessageDialog(null, "Login gagal!");
            }
        }

        // === Buat model, view, controller dengan parameter baru ===
        GameModel model = new GameModel(isSinglePlayer, timeStopKey, areaClearKey, player1Id, player2Id, db);
        GameView view = new GameView(model);
        new GameController(model, view, delay);

        JFrame frame = new JFrame("ReZy Retro Game MVC");
        frame.add(view);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        model.playBGM("sounds/bgm.wav");
    }
}   