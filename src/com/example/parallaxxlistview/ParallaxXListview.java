package com.example.parallaxxlistview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.TextView;

public class ParallaxXListview extends ListView implements OnScrollListener {
	private static final String TAG = "XListView";
	private float mLastY = -1; // save event y
	private Scroller mScroller; // used for scroll back
	private OnScrollListener mScrollListener; // user's scroll listener

	// the interface to trigger refresh and load more.
	private IXListViewListener mListViewListener;

	// -- header view
	private Parallaxheader mHeaderView;
	// header view content, use it to calculate the Header's height. And hide it
	// when disable pull refresh.
	private RelativeLayout mHeaderViewContent;
	
	private int mHeaderViewHeight; // header view's height
	private boolean mEnablePullRefresh = true;
	private boolean mPullRefreshing = false; // is refreashing.
	private boolean mPullRefreshSuccess = false;
	// -- footer view
	private XListViewFooter mFooterView;
	private boolean mEnablePullLoad;
	private boolean mPullLoading;
	private boolean mIsFooterReady = false;
	private boolean mHasMoreData = true;

	// total list items, used to detect is at the bottom of listview.
	private int mTotalItemCount;

	// for mScroller, scroll back from header or footer.
	private int mScrollBack;
	private final static int SCROLLBACK_HEADER = 0;
	private final static int SCROLLBACK_FOOTER = 1;

	private final static int SCROLL_DURATION = 400; // scroll back duration
	private final static int PULL_LOAD_MORE_DELTA = 50; // when pull up >= 50px
														// at bottom, trigger
														// load more.
	private final static float OFFSET_RADIO = 2.0f; // support iOS like pull

	private boolean isWaitToStopRefresh = false;

	private final static int STOP_REFRESH_DURATION = 1000;

	private final static int MIN_REFRSH_HEIGHT = 200;

	private final int[] firstItemLocation = new int[2];
	private final int[] listviewLocation = new int[2];

	// feature.

	/**
	 * @param context
	 */
	public ParallaxXListview(Context context) {
		super(context);
		initWithContext(context);
	}

	public ParallaxXListview(Context context, AttributeSet attrs) {
		super(context, attrs);
		initWithContext(context);
	}

	public ParallaxXListview(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initWithContext(context);
	}

	private void initWithContext(Context context) {
		mScroller = new Scroller(context, new DecelerateInterpolator());
		// XListView need the scroll event, and it will dispatch the event to
		// user's listener (as a proxy).
		super.setOnScrollListener(this);

		// init footer view
		mFooterView = new XListViewFooter(context);

		// getViewTreeObserver().addOnGlobalLayoutListener(new
		// OnGlobalLayoutListener() {
		//
		// @Override
		// public void onGlobalLayout() {
		// // TODO Auto-generated method stub
		// setMinimumHeight();
		// }
		// });
	}

