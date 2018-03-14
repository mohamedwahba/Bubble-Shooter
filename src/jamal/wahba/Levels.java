package jamal.wahba;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Levels extends Activity implements OnClickListener {
	private MediaPlayer mp;
	private String title;
	private String msg;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// set our MainGamePanel as the View
		setContentView(R.layout.main);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		Button level1 = (Button) findViewById(R.id.button1);
		level1.setOnClickListener(this);

		Button level2 = (Button) findViewById(R.id.button2);
		level2.setOnClickListener(this);

		Button level3 = (Button) findViewById(R.id.button3);
		level3.setOnClickListener(this);

		Button level4 = (Button) findViewById(R.id.button4);
		level4.setOnClickListener(this);

		Button help = (Button) findViewById(R.id.button5);
		help.setOnClickListener(this);

		title = "Help";
		msg = "Levels details"
				+ "\n"
				+ "The difference between Levels is number of wrong hitted balls"
				+ "\n"
				+ "Level 1: 8 Balls"
				+ "\n "
				+ "Level 2: 6 Balls "
				+ "\n "
				+ "Level 3: 4 Balls "
				+ "\n "
				+ "Level 4: 3 Balls "
				+ "\n "
				+ "Hint 1: When you press back button during the game you will lose your level and return back to main menu"
				+ "\n" + "Hint 2: Press menu button to exit the game" + "\n"
				+ "Hint 3: Back Button is disabled when you are in main menu";
		mp = MediaPlayer.create(Levels.this, R.raw.sleepaway);
		mp.start();
		mp.setLooping(true);
	}

	public void onClick(View v) {
		int level = 0;
		switch (v.getId()) {
		case R.id.button1:
			level = 1;
			break;
		case R.id.button2:
			level = 2;
			break;
		case R.id.button3:
			level = 3;
			break;
		case R.id.button4:
			level = 4;
			break;
		case R.id.button5:
			help();
			return;
		default:
			break;
		}
		Intent intent = new Intent(this, BubbleShooterActivity.class);
		intent.putExtra("Level", level);
		this.startActivity(intent);
		return;
	}

	private void help() {
		Dialog unsavedChangesDialog;
		unsavedChangesDialog = new AlertDialog.Builder(this).setTitle(title)
				.setMessage(msg).create();
		unsavedChangesDialog.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu arg0) {
		// TODO Auto-generated method stub
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, arg0);
		return super.onCreateOptionsMenu(arg0);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem arg0) {
		exit();
		return super.onOptionsItemSelected(arg0);
	}

	private void exit() {
		mp.pause();
		this.onDestroy();
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		return;
	}
}