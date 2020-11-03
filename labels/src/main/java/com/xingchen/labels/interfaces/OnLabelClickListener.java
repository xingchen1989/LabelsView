package com.xingchen.labels.interfaces;

import android.widget.TextView;

public interface OnLabelClickListener {
    /**
     * @param label    标签
     * @param position 标签位置
     */
    void onLabelClick(TextView label, int position);
}
