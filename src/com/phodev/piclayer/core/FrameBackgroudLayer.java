package com.phodev.piclayer.core;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Region.Op;
import android.util.FloatMath;
import android.view.MotionEvent;

/**
 * 第一次以合适的比例显示，若果原始尺寸大于屏幕尺寸，让最大的边充满屏幕，小的边等比缩放。如果图片尺寸小于屏幕尺寸，则居中显示不做任何处理
 * 
 * @author sky
 * 
 */
public class FrameBackgroudLayer extends BaseBackgroundLayer {
	private PointF mid = new PointF();
	// 模式
	private static final int NONE = 0;
	private static final int DRAG = 1;
	private static final int ZOOM = 2;
	private float oldDist;
	private int mode = NONE;
	private float picX = 0;
	private float picY = 0;
	private float scale = 1f;
	private final float maxFitScaleMultiple = 2;// 最大是fit scale的2倍
	private float maxScale;// 最大的Scale
	private int canvasW;
	private int canvasH;
	private int canvasCenterX;
	private int canvasCenterY;
	private FrameLayer frameLayer;
	private boolean needResetScale = false;
	private Paint paint;
	private boolean supportRotate = true;
	private boolean supportScaleCheck = false;
	private boolean supportPositionCheck = true;
	private float fitScale;
	private int bgAbsW;// 图片绝对尺寸
	private int bgAbsH;
	private int curBgW;// 安装当前缩放比例的尺寸
	private int curBgH;

	public FrameBackgroudLayer() {
		this(true, false);
	}

	public FrameBackgroudLayer(boolean supportRotate,
			boolean supportPositionCheck) {
		this.supportRotate = supportRotate;
		this.supportPositionCheck = supportPositionCheck;
		paint = new Paint();
	}

	@Override
	public void onDraw(Canvas canvas) {
		Bitmap b = getLayerBitmap();
		if (b == null)
			return;
		if (needResetScale) {
			canvasW = canvas.getWidth();
			canvasH = canvas.getHeight();
			bgAbsW = b.getWidth();
			bgAbsH = b.getHeight();
			// 以大的一边为基准，缩放到Canvas最大可以显示的尺寸
			// if (bW >= bH) {// 使用W
			// fitScale = (float) cW / bW;
			// } else {// 使用H
			// fitScale = (float) cH / bH;
			// }
			fitScale = calculateFitScale(bgAbsW, bgAbsH, canvasW, canvasH);
			maxScale = fitScale * maxFitScaleMultiple;
			scale = fitScale;
			updateCurBgPictureSize(scale);
			picX = (canvas.getWidth() - bgAbsW * scale) / 2;
			picY = (canvas.getHeight() - bgAbsH * scale) / 2;
			canvasCenterX = canvasW / 2;
			canvasCenterY = canvasH / 2;
			mid.set(0, 0);
			needResetScale = false;
		}
		final int c_count = canvas.save();
		if (frameLayer != null) {
			Rect r = frameLayer.getDisplayRect(canvas);
			if (r != null && !r.isEmpty()) {// 裁减点相框之外的视图
				canvas.clipRect(r, Op.REPLACE);
			}
			// 检查是否有交集
		} else {
			// 检查是否在中间
		}
		// 背景默认是白色
		canvas.drawColor(Color.WHITE);
		canvas.translate(picX, picY);
		if (supportRotate) {
			canvas.rotate(totalDegrees, canvasCenterX, canvasCenterY);
		}
		// canvas.scale(scale, scale, mid.x, mid.y);
		canvas.scale(scale, scale);
		canvas.drawBitmap(b, 0, 0, paint);
		canvas.restoreToCount(c_count);
		int left = (int) picX;
		int top = (int) picY;
		int right = (int) (left + b.getWidth() * scale);
		int bottom = (int) (picY + scale * b.getHeight());
		updateRect(left, top, right, bottom);
		// super.onDraw(canvas);
	}

	private int maxLeft;
	private int minRight;
	private int maxTop;
	private int minBottom;

	/**
	 * 更新当前图片缩放后的尺寸
	 */
	private void updateCurBgPictureSize(float scale) {
		curBgW = (int) (scale * bgAbsW);
		curBgH = (int) (scale * bgAbsH);
		if (curBgW > canvasW) {
			maxLeft = 0;
			minRight = canvasW;
		} else {
			maxLeft = (canvasW - curBgW) / 2;
			minRight = maxLeft + curBgW;
		}
		if (curBgH > canvasH) {
			maxTop = 0;
			minBottom = canvasH;
		} else {
			maxTop = (canvasH - curBgH) / 2;
			minBottom = maxTop + curBgH;
		}

	}

	@SuppressLint("WrongCall")
	@Override
	public void onMergeDraw(Canvas canvas, MergeRefer refer) {
		onDraw(canvas);
	}

