package com.phodev.piclayer.core;

import android.graphics.Bitmap;
import android.graphics.Rect;

/**
 * 
 * @author sky
 *
 */
public interface LayerParent {
	/**
	 * All or part of a child is dirty and needs to be redrawn.
	 * 
	 * @param child
	 *            The child which is dirty
	 * @param r
	 *            The area within the child that is invalid
	 */
	public void invalidateChild(Layer child, Rect dirty);

	/**
	 * 焦点发生改变
	 * 
	 * @param layer
	 * @param isFocused
	 */
	public void onChildFocusChanged(Layer layer, boolean isFocused);

	public boolean requestFocusFromParent(Layer layer);

	public void removeLayer(Layer l);

	public boolean removeLayer(Layer layer, LayerSafeCleaner cleaner);

	public void addLayer(Layer l);

	public BaseBackgroundLayer getBackgroundLayer();

	public void setBackgroundLayer(BaseBackgroundLayer layer);

	public Bitmap mergeAllLayer(MergeRefer refer);

	public LayerInputHolder getInputHolder();

	/**
	 * Layer安全清理
	 * 
	 * @author skg
	 * 
	 */
	public interface LayerSafeCleaner {
		public boolean destroyLayer(Layer layer);
	}

}
