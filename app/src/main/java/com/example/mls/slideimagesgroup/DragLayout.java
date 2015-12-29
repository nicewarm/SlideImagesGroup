package com.example.mls.slideimagesgroup;

import android.content.Context;
import android.graphics.Color;
import android.os.Debug;
import android.support.v4.view.ViewCompat;
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

    /*最右边的View*/
    private View mRightView;

    private int kAvatarWidth;

    public static final int kAvatarWidthDp = 40;
    public static final int spaceDpValue = 26;
    private int mVerDragRange;
    private int mHorDranRange;
    private int maxSpace;
    private int minSpace;
    private int space;

    private OnSlideToLeftAndRightListener listener;


    public interface OnSlideToLeftAndRightListener {
        public void onSlide(double offset);
    }

    private Context context;


    public DragLayout(Context context) {
        this(context, null);
    }

    public DragLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        kAvatarWidth = dip2px(context, kAvatarWidthDp);
        maxSpace = dip2px(context, spaceDpValue);
        minSpace = -maxSpace;
        space = minSpace;
        mDragHelper = ViewDragHelper.create(this, 1f, new DragHelperCallback());
        this.context = context;

    }

    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChildren(widthMeasureSpec, heightMeasureSpec);

        int maxWidth = MeasureSpec.getSize(widthMeasureSpec);
        int maxHeight = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(maxWidth, kAvatarWidth);
    }

    public void setData(List<String> images) {
        removeAllViews();
        initChildViews(images);
        requestLayout();
        invalidate();
    }

    /*初始化子view*/
    private void initChildViews(List<String> images) {
        for (int i = 0; i < 4; i++) {
            ImageView imageView = new ImageView(context);
            imageView.setBackgroundColor(randomColor());
            ViewGroup.LayoutParams layoutParams = new LayoutParams(150, 150);
            imageView.setLayoutParams(layoutParams);
            final int finalI = i;
            imageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mDragOffset == -1) {
                        smoothSlideTo(1);
                        mDragOffsetLast = 1;
                    }
                    if (mDragOffset == 1) {
                        Toast.makeText(context, finalI + "", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            addView(imageView);
        }
        if (getChildCount() > 0) {
            mRightView = getChildAt(getChildCount() - 1);
        }
    }

    /*随机颜色*/
    private int randomColor() {
        int colorValue = (int) (Math.random() * (16777216 - 1) + 1) * -1;
        String hex = Integer.toHexString(colorValue);
        return Color.parseColor("#" + hex);
    }


    /*初始化布局子view*/
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
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
            // 只有move事件，让dragHelper自己处理，其余事件不拦截，否则onClick就失效了。
            if (action == MotionEvent.ACTION_MOVE) {
                mDragHelper.processTouchEvent(ev);
            }
            return shouldIntercept;
        } else {
            mDragHelper.cancel();
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        // 只要有事件拦截，就全部交给mDragHelper处理，由DragHelperCallback实现拖动效果
        mDragHelper.processTouchEvent(ev);
        return true;
    }


    // 判断当前点击时间是否在View中
    private boolean isViewHit(View view, int x, int y) {
        int[] viewLocation = new int[2];
        view.getLocationOnScreen(viewLocation);
        int[] parentLocation = new int[2];
        this.getLocationOnScreen(parentLocation);
        int screenX = parentLocation[0] + x;
        int screenY = parentLocation[1] + y;
        return screenX >= viewLocation[0] && screenX < viewLocation[0] + view.getWidth() &&
                screenY >= viewLocation[1] && screenY < viewLocation[1] + view.getHeight();
    }

    //    探测子View的右边界
    private int getCurrentViewRight(View view) {
        int index = indexOfChild(view);
        int right = view.getWidth() * index + index * maxSpace;
        return right;
    }

    //    探测子View的左边界
    private int getCurrentViewLeft(View view) {
        int index = indexOfChild(view);
        int left = view.getWidth() * index + index * minSpace;
        return left;
    }


    //    让动画持续
    @Override
    public void computeScroll() {
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    //    指定滑动到某个位置
    private boolean smoothSlideTo(float slideOffset) {
        int right = slideOffset == 0f ? getCurrentViewLeft(mRightView) : getCurrentViewRight(mRightView);
        if (mDragHelper.smoothSlideViewTo(mRightView, right, 0)) {
            ViewCompat.postInvalidateOnAnimation(this);
            return true;
        }
        return false;
    }

    private double mDragOffset = -1;
    private double mDragOffsetLast = -1;

    /**
     * 这是拖拽效果的主要逻辑
     */
    private class DragHelperCallback extends ViewDragHelper.Callback {

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            //    按比例计算滑动的位移，在maxSpace和minSpace之间
            int minLeft = getCurrentViewLeft(changedView);
            int maxRight = getCurrentViewRight(changedView);
            int step = maxRight - minLeft;
            int halfStep = step / 2;
            int byOriginValue = left - (minLeft + maxRight) / 2;
            double spaceValue = byOriginValue * 1.0 / halfStep * maxSpace;
            mDragOffset = byOriginValue * 1.0 / halfStep;
            space = (int) spaceValue;
            if (space > maxSpace) {
                space = maxSpace;
            }
            if (space < minSpace) {
                space = minSpace;
            }

            if (listener != null) {
                listener.onSlide(mDragOffset);
            }

            requestLayout();
        }

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
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
            //   在释放滑动事件后，继续移动view，达到惯性的效果
            int right = 0;
            if (mDragOffset - mDragOffsetLast > 0) {
                right = getCurrentViewRight(releasedChild);
                mDragHelper.settleCapturedViewAt(right, 0);
                invalidate();
                mDragOffsetLast = 1;
            } else {
                right = getCurrentViewLeft(releasedChild);
                mDragHelper.settleCapturedViewAt(right, 0);
                invalidate();
                mDragOffsetLast = -1;
            }
        }

        //  判断滑动的边界，不能出借
        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            final int newLeft = Math.min(Math.max(left, getCurrentViewLeft(child)), getCurrentViewRight(child));
            return newLeft;
        }
    }
}
