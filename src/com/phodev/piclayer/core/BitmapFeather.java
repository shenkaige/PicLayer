package com.phodev.piclayer.core;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;

/**
 * 
 * @author sky
 *
 */
public class BitmapFeather {
	private int[] mPxs;
	private int bw;
	private int bh;
	private int alpha = 0;
	private int perAlpha;
	private final int maxAlpha = 255;

	/**
	 * 圆形其区域羽化
	 * 
	 * @param source
	 *            必须是可编辑的Bitmap
	 * @param radius
	 *            羽化圆形区域边界半径
	 * @param cx
	 *            羽化中心点
	 * @param cy
	 *            羽化中心点
	 * @param feahterRadius
	 *            羽化半径
	 * @return
	 */
	public Bitmap makeFeahter(Bitmap source, int radius, int cx, int cy,
			int feahterRadius) {
		if (feahterRadius <= 0) {
			return source;
		}
		if (source == null || source.isRecycled()) {
			return source;
		}
		if (!source.isMutable()) {
			throw new RuntimeException(
					"BitmapFeather.makeFeahter()source is not Mutable");
		}
		bw = source.getWidth();
		bh = source.getHeight();
		int length = bw * bh;
		if (mPxs == null || mPxs.length != length) {
			mPxs = new int[length];
		}
		source.getPixels(mPxs, 0, bw, 0, 0, bw, bh);
		makeFeahter(mPxs, bw, bh, radius, cx, cy, feahterRadius);
		source.setPixels(mPxs, 0, bw, 0, 0, bw, bh);
		return source;
	}

	/**
	 * 羽化
	 * 
	 * @param pxs
	 * @param sourceW
	 * @param sourceH
	 * @param radius
	 * @param cx
	 * @param cy
	 * @param feahterRadius
	 * @return
	 */
	public int[] makeFeahter(int[] pxs, int sourceW, int sourceH, int radius,
			int cx, int cy, int feahterRadius) {
		if (feahterRadius <= 0) {
			return pxs;
		}
		bw = sourceW;
		bh = sourceH;
		mPxs = pxs;
		perAlpha = maxAlpha / feahterRadius;
		alpha = 0;
		int start = radius - feahterRadius;
		if (start <= 0) {
			return mPxs;
		}
		for (int i = start; i < radius; i++) {
			alpha += perAlpha;
			midPointCircle(cx, cy, i);
		}
		return mPxs;
	}

	/**
	 * 矩形区域羽化
	 * 
	 * @param source
	 * @param rect
	 *            开始羽化的位置,向外扩张
	 * @param feahterRadius
	 * @return
	 */
	public Bitmap makeFeahter(Bitmap source, Rect rect, int feahterRadius) {
		return null;
	}

	private int setAlpha(int color, int alpha) {
		int r = Color.red(color);
		int g = Color.green(color);
		int b = Color.blue(color);
		return Color.argb(alpha, r, g, b);
	}

	private void midPointCircle(int cx, int cy, int r) {
		int x, y;
		float d;
		x = 0;
		y = r;
		d = 1.25f - r;
		plot_circle_point(cx, cy, x, y);
		while (x <= y) {
			if (d < 0)
				d += 2 * x + 3;
			else {
				d += 2 * (x - y) + 5;
				y--;
			}
			x++;
			plot_circle_point(cx, cy, x, y);
		}
	}

	private void plot_circle_point(int xc, int yc, int x, int y) {
		putpixel(xc + x, yc + y);
		putpixel(xc - x, yc + y);
		putpixel(xc + x, yc - y);
		putpixel(xc - x, yc - y);
		putpixel(xc + y, yc + x);
		putpixel(xc - y, yc + x);
		putpixel(xc + y, yc - x);
		putpixel(xc - y, yc - x);
	}

	// private void CirclePoints(int x, int y, int color) {
	// putpixel(x, y, color);
	// putpixel(y, x, color);
	// putpixel(-x, y, color);
	// putpixel(y, -x, color);
	// putpixel(x, -y, color);
	// putpixel(-y, x, color);
	// putpixel(-x, -y, color);
	// putpixel(-y, -x, color);
	// }

	public void putpixel(int x, int y) {
		if (x < 0 || y < 0) {
			return;
		}
		if (x > bw || y > bh) {
			return;
		}
		int index = y * bw + x;
		if (index >= 0 && index < mPxs.length) {
			int argb = mPxs[index];
			mPxs[index] = setAlpha(argb, alpha);

			index = (y - 1) * bw + x;
			if (index >= 0 && index < mPxs.length) {
				argb = mPxs[index];
				mPxs[index] = setAlpha(argb, alpha);
			}
		}
		// index = (x + 1) * bw + y;
		// argb = pxs[index];
		// pxs[index] = setAlpha(argb, alpha);
		// index = x * bw + y + 1;
		// argb = pxs[index];
		// pxs[index] = setAlpha(argb, alpha);
		// index = x * bw + y - 1;
		// argb = pxs[index];
		// pxs[index] = setAlpha(argb, alpha);
	}

