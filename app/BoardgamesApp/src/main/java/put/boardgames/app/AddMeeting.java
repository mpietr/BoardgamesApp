package put.boardgames.app;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import static put.boardgames.app.ErrorsSQL.ErrorSQL;
import static put.boardgames.app.Main.conn;
import static put.boardgames.app.Validators.*;

public class AddMeeting {
    private JLabel titleLabel;
    private JComboBox<Integer> dzienComboBox;
    private JComboBox<Integer> miesiacComboBox;
    private JComboBox<Integer> rokComboBox;
    private JComboBox<String> miejsceComboBox;
    private JTextField adresTextField;
    private JTextField miastoTextField;
    public JButton anulujButton;
    public JButton dodajButton;
    private JPanel addMeetingPanel;
    private JLabel miastoLabel;
    private JLabel adresLabel;
    private JLabel dataLabel;
    private JLabel miejsceLabel;
    private JTable jedzenieTable;
    private JTextField jedzenieTextField;
    private JButton dodajJedzenieButton;

    private final String dodajNoweMiejsce = "-- dodaj nowe miejsce --";

    private int currentMeetingId = 0;

    private final DefaultTableModel model = new DefaultTableModel();

    AddMeeting() {
        setupLabels();
        setupDateComboBoxes();
        setupMiejsceComboBox();
        jedzenieTable.setModel(model);
        model.addColumn("Potrawy i przekąski");
        setupListeners();
    }

    private void setupLabels() {
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        dataLabel.setFont(new Font("Arial", Font.BOLD, 20));
        miejsceLabel.setFont(new Font("Arial", Font.BOLD, 20));
        adresLabel.setFont(new Font("Arial", Font.BOLD, 20));
        miastoLabel.setFont(new Font("Arial", Font.BOLD, 20));
    }

    public void refreshView() {
        addValuesToMiejsceComboBox();
        resetInputFields();
    }

    private void resetInputFields() {
        adresTextField.setText("");
        miastoTextField.setText("");
        model.setRowCount(0);
    }

    private void setupDateComboBoxes() {

        for (int i = 1; i <= 31; i++) { dzienComboBox.addItem(i); }

        for (int i = 1; i <= 12; i++) { miesiacComboBox.addItem(i); }

        for (int i = 1900; i <= 2023; i++) { rokComboBox.addItem(i); }

        java.util.Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);

