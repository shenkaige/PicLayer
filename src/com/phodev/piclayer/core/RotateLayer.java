package com.phodev.piclayer.core;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Region.Op;
import android.view.MotionEvent;

/**
 * @author sky
 */
public class RotateLayer extends Layer implements MergeRefer {
	private Paint defPaint;
	private RectF mappingRect;
	private RectF rectBg;
	private int cCenterX;
	private int cCenterY;
	private int cWidth;
	private int cHeight;
	private float absLeft;
	private float absTop;
	private float rootDegrees = 0;
	private int totalDegrees = 0;// 当前总够旋转了角度数
	private float totalScale = 1;// 总共缩放的尺寸
	private float fitScale = 1;
	private boolean isFirstDraw = false;
	private float bili;
	private Bitmap originalBitmap;
	private Bitmap resultBitmap;
	private boolean isTouchPreesed = false;// 旋转touch up

	private int bgW;
	private int bgH;
	// 运行时参数,并不一定是真实的尺寸
	private int tempBgW;
	private int tempBgH;

	public RotateLayer() {
		defPaint = new Paint();
		defPaint.setAntiAlias(false);
		mappingRect = new RectF();
		rectBg = new RectF();
	}

	@Override
	public void setLayerResouce(Bitmap bitmap) {
		if (originalBitmap == null) {
			originalBitmap = bitmap;
		}
		super.setLayerResouce(bitmap);
	}

	@Override
	public void setLayerResouce(Bitmap bitmap, boolean destroyOldData) {
		isFirstDraw = true;
		super.setLayerResouce(bitmap, destroyOldData);
	}

	@Override
	public void onDraw(Canvas canvas) {
		Bitmap bgBitmap = getDrawBitmap();
		if (bgBitmap == null) {
			return;
		}
		if (isFirstDraw) {
			cWidth = canvas.getWidth();
			cHeight = canvas.getHeight();
			bgW = bgBitmap.getWidth();
			bgH = bgBitmap.getHeight();
			cCenterX = cWidth / 2;
			cCenterY = cHeight / 2;
			// 以大的一边为基准，缩放到Canvas最大可以显示的尺寸
			// if (bgW >= bgH) {// 使用W
			// fitScale = (float) cWidth / bgW;
			// } else {// 使用H
			// fitScale = (float) cHeight / bgH;
			// }
			fitScale = calculateFitScale(bgW, bgH, cWidth, cHeight);
			tempBgW = bgW;
			tempBgH = bgH;
			totalScale = fitScale;
			bili = (float) bgW / bgH;
			isFirstDraw = false;
		}

		float targW = tempBgW * totalScale;
		float targH = tempBgH * totalScale;
		rectBg.left = (cWidth - targW) / 2;
		rectBg.top = (cHeight - targH) / 2;
		rectBg.right = rectBg.left + targW;
		rectBg.bottom = rectBg.top + targH;

		Size s = getNewSize(targW, targH, totalDegrees, bili);
		mappingRect.left = (cWidth - s.width) / 2;
		mappingRect.top = (cHeight - s.height) / 2;
		mappingRect.right = mappingRect.left + s.width;
		mappingRect.bottom = mappingRect.top + s.height;

		absLeft = (cWidth - bgW) / 2;
		absTop = (cHeight - bgH) / 2;

		int c_count = canvas.save();
		canvas.clipRect(rectBg, Op.INTERSECT);
		if (!isTouchPreesed)
			canvas.clipRect(mappingRect, Op.INTERSECT);
		canvas.rotate(-totalDegrees + rootDegrees, cCenterX, cCenterY);
		canvas.scale(totalScale, totalScale, cCenterX, cCenterY);
		canvas.drawBitmap(bgBitmap, absLeft, absTop, defPaint);
		canvas.restoreToCount(c_count);
		//
		if (isTouchPreesed) {
			c_count = canvas.save();
			canvas.clipRect(rectBg, Op.INTERSECT);
			canvas.clipRect(mappingRect, Op.DIFFERENCE);
			canvas.drawColor(0x5f000000);
			canvas.restoreToCount(c_count);
		}

		super.onDraw(canvas);
	}

