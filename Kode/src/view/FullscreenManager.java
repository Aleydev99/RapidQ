package view;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import javax.swing.JFrame;

public class FullscreenManager {
    private final JFrame frame;
    private final GraphicsDevice device;
    private Rectangle windowedBounds;
    private boolean fullscreen;

    public FullscreenManager(JFrame frame) {
        this.frame = frame;
        this.device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        this.windowedBounds = frame.getBounds();
        this.fullscreen = false;
    }

    public void toggleFullscreen() {
        if (fullscreen) {
            exitFullscreen();
        } else {
            enterFullscreen();
        }
    }

    public void enterFullscreen() {
        if (fullscreen) {
            return;
        }
        windowedBounds = frame.getBounds();
        frame.dispose();
        frame.setUndecorated(true);
        frame.setVisible(true);
        device.setFullScreenWindow(frame);
        fullscreen = true;
    }

    public void exitFullscreen() {
        if (!fullscreen) {
            return;
        }
        device.setFullScreenWindow(null);
        frame.dispose();
        frame.setUndecorated(false);
        frame.setBounds(windowedBounds);
        frame.setVisible(true);
        fullscreen = false;
    }

    public boolean isFullscreen() {
        return fullscreen;
    }

    public JFrame getFrame() {
        return frame;
    }

    public void runWithWindowedContext(Runnable task) {
        boolean wasFullscreen = fullscreen;
        if (wasFullscreen) {
            exitFullscreen();
        }
        task.run();
        if (wasFullscreen) {
            enterFullscreen();
        }
    }
}
