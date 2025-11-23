import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import view.MainMenuView;

public class App {
    public static void main(String[] args) throws Exception {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            new MainMenuView();
        });
    }
}
