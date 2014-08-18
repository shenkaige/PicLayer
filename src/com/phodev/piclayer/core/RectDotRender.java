package com.phodev.piclayer.core;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import com.phodev.android.piclayer.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * 渲染Dot
 * 
 * @author sky
 * 
 */
public class RectDotRender {
	private final int dot_r = PictureConfig.getDotRadius();// 四个点的最大半径
	private final int dot_inner_r = PictureConfig.getDotRadiusInner();// 内嵌的白色点的R
	private int dot_bg = PictureConfig.dot_color_bg;
	private int dot_bg_pressed_color = PictureConfig.dot_color_inner;
	private int dot_inner_pressed_color = PictureConfig.dot_color_bg_pressed;
	// 'X'删除的绘画信心
	// private final int cancle_bg_color = PictureConfig.dot_cancle_color_bg;
	// private final int cancle_mark_color =
	// PictureConfig.dot_cancle_mark_color;
	// private final int lines_width =
	// PictureConfig.getDotCancleMarkLinesWidth();
	// private Paint linePaint;// cancel button上的'x'画笔
	private Paint paint;
	private Paint defPaint;// 默认使用的

	//
	private static RectDotRender instance;
	private Context mContext;

	public static RectDotRender getInstance(Context context) {
		if (instance == null) {
			instance = new RectDotRender(context);
		}
		return instance;
	}

	private RectDotRender(Context context) {
		this.mContext = context;
		paint = new Paint();
		paint.setAntiAlias(true);
		defPaint = new Paint();
		defPaint.setAntiAlias(true);
	}

	public void drawDot(Canvas canvas, DotDelegate dot) {
		switch (dot.getDotType()) {
		case STANDARD:
			if (dot.getState() == DotDelegate.State.PREESED) {
				drawDotSelected(canvas, dot);
			} else {
				drawDotNormal(canvas, dot);
			}
			break;
		case CANCLE:
			if (dot.getState() == DotDelegate.State.PREESED) {
				drawCancleDot(canvas, dot, true);
			} else {
				drawCancleDot(canvas, dot, false);
			}
			break;
		case ROTATE:
			drawRotateDot(canvas, dot);
			break;
		}
	}

	/**
	 * 绘画普通的点
	 * 
	 * @param canvas
	 * @param dot
	 */
	public void drawDotNormal(Canvas canvas, DotDelegate dot) {
		float cx = dot.getCX();
		float cy = dot.getCY();
		paint.setColor(dot_bg_pressed_color);
		canvas.drawCircle(cx, cy, dot_r, paint);
		paint.setColor(dot_bg);
		canvas.drawCircle(cx, cy, dot_inner_r, paint);
	}

	/**
	 * 画别选择的点
	 * 
	 * @param canvas
	 * @param dot
	 */
	public void drawDotSelected(Canvas canvas, DotDelegate dot) {
		float cx = dot.getCX();
		float cy = dot.getCY();
		paint.setColor(dot_inner_pressed_color);
		canvas.drawCircle(cx, cy, dot_r, paint);
		paint.setColor(dot_bg);
		canvas.drawCircle(cx, cy, dot_inner_r, paint);
	}

	/**
	 * 画取消的点
	 */
	public void drawCancleDotPressed(Canvas canvas, DotDelegate dot) {
		drawCancleDot(canvas, dot, true);
	}

	/**
	 * 画取消的点
	 */
	public void drawCancleDotNormal(Canvas canvas, DotDelegate dot) {
		drawCancleDot(canvas, dot, false);
	}

	/**
	 * 画取消的点
	 */
	private void drawCancleDot(Canvas canvas, DotDelegate dot, boolean pressed) {
		float cx = dot.getCX();
		float cy = dot.getCY();
		// // padding底图
		// if (pressed) {
		// paint.setColor(dot_inner_pressed_color);
		// } else {
		// paint.setColor(dot_bg_pressed_color);
		// }
		// canvas.drawCircle(cx, cy, dot_r, paint);
		// // 红色圆圈
		// paint.setColor(cancle_bg_color);
		// canvas.drawCircle(cx, cy, dot_inner_r, paint);
		// int r = dot_inner_r;
		// int offset = (int) Math.sqrt(r * r) / 2;
		// int x = (int) cx;
		// int y = (int) cy;
		// int startX = x - offset;
		// int startY = y - offset;
		// int stopX = x + offset;
		// int stopY = y + offset;
		// // 设置线的宽度
		// canvas.drawLine(startX, startY, stopX, stopY, getLinePaint());
		// startX = x + offset;
		// startY = y - offset;
		// stopX = x - offset;
		// stopY = y + offset;
		// canvas.drawLine(startX, startY, stopX, stopY, getLinePaint());
		Bitmap bitmap = getBitmap(R.drawable.btn_cancel);
		if (bitmap != null && !bitmap.isRecycled()) {
			int w = bitmap.getWidth();
			int h = bitmap.getHeight();
			cx -= w / 2;
			cy -= h / 2;
			canvas.drawBitmap(bitmap, cx, cy, defPaint);
		}
	}

	/**
	 * 旋转的点
	 * 
	 * @param canvas
	 * @param dot
	 * @param pressed
	 */
	private void drawRotateDot(Canvas canvas, DotDelegate dot) {
		float cx = dot.getCX();
		float cy = dot.getCY();
		Bitmap bitmap = getBitmap(R.drawable.btn_rotate);
		if (bitmap != null && !bitmap.isRecycled()) {
			int w = bitmap.getWidth();
			int h = bitmap.getHeight();
			cx -= w / 2;
			cy -= h / 2;
			canvas.drawBitmap(bitmap, cx, cy, defPaint);
		}
		// if (dot.getState() == DotDelegate.State.PREESED) {
		// //
		// drawDotSelected(canvas, dot);
		// } else {
		// //
		// drawDotNormal(canvas, dot);
		// }
	}

	// private Paint getLinePaint() {
	// if (linePaint == null) {
	// linePaint = new Paint();
	// linePaint.setColor(cancle_mark_color);
	// linePaint.setStrokeWidth(lines_width);
	// linePaint.setAntiAlias(true);
	// }
	// return linePaint;
	// }

	/**
	 * 获取Dot将会绘画的大小R
	 * 
	 * @return
	 */
	public int getRenderTargetRaduis() {
		return dot_r;
	}

	/**
	 * 在运行期间缓存
	 */
	private HashMap<Integer, SoftReference<Bitmap>> bitmaps = new HashMap<Integer, SoftReference<Bitmap>>();// 在运行期间缓存

	private Bitmap getBitmap(int resId) {
		SoftReference<Bitmap> refer = bitmaps.get(resId);
		if (refer == null || refer.get() == null) {
			refer = new SoftReference<Bitmap>(createBitmap(resId));
			bitmaps.put(resId, refer);
		}
		return refer.get();
	}

	/**
	 * 创建指定资源ID的Bitmap
	 * 
	 * @param resId
	 * @return
	 */
	private Bitmap createBitmap(int resId) {
		return BitmapFactory.decodeResource(mContext.getResources(), resId);
	}

	/**
	 * 释放所有Bitmap
	 */
	public void releaseAllBitmap() {
		if (bitmaps != null) {
			Iterator<Entry<Integer, SoftReference<Bitmap>>> iterator;
			iterator = bitmaps.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<Integer, SoftReference<Bitmap>> e = iterator.next();
				releaseBitmap(e.getValue().get());
			}
			bitmaps.clear();
		}
	}

	private void releaseBitmap(Bitmap b) {
		if (b != null && !b.isRecycled()) {
			b.recycle();
		}
	}
}
