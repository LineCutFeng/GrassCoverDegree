package com.lcf.nir_calculate;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.lcf.nir_calculate.adapter.FileListAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView picList;
    private List<String> nameList = new ArrayList<>();
    private FileListAdapter fileListAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        picList = findViewById(R.id.list_pic);
        String[] list = new String[0];
        try {
            list = getAssets().list("");
        } catch (IOException e) {
            e.printStackTrace();
        }
        nameList.addAll(Arrays.asList(list));
        Iterator<String> iterator = nameList.iterator();
        while (iterator.hasNext()) {
            String next = iterator.next();
            if (next.isEmpty() || !next.contains("sample")) {
                iterator.remove();
            }
        }
        fileListAdapter = new FileListAdapter(this, nameList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        picList.setLayoutManager(layoutManager);
        picList.setAdapter(fileListAdapter);
    }
}
