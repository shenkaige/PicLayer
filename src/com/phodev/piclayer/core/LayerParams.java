package com.phodev.piclayer.core;

/**
 * 避免设置高度，宽度分开设置的时候,多余的屏幕刷新
 * 
 * @author sky
 * 
 */
public class LayerParams {
	public int width;
	public int height;

	public LayerParams(int width, int height) {
		this.width = width;
		this.height = height;
	}
}
