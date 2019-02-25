package com.example.recyclerviewex.base;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class HeaderWrapperAdapter extends RecyclerView.Adapter<BaseRecyclerViewHolder> {
    private List<View> mHeaders;
    private List<View> mFooters;
    private BaseRecyclerAdapter<BaseRecyclerViewHolder> mAdapter;
    private static final int HEADER_VIEW_TYPE = 0x8888;
    private static final int FOOTER_VIEW_TYPE = 0x9999;

    HeaderWrapperAdapter(List<View> headers, List<View> footers, BaseRecyclerAdapter adapter) {
        this.mHeaders = headers;
        this.mFooters = footers;
        this.mAdapter = adapter;
    }

    @NonNull
    @Override
    public BaseRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        int realType = getType(viewType), position = getPosition(viewType);
        if (realType == HEADER_VIEW_TYPE) {
            return new HeaderViewHolder(mHeaders.get(position));
        } else if (realType == FOOTER_VIEW_TYPE) {
            return new HeaderViewHolder(mFooters.get(position - mAdapter.getItemCount() - mHeaders.size()));
        } else {
            return mAdapter.onCreateViewHolder(viewGroup, realType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull BaseRecyclerViewHolder viewHolder, int position) {
        if (position >= mHeaders.size() && position < mHeaders.size() + mAdapter.getItemCount()) {
            mAdapter.onBindViewHolder(viewHolder, position - mHeaders.size());
        }
    }

    @Override
    public void onBindViewHolder(@NonNull BaseRecyclerViewHolder viewHolder, int position, @NonNull List<Object> payloads) {
        if (position >= mHeaders.size() && position < mHeaders.size() + mAdapter.getItemCount()) {
            mAdapter.onBindViewHolder(viewHolder, position - mHeaders.size());
        }
    }

    @Override
    public int getItemCount() {
        return mHeaders.size() + mAdapter.getItemCount() + mFooters.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position < mHeaders.size()) {
            return makeTypePos(HEADER_VIEW_TYPE, position);
        } else if (position < mHeaders.size() + mAdapter.getItemCount()) {
            return makeTypePos(mAdapter.getItemViewType(position - mHeaders.size()), position);
        } else {
            return makeTypePos(FOOTER_VIEW_TYPE, position);
        }
    }

    private int makeTypePos(int type, int pos) {
        return (type << 16) + pos;
    }

    private int getType(int typePos) {
        return typePos >>> 16;
    }

    private int getPosition(int typePos) {
        return typePos & 0xffff;
    }

    private static final class HeaderViewHolder extends BaseRecyclerViewHolder {
        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
