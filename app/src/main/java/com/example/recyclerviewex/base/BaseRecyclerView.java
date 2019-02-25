package com.example.recyclerviewex.base;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class BaseRecyclerView extends RecyclerView {
    private List<View> mHeaders = new ArrayList<>();
    private List<View> mFooters = new ArrayList<>();
    private HeaderWrapperAdapter mWrapperAdapter;
    private Adapter mAdapter;

    public BaseRecyclerView(@NonNull Context context) {
        super(context);
    }

    public BaseRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setAdapter(@Nullable Adapter adapter) {
        if (adapter instanceof BaseRecyclerAdapter) {
            mAdapter = adapter;
            mWrapperAdapter = new HeaderWrapperAdapter(mHeaders, mFooters, (BaseRecyclerAdapter) adapter);
            super.setAdapter(mWrapperAdapter);
        } else {
            super.setAdapter(adapter);
        }
    }

    public void addHeaderView(View headerView) {
        mHeaders.add(headerView);
        if (mWrapperAdapter != null) {
            mWrapperAdapter.notifyItemInserted(mHeaders.size() - 1);
        }
    }

    public void removeHeaderView(View headerView) {
        int index = mHeaders.indexOf(headerView);
        if (index < 0) {
            return;
        }

        mHeaders.remove(headerView);
        if (mWrapperAdapter != null) {
            mWrapperAdapter.notifyItemRemoved(index);
        }
    }

    public void addFooterView(View footerView) {
        mFooters.add(footerView);
        if (mWrapperAdapter != null) {
            mWrapperAdapter.notifyItemInserted(mHeaders.size() + mAdapter.getItemCount() + mFooters.size() - 1);
        }
    }

    public void removeFooterView(View footerView) {
        int index = mFooters.indexOf(footerView);
        if (index < 0) {
            return;
        }
        mFooters.remove(footerView);
        if (mWrapperAdapter != null) {
            mWrapperAdapter.notifyItemRemoved(mHeaders.size() + mAdapter.getItemCount() + index);
        }
    }
}
