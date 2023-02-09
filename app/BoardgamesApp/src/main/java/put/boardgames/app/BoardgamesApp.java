package put.boardgames.app;

import javax.swing.*;
import java.awt.*;

public class BoardgamesApp extends JFrame {

    public Container contentPane;

    HomeScreen homeScreen;
    AddGame addGame;
    AddExpansion addExpansion;

    AddMeeting addMeeting;

    AddPlayer addPlayer;

    AddMatch addMatch;

    UpdateData updateData;

    PlayerProfile playerProfile;

    MeetingData meetingData;

    GameData gameData;

    Search search;

    BoardgamesApp() {
        setupFrame();
        setupListeners();
    }

    private void setupFrame() {
        setTitle("BoardgamesApp");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        contentPane = getContentPane();
        setMinimumSize(new Dimension(800, 600));

        homeScreen = new HomeScreen();
        addGame = new AddGame();
        addPlayer = new AddPlayer();
        addExpansion = new AddExpansion();
        addMeeting = new AddMeeting();
        addMatch = new AddMatch(addMeeting);
        updateData = new UpdateData();
        playerProfile = new PlayerProfile();
        meetingData = new MeetingData();
        gameData = new GameData();
        search = new Search();

        contentPane.add(homeScreen.getPanel());

        pack();
        setVisible(true);
    }

    private void setupListeners() {
        homeScreen.dodajGreButton.addActionListener(e -> {
            addGame.refreshView();
            switchPanel(addGame.getPanel());
            setTitle("Dodaj grę");
        });

        homeScreen.dodajGraczaButton.addActionListener(e ->
        {
            addPlayer.refreshView();
            switchPanel(addPlayer.getPanel());
            setTitle("Dodaj gracza");
        });

        homeScreen.dodajRozszerzenieButton.addActionListener(e ->
        {
            addExpansion.refreshView();
            switchPanel(addExpansion.getPanel());
            setTitle("Dodaj rozszerzenie");
        });

        homeScreen.dodajSpotkanieButton.addActionListener(e -> {
            addMeeting.refreshView();
            switchPanel(addMeeting.getPanel());
            setTitle("Dodaj spotkanie");
        });

        homeScreen.dodajRozgrywkeButton.addActionListener(e -> {
            addMatch.refreshView();
            switchPanel(addMatch.getPanel());
            setTitle("Dodaj rozgrywkę");
        });

        homeScreen.zarzadzajDanymiButton.addActionListener(e -> {
            updateData.refreshView();
            switchPanel(updateData.getPanel());
            setTitle("Zarządzaj danymi");
        });

        homeScreen.wyjdzButton.addActionListener( e -> {
            switchPanel(search.getPanel());
        });

        search.wrocButton.addActionListener(e -> {
            switchPanel(homeScreen.getPanel());
        });

        homeScreen.profilGraczaButton.addActionListener(e -> {
            switchPanel(playerProfile.getPanel());
            playerProfile.refreshView();
            setTitle("Profil gracza");
        });


        homeScreen.informacjeOSpotkaniuButton.addActionListener(e -> {
            meetingData.setupMeetingComboBox();
            switchPanel(meetingData.getPanel());
            setTitle("Informacje o spotkaniu");
        });

        homeScreen.informacjeOGrachButton.addActionListener(e -> {
            gameData.setupTable();
            gameData.setupLabels();
            switchPanel(gameData.getPanel());
            setTitle("Informacje o grach");
        });


        JPanel homePanel = homeScreen.getPanel();

        addGame.anulujButton.addActionListener(e ->
                {
                    switchPanel(homePanel);
                    addExpansion.setupList();
                    setTitle("BoardgamesApp");
                }
        );
        addPlayer.anulujButton.addActionListener(e -> {
            switchPanel(homePanel);
            setTitle("BoardgamesApp");
        });
        addExpansion.anulujButton.addActionListener(e -> {
            switchPanel(homePanel);
            setTitle("BoardgamesApp");
        });
        addMeeting.anulujButton.addActionListener(e ->
        {
            switchPanel(homePanel);
            setTitle("BoardgamesApp");
        });
        addMatch.anulujButton.addActionListener(e ->
        {
            switchPanel(homePanel);
            setTitle("BoardgamesApp");
        });
        updateData.anulujButton.addActionListener(e ->
        {
            switchPanel(homePanel);
            setTitle("BoardgamesApp");
        });
        playerProfile.wrocButton.addActionListener(e ->
        {
            switchPanel(homePanel);
            setTitle("BoardgamesApp");
        });
        meetingData.wrocButton.addActionListener(e ->
        {
            switchPanel(homePanel);
            setTitle("BoardgamesApp");
        });
        gameData.wrocButton.addActionListener(e ->
        {
            switchPanel(homePanel);
            setTitle("BoardgamesApp");
        });
    }



    public void switchPanel(JPanel newPanel) {
        contentPane.removeAll();
        contentPane.add(newPanel);
        pack();
        setVisible(true);
        SwingUtilities.updateComponentTreeUI(this);
    }

}
