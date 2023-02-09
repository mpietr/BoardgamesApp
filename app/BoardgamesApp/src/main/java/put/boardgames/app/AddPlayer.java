package put.boardgames.app;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static put.boardgames.app.ErrorsSQL.ErrorSQL;
import static put.boardgames.app.Main.conn;

public class AddPlayer {
    private JTextField nameTextField;
    private JTextField surnameTextField;
    private JComboBox<Integer> dayComboBox;
    private JComboBox<Integer> monthComboBox;
    private JComboBox<Integer> yearComboBox;
    private JPanel addPlayerPanel;
    private JTable playerTable;
    public JButton anulujButton;
    public JButton dodajButton;
    public JLabel titleLabel;

    private DefaultTableModel model = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public JPanel getPanel() {
        return addPlayerPanel;
    }

    private void setupTable() {
        playerTable.setModel(model);
        model.addColumn("Imię");
        model.addColumn("Nazwisko");
        model.addColumn("Data urodzenia");
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT imie, nazwisko, TO_CHAR(data_urodzenia,'DD.MM.YYYY') FROM gracze ORDER BY nazwisko")) {
            while (rs.next()) {
                addRowToTable(rs.getString(1), rs.getString(2), rs.getString(3));
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ErrorSQL(ex.getErrorCode()) + " " + "Błąd pobierania listy graczy", "Informacja", JOptionPane.ERROR_MESSAGE);
            System.out.println("Błąd wykonania polecenia: " + ex.getMessage());
        }
    }

    private void setupComboBoxes() {

        for (int i = 1; i <= 31; i++) {
            dayComboBox.addItem(i);
        }

        for (int i = 1; i <= 12; i++) {
            monthComboBox.addItem(i);
        }

        for (int i = 1900; i <= 2030; i++) {
            yearComboBox.addItem(i);
        }
    }

    void setupListeners() {

        dodajButton.addActionListener(e ->

        {
            String name = nameTextField.getText();
            String surname = surnameTextField.getText();

            int day = (Integer) dayComboBox.getSelectedItem();
            int month = (Integer) monthComboBox.getSelectedItem();
            int year = (Integer) yearComboBox.getSelectedItem();
            String birthDate = year + "-" + month + "-" + day;

            if (!Validators.isNameValid(name)) {
                JOptionPane.showMessageDialog(null, "Imię jest niepoprawne", "Informacja", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!Validators.isNameValid(surname)) {
                JOptionPane.showMessageDialog(null, "Nazwisko jest niepoprawne", "Informacja", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!Validators.isDateValid(day, month, year)) {
                JOptionPane.showMessageDialog(null, "Data urodzenia jest niepoprawna", "Informacja", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // INSERT INTO gracze(name, surname, data_urodzenia) VALUES('JAN', 'KOWALSKI', TO_DATE('20-12-1999', 'dd-mm-yyyy'));

            String sql =
                    "INSERT INTO gracze (imie, nazwisko, data_urodzenia)" +
                            "VALUES ('" + name + "', '" + surname + "', TO_DATE('" + birthDate + "', 'YYYY-MM-DD'))";

            Statement stmt = null;
            try {
                stmt = Main.conn.createStatement();
                stmt.executeUpdate(sql);
                stmt.close();
                addRowToTable(name, surname, String.format("%02d.%02d.%d", day, month, year));
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, ErrorSQL(ex.getErrorCode()) + " " + "Błąd dodania nowego gracza", "Informacja", JOptionPane.ERROR_MESSAGE);
                System.out.println("Błąd wykonania polecenia: " + ex.getMessage());
                return;
            }
            JOptionPane.showMessageDialog(null, "Dodano gracza", "Sukces", JOptionPane.INFORMATION_MESSAGE);
            resetInputFields();
            System.out.println("Dodano gracza " + name + " " + surname + " " + birthDate);
        });
    }

    private void addRowToTable(String name, String surname, String data) {
        Object[] row = {name, surname, data};
        model.addRow(row);
    }

    public void refreshView() {
        model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        setupTable();
        resetInputFields();
    }

    private void resetInputFields() {
        nameTextField.setText("");
        surnameTextField.setText("");
        dayComboBox.setSelectedIndex(0);
        monthComboBox.setSelectedIndex(0);
        yearComboBox.setSelectedIndex(0);
    }

    AddPlayer() {
        setupComboBoxes();
        setupTable();
        setupListeners();
    }
}
