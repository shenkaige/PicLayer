package com.phodev.piclayer.core;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;

/**
 * 
 * @author sky
 *
 */
public class PictureUtils {
	/**
	 * 图片模糊处理
	 * 
	 * @param sentBitmap
	 * @param radius
	 *            模糊半径
	 * @return
	 */
	public static Bitmap fastblur(Bitmap sentBitmap, int radius) {

		// Stack Blur v1.0 from
		// http://www.quasimondo.com/StackBlurForCanvas/StackBlurDemo.html
		//
		// Java Author: Mario Klingemann <mario at quasimondo.com>
		// http://incubator.quasimondo.com
		// created Feburary 29, 2004
		// Android port : Yahel Bouaziz <yahel at kayenko.com>
		// http://www.kayenko.com
		// ported april 5th, 2012

		// This is a compromise between Gaussian Blur and Box blur
		// It creates much better looking blurs than Box Blur, but is
		// 7x faster than my Gaussian Blur implementation.
		//
		// I called it Stack Blur because this describes best how this
		// filter works internally: it creates a kind of moving stack
		// of colors whilst scanning through the image. Thereby it
		// just has to add one new block of color to the right side
		// of the stack and remove the leftmost color. The remaining
		// colors on the topmost layer of the stack are either added on
		// or reduced by one, depending on if they are on the right or
		// on the left side of the stack.
		//
		// If you are using this algorithm in your code please add
		// the following line:
		//
		// Stack Blur Algorithm by Mario Klingemann <mario@quasimondo.com>

		Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

		if (radius < 1) {
			return (null);
		}

		int w = bitmap.getWidth();
		int h = bitmap.getHeight();

		int[] pix = new int[w * h];
		bitmap.getPixels(pix, 0, w, 0, 0, w, h);

		int wm = w - 1;
		int hm = h - 1;
		int wh = w * h;
		int div = radius + radius + 1;

		int r[] = new int[wh];
		int g[] = new int[wh];
		int b[] = new int[wh];
		int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
		int vmin[] = new int[Math.max(w, h)];

		int divsum = (div + 1) >> 1;
		divsum *= divsum;
		int dv[] = new int[256 * divsum];
		for (i = 0; i < 256 * divsum; i++) {
			dv[i] = (i / divsum);
		}

		yw = yi = 0;

		int[][] stack = new int[div][3];
		int stackpointer;
		int stackstart;
		int[] sir;
		int rbs;
		int r1 = radius + 1;
		int routsum, goutsum, boutsum;
		int rinsum, ginsum, binsum;

		for (y = 0; y < h; y++) {
			rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
			for (i = -radius; i <= radius; i++) {
				p = pix[yi + Math.min(wm, Math.max(i, 0))];
				sir = stack[i + radius];
				sir[0] = (p & 0xff0000) >> 16;
				sir[1] = (p & 0x00ff00) >> 8;
				sir[2] = (p & 0x0000ff);
				rbs = r1 - Math.abs(i);
				rsum += sir[0] * rbs;
				gsum += sir[1] * rbs;
				bsum += sir[2] * rbs;
				if (i > 0) {
					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];
				} else {
					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];
				}
			}
			stackpointer = radius;

