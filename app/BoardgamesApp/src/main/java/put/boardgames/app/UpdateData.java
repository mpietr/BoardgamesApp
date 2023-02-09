package put.boardgames.app;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Vector;

import static put.boardgames.app.Deleters.*;
import static put.boardgames.app.ErrorsSQL.ErrorSQL;
import static put.boardgames.app.Main.conn;
import static put.boardgames.app.Validators.isAdressNameValid;
import static put.boardgames.app.Validators.isFoodNameValid;


public class UpdateData {
    public JButton anulujButton;
    private JComboBox<String> typeComboBox;
    private JTable table;
    private JScrollPane tableScrollPane;
    private JPanel updateDataPanel;
    private JButton edytujButton;
    private JButton usunButton;
    private JLabel titleLabel;
    private JPanel editPanel;
    private JTextField gameTitleTextField;
    private JPanel editGamePanel;
    private JSpinner minSpinner;
    private JSpinner maxSpinner;
    private JCheckBox maxCheckBox;
    private JTextField expansionTitleTextField;
    private JCheckBox expansionMaxCheckBox;
    private JPanel editExpansionPanel;
    private JComboBox<Item<Integer>> expansionGameComboBox;
    private JButton zapiszButton;
    private JPanel savePanel;
    private JSpinner expansionMaxSpinner;
    private JPanel editPlayerPanel;

    private JPanel editMeetingPanel;


    private final JPanel[] editPanels = {savePanel, editGamePanel, editExpansionPanel, editPlayerPanel, editMeetingPanel};
    private JTextField playerNameTextField;
    private JTextField playerSurnameTextField;
    private JComboBox<Integer> dzienComboBox;
    private JComboBox<Integer> miesiacComboBox;
    private JComboBox<Integer> rokComboBox;
    private JLabel dataLabel;
    private JLabel miejsceLabel;
    private JComboBox<Item<Integer>> miejsceSpotkaniaComboBox;
    private JTextField adresTextField;
    private JLabel adresLabel;
    private JLabel miastoLabel;
    private JTextField miastoTextField;
    private JTextField jedzenieTextField;
    private JButton dodajJedzenieButton;
    private JTable jedzenieTable;
    private JButton usunJedzenieButton;
    private JComboBox<Integer> dzienSpotkaniaComboBox;
    private JComboBox<Integer> miesiacSpotkaniaComboBox;
    private JComboBox<Integer> rokSpotkaniaComboBox;
    private JButton edytujRozgrywkiSpotkaniaButton;
    private JSpinner ocenaSpinner;
    private JCheckBox ocenaCheckBox;

    private boolean isFoodEdited = false;

    private int meetingId;
    private boolean editMatchOfMeeting = false;

    private final String newMeetingPlace = "-- zmień na nowe miejsce --";

    private final ItemListener miejsceSpotkaniaCBListener = new ItemListener() {
        @Override
        public void itemStateChanged(ItemEvent e) {
            setNewMeetingTextFieldsEnabled(Objects.requireNonNull(miejsceSpotkaniaComboBox.getSelectedItem()).toString().equals(newMeetingPlace));
        }
    };

    private DefaultTableModel jedzenieTableModel;

    private ArrayList<JComboBox<Integer>> meetingDateComboBoxes = new ArrayList<>(Arrays.asList(dzienSpotkaniaComboBox, miesiacSpotkaniaComboBox, rokSpotkaniaComboBox));
    private ArrayList<JComboBox<Integer>> playerDateComboBoxes = new ArrayList<>(Arrays.asList(dzienComboBox, miesiacComboBox, rokComboBox));
    public static DefaultTableModel model;


    UpdateData() {
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        typeComboBox.addItem("Gry");
        typeComboBox.addItem("Rozszerzenia");
        typeComboBox.addItem("Gracze");
        typeComboBox.addItem("Spotkania");
        setupListeners();
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    }

    public void refreshView() {
        setupEditPanels();
        typeComboBox.setSelectedItem("Gry");
        setupGameTable();
    }

    private void setupEditPanels() {
        for (JPanel panel : editPanels) {
            panel.setVisible(false);
        }
        edytujButton.setEnabled(true);
    }


