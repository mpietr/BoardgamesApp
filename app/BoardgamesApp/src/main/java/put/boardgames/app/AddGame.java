package put.boardgames.app;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static put.boardgames.app.ErrorsSQL.ErrorSQL;
import static put.boardgames.app.Main.conn;


public class AddGame {
    private JTextField tytulTextField;
    public JButton anulujButton;
    private JButton dodajButton;
    private JPanel addGamePanel;
    private JTable gameTable;
    private JLabel titleLabel;
    private JSpinner minGraczySpinner;
    private JSpinner maxGraczySpinner;
    private JCheckBox maxGraczyCheckBox;
    private JSpinner ocenaGrySpinner;
    private JCheckBox ocenaGryCheckBox;

    private DefaultTableModel model;

    void setupListeners() {
        dodajButton.addActionListener(e -> {
            String tytul = tytulTextField.getText();
            int minGraczy = (Integer) minGraczySpinner.getValue();
            int maxGraczy = 0;
            if(maxGraczyCheckBox.isSelected()) {
                maxGraczy = (Integer) maxGraczySpinner.getValue();
            }
            int ocena = 0;
            if (ocenaGryCheckBox.isSelected()) {
                ocena = (Integer) ocenaGrySpinner.getValue();
            }


            if (!Validators.isGameTitleValid(tytul)) {
                JOptionPane.showMessageDialog(null, "Wprowadzony tytuł gry jest niepoprawny", "Informacja", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!Validators.isMinPlayersMaxPlayersValid(minGraczy, maxGraczy)) {
                JOptionPane.showMessageDialog(null, "Liczba graczy niepoprawna");
                return;
            }

            String sql;
            if (maxGraczy == 0 && ocena == 0) {
                sql =
                        "INSERT INTO Gry (tytul, min_graczy)" +
                                "VALUES ('" + tytul + "', " + minGraczy + ")";
            } else if (ocena == 0){
                sql =
                        "INSERT INTO Gry (tytul, min_graczy, max_graczy)" +
                                "VALUES ('" + tytul + "', " + minGraczy + ", " + maxGraczy + ")";

            } else if (maxGraczy == 0) {
                sql =
                        "INSERT INTO Gry (tytul, min_graczy, ocena)" +
                                "VALUES ('" + tytul + "', " + minGraczy + ", " + ocena + ")";
            }else {
                sql =
                        "INSERT INTO Gry (tytul, min_graczy, max_graczy, ocena)" +
                                "VALUES ('" + tytul + "', " + minGraczy + ", " + maxGraczy + ", " + ocena + ")";
            }


            Statement stmt = null;
            try {
                stmt = conn.createStatement();
                stmt.executeUpdate(sql);
                stmt.close();
                addRowToTable(tytul, minGraczy, maxGraczy, ocena);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, ErrorSQL(ex.getErrorCode()) + " " + "Błąd dodania gry", "Informacja", JOptionPane.ERROR_MESSAGE);
                System.out.println("Błąd wykonania polecenia: " + ex.getMessage());
                return;
            }
            JOptionPane.showMessageDialog(null, "Dodano grę", "Sukces", JOptionPane.INFORMATION_MESSAGE);
            resetInputFields();
        });

        ChangeListener listener = e -> {
            if (maxGraczyCheckBox.isSelected()) {
                int minVal = (Integer) minGraczySpinner.getValue();
                int maxVal = (Integer) maxGraczySpinner.getValue();
                maxGraczySpinner.setModel(new SpinnerNumberModel(minVal, minVal, 64, 1));
                maxGraczySpinner.setValue(Math.max(maxVal, minVal));
            }
        };

        minGraczySpinner.addChangeListener(listener);

        maxGraczyCheckBox.addActionListener(e -> {
            JCheckBox cb = (JCheckBox) e.getSource();
            if (cb.isSelected()) {
                maxGraczySpinner.setEnabled(true);
                int minVal = (Integer) minGraczySpinner.getValue();
                maxGraczySpinner.setModel(new SpinnerNumberModel(minVal, minVal, 64, 1));
            } else {
                maxGraczySpinner.setEnabled(false);
            }
        });

        ocenaGryCheckBox.addActionListener(e-> {
            JCheckBox cb = (JCheckBox) e.getSource();
            ocenaGrySpinner.setEnabled(cb.isSelected());
        });
    }

    AddGame() {
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        setupSpinners();
        setupListeners();
    }

    private void setupSpinners() {
        minGraczySpinner.setModel(new SpinnerNumberModel(1, 1, 64, 1));
        ocenaGrySpinner.setModel(new SpinnerNumberModel(1, 1, 10, 1));
    }

    public void refreshView() {
        resetInputFields();
        setupTable();
    }

    private void resetInputFields(){
        tytulTextField.setText("");
        minGraczySpinner.setValue(1);
        maxGraczySpinner.setValue(1);
        maxGraczyCheckBox.setSelected(false);
        maxGraczySpinner.setEnabled(false);
        ocenaGrySpinner.setEnabled(true);
        ocenaGryCheckBox.setSelected(true);
        ocenaGrySpinner.setValue(1);
    }


    private void setupTable() {
        model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        gameTable.setModel(model);
        model.addColumn("Tytuł");
        model.addColumn("Minimalna liczba graczy");
        model.addColumn("Maksymalna liczba graczy");
        model.addColumn("Ocena");
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT tytul, min_graczy, max_graczy, ocena " +
                             "FROM gry")) {
            while (rs.next()) {
                String tytul = rs.getString(1);
                Integer minGraczy = rs.getInt(2);
                Integer maxGraczy = rs.getInt(3);
                Integer ocena = rs.getInt(4);
                addRowToTable(tytul, minGraczy, maxGraczy, ocena);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ErrorSQL(ex.getErrorCode()) + " " + "Nie udało się pobrać zawartości bazy danych", "Informacja", JOptionPane.ERROR_MESSAGE);
            System.out.println("Błąd wykonania polecenia: " + ex.getMessage());
        }
    }

    private void addRowToTable(String title, Integer min, Integer max, Integer ocena) {
        if(max == 0) {
            max = null;
        }
        if(ocena == 0) {
            ocena = null;
        }
        Object[] row = {title, min, max, ocena};
        model.addRow(row);
    }

    public JPanel getPanel() {
        return addGamePanel;
    }
}
