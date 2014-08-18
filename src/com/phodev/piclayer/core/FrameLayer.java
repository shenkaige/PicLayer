package com.phodev.piclayer.core;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.view.MotionEvent;

/**
 * 
 * @author sky
 *
 */
public class FrameLayer extends Layer implements MergeRefer {
	private Paint paint;
	private float scale = 1;
	private boolean layerResourceChanged = false;

	public FrameLayer() {
		paint = new Paint();
	}

	@Override
	public void onDraw(Canvas canvas) {
		Bitmap b = getLayerBitmap();
		if (b == null || b.isRecycled())
			return;
		checkLayerInfo(canvas, b);
		int c_count = canvas.save();
		canvas.translate(getLayerX(), getLayerY());
		canvas.scale(scale, scale);
		canvas.drawBitmap(b, 0, 0, paint);
		canvas.restoreToCount(c_count);
		// super.onDraw(canvas);
	}

	@SuppressLint("WrongCall")
	@Override
	public void onMergeDraw(Canvas canvas, MergeRefer refer) {
		if (refer == this) {
			int count = canvas.save();
			canvas.translate(getLayerX(), getLayerY());
			canvas.scale(scale, scale);
			canvas.drawBitmap(getLayerBitmap(), 0, 0, paint);
			canvas.restoreToCount(count);
		} else {
			onDraw(canvas);
		}
	}

	/**
	 * 检查并初始化Layer必要信息
	 * 
	 * @param canvas
	 * @param layerBitmap
	 */
	private void checkLayerInfo(Canvas canvas, Bitmap layerBitmap) {
		if (layerResourceChanged && layerBitmap != null) {
			int cW = canvas.getWidth();
			int cH = canvas.getHeight();
			int bW = layerBitmap.getWidth();
			int bH = layerBitmap.getHeight();
			// // 以大的一边为基准，缩放到Canvas最大可以显示的尺寸
			// if (bW > cW && bH > cH) {
			// if (bW >= bH) {// 使用W
			// scale = (float) cW / bW;
			// } else {// 使用H
			// scale = (float) cH / bH;
			// }
			// } else if (bH > cH) {// 自有高度大于
			// scale = (float) cH / bH;
			// } else if (bW > cW) {// 只有宽度大于
			// scale = (float) cW / bW;
			// }
			scale = calculateFitScale(bW, bH, cW, cH);
			int scaledW = (int) (bW * scale);
			int scaledH = (int) (bH * scale);
			int left = (canvas.getWidth() - scaledW) / 2;
			int top = (canvas.getHeight() - scaledH) / 2;
			updateRect(left, top, left + scaledW, top + scaledH);
			setLayerX(left);
			setLayerY(top);

			int absLeft = (int) (left * scale);
			int absTop = (int) (top * scale);
			bitmapAbsRect.set(absLeft, absTop, absLeft + bW, absTop + bH);
			layerResourceChanged = false;
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			invalidate();
			break;
		case MotionEvent.ACTION_UP:
			invalidate();
			break;
		}
		return false;
	}

	/**
	 * 相框显示的区域
	 * 
	 * @return
	 */
	public Rect getDisplayRect(Canvas c) {
		checkLayerInfo(c, getLayerBitmap());
		return getRect();
	}

	Rect bitmapAbsRect = new Rect();

	public Rect getAbstactRect() {
		return bitmapAbsRect;
	}

	public float getFrameScale() {
		return scale;
	}

	@Override
	public void setLayerResouce(Bitmap bitmap, boolean destroyOldData) {
		layerResourceChanged = true;
		super.setLayerResouce(bitmap, destroyOldData);
	}

	private int c_count;
	private Bitmap b;// 文件需要调用者自己管理生命周期

	@Override
	public Canvas getMergeCanvas() {
		Bitmap orignalBitmap = getLayerBitmap();
		if (orignalBitmap != null) {
			int width = orignalBitmap.getWidth();
			int height = orignalBitmap.getHeight();
			b = Bitmap.createBitmap(width, height, Config.ARGB_8888);
			Canvas canvas = new Canvas(b);
			canvas.drawColor(PictureConfig.frame_def_bg_color, Mode.CLEAR);
			c_count = canvas.save();
			canvas.scale(1 / scale, 1 / scale);
			canvas.translate(-getLayerX(), -getLayerY());
			return canvas;
		} else {
			return null;
		}
	}

	@Override
	public Bitmap postMergeCanvas(Canvas canvas) {
		if (canvas != null) {
			canvas.restoreToCount(c_count);
		}
		return b;
	}

	@Override
	public Layer getMergeReferLayer() {
		return this;
	}

	@Override
	public int getMergeReferLayerPriority() {
		return MergeRefer.MERGE_ALWAYS_LAST;
	}
}
