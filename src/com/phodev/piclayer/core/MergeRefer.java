package com.phodev.piclayer.core;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * LayerView合并图层的时候需要参照
 * 
 * @author sky
 * 
 */
public interface MergeRefer {

	/**
	 * 安装正常顺序
	 */
	public static final int MERGE_NATURAL_ORDERING = 0;
	/**
	 * 画在最底层
	 */
	public static final int MERGE_ALWAYS_FIRST = 1;
	/**
	 * 画在最上面一层
	 */
	public static final int MERGE_ALWAYS_LAST = 2;

	/**
	 * 获取合并图从使用的Canvas
	 * 
	 * @return
	 */
	public Canvas getMergeCanvas();

	/**
	 * 提交合并好的Canvas
	 * 
	 * @param canvas
	 * @return
	 */
	public Bitmap postMergeCanvas(Canvas canvas);

	/**
	 * 获取合并的参照图层
	 * 
	 * @return
	 */
	public Layer getMergeReferLayer();

	/**
	 * 获取被参照层的Merge优先级
	 * 
	 * @return
	 */
	public int getMergeReferLayerPriority();
}
