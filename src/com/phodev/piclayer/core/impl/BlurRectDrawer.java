package com.phodev.piclayer.core.impl;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Region.Op;
import android.graphics.Shader.TileMode;

import com.phodev.piclayer.core.BitmapFeather;
import com.phodev.piclayer.core.BlurLayer;
import com.phodev.piclayer.core.PictureUtils;
import com.phodev.piclayer.core.BlurLayer.BlurLayerDrawer;

/**
 * 
 * @author sky
 *
 */
public class BlurRectDrawer implements BlurLayerDrawer {
	private Paint whitePaint;
	private Paint defPaint;
	private BitmapFeather mFeather;

	private Rect mBgRect;
	private float mBgScale;
	private final int color_top_white = 0xcfffffff;
	private final int feahterRadius = 28;// 模糊边缘
	private float scaledFeahterRadius;
	private boolean isFirstDraw = true;
	private Rect rectArea = new Rect();
	private final int rectAreaDefWidth = 100;// 虚化矩形默认宽度

	public BlurRectDrawer() {
		whitePaint = new Paint();
		whitePaint.setColor(color_top_white);
		whitePaint.setAntiAlias(true);
		defPaint = new Paint();
		defPaint.setAntiAlias(true);
		mFeather = new BitmapFeather();
	}

	public void setDrawerInfo(Rect bgRect, float bgScale, int radius) {
		mBgRect = bgRect;
		mBgScale = bgScale;
		scaledFeahterRadius = feahterRadius * mBgScale;
		// minOffsetLeft = bgRect.left;
		// maxOffsetLeft = bgRect.right;
	}

	private Paint shaderPaint = new Paint();
	private float cCenterX;
	private float cCenterY;
	private float totalDegrees = 90;// 默认旋转90度

	@Override
	public void drawPreview(Canvas canvas, BlurLayer refer) {
		if (isFirstDraw) {
			int cw = canvas.getWidth();
			int ch = canvas.getHeight();
			int a = (int) Math.sqrt(cw * cw + ch * ch) + 1;
			int rectW = rectAreaDefWidth;// 矩形宽度
			int rectH = a;
			int left = (cw - rectW) / 2;
			int top = ch - a;
			int right = (cw - rectW) / 2 + rectW;
			int bottom = (ch - rectH) / 2 + rectH;
			rectArea.set(left, top, right, bottom);
			cCenterX = cw / 2;
			cCenterY = ch / 2;
			// 配置Path
			setupPath(rectArea);
			centerP.set(cCenterX, cCenterY);
			spinRectPath(totalDegrees);
			//
			isFirstDraw = false;
		}
		shaderPaint.setShader(getLinearGradient());
		int c_count = canvas.save();
		canvas.clipRect(mBgRect, Op.INTERSECT);
		canvas.drawPath(getRectPath(), shaderPaint);
		//
		canvas.clipPath(getRectPath(), Op.DIFFERENCE);
		canvas.drawColor(color_top_white);
		canvas.restoreToCount(c_count);
	}

	@Override
	public Bitmap drawResult(Canvas canvas, int[] resPixs, Bitmap result,
			BlurLayer refer) {
		int left = mBgRect.left;
		int top = mBgRect.top;
		int sourceW = result.getWidth();
		int sourceH = result.getHeight();
		float offsetLeft = left / mBgScale;
		float offsetTop = top / mBgScale;
		scalePoint(tempAp, ap, mBgScale, offsetLeft, offsetTop);
		scalePoint(tempBp, bp, mBgScale, offsetLeft, offsetTop);
		scalePoint(tempCp, cp, mBgScale, offsetLeft, offsetTop);
		scalePoint(tempDp, dp, mBgScale, offsetLeft, offsetTop);
		mFeather.makeFeahter(resPixs, sourceW, sourceH, tempAp, tempBp, tempCp,
				tempDp, feahterRadius, 2);
		result.setPixels(resPixs, 0, sourceW, 0, 0, sourceW, sourceH);
		int c_count = canvas.save();
		canvas.clipPath(getClipPath(), Op.DIFFERENCE);
		canvas.translate(left, top);
		canvas.scale(mBgScale, mBgScale);
		canvas.drawBitmap(result, 0, 0, defPaint);
		canvas.restoreToCount(c_count);
		return result;
	}

