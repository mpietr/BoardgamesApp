package put.boardgames.app;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static put.boardgames.app.ErrorsSQL.ErrorSQL;
import static put.boardgames.app.Main.conn;

public class AddExpansion {
    private JPanel addExpansionPanel;
    private JList<String> gameList;
    private JTextField titleTextField;
    private JSpinner modGraczySpinner;
    public JButton anulujButton;
    public JButton dodajButton;
    private JLabel titleLabel;
    private JTable expansionTable;
    private JScrollPane tableScrollPane;
    private JCheckBox modGraczyCheckBox;
    private DefaultListModel listModel;
    private final DefaultTableModel tableModel = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    AddExpansion() {
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        modGraczySpinner.setModel(new SpinnerNumberModel(1, 1, 64, 1));
        refreshView();
        setupTable();
        setupListeners();
    }

    public void refreshView() {
        setupList();
        expansionTable.setVisible(false);
        resetInputFields();
    }

    private void resetInputFields() {
        titleTextField.setText("");
        modGraczySpinner.setValue(1);
        modGraczySpinner.setEnabled(false);
        modGraczyCheckBox.setSelected(false);
    }

    void setupListeners() {
        dodajButton.addActionListener(e -> {

            String gameTitle = gameList.getSelectedValue();
            String expansionTytul = titleTextField.getText();
            int modGraczy = (Integer) modGraczySpinner.getValue();

            if (!Validators.isGameTitleValid(expansionTytul)) {
                JOptionPane.showMessageDialog(null, "Wprowadzony tytuł rozszerzenia jest niepoprawny", "Informacja", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Integer gameId = selectGameId(gameTitle);

            String sql = "INSERT INTO rozszerzenia (tytul, mod_liczby_graczy, id_gry)" +
                    "VALUES('" + expansionTytul + "', '" + modGraczy + "', '" + gameId + "')";
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(sql);
                addRowToTable(expansionTytul);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, ErrorSQL(ex.getErrorCode()) + " " + "Błąd dodania rozszerzenia", "Informacja", JOptionPane.ERROR_MESSAGE);
                System.out.println(ex.getMessage());
                return;
            }
            JOptionPane.showMessageDialog(null, "Dodano rozszerzenie", "Sukces", JOptionPane.INFORMATION_MESSAGE);
            resetInputFields();
        });

        gameList.addListSelectionListener(e ->{
            if (gameList.getSelectedValue() != null) {
                updateTable(selectGameId(gameList.getSelectedValue()));
            }
        });

        modGraczyCheckBox.addActionListener(event -> {
            JCheckBox cb = (JCheckBox) event.getSource();
            modGraczySpinner.setEnabled(cb.isSelected());
        });
    }

    private Integer selectGameId(String gameTitle) {
        Integer gameId = null;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id_gry FROM gry " +
                     "WHERE UPPER(tytul) LIKE '" + gameTitle.toUpperCase() + "'")) {
            rs.next();
            gameId = rs.getInt(1);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ErrorSQL(ex.getErrorCode()) + " " + "Nie można pobrać ID gry", "Informacja", JOptionPane.ERROR_MESSAGE);
            System.out.println("Błąd wykonania polecenia1: " + ex.getMessage());
        }
        return gameId;
    }

    void setupList() {
        listModel = new DefaultListModel<String>();
        gameList.setModel(listModel);

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT tytul FROM gry")) {
            while (rs.next()) {
                listModel.addElement(rs.getString(1));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ErrorSQL(ex.getErrorCode()) + " " + "Błąd pobrania listy gier", "Informacja", JOptionPane.ERROR_MESSAGE);
            System.out.println("Błąd wykonania polecenia: " + ex.getMessage());
        }

        gameList.setSelectedIndex(0);

    }


    private void updateTable(Integer gameId) {
        expansionTable.setVisible(true);
        tableModel.setRowCount(0);
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT tytul FROM rozszerzenia " +
                             "WHERE id_gry = " + gameId)) {
            while (rs.next()) {
                System.out.println(rs.getString(1));
                addRowToTable(rs.getString(1));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ErrorSQL(ex.getErrorCode()) + " " + "Błąd aktualizacji tabeli", "Informacja", JOptionPane.ERROR_MESSAGE);
            System.out.println("Błąd wykonania polecenia: " + ex.getMessage());
        }
    }

    private void addRowToTable(String title) {
        Object[] row = {title};
        tableModel.addRow(row);
    }

    private void setupTable() {
        expansionTable.setModel(tableModel);
        tableModel.addColumn("Istniejące rozszerzenia wybranej gry");
        expansionTable.setVisible(false);
    }



    public JPanel getPanel() {
        return addExpansionPanel;
    }
}
