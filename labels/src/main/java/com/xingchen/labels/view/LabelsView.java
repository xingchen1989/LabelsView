package com.xingchen.labels.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.xingchen.labels.R;
import com.xingchen.labels.interfaces.LabelTextProvider;
import com.xingchen.labels.interfaces.OnLabelClickListener;
import com.xingchen.labels.interfaces.OnLabelSelectChangeListener;

import java.util.ArrayList;
import java.util.List;

public class LabelsView extends ViewGroup implements View.OnClickListener {
    private static final int KEY_DATA = R.id.label_key_data;//用于保存label数据的key
    private static final int KEY_POSITION = R.id.label_key_position;//用于保存label位置的key
    private static final int SELECT_NONE = 1;//不可选中，也不响应选中事件回调（默认）。
    private static final int SELECT_SINGLE = 2;//单选,可以反选。
    private static final int SELECT_SINGLE_IRREVOCABLY = 3;//单选,不可以反选。这种模式下，至少有一个是选中的，默认是第一个。
    private static final int SELECT_MULTI = 4;//多选,可以反选。

    private int mSelectType;//选中模式
    private int mMaxLines;//最大行数
    private int mLineMargin;//行与行的距离
    private int mWordMargin;//标签和标签的距离
    private int mLabelGravity;//标签的重力
    private int mLabelWidth;//标签宽度
    private int mLabelHeight;//标签高度
    private int mLabelMinWidth;//标签最小宽度
    private int mLabelMinHeight;//标签最小高度
    private int mTextColor;//文字颜色
    private int mTextPaddingLeft;//文字左内间距
    private int mTextPaddingTop;//文字上内间距
    private int mTextPaddingRight;//文字右内间距
    private int mTextPaddingBottom;//文字下内间距
    private float mTextSize;//文字大小
    private boolean isTextBold;//是否加粗
    private Drawable mLabelBg;//标签背景
    private OnLabelClickListener mLabelClickListener;
    private OnLabelSelectChangeListener mLabelSelectChangeListener;
    private final ArrayList<View> mSelectLabels = new ArrayList<>();//保存选中的label

    public LabelsView(Context context) {
        this(context, null);
    }

