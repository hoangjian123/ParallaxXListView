package com.example.parallaxxlistview;

import android.content.Context;
import android.content.res.Resources;

/**
 * 用于转换界面尺寸的工具类
 */
public class HPDisplayUtil {
	public static int screenW;
	public static int screenH;
	
	public static int convertDIP2PX(Context context, float dip) {
		Resources resources = context.getResources();
		float scale = resources.getDisplayMetrics().density;
		resources = null;
		return (int) (dip * scale + 0.5f * (dip >= 0 ? 1 : -1));
	}

	// 转换px为dip
	public static int convertPX2DIP(Context context, float px) {
		Resources resources = context.getResources();
		float scale = resources.getDisplayMetrics().density;
		resources = null;
		return (int) (px / scale + 0.5f * (px >= 0 ? 1 : -1));
	}

	/**
	 * 将px值转换为sp值，保证文字大小不变
	 * 
	 * @param pxValue
	 * @param fontScale
	 *            （DisplayMetrics类中属性scaledDensity）
	 * @return
	 */
	public static int px2sp(Context context, float pxValue) {
		Resources resources = context.getResources();
		final float fontScale = resources.getDisplayMetrics().scaledDensity;
		resources=null;
		return (int) (pxValue / fontScale + 0.5f);
	}

	/**
	 * 将sp值转换为px值，保证文字大小不变
	 * 
	 * @param spValue
	 * @param fontScale
	 *            （DisplayMetrics类中属性scaledDensity）
	 * @return
	 */
	public static int sp2px(Context context, float spValue) {
		Resources resources = context.getResources();
		final float fontScale = resources.getDisplayMetrics().scaledDensity;
		resources=null;
		return (int) (spValue * fontScale + 0.5f);
	}
}
