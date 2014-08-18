package com.phodev.piclayer.core;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Region.Op;
import android.view.MotionEvent;

/**
 * 图层
 * 
 * @author sky
 * 
 */
public class ClipLayer extends Layer {
	private Paint paintClipAera;
	private Paint paintLineBorder;
	private Paint paintLineInner;
	private Paint paintText;// 文字Paint
	private Rect clipAreaRect = new Rect();
	private Rect layerRect = new Rect();
	private boolean clipAreaChanged = false;
	private final int size_info_text_size = PictureConfig.getClipAreaTextSize();
	private final int text_color = PictureConfig.clip_area_text_color;
	private final int clip_area_color = PictureConfig.clip_area_color;
	private final int minClipAeraW = PictureConfig.clip_layer_min_w;
	private final int minClipAeraH = PictureConfig.clip_layer_min_h;
	final float line_border_widht = PictureConfig.getClipAreaBorderLineWidht();
	final float line_inner_widht = PictureConfig.getClipAreaInnerLineWidht();
	private final int line_border_color = PictureConfig.clip_area_grid_border_line_color;
	private final int line_inner_color = PictureConfig.clip_area_grid_inner_line_color;
	private boolean mIsRatioRestrain = PictureConfig.clip_area_scale_restrain;// 是否使用固定比例
	private float mRestrainRatio = PictureConfig.clip_area_w_h_scale;// 固定比例
	private boolean isFirstDraw = true;

	public ClipLayer(Context context) {
		paintClipAera = new Paint();
		paintClipAera.setColor(clip_area_color);

		paintLineBorder = new Paint();
		paintLineBorder.setColor(line_border_color);
		paintLineBorder.setStrokeWidth(line_border_widht);

		paintLineInner = new Paint();
		paintLineInner.setColor(line_inner_color);
		paintLineInner.setStrokeWidth(line_inner_widht);

		paintText = new Paint();
		paintText.setColor(text_color);
		paintText.setTextSize(size_info_text_size);
		paintText.setTextAlign(Paint.Align.CENTER);
		paintText.setAntiAlias(true);
		dotRender = RectDotRender.getInstance(context);
	}

	// /**
	// * 设置Clip区域的尺寸
	// *
	// * @see #invalidate()后生效
	// * @param widht
	// * @param height
	// */
	// public void setClipAreaSize(int widht, int height) {
	// clipW = widht;
	// clipH = height;
	// }
	//
	// /**
	// * 设置Clip区域的开始坐标
	// *
	// * @see #invalidate()后生效
	// * @param startX
	// * @param startY
	// */
	// public void setClipAreaOrdinate(int startX, int startY) {
	// clipAreaX = startX;
	// clipAreaY = startY;
	// }

	/**
	 * 自动调整坐标
	 */
	public void resetClipAreaAuto() {
		clipAreaChanged = true;
		isFirstDraw = true;
		invalidate();
	}

	/**
	 * 是否使用固定比例
	 * 
	 * @param isRatioRestrain
	 */
	public void setScaleState(boolean isRatioRestrain) {
		if (mRestrainRatio <= 0) {
			mRestrainRatio = 1;
		}
		mIsRatioRestrain = isRatioRestrain;
		resetClipAreaAuto();
	}

	/**
	 * 设置比例约束
	 * 
	 * @param restrainRatio
	 */
	public void setScaleRestrain(float restrainRatio) {
		mRestrainRatio = restrainRatio;
		setScaleState(true);
	}

	private BackgroundLayer clipContentLayer;// 准备被裁减的Layer

	/**
	 * 设置被剪切的Layer
	 */
	public void setClipContentLayer(BackgroundLayer layer) {
		clipContentLayer = layer;
	}

	/**
	 * 可裁减区域的Rect
	 * 
	 * @return
	 */
	private Rect getClipAbleRect() {
		if (clipContentLayer == null)
			return null;
		return clipContentLayer.getBgResourceOccupyArea();
	}

