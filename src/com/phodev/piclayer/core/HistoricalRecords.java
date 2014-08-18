package com.phodev.piclayer.core;

/**
 * 操作历史记录接口
 * 
 * @author sky
 * 
 */
public interface HistoricalRecords {

	public void forward();

	public void backward();

	public boolean canForward();

	public boolean canBackward();

}
