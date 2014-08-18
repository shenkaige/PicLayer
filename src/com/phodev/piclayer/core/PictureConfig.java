package com.phodev.piclayer.core;

import android.content.Context;
import android.graphics.Color;

/**
 * 图片编辑属性配置
 * 
 * @author sky
 * 
 */
public class PictureConfig {
	public final static boolean DEBUG = true;
	private static int dot_r_bg = 12;// 点的半径
	private static int dot_r_inner = 10;// 点你的内半径
	private static int dot_click_area_expand = 12;// 圆点点击区域扩展
	private static int dot_click_area_r = dot_r_bg + dot_click_area_expand;// 圆点被点击的区域半径
	private static int dot_cancle_mark_lines_width = 3;
	private static int picture_bg_padding = 2;// 试装图片背景padding
	private static int clip_area_text_size = 16;
	private static float clip_area_grid_border_line_widht = 1.5f;
	private static float clip_area_grid_inner_line_widht = 1.5f;
	private static float picBorderWidth = 1;
	// ///////////////////////////////////////////////////////////////////
	public final static int dot_color_bg = 0xFF6e6e6e;
	public final static int dot_color_bg_pressed = 0xFF20abfa;
	public final static int dot_color_inner = Color.WHITE;
	// Cancel Button属性, 'X'删除的绘画信息
	public final static int dot_cancle_color_bg = Color.RED;
	public final static int dot_cancle_mark_color = Color.WHITE;
	// 试装图片背景padding的color
	public final static int picture_bg_color = Color.WHITE;
	// 剪切层的半透明背景颜色
	public final static int clip_layer_bg_color = 0x8e000000;
	// 例如 240x320，480x640也可以写成240*320,或者240:320
	public final static char pictuer_size_asterisk = 'x';
	public final static int clip_area_text_color = Color.WHITE;
	public final static int clip_area_grid_border_line_color = 0xFFFFFFFF;
	public final static int clip_area_grid_inner_line_color = 0x60eeeeee;
	public final static int clip_area_color = 0x32FFFFFF;
	public final static int clip_layer_min_h = 50;// 最小剪切尺寸
	public final static int clip_layer_min_w = 50;// 最小剪切尺寸
	public final static float clip_area_w_h_scale = 0.75f;// 固定比例
	public final static boolean clip_area_scale_restrain = false;
	public final static int frame_def_bg_color = Color.WHITE;

	// ===========================图片效果矩阵========================//
	// // 原图
	// public static final float colormatrix_lomo[] = { 1.7f, 0.1f, 0.1f, 0,
	// -73.1f, 0, 1.7f, 0.1f, 0, -73.1f, 0, 0.1f, 1.6f, 0, -73.1f, 0, 0,
	// 0, 1.0f, 0 };
	// 黑白
	public static final float colormatrix_heibai[] = { 0.8f, 1.6f, 0.2f, 0,
			-163.9f, 0.8f, 1.6f, 0.2f, 0, -163.9f, 0.8f, 1.6f, 0.2f, 0,
			-163.9f, 0, 0, 0, 1.0f, 0 };
	// 怀旧
	public static final float colormatrix_huajiu[] = { 0.2f, 0.5f, 0.1f, 0,
			40.8f, 0.2f, 0.5f, 0.1f, 0, 40.8f, 0.2f, 0.5f, 0.1f, 0, 40.8f, 0,
			0, 0, 1, 0 };
	// 哥特
	public static final float colormatrix_gete[] = { 1.9f, -0.3f, -0.2f, 0,
			-87.0f, -0.2f, 1.7f, -0.1f, 0, -87.0f, -0.1f, -0.6f, 2.0f, 0,
			-87.0f, 0, 0, 0, 1.0f, 0 };
	// 淡雅
	public static final float colormatrix_danya[] = { 0.6f, 0.3f, 0.1f, 0,
			73.3f, 0.2f, 0.7f, 0.1f, 0, 73.3f, 0.2f, 0.3f, 0.4f, 0, 73.3f, 0,
			0, 0, 1.0f, 0 };
	// 蓝调
	public static final float colormatrix_landiao[] = { 2.1f, -1.4f, 0.6f,
			0.0f, -71.0f, -0.3f, 2.0f, -0.3f, 0.0f, -71.0f, -1.1f, -0.2f, 2.6f,
			0.0f, -71.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f };
	// 光晕
	public static final float colormatrix_guangyun[] = { 0.9f, 0, 0, 0, 64.9f,
			0, 0.9f, 0, 0, 64.9f, 0, 0, 0.9f, 0, 64.9f, 0, 0, 0, 1.0f, 0 };