	public void putpixel2(int x, int y) {
		if (x < 0 || y < 0) {
			return;
		}
		if (x > bw || y > bh) {
			return;
		}
		int index = y * bw + x;
		if (index >= 0 && index < mPxs.length) {
			int argb = mPxs[index];
			mPxs[index] = setAlpha(argb, alpha);
			index = (y - 1) * bw + x;
			if (index >= 0 && index < mPxs.length) {
				argb = mPxs[index];
				mPxs[index] = setAlpha(argb, alpha);
			}
			index = y * bw + x - 1;
			if (index >= 0 && index < mPxs.length) {
				argb = mPxs[index];
				mPxs[index] = setAlpha(argb, alpha);
			}

		}
	}

	/**
	 * 矩形羽化
	 * 
	 * @param pxs
	 * @param sourceW
	 * @param sourceH
	 * @param pa
	 * @param pb
	 * @param pc
	 * @param pd
	 * @param feahterRadius
	 * @param entadErrorRadius
	 *            从羽化半径开始，允许的误差半径，改半径概括的像素点的alpha会被设置成纯透明
	 * @return
	 */
	public int[] makeFeahter(int[] pxs, int sourceW, int sourceH, PointF pa,
			PointF pb, PointF pc, PointF pd, int feahterRadius,
			int entadErrorRadius) {
		if (feahterRadius <= 0) {
			return pxs;
		}
		bw = sourceW;
		bh = sourceH;
		mPxs = pxs;
		perAlpha = -maxAlpha / feahterRadius;
		alpha = 255;
		int startX = (int) pa.x;
		int startY = (int) pa.y;
		int endX = (int) pd.x;
		// int endY = (int) pd.y;
		//
		final float adCenterX = (startX + endX) / 2;// a点，d点两点直线的中点
		final float abCenterX = (startX + pb.x) / 2;// a点，d点两点直线的中点
		float abCenterY = (startY + pb.y) / 2;// a点，d点两点直线的中点
		//
		float k2 = (float) (startY - pd.y) / (startX - pd.x);// 直线斜率
		// 直线方程Ax+By+C=0;a,b,c
		float[] lines2 = new float[3];
		PictureUtils.getLine(lines2, abCenterX, abCenterY, k2);// 获取直线方程
		// 设置点对称属性
		symmetricPointsDrawer.setSymmeticLine(lines2[0], lines2[1], lines2[2],
				adCenterX);
		PointF result = new PointF();
		int x1, y1;
		for (int i = 0; i <= feahterRadius; i++) {
			alpha += perAlpha;
			getOffsetPoint(result, pa, pb, i);
			x1 = (int) result.x;
			y1 = (int) result.y;
			getOffsetPoint(result, pd, pc, i);
			ergodicLine(x1, y1, (int) result.x, (int) result.y,
					symmetricPointsDrawer);
		}
		// 进行误差处理
		alpha = 0;
		int j = feahterRadius + entadErrorRadius;
		for (int i = feahterRadius; i <= j; i++) {
			getOffsetPoint(result, pa, pb, i);
			x1 = (int) result.x;
			y1 = (int) result.y;
			getOffsetPoint(result, pd, pc, i);
			ergodicLine(x1, y1, (int) result.x, (int) result.y,
					symmetricPointsDrawer);
		}
		/**/
		// debug
		// ergodicLine((int) pa.x, (int) pa.y, (int) pb.x, (int) pb.y,
		// pointDrawBack);
		// ergodicLine((int) pb.x, (int) pb.y, (int) pc.x, (int) pc.y,
		// pointDrawBack);
		// ergodicLine((int) pc.x, (int) pc.y, (int) pd.x, (int) pd.y,
		// pointDrawBack);
		// ergodicLine((int) pd.x, (int) pd.y, (int) pa.x, (int) pa.y,
		// pointDrawBack);
		// debug end
		return mPxs;
	}