    private void setupListeners() {

        zapiszButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(null, "Nie wybrano żadnego wiersza", "Informacja", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String id = table.getValueAt(row, 0).toString();
            String type = Objects.requireNonNull(typeComboBox.getSelectedItem()).toString();

            switch (type) {
                case "Gry" -> {
                    if (updateGame(id, row) > 0) {
                        setupEditPanels();
                    }
                }
                case "Rozszerzenia" -> {
                    if (updateExpansion(id, row) > 0) {
                        setupEditPanels();
                    }
                }
                case "Gracze" -> {
                    if (updatePlayer(id, row) > 0) {
                        setupEditPanels();
                    }
                }
                case "Spotkania" -> {
                    if (updateMeeting(id, row) > 0) {
                        setupEditPanels();
                    }
                }
            }
        });

        usunButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(null, "Nie wybrano żadnego wiersza", "Informacja", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String id = table.getValueAt(row, 0).toString();
            String type = Objects.requireNonNull(typeComboBox.getSelectedItem()).toString();

            switch (type) {
                case "Gry" -> deleteGame(id, row);
                case "Rozszerzenia" -> deleteExpansion(id, row, null);
                case "Gracze" -> deletePlayer(id, row);
                case "Spotkania" -> {
                    if(editMatchOfMeeting) {
                        deleteMatch(id, row);
                    } else {
                        deleteMeeting(id, row);
                    }
                }
            }
        });

        typeComboBox.addActionListener(e -> {
            String type = Objects.requireNonNull(typeComboBox.getSelectedItem()).toString();
            setupEditPanels();
            switch (type) {
                case "Gry" -> setupGameTable();
                case "Rozszerzenia" -> setupExpansionTable();
                case "Gracze" -> setupPlayerTable();
                case "Spotkania" -> setupEditMeetingTable();
            }
        });

        edytujButton.addActionListener(e -> {
            String type = Objects.requireNonNull(typeComboBox.getSelectedItem()).toString();

            int row = table.getSelectedRow();
            if (row > -1) {
                setupEditPanels();
                savePanel.setVisible(true);
                switch (type) {
                    case "Gry" -> setupGameEditPanel(row);
                    case "Rozszerzenia" -> setupEditExpansionPanel(row);
                    case "Gracze" -> setupEditPlayerPanel(row);
                    case "Spotkania" ->  { setupEditMeetingPanel(row);
                    }
                }
            }

        });

        ocenaCheckBox.addActionListener(event -> {
            JCheckBox cb = (JCheckBox) event.getSource();
            ocenaSpinner.setEnabled(cb.isSelected());
        });

        expansionMaxCheckBox.addActionListener(e -> {
            JCheckBox cb = (JCheckBox) e.getSource();
            if (cb.isSelected()) {
                expansionMaxSpinner.setEnabled(true);
                System.out.println("Set enabled true");
            } else {
                expansionMaxSpinner.setEnabled(false);
                System.out.println("Set enabled false");
            }
        });

        dodajJedzenieButton.addActionListener(e -> {
            String name = jedzenieTextField.getText();
            if (isFoodNameValid(name)) {
                jedzenieTextField.setText("");
                jedzenieTableModel.addRow(new Object[]{name});
                isFoodEdited = true;
            } else {
                JOptionPane.showMessageDialog(null, "Nazwa jedzenia nie może być pusta", "Informacja", JOptionPane.ERROR_MESSAGE);
            }
        });

        usunJedzenieButton.addActionListener(e -> {
            int selectedRow = jedzenieTable.getSelectedRow();
            if (selectedRow > -1) {
                jedzenieTableModel.removeRow(selectedRow);
                isFoodEdited = true;
            } else {
                JOptionPane.showMessageDialog(null, "Zaznacz wiersz, który chcesz usunąć", "Informacja", JOptionPane.ERROR_MESSAGE);
            }
        });

        edytujRozgrywkiSpotkaniaButton.addActionListener(e -> {
            setupEditMatchTableAndPanel();
        });

        miejsceSpotkaniaComboBox.addItemListener(miejsceSpotkaniaCBListener);
    }

    private Integer updateMeeting(String id, int row) {
        String sql;
        int r = -1;

        Integer miejsceId = 0;
        String miasto;
        String adres;

        if (Objects.requireNonNull(miejsceSpotkaniaComboBox.getSelectedItem()).toString().equals(newMeetingPlace)) {
            adres = adresTextField.getText();
            miasto = miastoTextField.getText();
            if (!(isAdressNameValid(adres) && isAdressNameValid(miasto))) {
                JOptionPane.showMessageDialog(null, "Adres lub miasto nie mogą być puste", "Informacja", JOptionPane.ERROR_MESSAGE);
                return 0;
            }
            sql = "BEGIN INSERT INTO miejsca(adres, miasto) VALUES(?, ?) RETURNING id_miejsca INTO ?; END;";
            try (CallableStatement stmt = conn.prepareCall(sql)) {
                System.out.println(sql);
                stmt.setString(1, adres);
                stmt.setString(2, miasto);
                stmt.registerOutParameter(3, Types.INTEGER);
                stmt.execute();
                miejsceId = stmt.getInt(3);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, ErrorSQL(ex.getErrorCode()) + " " + "Błąd dodania miejsca", "Informacja", JOptionPane.ERROR_MESSAGE);
                System.out.println("Błąd wykonania polecenia: " + ex.getMessage());
                return 0;
            }
        } else {
            String[] miejsce = miejsceSpotkaniaComboBox.getSelectedItem().toString().split(",");
            adres = miejsce[0];
            miasto = miejsce[1].stripLeading();

            sql = "SELECT id_miejsca FROM miejsca WHERE miasto LIKE '" + miasto + "' AND adres LIKE '" + adres + "'";
            System.out.println(sql);
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)
            ) {
                rs.next();
                miejsceId = rs.getInt(1);

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, ErrorSQL(ex.getErrorCode()) + " " + "Błąd wykonania polecenia", "Informacja", JOptionPane.ERROR_MESSAGE);
                System.out.println("Błąd wykonania polecenia: " + ex.getErrorCode());
            }
        }

        String day = Objects.requireNonNull(dzienSpotkaniaComboBox.getSelectedItem()).toString();
        String month = Objects.requireNonNull(miesiacSpotkaniaComboBox.getSelectedItem()).toString();
        String year = Objects.requireNonNull(rokSpotkaniaComboBox.getSelectedItem()).toString();
        String date = year + "-" + month + "-" + day;

        // alter spotkania
        sql = "UPDATE spotkania SET data = TO_DATE('" + date + "', 'YYYY-MM-DD'), id_miejsca = " + miejsceId + " WHERE id_spotkania = " + id;
        try {
            Statement stmt = conn.createStatement();
            System.out.println(sql);
            r = stmt.executeUpdate(sql);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ErrorSQL(ex.getErrorCode()) + " " + "Błąd wykonania polecenia", "Informacja", JOptionPane.ERROR_MESSAGE);
            System.out.println("Błąd wykonania polecenia: " + ex.getErrorCode());
        }

        ArrayList<String> food = new ArrayList<>();
        if (isFoodEdited) {
            food = updateFood(id, row);
        }

        if (r == 1) {
            model.setValueAt(String.format("%02d", Integer.parseInt(day)) + "." + String.format("%02d", Integer.parseInt(month)) + "." + year, row, 1);
            model.setValueAt(adres + ", " + miasto, row, 2);
            if(isFoodEdited) {
                model.setValueAt(String.join(", ", food), row, 3);
                isFoodEdited = false;
            }
        }
        return r;
    }

    private ArrayList<String> updateFood(String id, int row) {

        ArrayList<String> food = new ArrayList<>();

        for (int i = 0; i < jedzenieTableModel.getRowCount(); i++) {
            food.add(jedzenieTableModel.getValueAt(i, 0).toString());
        }

        String sql = "INSERT INTO jedzenie (nazwa) VALUES (?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            System.out.println(sql);
            for (String f : food) {
                System.out.println(f);
                pstmt.setString(1, f);
                try (ResultSet rs = pstmt.executeQuery()) {
                } catch (SQLException ex) {
                    if(ex.getErrorCode() != 1) {
                        JOptionPane.showMessageDialog(null, ErrorSQL(ex.getErrorCode()) + " " + "Błąd przetwarzania polecenia", "Informacja", JOptionPane.ERROR_MESSAGE);
                        System.out.println("Błąd wykonania polecenia: " + ex.getMessage() + " :" + ex.getErrorCode());
                    }
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ErrorSQL(ex.getErrorCode()) + " " + "Błąd dodania jedzenia", "Informacja", JOptionPane.ERROR_MESSAGE);
            System.out.println("Błąd wykonania polecenia preparestmt insert jedzenie spot: " + ex.getMessage());
        }

        // usuwanie
        sql = "DELETE FROM jedz_spot WHERE id_spotkania = " + id;

        try (Statement stmt = conn.createStatement()) {
            System.out.println(sql);
            stmt.executeUpdate(sql);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ErrorSQL(ex.getErrorCode()) + " " + "Błąd edycji jedzenia", "Informacja", JOptionPane.ERROR_MESSAGE);
            System.out.println("Błąd wykonania polecenia: " + ex.getMessage());
        }

        sql = "INSERT INTO jedz_spot (id_spotkania, nazwa) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            System.out.println(sql);
            for (String f : food) {
                pstmt.setString(1, id);
                pstmt.setString(2, f);
                try (ResultSet rs = pstmt.executeQuery()) {
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(null, ErrorSQL(ex.getErrorCode()) + " " + "Błąd edycji jedzenia", "Informacja", JOptionPane.ERROR_MESSAGE);
                    System.out.println("Błąd wykonania polecenia: " + ex.getMessage() + " :" + ex.getErrorCode());
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ErrorSQL(ex.getErrorCode()) + " " + "Błąd edycji jedzenia", "Informacja", JOptionPane.ERROR_MESSAGE);
            System.out.println("Błąd wykonania polecenia preparestmt insert jedz_spot: " + ex.getMessage());
        }

        return food;
    }


    private Integer updatePlayer(String id, int row) {
        String sql;
        int rs = -1;

        String name = playerNameTextField.getText();
        String surname = playerSurnameTextField.getText();
        Integer day = (Integer) dzienComboBox.getSelectedItem();
        Integer month = (Integer) miesiacComboBox.getSelectedItem();
        Integer year = (Integer) rokComboBox.getSelectedItem();
        String birthDate = year + "-" + month + "-" + day;

        sql = "UPDATE Gracze SET imie = '" + name + "', nazwisko = '" + surname + "', data_urodzenia = TO_DATE('" + birthDate + "', 'YYYY-MM-DD') WHERE id_gracza = " + id;
        System.out.println(sql);

        try {
            Statement stmt = conn.createStatement();
            rs = stmt.executeUpdate(sql);
            System.out.println("Zaktualizowano " + rs + " wierszy w tabeli Gracze");
        } catch (SQLException ex) {
            System.out.println("Błąd wykonania zapytania" + ex.getMessage());
        }
        if (rs > 0) {
            model.setValueAt(name, row, 1);
            model.setValueAt(surname, row, 2);
            model.setValueAt(String.format("%02d", day) + "." + String.format("%02d", month) + "." + year, row, 3);
        }
        return rs;
    }

    private int updateExpansion(String id, int row) {
        String sql;
        int rs = -1;

        String title = expansionTitleTextField.getText();
        Integer max = (Integer) expansionMaxSpinner.getValue();

        Item game = (Item) expansionGameComboBox.getSelectedItem();

        sql = "UPDATE Rozszerzenia SET tytul = '" + title + "', id_gry = " + game.getValue() + ", mod_liczby_graczy = " + max + " WHERE id_roz = " + id;
        System.out.println(sql);

        try {
            Statement stmt = conn.createStatement();
            rs = stmt.executeUpdate(sql);
            System.out.println("Zaktualizowano " + rs + " wierszy w tabeli Rozszerzenia");
        } catch (SQLException ex) {
            System.out.println("Błąd wykonania zapytania" + ex.getMessage());
        }
        if (rs > 0) {
            model.setValueAt(title, row, 1);
            model.setValueAt(game.getDescription(), row, 3);
            model.setValueAt(max, row, 4);

        }
        return rs;
    }

    private int updateGame(String id, int row) {
        String sql;
        int rs = -1;
        String title = gameTitleTextField.getText();
        Integer min = (Integer) minSpinner.getValue();
        Integer max = (Integer) maxSpinner.getValue();
        Integer ocena = (Integer) ocenaSpinner.getValue();
        if(!ocenaCheckBox.isSelected()) {
            ocena = null;
        }
        if(!maxCheckBox.isSelected()) {
            max = null;
        }

        sql = "UPDATE Gry SET tytul = '" + title + "', min_graczy = " + min + ", max_graczy = " + max + ", ocena = " + ocena + " WHERE id_gry = " + id;
        System.out.println(sql);

        try {
            Statement stmt = conn.createStatement();
            rs = stmt.executeUpdate(sql);
            System.out.println("Zaktualizowano " + rs + " wierszy w tabeli Gry");
        } catch (SQLException ex) {
            System.out.println("Błąd wykonania zapytania" + ex.getMessage());
        }
        if (rs > 0) {
            model.setValueAt(title, row, 1);
            model.setValueAt(min, row, 2);
            model.setValueAt(max, row, 3);
            model.setValueAt(ocena, row, 4);
        }
        return rs;
    }

    private void fillTable(String sql, int[] columns) {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Vector<Object> data = new Vector<>();
                for (int c : columns) {
                    data.add(rs.getObject(c));
                }
                model.addRow(data);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getErrorCode() + ": " + ex.getMessage());
            System.out.println("Błąd wykonania polecenia: " + ex.getMessage());
        }
    }

    private void setupGameTable() {
        model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table.setModel(model);
        model.addColumn("ID");
        model.addColumn("Tytuł");
        model.addColumn("Min graczy");
        model.addColumn("Max graczy");
        model.addColumn("Ocena");

        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);

        int[] columns = {1, 2, 3, 4, 5};
        String sql = "SELECT * FROM Gry";
        fillTable(sql, columns);
    }

    private void setupGameEditPanel(int row) {

        editGamePanel.setVisible(true);

        String oldTitle = model.getValueAt(row, 1).toString();
        int oldMin = Integer.parseInt(model.getValueAt(row, 2).toString());
        Object o = model.getValueAt(row, 3);
        int oldMax = 0;
        if (o != null) {
            oldMax = Integer.parseInt(o.toString());
        }
        int oldOcena = 0;
        o = model.getValueAt(row, 4);
        if (o != null) {
            oldOcena = Integer.parseInt(o.toString());
        }

        gameTitleTextField.setText(oldTitle);
        minSpinner.setModel(new SpinnerNumberModel(1, 1, 64, 1));
        maxSpinner.setModel(new SpinnerNumberModel(1, 1, 64, 1));
        ocenaSpinner.setModel(new SpinnerNumberModel(1, 1, 10, 1));
        minSpinner.setValue(oldMin);
        if (oldMax == 0) {
            maxCheckBox.setSelected(false);
            maxSpinner.setEnabled(false);
        } else {
            maxCheckBox.setSelected(true);
            maxSpinner.setValue(oldMax);
        }
        maxCheckBox.addActionListener(event -> {
            JCheckBox cb = (JCheckBox) event.getSource();
            maxSpinner.setEnabled(cb.isSelected());
        });
        if (oldOcena == 0) {
            ocenaCheckBox.setSelected(false);
            ocenaSpinner.setEnabled(false);
        } else {
            ocenaCheckBox.setSelected(true);
            ocenaSpinner.setValue(oldOcena);
        }



    }

    private void setupExpansionTable() {
        model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table.setModel(model);
        model.addColumn("ID");
        model.addColumn("Tytuł");
        model.addColumn("ID gry");
        model.addColumn("Gra");
        model.addColumn("Modyfikacja limitu graczy");

        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);

        int[] columns = {1, 2, 3, 4, 5};
        String sql = "SELECT r.id_roz, r.tytul, id_gry, g.tytul, r.mod_liczby_graczy FROM rozszerzenia r JOIN gry g USING(id_gry)";
        fillTable(sql, columns);
    }

    private void setupEditExpansionPanel(int row) {
        editExpansionPanel.setVisible(true);
        expansionGameComboBox.removeAllItems();
        expansionMaxSpinner.setModel(new SpinnerNumberModel(1, 1, 64, 1));
        String oldTitle = model.getValueAt(row, 1).toString();
        Integer oldGameId = Integer.parseInt(model.getValueAt(row, 2).toString());
        String oldGameTitle = model.getValueAt(row, 3).toString();
        Object o = model.getValueAt(row, 4);
        if (o != null) {
            int oldMax = Integer.parseInt(o.toString());
            expansionMaxCheckBox.setSelected(true);
            expansionMaxSpinner.setValue(oldMax);
        } else {
            expansionMaxCheckBox.setSelected(false);
            expansionMaxSpinner.setEnabled(false);
        }

        expansionTitleTextField.setText(oldTitle);

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT id_gry, tytul FROM gry ORDER BY tytul")) {
            while (rs.next()) {
                expansionGameComboBox.addItem(new Item<Integer>(rs.getInt(1), rs.getString(2)));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getErrorCode() + ": " + ex.getMessage());
            System.out.println("Błąd wykonania polecenia: " + ex.getMessage());
        }

        expansionGameComboBox.setSelectedItem(new Item<>(oldGameId, oldGameTitle));


    }

    private void setupPlayerTable() {
        model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table.setModel(model);
        model.addColumn("ID");
        model.addColumn("Imię");
        model.addColumn("Nazwisko");
        model.addColumn("Data urodzenia");

        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);

        int[] columns = {1, 2, 3, 4};
        String sql = "SELECT id_gracza, imie, nazwisko, TO_CHAR(data_urodzenia, 'DD.MM.YYYY') FROM gracze";
        fillTable(sql, columns);
    }

    private void setupEditPlayerPanel(int row) {
        editPlayerPanel.setVisible(true);
        dzienComboBox.removeAllItems();
        miesiacComboBox.removeAllItems();
        rokComboBox.removeAllItems();

        for (int i = 1; i <= 31; i++) {
            dzienComboBox.addItem(i);
        }

        for (int i = 1; i <= 12; i++) {
            miesiacComboBox.addItem(i);
        }

        for (int i = 1900; i <= 2024; i++) {
            rokComboBox.addItem(i);
        }

        String oldName = model.getValueAt(row, 1).toString();
        String oldSurname = model.getValueAt(row, 2).toString();
        String oldDate = model.getValueAt(row, 3).toString();
        System.out.println(oldDate);
        String[] splitDate = oldDate.split("\\.");
        for (int i = 0; i < 3; i++) {
            playerDateComboBoxes.get(i).setSelectedItem(Integer.parseInt(splitDate[i]));
        }
        playerNameTextField.setText(oldName);
        playerSurnameTextField.setText(oldSurname);


    }

    private void setupEditMeetingTable() {
        editMatchOfMeeting = false;
        model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table.setModel(model);
        model.addColumn("ID");
        model.addColumn("Data spotkania");
        model.addColumn("Adres");
        model.addColumn("Menu");

        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);

        int[] columns = {1, 2, 3, 4};
        String sql = "SELECT s.id_spotkania, TO_CHAR(s.data, 'DD.MM.YYYY'), m.adres || ', ' ||  m.miasto, (SELECT LISTAGG(nazwa, ',')" +
                " FROM jedz_spot WHERE id_spotkania = s.id_spotkania)" +
                " FROM  spotkania s JOIN miejsca m USING(id_miejsca)";
        fillTable(sql, columns);
    }

    private void setupEditMeetingPanel(int row) {
        editMeetingPanel.setVisible(true);

        dzienSpotkaniaComboBox.removeAllItems();
        miesiacSpotkaniaComboBox.removeAllItems();
        rokSpotkaniaComboBox.removeAllItems();

        for (int i = 1; i <= 31; i++) {
            dzienSpotkaniaComboBox.addItem(i);
        }

        for (int i = 1; i <= 12; i++) {
            miesiacSpotkaniaComboBox.addItem(i);
        }

        for (int i = 1900; i <= 2024; i++) {
            rokSpotkaniaComboBox.addItem(i);
        }

        miejsceSpotkaniaComboBox.removeItemListener(miejsceSpotkaniaCBListener);

        miejsceSpotkaniaComboBox.removeAllItems();

        String oldDate = model.getValueAt(row, 1).toString();
        String[] splitDate = oldDate.split("\\.");
        for (int i = 0; i < 3; i++) {
            meetingDateComboBoxes.get(i).setSelectedItem(Integer.parseInt(splitDate[i]));
        }

        miejsceSpotkaniaComboBox.addItem(new Item<>(0, newMeetingPlace));

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT id_miejsca, adres || ', ' ||  miasto FROM miejsca ORDER BY miasto")) {
            while (rs.next()) {
                miejsceSpotkaniaComboBox.addItem(new Item<>(rs.getInt(1), rs.getString(2)));
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ErrorSQL(ex.getErrorCode()) + " " + "Błąd pobrania listy miejsc", "Błąd", JOptionPane.ERROR_MESSAGE);
            System.out.println("Błąd wykonania polecenia: " + ex.getMessage());
        }

        String currentPlace = model.getValueAt(row, 2).toString();
        System.out.println("Obecne: " + currentPlace);

        for (int i = 0; i < miejsceSpotkaniaComboBox.getItemCount(); i++) {
            if(miejsceSpotkaniaComboBox.getItemAt(i).toString().equals(currentPlace)) {
                miejsceSpotkaniaComboBox.setSelectedIndex(i);
            }
        }

        miejsceSpotkaniaComboBox.addItemListener(miejsceSpotkaniaCBListener);


        jedzenieTableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        jedzenieTable.setModel(jedzenieTableModel);
        jedzenieTableModel.addColumn("Potrawy i przekąski");

        meetingId = Integer.parseInt(model.getValueAt(row, 0).toString());

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT nazwa FROM jedz_spot WHERE id_spotkania = " + meetingId)) {
            while (rs.next()) {
                System.out.println(rs.getString(1));
                jedzenieTableModel.addRow(new Object[]{rs.getString(1)});
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ErrorSQL(ex.getErrorCode()) + " " + "Błąd pobrania listy jedzenia", "Błąd", JOptionPane.ERROR_MESSAGE);
            System.out.println("Błąd wykonania polecenia: " + ex.getMessage());
        }


    }

    private void setNewMeetingTextFieldsEnabled(boolean enabled) {
        adresTextField.setEnabled(enabled);
        miastoTextField.setEnabled(enabled);
    }

    private void setupEditMatchTableAndPanel() {
        editMatchOfMeeting = true;
        setupEditPanels();
        model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table.setModel(model);
        model.addColumn("Id_rozgrywki");
        model.addColumn("Id_zestawu");
        model.addColumn("Gra");
        model.addColumn("Rozszerzenia");
        model.addColumn("Czas trwania");
        model.addColumn("Gracze");


        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);


        table.getColumnModel().getColumn(1).setMinWidth(0);
        table.getColumnModel().getColumn(1).setMaxWidth(0);

        //TODO: zmień szerokość, zawijaj kolumny

        System.out.println("Meeting id " + meetingId);

        String sql = "SELECT x.id_rozgr, y.id_zest, g.tytul, " +
                "(SELECT LISTAGG(r.tytul, ',') FROM rozsz_zest rz JOIN rozszerzenia r ON rz.id_roz = r.id_roz WHERE rz.id_zestawu = y.id_zest) as id_rozszerzen, " +
                "y.czas_rozgrywki, x.gracze " +
                "FROM (SELECT grs.id_rozgrywki as id_rozgr, LISTAGG(g.imie || ' ' || g.nazwisko, ', ') as gracze FROM gr_w_rozgr_spot grs " +
                "JOIN gracze g ON grs.id_gracza = g.id_gracza " +
                "WHERE grs.id_spotkania = " + meetingId + " GROUP BY grs.id_rozgrywki) x " +
                "JOIN (SELECT r.id_rozgrywki as id_rozgr, r.id_zestawu as id_zest, z.Gry_id_gry as id_gry, r.czas as czas_rozgrywki FROM rozgrywki r JOIN zestawy z ON r.id_zestawu = z.id_zestawu) y " +
                "ON x.id_rozgr = y.id_rozgr " +
                "JOIN gry g ON y.id_gry = g.id_gry";
        int[] columns = {1, 2, 3, 4, 5, 6};

        fillTable(sql, columns);

        edytujButton.setEnabled(false);
    }

    public JPanel getPanel() {
        return updateDataPanel;
    }

}
