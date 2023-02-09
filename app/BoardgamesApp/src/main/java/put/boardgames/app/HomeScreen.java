package put.boardgames.app;

import javax.swing.*;
import java.awt.*;

public class HomeScreen {
    private JPanel homePanel;
    public JButton dodajGreButton;
    public JButton dodajGraczaButton;
    public JButton dodajRozszerzenieButton;
    public JButton dodajSpotkanieButton;
    public JButton dodajRozgrywkeButton;
    public JButton profilGraczaButton;
    public JButton informacjeOSpotkaniuButton;
    public JButton informacjeOGrachButton;
    public JButton wyjdzButton;
    public JLabel titleLabel;
    public JButton zarzadzajDanymiButton;

    private final JButton[] allButtons = {dodajGreButton, dodajGraczaButton, dodajRozszerzenieButton, dodajSpotkanieButton, dodajRozgrywkeButton, profilGraczaButton,
            informacjeOSpotkaniuButton, informacjeOGrachButton, wyjdzButton, zarzadzajDanymiButton};

    HomeScreen() {

        for (JButton allButton : allButtons) {
            allButton.setFont(new Font("Arial", Font.PLAIN, 20));
        }
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
    }

    public JPanel getPanel() {
        return homePanel;
    }

}
