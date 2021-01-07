package com.xingchen.labels.interfaces;

import android.widget.TextView;

public interface OnLabelSelectChangeListener {
    /**
     * @param label    标签
     * @param data     标签对应的数据
     * @param isSelect 是否选中
     * @param position 标签的位置
     */
    void onLabelSelectChange(TextView label, Object data, boolean isSelect, int position);
}
