package be.ghavelan.fastfill;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;


/**
 * Menu screen.
 */

public class TitleScreenActivity extends Activity implements View.OnTouchListener {

    private CustomTextView start;
    private CustomTextView instructions;
    private CustomTextView score;
    private int color_up;
    private int color_down;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.title_screen);
        //Different colors (up and down states)
        color_up = getResources().getColor(R.color.text_color);
        color_down = getResources().getColor(R.color.red);
        //Start game
        start = (CustomTextView) findViewById(R.id.start);
        start.setTextColor(color_up);
        //Instructions
        instructions = (CustomTextView) findViewById(R.id.instructions);
        instructions.setTextColor(color_up);
        //Best score
        score = (CustomTextView) findViewById(R.id.best);
        score.setTextColor(color_up);
        //Listener
        start.setOnTouchListener(this);
        instructions.setOnTouchListener(this);
        score.setOnTouchListener(this);

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:

                if (v.getId() == R.id.start) {

                    start.setTextColor(color_down);
                } else if (v.getId() == R.id.instructions) {

                    instructions.setTextColor(color_down);
                } else if (v.getId() == R.id.best) {

                    score.setTextColor(color_down);
                }

                break;

            case MotionEvent.ACTION_UP:

                if (v.getId() == R.id.start) {

                    new Handler().post(new Thread() {
                        @Override
                        public void run() {
                            startGame();
                        }
                    });
                    start.setTextColor(color_up);

                } else if (v.getId() == R.id.instructions) {

                    new Handler().post(new Thread() {
                        @Override
                        public void run() {
                            startInstructions();
                        }
                    });
                    instructions.setTextColor(color_up);

                } else if (v.getId() == R.id.best) {

                    new Handler().post(new Thread() {
                        @Override
                        public void run() {
                            startBestScore();
                        }
                    });
                    score.setTextColor(color_up);

                }

                break;

        }

        return true;
    }

    private void startGame() {

        Intent game = new Intent(TitleScreenActivity.this, GameActivity.class);
        startActivity(game);
        overridePendingTransition(R.anim.enter, R.anim.leave);

    }

    private void startInstructions() {

        Intent instructions = new Intent(TitleScreenActivity.this, InstructionsActivity.class);
        startActivity(instructions);
        overridePendingTransition(R.anim.enter, R.anim.leave);

    }

    private void startBestScore() {

        Intent results = new Intent(TitleScreenActivity.this, ResultsActivity.class);
        startActivity(results);
        overridePendingTransition(R.anim.enter, R.anim.leave);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        TitleScreenActivity.this.finish();
        overridePendingTransition(R.anim.enter, R.anim.leave);

    }

}
