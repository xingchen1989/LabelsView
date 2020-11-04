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

import com.xingchen.labels.R;
import com.xingchen.labels.interfaces.LabelTextProvider;
import com.xingchen.labels.interfaces.OnLabelClickListener;

import java.util.ArrayList;
import java.util.List;

public class LabelsView extends ViewGroup implements View.OnClickListener {
    private static final int KEY_DATA = R.id.label_key_data;//用于保存label数据的key
    private static final int KEY_POSITION = R.id.label_key_position;//用于保存label位置的key

    private int mMaxLines;//最大行数
    private int mLineMargin;//行与行的距离
    private int mWordMargin;//标签和标签的距离
    private int mLabelGravity;//标签的重力
    private int mTextColor;//文字颜色
    private int mTextPaddingLeft;//文字左内间距
    private int mTextPaddingTop;//文字上内间距
    private int mTextPaddingRight;//文字右内间距
    private int mTextPaddingBottom;//文字下内间距
    private float mTextSize;//文字大小
    private boolean isTextBold;//是否加粗
    private Drawable mLabelBg;//标签背景
    private OnLabelClickListener mLabelClickListener;

    public LabelsView(Context context) {
        this(context, null);
    }

    public LabelsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context, attrs);
        showEditPreview();
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
                if (++lineCount > mMaxLines) {
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
                if (++lineCount > mMaxLines) {
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
    public void onClick(View v) {
        if (v instanceof TextView) {
            TextView label = (TextView) v;
            mLabelClickListener.onLabelClick(label, (int) label.getTag(KEY_POSITION));
        }
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.LabelsView);
            mMaxLines = mTypedArray.getInteger(R.styleable.LabelsView_maxLines, -1);
            mWordMargin = mTypedArray.getDimensionPixelOffset(R.styleable.LabelsView_wordMargin, dp2px(5));
            mLineMargin = mTypedArray.getDimensionPixelOffset(R.styleable.LabelsView_lineMargin, dp2px(5));
            mTextSize = mTypedArray.getDimension(R.styleable.LabelsView_labelTextSize, sp2px(14));
            mTextColor = mTypedArray.getColor(R.styleable.LabelsView_labelTextColor, Color.BLACK);
            mLabelGravity = mTypedArray.getInt(R.styleable.LabelsView_labelGravity, Gravity.CENTER);
            isTextBold = mTypedArray.getBoolean(R.styleable.LabelsView_isTextBold, false);
            if (mTypedArray.hasValue(R.styleable.LabelsView_labelBackground)) {
                mLabelBg = mTypedArray.getDrawable(R.styleable.LabelsView_labelBackground);
            } else {
                mLabelBg = getResources().getDrawable(R.drawable.default_label_bg);
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
    }

    /**
     * 编辑预览
     */
    private void showEditPreview() {
        if (isInEditMode()) {
            ArrayList<String> label = new ArrayList<>();
            for (int i = 0; i < 9; i++) {
                label.add("测试" + i);
            }
            setLabels(label, new LabelTextProvider<String>() {
                @Override
                public CharSequence getLabelText(TextView label, int position, String data) {
                    return null;
                }
            });
        }
    }

    /**
     * 设置标签的点击监听
     *
     * @param labelClickListener
     */
    public void setLabelClickListener(OnLabelClickListener labelClickListener) {
        mLabelClickListener = labelClickListener;
        for (int i = 0; i < getChildCount(); i++) {
            TextView label = (TextView) getChildAt(i);
            label.setClickable(mLabelClickListener != null);
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
        addView(label);
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
