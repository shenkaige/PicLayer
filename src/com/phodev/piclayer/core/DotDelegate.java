package com.phodev.piclayer.core;

import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * 点的描述
 * 
 * @author sky
 * 
 */
public class DotDelegate {
	public final static int TOP_LEFT = 1;
	public final static int TOP_RIGHT = 2;
	public final static int BOTTOM_LEFT = 3;
	public final static int BOTTOM_RIGHT = 4;
	private int mDotPosition;
	private float cx;
	private float cy;
	private Rect dotRect;
	private PointF pseudoXY;
	private RectF pseudoRect;
	private int click_r = PictureConfig.getDotRadius()
			+ PictureConfig.getDotClickAreaExpand();
	private State mState = State.NORMAL;
	private Type mType;

	public enum Type {
		STANDARD, CANCLE, ROTATE;
	}

	public enum State {
		PREESED, NORMAL;
	}

	public DotDelegate() {
		this(0);
	}

	public DotDelegate(int dotPosition) {
		mDotPosition = dotPosition;
		dotRect = new Rect();
		pseudoRect = new RectF();
		pseudoXY = new PointF();
	}

	public void setDotType(Type type) {
		mType = type;
	}

	public Type getDotType() {
		return mType;
	}

	public int getDotPosition() {
		return mDotPosition;
	}

	/**
	 * 圆点的Top,Left,Right,Bottom的描述
	 * 
	 * @return
	 */
	public Rect getDelegateRect() {
		return dotRect;
	}

	/**
	 * 设置圆心的圆心坐标信息，半径参照定义好的半径，在此就不作为参数传入了
	 * 
	 * @param cx
	 * @param cy
	 */
	public void setDotInfo(float cx, float cy) {
		this.cx = cx;
		this.cy = cy;
		int cxTemp = (int) cx;
		int cyTemp = (int) cy;
		int left = cxTemp - click_r;
		int top = cyTemp - click_r;
		int right = cxTemp + click_r;
		int bottom = cyTemp + click_r;
		dotRect.set(left, top, right, bottom);
	}

	public void setState(State state) {
		mState = state;
	}

	public State getState() {
		return mState;
	}

	/**
	 * 圆心x坐标
	 * 
	 * @return
	 */
	public float getCX() {
		return cx;
	}

	/**
	 * 圆心y坐标
	 * 
	 * @return
	 */
	public float getCY() {
		return cy;
	}

	public void setPseudoXY(PointF point) {
		pseudoXY.set(point);
		int cxTemp = (int) pseudoXY.x;
		int cyTemp = (int) pseudoXY.y;
		int left = cxTemp - click_r;
		int top = cyTemp - click_r;
		int right = cxTemp + click_r;
		int bottom = cyTemp + click_r;
		pseudoRect.set(left, top, right, bottom);
	}

	public PointF getPseudoXY() {
		return pseudoXY;
	}

	public float getPseudoX() {
		return pseudoXY.x;
	}

	public float getPseudoY() {
		return pseudoXY.y;
	}

	/**
	 * 判断坐标是否在描述该点的Rect区域内
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean contains(float x, float y) {
		return dotRect.contains((int) x, (int) y);
	}

	public boolean containsPseudoXY(float x, float y) {
		return pseudoRect.contains(x, y);
	}

	private OnClickListener onClickListener;

	public interface OnClickListener {
		public void onDotClick(DotDelegate dot);
	}

	/**
	 * 设置Click监听
	 * 
	 * @param l
	 */
	public void setOnClickListener(OnClickListener l) {
		onClickListener = l;
	}

	protected void dispatchClick() {
		if (onClickListener != null) {
			onClickListener.onDotClick(this);
		}
	}
}
