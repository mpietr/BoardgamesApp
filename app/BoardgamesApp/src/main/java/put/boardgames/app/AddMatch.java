package put.boardgames.app;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.sql.*;
import java.util.ArrayList;

import static put.boardgames.app.ErrorsSQL.ErrorSQL;
import static put.boardgames.app.Main.conn;
import static put.boardgames.app.Validators.isMatchDurationValid;

public class AddMatch {
    private JLabel titleLabel;
    private JPanel addMatchPanel;
    private JComboBox<String> rozszerzenieComboBox1;
    private JComboBox<String> rozszerzenieComboBox2;
    private JComboBox<String> graComboBox;
    private JButton dodajRozszerzenieButton;
    private JTable playersTable;
    private JLabel rozszerzenieLabel1;
    private JLabel rozszerzenieLabel2;
    private JLabel rozszerzenieLabel3;
    public JButton anulujButton;
    public JButton dodajButton;
    private JLabel graLabel;
    private JComboBox rozszerzenieComboBox3;
    private JList graczeList;
    private JButton dodajGraczaButton;
    private JTextField durationTextField;

    private final JLabel[] labels = {rozszerzenieLabel1, rozszerzenieLabel2, rozszerzenieLabel3};
    private final JComboBox[] comboBoxes = {rozszerzenieComboBox1, rozszerzenieComboBox2, rozszerzenieComboBox3};
    private final int addMax = 3;
    private int expNo = 0;

    private ArrayList<PlayerOfMatch> players;
    private ArrayList<PlayerOfMatch> selectedPlayers;

