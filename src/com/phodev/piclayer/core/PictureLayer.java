package com.phodev.piclayer.core;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Region.Op;
import android.util.FloatMath;
import android.view.MotionEvent;

import com.phodev.piclayer.core.DotDelegate.OnClickListener;
import com.phodev.piclayer.core.DotDelegate.State;
import com.phodev.piclayer.core.DotDelegate.Type;
import com.phodev.piclayer.core.LayerParent.LayerSafeCleaner;

/**
 * 
 * @author sky
 *
 */
public class PictureLayer extends Layer {
	private Paint defaultPaint;
	private Paint picBorderPaint;
	private Bitmap originalBitmap;
	private Rect bgRect = new Rect();
	private final int bgPadding = PictureConfig.getPictureBgPadding();
	private final int bgPaddingColor = PictureConfig.picture_bg_color;
	private final float borderWidht = PictureConfig.getBackgroundBorderWidth();
	private final float harfBorderWidht = borderWidht / 2;
	private final int minWidth = 20;
	private final int minHeight = 20;
	private float maxWidth;
	private float maxHeight;

	// 放大缩小
	private Matrix scaleMatrix = new Matrix();
	private PointF start = new PointF();// touch down点的坐标
	private PointF end = new PointF();// 从start到end的点的坐标
	private PointF center = new PointF();// 圆心
	private DotDelegate dotCancle;
	private DotDelegate dotMultifunction;
	private DotDelegate pressedDot;
	RectDotRender dotRender;
	private boolean isFirstDraw = true;
	private boolean isMergeDraw = false;
	private float degrees = 0;
	private float scale = 1;
	private LayerSafeCleaner mCleaner;

	public PictureLayer(Context context) {
		this(context, null);
	}

	public PictureLayer(Context context, LayerSafeCleaner cleaner) {
		mCleaner = cleaner;
		defaultPaint = new Paint();
		picBorderPaint = new Paint();
		defaultPaint.setAntiAlias(true);
		picBorderPaint.setAntiAlias(true);
		picBorderPaint.setColor(bgPaddingColor);
		picBorderPaint.setStrokeWidth(PictureConfig.getBackgroundBorderWidth());
		dotRender = RectDotRender.getInstance(context);
		dotCancle = new DotDelegate();
		dotMultifunction = new DotDelegate();

		dotCancle.setDotType(Type.CANCLE);
		dotCancle.setOnClickListener(dotListener);
		dotMultifunction.setDotType(Type.ROTATE);
	}

	@Override
	public void setLayerResouce(Bitmap bitmap) {
		if (originalBitmap == null) {// 第一次设置的作为原始Bitmap
			originalBitmap = bitmap;
		}
		super.setLayerResouce(bitmap);
	}

