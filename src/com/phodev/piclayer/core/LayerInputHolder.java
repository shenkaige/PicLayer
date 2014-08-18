package com.phodev.piclayer.core;

import android.view.View;
import android.view.inputmethod.BaseInputConnection;

/**
 * @author sky
 */
public class LayerInputHolder extends BaseInputConnection {
	private LayerInputCallBack mLayerInputCallback;

	public interface LayerInputCallBack {
		public boolean commitText(CharSequence text, int newCursorPosition);
	}

	public LayerInputHolder(View targetView, boolean fullEditor) {
		super(targetView, fullEditor);
	}

	@Override
	public boolean commitText(CharSequence text, int newCursorPosition) {
		if (mLayerInputCallback != null) {
			return mLayerInputCallback.commitText(text, newCursorPosition);
		} else {
			return super.commitText(text, newCursorPosition);
		}
	}

	/**
	 * 如果之前有绑定过，在此绑定会替换掉之前的绑定，一最后一次绑定为准
	 * 
	 * @param inputCallback
	 */
	public void bundelInputConnection(LayerInputCallBack inputCallback) {
		mLayerInputCallback = inputCallback;
	}
}
