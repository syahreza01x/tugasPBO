import model.GameModel;
import model.DatabaseManager;
import view.GameView;
import view.HomePage;
import controller.GameController;
import controller.HomePageController;
import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.Properties;
import java.io.FileInputStream;
import view.SpecialModePanel;

public class Main {
    public static int getKeyCode(String key) {
        // Untuk key spesial
        return switch (key.toUpperCase()) {
            case "UP" -> KeyEvent.VK_UP;
            case "DOWN" -> KeyEvent.VK_DOWN;
            case "LEFT" -> KeyEvent.VK_LEFT;
            case "RIGHT" -> KeyEvent.VK_RIGHT;
            case "END" -> KeyEvent.VK_END;
            default -> key.length() == 1 ? KeyEvent.getExtendedKeyCodeForChar(key.charAt(0)) : 0;
        };
    }

    public static void main(String[] args) {
        DatabaseManager db = new DatabaseManager();
        db.connect();

        JFrame frame = new JFrame("Kelompok 5");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        final HomePage[] home = new HomePage[1];
        home[0] = new HomePage(db, frame, (playerIds, usernames, specialMode) -> {
            try {
                if (specialMode) {
                    // Masuk ke special mode
                    SpecialModePanel specialPanel = new SpecialModePanel(playerIds.get(0), usernames.get(0), db, frame, home[0]);
                    frame.setContentPane(specialPanel);
                    frame.revalidate();
                    return;
                } else {
                    String[] difficulties = {"Easy", "Normal", "Hard"};
                    int difficultyChoice = JOptionPane.showOptionDialog(frame, "Pilih Difficulty:", "Difficulty",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, difficulties, difficulties[0]);
                    int delay = switch (difficultyChoice) {
                        case 0 -> 1000 / 5;
                        case 2 -> 1000 / 15;
                        default -> 1000 / 10;
                    };

                    // Ambil setting kontrol dari file lokal
                    Properties props = new Properties();
                    try (FileInputStream in = new FileInputStream("controls.properties")) {
                        props.load(in);
                    } catch (Exception ignored) {}

                    // Player 1
                    int p1Up = getKeyCode(props.getProperty("p1.up", "W"));
                    int p1Down = getKeyCode(props.getProperty("p1.down", "S"));
                    int p1Left = getKeyCode(props.getProperty("p1.left", "A"));
                    int p1Right = getKeyCode(props.getProperty("p1.right", "D"));
                    int p1Skill = getKeyCode(props.getProperty("p1.skill", "E"));
                    // Player 2
                    int p2Up = getKeyCode(props.getProperty("p2.up", "UP"));
                    int p2Down = getKeyCode(props.getProperty("p2.down", "DOWN"));
                    int p2Left = getKeyCode(props.getProperty("p2.left", "LEFT"));
                    int p2Right = getKeyCode(props.getProperty("p2.right", "RIGHT"));
                    int p2Skill = getKeyCode(props.getProperty("p2.skill", "END"));

                    boolean isSinglePlayer = playerIds.size() == 1;
                    int player1Id = playerIds.get(0);
                    int player2Id = isSinglePlayer ? -1 : (playerIds.size() > 1 ? playerIds.get(1) : -1);

                    GameModel model = new GameModel(
                        isSinglePlayer, p1Up, p1Down, p1Left, p1Right, p1Skill,
                        p2Up, p2Down, p2Left, p2Right, p2Skill,
                        player1Id, player2Id, db
                    );
                    model.player1.username = usernames.get(0);
                    if (!isSinglePlayer && usernames.size() > 1) model.player2.username = usernames.get(1);

                    GameView view = new GameView(model);
                    new GameController(model, view, delay, frame, home[0]);

                    frame.setContentPane(view);
                    frame.revalidate();
                    model.playBGM("sounds/bgm.wav");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Terjadi error: " + ex.getMessage());
            }
        });

        new HomePageController(home[0], db); // Penting agar tombol HomePage aktif

        frame.setContentPane(home[0]);
        frame.setVisible(true);
    }
}