	@Override
	public int getAreaFakeSize() {
		return rectArea.width();
	}

	@Override
	public void updateAreaFakeSize(float newSize) {
		int add = (int) (newSize - rectArea.width()) / 2;
		rectArea.left -= add;
		rectArea.right += add;
		mirrorSize = rectArea.width() / 2;
		//
		setupPath(rectArea);
		spinRectPath(totalDegrees);
	}

	// private float rectLeftOffset = 0;
	// private int maxOffsetLeft;
	// private int minOffsetLeft;

	@Override
	public void updateAreaLocation(float xMoved, float yMoved) {
		moveRectPath(xMoved, yMoved);
		// // 只移动x轴
		// // rectArea.offset((int) xMoved, 0);
		// rectLeftOffset += xMoved;
		// int offsetW = rectArea.width() / 2;
		// int minLeft = minOffsetLeft - offsetW;
		// int maxLeft = maxOffsetLeft + offsetW;
		// if (rectLeftOffset < minLeft) {
		// // 最小是minLeft
		// Log.d("", "rectLeftOffset < minLeft--->minOffsetLeft"
		// + minOffsetLeft + "  maxOffsetLeft:" + maxOffsetLeft);
		// rectLeftOffset = minLeft;
		// } else if (rectLeftOffset > maxLeft) {
		// Log.d("", "rectLeftOffset > maxLeft--->minOffsetLeft"
		// + minOffsetLeft + "  maxOffsetLeft:" + maxOffsetLeft);
		//
		// rectLeftOffset = maxLeft;
		// }
	}

	@Override
	public void updateAreaDegrees(float degreesAdd) {
		spinRectPath(degreesAdd);
		totalDegrees += degreesAdd;
		totalDegrees %= 360;
		if (totalDegrees < 0) {
			totalDegrees += 360;
		}
	}

	private LinearGradient getLinearGradient() {
		float startX = (bp.x - ap.x) / 2 + ap.x;
		float startY = (bp.y - ap.y) / 2 + ap.y;
		LinearGradient linearGradient = new LinearGradient(startX, startY,
				ap.x, ap.y, colors, getRadialColorWeight(), TileMode.MIRROR);
		return linearGradient;
	}

	// 渐变颜色比例划分
	private float[] colorWeight = new float[] { 0, 0, 0, 1 };
	private final int[] colors = new int[] { Color.TRANSPARENT,
			Color.TRANSPARENT, 0x0feeeeee, 0xdfffffff };
	private int mirrorSize = 50;

	private float[] getRadialColorWeight() {
		float length1 = mirrorSize - feahterRadius;
		if (length1 > 0) {
			colorWeight[1] = length1 / mirrorSize;
			colorWeight[2] = (length1 + (float) feahterRadius / 3) / mirrorSize;
		}
		return colorWeight;
	}

	PointF centerP = new PointF();
	PointF rectCenterP = new PointF();
	PointF ap = new PointF();
	PointF bp = new PointF();
	PointF cp = new PointF();
	PointF dp = new PointF();
	PointF tempAp = new PointF();
	PointF tempBp = new PointF();
	PointF tempCp = new PointF();
	PointF tempDp = new PointF();

	Path rectAreaPath = new Path();
	Path clipPath = new Path();
	Rect clipRect = new Rect();

	private Path getRectPath() {
		return rectAreaPath;
	}

