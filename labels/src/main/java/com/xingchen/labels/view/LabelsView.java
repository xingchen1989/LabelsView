package com.xingchen.labels.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
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
    private int mSpaceType;//间距模式
    private int mMaxLines;//最大行数
    private int mLineMargin;//行间距
    private int mLabelMargin;//标签间距
    private int mLabelWidth;//标签宽度
    private int mLabelHeight;//标签高度
    private int mLabelMinWidth;//标签最小宽度
    private int mLabelMinHeight;//标签最小高度
    private int mTextPaddingLeft;//文字左内间距
    private int mTextPaddingTop;//文字上内间距
    private int mTextPaddingRight;//文字右内间距
    private int mTextPaddingBottom;//文字下内间距
    private int mTextGravity;//文字的重力
    private int mTextColor;//文字颜色
    private float mTextSize;//文字大小
    private boolean isTextBold;//是否加粗
    private boolean isForbidClick;//是否禁止点击
    private Drawable mLabelBg;//标签背景
    private OnLabelClickListener mLabelClickListener;
    private OnLabelSelectChangeListener mLabelSelectChangeListener;
    private final List<View> mSelectLabels = new ArrayList<>();//选中的标签

    public LabelsView(Context context) {
        this(context, null);
    }

    public LabelsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return isForbidClick || super.onInterceptTouchEvent(ev);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //控件最大可用宽度
        int maxWidth = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
        //记录有多少行
        int lineCount = 1;
        //记录换行位置
        int lineFeedPos = 0;
        //记录最宽的行宽
        int maxLineWidth = 0;
        //记录行高
        int maxLabelHeight = 0;
        //记录行的宽度
        int totalLabelWidth = 0;
        //记录内容的高度
        int layoutContentHeight = 0;
        //初始横坐标
        int coordinateX = getPaddingLeft();
        //初始纵坐标
        int coordinateY = getPaddingTop();
        //循环测量item并计算控件的内容宽高
        for (int i = 0; i < getChildCount(); i++) {
            //测量标签大小
            measureChild(getChildAt(i), widthMeasureSpec, heightMeasureSpec);
            //标签的宽度
            int labelWidth = getChildAt(i).getMeasuredWidth();
            //标签的高度
            int labelHeight = getChildAt(i).getMeasuredHeight();
            //记录标签总间隔
            int labelTotalMargin = (i - lineFeedPos) * mLabelMargin;
            //不需要换行
            if (totalLabelWidth + labelWidth + labelTotalMargin <= maxWidth) {
                //计算行宽
                totalLabelWidth += labelWidth;
                //记录行高
                maxLabelHeight = Math.max(maxLabelHeight, labelHeight);
                //记录最大行宽
                maxLineWidth = Math.max(maxLineWidth, totalLabelWidth + labelTotalMargin);
                if (i == getChildCount() - 1) {
                    //标签默认间距
                    int defaultLabelMargin = mLabelMargin;
                    if (mSpaceType == 2 && (getChildCount() - lineFeedPos) > 1) {
                        defaultLabelMargin = (maxWidth - totalLabelWidth) / (getChildCount() - lineFeedPos - 1);
                    }
                    for (int j = lineFeedPos; j < getChildCount(); j++) {
                        //记录横坐标
                        getChildAt(j).setTag(R.id.label_coordinate_x, coordinateX);
                        //记录纵坐标
                        getChildAt(j).setTag(R.id.label_coordinate_y, coordinateY);
                        //刷新下次的横坐标
                        coordinateX += getChildAt(j).getMeasuredWidth() + defaultLabelMargin;
                    }
                }
            } else {
                //标签默认间距
                int defaultLabelMargin = mLabelMargin;
                if (mSpaceType == 2 && (i - lineFeedPos) > 1) {
                    defaultLabelMargin = (maxWidth - totalLabelWidth) / (i - lineFeedPos - 1);
                }
                for (int j = lineFeedPos; j < i; j++) {
                    //记录横坐标
                    getChildAt(j).setTag(R.id.label_coordinate_x, coordinateX);
                    //记录纵坐标
                    getChildAt(j).setTag(R.id.label_coordinate_y, coordinateY);
                    //刷新下次的横坐标
                    coordinateX += getChildAt(j).getMeasuredWidth() + defaultLabelMargin;
                }
                if (++lineCount > mMaxLines && mMaxLines > 0) {
                    break;
                } else {
                    //下一行初始横坐标
                    coordinateX = getPaddingLeft();
                    //下一行初始纵坐标
                    coordinateY += (maxLabelHeight + mLineMargin);
                    //记录横坐标
                    getChildAt(i).setTag(R.id.label_coordinate_x, coordinateX);
                    //记录纵坐标
                    getChildAt(i).setTag(R.id.label_coordinate_y, coordinateY);
                    //换行的情况记录加上上一行内容高度和间距
                    layoutContentHeight += (maxLabelHeight + mLineMargin);
                    //下一行开始的初始行高，为下次记录做准备
                    maxLabelHeight = labelHeight;
                    //下一行开始的初始行宽，为下次记录做准备
                    totalLabelWidth = labelWidth;
                    //记录换行位置
                    lineFeedPos = i;
                }
            }
        }
        //统计最后的高度
        layoutContentHeight += maxLabelHeight;
        int widthSize = maxLineWidth + getPaddingLeft() + getPaddingRight();
        int heightSize = layoutContentHeight + getPaddingTop() + getPaddingBottom();
        setMeasuredDimension(resolveSizeAndState(widthSize, widthMeasureSpec, 0), resolveSizeAndState(heightSize, heightMeasureSpec, 0));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        for (int i = 0; i < getChildCount(); i++) {
            View label = getChildAt(i);
            Object tagX = label.getTag(R.id.label_coordinate_x);
            Object tagY = label.getTag(R.id.label_coordinate_y);
            if (tagX != null && tagY != null) {
                int left = (int) tagX;
                int top = (int) tagY;
                int right = left + label.getMeasuredWidth();
                int bottom = top + label.getMeasuredHeight();
                label.layout(left, top, right, bottom);
            }
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
     * 初始化自定义属性
     *
     * @param context
     * @param attrs
     */
    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.LabelsView);
        isTextBold = mTypedArray.getBoolean(R.styleable.LabelsView_labelTextIsBold, false);
        isForbidClick = mTypedArray.getBoolean(R.styleable.LabelsView_labelForbidClick, false);
        mSpaceType = mTypedArray.getInt(R.styleable.LabelsView_labelSpaceType, 1);
        mSelectType = mTypedArray.getInt(R.styleable.LabelsView_labelSelectType, 1);
        mMaxLines = mTypedArray.getInteger(R.styleable.LabelsView_labelMaxLines, -1);
        mTextSize = mTypedArray.getDimension(R.styleable.LabelsView_labelTextSize, sp2px(14));
        mTextColor = mTypedArray.getColor(R.styleable.LabelsView_labelTextColor, Color.BLACK);
        mTextGravity = mTypedArray.getInt(R.styleable.LabelsView_labelTextGravity, Gravity.CENTER);
        mLabelMargin = mTypedArray.getDimensionPixelOffset(R.styleable.LabelsView_labelLabelMargin, dp2px(5));
        mLineMargin = mTypedArray.getDimensionPixelOffset(R.styleable.LabelsView_labelLineMargin, dp2px(5));
        mLabelWidth = mTypedArray.getLayoutDimension(R.styleable.LabelsView_labelTextWidth, LayoutParams.WRAP_CONTENT);
        mLabelHeight = mTypedArray.getLayoutDimension(R.styleable.LabelsView_labelTextHeight, LayoutParams.WRAP_CONTENT);
        mLabelMinWidth = mTypedArray.getLayoutDimension(R.styleable.LabelsView_labelTextMinWidth, LayoutParams.WRAP_CONTENT);
        mLabelMinHeight = mTypedArray.getLayoutDimension(R.styleable.LabelsView_labelTextMinHeight, LayoutParams.WRAP_CONTENT);
        if (mTypedArray.hasValue(R.styleable.LabelsView_labelBackground)) {
            mLabelBg = mTypedArray.getDrawable(R.styleable.LabelsView_labelBackground);
        } else {
            mLabelBg = ContextCompat.getDrawable(context, R.drawable.default_label_bg);
        }
        if (mTypedArray.hasValue(R.styleable.LabelsView_labelTextPadding)) {
            mTextPaddingLeft = mTypedArray.getDimensionPixelOffset(R.styleable.LabelsView_labelTextPadding, 0);
            mTextPaddingTop = mTypedArray.getDimensionPixelOffset(R.styleable.LabelsView_labelTextPadding, 0);
            mTextPaddingRight = mTypedArray.getDimensionPixelOffset(R.styleable.LabelsView_labelTextPadding, 0);
            mTextPaddingBottom = mTypedArray.getDimensionPixelOffset(R.styleable.LabelsView_labelTextPadding, 0);
        } else {
            mTextPaddingLeft = mTypedArray.getDimensionPixelOffset(R.styleable.LabelsView_labelTextPaddingLeft, dp2px(10));
            mTextPaddingTop = mTypedArray.getDimensionPixelOffset(R.styleable.LabelsView_labelTextPaddingTop, dp2px(5));
            mTextPaddingRight = mTypedArray.getDimensionPixelOffset(R.styleable.LabelsView_labelTextPaddingRight, dp2px(10));
            mTextPaddingBottom = mTypedArray.getDimensionPixelOffset(R.styleable.LabelsView_labelTextPaddingBottom, dp2px(5));
        }
        mTypedArray.recycle();
    }

    /**
     * 设置标签的点击监听
     *
     * @param labelClickListener
     */
    public void setOnLabelClickListener(OnLabelClickListener labelClickListener) {
        mLabelClickListener = labelClickListener;
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
     * @param positions
     */
    public void setSelects(List<Integer> positions) {
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
                mLabelSelectChangeListener.onLabelSelectChange((TextView) label, label.getTag(KEY_DATA), isSelect, (int) label.getTag(KEY_POSITION));
            }
        }
    }

    /**
     * 添加标签
     * 设置给label的背景(Drawable)是一个Drawable对象的拷贝，因为如果所有的标签都共用一个Drawable对象，会引起背景错乱。
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
        label.setGravity(mTextGravity);
        label.setMinWidth(mLabelMinWidth);
        label.setMinHeight(mLabelMinHeight);
        label.getPaint().setFakeBoldText(isTextBold);
        label.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
        label.setText(provider.getLabelText(label, position, data));
        label.setBackground(mLabelBg.getConstantState().newDrawable());
        if (mLabelWidth == LayoutParams.WRAP_CONTENT && mLabelHeight == LayoutParams.WRAP_CONTENT) {
            label.setPadding(mTextPaddingLeft, mTextPaddingTop, mTextPaddingRight, mTextPaddingBottom);
        } else if (mLabelWidth == LayoutParams.WRAP_CONTENT) {
            label.setPadding(mTextPaddingLeft, 0, mTextPaddingRight, 0);
        } else if (mLabelHeight == LayoutParams.WRAP_CONTENT) {
            label.setPadding(0, mTextPaddingTop, 0, mTextPaddingBottom);
        }
        addView(label, mLabelWidth, mLabelHeight);
        label.setOnClickListener(this);
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
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spVal, getResources().getDisplayMetrics());
    }

    /**
     * dp转px
     */
    private int dp2px(float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpVal, getResources().getDisplayMetrics());
    }
}