			for (x = 0; x < w; x++) {

				r[yi] = dv[rsum];
				g[yi] = dv[gsum];
				b[yi] = dv[bsum];

				rsum -= routsum;
				gsum -= goutsum;
				bsum -= boutsum;

				stackstart = stackpointer - radius + div;
				sir = stack[stackstart % div];

				routsum -= sir[0];
				goutsum -= sir[1];
				boutsum -= sir[2];

				if (y == 0) {
					vmin[x] = Math.min(x + radius + 1, wm);
				}
				p = pix[yw + vmin[x]];

				sir[0] = (p & 0xff0000) >> 16;
				sir[1] = (p & 0x00ff00) >> 8;
				sir[2] = (p & 0x0000ff);

				rinsum += sir[0];
				ginsum += sir[1];
				binsum += sir[2];

				rsum += rinsum;
				gsum += ginsum;
				bsum += binsum;

				stackpointer = (stackpointer + 1) % div;
				sir = stack[(stackpointer) % div];

				routsum += sir[0];
				goutsum += sir[1];
				boutsum += sir[2];

				rinsum -= sir[0];
				ginsum -= sir[1];
				binsum -= sir[2];

				yi++;
			}
			yw += w;
		}
		for (x = 0; x < w; x++) {
			rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
			yp = -radius * w;
			for (i = -radius; i <= radius; i++) {
				yi = Math.max(0, yp) + x;

				sir = stack[i + radius];

				sir[0] = r[yi];
				sir[1] = g[yi];
				sir[2] = b[yi];

				rbs = r1 - Math.abs(i);

				rsum += r[yi] * rbs;
				gsum += g[yi] * rbs;
				bsum += b[yi] * rbs;

				if (i > 0) {
					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];
				} else {
					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];
				}

				if (i < hm) {
					yp += w;
				}
			}
			yi = x;
			stackpointer = radius;
			for (y = 0; y < h; y++) {
				pix[yi] = 0xff000000 | (dv[rsum] << 16) | (dv[gsum] << 8)
						| dv[bsum];

				rsum -= routsum;
				gsum -= goutsum;
				bsum -= boutsum;

				stackstart = stackpointer - radius + div;
				sir = stack[stackstart % div];

				routsum -= sir[0];
				goutsum -= sir[1];
				boutsum -= sir[2];

				if (x == 0) {
					vmin[y] = Math.min(y + r1, hm) * w;
				}
				p = x + vmin[y];

				sir[0] = r[p];
				sir[1] = g[p];
				sir[2] = b[p];

				rinsum += sir[0];
				ginsum += sir[1];
				binsum += sir[2];

				rsum += rinsum;
				gsum += ginsum;
				bsum += binsum;

				stackpointer = (stackpointer + 1) % div;
				sir = stack[stackpointer];

				routsum += sir[0];
				goutsum += sir[1];
				boutsum += sir[2];

				rinsum -= sir[0];
				ginsum -= sir[1];
				binsum -= sir[2];

				yi += w;
			}
		}
		bitmap.setPixels(pix, 0, w, 0, 0, w, h);
		return (bitmap);
	}

	/**
	 * 根据连点，跟圆心的左边，算出夹角
	 * 
	 * @param curX
	 * @param curY
	 * @param preX
	 * @param preY
	 * @param centerX
	 * @param centerY
	 * @return
	 */
	public static float getRawDegrees(float curX, float curY, float preX,
			float preY, float centerX, float centerY) {
		double dCur = getRadian(curX, curY, centerX, centerY);
		double dPre = getRadian(preX, preY, centerX, centerY);
		return (float) ((dPre - dCur) * 180 / Math.PI);
	}

	/**
	 * 计算一条线的角度
	 * 
	 * @param startX
	 * @param startY
	 * @param endX
	 * @param endY
	 * @return
	 */
	public static float getLineDegrees(float startX, float startY, float endX,
			float endY) {
		return (float) (Math.toDegrees(getRadian(startX, startY, endX, endY)));
	}

	/**
	 * 算出一点和边的弧度
	 * 
	 * @param x
	 * @param y
	 * @param centerX
	 * @param centerY
	 * @return
	 */
	public static double getRadian(float x, float y, float centerX,
			float centerY) {
		double radian = 0;
		y -= centerY;
		x -= centerX;
		double delt = Math.abs(y / x);
		if (y > 0 && x > 0) {
			radian = Math.atan(delt);
		} else if (y > 0 && x < 0) {
			radian = Math.PI - Math.atan(delt);
		} else if (y < 0 && x < 0) {
			radian = Math.PI + Math.atan(delt);
		} else if (y < 0 && x > 0) {
			radian = 2 * Math.PI - Math.atan(delt);
		}
		return radian;
		// return Math.atan2(x, y);
	}

	public static void spinPoint(PointF tarP, PointF center, float degrees) {
		float dx = tarP.x - center.x;
		float dy = tarP.y - center.y;
		double angle = Math.toRadians(degrees);
		float cosV = (float) Math.cos(angle);
		float sinV = (float) Math.sin(angle);
		tarP.x = cosV * dx - sinV * dy + center.x;
		tarP.y = cosV * dy + sinV * dx + center.y;
	}

	/**
	 * 计算中点位置
	 * 
	 * @param resutlPoint
	 * @param x0
	 * @param y0
	 * @param x1
	 * @param y1
	 * @return
	 */
	public static PointF midPoint(PointF resutlPoint, float x0, float y0,
			float x1, float y1) {
		float x = x0 + x1;
		float y = x1 + y1;
		resutlPoint.set(x / 2, y / 2);
		return resutlPoint;
	}

	// ////////////////////////////
	/**
	 * 直线方程式
	 * 
	 * @param result
	 *            Ax+By+C=0,result.length>=3,依次为，a,b,c
	 * @param x1
	 *            直线上点的x坐标
	 * @param y1
	 *            直线上点的y坐标
	 * @param k
	 *            斜率
	 * @return
	 */
	public static float[] getLine(float[] result, float x1, float y1, float k) {
		float a;
		float b;
		float c;
		if (k == Float.NaN) {// 斜率不存在
			a = 0;
			b = 0;
			c = 0;
		} else {
			a = -k;
			b = 1;
			c = k * x1 - y1;
		}
		result[0] = a;
		result[1] = b;
		result[2] = c;
		return result;
	}

	/**
	 * 根据直线方程获取对称点
	 * 
	 * @param x1
	 * @param y1
	 * @param a
	 * @param b
	 * @param c
	 * @return
	 */
	public static PointF getSymmetryPoint(PointF result, float x1, float y1,
			float a, float b, float c, float lineX) {
		float x2;
		float y2;
		if (a == 0 && b == 0) {// 斜率不存在
			y2 = y1;
			x2 = lineX * 2 - x1;
		} else {
			// x2 = ((b * b - a * a) * x1 - 2 * a * b * y1 - 2 * a * c)
			// / (a * a + b * b);
			// y2 = ((a * a - b * b) * y1 - 2 * a * b * x1 - 2 * b * c)
			// / (a * a + b * b);
			float aa = a * a;
			float bb = b * b;
			float ab2 = 2 * a * b;
			float aa_bb = aa + bb;
			x2 = ((bb - aa) * x1 - ab2 * y1 - 2 * a * c) / aa_bb;
			y2 = ((aa - bb) * y1 - ab2 * x1 - 2 * b * c) / aa_bb;
		}
		result.set(x2, y2);
		return result;
	}

	/**
	 * 根据两点算斜率
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 */
	public static float getSlope(float x1, float y1, float x2, float y2) {
		float x = (x2 - x1);
		float y = y2 - y1;
		if (x == 0) {
			// throw new RuntimeException("斜率 x不能等于0");
			return Float.NaN;
		}
		return y / x;
	}

	private static Matrix scaleMatrix;

	public static Matrix getEmptyScaleMatrix() {
		if (scaleMatrix == null) {
			scaleMatrix = new Matrix();
		} else {
			scaleMatrix.reset();
		}
		return scaleMatrix;
	}

	/**
	 * 缩放图片到指定的尺寸
	 * 
	 * @param source
	 * @param targetW
	 * @param targetH
	 * @return
	 */
	public static Bitmap scaleBitmap(Bitmap source, int targetW, int targetH) {
		Bitmap result = null;
		if (source != null && !source.isRecycled()) {
			Matrix matrix = getEmptyScaleMatrix();
			int w = source.getWidth();
			int h = source.getHeight();
			if (w > 0 && h > 0) {
				float scaleX = (float) targetW / w;
				float scaleY = (float) targetH / h;
				if (scaleX <= 0) {
					scaleX = 1;
				}
				if (scaleY <= 0) {
					scaleY = 1;
				}
				if (scaleX != 1 || scaleY != 1) {
					matrix.postScale(scaleX, scaleY);
					result = Bitmap.createBitmap(source, 0, 0, w, h, matrix,
							false);
				}
			}
		}
		return result;
	}
}
