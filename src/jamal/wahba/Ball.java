package jamal.wahba;


import android.graphics.Bitmap;

public class Ball {
	private Bitmap mBitmap;	// the actual mBitmap
	private float x;			// the X coordinate
	private float y;			// the Y coordinate
	private int color;
	
	public Ball(Bitmap mBitmap, float x, float y, int color) {
		this.mBitmap = mBitmap;
		this.x = x;
		this.y = y;
		this.color = color;
	}
	 
	public Bitmap getBitmap() {
		return mBitmap;
	}
	
	
	public void setmBitmap(Bitmap mBitmap) {
		this.mBitmap = mBitmap;
	}

	public float getX() {
		return x;
	}
	public void setX(float x) {
		this.x = x;
	}
	public float getY() {
		return y;
	}
	public void setY(float y) {
		this.y = y;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}
	

}
