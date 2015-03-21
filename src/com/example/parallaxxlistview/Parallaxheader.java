package com.example.parallaxxlistview;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Parallaxheader extends LinearLayout {
	private int headerHeight;

	private LinearLayout mContainer;
	private int mState = STATE_NORMAL;


	private final int ROTATE_ANIM_DURATION = 180;

	public final static int STATE_NORMAL = 0;
	public final static int STATE_READY = 1;
	public final static int STATE_REFRESHING = 2;
	public final static int STATE_SUCCEES = 3;

	private String pullText;
	private String toRefreshText;
	private String refreshingText;
	private String successText;
	private ImageView iv_pic;
	private ImageView iv_icon;
	private TextView tv_des;
	private MaterialHeader progress;
	private Handler mHandler = new Handler();
	private boolean isStateChanged = false;

	public Parallaxheader(Context context) {
		super(context);
		initView(context);
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public Parallaxheader(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	private void initView(Context context) {
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		mContainer = (LinearLayout) LayoutInflater.from(context).inflate(
				R.layout.parallaxxlistview_header, null);
		addView(mContainer, lp);
		setGravity(Gravity.BOTTOM);

		iv_pic = (ImageView) findViewById(R.id.iv_pic);
		iv_icon = (ImageView) findViewById(R.id.iv_icon);
		tv_des = (TextView) findViewById(R.id.tv_des);
		progress = (MaterialHeader) findViewById(R.id.progress);
	}

	public void setState(int state) {
		if (state == mState)
			return;
		isStateChanged = true;
		if (state == STATE_REFRESHING) {
			resetProgress();
		} else { 
		}

		switch (state) {
		case STATE_NORMAL:
			if (mState == STATE_READY) {
				resetProgress();
			}
			if (mState == STATE_REFRESHING) {
				resetProgress();
			}

			break;
		case STATE_READY:
			if (mState != STATE_READY) {
			}
			break;
		case STATE_REFRESHING:
			progress.startAnimation();
			break;
		case STATE_SUCCEES:
			resetProgress();
			break;
		default:
		}

		mState = state;
	}

	public void resetProgress(){
		progress.setVisibility(View.INVISIBLE);
		progress.stopAnimation();
	}


	private float currentProgress;

	public void setProgress(float progress) {
//		this.progress.setProgress(progress);
		if(this.progress.getVisibility()!=View.VISIBLE)
			this.progress.setVisibility(View.VISIBLE);
		
		this.progress.setProgress(progress);
	}
	
	public void setMaxProgress(float max){
//		progress.setMaxProgress(max);
	}

	public float getProgress() {
		return progress.getProgress();
	}

	public void setHeaderHeight(int height) {
		headerHeight = height;
	}

	public void setVisiableHeight(int height) {
		if (height < 0)
			height = 0;
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mContainer
				.getLayoutParams();
		lp.height = height + headerHeight;
//		setProgress(height);
		mContainer.setLayoutParams(lp);
	}

	public int getMoreVisiableHeight() {
		return mContainer.getHeight() - headerHeight;
	}

	public String getPullText() {
		return pullText;
	}

	public void setPullText(String pullText) {
		this.pullText = pullText;
	}

	public String getToRefreshText() {
		return toRefreshText;
	}

	public void setToRefreshText(String toRefreshText) {
		this.toRefreshText = toRefreshText;
	}

	public String getRefreshingText() {
		return refreshingText;
	}

	public void setRefreshingText(String refreshingText) {
		this.refreshingText = refreshingText;
	}

	public void setSuccessText(String successText) {
		this.successText = successText;
	}
}
