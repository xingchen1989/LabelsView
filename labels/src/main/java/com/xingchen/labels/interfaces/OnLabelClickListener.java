package com.xingchen.labels.interfaces;

import android.widget.TextView;

public interface OnLabelClickListener {
    /**
     * @param label    标签
     * @param data     标签对应的数据
     * @param position 标签的位置
     */
    void onLabelClick(TextView label, Object data, int position);
}
