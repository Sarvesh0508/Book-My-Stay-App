import com.bookmystay.MainWindow;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class BookMyStayApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Set Look and Feel to System Platform Native
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Fallback to cross-platform metal design if platform is unsupported
            }
            
            MainWindow frame = new MainWindow();
            frame.setVisible(true);
        });
    }
}
