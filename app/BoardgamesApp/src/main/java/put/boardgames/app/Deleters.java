package put.boardgames.app;

import javax.swing.*;
import java.sql.SQLException;
import java.sql.Statement;

import static put.boardgames.app.Main.conn;
import static put.boardgames.app.UpdateData.model;

public class Deleters {

    public static Integer deleteMatch(String id, int row) {

        int rs = -1;

        String sql = "DELETE FROM Rozgrywki WHERE id_rozgrywki = " + id;
        try {
            System.out.println(sql);
            Statement stmt = conn.createStatement();
            rs = stmt.executeUpdate(sql);
            System.out.println("Usunięto " + rs + " rekordów z tabeli Rozgrywki");
            model.removeRow(row);
        } catch (SQLException e) {
            System.out.println("Błąd. " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Nie można usunąć rozgrywki", "Błąd", JOptionPane.ERROR_MESSAGE);
        }
        return rs;
    }

    public static Integer deleteMeeting(String id, int row) {
        int rs = -1;
        String sql = "DELETE FROM Spotkania WHERE id_spotkania = " + id;
        try {
            System.out.println(sql);
            Statement stmt = conn.createStatement();
            rs = stmt.executeUpdate(sql);
            System.out.println("Usunięto " + rs + " rekordów z tabeli Spotkania");
            model.removeRow(row);
        } catch (SQLException e) {
            System.out.println("Błąd. " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Nie można usunąć spotkania", "Błąd", JOptionPane.ERROR_MESSAGE);
        }
        return rs;
    }

    public static Integer deletePlayer(String id, int row) {
        int rs = -1;
        String sql = "DELETE FROM Gracze WHERE id_gracza = " + id;
        try {
            System.out.println(sql);
            Statement stmt = conn.createStatement();
            rs = stmt.executeUpdate(sql);
            System.out.println("Usunięto " + rs + " rekordów z tabeli Gracze");
            model.removeRow(row);
        } catch (SQLException e) {
            System.out.println("Błąd. " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Nie można usunąć gracza, ponieważ jest on przypisany do spotkania", "Błąd", JOptionPane.ERROR_MESSAGE);
        }
        return rs;
    }

    public static Integer deleteSet(String id_gry) {
        int rs = -1;
        String sql = "DELETE FROM Zestawy WHERE gry_id_gry = " + id_gry;
        try {
            System.out.println(sql);
            Statement stmt = conn.createStatement();
            rs = stmt.executeUpdate(sql);
            System.out.println("Usunięto " + rs + " wierszy z tabeli Zestawy");
        } catch (SQLException ex) {
            System.out.println("Błąd wykonania zapytania" + ex.getMessage());
            JOptionPane.showMessageDialog(null, "Nie można usunąć gry, ponieważ istnieją zestawy tej gry używane przez graczy", "Informacja", JOptionPane.INFORMATION_MESSAGE);
        }
        return rs;
    }

    public static Integer deleteExpansion(String id_roz, int row, String id_gry) {
        int rs = -1;
        String sql;
        if (id_gry != null) {
            sql = "DELETE FROM Rozszerzenia WHERE id_gry = " + id_gry;
        } else {
            sql = "DELETE FROM Rozszerzenia WHERE id_roz = " + id_roz;
        }
        try {
            System.out.println(sql);
            Statement stmt = conn.createStatement();
            rs = stmt.executeUpdate(sql);
            System.out.println("Usunięto " + rs + " wierszy z tabeli Rozszerzenia");
            if (id_gry == null) {
                model.removeRow(row);
            }
        } catch (SQLException ex1) {
            System.out.println("Błąd wykonania zapytania" + ex1.getMessage());
            return rs;
        }
        return rs;
    }

    public static Integer deleteGame(String id, int row) {
        int rs = -1;
        String sql = "DELETE FROM Gry WHERE id_gry = " + id;
        System.out.println(sql);
        try {
            Statement stmt = conn.createStatement();
            rs = stmt.executeUpdate(sql);
            System.out.println("Usunięto " + rs + " wierszy z tabeli Gry");
        } catch (SQLException ex) {
            System.out.println("Błąd wykonania zapytania" + ex.getMessage());
            int result = JOptionPane.showConfirmDialog(null, "Czy chcesz usunąć grę wraz z jej zestawami i rozszerzeniami?", "Informacja", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                if (deleteSet(id) >= 0) {
                    if (deleteExpansion(null, row, id) > 0) {
                        deleteGame(id, row);
                    }
                }
            } else if (result == JOptionPane.NO_OPTION) {
                JOptionPane.showMessageDialog(null, "Operacja anulowana", "Informacja", JOptionPane.INFORMATION_MESSAGE);
            }
        }
        if (rs > 0) {
            model.removeRow(row);
        }
        return rs;
    }
}