	// public void setLayerParams(ClipLayerParams lp) {
	// if (lp != null) {
	// clipAreaX = lp.clipAreaX;
	// clipAreaY = lp.clipAreaY;
	// clipW = lp.clipW;
	// clipH = lp.clipH;
	// invalidate();
	// }
	// }

	// public class ClipLayerParams {
	// private float clipAreaX;
	// private float clipAreaY;
	// private int clipW;
	// private int clipH;
	// }
	/**
	 * 根据图层信息绘画改图层
	 * 
	 * @param canvas
	 */
	public void onDraw(Canvas canvas) {
		Rect clipableRect = getClipAbleRect();
		if (clipableRect == null)
			return;
		final Rect r = clipAreaRect;
		if (isFirstDraw) {
			int bgW = clipableRect.width();
			int bgH = clipableRect.height();
			int tW, tH, tStartX, tStartY;
			if (bgW > bgH && bgW / bgH >= 3) {
				tW = bgW / 2;
				tH = bgH;
			} else if (bgW < bgH && bgH / bgW >= 3) {
				tW = bgW;
				tH = bgH / 2;
			} else {
				tW = bgW / 2;
				tH = bgH / 2;
			}
			if (mIsRatioRestrain) {
				if (tW / tH > mRestrainRatio) {
					tW = (int) (tH * mRestrainRatio);
				} else {
					tH = (int) (tW / mRestrainRatio);
				}
			}
			tStartX = (canvas.getWidth() - tW) / 2;
			tStartY = (canvas.getHeight() - tH) / 2;
			clipAreaRect.set(tStartX, tStartY, tStartX + tW, tStartY + tH);
			isFirstDraw = false;
		}

		checkClipAreaBoundary(r, clipableRect);

		// 更新Layer的Rect
		int readerR = dotRender.getRenderTargetRaduis();
		int lLeft = r.left - readerR;
		int lTop = r.top - readerR;
		int lRight = r.right + readerR;
		int lBottom = r.bottom + readerR;

		layerRect.set(lLeft, lTop, lRight, lBottom);
		drawClipAera(canvas);

		super.onDraw(canvas);
	}

	@Override
	public Rect getRect() {
		return layerRect;
	}

	@Override
	public Rect getDirtyRect() {
		return layerRect;
	}

	public Rect getClipAreaRect() {
		return clipAreaRect;
	}

	private float anchorX;
	private float anchorY;

