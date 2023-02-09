package put.boardgames.app;

import javax.swing.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    public static Connection conn;

    //args: database connection url, username, password
    public static void main(String[] args) {

        final JPanel panel = new JPanel();

        conn = null;
        String connectionString = args[0];
        Properties connectionProps = new Properties();
        connectionProps.put("user", args[1]);
        connectionProps.put("password", args[2]);

        try {
            conn = DriverManager.getConnection(connectionString,
                    connectionProps);
            System.out.println("Połączono z bazą danych");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(panel, "Nie udało się połączyć z bazą danych", "Error", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE,
                    "Nie udało się połączyć z bazą danych", ex);
            System.exit(-1);
        }

        BoardgamesApp bg = new BoardgamesApp();

    }
}