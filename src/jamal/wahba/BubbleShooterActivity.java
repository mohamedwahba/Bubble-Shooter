package jamal.wahba;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class BubbleShooterActivity extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// requesting to turn the title OFF
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// making it full screen
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		// set our MainGamePanel as the View
		setContentView(new MainGamePanel(this, getIntent().getExtras().getInt(
				"Level")));
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		android.os.Process.killProcess(android.os.Process.myPid());
	}

}