    public LabelsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context, attrs);
    }

    /**
     * 初始化自定义属性
     *
     * @param context
     * @param attrs
     */
    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.LabelsView);
        mSelectType = mTypedArray.getInt(R.styleable.LabelsView_selectType, 1);
        mMaxLines = mTypedArray.getInteger(R.styleable.LabelsView_maxLines, -1);
        mWordMargin = mTypedArray.getDimensionPixelOffset(R.styleable.LabelsView_wordMargin, dp2px(5));
        mLineMargin = mTypedArray.getDimensionPixelOffset(R.styleable.LabelsView_lineMargin, dp2px(5));
        mTextSize = mTypedArray.getDimension(R.styleable.LabelsView_labelTextSize, sp2px(14));
        mTextColor = mTypedArray.getColor(R.styleable.LabelsView_labelTextColor, Color.BLACK);
        mLabelGravity = mTypedArray.getInt(R.styleable.LabelsView_labelGravity, Gravity.CENTER);
        mLabelWidth = mTypedArray.getLayoutDimension(R.styleable.LabelsView_labelTextWidth, LayoutParams.WRAP_CONTENT);
        mLabelHeight = mTypedArray.getLayoutDimension(R.styleable.LabelsView_labelTextHeight, LayoutParams.WRAP_CONTENT);
        mLabelMinWidth = mTypedArray.getLayoutDimension(R.styleable.LabelsView_labelTextMinWidth, LayoutParams.WRAP_CONTENT);
        mLabelMinHeight = mTypedArray.getLayoutDimension(R.styleable.LabelsView_labelTextMinHeight, LayoutParams.WRAP_CONTENT);
        isTextBold = mTypedArray.getBoolean(R.styleable.LabelsView_isTextBold, false);
        if (mTypedArray.hasValue(R.styleable.LabelsView_labelBackground)) {
            mLabelBg = mTypedArray.getDrawable(R.styleable.LabelsView_labelBackground);
        } else {
            mLabelBg = ContextCompat.getDrawable(context, R.drawable.default_label_bg);
        }
        if (mTypedArray.hasValue(R.styleable.LabelsView_labelTextPadding)) {
            mTextPaddingLeft = mTextPaddingTop = mTextPaddingRight = mTextPaddingBottom =
                    mTypedArray.getDimensionPixelOffset(R.styleable.LabelsView_labelTextPadding, 0);
        } else {
            mTextPaddingLeft = mTypedArray.getDimensionPixelOffset(R.styleable.LabelsView_labelTextPaddingLeft, dp2px(10));
            mTextPaddingTop = mTypedArray.getDimensionPixelOffset(R.styleable.LabelsView_labelTextPaddingTop, dp2px(5));
            mTextPaddingRight = mTypedArray.getDimensionPixelOffset(R.styleable.LabelsView_labelTextPaddingRight, dp2px(10));
            mTextPaddingBottom = mTypedArray.getDimensionPixelOffset(R.styleable.LabelsView_labelTextPaddingBottom, dp2px(5));
        }
        mTypedArray.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //控件最大可用宽度
        int maxWidth = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
        //记录行的宽度
        int lineWidth = 0;
        //记录最宽的行宽
        int maxLineWidth = 0;
        //记录内容的高度
        int contentHeight = 0;
        //记录一行中item高度最大的高度
        int maxItemHeight = 0;
        //标签之间的间距
        int wordMargin = 0;
        //记录有多少行
        int lineCount = 1;

        //循环测量item并计算控件的内容宽高
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            measureChild(child, widthMeasureSpec, heightMeasureSpec);

            //第一项不考虑间距
            wordMargin = i > 0 ? mWordMargin : 0;

            //不需要换行
            if (lineWidth + child.getMeasuredWidth() + wordMargin < maxWidth) {
                //记录行宽
                lineWidth += (child.getMeasuredWidth() + wordMargin);
                //记录最大行宽
                maxLineWidth = Math.max(maxLineWidth, lineWidth);
                //记录一行中item高度最大的高度
                maxItemHeight = Math.max(maxItemHeight, child.getMeasuredHeight());
            } else {
                if (++lineCount > mMaxLines && mMaxLines > 0) {
                    break;
                }
                //换行的情况记录加上上一行内容高度和间距
                contentHeight += (maxItemHeight + mLineMargin);
                //下一行开始的初始行宽，为下次记录做准备
                lineWidth = child.getMeasuredWidth();
                //下一行开始的初始行高，为下次记录做准备
                maxItemHeight = child.getMeasuredHeight();
            }
        }
        //统计最后的高度
        contentHeight += maxItemHeight;

        int wideSize = maxLineWidth + getPaddingLeft() + getPaddingRight();
        int heightSize = contentHeight + getPaddingTop() + getPaddingBottom();
        setMeasuredDimension(resolveSizeAndState(wideSize, widthMeasureSpec, 0),
                resolveSizeAndState(heightSize, heightMeasureSpec, 0));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int lineCount = 1;
        int maxItemHeight = 0;
        int xCoordinate = 0;
        int yCoordinate = getPaddingTop();

        int availableSpace = getWidth() - getPaddingLeft() - getPaddingRight();

        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (availableSpace > xCoordinate + view.getMeasuredWidth()) {
                maxItemHeight = Math.max(maxItemHeight, view.getMeasuredHeight());
            } else {
                if (++lineCount > mMaxLines && mMaxLines > 0) {
                    break;
                }
                xCoordinate = 0;
                yCoordinate += (mLineMargin + maxItemHeight);
                maxItemHeight = view.getMeasuredHeight();
            }
            view.layout(xCoordinate, yCoordinate, xCoordinate + view.getMeasuredWidth(), yCoordinate + view.getMeasuredHeight());
            xCoordinate += (mWordMargin + view.getMeasuredWidth());
        }
    }

    @Override
    public void onClick(View label) {
        if (label instanceof TextView) {
            if (mSelectType != SELECT_NONE) {
                if (mSelectType == SELECT_SINGLE) {
                    if (mSelectLabels.contains(label)) {
                        setLabelSelect(label, !label.isSelected());
                    } else {
                        clearAllSelect();
                        setLabelSelect(label, true);
                    }
                } else if (mSelectType == SELECT_SINGLE_IRREVOCABLY) {
                    if (!label.isSelected()) {
                        clearAllSelect();
                        setLabelSelect(label, true);
                    }
                } else if (mSelectType == SELECT_MULTI) {
                    setLabelSelect(label, !label.isSelected());
                }
            }
            if (mLabelClickListener != null) {
                mLabelClickListener.onLabelClick((TextView) label, label.getTag(KEY_DATA), (int) label.getTag(KEY_POSITION));
            }
        }
    }

    /**
     * 设置标签的点击监听
     *
     * @param labelClickListener
     */
    public void setOnLabelClickListener(OnLabelClickListener labelClickListener) {
        mLabelClickListener = labelClickListener;
        for (int i = 0; i < getChildCount(); i++) {
            TextView label = (TextView) getChildAt(i);
            label.setClickable(mLabelClickListener != null);
        }
    }

    /**
     * 设置标签的选择监听
     *
     * @param labelSelectChangeListener
     */
    public void setOnLabelSelectChangeListener(OnLabelSelectChangeListener labelSelectChangeListener) {
        mLabelSelectChangeListener = labelSelectChangeListener;
    }

    /**
     * 返回所有选中的标签的位置
     *
     * @return
     */
    public List<Integer> getSelectedLabelsIndex() {
        List<Integer> indexList = new ArrayList<>();
        for (View view : mSelectLabels) {
            indexList.add((Integer) view.getTag(KEY_POSITION));
        }
        return indexList;
    }

    /**
     * 返回所有选中的标签的文本
     *
     * @return
     */
    public List<String> getSelectedLabelsText() {
        List<String> indexList = new ArrayList<>();
        for (View view : mSelectLabels) {
            indexList.add(((TextView) view).getText().toString());
        }
        return indexList;
    }

    /**
     * 获取选中的label(返回的是所头选中的标签的数据)
     *
     * @param <T>
     * @return
     */
    public <T> List<T> getSelectedLabelData() {
        List<T> list = new ArrayList<>();
        for (View label : mSelectLabels) {
            Object data = label.getTag(KEY_DATA);
            if (data != null) {
                list.add((T) data);
            }
        }
        return list;
    }

    /**
     * 设置选中label
     *
     * @param positions
     */
    public void setSelects(int... positions) {
        if (mSelectType != SELECT_NONE) {
            ArrayList<View> selectLabels = new ArrayList<>();
            int count = getChildCount();
            int size = mSelectType == SELECT_SINGLE || mSelectType == SELECT_SINGLE_IRREVOCABLY ? 1 : Integer.MAX_VALUE;
            for (int position : positions) {
                if (position < count) {
                    View label = getChildAt(position);
                    if (!selectLabels.contains(label)) {
                        selectLabels.add(label);
                        setLabelSelect(label, true);
                    }
                    if (selectLabels.size() == size) {
                        break;
                    }
                }
            }
            for (int i = 0; i < count; i++) {
                View label = getChildAt(i);
                if (!selectLabels.contains(label)) {
                    setLabelSelect(label, false);
                }
            }
        }
    }

    /**
     * 设置选中label
     *
     * @param positionList
     */
    public void setSelects(List<Integer> positionList) {
        if (mSelectType != SELECT_NONE) {
            ArrayList<View> selectLabels = new ArrayList<>();
            int count = getChildCount();
            int size = mSelectType == SELECT_SINGLE || mSelectType == SELECT_SINGLE_IRREVOCABLY ? 1 : Integer.MAX_VALUE;
            for (int position : positionList) {
                if (position < count) {
                    View label = getChildAt(position);
                    if (!selectLabels.contains(label)) {
                        selectLabels.add(label);
                        setLabelSelect(label, true);
                    }
                    if (selectLabels.size() == size) {
                        break;
                    }
                }
            }
            for (int i = 0; i < count; i++) {
                View label = getChildAt(i);
                if (!selectLabels.contains(label)) {
                    setLabelSelect(label, false);
                }
            }
        }
    }

    /**
     * 设置标签列表
     *
     * @param labels
     */
    public void setLabels(List<String> labels) {
        setLabels(labels, new LabelTextProvider<String>() {
            @Override
            public CharSequence getLabelText(TextView label, int position, String data) {
                return data.trim();
            }
        });
    }

    /**
     * 设置标签列表
     *
     * @param labels
     * @param provider
     * @param <T>
     */
    public <T> void setLabels(List<T> labels, LabelTextProvider<T> provider) {
        removeAllViews();
        if (labels != null) {
            for (int i = 0; i < labels.size(); i++) {
                addLabel(labels.get(i), i, provider);
            }
        }
    }

    /**
     * 是否选中某个标签
     *
     * @param label
     * @param isSelect
     */
    private void setLabelSelect(View label, boolean isSelect) {
        if (label.isSelected() != isSelect) {
            label.setSelected(isSelect);
            if (isSelect) {
                mSelectLabels.add(label);
            } else {
                mSelectLabels.remove(label);
            }
            if (mLabelSelectChangeListener != null) {
                mLabelSelectChangeListener.onLabelSelectChange((TextView) label,
                        label.getTag(KEY_DATA), isSelect, (int) label.getTag(KEY_POSITION));
            }
        }
    }

    /**
     * 添加标签
     *
     * @param data
     * @param position
     * @param provider
     * @param <T>
     */
    private <T> void addLabel(T data, int position, LabelTextProvider<T> provider) {
        TextView label = new TextView(getContext());
        label.setTag(KEY_DATA, data);
        label.setTag(KEY_POSITION, position);
        label.setMinWidth(mLabelMinWidth);
        label.setMinHeight(mLabelMinHeight);
        label.setTextColor(mTextColor);
        label.setGravity(mLabelGravity);
        label.getPaint().setFakeBoldText(isTextBold);
        label.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
        label.setPadding(mTextPaddingLeft, mTextPaddingTop, mTextPaddingRight, mTextPaddingBottom);
        label.setText(provider.getLabelText(label, position, data));
        //设置给label的背景(Drawable)是一个Drawable对象的拷贝，
        // 因为如果所有的标签都共用一个Drawable对象，会引起背景错乱。
        label.setBackground(mLabelBg.getConstantState().newDrawable());
        label.setOnClickListener(this);
        addView(label, mLabelWidth, mLabelHeight);
    }

    /**
     * 清除所有已选项
     */
    private void clearAllSelect() {
        for (View item : mSelectLabels) {
            item.setSelected(false);
        }
        mSelectLabels.clear();
    }


    /**
     * sp转px
     */
    private int sp2px(float spVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                spVal, getResources().getDisplayMetrics());
    }

    /**
     * dp转px
     */
    private int dp2px(float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, getResources().getDisplayMetrics());
    }
}