	public void addHeaderView() {
		// init header view
		mHeaderView = new Parallaxheader(getContext());
//		mHeaderView.setMaxProgress(MIN_REFRSH_HEIGHT);
		mHeaderViewContent = (RelativeLayout) mHeaderView
				.findViewById(R.id.rl_header);
		addHeaderView(mHeaderView);
		// init header height
		mHeaderView.getViewTreeObserver().addOnGlobalLayoutListener(
				new OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						mHeaderViewHeight = mHeaderViewContent.getHeight();
						mHeaderView.setHeaderHeight(mHeaderViewHeight);
						getViewTreeObserver()
								.removeGlobalOnLayoutListener(this);
					}
				});
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		// make sure XListViewFooter is the last footer view, and only add once.
		if (mIsFooterReady == false) {
			mIsFooterReady = true;
			addFooterView(mFooterView);
		}
		super.setAdapter(adapter);
	}

	/**
	 * enable or disable pull down refresh feature.
	 * 
	 * @param enable
	 */
	public void setPullRefreshEnable(boolean enable) {
		if (mHeaderView == null) {
			addHeaderView();
		}

		mEnablePullRefresh = enable;
		if (!mEnablePullRefresh) { // disable, hide the content
			mHeaderViewContent.setVisibility(View.INVISIBLE);
		} else {
			mHeaderViewContent.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * enable or disable pull up load more feature.
	 * 
	 * @param enable
	 */
	public void setPullLoadEnable(boolean enable) {
		mEnablePullLoad = enable;
		if (!mEnablePullLoad) {
			mFooterView.hide();
			mFooterView.setOnClickListener(null);
		} else {
			mPullLoading = false;
			mFooterView.show();
			mFooterView.setState(XListViewFooter.STATE_NORMAL);
			// both "pull up" and "click" will invoke load more.
			mFooterView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mHasMoreData)
						startLoadMore();
				}
			});
		}
	}

	/**
	 * stop refresh, reset header view.
	 */
	public void stopRefresh() {
		if (mPullRefreshing == true) {
			mPullRefreshing = false;
			mHeaderView.setState(mHeaderView.STATE_SUCCEES);
			mPullRefreshSuccess = true;
			resetHeaderHeight(isWaitToStopRefresh);
		}
	}

	/**
	 * 设置是否等待后stopRefrsh
	 * 
	 * @param isWaitToStopRefresh
	 */
	public void setWaitToStopRefresh(boolean isWaitToStopRefresh) {
		this.isWaitToStopRefresh = isWaitToStopRefresh;
	}

	/**
	 * stop load more, reset footer view.
	 */
	public void stopLoadMore() {
		if (mPullLoading == true) {
			mPullLoading = false;
			mFooterView.setState(XListViewFooter.STATE_NORMAL);
		}
	}

	/**
	 * set last refresh time
	 * 
	 * @param time
	 */
	public void setRefreshTime(String time) {
//		mHeaderTimeView.setText(time);
	}

	private void invokeOnScrolling() {
		if (mScrollListener instanceof OnXScrollListener) {
			OnXScrollListener l = (OnXScrollListener) mScrollListener;
			l.onXScrolling(this);
		}
	}

	@SuppressLint("NewApi")
	private void updateHeaderHeight(float delta) {

		int moreVisalbleHeight = mHeaderView.getMoreVisiableHeight();
		// float progress=mHeaderView.getProgress();
		mHeaderView.setVisiableHeight((int) delta + moreVisalbleHeight);
		mHeaderView.setProgress(Math.min(mHeaderView.getMoreVisiableHeight()
				/ (MIN_REFRSH_HEIGHT * 1f), 1f));
		if (mEnablePullRefresh && !mPullRefreshing) { // 未处于刷新状态，更新箭头
			if (mHeaderView.getMoreVisiableHeight() > MIN_REFRSH_HEIGHT) {
				mHeaderView.setState(Parallaxheader.STATE_READY);
			} else {
				mHeaderView.setState(Parallaxheader.STATE_NORMAL);
			}
		}
		// if (VERSION.SDK_INT >= 11)
		// smoothScrollToPositionFromTop(0, 0, 500);
		// else
		setSelection(0); // scroll to top each time
	}

	/**
	 * reset header view's height.
	 */
	private void resetHeaderHeight(boolean isWait) {
		final int height = mHeaderView.getMoreVisiableHeight();
		if (height == 0) // not visible.
			return;
		// refreshing and header isn't shown fully. do nothing.
		if (mPullRefreshing && height <= MIN_REFRSH_HEIGHT) {
			return;
		}
		int finalHeight = 0; // default: scroll back to dismiss header.
		// is refreshing, just scroll back to show all the header.
		// if (mPullRefreshing && height > MIN_REFRSH_HEIGHT) {
		// finalHeight = MIN_REFRSH_HEIGHT;
		// }
		mScrollBack = SCROLLBACK_HEADER;
		// if (isWait) {
		// new Handler().postDelayed(new Runnable() {
		// @Override
		// public void run() {
		// // TODO Auto-generated method stub
		// if (mPullRefreshSuccess) {
		// mScroller.startScroll(0, MIN_REFRSH_HEIGHT, 0,
		// -MIN_REFRSH_HEIGHT, SCROLL_DURATION);
		// // trigger computeScroll
		// invalidate();
		// mPullRefreshSuccess = false;
		// }
		// }
		// }, STOP_REFRESH_DURATION);
		// } else {
		mScroller.startScroll(0, height, 0, finalHeight - height,
				SCROLL_DURATION);
		// trigger computeScroll
		invalidate();
		// }
	}

	private void updateFooterHeight(float delta) {
		int height = mFooterView.getBottomMargin() + (int) delta;
		if (mEnablePullLoad && !mPullLoading && mHasMoreData) {
			if (height > PULL_LOAD_MORE_DELTA) { // height enough to invoke load
													// more.
				mFooterView.setState(XListViewFooter.STATE_READY);
			} else {
				mFooterView.setState(XListViewFooter.STATE_NORMAL);
			}
		}
		mFooterView.setBottomMargin(height);

		// setSelection(mTotalItemCount - 1); // scroll to bottom
	}

	private void resetFooterHeight() {
		int bottomMargin = mFooterView.getBottomMargin();
		if (bottomMargin > 0
				|| mFooterView.getState() == mFooterView.STATE_READY) {
			mScrollBack = SCROLLBACK_FOOTER;
			mScroller.startScroll(0, bottomMargin, 0, -bottomMargin,
					SCROLL_DURATION);
			invalidate();
		}
	}

	private void startLoadMore() {
		mPullLoading = true;
		mFooterView.setState(XListViewFooter.STATE_LOADING);
		if (mListViewListener != null) {
			mListViewListener.onLoadMore();
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (mLastY == -1) {
			mLastY = ev.getRawY();
		}

		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mLastY = ev.getRawY();
			break;
		case MotionEvent.ACTION_MOVE:
			final float deltaY = ev.getRawY() - mLastY;
			mLastY = ev.getRawY();

			mHeaderView.getLocationOnScreen(firstItemLocation);
			getLocationOnScreen(listviewLocation);
			if (getFirstVisiblePosition() == 0
					&& firstItemLocation[1] == listviewLocation[1]
					&& (mHeaderView.getMoreVisiableHeight() > 0 || deltaY > 0)) {
				// the first item is showing, header has shown or pull down.
				updateHeaderHeight(deltaY / OFFSET_RADIO);
				invokeOnScrolling();
			} else if (getLastVisiblePosition() == mTotalItemCount - 1
					&& (mFooterView.getBottomMargin() > 0 || deltaY < 0)) {
				// last item, already pulled up or want to pull up.
				updateFooterHeight(-deltaY / OFFSET_RADIO);
			}
			break;
		default :
			mLastY = -1; // reset
			Log.i(TAG, "getLastVisiblePosition()=" + getLastVisiblePosition()
					+ ",mTotalItemCount=" + mTotalItemCount);
			if (getFirstVisiblePosition() == 0) {
				// invoke refresh
				if (mEnablePullRefresh
						&& mHeaderView.getMoreVisiableHeight() > MIN_REFRSH_HEIGHT) {
					mPullRefreshing = true;
					mHeaderView.setState(Parallaxheader.STATE_REFRESHING);
					if (mListViewListener != null) {
						mListViewListener.onRefresh();
					}
				}else{
					mHeaderView.resetProgress();
				}
				resetHeaderHeight(false);
			}
			if (getLastVisiblePosition() == mTotalItemCount - 1) {
				// invoke load more.
				if (mEnablePullLoad
						&& mFooterView.getBottomMargin() > PULL_LOAD_MORE_DELTA
						&& !mPullLoading && mHasMoreData) {
					startLoadMore();
				}
				resetFooterHeight();
			}
			break;
		}
		return super.onTouchEvent(ev);
	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			if (mScrollBack == SCROLLBACK_HEADER) {
				mHeaderView.setVisiableHeight(mScroller.getCurrY());
			} else {
				mFooterView.setBottomMargin(mScroller.getCurrY());
			}
			postInvalidate();
			invokeOnScrolling();
		}
		super.computeScroll();
	}

	@Override
	public void setOnScrollListener(OnScrollListener l) {
		mScrollListener = l;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (mScrollListener != null) {
			mScrollListener.onScrollStateChanged(view, scrollState);
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// send to user's listener
		mTotalItemCount = totalItemCount;
		if (mScrollListener != null) {
			mScrollListener.onScroll(view, firstVisibleItem, visibleItemCount,
					totalItemCount);
		}
	}

	public void setXListViewListener(IXListViewListener l) {
		mListViewListener = l;
	}

	public void setRefreshing(boolean isRefreshCalback) {
		mHeaderView.setVisiableHeight(mHeaderViewHeight);
		mPullRefreshing = true;
		mHeaderView.setState(Parallaxheader.STATE_REFRESHING);
		if (mListViewListener != null && isRefreshCalback) {
			mListViewListener.onRefresh();
		}
	}

	public void setNoMoreData() {
		// TODO Auto-generated method stub
		mFooterView.setState(mFooterView.STATE_NOMORE);
		mHasMoreData = false;
	}

	public void setHasMoreData() {
		mHasMoreData = true;
		mFooterView.setState(mFooterView.STATE_NORMAL);
	}

	/**
	 * you can listen ListView.OnScrollListener or this one. it will invoke
	 * onXScrolling when header/footer scroll back.
	 */
	public interface OnXScrollListener extends OnScrollListener {
		public void onXScrolling(View view);
	}
}
