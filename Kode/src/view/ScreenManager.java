package view;

import java.awt.CardLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class ScreenManager {
    public static final String MAIN_MENU = "MAIN_MENU";
    public static final String SETTINGS = "SETTINGS";

    private final JFrame frame;
    private final CardLayout cardLayout;
    private final JPanel rootPanel;
    private final FullscreenManager fullscreenManager;
    private final MainMenuView mainMenuView;
    private final SettingsView settingsView;

    public ScreenManager(JFrame frame) {
        this.frame = frame;
        this.cardLayout = new CardLayout();
        this.rootPanel = new JPanel(cardLayout);
        this.rootPanel.setOpaque(false);
        this.fullscreenManager = new FullscreenManager(frame);
        this.mainMenuView = new MainMenuView(this);
        this.settingsView = new SettingsView(this);
        rootPanel.add(mainMenuView, MAIN_MENU);
        rootPanel.add(settingsView, SETTINGS);
    }

    public JPanel getRootPanel() {
        return rootPanel;
    }

    public JFrame getFrame() {
        return frame;
    }

    public FullscreenManager getFullscreenManager() {
        return fullscreenManager;
    }

    public void showMainMenu() {
        cardLayout.show(rootPanel, MAIN_MENU);
        mainMenuView.requestFocusInWindow();
    }

    public void showSettings() {
        settingsView.syncFullscreenState();
        cardLayout.show(rootPanel, SETTINGS);
        settingsView.requestFocusInWindow();
    }
}
