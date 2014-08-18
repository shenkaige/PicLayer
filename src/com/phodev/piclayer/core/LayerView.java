package com.phodev.piclayer.core;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

/**
 * @author sky
 */
public class LayerView extends View implements LayerParent {
	private LayerInputHolder mInputHolder;

	public LayerView(Context context) {
		super(context);
		init(context);
	}

	public LayerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	/**
	 * 初始化基本信息
	 * 
	 * @param context
	 */
	private void init(Context context) {
		setFocusableInTouchMode(true);
		setLongClickable(true);
	}

	@Override
	public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
		if (mInputHolder == null) {
			mInputHolder = new LayerInputHolder(this, false);
		}
		return mInputHolder;
	}

	@Override
	public LayerInputHolder getInputHolder() {
		return mInputHolder;
	}

	/* =======================Layers Manager========================== */
	private ArrayList<Layer> childList = new ArrayList<Layer>();
	private BaseBackgroundLayer backgroundLayer;

	@Override
	public void addLayer(Layer layer) {
		if (layer != null) {
			if (childList.contains(layer)) {
				// 如果已经存在了，先移除，然后在添加，这个时候要调整到做上面一个(在List中是最后一个)
				childList.remove(layer);
			}
			layer.assignParent(this);// 设置Parent
			childList.add(layer);
			updateTopLayer(layer);
			invalidate();
		}
	}

	@Override
	public BaseBackgroundLayer getBackgroundLayer() {
		return backgroundLayer;
	}

	/**
	 * 设置背景图层
	 * 
	 * @param layer
	 */
	@Override
	public void setBackgroundLayer(BaseBackgroundLayer layer) {
		if (backgroundLayer != null) {
			backgroundLayer.assignParent(null);
		}
		backgroundLayer = layer;
		if (backgroundLayer != null) {
			layer.assignParent(this);
		}
		invalidate();
	}

	@Override
	public void removeLayer(Layer l) {
		if (l != null) {
			if (childList.remove(l)) {
				l.assignParent(null);
			}
			updateTopLayer();
			invalidate();
		}
	}

	public boolean isContainLayer(Layer l) {
		return childList.contains(l);
	}

	/**
	 * 合并效果
	 * 
	 * @return
	 */
	@Override
	public Bitmap mergeAllLayer(MergeRefer refer) {
		if (refer != null) {
			Canvas canvas = refer.getMergeCanvas();
			Layer referLayer = refer.getMergeReferLayer();
			if (canvas != null) {
				switch (refer.getMergeReferLayerPriority()) {
				case MergeRefer.MERGE_ALWAYS_FIRST:
					// mergeSelf before
					referLayer.onMergeDraw(canvas, refer);
					mergeBackGround(canvas, refer);
					dispatchMergeChildLayers(canvas, refer, referLayer, false);
					break;
				case MergeRefer.MERGE_ALWAYS_LAST:
					mergeBackGround(canvas, refer);
					dispatchMergeChildLayers(canvas, refer, referLayer, false);
					// mergeSelf after
					referLayer.onMergeDraw(canvas, refer);
					break;
				case MergeRefer.MERGE_NATURAL_ORDERING:
				default:
					mergeBackGround(canvas, refer);
					dispatchMergeChildLayers(canvas, refer, referLayer, true);
					break;
				}
				return refer.postMergeCanvas(canvas);
			}
		}
		return null;
	}

	/**
	 * 分发Merge child layers
	 * 
	 * @param canvas
	 * @param refer
	 * @param referLayer
	 * @param mergeSlef
	 */
	private void dispatchMergeChildLayers(Canvas canvas, MergeRefer refer,
			Layer referLayer, boolean mergeSlef) {
		for (Layer l : childList) {
			if (mergeSlef) {
				l.onMergeDraw(canvas, refer);
			} else if (l != referLayer) {
				l.onMergeDraw(canvas, refer);
			}
		}
	}

	/**
	 * Merge Background
	 * 
	 * @param canvas
	 * @param refer
	 */
	private void mergeBackGround(Canvas canvas, MergeRefer refer) {
		if (backgroundLayer != null) {
			backgroundLayer.onMergeDraw(canvas, refer);
		}
	}

	@Override
	public void invalidateChild(Layer child, Rect r) {
		// 子Layer通知Group刷新自己，这个时候，由Group来判断那个需要刷新，并吧需要draw的Layer 画一遍,达到局部刷新的效果
		// r.intersect(r);
		// drawLayer(child);
		if (child != null
				&& (child == backgroundLayer || childList.contains(child))) {
			this.invalidate();
		}
	}

	private Layer topLayer = null;
	private Layer foucesedLayer = null;
	private boolean state = false;// 只可以在dispatchTouchEvent中使用
	private int foucesedIndex = -1;// 只可以在dispatchTouchEvent中使用

	/**
	 * 分发Touch事件
	 * 
	 * @param e
	 * @return
	 */
	@Override
	public boolean dispatchTouchEvent(MotionEvent e) {
		// 1, 如果FoucesedLayer已经不再是最上层的Layer即Top Layer，这个时候要从头开始分发事件
		// 2,如果Layer已经被remove也要重新分发事件
		if (foucesedLayer != null && foucesedLayer.getParent() == this) {
			if (foucesedLayer == topLayer && foucesedLayer.onTouchEvent(e)) {
				return true;
			} else {
				foucesedLayer.setFoucsed(false);
				foucesedLayer = null;
			}
		}
		state = false;
		foucesedIndex = -1;
		// 继续分发到Layer
		int j = childList.size() - 1;
		for (int i = j; i >= 0; i--) {
			Layer layer = childList.get(i);
			if (layer.onTouchEvent(e)) {
				foucesedLayer = layer;
				foucesedIndex = i;
				layer.setFoucsed(true);
				state = true;
				break;
			}
		}
		// 让最后点击的Layer被调到List的最上面
		if (foucesedLayer != null) {
			j = childList.size();
			if (foucesedIndex >= 0 && foucesedIndex < j) {
				Layer layer = childList.get(foucesedIndex);
				if (layer == foucesedLayer) {
					childList.remove(foucesedIndex);
					childList.add(foucesedLayer);
				}
				updateTopLayer(layer);
			}
		}
		if (backgroundLayer != null)
			state |= backgroundLayer.onTouchEvent(e);
		// if (state) {
		// return state;
		// } else {
		// return super.dispatchTouchEvent(e);
		// }
		return super.dispatchTouchEvent(e) || state;
	}

	/**
	 * 更新最上面的Layer
	 * 
	 * @param layer
	 */
	private void updateTopLayer() {
		int topIndex = childList.size() - 1;
		if (topIndex >= 0) {
			updateTopLayer(childList.get(topIndex));
		} else {
			updateTopLayer(null);
		}
	}

	/**
	 * 更新最上面的Layer
	 * 
	 * @param layer
	 */
	private void updateTopLayer(Layer l) {
		topLayer = l;
	}

	/**
	 * 分发Key事件
	 * 
	 * @param e
	 * @return
	 */
	public boolean dispatchKeyEvent(KeyEvent e) {
		// 继续分发到Layer
		// if (focusedLayer != null) {
		// return focusedLayer.onKeyEvent(e);
		// }
		for (Layer l : childList) {
			if (l.onKeyEvent(e))
				return true;
		}
		return false;
	}

	@Override
	public void onChildFocusChanged(Layer layer, boolean isFocused) {
		// if (isFocused) {
		// focusedLayer = layer;
		// } else {
		// focusedLayer = null;
		// }
	}

	@Override
	public boolean requestFocusFromParent(Layer layer) {
		if (layer != null) {
			if (layer == foucesedLayer && layer == topLayer) {
				return true;
			} else {
				if (foucesedLayer != null) {
					foucesedLayer.setFoucsed(false);
				}
				foucesedLayer = layer;
				foucesedIndex = childList.indexOf(layer);
				// 让最后点击的Layer被调到List的最上面
				if (foucesedLayer != null) {
					foucesedLayer.setFoucsed(true);
					int j = childList.size() - 1;
					// 如果等于j的时候证明已经是在最上面个，不需要移动
					if (foucesedIndex >= 0 && foucesedIndex < j) {
						if (layer != null) {
							childList.remove(foucesedIndex);
							childList.add(layer);
						}
					}
					updateTopLayer(layer);
					invalidate();
					return true;
				}
			}
		}
		return false;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (canvas != null) {
			canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
			if (backgroundLayer != null)
				drawLayer(backgroundLayer, canvas);// 先画背景层
			for (Layer l : childList) {
				drawLayer(l, canvas);
			}
		}
	}

	@SuppressLint("WrongCall")
	private void drawLayer(Layer l, Canvas canvas) {
		// canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
		l.onDraw(canvas);
	}

	/**
	 * 移除Layer并销毁掉
	 * 
	 * @param layer
	 * @param cleaner
	 * @return
	 */
	@Override
	public boolean removeLayer(Layer layer, LayerSafeCleaner cleaner) {
		removeLayer(layer);
		if (cleaner != null) {
			return cleaner.destroyLayer(layer);
		}
		return false;
	}

	/**
	 * 销毁所有Layer
	 * 
	 * @param cleaner
	 * @return
	 */
	public boolean safeDestroyAllLayer(LayerSafeCleaner cleaner) {
		if (cleaner != null) {
			for (Layer layer : childList) {
				cleaner.destroyLayer(layer);
			}
			cleaner.destroyLayer(backgroundLayer);
			childList.clear();
			updateTopLayer(null);
			foucesedLayer = null;
			foucesedIndex = -1;
		}
		return true;
	}

	/* ======================Layers Manager End====================== */
}
