package com.phodev.piclayer.core;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.FloatMath;
import android.view.MotionEvent;
import com.phodev.piclayer.core.impl.BlurCircleDrawer;
import com.phodev.piclayer.core.impl.BlurRectDrawer;

/**
 * @author sky
 */
public class BlurLayer extends Layer {
	private Paint defPaint;
	private Paint whitePaint;
	private BaseBackgroundLayer bgLayer;
	private int maxAreaSize;
	private int minAreaSize = 50;
	private float cCenterX;
	private float cCenterY;
	private boolean needReset = true;
	private boolean isPressed = false;
	private Bitmap resultBitmap;
	private int[] pxs;
	private int bw;
	private int bh;
	private BlurCircleDrawer circleDrawer = new BlurCircleDrawer();
	private BlurRectDrawer rectDrawer = new BlurRectDrawer();
	private BlurLayerDrawer drawer = rectDrawer;// 默认

	@Override
	public void setLayerResouce(Bitmap bitmap, boolean destroyOldData) {
		if (resultBitmap != null && !resultBitmap.isRecycled()) {
			resultBitmap.recycle();
		}
		if (bitmap != null && !bitmap.isRecycled()) {
			resultBitmap = bitmap.copy(bitmap.getConfig(), true);
		}
		bw = bitmap.getWidth();
		bh = bitmap.getHeight();
		if (pxs == null || pxs.length != (bw * bh)) {
			pxs = new int[bw * bh];
		}
		bitmap.getPixels(pxs, 0, bw, 0, 0, bw, bh);
		super.setLayerResouce(bitmap, destroyOldData);
	}

	public BlurLayer() {
		defPaint = new Paint();
		defPaint.setAntiAlias(true);
		whitePaint = new Paint();
		whitePaint.setColor(0xefeeeeee);
		whitePaint.setAntiAlias(true);
	}

	@Override
	public void onDraw(Canvas canvas) {
		Bitmap source = getLayerBitmap();
		if (source == null || source.isRecycled())
			return;
		if (!checkBgLayerInfo())
			return;
		Rect bgOccupyArea = bgLayer.getBgResourceOccupyArea();
		float scale = bgLayer.getBackgroundScale();
		int cavnasCenterX = canvas.getWidth() / 2;
		int cavnasCenterY = canvas.getHeight() / 2;
		if (needReset) {
			needReset = false;
			int w = bgOccupyArea.width();
			int h = bgOccupyArea.height();
			if (w > h) {
				maxAreaSize = h;
			} else {
				maxAreaSize = w;
			}
			cCenterX = canvas.getWidth() / 2;
			cCenterY = canvas.getHeight() / 2;
			circleDrawer.setDrawerInfo(bgOccupyArea, scale, cavnasCenterX,
					cavnasCenterY, 80);
			rectDrawer.setDrawerInfo(bgOccupyArea, scale, 80);
		}
		if (isPressed) {
			drawer.drawPreview(canvas, this);
		} else {
			source.getPixels(pxs, 0, bw, 0, 0, bw, bh);
			drawer.drawResult(canvas, pxs, resultBitmap, this);
		}
		// super.onDraw(canvas);
	}

	@SuppressLint("WrongCall")
	@Override
	public void onMergeDraw(Canvas canvas, MergeRefer refer) {
		onDraw(canvas);
	}

	private boolean checkBgLayerInfo() {
		LayerParent parent = getParent();
		if (parent == null) {
			return false;
		}
		bgLayer = parent.getBackgroundLayer();
		if (bgLayer == null) {
			return false;
		}
		return true;
	}

	PointF start = new PointF();
	PointF mid = new PointF();
	PointF anchor = new PointF();
	private final int NONE = 0;
	private final int DRAG = 1;
	private final int ZOOM = 2;
	private int mode;// 当前action的模式
	private float oldDist;
	float preDegrees = 0;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x = event.getX();
		float y = event.getY();
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			mode = DRAG;
			start.set(x, y);
			anchor.set(x, y);
			isPressed = true;
			invalidate();
			break;
		case MotionEvent.ACTION_UP:
			isPressed = false;
			invalidate();
			// break;继续执行不需要break
		case MotionEvent.ACTION_POINTER_UP:
			mode = NONE;
			break;
		case MotionEvent.ACTION_POINTER_DOWN:// 设置多点触摸模式
			oldDist = spacing(event);
			preDegrees = getPointerDegrees(event);
			if (oldDist > 10f) {
				midPoint(mid, event);
				mode = ZOOM;
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (mode == DRAG) {
				start.set(x, y);
				updateLocation(x - anchor.x, y - anchor.y);
				invalidate();
			} else if (mode == ZOOM) {
				float newDist = spacing(event);
				if (newDist > 10f) {
					float areaScale = newDist / oldDist;
					oldDist = newDist;// 更新oldDist
					updateSize(areaScale);
				}
				float degrees = getPointerDegrees(event);
				updateDegrees(degrees - preDegrees);
				preDegrees = degrees;
				// // 更新旋转角度
				// updateDegrees(PictureUtils.getRawDegrees(event.getX(0),
				// event.getY(0), event.getX(1), event.getY(1), cCenterX,
				// cCenterY));
				invalidate();
			}
			anchor.set(x, y);
			break;
		}
		return super.onTouchEvent(event);
	}

	/**
	 * 使用圆圈作为非虚化区域
	 */
	public void useRoundArea() {
		drawer = circleDrawer;
		showPreviewOnce();
	}

	/**
	 * 使用作为非虚化区域
	 */
	public void useRectArea() {
		drawer = rectDrawer;
		showPreviewOnce();
	}

	public void showPreviewOnce() {
		isPressed = true;
		invalidate();
		isPressed = false;
		invalidate();
	}

	/**
	 * 更新区域大小（如果是Circle就是更新直径，如果是矩形就更新宽度）
	 * 
	 * @param distAdd
	 */
	private void updateSize(float scale) {
		float newSize = scale * drawer.getAreaFakeSize();
		if (newSize < minAreaSize) {
			newSize = minAreaSize;
		} else if (newSize > maxAreaSize) {
			newSize = maxAreaSize;
		}
		drawer.updateAreaFakeSize(newSize);
	}

	/**
	 * 更新区域位置
	 * 
	 * @param centerX
	 * @param centerY
	 */
	private void updateLocation(float xDist, float yDist) {
		drawer.updateAreaLocation(xDist, yDist);
	}

	private void updateDegrees(float degreesAdd) {
		drawer.updateAreaDegrees(degreesAdd);
	}

	public interface BlurLayerDrawer {
		public void drawPreview(Canvas canvas, BlurLayer refer);

		public Bitmap drawResult(Canvas canvas, int[] resPixs, Bitmap result,
				BlurLayer refer);

		/**
		 * 获取MappingArea区域伪尺寸
		 * 
		 * @return
		 */
		public int getAreaFakeSize();

		/**
		 * 更新MappingArea伪尺寸
		 * 
		 * @param newSize
		 */
		public void updateAreaFakeSize(float newSize);

		public void updateAreaLocation(float xMoved, float yMoved);

		public void updateAreaDegrees(float degreesAdd);
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
	}
}