    private final DefaultTableModel tableModel = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 2;
        }
    };
    private DefaultListModel listModel;

    private Integer currentMeetingId;
    private int currentSetId;

    private int currentMatchId;

    private AddMeeting addMeeting;




    AddMatch(AddMeeting addMeeting) {
        this.addMeeting = addMeeting;
        setupPanel();
        setupTable();
        setupList();
        setupListeners();
        setupComboBoxListener();
        setupComboBox();
    }

    public void refreshView() {
        resetInputFields();
        setupList();
        setupComboBox();
    }

    private void resetInputFields() {
        changeVisibility(0);
        tableModel.setRowCount(0);
        durationTextField.setText("");
    }

    public JPanel getPanel() {
        return addMatchPanel;
    }

    private void setupPanel() {
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        changeVisibility(0);

    }

    private void setupTable() {
        playersTable.setModel(tableModel);
        tableModel.addColumn("Imię");
        tableModel.addColumn("Nazwisko");
        tableModel.addColumn("Pozycja");
    }


    private void changeVisibility(int no_visible) {
        for (int i = 0; i < addMax; i++) {
            labels[i].setVisible(i < no_visible);
            comboBoxes[i].setVisible(i < no_visible);
        }
    }

    private void setupListeners() {
        dodajRozszerzenieButton.addActionListener(e ->
        {
            expNo++;
            changeVisibility(expNo);
        });

        dodajGraczaButton.addActionListener(e -> moveSelectedPlayers());

        dodajButton.addActionListener(e -> {
            try {
                conn.setAutoCommit(false);
            } catch (SQLException ex) {
                System.out.println("Błąd wykonania polecenia: " + ex.getErrorCode());
            }

            boolean insertSetExecuted = insertSet();
            boolean insertMatchExecuted = insertMatch();
            boolean insertPlayers = insertPlayersOfMatch();

            if (insertSetExecuted && insertMatchExecuted && insertPlayers) {
                try {
                    conn.commit();
                    JOptionPane.showMessageDialog(null, "Dodano rozgrywkę", "Sukces", JOptionPane.INFORMATION_MESSAGE);
                    resetInputFields();
                } catch (SQLException ex) {
                    System.out.println("Błąd wykonania polecenia: " + ex.getErrorCode());
                }

            } else {
                JOptionPane.showMessageDialog(null, "Nie udało się dodać rozgrywki");
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.out.println("Błąd wykonania polecenia: " + ex.getErrorCode());
                }
            }

            try {
                conn.setAutoCommit(true);
            } catch (SQLException ex) {
                System.out.println("Błąd wykonania polecenia: " + ex.getErrorCode());
            }
        });
    }

    private void moveSelectedPlayers() {
        tableModel.setRowCount(0);
        selectedPlayers = new ArrayList<>();
        int[] indices = graczeList.getSelectedIndices();
        for (int i : indices) {
            PlayerOfMatch p = players.get(i);
            selectedPlayers.add(p);
            tableModel.addRow(p.getAsRow());
        }
    }

    private boolean insertSet() {
        String sql = "BEGIN INSERT INTO zestawy (Gry_id_gry) " +
                "VALUES ((SELECT MIN(id_gry) FROM gry WHERE tytul LIKE ?)) " +
                "RETURNING id_zestawu INTO ?; END;";
        try (CallableStatement stmt = conn.prepareCall(sql)) {
            String gameTitle = graComboBox.getSelectedItem().toString();
            stmt.setString(1, gameTitle);
            stmt.registerOutParameter(2, Types.INTEGER);
            stmt.execute();
            currentSetId = stmt.getInt(2);
            System.out.println(currentSetId);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ErrorSQL(ex.getErrorCode()) + " " + "Błąd dodania zestawu", "Informacja", JOptionPane.ERROR_MESSAGE);
            System.out.println("Błąd wykonania polecenia 1: " + ex.getErrorCode());
            return false;
        }



        if (expNo > 0) {

            ArrayList<String> expansions = new ArrayList<>();

            for (int i = 0; i < expNo; i++) {
                Object o = comboBoxes[i].getSelectedItem();
                if( o != null){
                    expansions.add(o.toString());
                }
            }

            if (expansions.isEmpty()) {
                return true;
            }

            sql = "INSERT INTO Rozsz_zest(id_roz, id_zestawu) VALUES(" +
                    "(SELECT MIN(id_roz) FROM rozszerzenia WHERE tytul LIKE ?), ?)";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (String e : expansions) {
                    pstmt.setString(1, e);
                    pstmt.setInt(2, currentSetId);
                }

                try (ResultSet rs = pstmt.executeQuery()) {
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(null, ErrorSQL(ex.getErrorCode()) + " " + "Błąd wykonania polecenia", "Informacja", JOptionPane.ERROR_MESSAGE);
                    System.out.println("Błąd wykonania polecenia set: " + ex.getMessage());
                    return false;
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, ErrorSQL(ex.getErrorCode()) + " " + "Błąd wykonania polecenia", "Informacja", JOptionPane.ERROR_MESSAGE);
                System.out.println("Błąd wykonania polecenia1: " + ex.getMessage());
                return false;
            }
        }
        return true;
    }

    private boolean insertMatch() {
        String sql = "BEGIN INSERT INTO rozgrywki(czas, id_zestawu) VALUES (?, ?) RETURNING id_rozgrywki INTO ?; END;";
        int matchDuration = 0;

        try {
            matchDuration = Integer.parseInt(durationTextField.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Wpisz liczbę minut w polu tekstowym przeznaczonym na długość rozgrywki");
            return false;
        }

        if (isMatchDurationValid(matchDuration)) {
            try (CallableStatement stmt = conn.prepareCall(sql)) {
                stmt.setInt(1, matchDuration);
                stmt.setInt(2, currentSetId);
                stmt.registerOutParameter(3, Types.INTEGER);
                stmt.execute();
                currentMatchId = stmt.getInt(3);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, ErrorSQL(ex.getErrorCode()) + " " + "Błąd wykonania polecenia", "Informacja", JOptionPane.ERROR_MESSAGE);
                System.out.println("Błąd wykonania polecenia 2: " + ex.getErrorCode());
                return false;
            }
        }
        return true;
    }

    private boolean insertPlayersOfMatch() {

        currentMeetingId = addMeeting.getCurrentMeetingId();

        if (playersTable.isEditing()) {
            playersTable.getCellEditor().stopCellEditing();
        }

        if (tableModel.getRowCount() < 1) {
            JOptionPane.showMessageDialog(addMatchPanel, "Dodaj przynajmniej jednego gracza");
            return false;
        }


        if (currentMeetingId == 0) {
            JOptionPane.showMessageDialog(addMatchPanel, "Nie dodano jeszcze ani jednego spotkania");
            return false;
        }

        String sql = "INSERT INTO Gr_w_rozgr_spot (id_gracza, id_spotkania, id_rozgrywki, pozycja) " +
                "VALUES (?, " + currentMeetingId + ", " + currentMatchId + ", ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                pstmt.setInt(1, selectedPlayers.get(i).id_gracza);
                try {
                    System.out.println(tableModel.getValueAt(i, 2).toString());
                    pstmt.setInt(2, Integer.parseInt(tableModel.getValueAt(i, 2).toString()));
                } catch (NumberFormatException | NullPointerException e) {
                    pstmt.setNull(2, Types.INTEGER);
                }

                try (ResultSet rs = pstmt.executeQuery()) {
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(null, ErrorSQL(ex.getErrorCode()) + " " + "Błąd wykonania polecenia", "Informacja", JOptionPane.ERROR_MESSAGE);
                    System.out.println("Błąd wykonania polecenia x: " + ex.getMessage());
                    return false;
                }
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ErrorSQL(ex.getErrorCode()) + " " + "Błąd wykonania polecenia", "Informacja", JOptionPane.ERROR_MESSAGE);
            System.out.println("Błąd wykonania polecenia1: " + ex.getMessage());
            return false;
        }
        return true;
    }

    private void setupComboBoxListener() {
        graComboBox.addItemListener(event -> {
            if (event.getStateChange() == ItemEvent.SELECTED) {
                String title = event.getItem().toString();
                ArrayList<String> expansions = new ArrayList<>();
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(
                             "SELECT tytul FROM rozszerzenia WHERE id_gry = (SELECT MIN(id_gry) FROM gry WHERE tytul LIKE '" + title + "')")) {
                    while (rs.next()) {
                        expansions.add(rs.getString(1));
                    }

                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(null, ErrorSQL(ex.getErrorCode()) + " " + "Błąd pobrania listy rozszerzeń", "Informacja", JOptionPane.ERROR_MESSAGE);
                    System.out.println("Błąd wykonania polecenia: " + ex.getMessage());
                }
                DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(expansions.toArray(new String[expansions.size()]));
                for (JComboBox cm : comboBoxes) {
                    cm.setModel(model);
                }
            }
        });
    }

    private void setupComboBox() {
        graComboBox.removeAllItems();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT tytul FROM gry ORDER BY tytul")) {
            while (rs.next()) {
                graComboBox.addItem(rs.getString(1));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ErrorSQL(ex.getErrorCode()) + " " + "Błąd pobrania listy gier", "Informacja", JOptionPane.ERROR_MESSAGE);
            System.out.println("Błąd wykonania polecenia: " + ex.getMessage());
        }
    }


    private void setupList() {

        players = new ArrayList<>();

        listModel = new DefaultListModel<String>();

        graczeList.setModel(listModel);
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT id_gracza, imie, nazwisko FROM gracze ORDER BY nazwisko")) {
            while (rs.next()) {
                listModel.addElement(rs.getString(3) + ", " + rs.getString(2));
                players.add(new PlayerOfMatch(rs.getInt(1), rs.getString(2), rs.getString(3)));
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ErrorSQL(ex.getErrorCode()) + " " + "Błąd pobrania listy graczy", "Informacja", JOptionPane.ERROR_MESSAGE);
            System.out.println("Błąd wykonania polecenia: " + ex.getMessage());
        }

        graczeList.setSelectionModel(new DefaultListSelectionModel() {
            @Override
            public void setSelectionInterval(int index0, int index1) {
                if (super.isSelectedIndex(index0)) {
                    super.removeSelectionInterval(index0, index1);
                } else {
                    super.addSelectionInterval(index0, index1);
                }
            }
        });
    }
}
