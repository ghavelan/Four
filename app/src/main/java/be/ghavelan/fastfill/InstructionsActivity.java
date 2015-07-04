package be.ghavelan.fastfill;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

/**
 * Instructions screen.
 */
public class InstructionsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.instructions_screen);

        CustomTextView text = (CustomTextView) findViewById(R.id.instructions_text);
        text.setText(getResources().getString(R.string.instructions_text));

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        InstructionsActivity.this.finish();
        overridePendingTransition(R.anim.enter, R.anim.leave);

    }

}