	// 梦幻
	public static final float colormatrix_menghuan[] = { 0.8f, 0.3f, 0.1f,
			0.0f, 46.5f, 0.1f, 0.9f, 0.0f, 0.0f, 46.5f, 0.1f, 0.3f, 0.7f, 0.0f,
			46.5f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f };
	// 酒红
	public static final float colormatrix_jiuhong[] = { 1.2f, 0.0f, 0.0f, 0.0f,
			0.0f, 0.0f, 0.9f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.8f, 0.0f, 0.0f,
			0, 0, 0, 1.0f, 0 };
	// 胶片
	public static final float colormatrix_fanse[] = { -1.0f, 0.0f, 0.0f, 0.0f,
			255.0f, 0.0f, -1.0f, 0.0f, 0.0f, 255.0f, 0.0f, 0.0f, -1.0f, 0.0f,
			255.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f };
	// 湖光掠影
	public static final float colormatrix_huguang[] = { 0.8f, 0.0f, 0.0f, 0.0f,
			0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.9f, 0.0f, 0.0f,
			0, 0, 0, 1.0f, 0 };
	// 褐片
	public static final float colormatrix_hepian[] = { 1.0f, 0.0f, 0.0f, 0.0f,
			0.0f, 0.0f, 0.8f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.8f, 0.0f, 0.0f,
			0, 0, 0, 1.0f, 0 };
	// 复古
	public static final float colormatrix_fugu[] = { 0.9f, 0.0f, 0.0f, 0.0f,
			0.0f, 0.0f, 0.8f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.5f, 0.0f, 0.0f,
			0, 0, 0, 1.0f, 0 };
	// 泛黄
	public static final float colormatrix_huan_huang[] = { 1.0f, 0.0f, 0.0f,
			0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.5f, 0.0f,
			0.0f, 0, 0, 0, 1.0f, 0 };
	// 传统
	public static final float colormatrix_chuan_tong[] = { 1.0f, 0.0f, 0.0f, 0,
			-10f, 0.0f, 1.0f, 0.0f, 0, -10f, 0.0f, 0.0f, 1.0f, 0, -10f, 0, 0,
			0, 1, 0 };
	// 胶片2
	public static final float colormatrix_jiao_pian[] = { 0.71f, 0.2f, 0.0f,
			0.0f, 60.0f, 0.0f, 0.94f, 0.0f, 0.0f, 60.0f, 0.0f, 0.0f, 0.62f,
			0.0f, 60.0f, 0, 0, 0, 1.0f, 0 };

	// // 锐色
	// public static final float colormatrix_ruise[] = { 4.8f, -1.0f, -0.1f, 0,
	// -388.4f, -0.5f, 4.4f, -0.1f, 0, -388.4f, -0.5f, -1.0f, 5.2f, 0,
	// -388.4f, 0, 0, 0, 1.0f, 0 };
	// // 清宁
	// public static final float colormatrix_qingning[] = { 0.9f, 0, 0, 0, 0, 0,
	// 1.1f, 0, 0, 0, 0, 0, 0.9f, 0, 0, 0, 0, 0, 1.0f, 0 };
	// // 浪漫
	// public static final float colormatrix_langman[] = { 0.9f, 0, 0, 0, 63.0f,
	// 0, 0.9f, 0, 0, 63.0f, 0, 0, 0.9f, 0, 63.0f, 0, 0, 0, 1.0f, 0 };
	//
	// // 夜色
	// public static final float colormatrix_yese[] = { 1.0f, 0.0f, 0.0f, 0.0f,
	// -66.6f, 0.0f, 1.1f, 0.0f, 0.0f, -66.6f, 0.0f, 0.0f, 1.0f, 0.0f,
	// -66.6f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f };
	// ===========================图片效果矩阵============================//

	public static final class Frame {
		private int mFrameName;
		private int mDelegateImgResId;
		private int mFrameResId;

