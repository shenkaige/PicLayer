package com.phodev.piclayer.core;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * 预览图从
 * 
 * @author sky
 * 
 */
public class PreviewLayer extends Layer {
	private Paint paint;
	private int canvasW;
	private int canvasH;
	private float scale;

	public PreviewLayer() {
		paint = new Paint();
		paint.setAntiAlias(true);
	}

	@Override
	public void onDraw(Canvas canvas) {
		Bitmap b = getLayerBitmap();
		if (b == null || b.isRecycled())
			return;
		int bW = b.getWidth();
		int bH = b.getHeight();
		float left = 0;
		float top = 0;
		canvasW = canvas.getWidth();
		canvasH = canvas.getHeight();
		scale = calculateFitScale(bW, bH, canvasW, canvasH);
		left = (canvasW - (bW * scale)) / 2;
		top = (canvasH - (bH * scale)) / 2;
		int c_count = canvas.save();
		canvas.scale(scale, scale);
		canvas.drawBitmap(b, left / scale, top / scale, paint);
		canvas.restoreToCount(c_count);
		// super.onDraw(canvas);
	}
}
