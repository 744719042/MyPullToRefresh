package com.example.recyclerviewex;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.widget.ListView;
import android.widget.ScrollView;

import com.example.recyclerviewex.adapter.UserListAdapter;
import com.example.recyclerviewex.recycler.RecyclerAdapter;

public class RefreshActivity extends AppCompatActivity {
    private ListView listView;
    private ScrollView scrollView;
    private RecyclerView recyclerView;
    private RecyclerAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refresh);
//        listView = findViewById(R.id.listview);
//        listView.setAdapter(new UserListAdapter(this));
        scrollView = findViewById(R.id.scrollView);

//        recyclerView = findViewById(R.id.recyclerView);
//        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
//        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
//        recyclerView.setLayoutManager(layoutManager);
//        recyclerView.setAdapter(adapter = new RecyclerAdapter(this));
    }
}
