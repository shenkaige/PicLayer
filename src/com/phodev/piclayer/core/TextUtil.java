package com.phodev.piclayer.core;

import java.util.Vector;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;

/**
 * 
 * @author sky
 *
 */
public class TextUtil {
	private float mTextWidth = 0;// 绘制宽度
	private float mTextHeight = 0;// 绘制高度
	private int mFontHeight = 0;// 绘制字体高度
	private int mPageLineNum = 0;// 每一页显示的行数
	private int mFontColor = 0;// 字体颜色
	private int mRealLine = 0;// 字符串真实的行数
	private float mOriginalSize = 0;
	private float mTextSize = 0;// 字体大小
	private String mStrText = "";
	private Vector mString = null;
	private Paint mPaint = null;
	private float offset;
	private String input = "点击输入文字";
	private int mMaxNum = 50;

	public TextUtil(String StrText, float w, float h, int textcolor,
			float textsize) {
		mPaint = new Paint();
		mString = new Vector();
		this.mStrText = StrText;
		this.mTextWidth = w;
		this.mTextHeight = h;
		this.mFontColor = textcolor;
		this.mTextSize = textsize;
		this.mOriginalSize = textsize;
		InitText();
	}

	public void InitText() {
		// 清空Vector
		// 对画笔属性的设置
		// mPaint.setARGB(this.mAlpha, Color.red(this.mFontColor), Color
		// .green(this.mFontColor), Color.blue(this.mFontColor));
		mStrText = mStrText + input;
		mPaint.setTextSize(this.mTextSize);
		mPaint.setColor(mFontColor);
		mPaint.setAntiAlias(true);
		calculateLine("");
	}

	/**
	 * 得到字符串信息包括行数，页数等信息
	 */
	public void AddText(String appendString) {
		if (mStrText.equals(input))
			mStrText = "";
		if (mStrText.length() == mMaxNum)
			return;
		calculateLine(appendString);
		while (mRealLine > mPageLineNum + 1) {
			float delt;
			if (mTextSize > 10)
				delt = 1;
			else
				delt = 0.2f;
			if (mTextSize == 0.2)
				return;
			mTextSize = mTextSize - delt;
			mPaint.setTextSize(mTextSize);
			calculateLine("");
		}
	}

	public void deleteText() {
		if (mStrText.length() == 0)
			return;
		if (mStrText.length() == 1) {
			mStrText = "";
			calculateLine("");
		} else {
			mStrText = mStrText.substring(0, mStrText.length() - 1);
			calculateLine("");
			if (mPaint.getTextSize() == mOriginalSize)
				return;
			while (mRealLine < mPageLineNum + 1) {
				float delt;
				if (mTextSize > 10)
					delt = 1;
				else
					delt = 0.2f;
				mTextSize = mTextSize + delt;
				mPaint.setTextSize(mTextSize);
				calculateLine("");
			}
		}
	}

	private void calculateLine(String appendString) {
		mString.clear();
		if (!appendString.equals("")) {
			mStrText = mStrText + appendString;
			if (mStrText.length() > mMaxNum) {
				mStrText = mStrText.substring(0, mMaxNum);
			}
		}
		mRealLine = 0;
		FontMetrics fm = mPaint.getFontMetrics();// 得到系统默认字体属性
		mFontHeight = (int) (Math.ceil(fm.descent - fm.top));// 获得字体高度
		mPageLineNum = (int) ((mTextHeight - mTextSize) / mFontHeight);// 获得行数
		char ch;
		int w = 0;
		int istart = 0;
		int count = this.mStrText.length();
		for (int i = 0; i < count; i++) {
			ch = this.mStrText.charAt(i);
			float[] widths = new float[1];
			String str = String.valueOf(ch);
			mPaint.getTextWidths(str, widths);
			if (ch == '\n') {
				mRealLine++;// 真实的行数加一
				mString.addElement(this.mStrText.substring(istart, i));
				istart = i + 1;
				w = 0;
			} else {
				w += (int) Math.ceil(widths[0]);
				if (w > this.mTextWidth) {
					mRealLine++;// 真实的行数加一
					mString.addElement(this.mStrText.substring(istart, i));
					istart = i;
					i--;
					w = 0;
				} else {
					if (i == count - 1) {
						mRealLine++;// 真实的行数加一
						mString.addElement(this.mStrText.substring(istart,
								count));
					}
				}
			}
		}
	}

	/**
	 * 绘制字符串
	 * 
	 * @param canvas
	 */
	public void DrawText(Canvas canvas, float startX, float startY,
			boolean islight) {
		float deltX = 0;
		float deltY = 0;
		mPaint.setAlpha(255);
		if (mRealLine == 0) {
			deltX = mTextWidth / 2;
			deltY = (mPageLineNum + 1) * mFontHeight / 2 + this.mFontHeight
					* mRealLine;
		}
		for (int i = 0, j = 0; i < this.mRealLine; i++, j++) {
			if (j > this.mPageLineNum) {
				break;
			}
			if (mRealLine == 1) {
				float strLen = mPaint.measureText(mStrText);
				deltX = (mTextWidth - strLen) / 2;
			}
			deltY = (mPageLineNum + 1 - mRealLine) * mFontHeight / 2;
			if (deltY < 0)
				deltY = 0;
			canvas.drawText((String) (mString.elementAt(i)), startX + deltX,
					startY + deltY + mTextSize + this.mFontHeight * j, mPaint);
		}
		if (mStrText.equals(input))
			return;
		if (islight)
			mPaint.setAlpha(255);
		else
			mPaint.setAlpha(0);
		float strlen = 0;
		if (mRealLine != 0)
			strlen = mPaint.measureText((String) mString
					.elementAt(mRealLine - 1));
		canvas.drawLine(startX + deltX + strlen, startY + deltY
				+ this.mFontHeight * (mRealLine - 1), startX + deltX + strlen,
				startY + deltY + this.mFontHeight + this.mFontHeight
						* (mRealLine - 1), mPaint);
	}
}
