package jamal.wahba;

import java.util.Arrays;
import java.util.Random;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MainGamePanel extends SurfaceView implements
		SurfaceHolder.Callback {

	private MainThread thread;
	private Ball[][] Grid;
	private float mSpeedX, mSpeedY, mX, mY;
	private Ball moving_ball;
	private Ball[] coming_balls;
	boolean finished;
	private Random r1;
	private boolean[][] V;
	private boolean is_lose, is_win;
	private int jumps, level;
	private Handler handler;
	final int ROWS = 13;
	final int COLS = 8;
	private int SCORE;
	private String Title;
	private String Msg;
	private Paint t;

	private MediaPlayer bomb;
	private MediaPlayer stick;

	public MainGamePanel(Context context, int level) {
		super(context);
		this.level = level;
		initialization();
	}

	private void initialization() {
		stick = MediaPlayer.create(getContext(), R.raw.stick);
		bomb = MediaPlayer.create(getContext(), R.raw.bomb);

		getHolder().addCallback(this);
		handler = new Handler();
		coming_balls = new Ball[3];
		create_level();
		r1 = new Random();
		t = new Paint();
		t.setColor(Color.WHITE);
		t.setTextSize(35);

		Grid = new Ball[ROWS][COLS];
		int y = 0;
		for (int i = 0; i < 4; i++) {
			int x = 0;
			if (i % 2 == 0)
				x = 0;
			else
				x = 56 / 2;
			for (int j = 0; j < COLS; j++) {
				int random = r1.nextInt(4);
				if (random == 0)
					Grid[i][j] = new Ball(BitmapFactory.decodeResource(
							getResources(), R.drawable.blue), x, y, Color.BLUE);
				else if (random == 1)
					Grid[i][j] = new Ball(BitmapFactory.decodeResource(
							getResources(), R.drawable.green), x, y,
							Color.GREEN);
				else if (random == 2)
					Grid[i][j] = new Ball(BitmapFactory.decodeResource(
							getResources(), R.drawable.red), x, y, Color.RED);
				else if (random == 3)
					Grid[i][j] = new Ball(BitmapFactory.decodeResource(
							getResources(), R.drawable.violet), x, y,
							Color.MAGENTA);
				x += 56;
			}

			y += 56;
		}
		for (int i = 4; i < ROWS; i++) {
			int x = 0;
			if (i % 2 == 0)
				x = 0;
			else
				x = 56 / 2;
			for (int j = 0; j < COLS; j++) {
				Grid[i][j] = new Ball(BitmapFactory.decodeResource(
						getResources(), R.drawable.black), x, y, Color.BLACK);
				x += 56;
			}
			y += 56;
		}

		// create the game loop thread
		thread = new MainThread(getHolder(), this);

		mSpeedX = 0;
		mSpeedY = 0;
		finished = true;
		mX = 200;
		mY = 700;
		SCORE = 0;

		V = new boolean[ROWS][COLS];

		initialize_ball();
		create_moving_ball();

		// make the GamePanel focusable so it can handle events
		setFocusable(true);
	}

	private void initialize_ball() {
		int coming_x = 0;
		int coming_y = 700;

		for (int i = 0; i < 3; i++) {
			if (coming_balls[i] == null) {
				boolean ok = false;
				for (int j = 0; j < COLS; j++)
					if (Grid[2][j] != null
							&& Grid[2][j].getColor() != Color.BLACK
							&& Grid[2][j].getColor() != Color.DKGRAY)
						ok = true;
				if (ok) {
					int random = r1.nextInt(4);
					if (random == 0)
						coming_balls[i] = new Ball(
								BitmapFactory.decodeResource(getResources(),
										R.drawable.blue), coming_x, coming_y,
								Color.BLUE);
					else if (random == 1)
						coming_balls[i] = new Ball(
								BitmapFactory.decodeResource(getResources(),
										R.drawable.green), coming_x, coming_y,
								Color.GREEN);
					else if (random == 2)
						coming_balls[i] = new Ball(
								BitmapFactory.decodeResource(getResources(),
										R.drawable.violet), coming_x, coming_y,
								Color.MAGENTA);
					else if (random == 3)
						coming_balls[i] = new Ball(
								BitmapFactory.decodeResource(getResources(),
										R.drawable.red), coming_x, coming_y,
								Color.RED);
				} else {
					for (int row = 0; row < 2; row++)
						for (int j = 0; j < COLS; j++)
							if (Grid[row][j] != null
									&& Grid[row][j].getColor() != Color.BLACK
									&& Grid[row][j].getColor() != Color.DKGRAY) {
								coming_balls[i] = new Ball(
										Grid[row][j].getBitmap(), coming_x,
										coming_y, Grid[row][j].getColor());
							}
				}
			}
			coming_x += 56;
		}
	}

	private void create_moving_ball() {

		mX = 200;
		mY = 700;
		Ball coming_ball = coming_balls[2];
		coming_ball.setX(mX);
		coming_ball.setY(mY);
		moving_ball = coming_ball;

		for (int i = 2; i > 0; i--) {
			coming_balls[i] = coming_balls[i - 1];
			coming_balls[i].setX(coming_balls[i].getX() + 56);
		}
		coming_balls[0] = null;
		initialize_ball();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			goToMain();
		}
		return super.onKeyDown(keyCode, event);
	}

	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub

	}

	public void surfaceCreated(SurfaceHolder holder) {
		// at this point the surface is created and
		// we can safely start the game loop
		thread.setRunning(true);
		thread.start();
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		// tell the thread to shut down and wait for it to finish
		// this is a clean shutdown
		boolean retry = true;
//		thread.setRunning(false);
		while (retry) {
			try {
				thread.join();
				retry = false;
			} catch (InterruptedException e) {
				// try again shutting down the thread
			}
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {

		// fills the canvas with black
		canvas.drawColor(Color.BLACK);

		if (is_win) {
			handler.post(new Runnable() {
				public void run() {
					win();
				}
			});
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		is_lose = false;
		for (int i = 0; i < ROWS; i++) {
			for (int i1 = 0; i1 < COLS; i1++) {
				if (i == 12 && Grid[i][i1].getColor() != Color.BLACK)
					is_lose = true;
				draw(canvas, Grid[i][i1]);
				if (Grid[i][i1].getColor() == Color.DKGRAY) {
					bomb.start();
					Grid[i][i1].setColor(Color.BLACK);
					Grid[i][i1].setmBitmap(BitmapFactory.decodeResource(
							getResources(), R.drawable.black));
				}

			}
		}

		draw(canvas, moving_ball);

		for (Ball coming : coming_balls)
			draw(canvas, coming);

		canvas.drawText("Score", 350, 730, t);
		canvas.drawText("" + SCORE,
				400 - (Integer.toString(SCORE).length() * 12), 760, t);

		if (is_lose) {
			handler.post(new Runnable() {
				public void run() {
					lose();
				}
			});
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (is_lose || is_win)
			surfaceDestroyed(getHolder());

	}

	private void lose() {
		Title = "You Lose!";
		Msg = "Ya Loser :P :P";
		createAlertDialog();
	}

	private void win() {
		Title = " You Win.";
		Msg = "ya Winner :) :) /n Your Score is " + SCORE;
		createAlertDialog();
	}

	private void createAlertDialog() {
		AlertDialog unsavedChangesDialog;
		unsavedChangesDialog = new AlertDialog.Builder(getContext())
				.setTitle(Title)
				.setMessage(Msg)
				.setNeutralButton("Back To Main Menu",
						new AlertDialog.OnClickListener() {
							public void onClick(DialogInterface arg0, int arg1) {
								goToMain();
							}

						}).create();

		unsavedChangesDialog.show();
	}

	private void goToMain() {
		// TODO Auto-generated method stub
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	public void draw(Canvas canvas, Ball ball) {
		canvas.drawBitmap(ball.getBitmap(), ball.getX(), ball.getY(), null);
	}

	@Override
	public boolean onTouchEvent(MotionEvent me) {
		if (!finished)
			return false;
		finished = false;
		float x = me.getX();
		float y = me.getY();
		mSpeedX = x - mX;
		mSpeedY = y - mY;
		return super.onTouchEvent(me);
	}

	public void update() {
		if (finished)
			return;
		mX += mSpeedX * 0.05;
		mY += mSpeedY * 0.05;
		checkBorders();
		moving_ball.setX(mX);
		moving_ball.setY(mY);
	}

	private void checkBorders() {
		float xx_ = mX;
		float yy_ = mY;
		Rect R2 = new Rect((int) xx_, (int) yy_, (int) (xx_ + moving_ball
				.getBitmap().getWidth()), (int) (yy_ + moving_ball.getBitmap()
				.getHeight()));
		for (int i = 0; i < ROWS; i++) {
			for (int j = 0; j < COLS; j++) {
				if (Grid[i][j].getColor() == Color.BLACK)
					continue;
				float xx = Grid[i][j].getX();
				float yy = Grid[i][j].getY();
				Rect R1 = new Rect((int) xx, (int) yy, (int) (xx + Grid[i][j]
						.getBitmap().getWidth()), (int) (yy + Grid[i][j]
						.getBitmap().getHeight()));
				if (R1.intersect(R2)) {
					float updated_Left_X = xx
							- Grid[i][j].getBitmap().getWidth() / 2;
					float updated_Y = yy + Grid[i][j].getBitmap().getHeight();
					float updated_Right_X = xx
							+ Grid[i][j].getBitmap().getWidth() / 2;

					if (i % 2 == 0 && j == 0) {
						mX = updated_Right_X;
						mY = updated_Y;
						moving_ball.setX(mX);
						moving_ball.setY(mY);
						Grid[i + 1][j] = moving_ball;
						dfs1(i + 1, j);
					} else if (i % 2 != 0 && j == 7) {
						mX = updated_Left_X;
						mY = updated_Y;
						moving_ball.setX(mX);
						moving_ball.setY(mY);
						Grid[i + 1][j] = moving_ball;
						dfs1(i + 1, j);
					} else {
						if (dist(mX, mY, updated_Left_X, updated_Y) < dist(mX,
								mY, updated_Right_X, updated_Y)) {
							mX = updated_Left_X;
							mY = updated_Y;
							moving_ball.setX(mX);
							moving_ball.setY(mY);
							if (i % 2 == 0) {
								if (j - 1 < 0)
									return;
								if (Grid[i + 1][j - 1].getColor() != Color.BLACK) {
									// left
									mX = xx - 56;
									mY = yy;
									moving_ball.setX(mX);
									moving_ball.setY(mY);
									Grid[i][j - 1] = moving_ball;
									dfs1(i, j - 1);
								} else {
									Grid[i + 1][j - 1] = moving_ball;
									dfs1(i + 1, j - 1);
								}
							} else {
								if (Grid[i + 1][j].getColor() != Color.BLACK) {
									// left
									mX = xx - 56;
									mY = yy;
									moving_ball.setX(mX);
									moving_ball.setY(mY);
									Grid[i][j - 1] = moving_ball;
									dfs1(i, j - 1);
								} else {
									Grid[i + 1][j] = moving_ball;
									dfs1(i + 1, j);
								}
							}
						} else {
							mX = updated_Right_X;
							mY = updated_Y;
							moving_ball.setX(mX);
							moving_ball.setY(mY);
							if (i % 2 == 0) {
								if (Grid[i + 1][j].getColor() != Color.BLACK) {
									// right
									mX = xx + 56;
									mY = yy;
									moving_ball.setX(mX);
									moving_ball.setY(mY);
									Grid[i][j + 1] = moving_ball;
									dfs1(i, j + 1);
								} else {
									Grid[i + 1][j] = moving_ball;
									dfs1(i + 1, j);
								}
							} else {
								if (j + 1 >= COLS)
									return;
								if (Grid[i + 1][j + 1].getColor() != Color.BLACK) {
									mX = xx + 56;
									mY = yy;
									moving_ball.setX(mX);
									moving_ball.setY(mY);
									Grid[i][j + 1] = moving_ball;
									dfs1(i, j + 1);
								} else {
									Grid[i + 1][j + 1] = moving_ball;
									dfs1(i + 1, j + 1);
								}
							}
						}
					}

					is_win = true;
					stick.start();
					int counter = 0;
					for (int k = 0; k < V.length; k++) {
						for (int k2 = 0; k2 < V[0].length; k2++) {
							if (V[k][k2])
								counter++;
						}
					}
					if (counter > 2) {
						create_level();
						for (int k = 0; k < V.length; k++) {
							for (int k2 = 0; k2 < V[0].length; k2++) {
								if (Grid[k][k2].getColor() != Color.BLACK
										&& V[k][k2]) {
									SCORE += 10;
									Grid[k][k2].setColor(Color.DKGRAY);
									Grid[k][k2].setmBitmap(BitmapFactory
											.decodeResource(getResources(),
													R.drawable.bomb));
								}
							}
						}
					} else {
						jumps--;
					}

					connected_component();

					// winning
					for (int k = 0; k < V.length && is_win; k++) {
						for (int k2 = 0; k2 < V[0].length && is_win; k2++) {
							if (Grid[k][k2] != null
									&& Grid[k][k2].getColor() != Color.BLACK
									&& Grid[k][k2].getColor() != Color.DKGRAY) {
								is_win = false;
							}
						}
					}

					create_moving_ball();
					finished = true;

					if (jumps <= 0) {
						shift();
					}
					return;
				}
			}
		}
		if (mX <= 0) { // shmal
			mSpeedX = -mSpeedX;
			mX = 0;
		} else if (mX + moving_ball.getBitmap().getWidth() >= this.getWidth()) { // ymeen
			mSpeedX = -mSpeedX;
			mX = (int) (this.getWidth() - moving_ball.getBitmap().getWidth());
		}
		if (mY <= 0) { // fou2
			finished = true;
			mY = 0;
			mX = Math.round(mX / 56.0) * 56;
			moving_ball.setX(mX);
			moving_ball.setY(mY);
			mSpeedY = -mSpeedY;
			add_to_grid(mX, mY);
			create_moving_ball();
			jumps--;
			if (jumps <= 0)
				shift();
		}

	}

	private void add_to_grid(float mX2, float mY2) {
		Grid[(int) (mY2 / 56) >= ROWS ? ROWS - 1 : (int) (mY2 / 56)][(int) (mX2 * 1. / 56.) >= COLS ? COLS - 1
				: (int) (mX2 * 1. / 56.)] = moving_ball;
	}

	private float dist(float mX2, float mY2, float updated_Left_X,
			float updated_Y) {
		return (float) Math.hypot(mX2 - updated_Left_X, mY2 - updated_Y);
	}

	public void dfs1(int i, int j) {
		if (i < 0 || j < 0 || j >= COLS || i >= ROWS)
			return;
		if (V[i][j])
			return;
		if (Grid[i][j].getColor() == Color.BLACK
				|| Grid[i][j].getColor() == Color.DKGRAY)
			return;
		if (Grid[i][j].getColor() != moving_ball.getColor())
			return;
		V[i][j] = true;
		if (i % 2 != 0) {
			dfs1(i - 1, j);
			dfs1(i - 1, j + 1);
			dfs1(i, j - 1);
			dfs1(i, j + 1);
			dfs1(i + 1, j);
			dfs1(i + 1, j + 1);
		} else {
			dfs1(i - 1, j);
			dfs1(i - 1, j - 1);
			dfs1(i, j - 1);
			dfs1(i, j + 1);
			dfs1(i + 1, j);
			dfs1(i + 1, j - 1);
		}
	}

	private void dfs2(int i, int j) {
		if (i < 0 || j < 0 || j >= COLS || i >= ROWS)
			return;
		if (V[i][j])
			return;
		if (Grid[i][j].getColor() == Color.BLACK
				|| Grid[i][j].getColor() == Color.DKGRAY)
			return;
		V[i][j] = true;
		if (i % 2 == 0) {
			dfs2(i + 1, j - 1);
			dfs2(i + 1, j);
			dfs2(i, j + 1);
			dfs2(i, j - 1);
		} else {
			dfs2(i + 1, j);
			dfs2(i + 1, j + 1);
			dfs2(i, j + 1);
			dfs2(i, j - 1);
		}
	}

	private void create_level() {
		if (level == 1)
			jumps = 8;
		else if (level == 2)
			jumps = 6;
		else if (level == 3)
			jumps = 4;
		else if (level == 4)
			jumps = 3;
	}

	private void shift() {
		create_level();
		boolean cont = true;
		for (int k = 0; k < COLS; k++)
			if (Grid[ROWS - 1][k].getColor() != Color.BLACK) {
				cont = false;
				break;
			}
		if (cont) {
			for (int i = ROWS - 1; i > 0; i--) {
				for (int j = 0; j < COLS; j++) {
					Grid[i][j] = null;
					Grid[i][j] = new Ball(Grid[i - 1][j].getBitmap(),
							Grid[i - 1][j].getX(), Grid[i - 1][j].getY() + 56,
							Grid[i - 1][j].getColor());

					if (i % 2 == 0)
						Grid[i][j].setX(Grid[i][j].getX() - 56 / 2);
					else
						Grid[i][j].setX(Grid[i][j].getX() + 56 / 2);
				}
			}
			int y = 0;
			for (int i1 = 0; i1 < 1; i1++) {
				int x = 0;
				if (i1 % 2 == 0)
					x = 0;
				else
					x = 56 / 2;
				for (int j1 = 0; j1 < COLS; j1++) {
					int random = r1.nextInt(4);
					if (random == 0)
						Grid[i1][j1] = new Ball(BitmapFactory.decodeResource(
								getResources(), R.drawable.blue), x, y,
								Color.BLUE);
					else if (random == 1)
						Grid[i1][j1] = new Ball(BitmapFactory.decodeResource(
								getResources(), R.drawable.green), x, y,
								Color.GREEN);
					else if (random == 2)
						Grid[i1][j1] = new Ball(BitmapFactory.decodeResource(
								getResources(), R.drawable.red), x, y,
								Color.RED);
					else if (random == 3)
						Grid[i1][j1] = new Ball(BitmapFactory.decodeResource(
								getResources(), R.drawable.violet), x, y,
								Color.MAGENTA);
					x += 56;
				}
				y += 56;
			}
			connected_component();
		}

	}

	public void connected_component() {
		for (boolean[] VV : V)
			Arrays.fill(VV, false);

		// connected component
		for (int cols = 0; cols < COLS; cols++) {
			if (!V[0][cols])
				dfs2(0, cols);
		}
		for (int k = 0; k < V.length; k++) {
			for (int k2 = 0; k2 < V[0].length; k2++) {
				if (Grid[k][k2].getColor() != Color.BLACK
						&& Grid[k][k2].getColor() != Color.DKGRAY && !V[k][k2]) {
					SCORE += 10;
					Grid[k][k2].setColor(Color.DKGRAY);
					Grid[k][k2].setmBitmap(BitmapFactory.decodeResource(
							getResources(), R.drawable.bomb));
				}
			}
		}
		for (boolean[] VV : V)
			Arrays.fill(VV, false);
	}
}