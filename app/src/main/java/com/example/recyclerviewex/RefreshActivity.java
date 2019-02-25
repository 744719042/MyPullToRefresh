package com.example.recyclerviewex;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ScrollView;

import com.example.recyclerviewex.base.BaseRecyclerView;
import com.example.recyclerviewex.recycler.UserRecyclerAdapter;

public class RefreshActivity extends AppCompatActivity {
    private ListView listView;
    private ScrollView scrollView;
    private RecyclerView recyclerView;
    private UserRecyclerAdapter adapter;
    private View headerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refresh);
//        listView = findViewById(R.id.listview);
//        listView.setAdapter(new UserListAdapter(this));
//        scrollView = findViewById(R.id.scrollView);
        recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
//        headerView = LayoutInflater.from(this).inflate(R.layout.layout_header, recyclerView, false);
//        recyclerView.addHeaderView(headerView);
        recyclerView.setAdapter(adapter = new UserRecyclerAdapter(this));
    }
}
