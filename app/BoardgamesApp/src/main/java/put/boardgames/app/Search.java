package put.boardgames.app;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static put.boardgames.app.Main.conn;

public class Search {
    private JTable table;
    private DefaultTableModel model = new DefaultTableModel();
    private JTextField textField;
    private JButton szukajButton;
    public JButton wrocButton;
    private JPanel searchPanel;

    Search() {
        setupTable();
        setupListeners();
    }

    private void setupListeners() {
        szukajButton.addActionListener(e -> szukajGracza());
    }

    private void szukajGracza() {
        String zapytanie = textField.getText().toUpperCase();
        if (zapytanie.equals("")) {
            JOptionPane.showMessageDialog(null,  "Wpisz wartość wo wyszukiwarki", "Informacja", JOptionPane.ERROR_MESSAGE);
        }
        else {
            model.setRowCount(0);
            String sql = "SELECT imie, nazwisko, TO_CHAR(data_urodzenia,'DD.MM.YYYY') FROM Gracze WHERE UPPER(imie) LIKE '%" + zapytanie + "%' OR UPPER(nazwisko) LIKE '%" + zapytanie + "%'";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    model.addRow(new Object[]{rs.getString(1), rs.getString(2), rs.getString(3)});
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, ex.getErrorCode() + ": " + ex.getMessage());
                System.out.println("Błąd wykonania polecenia: " + ex.getMessage());
            }
        }
    }

    private void setupTable(){
        table.setModel(model);
        model.addColumn("Imię");
        model.addColumn("Nazwisko");
        model.addColumn("Data urodzenia");
    }

    public JPanel getPanel() {
        return searchPanel;
    }

}