	/**
	 * 遍历直线上的点
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param color
	 * @param drawer
	 */
	void ergodicLine(int x1, int y1, int x2, int y2, DrawPointCallBack drawer) {
		int dx, dy, e;
		dx = x2 - x1;
		dy = y2 - y1;
		if (dx >= 0) {
			if (dy >= 0) // dy>=0
			{
				if (dx >= dy) // 1/8 octant
				{
					// Log.e("", "1/8 octant");
					e = dy - dx / 2;
					while (x1 <= x2) {
						drawer.drawPoint(x1, y1);
						if (e > 0) {
							y1 += 1;
							e -= dx;
						}
						x1 += 1;
						e += dy;
					}
				} else // 2/8 octant
				{
					// Log.e("", "2/8 octant");
					e = dx - dy / 2;
					while (y1 <= y2) {
						drawer.drawPoint(x1, y1);
						if (e > 0) {
							x1 += 1;
							e -= dy;
						}
						y1 += 1;
						e += dx;
					}
				}
			} else // dy<0
			{
				dy = -dy; // dy=abs(dy)

				if (dx >= dy) // 8/8 octant
				{
					// Log.e("", "8/8 octant");
					e = dy - dx / 2;
					while (x1 <= x2) {
						drawer.drawPoint(x1, y1);
						if (e > 0) {
							y1 -= 1;
							e -= dx;
						}
						x1 += 1;
						e += dy;
					}
				} else // 7/8 octant
				{
					// Log.e("", "7/8 octant");
					e = dx - dy / 2;
					while (y1 >= y2) {
						drawer.drawPoint(x1, y1);
						if (e > 0) {
							x1 += 1;
							e -= dy;
						}
						y1 -= 1;
						e += dx;
					}
				}
			}
		} else // dx<0
		{
			dx = -dx; // dx=abs(dx)
			if (dy >= 0) // dy>=0
			{
				if (dx >= dy) // 4/8 octant
				{
					// Log.e("", "4/8 octant");
					e = dy - dx / 2;
					while (x1 >= x2) {
						drawer.drawPoint(x1, y1);
						if (e > 0) {
							y1 += 1;
							e -= dx;
						}
						x1 -= 1;
						e += dy;
					}
				} else // 3/8 octant
				{
					// Log.e("", "3/8 octant");
					e = dx - dy / 2;
					while (y1 <= y2) {
						drawer.drawPoint(x1, y1);
						if (e > 0) {
							x1 -= 1;
							e -= dy;
						}
						y1 += 1;
						e += dx;
					}
				}
			} else // dy<0
			{
				dy = -dy; // dy=abs(dy)

				if (dx >= dy) // 5/8 octant
				{
					// Log.e("", "5/8 octant");
					e = dy - dx / 2;
					while (x1 >= x2) {
						drawer.drawPoint(x1, y1);
						if (e > 0) {
							y1 -= 1;
							e -= dx;
						}
						x1 -= 1;
						e += dy;
					}
				} else // 6/8 octant
				{
					// Log.e("", "6/8 octant");
					e = dx - dy / 2;
					while (y1 >= y2) {
						drawer.drawPoint(x1, y1);
						if (e > 0) {
							x1 -= 1;
							e -= dy;
						}
						y1 -= 1;
						e += dx;
					}
				}
			}
		}
	}

	/**
	 * 直线上点遍历回调
	 * 
	 * @author skg
	 * 
	 */
	public interface DrawPointCallBack {
		public void drawPoint(int x, int y);
	}

	// 直线点遍历回调
	DrawPointCallBack pointDrawBack = new DrawPointCallBack() {
		@Override
		public void drawPoint(int x, int y) {
			putpixel(x, y);
		}
	};
	// 对称点回调
	SymmetricPointsDrawer symmetricPointsDrawer = new SymmetricPointsDrawer();

	/**
	 * 点回调，顺便求出对称点
	 * 
	 * @author skg
	 * 
	 */
	class SymmetricPointsDrawer implements DrawPointCallBack {
		float a;
		float b;
		float c;
		float lineX;
		PointF result;

		SymmetricPointsDrawer() {
			result = new PointF();
		}

		/**
		 * 设置对称点求 参考线(Ax+By+C=0)
		 * 
		 * @param a
		 * @param b
		 * @param c
		 * @param lineCenterX
		 *            如果斜率不存在的话，就按照该x的中心点求对称点
		 */
		public void setSymmeticLine(float a, float b, float c, float lineCenterX) {
			this.a = a;
			this.b = b;
			this.c = c;
			lineX = lineCenterX;
		}

		@Override
		public void drawPoint(int x, int y) {
			PictureUtils.getSymmetryPoint(result, x, y, a, b, c, lineX);
			putpixel2(x, y);
			putpixel2((int) result.x, (int) result.y);
		}

	}

	private void getOffsetPoint(PointF result, PointF ap, PointF bp,
			float offset) {
		// 平移向量起始点坐标
		float dX = ap.x - bp.x;
		float dY = ap.y - bp.y;
		float distAB = (float) Math.sqrt(dX * dX + dY * dY);
		float d = offset / distAB;
		result.x = ap.x - d * dX;
		result.y = ap.y - d * dY;
	}

}