        dzienComboBox.setSelectedItem(day);
        miesiacComboBox.setSelectedItem(month + 1);
        rokComboBox.setSelectedItem(year);

    }

    private void setupMiejsceComboBox() {
        miejsceComboBox.addItem(dodajNoweMiejsce);
        addValuesToMiejsceComboBox();

    }

    private void addValuesToMiejsceComboBox() {
        while (miejsceComboBox.getItemCount() > 1) {
            miejsceComboBox.removeItemAt(1);
        }
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT adres, miasto FROM miejsca ORDER BY miasto")) {
            while (rs.next()) {
                miejsceComboBox.addItem(rs.getString(2) + ", " + rs.getString(1));
                System.out.println(rs.getString(1));
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ErrorSQL(ex.getErrorCode()) + " " + "Błąd pobrania listy miejsc", "Błąd", JOptionPane.ERROR_MESSAGE);
            System.out.println("Błąd wykonania polecenia: " + ex.getMessage());
        }
        miejsceComboBox.setSelectedItem(dodajNoweMiejsce);
    }

    private void setupListeners() {
        miejsceComboBox.addItemListener(e -> changeVisibility(Objects.requireNonNull(miejsceComboBox.getSelectedItem()).toString().equals(dodajNoweMiejsce)));
        dodajJedzenieButton.addActionListener(e -> addFood());
        dodajButton.addActionListener(e ->
        {

            try {
                conn.setAutoCommit(false);
            } catch (SQLException ex) {
                System.out.println("Błąd wykonania polecenia autocommit off: " + ex.getMessage());
            }

            boolean insertMeetingExecuted = insertMeeting();
            boolean insertFoodExecuted = false;
            if (insertMeetingExecuted && model.getRowCount() > 0) {
                insertFoodExecuted = insertFood();
            } else {
                insertFoodExecuted = true;
            }

            if (insertMeetingExecuted && insertFoodExecuted) {
                try {
                    conn.commit();
                    resetInputFields();
                    JOptionPane.showMessageDialog(null, "Dodano spotkanie", "Sukces", JOptionPane.INFORMATION_MESSAGE);
                } catch (SQLException ex) {
                    System.out.println("Błąd wykonania polecenia któraś składowa zawiodła: " + ex.getMessage());
                }
            }
            else {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.out.println("Błąd wykonania polecenia rollback: " + ex.getMessage());
                }
            }

            try {
                conn.setAutoCommit(true);
            } catch (SQLException ex) {
                System.out.println("Błąd wykonania polecenia autocommit : " + ex.getMessage());
            }
        });

    }

    private boolean insertMeeting()  {
        int dzien = (Integer) dzienComboBox.getSelectedItem();
        int miesiac = (Integer) miesiacComboBox.getSelectedItem();
        int rok = (Integer) rokComboBox.getSelectedItem();
        String data = rok + "-" + miesiac + "-" + dzien;
        Integer miejsceId = 0;
        if (Objects.requireNonNull(miejsceComboBox.getSelectedItem()).toString().equals(dodajNoweMiejsce)) {
            String adres = adresTextField.getText();
            String miasto = miastoTextField.getText();
            if (!(isAdressNameValid(adres) && isAdressNameValid(miasto))) {
                JOptionPane.showMessageDialog(null, "Adres lub miasto nie mogą być puste", "Informacja", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            try (CallableStatement stmt = conn.prepareCall("BEGIN INSERT INTO miejsca(adres, miasto) VALUES(?, ?) RETURNING id_miejsca INTO ?; END;")) {
                stmt.setString(1, adres);
                stmt.setString(2, miasto);
                stmt.registerOutParameter(3, Types.INTEGER);
                stmt.execute();
                miejsceId = stmt.getInt(3);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, ErrorSQL(ex.getErrorCode()) + " " + "Błąd dodania miejsca", "Informacja", JOptionPane.ERROR_MESSAGE);
                System.out.println("Błąd wykonania polecenia: " + ex.getMessage());
                return false;
            }
        } else {
            String[] miejsce = miejsceComboBox.getSelectedItem().toString().split(",");
            String miasto = miejsce[0];
            String adres = miejsce[1].stripLeading();


            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                         "SELECT id_miejsca FROM miejsca WHERE miasto LIKE '" + miasto + "' AND adres LIKE '" + adres + "'")) {
                rs.next();
                miejsceId = rs.getInt(1);

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, ErrorSQL(ex.getErrorCode()) + " " + "Błąd wykonania polecenia", "Informacja", JOptionPane.ERROR_MESSAGE);
                System.out.println("Błąd wykonania polecenia: " + ex.getErrorCode());
                return false;
            }
        }

        String sql = "BEGIN INSERT INTO spotkania(data, id_miejsca) VALUES(TO_DATE(?, 'YYYY-MM-DD'), ?) RETURNING id_spotkania INTO ?; END;";

        try (CallableStatement stmt = conn.prepareCall(sql)) {
            //stmt.executeUpdate("INSERT INTO spotkania (data, id_miejsca) VALUES(TO_DATE('" + data + "', 'YYYY-MM-DD'), " + miejsceId + ")");
            stmt.setString(1, data);
            stmt.setInt(2, miejsceId);
            stmt.registerOutParameter(3, Types.INTEGER);
            stmt.execute();
            currentMeetingId = stmt.getInt(3);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ErrorSQL(ex.getErrorCode()) + " " + "Błąd wykonania polecenia", "Informacja", JOptionPane.ERROR_MESSAGE);
            System.out.println("Błąd wykonania polecenia: " + ex.getErrorCode());
            return false;
        }
        return true;
    }

    private boolean insertFood() {


        ArrayList<String> food = new ArrayList<>();

        for (int i = 0; i < model.getRowCount(); i++) {
            food.add(model.getValueAt(i, 0).toString());
        }

        try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO jedzenie (nazwa) VALUES (?)")) {

            for (String f : food) {
                System.out.println(f);
                pstmt.setString(1, f);
                try (ResultSet rs = pstmt.executeQuery()) {
                } catch (SQLException ex) {
                    if(ex.getErrorCode() != 1) {
                        JOptionPane.showMessageDialog(null, ErrorSQL(ex.getErrorCode()) + " " + "Błąd przetwarzania polecenia", "Informacja", JOptionPane.ERROR_MESSAGE);
                        System.out.println("Błąd wykonania polecenia: " + ex.getMessage() + " :" + ex.getErrorCode());
                        return false;
                    }
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ErrorSQL(ex.getErrorCode()) + " " + "Błąd dodania jedzenia", "Informacja", JOptionPane.ERROR_MESSAGE);
            System.out.println("Błąd wykonania polecenia preparestmt insert jedzenie spot: " + ex.getMessage());
            return false;
        }

        try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO Jedz_spot (id_spotkania, nazwa) VALUES(?,?)")) {

            for (String f : food) {
                pstmt.setInt(1, currentMeetingId);
                System.out.println("currentMeetingId: " + currentMeetingId);
                pstmt.setString(2, f);
                try (ResultSet rs = pstmt.executeQuery()) {
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(null, ErrorSQL(ex.getErrorCode()) + " " + "Błąd wykonania polecenia", "Informacja", JOptionPane.ERROR_MESSAGE);
                    System.out.println("Błąd wykonania polecenia executequery: " + ex.getMessage());
                    return false;
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ErrorSQL(ex.getErrorCode()) + " " + "Błąd wykonania polecenia", "Informacja", JOptionPane.ERROR_MESSAGE);
            System.out.println("Błąd wykonania polecenia preparestmt: " + ex.getMessage());
            return false;
        }
        return true;
    }

    private void addFood() {
        String name = jedzenieTextField.getText();
        if (isFoodNameValid(name)) {
            jedzenieTextField.setText("");
            Object[] row = {name};
            model.addRow(row);
        } else {
            JOptionPane.showMessageDialog(null, "Nazwa jedzenia nie może być pusta", "Informacja", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void changeVisibility(boolean visible) {
        adresTextField.setVisible(visible);
        miastoTextField.setVisible(visible);
        miastoLabel.setVisible(visible);
        adresLabel.setVisible(visible);
    }

    public JPanel getPanel() {
        return addMeetingPanel;
    }

    public int getCurrentMeetingId() {
        if (this.currentMeetingId != 0)
            return this.currentMeetingId;
        else {
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                         "SELECT MAX(id_spotkania) FROM spotkania WHERE data = (SELECT MAX(data) FROM spotkania)")) {
                rs.next();
                return rs.getInt(1);

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, ErrorSQL(ex.getErrorCode()) + " " + "Nie można pobrać ID spotkania", "Informacja", JOptionPane.ERROR_MESSAGE);
                System.out.println("Błąd wykonania polecenia: " + ex.getErrorCode());

            }
        }
        return 0;
    }

}
