package put.boardgames.app;

public class PlayerOfMatch {
    int id_gracza;
    String imie;
    String nazwisko;

    PlayerOfMatch(int id_gracza, String imie, String nazwisko) {
        this.id_gracza = id_gracza;
        this.imie = imie;
        this.nazwisko = nazwisko;
    }


    public Object[] getAsRow() {
        Object[] row = {this.imie, this.nazwisko, null};
        return row;
    }
}

