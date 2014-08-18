package com.phodev.piclayer.core.impl;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Region.Op;
import android.graphics.Shader.TileMode;

import com.phodev.piclayer.core.BitmapFeather;
import com.phodev.piclayer.core.BlurLayer;
import com.phodev.piclayer.core.BlurLayer.BlurLayerDrawer;

/**
 * 
 * @author sky
 *
 */
public class BlurCircleDrawer implements BlurLayerDrawer {
	private Paint pathPath;
	private Paint whitePaint;
	private Paint defPaint;
	private Path cPath;
	private BitmapFeather mFeather;

	private Rect mBgRect;
	private float mBgScale;
	private float mCenterX;
	private float mCenterY;
	private int mRadius;
	private final int color_top_white = 0xcfffffff;
	private final int feahterRadius = 28;// 模糊边缘

	public BlurCircleDrawer() {
		pathPath = new Paint();
		whitePaint = new Paint();
		defPaint = new Paint();
		whitePaint.setColor(color_top_white);
		cPath = new Path();
		mFeather = new BitmapFeather();
	}

	public void setDrawerInfo(Rect bgRect, float bgScale, float cx, float cy,
			int radius) {
		mBgRect = bgRect;
		mCenterX = cx;
		mCenterY = cy;
		mRadius = radius;
		mBgScale = bgScale;
	}

	@Override
	public void drawPreview(Canvas canvas, BlurLayer refer) {
		RadialGradient radialGradient = getRadialGradient();
		pathPath.setColor(Color.WHITE);
		pathPath.setShader(radialGradient);
		cPath.reset();
		cPath.addCircle(0, 0, mRadius, Direction.CCW);
		cPath.offset(mCenterX, mCenterY);
		int c_count = canvas.save();
		canvas.clipPath(cPath, Op.DIFFERENCE);
		canvas.drawRect(mBgRect, whitePaint);
		canvas.restoreToCount(c_count);
		//
		c_count = canvas.save();
		canvas.clipRect(mBgRect, Op.INTERSECT);
		canvas.drawCircle(mCenterX, mCenterY, mRadius, pathPath);
		canvas.restoreToCount(c_count);
	}

	@Override
	public Bitmap drawResult(Canvas canvas, int[] pxs, Bitmap result,
			BlurLayer refer) {
		int bw = result.getWidth();
		int bh = result.getHeight();
		int left = mBgRect.left;
		int top = mBgRect.top;
		mFeather.makeFeahter(pxs, bw, bh, (int) (mRadius / mBgScale),
				(int) ((mCenterX - mBgRect.left) / mBgScale),
				(int) ((mCenterY - mBgRect.top) / mBgScale), feahterRadius);
		result.setPixels(pxs, 0, bw, 0, 0, bw, bh);

		cPath.reset();
		cPath.addCircle(0, 0, mRadius - feahterRadius * mBgScale, Direction.CCW);
		cPath.offset(mCenterX, mCenterY);

		int c_count = canvas.save();
		canvas.clipPath(cPath, Op.DIFFERENCE);
		canvas.translate(left, top);
		canvas.scale(mBgScale, mBgScale);
		canvas.drawBitmap(result, 0, 0, defPaint);
		canvas.restoreToCount(c_count);
		return result;
	}

	@Override
	public int getAreaFakeSize() {
		return mRadius * 2;
	}

	@Override
	public void updateAreaFakeSize(float newSize) {
		mRadius = (int) newSize / 2;
	}

	@Override
	public void updateAreaLocation(float xMoved, float yMoved) {
		mCenterX += xMoved;
		mCenterY += yMoved;
		if (mCenterX < mBgRect.left) {
			mCenterX = mBgRect.left;
		}
		if (mCenterX > mBgRect.right) {
			mCenterX = mBgRect.right;
		}
		if (mCenterY < mBgRect.top) {
			mCenterY = mBgRect.top;
		}
		if (mCenterY > mBgRect.bottom) {
			mCenterY = mBgRect.bottom;
		}
	}

	@Override
	public void updateAreaDegrees(float degreesAdd) {
		// 圆形不存在角度旋转
	}

	private RadialGradient getRadialGradient() {
		RadialGradient radialGradient = new RadialGradient(mCenterX, mCenterY,
				mRadius, colors, getRadialColorWeight(), TileMode.CLAMP);
		return radialGradient;
	}

	// 渐变颜色比例划分
	private float[] colorWeight = new float[] { 0, 0, 0, 1 };
	private final int[] colors = new int[] { Color.TRANSPARENT,
			Color.TRANSPARENT, 0x0feeeeee, 0xdfffffff };

	private float[] getRadialColorWeight() {
		float length1 = mRadius - feahterRadius;
		if (length1 > 0) {
			colorWeight[1] = length1 / mRadius;
			colorWeight[2] = (length1 + (float) feahterRadius / 3) / mRadius;
		}
		return colorWeight;
	}
}
