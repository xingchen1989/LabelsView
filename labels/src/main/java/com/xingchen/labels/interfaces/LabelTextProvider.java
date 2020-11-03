package com.xingchen.labels.interfaces;

import android.widget.TextView;

/**
 * 给标签提供最终需要显示的数据。因为LabelsView的列表可以设置任何类型的数据，而LabelsView里的每个item的是一
 * 个TextView，只能显示CharSequence的数据，所以LabelTextProvider需要根据每个item的数据返回item最终要显示
 * 的CharSequence。
 *
 * @param <T>
 */
public interface LabelTextProvider<T> {
    /**
     * 根据data和position返回label需要需要显示的数据。
     *
     * @param label
     * @param position
     * @param data
     * @return
     */
    CharSequence getLabelText(TextView label, int position, T data);
}
