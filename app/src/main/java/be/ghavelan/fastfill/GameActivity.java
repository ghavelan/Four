package be.ghavelan.fastfill;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * <p>
 * Main game.
 * </p>
 */

public class GameActivity extends Activity implements View.OnTouchListener {

    //Minimum number of lines
    public static final int MIN_LINES = 8;
    //Maximum number of initially coloured shapes
    public static final int MAX_COLOURED = 3;
    //Initial number of grids, lives and score
    private static final int GRID = 0;
    private static final int LIFE = 3;
    private static final int SCORE = 0;
    //Initial time
    private static final long INITIAL_TIME = 60000;
    // Game view
    private GameView gv;
    // Buttons used to fill shapes
    private Button redButton;
    private Button blueButton;
    private Button yellowButton;
    private Button whiteButton;
    //Button used to pause the game
    private ImageButton pauseButton;
    // Score, time, number of grid and trials
    private CustomTextView scoreDisplay;
    private CustomTextView timerDisplay;
    private CustomTextView gridNumberDisplay;
    private CustomTextView lifeDisplay;
    // Count down timer
    private MyTimer countDown = null;
    // Initial time to draw the grid
    private long initialTime;
    // Current time
    private long currentTime;
    // To store buttons state
    private boolean isRedPressed = false;
    private boolean isBluePressed = false;
    private boolean isYellowPressed = false;
    private boolean isWhitePressed = false;
    //States of the game
    private boolean isGameOver = false;
    private boolean isGameSuccess = false;
    private boolean isPaused = false;
    //Best score
    private int best_score;
    //To store actual number of grids, lives and score
    private int grid_number;
    private int life_number;
    private int score_result;
    private int score_save;
    //Dialog
    private MyDialog dialog = null;
    //Number of lines
    private int number_of_lines;
    //Play sound effects
    private SoundPool soundPool;
    // Sounds ID
    private int ID_SUCCESS;
    private int ID_FAIL;
    private int ID_BEEP;
    private int ID_TIME;
    //True when sounds are first loaded
    private boolean isLoaded = false;
    //Used to start the countdown timer when a button is first touched
    private boolean isCountdownStarted = false;
    //Settings
    private Settings settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        //Full screen mode
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //Allows volume keys to set game volume
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        //layout to display
        setContentView(R.layout.game_screen);
        //Gets the game view (the grid)
        gv = (GameView) findViewById(R.id.perimeter);
        //To link the grid view (gv) with the game state (game over or not)
        gv.setGame(this);
        //Initialize the time, score, grid number and lives
        initialTime = INITIAL_TIME;
        grid_number = GRID;
        score_result = SCORE;
        life_number = LIFE;
        currentTime = initialTime;
        //Buttons used to select colors
        redButton = (Button) findViewById(R.id.button_red_normal);
        blueButton = (Button) findViewById(R.id.button_blue_normal);
        yellowButton = (Button) findViewById(R.id.button_yellow_normal);
        whiteButton = (Button) findViewById(R.id.button_white_normal);
        //Button pause/play
        pauseButton = (ImageButton) findViewById(R.id.play_pause);
        pauseButton.setBackgroundResource(R.drawable.ic_pause);
        //Score, time, number of grids and lives
        scoreDisplay = (CustomTextView) findViewById(R.id.score_result);
        scoreDisplay.setTextColor(getResources().getColor(R.color.red));
        scoreDisplay.setText(String.valueOf(score_result));
        timerDisplay = (CustomTextView) findViewById(R.id.time_result);
        timerDisplay.setText(String.valueOf(Math.round(initialTime * 0.001f)));
        gridNumberDisplay = (CustomTextView) findViewById(R.id.grid_result);
        gridNumberDisplay.setTextColor(getResources().getColor(R.color.blue));
        gridNumberDisplay.setText(String.valueOf(grid_number));
        lifeDisplay = (CustomTextView) findViewById(R.id.live_result);
        lifeDisplay.setTextColor(getResources().getColor(R.color.yellow));
        lifeDisplay.setText(String.valueOf(life_number));
        //Dialog box
        dialog = new MyDialog(this, null, null);
        //Sounds
        soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                isLoaded = true;
            }
        });
        ID_SUCCESS = soundPool.load(this, R.raw.success, 1);
        ID_FAIL = soundPool.load(this, R.raw.fail, 1);
        ID_BEEP = soundPool.load(this, R.raw.beep_timer, 1);
        ID_TIME = soundPool.load(this, R.raw.time_up, 1);
        //Listeners
        redButton.setOnTouchListener(this);
        blueButton.setOnTouchListener(this);
        yellowButton.setOnTouchListener(this);
        whiteButton.setOnTouchListener(this);
        pauseButton.setOnTouchListener(this);
        //Settings
        settings = new Settings(this);
        best_score = settings.getScore();

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopGame();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Resume the state of the buttons
        redButton.setPressed(isRedPressed);
        blueButton.setPressed(isBluePressed);
        yellowButton.setPressed(isYellowPressed);
        whiteButton.setPressed(isWhitePressed);

        if (!isOnPause() && isCountDownStarted()) {
            restartGame();
            countDown.start();
        }

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dialog.cancel();
        soundPool.release();
        soundPool = null;
        GameActivity.this.finish();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        GameActivity.this.finish();
        overridePendingTransition(R.anim.enter, R.anim.leave);

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:

                if (v.getId() == R.id.button_red_normal) {
                    manageButtons(true, false, false, false);
                } else if (v.getId() == R.id.button_blue_normal) {
                    manageButtons(false, true, false, false);
                } else if (v.getId() == R.id.button_yellow_normal) {
                    manageButtons(false, false, true, false);
                } else if (v.getId() == R.id.button_white_normal) {
                    manageButtons(false, false, false, true);
                } else if (v.getId() == R.id.play_pause) {

                    if (isCountDownStarted()) {

                        isPaused = !isPaused;
                    }

                }
                break;

            case MotionEvent.ACTION_MOVE:
                break;

            case MotionEvent.ACTION_UP:

                if (v.getId() == R.id.button_red_normal) {
                    //passing the color to the game view object
                    gv.setColor(getResources().getColor(R.color.red));
                } else if (v.getId() == R.id.button_blue_normal) {
                    //passing the color to the game view object
                    gv.setColor(getResources().getColor(R.color.blue));
                } else if (v.getId() == R.id.button_yellow_normal) {
                    //passing the color to the game view object
                    gv.setColor(getResources().getColor(R.color.yellow));
                } else if (v.getId() == R.id.button_white_normal) {
                    //passing the color to the game view object
                    gv.setColor(getResources().getColor(R.color.white));
                } else if (v.getId() == R.id.play_pause) {

                    if (isCountDownStarted()) {

                        if (isOnPause()) {
                            gv.hide();
                            stopGame();
                            pauseButton.setBackgroundResource(R.drawable.ic_play);
                        } else {
                            gv.show();
                            restartGame();
                            countDown.start();
                            pauseButton.setBackgroundResource(R.drawable.ic_pause);
                        }
                    }

                }
                break;
        }

        return true;

    }

    /**
     * Manages button's states
     *
     * @param redPressed    State of the red button
     * @param bluePressed   State of the blue button
     * @param yellowPressed State of the yellow button
     * @param whitePressed  State of the white button
     */
    private void manageButtons(boolean redPressed, boolean bluePressed, boolean yellowPressed,
                               boolean whitePressed) {

        redButton.setPressed(redPressed);
        blueButton.setPressed(bluePressed);
        yellowButton.setPressed(yellowPressed);
        whiteButton.setPressed(whitePressed);
        //saving states of the buttons
        this.isRedPressed = redPressed;
        this.isBluePressed = bluePressed;
        this.isYellowPressed = yellowPressed;
        this.isWhitePressed = whitePressed;
        //Start the countdown timer once a button is pressed
        if (!isCountDownStarted()) {
            isCountdownStarted = true;
            restartGame();
            countDown.start();
        }

    }

    /**
     * Called when the game is over
     */
    public void gameOver() {
        isGameOver = true;
        isGameSuccess = false;
        final long HOLDING_TIME = 700;
        if (isLoaded) {
            soundPool.play(ID_FAIL, 1, 1, 1, 0, 1f);
        }
        gv.getChosenColors().clear();
        stopGame();
        if (life_number > 1) {
            //a life less
            life_number--;
            score_result = score_save;
            this.dialog.reset();
            this.dialog.setTitle(getResources().getString(R.string.game_mistake));
            this.dialog.setMessage(getResources().getString(R.string.game_over_touch) + "\n\n" +
                    getResources().getString(R.string.try_again));

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    dialog.show();
                }
            }, HOLDING_TIME);

        } else {
            life_number--;
            lifeDisplay.setText(String.valueOf(life_number));
            score_save = 0;
            this.dialog.reset();
            this.dialog.setTitle(getResources().getString(R.string.game_over));
            this.dialog.setMessage(getResources().getString(R.string.new_game));
            this.dialog.startANewGame();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    dialog.show();
                }
            }, HOLDING_TIME);
        }

    }

    /**
     * Called when success
     */
    public void gameSuccess() {
        isGameOver = false;
        isGameSuccess = true;
        final int INCREASE = 2;
        final long BONUS = 30000;
        if (isLoaded) {
            soundPool.play(ID_SUCCESS, 1, 1, 1, 0, 1f);
        }
        stopGame();
        //Saving the score
        score_save = score_result;
        //Increments number of grids
        gridNumberDisplay.setText(String.valueOf(grid_number));
        //Number of lines increment by 1 every #INCREASE grids
        number_of_lines = MIN_LINES + grid_number / INCREASE;
        //Number of (eventually additional) colors used to paint the entire grid
        int number_of_colors = gv.getChosenColors().size();
        //Number of squares
        int number_of_squares = gv.getGrid().getShapes().size();
        //Time bonus
        int bonus = Math.round(1000 * BONUS *
                number_of_squares / (number_of_colors * (initialTime - currentTime)));
        //New time
        currentTime += bonus;
        //New total time
        initialTime = currentTime;
        //Dialog (new game)
        this.dialog.reset();
        this.dialog.setTitle(getResources().getString(R.string.game_succeed));
        this.dialog.setMessage(getResources().getString(R.string.time_bonus, bonus / 1000) + "\n\n" +
                getResources().getString(R.string.game_continue));
        this.dialog.continueGame();
        this.dialog.show();
    }

    /**
     * To increase the grid number
     */
    public void increaseGridNumber() {
        ++grid_number;
    }

    /**
     * Updates the score.
     *
     * @param bonus Bonus to add
     */
    public void updateScore(int bonus) {
        score_result += bonus;
        scoreDisplay.setText(String.valueOf(score_result));
        if (score_result > best_score) {
            settings.save(score_result, grid_number, (DateFormat.format("dd/MM/yyyy", new java.util.Date()).toString()));
        }
    }

    /**
     * Returns true if the game is over
     *
     * @return Boolean
     */
    public boolean isOver() {
        return isGameOver;
    }

    /**
     * Returns true if the game is paused
     *
     * @return Boolean
     */
    public boolean isOnPause() {
        return isPaused;
    }

    /**
     * Returns true if the grid is successfully filled
     *
     * @return Boolean
     */
    public boolean isOk() {
        return isGameSuccess;
    }

    /**
     * Returns true when a button is first pressed
     *
     * @return Boolean
     */
    public boolean isCountDownStarted() {
        return isCountdownStarted;
    }

    /**
     * Stops the game thread and the timer
     */
    private void stopGame() {
        //Pause the running thread
        if (gv.getGameThread() != null) {
            //in the pause method, the game thread is set to null
            gv.pause();
        }
        // stop the countdown timer
        if (countDown != null) {
            countDown.cancel();
            countDown = null;
        }
    }

    /**
     * Restarts the game thread and resets the timer
     */
    private void restartGame() {
        //If the game is pending
        if (!isGameOver && !isGameSuccess) {
            //Resume the game thread
            gv.resume();
            //Restart a new countdown timer (with the current time properly saved)
            if (countDown == null) {
                final long TICK_INTERVAL = 100;
                countDown = new MyTimer(currentTime, TICK_INTERVAL);
            }
        }
    }

    /**
     * Internal class to manage a dialog box.
     * The dialog is used either to start the same grid again or a completely new one.
     */

    class MyDialog extends Dialog implements View.OnTouchListener {

        private Button ok;
        private Button cancel;
        private Button save;
        private String title;
        private String text_message;
        //newGame is set to true only when a new grid is asked (game continues)
        private boolean continueGame = false;
        // restart is set to true when the game is restarting from the beginning (game over)
        private boolean restartGame = false;
        private CustomTextView text;
        private CustomTextView message;

        public MyDialog(Context context, String title, String message) {
            super(context);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            //Animation
            getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            //Not cancelable when back key is pressed
            setCancelable(false);
            //Not cancelable when touched outside the window's bounds
            setCanceledOnTouchOutside(false);
            this.title = title;
            this.text_message = message;
            setContentView(R.layout.dialog_screen);
            initDialog();
        }

        private void initDialog() {
            this.ok = (Button) findViewById(R.id.dialog_ok);
            this.ok.setText(getResources().getString(R.string.ok));
            this.cancel = (Button) findViewById(R.id.dialog_cancel);
            this.cancel.setText(getResources().getString(R.string.not_ok));
            this.save = (Button) findViewById(R.id.save);
            this.text = (CustomTextView) findViewById(R.id.dialog_title);
            this.text.setText(title);
            this.message = (CustomTextView) findViewById(R.id.text_message);
            this.message.setText(text_message);
            //Listeners
            this.ok.setOnTouchListener(this);
            this.cancel.setOnTouchListener(this);
            this.save.setOnTouchListener(this);
        }

        public void setTitle(String title) {
            this.text.setText(title);

        }

        /**
         * Setters
         *
         * @param message Message to display
         */
        public void setMessage(String message) {
            this.message.setText(message);
        }

        /**
         * To start a new game (new grid)
         */
        public void continueGame() {
            this.continueGame = true;
            this.restartGame = false;
        }

        /**
         * To reset the dialog's state
         */
        public void reset() {
            this.continueGame = false;
            this.restartGame = false;
            this.ok.setPressed(false);
            this.cancel.setPressed(false);
        }

        /**
         * To restart the game from the beginning
         */
        public void startANewGame() {
            this.continueGame = true;
            this.restartGame = true;
            isCountdownStarted = false;
            life_number = LIFE;
            number_of_lines = MIN_LINES;
            score_result = SCORE;
            grid_number = GRID;
            initialTime = INITIAL_TIME;
            currentTime = initialTime;
        }

        public boolean isGameRestarted() {
            return restartGame;
        }

        /**
         * To store image in internal storage.
         *
         * @param v View to be stored.
         */
        private void saveImage(View v) {

            v.buildDrawingCache();
            Bitmap bitmap = v.getDrawingCache();
            String date = (DateFormat.format("ddMMyyyyhhmmss", new java.util.Date()).toString());
            //Create a new directory to store the image
            String path;
            File output;
            LayoutInflater inflater;
            View layout;
            Toast toast;
            if (android.os.Environment.getExternalStorageState().equals(
                    android.os.Environment.MEDIA_MOUNTED)) {
                path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() +
                        File.separator + getResources().getString(R.string.app_name);
                output = new File(path);
                output.mkdirs();
                //Create the file name
                File newFile = new File(path + File.separator + "image" + date + ".png");

                FileOutputStream out;

                try {
                    out = new FileOutputStream(newFile);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.close();

                    inflater = getLayoutInflater();
                    layout = inflater.inflate(R.layout.toast_screen,
                            (ViewGroup) findViewById(R.id.toast_layout));

                    toast = new Toast(getApplicationContext());
                    toast.setGravity(Gravity.TOP, 0, 0);
                    toast.setDuration(Toast.LENGTH_SHORT);
                    toast.setView(layout);
                    toast.show();

                } catch (IOException io) {
                    io.printStackTrace();
                }
            } else {
                inflater = getLayoutInflater();
                layout = inflater.inflate(R.layout.toast_screen,
                        (ViewGroup) findViewById(R.id.toast_layout));
                CustomTextView tv = (CustomTextView) layout.findViewById(R.id.toast_text);
                tv.setText(getResources().getString(R.string.insert_sd));
                toast = new Toast(getApplicationContext());
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.setView(layout);
                toast.show();
            }

        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:
                    if (v.getId() == R.id.save) {
                        save.setBackgroundResource(R.drawable.ic_download_touched);
                    }
                    break;

                case MotionEvent.ACTION_MOVE:
                    break;

                case MotionEvent.ACTION_UP:

                    if (v.getId() == R.id.dialog_ok) {

                        ok.setPressed(true);
                        cancel.setPressed(false);
                        //Reset game state
                        isGameSuccess = false;
                        isGameOver = false;
                        //at least : clear the shapes (new game and restart same game)
                        gv.clearColoredShapes();
                        //Restart the same grid.
                        if (!continueGame) {
                            //a life less
                            lifeDisplay.setText(String.valueOf(life_number));
                            currentTime = initialTime;
                            scoreDisplay.setText(String.valueOf(score_result));
                            //To get saved shapes
                            Map<Rect, Integer> savedColoredShapes = gv.getInitiallyColoredShapes();

                            if (!savedColoredShapes.isEmpty()) {
                                for (Map.Entry<Rect, Integer> entry : savedColoredShapes.entrySet()) {
                                    //Puts shapes
                                    gv.getGrid().putShape(entry.getKey(), entry.getValue());
                                    //Updates surface painted
                                    gv.updateSurfacePainted(entry.getKey().width() * entry.getKey().height());
                                }
                            }

                        }
                        //Start another grid (success)
                        else {

                            //Clear saved shapes
                            gv.getInitiallyColoredShapes().clear();
                            //Clear Set of colors already painted
                            gv.getChosenColors().clear();
                            //Clear lines
                            gv.getGrid().getLines().clear();
                            gv.getGrid().computeMondrian(number_of_lines);
                            gv.randomlyColorShapes(0, MAX_COLOURED, MondrianGrid.RANDOM);

                            if (isGameRestarted()) {
                                scoreDisplay.setText(String.valueOf(score_result));
                                gridNumberDisplay.setText(String.valueOf(grid_number));
                                lifeDisplay.setText(String.valueOf(life_number));
                            }

                        }
                        dismiss();
                        restartGame();
                        if (isCountDownStarted()) {
                            countDown.start();
                        }

                    } else if (v.getId() == R.id.dialog_cancel) {

                        cancel.setPressed(true);
                        ok.setPressed(false);
                        dismiss();
                        stopGame();
                        GameActivity.this.finish();
                        overridePendingTransition(R.anim.enter, R.anim.leave);

                    } else if (v.getId() == R.id.save) {
                        save.setBackgroundResource(R.drawable.ic_download_touched);
                        saveImage(gv);
                        save.setBackgroundResource(R.drawable.ic_download);
                    }

                    break;

            }
            return true;
        }
    }

    /**
     * Internal class to manage a countdown timer
     */
    class MyTimer extends CountDownTimer {

        int red = getResources().getColor(R.color.red);
        int white = getResources().getColor(R.color.white);
        private boolean playSong = true;

        public MyTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
            currentTime = millisInFuture;
            timerDisplay.setText(String.valueOf(Math.round(millisInFuture * 0.001f)));
        }

        @Override
        public void onTick(long millisUntilFinished) {
            currentTime = millisUntilFinished;
            timerDisplay.setText(String.valueOf(Math.round(millisUntilFinished * 0.001f)));
            final long TIME_THRESHOLD = 10500;
            if (currentTime <= TIME_THRESHOLD) {
                timerDisplay.setTextColor(red);
                if (playSong && isLoaded) {
                    soundPool.play(ID_BEEP, 1, 1, 1, 0, 1f);
                    playSong = !playSong;
                }
            } else {
                timerDisplay.setTextColor(white);
            }
        }

        @Override
        public void onFinish() {
            isGameOver = true;
            isGameSuccess = false;
            if (isLoaded) {
                soundPool.play(ID_TIME, 1, 1, 1, 0, 1f);
            }
            stopGame();
            dialog.reset();
            dialog.setTitle(getResources().getString(R.string.game_over));
            if (life_number > 1) {
                life_number--;
                dialog.setMessage(getResources().getString(R.string.game_over_time) + "\n" +
                        "\n" + getResources().getString(R.string.try_again));
                dialog.show();
            } else {
                life_number--;
                lifeDisplay.setText(String.valueOf(life_number));
                dialog.setMessage(getResources().getString(R.string.new_game));
                dialog.startANewGame();
                dialog.show();
                timerDisplay.setTextColor(white);
            }

        }

    }

}