	/**
	 * 设置Path初始信息
	 * 
	 * @param rect
	 */
	private void setupPath(Rect rect) {
		ap.set(rect.left, rect.top);
		bp.set(rect.right, rect.top);
		cp.set(rect.right, rect.bottom);
		dp.set(rect.left, rect.bottom);
		refreshRectPath();
	}

	/**
	 * 平移Path
	 * 
	 * @param xDist
	 * @param yDist
	 * @return
	 */
	private Path moveRectPath(float xDist, float yDist) {
		float totalDegrees = this.totalDegrees + 270;
		totalDegrees %= 360;
		float k = PictureUtils.getSlope(ap.x, ap.y, dp.x, dp.y);
		if (k == 0) {
			offsetPathPoint(0, yDist);
		} else if (Float.isNaN(k)) {
			offsetPathPoint(xDist, 0);
		} else {
			float sk = -1 / k;
			if (Math.abs(sk) > 0 && Math.abs(sk) <= 1) {
				yDist = xDist * sk;
			} else {
				xDist = yDist / sk;
			}
			offsetPathPoint(xDist, yDist);
		}
		return refreshRectPath();
	}

	/**
	 * 旋转Path
	 * 
	 * @param degrees
	 * @return
	 */
	private Path spinRectPath(float degrees) {
		// 旋转一定角度，更新矩形四个点的坐标
		PictureUtils.spinPoint(ap, centerP, degrees);
		PictureUtils.spinPoint(bp, centerP, degrees);
		PictureUtils.spinPoint(cp, centerP, degrees);
		PictureUtils.spinPoint(dp, centerP, degrees);
		return refreshRectPath();
	}

	private Path refreshRectPath() {
		PictureUtils.midPoint(rectCenterP, ap.x, ap.y, cp.x, cp.y);
		rectAreaPath.reset();
		rectAreaPath.moveTo(ap.x, ap.y);
		rectAreaPath.lineTo(bp.x, bp.y);
		rectAreaPath.lineTo(cp.x, cp.y);
		rectAreaPath.lineTo(dp.x, dp.y);
		rectAreaPath.lineTo(ap.x, ap.y);
		return rectAreaPath;
	}

	private void offsetPathPoint(float dx, float dy) {
		ap.offset(dx, dy);
		bp.offset(dx, dy);
		cp.offset(dx, dy);
		dp.offset(dx, dy);
	}

	public void scalePoint(PointF result, PointF target, float scale,
			float leftOffset, float topOffset) {
		result.x = target.x / scale - leftOffset;
		result.y = target.y / scale - topOffset;
	}

	/**
	 * 裁减掉不需要的模糊区域
	 */
	private Path getClipPath() {
		float x1, y1;
		float x2, y2;
		float x3, y3;
		float x4, y4;
		PointF temp;
		float r = scaledFeahterRadius;
		temp = getOffsetPoint(ap, bp, r);
		x1 = temp.x;
		y1 = temp.y;
		temp = getOffsetPoint(bp, ap, r);
		x2 = temp.x;
		y2 = temp.y;
		temp = getOffsetPoint(cp, dp, r);
		x3 = temp.x;
		y3 = temp.y;
		temp = getOffsetPoint(dp, cp, r);
		x4 = temp.x;
		y4 = temp.y;
		clipPath.reset();
		clipPath.moveTo(x1, y1);
		clipPath.lineTo(x2, y2);
		clipPath.lineTo(x3, y3);
		clipPath.lineTo(x4, y4);
		clipPath.lineTo(x1, y1);
		return clipPath;
	}

	private PointF tempP = new PointF();

	private PointF getOffsetPoint(PointF ap, PointF bp, float offset) {
		// 平移向量起始点坐标
		float dX = ap.x - bp.x;
		float dY = ap.y - bp.y;
		float distAB = (float) Math.sqrt(dX * dX + dY * dY);
		float d = offset / distAB;
		tempP.x = ap.x - d * dX;
		tempP.y = ap.y - d * dY;
		return tempP;
	}

}
