package com.example.recyclerviewex.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;

import com.example.recyclerviewex.R;
import com.example.recyclerviewex.base.BaseRecyclerView;
import com.example.recyclerviewex.utils.Utils;

public class PullRefreshView extends FrameLayout {
    private static final String TAG = "PullRefreshView";

    private View mHeaderView;
    private View mContentView;
    private static final int MAX_PULL_LENGTH = Utils.dp2px(200);
    private static final long MAX_GOBACK_DURATION = 200;

    private static final int REFRESH_IDLE = 0; // 静止状态
    private static final int REFRESH_PULL = 1; // 手动下拉
    private static final int REFRESH_RELEASED = 2; // 下拉松手
    private static final int REFRESH_REFRESHING = 3; // 正在刷新
    private int mState = REFRESH_IDLE;
    private int mHeaderHeight;
    private int mTouchSlop;

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
//        if (getChildCount() != 1) {
//            throw new IllegalArgumentException("子控件必须只有一个");
//        }
//        mHeaderView = getChildAt(0);
        mContentView = getChildAt(0);
        FrameLayout.LayoutParams contentParams = (LayoutParams) mContentView.getLayoutParams();
        contentParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        contentParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        removeView(mContentView);

        if (mContentView instanceof ListView) {
            mContentView = new InternalListView(getContext(), (ListView) mContentView);
        } else if (mContentView instanceof ScrollView) {
            mContentView = new InternalScrollView(getContext(), (ScrollView) mContentView);
        } else if (mContentView instanceof RecyclerView) {
            mContentView = new InternalRecyclerView(getContext(), (RecyclerView) mContentView);
            InternalRecyclerView recyclerView = (InternalRecyclerView) mContentView;
            mHeaderView = recyclerView.getHeaderView();
            mHeaderView.measure(0, 0);
            mHeaderHeight = Utils.dp2px(100);
//            removeView(mHeaderView);
            setHeaderPaddingTop(-mHeaderHeight);
        }

