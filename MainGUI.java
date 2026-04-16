import gui.LoginFrame;
import javax.swing.*;

/**
 * Entry point for the ATTENDTRACK GUI application.
 * Run with: java -cp ".;lib/*" MainGUI
 */
public class MainGUI {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
