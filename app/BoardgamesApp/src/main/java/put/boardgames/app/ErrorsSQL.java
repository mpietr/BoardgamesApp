package put.boardgames.app;

public class ErrorsSQL {
    static String ErrorSQL(int code) {
        if (code == 20001) {
            return "Data z przyszłości";
        }
        if (code == 2290) {
            return "Niespójność wprowadzonych danych";
        }
        return String.valueOf(code);
    }
}