        mContentView.setLayoutParams(contentParams);
        addView(mContentView);
    }

    private void setHeaderPaddingTop(int top) {
        mHeaderView.setPadding(0, top, 0, 0);
        mHeaderView.requestLayout();
    }

    private void goBackAndShowRefresh() {
        if (!isRefreshing()) {
            return;
        }

        int paddingTop = getHeaderPaddingTop();
        if (paddingTop > 0) {
            ValueAnimator valueAnimator = ValueAnimator.ofInt(paddingTop, 0);
            valueAnimator.setDuration(MAX_GOBACK_DURATION);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (int) animation.getAnimatedValue();
                    setHeaderPaddingTop(value);
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
        ValueAnimator valueAnimator = ValueAnimator.ofInt(getHeaderPaddingTop(), -mHeaderHeight);
        valueAnimator.setDuration(MAX_GOBACK_DURATION);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                setHeaderPaddingTop(value);
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
        if (getHeaderPaddingTop() >= MAX_PULL_LENGTH) {
            return;
        }
        Log.e(TAG, "diff = " + diff);
        setHeaderPaddingTop(getHeaderPaddingTop() + diff);
        mHeaderView.requestLayout();
    }

    private boolean shouldRefresh() {
        return getHeaderPaddingTop() >= 0;
    }

    private int getHeaderPaddingTop() {
        return mHeaderView.getPaddingTop();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        return mContentView.dispatchTouchEvent(event);
    }

    private class InternalListView extends ListView {

        private int mDownY;
        private int mLastY;
        private boolean mIsDragging = false;

        public InternalListView(Context context, ListView origin) {
            super(context);
            ListView.LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            FrameLayout frameLayout = new FrameLayout(getContext());
            frameLayout.addView(mHeaderView);
            frameLayout.setLayoutParams(layoutParams);
            addHeaderView(frameLayout);
            setId(origin.getId());
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent event) {
            int action = event.getActionMasked();
            int y = (int) event.getRawY();
            Log.e(TAG, "y = " + y);
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    mDownY = y;
                    break;
                case MotionEvent.ACTION_MOVE:
                    int motionY = y - mDownY;
                    int diff = y - mLastY;
                    if (!mIsDragging && Math.abs(motionY) > mTouchSlop && ((motionY > 0 && isFirstAtTop()) ||
                            isFirstAtTop() && motionY < 0 && getHeaderPaddingTop() > -mHeaderHeight)) {
                        mIsDragging = true;
                    }

                    if (mIsDragging) {
                        mState = REFRESH_PULL;
                        offsetHeader(diff);
                        Log.e(TAG, "top = " + getHeaderPaddingTop());
                        if (getHeaderPaddingTop() <= -mHeaderHeight) {
                            mState = REFRESH_IDLE;
                            mIsDragging = false;
                            setHeaderPaddingTop(-mHeaderHeight);
                        }
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
            if (getChildCount() < 2) {
                return false;
            }

            View view = getChildAt(1);
            Log.e(TAG, "first = " + getFirstVisiblePosition() + ", top = " + view.getTop());
            return view.getTop() < mTouchSlop && getFirstVisiblePosition() <= 1;
        }
    }

    private class InternalScrollView extends ScrollView {
        private int mDownY;
        private int mLastY;
        private boolean mIsDragging = false;

        public InternalScrollView(Context context, ScrollView origin) {
            super(context);
            setId(origin.getId());
            LinearLayout linearLayout = new LinearLayout(getContext());
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            View content = origin.getChildAt(0);
            origin.removeAllViews();
            linearLayout.addView(mHeaderView);
            linearLayout.addView(content);
            addView(linearLayout);
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent event) {
            int action = event.getActionMasked();
            int y = (int) event.getRawY();
            Log.e(TAG, "y = " + y);
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    mDownY = y;
                    break;
                case MotionEvent.ACTION_MOVE:
                    int motionY = y - mDownY;
                    int diff = y - mLastY;
                    if (!mIsDragging && Math.abs(motionY) > mTouchSlop && ((motionY > 0 && isFirstAtTop()) ||
                            isFirstAtTop() && motionY < 0 && getHeaderPaddingTop() > -mHeaderHeight)) {
                        mIsDragging = true;
                    }

                    if (mIsDragging) {
                        mState = REFRESH_PULL;
                        offsetHeader(diff);
                        Log.e(TAG, "top = " + getHeaderPaddingTop());
                        if (getHeaderPaddingTop() <= -mHeaderHeight) {
                            mState = REFRESH_IDLE;
                            mIsDragging = false;
                            setHeaderPaddingTop(-mHeaderHeight);
                        }
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
            Log.e(TAG, "scrollY = " + mContentView.getScrollY());
            return mContentView.getScrollY() <= 0;
        }
    }

    private class InternalRecyclerView extends BaseRecyclerView {
        private int mDownY;
        private int mLastY;
        private boolean mIsDragging = false;
        private View mHeaderView;

        public InternalRecyclerView(Context context, RecyclerView origin) {
            super(context);
            setId(origin.getId());
            setLayoutManager(new LinearLayoutManager(context));
            mHeaderView = LayoutInflater.from(context).inflate(R.layout.layout_header, this, false);
            addHeaderView(mHeaderView);
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent event) {
            int action = event.getActionMasked();
            int y = (int) event.getRawY();
            Log.e(TAG, "y = " + y);
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    mDownY = y;
                    break;
                case MotionEvent.ACTION_MOVE:
                    int motionY = y - mDownY;
                    int diff = y - mLastY;
                    if (!mIsDragging && Math.abs(motionY) > mTouchSlop && ((motionY > 0 && isFirstAtTop()) ||
                            isFirstAtTop() && motionY < 0 && getHeaderPaddingTop() > -mHeaderHeight)) {
                        mIsDragging = true;
                    }

                    if (mIsDragging) {
                        mState = REFRESH_PULL;
                        offsetHeader(diff);
                        Log.e(TAG, "top = " + getHeaderPaddingTop());
                        if (getHeaderPaddingTop() <= -mHeaderHeight) {
                            mState = REFRESH_IDLE;
                            mIsDragging = false;
                            setHeaderPaddingTop(-mHeaderHeight);
                        }
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
            return !canScrollVertically(-1);
        }

        public View getHeaderView() {
            return mHeaderView;
        }
    }
}
