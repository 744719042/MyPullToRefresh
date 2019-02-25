package com.example.recyclerviewex.recycler;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.example.recyclerviewex.R;
import com.example.recyclerviewex.base.BaseRecyclerAdapter;
import com.example.recyclerviewex.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/2/1.
 */

public class UserRecyclerAdapter extends BaseRecyclerAdapter<UserRecyclerViewHolder> {
    public static List<User> users = new ArrayList<User>() {
        {
            add(new User(R.drawable.beach, "刘备", "唯贤唯德，能服于人"));
            add(new User(R.drawable.bamboo, "诸葛亮", "淡泊以明志，宁静以致远"));
            add(new User(R.drawable.road, "关羽", "安能与老兵同列"));
            add(new User(R.drawable.flower, "赵云", "子龙一身是胆"));
            add(new User(R.drawable.lake, "曹操", "宁教我负天下人，不教天下人负我"));
            add(new User(R.drawable.rain, "司马懿", "老而不死是为贼"));
            add(new User(R.drawable.sea, "司马昭", "司马昭之心路人皆知"));
            add(new User(R.drawable.moon, "孙权", "生子当如孙仲谋"));
            add(new User(R.drawable.peach, "周瑜", "既生瑜何生亮"));
            add(new User(R.drawable.pool, "吕蒙", "士别三日当刮目相待"));
        }
    };

    public static SparseArray<String> groups = new SparseArray<>();

    static {
        groups.put(0, "蜀国");
        groups.put(1, "魏国");
        groups.put(2, "吴国");
    }

    private Context context;
    private LayoutInflater inflater;

    public UserRecyclerAdapter(Context context) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public UserRecyclerViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.user_item, parent, false);
        return new UserRecyclerViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(UserRecyclerViewHolder holder, int position) {
        holder.bindView(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }


    public List<?> getData() {
        return users;
    }
}
