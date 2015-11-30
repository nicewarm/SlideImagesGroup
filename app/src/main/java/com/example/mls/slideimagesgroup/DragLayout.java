package com.example.mls.slideimagesgroup;

import android.content.Context;
import android.graphics.Color;
import android.os.Debug;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by MLS on 15/11/27.
 */
public class DragLayout extends ViewGroup {


    /* 拖拽工具类 */
    private final ViewDragHelper mDragHelper;

    private Context context;


    public DragLayout(Context context) {
        this(context, null);
    }

    public DragLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mDragHelper = ViewDragHelper.create(this, 1f, new DragHelperCallback());
        this.context = context;

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChildren(widthMeasureSpec, heightMeasureSpec);

        int maxWidth = MeasureSpec.getSize(widthMeasureSpec);
        int maxHeight = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(
                resolveSizeAndState(maxWidth, widthMeasureSpec, 0),
                resolveSizeAndState(maxHeight, heightMeasureSpec, 0));
    }

    private int mVerDragRange;
    private int mHorDranRange;
    private int maxSpace = 80;
    private int minSpace = -80;
    private int space = minSpace;

    public void setData(List<String> images) {
        removeAllViews();
        for (int i = 0; i < 4; i++) {
            ImageView imageView = new ImageView(context);
            ViewGroup.LayoutParams layoutParams = new LayoutParams(150, 150);
            imageView.setBackgroundColor(Color.parseColor("#0096db"));
            imageView.setLayoutParams(layoutParams);
            final int finalI = i;
            imageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, finalI + "", Toast.LENGTH_SHORT).show();
                }
            });
            addView(imageView);
        }
        requestLayout();
        invalidate();
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int wid = 0;
        int viewHeight = 0;

        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            viewHeight = view.getMeasuredHeight();
            view.layout(wid, 0, wid + view.getMeasuredWidth(), view.getMeasuredHeight());
            wid += view.getWidth() + space;
        }
        mHorDranRange = r;
        mVerDragRange = b - viewHeight;

    }


    /* touch事件的拦截与处理都交给mDraghelper来处理 */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean shouldIntercept = mDragHelper.shouldInterceptTouchEvent(ev);
        int action = ev.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            // action_down时就让mDragHelper开始工作，否则有时候导致异常 他大爷的
            mDragHelper.processTouchEvent(ev);
        }
        return shouldIntercept;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // 统一交给mDragHelper处理，由DragHelperCallback实现拖动效果
        mDragHelper.processTouchEvent(e); // 该行代码可能会抛异常，正式发布时请将这行代码加上try catch
        return true;
    }

    private int getCurrentViewRight(View view) {
        int index = indexOfChild(view);
        int right = view.getWidth() * index + index * maxSpace;
        return right;
    }

    private int getCurrentViewLeft(View view) {
        int index = indexOfChild(view);
        int left = view.getWidth() * index + index * minSpace;
        return left;
    }

    /**
     * 这是拖拽效果的主要逻辑
     */
    private class DragHelperCallback extends ViewDragHelper.Callback {

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            int minLeft = getCurrentViewLeft(changedView);
            int maxRight = getCurrentViewRight(changedView);
            int step = maxRight - minLeft;
            int halfStep = step / 2;
            int byOriginValue = left - (minLeft + maxRight) / 2;
            double spaceValue = byOriginValue * 1.0 / halfStep * maxSpace;
            space = (int) spaceValue;
            if (space > maxSpace) {
                space = maxSpace;
            }
            if (space < minSpace) {
                space = minSpace;
            }
            requestLayout();
        }

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            // 两个子View都需要跟踪，返回true
            return true;
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            return mVerDragRange;
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return mHorDranRange;
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
        }


        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            final int newLeft = Math.min(Math.max(left, getCurrentViewLeft(child)), getCurrentViewRight(child));
            return newLeft;
        }
    }
}
