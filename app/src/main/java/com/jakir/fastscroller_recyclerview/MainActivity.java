package com.jakir.fastscroller_recyclerview;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.jakir.fastscroller.FastScroller;

public class MainActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeColors(Color.GREEN);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(false);
            loadItems();
        });

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setPadding(recyclerView.getPaddingLeft(), recyclerView.getPaddingTop(), recyclerView.getPaddingRight(), FastScroller.getNavigationBarHeight(this));

        loadItems();


//        FastScroller.attach(recyclerView);
//        FastScroller.attach(recyclerView, null, null, null, null, null);

//        FastScroller.attach(recyclerView, swipeRefreshLayout);
//        FastScroller.attach(recyclerView, null, null, null, null, null, swipeRefreshLayout);

        FastScroller.attach(recyclerView, null, null, null, null, Color.TRANSPARENT, swipeRefreshLayout);
    }

    private void loadItems() {
        View v = LayoutInflater.from(this).inflate(R.layout.edittext, null);
        EditText editText = v.findViewById(R.id.e);
        new MaterialAlertDialogBuilder(this).setTitle("Select item count").setView(v).setPositiveButton("OK", (dialog, which) -> {
            int i = Integer.parseInt(editText.getText().toString().isEmpty() ? "10" : editText.getText().toString());
            recyclerView.setAdapter(new SimpleAdapter(i));
        }).create().show()
        ;
    }
}
