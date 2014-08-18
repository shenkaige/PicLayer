package com.phodev.piclayer.core;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;

/**
 * 背景Layer,最后一个添加，该Layer不接受任何Touch，Key事件，永远在底层
 * 
 * @author sky
 * 
 */
public class BackgroundLayer extends BaseBackgroundLayer {
	private float bg_scale;
	private Bitmap orignalBitmap;// 原始图片资源
	private Paint paint = new Paint();
	private int bgX;
	private int bgY;
	private Rect clipAreaMappingRect = new Rect();
	private Rect bitmapRect = new Rect();// 原始图片的Rect信息,left,top从0开始
	private Rect bgBgResourceOccupy = new Rect();// 背景图片资源占用区域
	private boolean originalBitmapChanged = false;

	@Override
	public void setLayerResouce(Bitmap bitmap) {
		if (bitmap != null) {
			orignalBitmap = bitmap;
			bitmapRect.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
		}
		originalBitmapChanged = true;
		super.setLayerResouce(bitmap);
	}

	@Override
	public void onDraw(Canvas canvas) {
		if (originalBitmapChanged) {
			// 第一次画的适合需要update，其他情况会自动更新FitScaleBitmap
			updateResourceInfo(canvas.getWidth(), canvas.getHeight(),
					getLayerBitmap());
			originalBitmapChanged = false;
		}
		Bitmap face = getLayerBitmap();
		if (face != null && !face.isRecycled()) {
			int count = canvas.save();
			canvas.translate(bgX, bgY);
			canvas.scale(bg_scale, bg_scale);
			canvas.drawBitmap(face, 0, 0, paint);
			canvas.restoreToCount(count);
		}
		super.onDraw(canvas);
	}

	@SuppressLint("WrongCall")
	@Override
	public void onMergeDraw(Canvas canvas, MergeRefer refer) {
		if (refer != this) {
			onDraw(canvas);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return false;
	}

	private int c_count;
	private Bitmap b;

	@Override
	public Canvas getMergeCanvas() {
		int width = orignalBitmap.getWidth();
		int height = orignalBitmap.getHeight();
		Rect r = getBgResourceOccupyArea();
		boolean isSmallerBg = false;
		if (r != null && !r.isEmpty()) {
			// 如果bg小于现在显示区域的大小
			if (width < r.width()) {
				width = r.width();
				isSmallerBg = true;
			}
			if (height < r.height()) {
				height = r.height();
				isSmallerBg = true;
			}

		}
		b = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(b);
		if (isSmallerBg) {
			int c_count_0 = canvas.save();
			canvas.scale(bg_scale, bg_scale);
			bg_scale = 1;// 清除scale
			canvas.drawBitmap(orignalBitmap, 0, 0, paint);
			canvas.restoreToCount(c_count_0);
		} else {
			canvas.drawBitmap(orignalBitmap, 0, 0, paint);
		}
		c_count = canvas.save();
		canvas.scale(1 / bg_scale, 1 / bg_scale);
		canvas.translate(-bgX, -bgY);
		return canvas;
	}

	@Override
	public Bitmap postMergeCanvas(Canvas canvas) {
		canvas.restoreToCount(c_count);
		return b;
	}

	/**
	 * 根据NewResource来更新相关信息
	 * 
	 * @param canvasW
	 * @param canvasH
	 * @param newResource
	 */
	private void updateResourceInfo(int canvasW, int canvasH, Bitmap newResource) {
		if (newResource != null) {// 更新适合缩放比例的Bitmap,尝试缩放到合适的尺寸一遍显示
			int bitW = newResource.getWidth();
			int bitH = newResource.getHeight();
			// if (bitW > canvasW || bitH > canvasH) {// 有必要缩放
			// if (bitW > bitH || bitW == bitH) {// 宽度比较大
			// bg_scale = (float) canvasW / bitW;
			// } else if (bitW < bitH) {// 宽度比较小
			// bg_scale = (float) canvasH / bitH;
			// }
			bg_scale = calculateFitScale(bitW, bitH, canvasW, canvasH);
			int scaledW = (int) (bitW * bg_scale);
			int scaledH = (int) (bitH * bg_scale);

			// 更新x,y坐标
			bgX = (canvasW - scaledW) / 2;
			bgY = (canvasH - scaledH) / 2;
			// 更新占用空间
			bgBgResourceOccupy.left = bgX;
			bgBgResourceOccupy.top = bgY;
			bgBgResourceOccupy.right = bgX + scaledW;
			bgBgResourceOccupy.bottom = bgY + scaledH;
		} else {
			bgBgResourceOccupy.set(0, 0, 0, 0);
			bgX = 0;
			bgY = 0;
		}
	}

	/**
	 * 获取图片缩放比
	 * 
	 * @return
	 */
	public float getBackgroundScale() {
		return bg_scale;
	}

	/**
	 * 根据Rect算出真实对应的Rect
	 * 
	 * @param clipArea
	 * @return
	 */
	public Rect getClipAreaMapping(Rect clipArea) {
		int offsetX = bgBgResourceOccupy.left;
		int offsetY = bgBgResourceOccupy.top;
		clipAreaMappingRect.left = (int) ((clipArea.left - offsetX) / bg_scale);
		clipAreaMappingRect.top = (int) ((clipArea.top - offsetY) / bg_scale);
		clipAreaMappingRect.right = (int) ((clipArea.right - offsetX) / bg_scale);
		clipAreaMappingRect.bottom = (int) ((clipArea.bottom - offsetY) / bg_scale);
		clipAreaMappingRect.intersect(bitmapRect);
		return clipAreaMappingRect;
	}

	/**
	 * 获取图片内容占用的区域--注：不是整个Layer占用的Area
	 * 
	 * @return
	 */
	public Rect getBgResourceOccupyArea() {
		return bgBgResourceOccupy;
	}

	/**
	 * 根据指定区域裁减背景图片
	 * 
	 * @param clipArea
	 * @return
	 */
	public Bitmap clipBackgroundBitmap(Rect clipArea) {
		return rawClipBackgroundBitmap(getClipAreaMapping(clipArea));
	}

	/**
	 * 根据指定区域裁减背景图片
	 */
	public Bitmap rawClipBackgroundBitmap(Rect rawRect) {
		Bitmap tempB = orignalBitmap;
		if (tempB == null || rawRect == null || tempB.isRecycled()
				|| rawRect.isEmpty()) {
			return null;
		}
		Bitmap bitmap = null;
		try {
			rawRect.intersect(bitmapRect);// 防止越界
			bitmap = Bitmap.createBitmap(tempB, rawRect.left, rawRect.top,
					rawRect.width(), rawRect.height());
		} catch (Exception e) {
			if (PictureConfig.DEBUG)
				e.printStackTrace();
		}
		// invalidate();
		return bitmap;
	}
}
