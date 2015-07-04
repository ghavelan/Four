package be.ghavelan.fastfill;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.view.Window;
import android.view.WindowManager;

/**
 * Instructions screen.
 */
public class ResultsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.results_screen);

        CustomTextView resultsView = (CustomTextView) findViewById(R.id.best_results);
        CustomTextView gridsView = (CustomTextView) findViewById(R.id.best_grids);
        CustomTextView dateView = (CustomTextView) findViewById(R.id.best_date);
        Settings settings = new Settings(this);
        int score = settings.getScore();
        int grids = settings.getGrids();
        String date = settings.getDate();
        resultsView.setText(Html.fromHtml("<u>" + getResources().getString(R.string.best_results, score) + "</u>"));
        gridsView.setText(Html.fromHtml("<u>" + getResources().getString(R.string.best_grids, grids) + "</u>"));
        dateView.setText(Html.fromHtml("<u>" + getResources().getString(R.string.best_date, date) + "</u>"));

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ResultsActivity.this.finish();
        overridePendingTransition(R.anim.enter, R.anim.leave);

    }

}