		public Frame(int frameResId, int delegateImgResId, int nameResId) {
			mFrameName = nameResId;
			mDelegateImgResId = delegateImgResId;
			mFrameResId = frameResId;
		}

		public int getFrameNameId() {
			return mFrameName;
		}

		public int getDelegateImgResId() {
			return mDelegateImgResId;
		}

		public int getFrameResId() {
			return mFrameResId;
		}

	}

	public static final class TextBg {
		private int mTextBg;
		private int mTextSrc;
		private int mTextBigSrc;
		private int mTextColor;

		public TextBg(int resId, int reBigId, int color) {
			// mTextBg = bgResId;
			mTextSrc = resId;
			mTextBigSrc = reBigId;
			mTextColor = color;
		}

		public int getTextBg() {
			return mTextBg;
		}

		public int getTextSrc() {
			return mTextSrc;
		}

		public int getTextBigSrc() {
			return mTextBigSrc;
		}

		public int getTextColor() {
			return mTextColor;
		}
	}

	static class PictureDescription {
		private int mNameResId;
		private int mDelegateImgResId;

		public void setName(int nameResId) {
			mNameResId = nameResId;
		}

		public void setDelegateImgResId(int delegateImgResId) {
			mDelegateImgResId = delegateImgResId;
		}

		public int getNameResId() {
			return mNameResId;
		}

		public int getDelegateImgResId() {
			return mDelegateImgResId;
		}
	}

	public static void initConfig(Context context) {
		if (context == null)
			return;
		float density = context.getResources().getDisplayMetrics().density;
		dot_r_bg = (int) (dot_r_bg * density);
		dot_r_inner = (int) (dot_r_inner * density);
		dot_click_area_expand = (int) (dot_click_area_expand * density);
		dot_click_area_r = (int) (dot_click_area_r * density);
		dot_cancle_mark_lines_width = (int) (dot_cancle_mark_lines_width * density);
		picture_bg_padding = (int) (picture_bg_padding * density);
		clip_area_text_size = (int) (clip_area_text_size * density);

		clip_area_grid_border_line_widht *= density;
		clip_area_grid_inner_line_widht *= density;
		defaultCornerRadius *= density;
		defaultBlurCircleRadisu *= density;
		picBorderWidth *= density;
	}

	/**
	 * 点的半径
	 * 
	 * @return
	 */
	public static int getDotRadius() {
		return dot_r_bg;
	}

	/**
	 * 点你的内半径
	 * 
	 * @return
	 */
	public static int getDotRadiusInner() {
		return dot_r_inner;
	}

	/**
	 * 圆点点击区域扩展
	 * 
	 * @return
	 */
	public static int getDotClickAreaExpand() {
		return dot_click_area_expand;
	}

	/**
	 * 圆点被点击的区域半径
	 * 
	 * @return
	 */
	public static int getDotClickAreaRadius() {
		return dot_click_area_r;
	}

	/**
	 * Cancel Button属性, 'X'删除的绘画信息
	 * 
	 * @return
	 */
	public static int getDotCancleMarkLinesWidth() {
		return dot_cancle_mark_lines_width;
	}

	/**
	 * 试装图片背景padding
	 * 
	 * @return
	 */
	public static int getPictureBgPadding() {
		return picture_bg_padding;

	}

	/**
	 * 裁剪区域字体大小
	 * 
	 * @return
	 */
	public static int getClipAreaTextSize() {
		return clip_area_text_size;
	}

	/**
	 * 剪切区域网格边框的宽度
	 * 
	 * @return
	 */
	public static float getClipAreaBorderLineWidht() {
		return clip_area_grid_border_line_widht;
	}

	/**
	 * 裁剪区域网格线的宽度
	 * 
	 * @return
	 */
	public static float getClipAreaInnerLineWidht() {
		return clip_area_grid_inner_line_widht;
	}

	private static int defaultCornerRadius = 4;

	public static int getDefualtCornerRadius() {
		return defaultCornerRadius;
	}

	private static int defaultBlurCircleRadisu = 80;

	public static int getDefualtBlurCircleRadius() {
		return defaultBlurCircleRadisu;
	}

	public static float getBackgroundBorderWidth() {
		return picBorderWidth;
	}
}
