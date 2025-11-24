package com.jakir.fastscroller_recyclerview;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.jakir.fastscroller.FastScroller;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    View v = LayoutInflater.from(this).inflate(R.layout.edittext,null);
        EditText editText=v.findViewById(R.id.e);
        new MaterialAlertDialogBuilder(this).setTitle("Select item count").setView(v).setPositiveButton("OK", (dialog, which) -> {
           int i = Integer.parseInt(editText.getText().toString().isEmpty()?"10":editText.getText().toString());
            recyclerView.setAdapter(new SimpleAdapter(i));
        }).create().show()
        ;
        // attach FastScroller

        FastScroller.attach(recyclerView);
//        FastScroller.attach(recyclerView, null, null, null, null, 0x00000000);
//        FastScroller.attach(recyclerView, null, null, null, null, Color.TRANSPARENT);
//        FastScroller.attach(recyclerView, null, null, Color.rgb(0, 0, 250), null, Color.parseColor("#00000000"));
//        FastScroller.attach(recyclerView, null, null, Color.rgb(255, 0, 0), null, null);
//        FastScroller.attach(recyclerView, 10, null, null, null, 0x00000000);
//        FastScroller.attach(recyclerView, null, null, null, null, null);

    }
}
