import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import util.Constants;
import view.ScreenManager;

public class App {
    public static void main(String[] args) throws Exception {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("RapidQ - Speed Quiz Game");
            frame.setSize(Constants.WINDOW_SIZE);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setResizable(false);

            ScreenManager screenManager = new ScreenManager(frame);
            frame.setContentPane(screenManager.getRootPanel());
            frame.setVisible(true);
            screenManager.showMainMenu();
        });
    }
}
