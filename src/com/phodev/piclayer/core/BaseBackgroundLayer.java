package com.phodev.piclayer.core;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

/**
 * 背景Layer,最后一个添加，该Layer不接受任何Touch，Key事件，永远在底层
 * 
 * @author sky
 * 
 */
public abstract class BaseBackgroundLayer extends Layer implements MergeRefer {

	/**
	 * 获取图片缩放比
	 * 
	 * @return
	 */
	public abstract float getBackgroundScale();

	/**
	 * 根据Rect算出真实对应的Rect
	 * 
	 * @param clipArea
	 * @return
	 */
	public abstract Rect getClipAreaMapping(Rect clipArea);

	/**
	 * 获取图片内容占用的区域--注：不是整个Layer占用的Area
	 * 
	 * @return
	 */
	public abstract Rect getBgResourceOccupyArea();

	/**
	 * 根据指定区域裁减背景图片
	 * 
	 * @param clipArea
	 * @return
	 */
	public abstract Bitmap clipBackgroundBitmap(Rect clipArea);

	/**
	 * 根据指定区域裁减背景图片
	 */
	public abstract Bitmap rawClipBackgroundBitmap(Rect rawRect);

	/**
	 * 获取合并图从使用的Canvas
	 * 
	 * @return
	 */
	public Canvas getMergeCanvas() {
		return null;
	}

	/**
	 * 提交合并好的Canvas
	 * 
	 * @param canvas
	 * @return
	 */
	public Bitmap postMergeCanvas(Canvas canvas) {
		return null;
	}

	/**
	 * 获取合并的参照图层
	 * 
	 * @return
	 */
	public Layer getMergeReferLayer() {
		return null;
	}

	/**
	 * 获取被参照层的Merge优先级
	 * 
	 * @return
	 */
	public int getMergeReferLayerPriority() {
		return MergeRefer.MERGE_NATURAL_ORDERING;
	}

}
