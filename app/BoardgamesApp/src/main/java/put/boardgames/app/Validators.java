package put.boardgames.app;

public class Validators {

    public static boolean isMatchDurationValid(int duration) {
        return duration > 0;
    }

    public static boolean isFoodNameValid(String name) {
        return name.length() > 0 && name.matches("[a-zA-ZąćęłńóśżźĄĘÓŁŃŹŻĆŚ ]+");
    }

    public static boolean isNameValid(String name) {
        if (name.length() == 0) {
            return false;
        }
        return Character.isUpperCase(name.charAt(0));
    }

    public static boolean isAdressNameValid(String name) {
        if (name.length() == 0) {
            return false;
        }
        return true;
    }

    public static boolean isDateValid(int day, int month, int year) {
        if (day < 1 || month < 1 || year < 1900) {
            return false;
        }
        if (month == 4 && day > 30) {
            return false;
        }
        if (month == 6 && day > 30) {
            return false;
        }
        if (month == 9 && day > 30) {
            return false;
        }
        if (month == 11 && day > 30) {
            return false;
        }
        if (year % 4 == 0 && month == 2 && day > 29) {
            return false;
        }
        return month != 2 || day <= 28;

    }

    public static boolean isGameTitleValid(String title) {

        return title.length() >= 1;
    }

    public static boolean isMinPlayersMaxPlayersValid(int minPlayers, int maxPlayers) {
        if (maxPlayers == 0) {
            return true;
        }
        return minPlayers >= 1 && maxPlayers >= 1 && minPlayers <= maxPlayers;
    }
}
