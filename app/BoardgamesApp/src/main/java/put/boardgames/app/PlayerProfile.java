package put.boardgames.app;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

import static put.boardgames.app.ErrorsSQL.ErrorSQL;
import static put.boardgames.app.Main.conn;

public class PlayerProfile {
    private JComboBox playersComboBox;
    private JLabel nameLabel;
    private JLabel dateLabel;
    private JLabel meetingLabel;
    private JLabel matchLabel;
    private JTable statsTable;
    public JButton wrocButton;
    private JPanel playerProfilePanel;
    private JPanel playerInfoPanel;
    private JLabel statsLabel;
    private JLabel profileLabel;

    private Item currentPlayer;

    private ItemListener listener = new ItemListener() {
        @Override
        public void itemStateChanged(ItemEvent e) {
            currentPlayer = Objects.requireNonNull( (Item) playersComboBox.getSelectedItem());
            changeLabels();
            setupTable();
            System.out.println(currentPlayer.toString());
        }
    };

    private DefaultTableModel model;

    PlayerProfile () {
        setupListeners();
        setupComboBox();
        setupLabels();
        changeLabels();
        setupTable();
    }

    private void setupLabels() {
        profileLabel.setFont(new Font("Arial", Font.BOLD, 42));
        nameLabel.setFont(new Font("Arial", Font.BOLD, 32));
        dateLabel.setFont(new Font("Arial", Font.BOLD, 24));
        meetingLabel.setFont(new Font("Arial", Font.BOLD, 24));
        matchLabel.setFont(new Font("Arial", Font.BOLD, 24));
        statsLabel.setFont(new Font("Arial", Font.BOLD, 24));
    }

    public void refreshView() {
        playersComboBox.removeItemListener(listener);
        playersComboBox.removeAllItems();
        setupComboBox();
        playersComboBox.addItemListener(listener);
        playersComboBox.setSelectedIndex(0);
        currentPlayer = Objects.requireNonNull( (Item) playersComboBox.getSelectedItem());
        changeLabels();
    }

    private void changeLabels() {
        nameLabel.setText(currentPlayer.getDescription());
        //SELECT id_gracza, (SELECT COUNT(*) FROM gr_w_rozgr_spot WHERE id_gracza = g.id_gracza), (SELECT COUNT(DISTINCT id_spotkania)FROM gr_w_rozgr_spot WHERE id_gracza = g.id_gracza) FROM gracze g WHERE id_gracza = 100;

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT g.id_gracza, TO_CHAR(g.data_urodzenia, 'DD.MM.YYYY'), " +
                             "(SELECT COUNT(*) FROM gr_w_rozgr_spot WHERE id_gracza = g.id_gracza), " +
                             "(SELECT COUNT(DISTINCT id_spotkania)FROM gr_w_rozgr_spot WHERE id_gracza = g.id_gracza) " +
                             "FROM gracze g WHERE id_gracza = " + currentPlayer.getValue())) {
            rs.next();
            dateLabel.setText("Data urodzenia: " + rs.getString(2));
            meetingLabel.setText("Liczba spotkań: " + rs.getString(4));
            matchLabel.setText("Liczba rozegranych rozgrywek: " + rs.getString(3));

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ErrorSQL(ex.getErrorCode()) + " " + "Błąd pobrania informacji o graczu", "Informacja", JOptionPane.ERROR_MESSAGE);
            System.out.println("Błąd wykonania polecenia: " + ex.getMessage());
        }

    }

    private void setupTable(){
        model = new DefaultTableModel();
        statsTable.setModel(model);
        model.addColumn("Tytuł gry");
        model.addColumn("Procent wygranych");

        String query = "SELECT DISTINCT g.tytul, ProcentWygranych(grs.id_gracza, g.id_gry) " +
                "FROM gry g JOIN zestawy z ON g.id_gry = z.gry_id_gry " +
                "JOIN rozgrywki r ON z.id_zestawu = r.id_zestawu " +
                "JOIN gr_w_rozgr_spot grs ON grs.id_rozgrywki = r.id_rozgrywki " +
                "WHERE grs.id_gracza = " + currentPlayer.getValue();

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while(rs.next()) {
                model.addRow(new Object[]{rs.getString(1), rs.getInt(2) + "%"});
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ErrorSQL(ex.getErrorCode()) + " " + "Błąd pobrania statystyk gier gracza", "Informacja", JOptionPane.ERROR_MESSAGE);
            System.out.println("Błąd wykonania polecenia: " + ex.getMessage());
        }
    }
    private void setupComboBox() {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT id_gracza, imie, nazwisko FROM gracze ORDER BY nazwisko")) {
            while (rs.next()) {
                playersComboBox.addItem(new Item(rs.getInt(1), rs.getString(2) + " " + rs.getString(3)));
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ErrorSQL(ex.getErrorCode()) + " " + "Błąd pobrania listy graczy", "Informacja", JOptionPane.ERROR_MESSAGE);
            System.out.println("Błąd wykonania polecenia: " + ex.getMessage());
        }

    }

    private void setupListeners() {
        playersComboBox.addItemListener(listener);
    }

    public JPanel getPanel() {
        return playerProfilePanel;
    }

}