	@Override
	public void setLayerResouce(Bitmap bitmap) {
		super.setLayerResouce(bitmap);
		resetScale();
	}

	/**
	 * 为背景绑定相框
	 * 
	 * @param layer
	 */
	public void bundleFrameLayer(FrameLayer layer) {
		frameLayer = layer;
		needResetScale = true;
		// 刷新背景显示
	}

	private float anchorX;
	private float anchorY;
	private float perDegrees = 0;
	private float totalDegrees = 0;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x = event.getX();
		float y = event.getY();
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			mode = DRAG;
			anchorX = x;
			anchorY = y;
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:
			mode = NONE;
			perDegrees = 0;
			boolean needInvalidate = false;
			if (supportScaleCheck) {
				if (scale < fitScale) {
					scale = fitScale;
					needResetScale = true;// 还原到原始缩放比例
					updateCurBgPictureSize(scale);
					needInvalidate = true;
				} else if (scale > maxScale) {
					scale = maxScale;
					updateCurBgPictureSize(scale);
					needInvalidate = true;
				}
			}
			if (supportPositionCheck) {// 检查距离
				if (picX > maxLeft) {
					picX = maxLeft;
				} else {
					float right = picX + curBgW;
					if (right < minRight) {
						picX += minRight - right;
					}
				}
				if (picY > maxTop) {
					picY = maxTop;
				} else {
					float bottom = picY + curBgH;
					if (bottom < minBottom) {
						picY += minBottom - bottom;
					}
				}
				needInvalidate = true;
			}
			if (needInvalidate) {
				invalidate();
			}
			break;
		case MotionEvent.ACTION_POINTER_DOWN:// 设置多点触摸模式
			oldDist = spacing(event);
			perDegrees = getPointerDegrees(event);
			if (oldDist > 10f) {
				midPoint(mid, event);
				mode = ZOOM;
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (mode == DRAG) {
				picX += (x - anchorX);
				picY += (y - anchorY);
			} else if (mode == ZOOM) {
				float newDist = spacing(event);
				if (newDist > 10f) {
					scale *= newDist / oldDist;
					updateCurBgPictureSize(scale);
					oldDist = newDist;// 更新oldDist
				}
				if (supportRotate) {
					if (event.getPointerCount() > 1) {
						float degrees = getPointerDegrees(event);
						totalDegrees += (degrees - perDegrees);
						perDegrees = degrees;
					}
				}
				midPoint(mid, event);
			}
			anchorX = x;
			anchorY = y;
			invalidate();
			break;
		}
		return true;
		// return super.onTouchEvent(event);
	}

	/**
	 * 计算移动距离
	 * 
	 * @param event
	 * @return
	 */
	private float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}

	/**
	 * 计算中点位置
	 * 
	 * @param point
	 * @param event
	 */
	private void midPoint(PointF point, MotionEvent event) {
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}

	@Override
	public void setLayerResouce(Bitmap bitmap, boolean destroyOldData) {
		needResetScale = true;
		super.setLayerResouce(bitmap, destroyOldData);
	}

	@Override
	public float getBackgroundScale() {
		return scale;
	}

	@Override
	public Rect getClipAreaMapping(Rect clipArea) {
		return null;
	}

	@Override
	public Rect getBgResourceOccupyArea() {
		if (frameLayer != null) {
			return frameLayer.getAbstactRect();
		} else {
			return getRect();
		}
	}

	@Override
	public Bitmap clipBackgroundBitmap(Rect clipArea) {
		return null;
	}

	@Override
	public Bitmap rawClipBackgroundBitmap(Rect rawRect) {
		return null;
	}

	@Override
	public Canvas getMergeCanvas() {
		return null;
	}

	@Override
	public Bitmap postMergeCanvas(Canvas canvas) {
		return null;
	}

	/**
	 * 重置缩放比例
	 */
	public void resetScale() {
		needResetScale = true;
	}

	public void setMinScaleCheck() {
		supportScaleCheck = true;
	}

	public boolean isHaveMinScaleCheck() {
		return supportScaleCheck;
	}

	public void cancelMinScaleCheck() {
		supportScaleCheck = false;
	}

	private float tempX0;
	private float tempY0;
	private float tempX1;
	private float tempY1;

	private float getPointerDegrees(MotionEvent event) {
		tempX0 = event.getX(0);
		tempY0 = event.getY(0);
		tempX1 = event.getX(1);
		tempY1 = event.getY(1);
		return PictureUtils.getLineDegrees(tempX0, tempY0, tempX1, tempY1);
		// if (tempY0 > tempY1) {
		// return PictureUtils.getLineDegrees(tempX0, tempY0, tempX1, tempY1);
		// } else {
		// return PictureUtils.getLineDegrees(tempX1, tempY1, tempX0, tempY0);
		// }

	}
}
