import model.GameModel;
import model.DatabaseManager;
import view.GameView;
import view.HomePage;
import controller.GameController;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        DatabaseManager db = new DatabaseManager();
        db.connect();

        JFrame frame = new JFrame("ReZy Retro Game MVC");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        final HomePage[] home = new HomePage[1];
        home[0] = new HomePage(db, frame, (playerIds, usernames) -> {
            String[] difficulties = {"Easy", "Normal", "Hard"};
            int difficultyChoice = JOptionPane.showOptionDialog(frame, "Pilih Difficulty:", "Difficulty",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, difficulties, difficulties[0]);
            int delay = switch (difficultyChoice) {
                case 0 -> 1000 / 5;
                case 2 -> 1000 / 15;
                default -> 1000 / 10;
            };

            int timeStopKey = GameModel.DEFAULT_TIME_STOP_KEY;
            int areaClearKey = GameModel.DEFAULT_AREA_CLEAR_KEY;

            boolean isSinglePlayer = playerIds.size() == 1;
            int player1Id = playerIds.get(0);
            int player2Id = isSinglePlayer ? -1 : playerIds.get(1);

            GameModel model = new GameModel(isSinglePlayer, timeStopKey, areaClearKey, player1Id, player2Id, db);
            model.username1 = usernames.get(0);
            if (!isSinglePlayer) model.username2 = usernames.get(1);

            GameView view = new GameView(model);
            new GameController(model, view, delay, frame, home[0]);

            frame.setContentPane(view);
            frame.revalidate();
            model.playBGM("sounds/bgm.wav");
        });

        frame.setContentPane(home[0]);
        frame.setVisible(true);
    }
}