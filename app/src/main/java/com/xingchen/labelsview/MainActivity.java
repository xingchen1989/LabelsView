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
        List<String> labels = new ArrayList<>();
        labels.add("糖尿病");
        labels.add("高血压");
        labels.add("高血糖");
        labels.add("甲状腺机能障碍");
        labels.add("冠心病");
        labels.add("脑血栓");
        labels.add("脑血管病变");
        labelsView.setLabels(labels);
        labelsView.setLabelClickListener(new OnLabelClickListener() {
            @Override
            public void onLabelClick(TextView label, int position) {
                Toast.makeText(MainActivity.this, "点击了：" + label.getText(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