	/**
	 * 根据图层信息绘画改图层
	 * 
	 * @param canvas
	 */
	public void onDraw(Canvas canvas) {
		Bitmap bmp = getDrawBtimap();
		Rect bgR;
		if (bmp != null) {
			bgR = getParent().getBackgroundLayer().getBgResourceOccupyArea();
			if (isFirstDraw) {
				if (bgR != null && !bgR.isEmpty()) {
					int bw = bmp.getWidth();
					int bh = bmp.getHeight();
					maxWidth = bgR.width() * 1.2f;
					maxHeight = bgR.height() * 1.2f;
					int sizeOffset = (int) ((borderWidht + bgPadding + dotRender
							.getRenderTargetRaduis()) * 2);
					int bgMaxW = bgR.width() - sizeOffset;
					int bgMaxH = bgR.height() - sizeOffset;
					if (bgMaxW < minWidth) {
						bgMaxW = minWidth;
					}
					if (bgMaxH < minHeight) {
						bgMaxH = minHeight;
					}
					if (bw > bgMaxW || bh > bgMaxH) {
						scale = calculateFitScale(bw, bh, bgMaxW, bgMaxH);
						scaleLayer(scale);
					}
					bmp = getDrawBtimap();
				}
				setLayerX((canvas.getWidth() - bmp.getWidth()) / 2);
				setLayerY((canvas.getHeight() - bmp.getHeight()) / 2);
				isFirstDraw = false;
			}
			float left = getLayerX();
			float top = getLayerY();
			int bW = bmp.getWidth();
			int bH = bmp.getHeight();
			int bl = (int) left;
			int bt = (int) top;
			int br = bl + bW;
			int bb = bt + bH;
			bgRect.set(bl - bgPadding, bt - bgPadding, br + bgPadding, bb
					+ bgPadding);

			dotCancle.setDotInfo(bgRect.left, bgRect.top);
			dotMultifunction.setDotInfo(bgRect.right, bgRect.bottom);

			updateDotPseudoXY(dotMultifunction, center, degrees);
			updateDotPseudoXY(dotCancle, center, degrees);

			// debugDot(canvas);
			int c_save = canvas.save();
			canvas.clipRect(bgR, Op.INTERSECT);
			canvas.rotate(degrees, bgRect.exactCenterX(), bgRect.exactCenterY());
			if (isFocused() && !isMergeDraw) {
				drawRectBorder(canvas, bgRect);
				canvas.drawBitmap(bmp, bl, bt, defaultPaint);// 话图片

				dotRender.drawDot(canvas, dotMultifunction);
				dotRender.drawDot(canvas, dotCancle);
			} else {
				canvas.drawBitmap(bmp, bl, bt, defaultPaint);// 话图片
			}
			canvas.restoreToCount(c_save);
			int radius = dotRender.getRenderTargetRaduis();
			updateRect(bgRect.left - radius, bgRect.top - radius, bgRect.right
					+ radius, bgRect.bottom);
		}
		// super.onDraw(canvas);
	}

	/**
	 * 画矩形边框
	 * 
	 * @param canvas
	 * @param rect
	 */
	private void drawRectBorder(Canvas canvas, Rect rect) {
		canvas.drawLine(rect.left, rect.top, rect.right, rect.top,
				picBorderPaint);
		canvas.drawLine(rect.right, rect.top - harfBorderWidht, rect.right,
				rect.bottom + harfBorderWidht, picBorderPaint);
		canvas.drawLine(rect.right, rect.bottom, rect.left, rect.bottom,
				picBorderPaint);
		canvas.drawLine(rect.left, rect.bottom + harfBorderWidht, rect.left,
				rect.top - harfBorderWidht, picBorderPaint);
	}

	@SuppressLint("WrongCall")
	@Override
	public void onMergeDraw(Canvas canvas, MergeRefer refer) {
		isMergeDraw = true;
		onDraw(canvas);
		isMergeDraw = false;
		// super.onMergeDraw(canvas);
	}

	/**
	 * 用来绘画的Bitmap
	 * 
	 * @return
	 */
	private Bitmap getDrawBtimap() {
		if (getLayerBitmap() == null) {
			return originalBitmap;
		} else {
			return getLayerBitmap();
		}
	}

	private boolean isTouchDownInLayer = false;

