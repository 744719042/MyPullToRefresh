package com.example.recyclerviewex.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.example.recyclerviewex.utils.Utils;

public class PullRefreshView extends LinearLayout {
    private static final String TAG = "PullRefreshView";

    private View mHeaderView;
    private View mContentView;
    private boolean mFirstInit = true;
    private static final int MAX_PULL_LENGTH = Utils.dp2px(200);
    private static final long MAX_GOBACK_DURATION = 200;

    private static final int REFRESH_IDLE = 0; // 静止状态
    private static final int REFRESH_PULL = 1; // 手动下拉
    private static final int REFRESH_RELEASED = 2; // 下拉松手
    private static final int REFRESH_REFRESHING = 3; // 正在刷新
    private int mState = REFRESH_IDLE;
    private int mHeaderHeight;

    public interface RefreshListener {
        void onRefresh();
    }

    private RefreshListener mRefreshListener;

    public PullRefreshView(Context context) {
        this(context, null);
    }

    public PullRefreshView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PullRefreshView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        setOrientation(VERTICAL);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initViews();
    }

    public void setRefreshListener(RefreshListener listener) {
        this.mRefreshListener = listener;
    }

    private void notifyRefreshStart() {
        if (mRefreshListener != null) {
            mRefreshListener.onRefresh();
        }
    }

    public void notifyRefreshComplete() {
        if (isRefreshing()) {
            headerGoBack();
        }
    }

    private void initViews() {
        if (getChildCount() != 2) {
            throw new IllegalArgumentException("");
        }
        mHeaderView = getChildAt(0);
        mContentView = getChildAt(1);

        LinearLayout.LayoutParams headerParams = (LayoutParams) mHeaderView.getLayoutParams();
        mHeaderView.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        mHeaderHeight = mHeaderView.getMeasuredHeight();
        headerParams.topMargin = -mHeaderHeight;
        headerParams.leftMargin = headerParams.rightMargin = headerParams.bottomMargin = 0;

        LinearLayout.LayoutParams contentParams = (LayoutParams) mContentView.getLayoutParams();
        contentParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        contentParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mFirstInit && (mHeaderView == null || mContentView == null)) {
            initViews();
            mFirstInit = false;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    private int mDownY;
    private int mLastY;
    private int mTouchSlop;
    private boolean mIsDragging = false;

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        int y = (int) event.getRawY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mDownY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                int motionY = y - mDownY;
                int diff = y - mLastY;
                if (!mIsDragging && Math.abs(motionY) > mTouchSlop && ((motionY > 0 && isFirstAtTop()) ||
                        isFirstAtTop() && motionY < 0 && getHeaderMarginTop() > -mHeaderHeight)) {
                    mIsDragging = true;
                    event.setAction(MotionEvent.ACTION_CANCEL);
                    super.dispatchTouchEvent(event);
                }

                if (mIsDragging) {
                    mState = REFRESH_PULL;
                    offsetHeader(diff);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsDragging = false;
                if (isPulling()) {
                    mState = REFRESH_RELEASED;
                    if (shouldRefresh()) {
                        mState = REFRESH_REFRESHING;
                        goBackAndShowRefresh();
                    } else {
                        headerGoBack();
                    }
                }
                break;
        }
        mLastY = y;
        return mIsDragging || super.dispatchTouchEvent(event);
    }

    public boolean isFirstAtTop() {
        if (mContentView instanceof ListView) {
            ListView listView = (ListView) mContentView;
            if (listView.getChildCount() > 0) {
                View child = listView.getChildAt(0);
                if (listView.getFirstVisiblePosition() == 0 && child.getTop() == 0) {
                    return true;
                }
            }
        }

        return false;
    }


    private void goBackAndShowRefresh() {
        if (!isRefreshing()) {
            return;
        }

        int marginTop = getHeaderMarginTop();
        if (marginTop > 0) {
            ValueAnimator valueAnimator = ValueAnimator.ofInt(marginTop, 0);
            valueAnimator.setDuration(MAX_GOBACK_DURATION);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    LinearLayout.LayoutParams headerParams = (LayoutParams) mHeaderView.getLayoutParams();
                    headerParams.topMargin = (int) animation.getAnimatedValue();
                    mHeaderView.requestLayout();
                }
            });
            valueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    notifyRefreshStart();
                }
            });
            valueAnimator.start();
        } else {
            notifyRefreshStart();
        }
    }

    private void headerGoBack() {
        if (!isReleased() && !isRefreshing()) {
            return;
        }
        ValueAnimator valueAnimator = ValueAnimator.ofInt(getHeaderMarginTop(), -mHeaderHeight);
        valueAnimator.setDuration(MAX_GOBACK_DURATION);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                LinearLayout.LayoutParams headerParams = (LayoutParams) mHeaderView.getLayoutParams();
                headerParams.topMargin = (int) animation.getAnimatedValue();
                mHeaderView.requestLayout();
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mState = REFRESH_IDLE;
            }
        });
        valueAnimator.start();
    }

    private boolean isIdle() {
        return mState == REFRESH_IDLE;
    }

    private boolean isPulling() {
        return mState == REFRESH_PULL;
    }

    private boolean isRefreshing() {
        return mState == REFRESH_REFRESHING;
    }

    private boolean isReleased() {
        return mState == REFRESH_RELEASED;
    }

    private void offsetHeader(int diff) {
        LinearLayout.LayoutParams headerParams = (LayoutParams) mHeaderView.getLayoutParams();
        if (headerParams.topMargin >= MAX_PULL_LENGTH || headerParams.topMargin < -mHeaderHeight) {
            return;
        }
        Log.e(TAG, "diff = " + diff);
        headerParams.topMargin += diff;
        mHeaderView.requestLayout();
    }

    private boolean shouldRefresh() {
        return getHeaderMarginTop() >= 0;
    }

    private int getHeaderMarginTop() {
        LinearLayout.LayoutParams headerParams = (LayoutParams) mHeaderView.getLayoutParams();
        return headerParams.topMargin;
    }
}
