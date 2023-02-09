package put.boardgames.app;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static put.boardgames.app.Main.conn;

public class GameData {
    private JTable gameTable;
    public JButton wrocButton;
    private JPanel gameDataPanel;
    private JLabel longestMatchLabel;
    private JLabel shortestMatchLabel;
    private JLabel titleLabel;
    private DefaultTableModel model;

    GameData() {
        setupTable();
        setupLabels();
    }

    public void setupLabels() {
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        longestMatchLabel.setFont(new Font("Arial", Font.BOLD, 16));
        shortestMatchLabel.setFont(new Font("Arial", Font.BOLD, 16));
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT * FROM " +
                             "(SELECT g.tytul, r.czas FROM gry g " +
                             "JOIN zestawy z ON g.id_gry = z.Gry_id_gry " +
                             "JOIN rozgrywki r ON r.id_zestawu = z.id_zestawu " +
                             "ORDER BY r.czas DESC) " +
                             "WHERE ROWNUM = 1")) {
            while (rs.next()) {
                longestMatchLabel.setText("Najdłuższa rozgrywka trwała " + rs.getInt(2) + " minut (" + rs.getString(1) + ")");
                if (rs.wasNull()) {
                    longestMatchLabel.setText("");
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getErrorCode() + ": " + ex.getMessage());
            System.out.println("Błąd wykonania polecenia: " + ex.getMessage());
        }

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT * FROM " +
                             "(SELECT g.tytul, r.czas FROM gry g " +
                             "JOIN zestawy z ON g.id_gry = z.Gry_id_gry " +
                             "JOIN rozgrywki r ON r.id_zestawu = z.id_zestawu " +
                             "ORDER BY r.czas ASC) " +
                             "WHERE ROWNUM = 1")) {
            while (rs.next()) {
                shortestMatchLabel.setText("Najkrótsza rozgrywka trwała " + rs.getInt(2) + " minut (" + rs.getString(1) + ")");
                if (rs.wasNull()) {
                    shortestMatchLabel.setText("");
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getErrorCode() + ": " + ex.getMessage());
            System.out.println("Błąd wykonania polecenia: " + ex.getMessage());
        }
    }

    public void setupTable() {
        model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        gameTable.setModel(model);
        model.addColumn("Tytuł");
        model.addColumn("Minimalna l. graczy");
        model.addColumn("Maksymalna l.graczy");
        model.addColumn("Średni czas gry");
        model.addColumn("Liczba rozgrywek");

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT x.id, g.tytul, g.min_graczy, g.max_graczy, x.srednia, y.liczba FROM " +
                             "(SELECT g.id_gry AS id, AVG(r.czas) as srednia FROM gry g JOIN zestawy z ON g.id_gry = z.Gry_id_gry JOIN rozgrywki r ON r.id_zestawu = z.id_zestawu GROUP BY id_gry) x " +
                             "JOIN (SELECT g.id_gry AS id, COUNT(r.czas) as liczba FROM gry g JOIN zestawy z ON g.id_gry = z.Gry_id_gry JOIN rozgrywki r ON r.id_zestawu = z.id_zestawu GROUP BY id_gry) y " +
                             "ON x.id = y.id " +
                             "JOIN gry g ON x.id = g.id_gry " +
                             "ORDER BY g.tytul")) {
            while (rs.next()) {
                Integer max = rs.getInt(4);
                if (max == 0) {
                    model.addRow(new Object[]{rs.getString(2), rs.getInt(3), null, rs.getDouble(5), rs.getInt(6)});
                } else {
                    model.addRow(new Object[]{rs.getString(2), rs.getInt(3), max, rs.getDouble(5), rs.getInt(6)});
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getErrorCode() + ": " + ex.getMessage());
            System.out.println("Błąd wykonania polecenia: " + ex.getMessage());
        }
    }

    public JPanel getPanel() {
        return gameDataPanel;
    }

}
