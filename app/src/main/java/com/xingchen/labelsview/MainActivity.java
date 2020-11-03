package com.xingchen.labelsview;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.xingchen.labels.interfaces.OnLabelClickListener;
import com.xingchen.labels.view.LabelsView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private LabelsView labelsView;

    private List<String> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        labelsView = findViewById(R.id.labels);
        labelsView.setLabelClickListener(new OnLabelClickListener() {
            @Override
            public void onLabelClick(TextView label, int position) {
                Toast.makeText(MainActivity.this, position + "", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void add(View view) {
        for (int i = 0; i < 9; i++) {
            list.add("测试" + i);
        }
        labelsView.setLabels(list);
    }
}
