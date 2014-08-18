package com.phodev.piclayer.core;

import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Region.Op;
import android.os.Handler;
import android.os.Message;
import android.util.FloatMath;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.phodev.piclayer.core.DotDelegate.OnClickListener;
import com.phodev.piclayer.core.DotDelegate.State;
import com.phodev.piclayer.core.DotDelegate.Type;
import com.phodev.piclayer.core.LayerInputHolder.LayerInputCallBack;
import com.phodev.piclayer.core.LayerParent.LayerSafeCleaner;

/**
 * @zxj
 * @author sky
 */
public class TextLayer extends Layer implements LayerInputCallBack {
	private Context mContext;
	private boolean isEdit = false;
	private Bitmap src;
	private Paint defPaint;
	private Paint paintCursor;
	private Paint paintLine;
	private Rect layerRect = new Rect();
	private Rect textRect = new Rect();
	private boolean isFirst;
	private int left = 0, top = 0;
	private InputMethodManager imm;
	private int textsize = 45;
	private Timer timer;
	private MyTimerTask task;
	private boolean light;
	private int AUTO_SHOW = 1000;
	private boolean isshow = false;
	TextUtil util;
	private float scale = 1;
	private float pre_scale = 1;
	private Matrix scaleMatrix = new Matrix();
	private Bitmap originalBitmap;
	private boolean isFirstDraw = true;
	private DotDelegate dot_Cancel = new DotDelegate();
	private DotDelegate dot_Rotate = new DotDelegate();
	private DotDelegate pressedDot;
	private RectDotRender dotRender;
	private PointF start = new PointF();// touch down点的坐标
	private PointF end = new PointF();// 从start到end的点的坐标
	private PointF center = new PointF();// 圆心
	private float degrees = 0;
	private int offset = 40;// 以100为基准的offset
	private float defaultWidth = (float) (400 / 1.5);// 默认宽度
	private int textColor;
	private LayerSafeCleaner mCleaner;
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == AUTO_SHOW) {
				invalidate();
				startTimer();
			}
		}
	};;

	public TextLayer(Context context, LayerSafeCleaner cleaner, Bitmap src,
			int textColor) {
		this.mContext = context;
		this.mCleaner = cleaner;
		this.src = src;
		this.textColor = textColor;
		defPaint = new Paint();
		defPaint.setAntiAlias(true);

		paintCursor = new Paint();
		paintCursor.setColor(Color.BLUE);
		paintCursor.setStrokeWidth(2);
		paintCursor.setAntiAlias(true);

		paintLine = new Paint();
		paintLine.setAntiAlias(true);
		paintLine.setColor(Color.WHITE);
		paintLine.setStrokeWidth(2);

		imm = (InputMethodManager) mContext
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		timer = new Timer();
		// startTimer();

		float density = context.getResources().getDisplayMetrics().density;
		defaultWidth = (int) (defaultWidth * density);
		offset = (int) (offset * density);

		dotRender = RectDotRender.getInstance(context);
		setLayerResouce(src);
		setOnLayerClickListener(onClickListener);
		dot_Cancel.setDotType(Type.CANCLE);
		dot_Rotate.setDotType(Type.ROTATE);
		dot_Cancel.setOnClickListener(dotListener);
	}

	public void resetTextAreaAuto() {
		isFirst = true;
		invalidate();
	}

	@Override
	public void setLayerResouce(Bitmap bitmap) {
		if (originalBitmap == null) {// 第一次设置的作为原始Bitmap
			originalBitmap = bitmap;
		}
		super.setLayerResouce(bitmap);
	}

	int canvasCenterX;
	int canvasCenterY;

	@Override
	public void onDraw(Canvas canvas) {
		Bitmap bmp = getDrawBtimap();
		Rect bgR;
		if (bmp == null || bmp.isRecycled())
			return;
		bgR = getParent().getBackgroundLayer().getBgResourceOccupyArea();
		if (isFirstDraw) {
			// float scale = 1;
			if (bgR != null && !bgR.isEmpty()) {

				int bw = bmp.getWidth();
				int bh = bmp.getHeight();
				if (bw > bgR.width() || bh > bgR.height()) {
					if (bgR.width() / bw < bgR.height() / bh) {// 使用W
						pre_scale = (float) bgR.width() / bw;
					} else {// 使用H
						pre_scale = (float) bgR.height() / bh;
					}
					Bitmap b = scaleBitmap(originalBitmap, pre_scale);
					if (originalBitmap != null && !originalBitmap.isRecycled()) {
						originalBitmap.recycle();
					}
					originalBitmap = b;
					setLayerResouce(b);
				}
				bmp = getDrawBtimap();
			}
			canvasCenterX = canvas.getWidth() / 2;
			canvasCenterY = canvas.getHeight() / 2;
			setLayerX((canvas.getWidth() - bmp.getWidth()) / 2);
			setLayerY((canvas.getHeight() - bmp.getHeight()) / 2);

			// 使用默认尺寸
			// scale = (float) defaultWidth / bmp.getWidth();
			// scaleLayer(scale);
			// bmp = getDrawBtimap();
			//
			util = new TextUtil("", (defaultWidth - offset * 2) * pre_scale,
					(defaultWidth - offset * 2) * pre_scale, textColor,
					textsize * pre_scale);
			isFirstDraw = false;
		}

		int width = bmp.getWidth();
		int height = bmp.getHeight();
		float left = getLayerX();
		float top = getLayerY();
		int l = (int) left;
		int t = (int) top;
		int r = l + width;
		int b = t + height;
		layerRect.set(l, t, r, b);
		int c_count = canvas.save();
		canvas.clipRect(bgR, Op.INTERSECT);
		canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
				| Paint.FILTER_BITMAP_FLAG));
		canvas.rotate(degrees, bgR.exactCenterX(), bgR.exactCenterY());
		canvas.drawBitmap(bmp, left, top, defPaint);
		if (isFocused() && !isMergeDraw) {
			canvas.drawLine(left, top, left + width, top, paintLine);
			canvas.drawLine(left, top, left, top + height, paintLine);
			canvas.drawLine(left + width, top + height, left, top + height,
					paintLine);
			canvas.drawLine(left + width, top + height, left + width, top,
					paintLine);
			dot_Cancel.setDotInfo(left, top);
			dot_Rotate.setDotInfo(left + width, top + height);
			updateDotPseudoXY(dot_Rotate, center, degrees);
			updateDotPseudoXY(dot_Cancel, center, degrees);
			dotRender.drawDot(canvas, dot_Cancel);
			dotRender.drawDot(canvas, dot_Rotate);
		}
		// 缩放画布
		int c_count_2 = canvas.save();
		canvas.scale(scale, scale);
		if (isMergeDraw || !isFocused())
			light = false;
		util.DrawText(canvas, left / scale + offset * pre_scale, top / scale
				+ offset * pre_scale, light);
		canvas.restoreToCount(c_count_2);
		canvas.restoreToCount(c_count);
		int radius = dotRender.getRenderTargetRaduis();
		updateRect(layerRect.left - radius, layerRect.top - radius,
				layerRect.right + radius, layerRect.bottom);
	}

	private boolean isMergeDraw = false;

	@SuppressLint("WrongCall")
	@Override
	public void onMergeDraw(Canvas canvas, MergeRefer refer) {
		isMergeDraw = true;
		onDraw(canvas);
		isMergeDraw = false;
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}

	private void updateDotPseudoXY(DotDelegate dot, PointF center, float degrees) {
		PointF tempPoint = getPointAfterSpin(dot.getCX(), dot.getCY(), center,
				degrees);
		dot.setPseudoXY(tempPoint);
	}

	@Override
	public Rect getRect() {
		return layerRect;
	}

	@Override
	public Rect getDirtyRect() {
		return layerRect;
	}

	public Rect getTextRect() {
		return textRect;
	}

	@Override
	protected void onFoucsChanged(boolean focused) {
		if (!focused) {
			invalidate();
		}
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

	/**
	 * 检查是否点击了HotDot
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	private DotDelegate checkDot(float x, float y) {
		if (dot_Cancel.containsPseudoXY(x, y)) {
			return dot_Cancel;
		} else if (dot_Rotate.containsPseudoXY(x, y)) {
			return dot_Rotate;
		}
		return null;
	}

	private boolean isTouchDownInLayer = false;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int x = (int) event.getX();
		int y = (int) event.getY();
		boolean state = false;
		// 点击在区域内相关操作
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			pressedDot = checkDot(x, y);
			if (pressedDot != null) {
				start.set(event.getX(), event.getY());
				pressedDot.setState(State.PREESED);
				invalidate();
				state = true;
				isTouchDownInLayer = true;
			} else if (isLayerContain(x, y)) {
				start.set(event.getX(), event.getY());
				invalidate();
				state = true;
				isTouchDownInLayer = true;
				startTimer();
			} else {
				state = false;
				isTouchDownInLayer = false;
				if (timer != null) {
					timer.cancel();
					timer = null;
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			// 如果点中的是HotDot，要让Dot变成Selected状态，并响应ClipArea缩放
			DotDelegate dot = checkDot(x, y);
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
				center.x = layerRect.exactCenterX();
				center.y = layerRect.exactCenterY();
				// 如果是某个点是Pressed状态，先根据移动的距离来缩放ClipArea的Height Width
				if (pressedDot == dot_Rotate) {
					end.x = mX;
					end.y = mY;
					float newSpin = getDegrees(start, end, center);
					degrees -= newSpin;
					handlerZoom(mX, mY);
					invalidate();
				} else if (isLayerContain(mX, mY)) {
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
		return super.onTouchEvent(event) || state;
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

	private void updateXY(float left, float top) {
		setLayerX(left);
		setLayerY(top);
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

	private void handlerZoom(float curx, float cury) {
		float nDist;
		float oDist;
		int oldWidth = layerRect.width();
		int oldHeight = layerRect.height();
		float centerX = layerRect.exactCenterX();
		float centerY = layerRect.exactCenterY();
		int newWidht;
		int newHeight;
		int dx;
		int dy;
		float curScale;
		nDist = spacing(curx, cury, centerX, centerY);
		oDist = spacing(dot_Rotate.getCX(), dot_Rotate.getCY(), centerX,
				centerY);
		curScale = nDist / oDist;
		newWidht = (int) (curScale * oldWidth);
		newHeight = (int) (curScale * oldHeight);
		dx = (newWidht - oldWidth) / 2;
		dy = (newHeight - oldHeight) / 2;
		if (newWidht <= PictureConfig.clip_layer_min_w
				|| newHeight <= PictureConfig.clip_layer_min_h) {
			return;
		}
		layerRect.inset(dx, dy);
		scale *= curScale;
		scaleLayer(scale);
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

	private void startTimer() {

		if (task != null) {
			task.cancel();
		}
		task = new MyTimerTask();
		if (timer == null)
			timer = new Timer();
		timer.schedule(task, 500);

	}

	class MyTimerTask extends TimerTask {

		@Override
		public void run() {
			light = !light;
			Message msg = mHandler.obtainMessage(AUTO_SHOW);
			msg.sendToTarget();
		}

	}

	/**
	 * 弹出软键盘
	 */
	private void showSoftInput() {
		imm.showSoftInput((View) getParent(), 0);
		LayerParent parent = getParent();
		if (parent != null) {
			parent.getInputHolder().bundelInputConnection(this);
		}
	}

	/**
	 * 隐藏软键盘
	 */
	private void hideSoftInput() {
		LayerParent parent = getParent();
		if (parent != null) {
			View view = (View) getParent();
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}
	}

	@Override
	public boolean onKeyEvent(KeyEvent event) {
		int keyCode = event.getKeyCode();
		if (keyCode == 67 && event.getAction() == KeyEvent.ACTION_DOWN) {
			util.deleteText();
			invalidate();
		} else if (keyCode == 4 && event.getAction() == KeyEvent.ACTION_DOWN) {
			isshow = !isshow;
		}
		return super.onKeyEvent(event);
	}

	@Override
	public boolean commitText(CharSequence text, int newCursorPosition) {
		util.AddText((String) text);
		invalidate();
		return false;
	}

	private OnLayerClickListener onClickListener = new OnLayerClickListener() {

		@Override
		public void onClick(Layer l) {
			if (isshow) {
				hideSoftInput();
				isshow = false;
			} else {
				showSoftInput();
				isshow = true;
				util.AddText("");
				// util.resetLeftAndTop(left, top + textsize);
			}
		}
	};
	private final OnClickListener dotListener = new OnClickListener() {

		@Override
		public void onDotClick(DotDelegate dot) {
			if (dot == dot_Cancel) {
				LayerParent p = getParent();
				if (p != null) {
					hideSoftInput();
					p.removeLayer(TextLayer.this, mCleaner);
					if (timer != null) {
						timer.cancel();
						timer = null;
					}
				}
			}
		}
	};

}
