package put.boardgames.app;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static put.boardgames.app.Main.conn;

public class MeetingData {
    private JComboBox<Item<Integer>> meetingComboBox;
    private JTable playersTable;
    public JButton wrocButton;
    private JPanel meetingDataPanel;
    private JLabel foodLabel;
    private JComboBox<Item<Integer>> matchComboBox;
    private JLabel gameLabel;
    private JLabel expansionsLabel;
    private JLabel durationLabel;

    private DefaultTableModel model;

    MeetingData () {
        setupMeetingComboBox();
        setupMeetingData();
        setupListeners();
    }

    public void setupMeetingComboBox() {
        meetingComboBox.removeAllItems();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT s.id_spotkania, TO_CHAR(s.data, 'DD.MM.YYYY')|| ': ' || m.adres || ', ' || m.miasto " +
                             "FROM spotkania s JOIN miejsca m ON s.id_miejsca = m.id_miejsca " +
                             "ORDER BY s.id_spotkania DESC")) {
            while (rs.next()) {
                meetingComboBox.addItem(new Item<>(rs.getInt(1), rs.getString(2)));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getErrorCode() + ": " + ex.getMessage());
            System.out.println("Błąd wykonania polecenia: " + ex.getMessage());
        }
    }

    private void setupListeners() {
        meetingComboBox.addActionListener(e -> {
            removeData();
            setupMeetingData();
            System.out.print("wybrano nowe");
        });

        matchComboBox.addActionListener(e -> {
            setupMatchData();
        });
    }


    private void setupMeetingData() {
        Item<Integer> meeting = (Item<Integer>) meetingComboBox.getSelectedItem();
        if(meeting == null) {
            return;
        }
        Integer id = meeting.getValue();

        //SELECT LISTAGG(nazwa, ', ') FROM jedz_spot WHERE id_spotkania = 641;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT LISTAGG(nazwa, ', ') FROM jedz_spot WHERE id_spotkania = " + id)) {
            if (rs.next()){
                String menu = rs.getString(1);
                if(rs.wasNull()) {
                    foodLabel.setText("Menu: brak");
                } else {
                    foodLabel.setText("Menu: " + menu);
                }
            } else {
                foodLabel.setText("Menu: brak");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getErrorCode() + ": " + ex.getMessage());
            System.out.println("Błąd wykonania polecenia: " + ex.getMessage());
        }
        matchComboBox.removeAllItems();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT DISTINCT gr.id_rozgrywki, g.tytul FROM gr_w_rozgr_spot gr " +
                             "JOIN rozgrywki r ON gr.id_rozgrywki = r.id_rozgrywki " +
                             "JOIN zestawy z ON r.id_zestawu = z.id_zestawu " +
                             "JOIN gry g ON z.Gry_id_gry = g.id_gry " +
                             "WHERE gr.id_spotkania = " + id +" ORDER BY id_rozgrywki")) {
            int count = 1;
            while (rs.next()) {
                matchComboBox.addItem(new Item<>(rs.getInt(1), count + ". "+ rs.getString(2)));
                count++;
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getErrorCode() + ": " + ex.getMessage());
            System.out.println("Błąd wykonania polecenia: " + ex.getMessage());
        }
        setupMatchData();
    }

    private void setupMatchData() {
        Item<Integer> match = (Item<Integer>) matchComboBox.getSelectedItem();
        if (match == null) {
            return;
        }
        Integer matchId = match.getValue();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT g.tytul, " +
                             "(SELECT LISTAGG(r.tytul, ',') FROM rozsz_zest rz JOIN rozszerzenia r ON rz.id_roz = r.id_roz WHERE rz.id_zestawu = z.id_zestawu), " +
                             "r.czas FROM rozgrywki r " +
                             "JOIN zestawy z ON r.id_zestawu = z.id_zestawu " +
                             "JOIN gry g ON g.id_gry = z.Gry_id_gry " +
                             "WHERE id_rozgrywki = " + matchId)) {

            rs.next();
            gameLabel.setText("Gra: " + rs.getString(1));
            String rozszerzenia = rs.getString(2);
            if (rs.wasNull()) {
                rozszerzenia = "brak";
            }
            expansionsLabel.setText("Rozszerzenia: " + rozszerzenia);
            durationLabel.setText("Czas trwania: " + rs.getInt(3) + " min");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getErrorCode() + ": " + ex.getMessage());
            System.out.println("Błąd wykonania polecenia: " + ex.getMessage());
        }

        model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        playersTable.setModel(model);
        model.addColumn("Gracz");
        model.addColumn("Pozycja");


        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT g.imie || ' ' || g.nazwisko, gr.pozycja " +
                             "FROM gr_w_rozgr_spot gr JOIN gracze g ON gr.id_gracza = g.id_gracza " +
                             "WHERE id_rozgrywki = " + matchId + " ORDER BY g.nazwisko, g.imie")) {
            while (rs.next()) {
                String name = rs.getString(1);
                Integer p = rs.getInt(2);
                if(rs.wasNull()) {
                    model.addRow(new Object[]{name, null});
                } else {
                    model.addRow(new Object[]{name, p});
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getErrorCode() + ": " + ex.getMessage());
            System.out.println("Błąd wykonania polecenia: " + ex.getMessage());
        }

    }

    private void removeData() {
        gameLabel.setText("Gra: ");
        expansionsLabel.setText("Rozszerzenia: ");
        durationLabel.setText("Czas trwania: ");
        foodLabel.setText("Menu: ");
        model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        playersTable.setModel(model);
    }

    public JPanel getPanel() {
        return meetingDataPanel;
    }
}
