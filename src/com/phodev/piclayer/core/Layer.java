package com.phodev.piclayer.core;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.KeyEvent;
import android.view.MotionEvent;

/**
 * 图层
 * 
 * @author sky
 * 
 */
public abstract class Layer {
	// private SoftReference<Bitmap> layerBitmap;
	private Bitmap layerBitmap;
	private LayerParent mParent;
	private LayerParams mLayerParams;
	private float x;// Layer的x坐标
	private float y;// Layer的y坐标
	private int width;// Layer的宽度
	private int height;// Layer的高度
	private boolean focused = false;

	private Rect layerRect = new Rect();// Layer所占用的巨型信息
	private Rect layerDirtyRect = new Rect();
	private long clik_time_mark;// 点击时间记录
	private final int clik_max_time = 800;

	public void setLayerResouce(Bitmap bitmap, boolean destroyOldData) {
		if (destroyOldData)
			destroyLayerData();
		if (bitmap != null) {
			// this.layerBitmap = new SoftReference<Bitmap>(bitmap);
			layerBitmap = bitmap;
			width = bitmap.getWidth();
			height = bitmap.getHeight();
		}
	}

	/**
	 * 添加图层数据,默认不会销毁旧的数据
	 * 
	 * @param bitmap
	 */
	public void setLayerResouce(Bitmap bitmap) {
		setLayerResouce(bitmap, false);
	}

	public Bitmap getLayerBitmap() {
		// return layerBitmap == null ? null : layerBitmap.get();
		return layerBitmap;
	}

	/**
	 * 被添加到LayerGroup中的时候被调用
	 * 
	 * @param parent
	 */
	protected void assignParent(LayerParent parent) {
		if (mParent == null) {
			mParent = parent;
		} else if (parent == null) {
			mParent = null;
		} else {
			throw new RuntimeException("Layer " + this + " being added, but"
					+ " it already has a parent");
		}
	}

	/**
	 * 根据图层信息绘画改图层
	 * 
	 * @param canvas
	 */
	public void onDraw(Canvas canvas) {
		int x = (int) this.x;
		int y = (int) this.y;
		updateRect(x, y, x + width, y + height);
	}

	/**
	 * 合并Layer的效果
	 * 
	 * @param canvas
	 */
	public void onMergeDraw(Canvas canvas, MergeRefer refer) {
		int x = (int) this.x;
		int y = (int) this.y;
		updateRect(x, y, x + width, y + height);
	}

	/**
	 * Touch事件出发
	 * 
	 * @param event
	 * @return
	 */
	public boolean onTouchEvent(MotionEvent event) {
		int x = (int) event.getX();
		int y = (int) event.getY();
		if (!getRect().contains(x, y))
			return false;
		boolean state = false;
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			clik_time_mark = System.currentTimeMillis();
			state = true;
			break;
		case MotionEvent.ACTION_UP:
			// 点击判断
			if (layerClickListener != null) {
				if (System.currentTimeMillis() - clik_time_mark < clik_max_time) {
					layerClickListener.onClick(this);
					state = true;
				}
			}
			break;
		}
		return state;
	}

	/**
	 * Key时间出发
	 * 
	 * @param event
	 * @return
	 */
	public boolean onKeyEvent(KeyEvent event) {
		return false;
	}

	private OnLayerClickListener layerClickListener;

	public interface OnLayerClickListener {
		public void onClick(Layer l);
	}

	public void setOnLayerClickListener(OnLayerClickListener l) {
		layerClickListener = l;
	}

	/**
	 * 刷新Layer,请求Re Draw
	 */
	public void invalidate() {
		if (mParent != null) {
			mParent.invalidateChild(this, layerDirtyRect);
		}
	}

	/**
	 * Layer的宽度，并不是某个Bitmap的高度
	 * 
	 * @return
	 */
	public int getHeight() {
		return width;
	}

	/**
	 * Layer的宽度，并不是某个Bitmap的宽度
	 * 
	 * @return
	 */
	public int getWidth() {
		return height;
	}

	/**
	 * 获取Layer X坐标
	 * 
	 * @return
	 */
	public float getLayerX() {
		return x;
	}

	/**
	 * 获取Layer Y坐标
	 * 
	 * @return
	 */
	public float getLayerY() {
		return y;
	}

	/**
	 * 更新Layer所在的x坐标
	 * 
	 * @param x
	 */
	public void setLayerX(float x) {
		this.x = x;
	}

	/**
	 * 更新Layer所在的y坐标
	 * 
	 * @param y
	 */
	public void setLayerY(float y) {
		this.y = y;
	}

	/**
	 * 这事Layer参数系信息
	 * 
	 * @param lp
	 */
	public void setLayerParams(LayerParams lp) {
		mLayerParams = lp;
		invalidate();
	}

	/**
	 * Layer所在的位置信息,图层没有画的时候是null
	 * 
	 * @return
	 */
	public Rect getRect() {
		return layerRect;
	}

	/**
	 * 不在需要的地方
	 * 
	 * @return
	 */
	public Rect getDirtyRect() {
		return layerDirtyRect;
	}

	/**
	 * 是否获取到焦点了
	 * 
	 * @return
	 */
	public boolean isFocused() {
		return focused;
	}

	/**
	 * 请求获取焦点
	 */
	public boolean requestFocus() {
		LayerParent parent = getParent();
		if (parent != null) {
			return parent.requestFocusFromParent(this);
		}
		return false;
	}

	/**
	 * 设置焦点
	 * 
	 * @param foucsed
	 */
	protected void setFoucsed(boolean focus) {
		this.focused = focus;
		onFoucsChanged(focused);
	}

	protected void onFoucsChanged(boolean focused) {

	}

	protected void updateRect(int left, int top, int right, int bottom) {

		layerDirtyRect.setEmpty();
		layerDirtyRect.set(layerRect);

		layerRect.left = left;
		layerRect.top = top;
		layerRect.right = right;
		layerRect.bottom = bottom;
	}

	public LayerParent getParent() {
		return mParent;
	}

	/**
	 * 清除Layer的Bitmap数据
	 */
	public void destroyLayerData() {
		if (layerBitmap != null) {
			// Bitmap b = layerBitmap.get();
			Bitmap b = layerBitmap;
			if (b != null && !b.isRecycled()) {
				b.recycle();
			}
			// layerBitmap.clear();
			layerBitmap = null;
		}
	}

	/**
	 * 计算缩放比例
	 * 
	 * @param bW
	 * @param bH
	 * @param cW
	 * @param cH
	 * @return
	 */
	public float calculateFitScale(int bW, int bH, int cW, int cH) {
		// float fitScale = 1;
		float sw = 1;
		float sh = 1;
		if (bW > 0) {
			sw = (float) cW / bW;
		}
		if (bH > 0) {
			sh = (float) cH / bH;
		}
		if (sw < sh) {
			return sw;
		} else {
			return sh;
		}
		// if ((bW > cW && bH > cH) || (bW < cW && bH < cH)) {
		// int distW = bW - cW;
		// int distH = bH - cH;
		// if (distW > distH) {// 使用W
		// fitScale = (float) cW / bW;
		// } else {// 使用H
		// fitScale = (float) cH / bH;
		// }
		// } else if (bH > cH) {// 自有高度大于
		// fitScale = (float) cH / bH;
		// } else if (bW > cW) {// 只有宽度大于
		// fitScale = (float) cW / bW;
		// }
		// return fitScale;
	}
}
