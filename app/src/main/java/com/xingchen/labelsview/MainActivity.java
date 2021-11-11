package com.xingchen.labelsview;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.xingchen.labels.view.LabelsView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private LabelsView labelsView;
    private LabelsView labelsView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        labelsView = findViewById(R.id.labels);
        labelsView2 = findViewById(R.id.lv_key_point);
//        labelsView.setLabels(getLabels());
        labelsView2.setLabels(getLabels2());
//        labelsView.setSelects(0, 2);
//        labelsView.setOnLabelClickListener(new OnLabelClickListener() {
//            @Override
//            public void onLabelClick(TextView label, Object data, int position) {
//                Toast.makeText(MainActivity.this, "点击了：" + label.getText(), Toast.LENGTH_SHORT).show();
//            }
//        });
//        labelsView.setOnLabelSelectChangeListener(new OnLabelSelectChangeListener() {
//            @Override
//            public void onLabelSelectChange(TextView label, Object data, boolean isSelect, int position) {
//                Toast.makeText(MainActivity.this, "选项：" + label.getText() + isSelect, Toast.LENGTH_SHORT).show();
//            }
//        });
    }

    public List<String> getLabels() {
        List<String> labels = new ArrayList<>();
        labels.add("糖病");
        labels.add("高血压");
        labels.add("高血糖");
        labels.add("甲状");
        labels.add("冠心病");
        labels.add("脑血栓");
        labels.add("脑血管");
        return labels;
    }

    public List<String> getLabels2() {
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            labels.add("a" + i);
        }
        return labels;
    }
}
