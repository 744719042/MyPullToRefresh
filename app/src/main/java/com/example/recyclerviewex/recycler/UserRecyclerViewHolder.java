package com.example.recyclerviewex.recycler;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.recyclerviewex.R;
import com.example.recyclerviewex.base.BaseRecyclerViewHolder;
import com.example.recyclerviewex.model.User;

/**
 * Created by Administrator on 2018/2/1.
 */

public class UserRecyclerViewHolder extends BaseRecyclerViewHolder {
    ImageView portrait;
    TextView name;
    TextView desc;

    public UserRecyclerViewHolder(View itemView) {
        super(itemView);
        name = (TextView) itemView.findViewById(R.id.name);
        desc = (TextView) itemView.findViewById(R.id.desc);
        portrait = (ImageView) itemView.findViewById(R.id.portrait);
    }


    public void bindView(final User user) {
//        itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Snackbar.make(itemView, user.getDesc(), Snackbar.LENGTH_LONG).setAction("确定", null).show();
//            }
//        });
        name.setText(user.getName());
        desc.setText(user.getDesc());
        portrait.setImageResource(user.getPortrait());
    }
}