	/**
	 * Touch事件出发
	 * 
	 * @param event
	 * @return
	 */
	public boolean onTouchEvent(MotionEvent event) {
		int x = (int) event.getX();
		int y = (int) event.getY();
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (getRect() != null && getRect().contains(x, y)) {
				updateAnchor(event);
				// 如果点中的是HotDot，要让Dot变成Selected状态，并响应ClipArea缩放
				pressedDot = checkDot(anchorX, anchorY);
				if (pressedDot != null) {
					isDotPressed = true;
					invalidate();
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			pressedDot = null;
			isDotPressed = false;
			invalidate();
			break;
		case MotionEvent.ACTION_MOVE:
			// 如果是某个点是Pressed状态，先根据移动的距离来缩放ClipArea的Height Width
			if (isDotPressed) {
				// 缩放ClipAera
				updateClipArea(pressedDot, x, y);
				invalidate();
			} else if (getRect() != null && getRect().contains(x, y)) {
				clipAreaRect.offset((int) (x - anchorX), (int) (y - anchorY));
				invalidate();
			}
			updateAnchor(event);
			break;
		}
		// 拦截所有Touch处理，ClipLayer不希望其他同事存在的Layer处理，也不会存在需要同时处理的情况
		return true;
	}

	/**
	 * 跟心锚点
	 * 
	 * @param e
	 */
	private void updateAnchor(MotionEvent e) {
		anchorX = e.getX();
		anchorY = e.getY();
	}

	/**
	 * 绘画Area
	 * 
	 * @param canvas
	 */
	private void drawClipAera(Canvas canvas) {
		int cTime = canvas.save();
		canvas.clipRect(getClipAbleRect());
		canvas.clipRect(clipAreaRect, Op.XOR);
		canvas.drawColor(PictureConfig.clip_layer_bg_color);
		canvas.restoreToCount(cTime);

		canvas.drawRect(clipAreaRect, paintClipAera);
		int clLeft = clipAreaRect.left;
		int clTop = clipAreaRect.top;
		int clW = clipAreaRect.width();
		int clH = clipAreaRect.height();
		// 画边缘的四条线
		canvas.drawLine(clLeft, clTop, clLeft + clW, clTop, paintLineBorder);
		canvas.drawLine(clLeft, clTop, clLeft, clTop + clH, paintLineBorder);
		canvas.drawLine(clLeft + clW, clTop + clH, clLeft, clTop + clH,
				paintLineBorder);
		canvas.drawLine(clLeft + clW, clTop + clH, clLeft + clW, clTop,
				paintLineBorder);

		// ------------------------画网格 Start---------------------------//
		int xInterval = clW / 3;
		int yInterval = clH / 3;
		int startX, startY, endX, endY;
		startX = clLeft + xInterval;
		startY = clTop;
		endX = startX;
		endY = clTop + clH;
		canvas.drawLine(startX, startY, endX, endY, paintLineInner);// 竖线1

		startX = clLeft + xInterval * 2;
		// startY = ;//start y不变
		endX = startX;
		// endY =;//end y不变
		canvas.drawLine(startX, startY, endX, endY, paintLineInner);// 竖线2

		startX = clLeft;
		startY = clTop + yInterval;
		endX = clLeft + clW;
		endY = startY;
		canvas.drawLine(startX, startY, endX, endY, paintLineInner);// 横线1

		// startX =;//x 不变即left不变
		startY = clTop + yInterval * 2;
		// endX =;//end x 不变
		endY = startY;
		canvas.drawLine(startX, startY, endX, endY, paintLineInner);// 横线2
		// ------------------------画网格 End---------------------------//

		// 画Size Text
		updateClipSizeInfoStrCahce();
		if (sizeStrCache != null) {
			int ssX = clW / 2 + clLeft;
			int ssY = (clH + size_info_text_size) / 2 + clTop;
			canvas.drawText(sizeStrCache, ssX, ssY, paintText);
		}
		// 画四个点
		dotT_Left.setDotInfo(clLeft, clTop);
		dotT_Right.setDotInfo(clLeft + clW, clTop);
		dotB_Left.setDotInfo(clLeft, clTop + clH);
		dotB_Right.setDotInfo(clLeft + clW, clTop + clH);
		dotRender.drawDotNormal(canvas, dotT_Left);
		dotRender.drawDotNormal(canvas, dotT_Right);
		dotRender.drawDotNormal(canvas, dotB_Left);
		dotRender.drawDotNormal(canvas, dotB_Right);
		if (pressedDot != null) {
			dotRender.drawDotSelected(canvas, pressedDot);
		}
	}

	private StringBuilder strBuilder = new StringBuilder();
	private final char asterisk = PictureConfig.pictuer_size_asterisk;
	// 四个点的信息
	private DotDelegate dotT_Left = new DotDelegate(DotDelegate.TOP_LEFT);
	private DotDelegate dotT_Right = new DotDelegate(DotDelegate.TOP_RIGHT);
	private DotDelegate dotB_Left = new DotDelegate(DotDelegate.BOTTOM_LEFT);
	private DotDelegate dotB_Right = new DotDelegate(DotDelegate.BOTTOM_RIGHT);
	private DotDelegate pressedDot;
	private boolean isDotPressed = false;
	private RectDotRender dotRender;
	private String sizeStrCache;

	/**
	 * 获取当前ClipArea映射的尺寸大小
	 * 
	 * @return
	 */
	private String updateClipSizeInfoStrCahce() {
		if (clipAreaChanged || sizeStrCache == null) {
			int w = 0;
			int h = 0;
			if (clipContentLayer != null) {
				float scale = clipContentLayer.getBackgroundScale();
				w = (int) (clipAreaRect.width() / scale);
				h = (int) (clipAreaRect.height() / scale);
			}
			int l = strBuilder.length();
			if (l > 0)
				strBuilder.delete(0, l);
			strBuilder.append(w);
			strBuilder.append(asterisk);
			strBuilder.append(h);
			sizeStrCache = strBuilder.toString();
			clipAreaChanged = false;
		}
		return sizeStrCache;
	}

	/**
	 * 检查是否点击了HotDot
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	private DotDelegate checkDot(float x, float y) {
		if (dotT_Left.contains(x, y)) {
			return dotT_Left;
		} else if (dotT_Right.contains(x, y)) {
			return dotT_Right;
		} else if (dotB_Left.contains(x, y)) {
			return dotB_Left;
		} else if (dotB_Right.contains(x, y)) {
			return dotB_Right;
		}
		return null;
	}

	/**
	 * 检查边界
	 * 
	 * @param clipAreaR
	 * @param clipableR
	 */
	private void checkClipAreaBoundary(Rect clipAreaR, Rect clipableR) {
		int clipW = clipAreaR.width();
		int clipH = clipAreaR.height();
		// 如果ClipArea区域大于指定的区域首相让用指定的区域尺寸替换
		if (clipW > clipableR.width()) {
			clipAreaR.right = clipableR.right;
			clipAreaR.left = clipableR.left;
		}
		if (clipH > clipableR.height()) {
			clipAreaR.top = clipableR.top;
			clipAreaR.bottom = clipableR.bottom;
		} else if (clipH < PictureConfig.clip_layer_min_h) {
			clipAreaR.bottom = PictureConfig.clip_layer_min_h;
		}
		// 判断起始XY不能超过Background有效图片边界
		if (clipAreaR.left < clipableR.left) {
			clipAreaR.right += clipableR.left - clipAreaR.left;
			clipAreaR.left = clipableR.left;
		}
		if (clipAreaR.top < clipableR.top) {
			clipAreaR.bottom += clipableR.top - clipAreaR.top;
			clipAreaR.top = clipableR.top;
		}
		if (clipAreaR.right > clipableR.right) {
			clipAreaR.left -= clipAreaR.right - clipableR.right;
			clipAreaR.right = clipableR.right;
		}
		if (clipAreaR.bottom > clipableR.bottom) {
			clipAreaR.top -= clipAreaR.bottom - clipableR.bottom;
			clipAreaR.bottom = clipableR.bottom;
		}
	}

	/**
	 * 改变ClipArea尺寸
	 * 
	 * @param pressedDot
	 * @param curX
	 * @param curY
	 */
	private void updateClipArea(DotDelegate pressedDot, float curX, float curY) {
		Rect r = clipAreaRect;
		Rect rClipAble = getClipAbleRect();
		// 内部调用不做判断
		// if (pressedDot != null && r != null && rClipAble != null)
		if (curX < rClipAble.left) {
			curX = rClipAble.left;
		} else if (curX > rClipAble.right) {
			curX = rClipAble.right;
		}
		if (curY < rClipAble.top) {
			curY = rClipAble.top;
		} else if (curY > rClipAble.bottom) {
			curY = rClipAble.bottom;
		}
		int mX = (int) curX;
		int mY = (int) curY;
		int dotX = (int) pressedDot.getCX();
		int dotY = (int) pressedDot.getCY();
		int absX = Math.abs(dotX - mX);
		int absY = Math.abs(dotY - mY);
		int absScale;
		if (absX == 0 || absY == 0) {
			return;
		} else {
			absScale = absX / absY;
		}
		int rw;
		int rh;
		switch (pressedDot.getDotPosition()) {
		case DotDelegate.TOP_LEFT:// 左上角
			if (mIsRatioRestrain) {
				if (absScale < mRestrainRatio) {
					// use x
					r.left = mX;
					rw = r.width();
					rh = (int) (rw / mRestrainRatio);
					r.top -= rh - r.height();
				} else {
					// use y
					r.top = mY;
					rh = r.height();
					rw = (int) (rh * mRestrainRatio);
					r.left -= rw - r.width();
				}
				// 如果小于最小尺寸，用最小尺寸反过来求高度
				if (r.width() < minClipAeraW) {
					r.left -= minClipAeraW - r.width();
					rh = (int) (r.width() / mRestrainRatio);
					r.top -= rh - r.height();
				}
			} else {
				r.left = mX;
				r.top = mY;
				// 如果小于最小尺寸，用最小尺寸反过来求高度
				if (r.width() < minClipAeraW) {
					r.left -= minClipAeraW - r.width();
				}
				if (r.height() < minClipAeraH) {
					r.top -= minClipAeraH - r.height();
				}
			}
			break;
		case DotDelegate.TOP_RIGHT:// 右上角
			if (mIsRatioRestrain) {
				if (absScale < mRestrainRatio) {
					// use x
					r.right = mX;
					rw = r.width();
					rh = (int) (rw / mRestrainRatio);
					r.top -= rh - r.height();
				} else {
					// use y
					r.top = mY;
					rh = r.height();
					rw = (int) (rh * mRestrainRatio);
					r.right += rw - r.width();
				}
				// 如果小于最小尺寸，用最小尺寸反过来求高度
				if (r.width() < minClipAeraW) {
					r.right += minClipAeraW - r.width();
					rh = (int) (r.width() / mRestrainRatio);
					r.top -= rh - r.height();
				}
			} else {
				r.right = mX;
				r.top = mY;
				// 如果小于最小尺寸，用最小尺寸反过来求高度
				if (r.width() < minClipAeraW) {
					r.right += minClipAeraW - r.width();
				}
				if (r.height() < minClipAeraH) {
					r.top -= minClipAeraH - r.height();
				}
			}
			break;
		case DotDelegate.BOTTOM_LEFT:// 左下角
			if (mIsRatioRestrain) {
				if (absScale < mRestrainRatio) {
					// use x
					r.left = mX;
					rw = r.width();
					rh = (int) (rw / mRestrainRatio);
					r.bottom += rh - r.height();
				} else {
					// use y
					r.bottom = mY;
					rh = r.height();
					rw = (int) (rh * mRestrainRatio);
					r.left -= rw - r.width();
				}
				// 如果小于最小尺寸，用最小尺寸反过来求高度
				if (r.width() < minClipAeraW) {
					r.left -= minClipAeraW - r.width();
					rh = (int) (r.width() / mRestrainRatio);
					r.bottom += rh - r.height();
				}
			} else {
				r.left = mX;
				r.bottom = mY;
				if (r.width() < minClipAeraW) {
					r.left -= minClipAeraW - r.width();
				}
				if (r.height() < minClipAeraH) {
					r.bottom += minClipAeraH - r.height();
				}
			}
			break;
		case DotDelegate.BOTTOM_RIGHT:// 右下角
			if (mIsRatioRestrain) {
				if (absScale < mRestrainRatio) {
					// use x
					r.right = mX;
					rw = r.width();
					rh = (int) (rw / mRestrainRatio);
					r.bottom += rh - r.height();
				} else {
					// use y
					r.bottom = mY;
					rh = r.height();
					rw = (int) (rh * mRestrainRatio);
					r.right += rw - r.width();
				}
				// 如果小于最小尺寸，用最小尺寸反过来求高度
				if (r.width() < minClipAeraW) {
					r.right += minClipAeraW - r.width();
					rh = (int) (r.width() / mRestrainRatio);
					r.bottom += rh - r.height();
				}
			} else {
				r.right = mX;
				r.bottom = mY;
				if (r.width() < minClipAeraW) {
					r.right += minClipAeraW - r.width();
				}
				if (r.height() < minClipAeraH) {
					r.bottom += minClipAeraH - r.height();
				}
			}
			break;
		}
		clipAreaChanged = true;
	}
}
