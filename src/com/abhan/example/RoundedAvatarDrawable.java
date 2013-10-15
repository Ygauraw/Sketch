package com.abhan.example;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

public class RoundedAvatarDrawable extends Drawable {
	private final Bitmap mBitmap;
	private final Paint mPaint;
	private final RectF mRectF;
	private final int mBitmapWidth;
	private final int mBitmapHeight;
	private final float mShadowOffset;
	private final Paint mShadowPaint;
	
	public RoundedAvatarDrawable(Bitmap bitmap, float shadowSize, int shadowColor) {
		mBitmap = bitmap;
		mRectF = new RectF();
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setDither(true);
		mShadowPaint = new Paint(mPaint);
		final BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP,
				Shader.TileMode.CLAMP);
		mPaint.setShader(shader);
		mShadowPaint.setColor(Color.WHITE);
		mShadowPaint.setShadowLayer(shadowSize, 0.0f, 0.0f, shadowColor);
		mShadowOffset = shadowSize;
		mBitmapWidth = (int) (mBitmap.getWidth() + mShadowOffset + 0.5f);
		mBitmapHeight = (int) (mBitmap.getHeight() + mShadowOffset + 0.5f);
	}
	
	@Override
	public void draw(Canvas canvas) {
		canvas.drawOval(mRectF, mShadowPaint);
		canvas.drawOval(mRectF, mPaint);
	}
	
	@Override
	protected void onBoundsChange(Rect bounds) {
		super.onBoundsChange(bounds);
		mRectF.set(bounds);
		mRectF.inset(mShadowOffset, mShadowOffset);
	}
	
	@Override
	public void setAlpha(int alpha) {
		if (mPaint.getAlpha() != alpha) {
			mPaint.setAlpha(alpha);
			invalidateSelf();
		}
	}
	
	@Override
	public void setColorFilter(ColorFilter cf) {
		mPaint.setColorFilter(cf);
	}
	
	@Override
	public int getOpacity() {
		return PixelFormat.TRANSLUCENT;
	}
	
	@Override
	public int getIntrinsicWidth() {
		return mBitmapWidth;
	}
	
	@Override
	public int getIntrinsicHeight() {
		return mBitmapHeight;
	}
	
	public void setAntiAlias(boolean aa) {
		mPaint.setAntiAlias(aa);
		invalidateSelf();
	}
	
	@Override
	public void setFilterBitmap(boolean filter) {
		mPaint.setFilterBitmap(filter);
		invalidateSelf();
	}
	
	@Override
	public void setDither(boolean dither) {
		mPaint.setDither(dither);
		invalidateSelf();
	}
	
	public Bitmap getBitmap() {
		return mBitmap;
	}
}