	@Override
	public void onMergeDraw(Canvas canvas, MergeRefer refer) {
		Bitmap b = getDrawBitmap();
		if (b != null && !b.isRecycled()) {
			float left = (canvas.getWidth() - bgW) / 2;
			float top = (canvas.getHeight() - bgH) / 2;
			canvas.drawBitmap(b, left, top, defPaint);
		}
	}

	float preX;
	float preY;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_MOVE:
			float x = event.getX();
			float y = event.getY();
			totalDegrees += getRawDegrees(x, y, preX, preY, cCenterX, cCenterY);
			totalDegrees %= 360;
			preX = x;
			preY = y;
			invalidate();
			break;
		case MotionEvent.ACTION_DOWN:
			isTouchPreesed = true;
			preX = event.getX();
			preY = event.getY();
			totalScale = fitScale;
			invalidate();
			break;
		case MotionEvent.ACTION_UP:
			isTouchPreesed = false;
			checkTotalScale();
			invalidate();
			break;
		}
		return super.onTouchEvent(event);
	}

	/**
	 * 检查TotalScale，让图片是用最大可
	 */
	private void checkTotalScale() {
		// 以大的一边为基准，缩放到Canvas最大可以显示的尺寸
		if (mappingRect.width() > mappingRect.height()) {// 使用W
			float w = mappingRect.width() / totalScale;
			totalScale = (float) rectBg.width() / w;
		} else {// 使用H
			float h = mappingRect.height() / totalScale;
			totalScale = (float) rectBg.height() / h;
		}
	}

	private Size newSize = new Size();

	public Size getNewSize(float origW, float origH, float angle, float bili) {
		float factor = 0.0f;
		angle = (float) Math.toRadians(angle);
		if (bili <= 1.0f) {
			factor = (float) (Math.abs(Math.sin(angle)) / bili + Math.abs(Math
					.cos(angle)));
			newSize.width = origW / factor;
			newSize.height = newSize.width / bili;
		} else {
			factor = (float) (Math.abs(Math.sin(angle)) * bili + Math.abs(Math
					.cos(angle)));
			newSize.height = origH / factor;
			newSize.width = newSize.height * bili;
		}
		return newSize;
	}

	class Size {
		float width;
		float height;
	}

	/**
	 * 根据连点，跟圆心的左边，算出夹角
	 * 
	 * @param curX
	 * @param curY
	 * @param preX
	 * @param preY
	 * @param centerX
	 * @param centerY
	 * @return
	 */
	private float getRawDegrees(float curX, float curY, float preX, float preY,
			float centerX, float centerY) {
		double dCur = getRadian(curX, curY, centerX, centerY);
		double dPre = getRadian(preX, preY, centerX, centerY);
		return (float) ((dPre - dCur) * 180 / Math.PI);
	}

	/**
	 * 算出一点和边的弧度
	 * 
	 * @param x
	 * @param y
	 * @param centerX
	 * @param centerY
	 * @return
	 */
	private double getRadian(float x, float y, float centerX, float centerY) {
		double radian = 0;
		y -= centerY;
		x -= centerX;
		double delt = Math.abs(y / x);
		if (y > 0 && x > 0) {
			radian = Math.atan(delt);
		} else if (y > 0 && x < 0) {
			radian = Math.PI - Math.atan(delt);
		} else if (y < 0 && x < 0) {
			radian = Math.PI + Math.atan(delt);
		} else if (y < 0 && x > 0) {
			radian = 2 * Math.PI - Math.atan(delt);
		}
		return radian;
	}

	/**
	 * 旋转固定角度
	 * 
	 * @param degrees
	 */
	private void rotate(float degrees) {
		rootDegrees += degrees;
		rootDegrees %= 360;
		// 交换宽高
		int tempV = tempBgW;
		tempBgW = tempBgH;
		tempBgH = tempV;
		// // 更新宽高比,以大的一边为基准，缩放到Canvas最大可以显示的尺寸
		// if (tempBgW > tempBgH) {// 使用W
		// fitScale = (float) cWidth / tempBgW;
		// } else {// 使用H
		// fitScale = (float) cHeight / tempBgH;
		// }
		fitScale = calculateFitScale(tempBgW, tempBgH, cWidth, cHeight);
		bili = (float) tempBgW / tempBgH;
		totalScale = fitScale;
		invalidate();
		checkTotalScale();
		invalidate();
	}

	boolean isNeedNegate = false;

	public void anticlockwiseRotate() {
		rotate(-90);
		isNeedNegate = !isNeedNegate;
	}

	public void clockwiseRotate() {
		rotate(90);
		isNeedNegate = !isNeedNegate;
	}

	/**
	 * 获取绘画Bitmap
	 * 
	 * @return
	 */
	private Bitmap getDrawBitmap() {
		if (resultBitmap == null || resultBitmap.isRecycled()) {
			return originalBitmap;
		} else {
			return resultBitmap;
		}
	}

	private Matrix matrix = new Matrix();

	/**
	 * 垂直翻转
	 */
	public void flipVertical() {
		Bitmap source = getDrawBitmap();
		if (source == null || source.isRecycled()) {
			return;
		}
		if (isNeedNegate) {
			totalDegrees = 180 - totalDegrees;
		} else {
			totalDegrees = 360 - totalDegrees;
		}
		matrix.reset();
		matrix.setScale(1, -1, cCenterX, cCenterY);
		Bitmap b = Bitmap.createBitmap(source, 0, 0, source.getWidth(),
				source.getHeight(), matrix, false);
		setupResultBitmap(b);
		invalidate();
	}

	/**
	 * 水平翻转
	 */
	public void flipHorizontal() {
		Bitmap source = getDrawBitmap();
		if (source == null || source.isRecycled()) {
			return;
		}
		if (isNeedNegate) {
			totalDegrees = 360 - totalDegrees;
		} else {
			totalDegrees = 180 - totalDegrees;
		}
		matrix.reset();
		matrix.setScale(1, -1, cCenterX, cCenterY);
		Bitmap b = Bitmap.createBitmap(source, 0, 0, source.getWidth(),
				source.getHeight(), matrix, false);
		setupResultBitmap(b);
		invalidate();
	}

	private void setupResultBitmap(Bitmap b) {
		if (resultBitmap != null && resultBitmap != originalBitmap
				&& !resultBitmap.isRecycled()) {
			resultBitmap.recycle();
		}
		resultBitmap = b;
	}

	Canvas tempCanvas;
	int temp_canvas_count_root;
	Bitmap tempResultBitmap;

	@Override
	public Canvas getMergeCanvas() {
		if (tempCanvas == null) {
			tempCanvas = new Canvas();
		}
		Bitmap b = getDrawBitmap();
		if (b != null) {
			int w = (int) (mappingRect.width() / totalScale);
			int h = (int) (mappingRect.height() / totalScale);
			tempResultBitmap = Bitmap.createBitmap(w, h, b.getConfig());
			tempCanvas.setBitmap(tempResultBitmap);
			float cX = w / 2;
			float cY = h / 2;
			temp_canvas_count_root = tempCanvas.save();
			tempCanvas.rotate(rootDegrees - totalDegrees, cX, cY);
			// tempCanvas.translate(-offsetLeft, -offsetTop);
		}
		return tempCanvas;
	}

	@Override
	public Bitmap postMergeCanvas(Canvas canvas) {
		if (tempCanvas != null)
			tempCanvas.restoreToCount(temp_canvas_count_root);
		return tempResultBitmap;
	}

	@Override
	public Layer getMergeReferLayer() {
		return this;
	}

	@Override
	public int getMergeReferLayerPriority() {
		return MERGE_NATURAL_ORDERING;
	}
}
