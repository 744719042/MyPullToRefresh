package com.example.recyclerviewex;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.ScrollView;

import com.example.recyclerviewex.adapter.UserListAdapter;

public class RefreshActivity extends AppCompatActivity {
    private ListView listView;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refresh);
//        listView = findViewById(R.id.listview);
//        listView.setAdapter(new UserListAdapter(this));
        scrollView = findViewById(R.id.scrollView);
    }
}
