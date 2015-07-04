package be.ghavelan.fastfill;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Shared preferences.
 */

public class Settings {

    public static final String RESULTS = "GAME_RESULTS";
    public static final String SCORE_KEY = "SCORE_RESULTS";
    public static final String GRIDS_KEY = "GRIDS_RESULTS";
    public static final String DATE_KEY = "DATE_RESULTS";

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    public Settings(Context context) {
        this.preferences = context.getSharedPreferences(RESULTS, Context.MODE_PRIVATE);
        this.editor = this.preferences.edit();
    }

    public void save(int score, int grids, String date) {
        editor.putInt(SCORE_KEY, score);
        editor.putInt(GRIDS_KEY, grids);
        editor.putString(DATE_KEY, date);
        editor.commit();
    }

    public int getScore() {

        return preferences.getInt(SCORE_KEY, 0);
    }

    public int getGrids() {

        return preferences.getInt(GRIDS_KEY, 0);
    }

    public String getDate() {

        return preferences.getString(DATE_KEY, "");
    }
}