	/**
	 * Touch事件出发
	 * 
	 * @param event
	 * @return
	 */
	public boolean onTouchEvent(MotionEvent event) {
		int mx = (int) event.getX();
		int my = (int) event.getY();
		boolean state = false;
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			pressedDot = checkDot(mx, my);
			if (pressedDot != null) {
				start.set(event.getX(), event.getY());
				pressedDot.setState(State.PREESED);
				invalidate();
				state = true;
				isTouchDownInLayer = true;
			} else if (isLayerContain(mx, my)) {
				start.set(event.getX(), event.getY());
				invalidate();
				state = true;
				isTouchDownInLayer = true;
			} else {
				state = false;
				isTouchDownInLayer = false;
			}
			break;
		case MotionEvent.ACTION_UP:
			// 如果点中的是HotDot，要让Dot变成Selected状态，并响应ClipArea缩放
			DotDelegate dot = checkDot(mx, my);
			if (dot == pressedDot && dot != null) {
				dot.dispatchClick();
			}
			if (pressedDot != null) {
				pressedDot.setState(State.NORMAL);
				pressedDot = null;
			}
			invalidate();
			if (isFocused()) {
				state = true;
			} else {
				state = false;
			}
			isTouchDownInLayer = false;
			break;
		case MotionEvent.ACTION_MOVE:// 若为DRAG模式，则点击移动图片
			if (isTouchDownInLayer) {
				float mX = event.getX();
				float mY = event.getY();
				center.x = bgRect.exactCenterX();
				center.y = bgRect.exactCenterY();
				// 如果是某个点是Pressed状态，先根据移动的距离来缩放ClipArea的Height Width
				if (pressedDot == dotMultifunction) {
					end.x = mX;
					end.y = mY;
					float newSpin = getDegrees(start, end, center);
					degrees -= newSpin;
					handlerZoom(mX, mY);
					invalidate();
				} else {
					float left = getLayerX() + mX - start.x;
					float top = getLayerY() + mY - start.y;
					updateXY(left, top);
					invalidate();
				}
				start.set(mX, mY);
				state = true;
			}
			break;
		}
		return state;
	}

	@Override
	protected void onFoucsChanged(boolean focused) {
		if (!focused) {
			invalidate();
		}
	}

	private void updateXY(float left, float top) {
		// 限制边界
		// LayerParent p = getParent();
		// BaseBackgroundLayer bg;
		// if (p != null && (bg = p.getBackgroundLayer()) != null) {
		// Rect bgRect = bg.getBgResourceOccupyArea();
		// if (bgRect != null) {
		// if (left < bgRect.left) {
		// left = bgRect.left;
		// }
		// if (top < bgRect.top) {
		// top = bgRect.top;
		// }
		// Bitmap b = getDrawBtimap();
		// if (b != null) {
		// float right = left + b.getWidth();
		// float bottom = top + b.getHeight();
		// if (right > bgRect.right) {
		// left -= right - bgRect.right;
		// }
		// if (bottom > bgRect.bottom) {
		// top -= bottom - bgRect.bottom;
		// }
		// }
		// }
		// }
		setLayerX(left);
		setLayerY(top);
	}

	private void handlerZoom(float curx, float cury) {
		float nDist;
		float oDist;
		int oldWidth = bgRect.width();
		int oldHeight = bgRect.height();
		float centerX = bgRect.exactCenterX();
		float centerY = bgRect.exactCenterY();
		int newWidht;
		int newHeight;
		int dx;
		int dy;
		float curScale;
		nDist = spacing(curx, cury, centerX, centerY);
		oDist = spacing(dotMultifunction.getCX(), dotMultifunction.getCY(),
				centerX, centerY);
		curScale = nDist / oDist;
		newWidht = (int) (curScale * oldWidth);
		newHeight = (int) (curScale * oldHeight);
		dx = (newWidht - oldWidth) / 2;
		dy = (newHeight - oldHeight) / 2;
		if (newWidht >= maxWidth || newHeight >= maxHeight
				|| newWidht <= minWidth || newHeight <= minHeight) {
			return;
		}

		bgRect.inset(dx, dy);
		scale *= curScale;
		scaleLayer(scale);
	}

	/**
	 * 两点之间距离
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 */
	private float spacing(float x1, float y1, float x2, float y2) {
		float x = x1 - x2;
		float y = y1 - y2;
		return FloatMath.sqrt(x * x + y * y);
	}

	/**
	 * 缩放图层
	 * 
	 * @param scale
	 */
	private boolean scaleLayer(float scale) {
		// 缩放Bitmap
		Bitmap b = getLayerBitmap();
		Rect r = getRect();
		if (b == null || r == null)
			return false;
		int bW = b.getWidth();
		int bH = b.getHeight();
		Bitmap resizeBitmap = scaleBitmap(originalBitmap, scale);
		// 施放不在需要的Bitmap
		if (b != null && b != originalBitmap && !b.isRecycled()) {
			b.recycle();
			b = null;
		}
		if (resizeBitmap != null) {
			// 更新Layer的坐标
			setLayerX(getLayerX() - (resizeBitmap.getWidth() - bW) / 2);
			setLayerY(getLayerY() - (resizeBitmap.getHeight() - bH) / 2);
			setLayerResouce(resizeBitmap);
			return true;
		} else {
			return false;
		}
	}

	private Bitmap scaleBitmap(Bitmap b, float scale) {
		Bitmap resizeBitmap = null;
		if (b != null && !b.isRecycled()) {
			scaleMatrix.reset();
			scaleMatrix.postScale(scale, scale);
			try {
				resizeBitmap = Bitmap.createBitmap(b, 0, 0, b.getWidth(),
						b.getHeight(), scaleMatrix, false);
			} catch (Exception e) {
				return b;
			}
		}
		return resizeBitmap;
	}

	private final OnClickListener dotListener = new OnClickListener() {

		@Override
		public void onDotClick(DotDelegate dot) {
			if (dot == dotCancle) {
				LayerParent p = getParent();
				if (p != null) {
					p.removeLayer(PictureLayer.this, mCleaner);
				}
			}
		}
	};

	/**
	 * 检查是否点击了HotDot
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	private DotDelegate checkDot(float x, float y) {
		if (dotCancle.containsPseudoXY(x, y)) {
			return dotCancle;
		} else if (dotMultifunction.containsPseudoXY(x, y)) {
			return dotMultifunction;
		}
		// else if (dotRotate.contains(x, y)) {
		// return dotRotate;
		// }
		return null;
	}

	/**
	 * 判断坐标是否在Layer内
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	private boolean isLayerContain(float x, float y) {
		Rect r = getRect();
		PointF p = getPointAfterSpin(x, y, center, -degrees);
		if (r != null && p != null && r.contains((int) p.x, (int) p.y)) {
			return true;
		}
		return false;
	}

	/**
	 * 获取旋转角度
	 * 
	 * @param start
	 * @param end
	 * @param center
	 * @return
	 */
	private float getDegrees(PointF start, PointF end, PointF center) {
		float degrees = getDegrees(end.x, end.y, start.x, start.y, center.x,
				center.y);
		return degrees;
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
	private float getDegrees(float curX, float curY, float preX, float preY,
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
		if (x == 0)
			return 0;
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

	// 如果P点绕另一点P0（x0,y0）旋转β到点P1，旋转后位置计算公式如下：
	// dx = x-x0;
	// dy = y-y0;
	// x1=cos(β)*dx-sin(β)*dy+x0;
	// y1=cos(β)*dy+sin(β)*dx+y0;
	private PointF tempPoint = new PointF();

	PointF getPointAfterSpin(float x, float y, PointF center, float degrees) {
		float dx = x - center.x;
		float dy = y - center.y;
		double angle = Math.toRadians(degrees);
		float cosV = (float) Math.cos(angle);
		float sinV = (float) Math.sin(angle);
		tempPoint.x = cosV * dx - sinV * dy + center.x;
		tempPoint.y = cosV * dy + sinV * dx + center.y;
		return tempPoint;
	}

	private void updateDotPseudoXY(DotDelegate dot, PointF center, float degrees) {
		PointF tempPoint = getPointAfterSpin(dot.getCX(), dot.getCY(), center,
				degrees);
		dot.setPseudoXY(tempPoint);
	}

	// just debug//
	void debugDot(Canvas canvas) {
		Paint p = new Paint();
		p.setColor(Color.CYAN);
		p.setAlpha(50);
		canvas.drawCircle(dotMultifunction.getPseudoXY().x,
				dotMultifunction.getPseudoXY().y, 30, p);
		canvas.drawCircle(dotCancle.getPseudoXY().x, dotCancle.getPseudoXY().y,
				30, p);
	}
	// debug end//

}
