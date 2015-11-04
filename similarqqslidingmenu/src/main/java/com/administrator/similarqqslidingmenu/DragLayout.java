package com.administrator.similarqqslidingmenu;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.support.v4.widget.ViewDragHelper.Callback;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.nineoldandroids.view.ViewHelper;

public class DragLayout extends FrameLayout {

    protected static final String TAG = DragLayout.class.getSimpleName();
    private ViewDragHelper mDragHelper;
    // 提供信息, 接收事件
    private Callback mCallback = new Callback() {
        /**
         * 当触摸到某个子View的时候, 这个方法会被调用, 返回值决定了是否捕获(处理)这个子View child: 触摸到的子View
         * pointerId: 多点触摸的id, 我们用不到
         */
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            Log.i(TAG, "tryCaptureView--------");
            // if(child == mMainContent) {
            // return true;
            // }else if(child == mLeftContent) {
            // return false;
            // }
            return child == mMainContent || child == mLeftContent;
        }

        // 修正View水平方向的位置, 返回值决定了View在水平方向上的位置, 这个方法调用的时候, View的位置还没有改变
        // child: 要处理的子View
        // left:ViewDragHelper建议的View的left值
        // dx: 子View原先的left值和建议的left值之间的差值
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            Log.i(TAG, "clampViewPositionHorizontal---- left: " + left + " dx: " + dx + " oldLeft"
                    + child.getLeft());
            if (child == mMainContent) {
                left = fixLeft(left);
            }
            return left;
        }

        private int fixLeft(int left) {
            if (left < 0) {
                left = 0;
            } else if (left > mDragRange) {
                left = mDragRange;
            }
            return left;
        };

        // 这个方法不重要, 但是必须有, 不影响水平方向的拖拽范围
        // 影响松手之后的动画执行时间, 如果页面中有ListView等可以滑动的控件时, 这个方法必须返回大于0的值
        // 否则就无法拖动了
        public int getViewHorizontalDragRange(View child) {
            return mDragRange;
        };

        // 当View的位置改变之后, 这个方法会被调用
        // changedView: 位置改变的View
        // left: View的left 值
        // dx: View当前的left值和位置改变之前的left值的差值
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            if (changedView == mLeftContent) {
                mLeftContent.layout(0, 0, mWidth, mHeight);
                int newLeft = mMainContent.getLeft() + dx;
                newLeft = fixLeft(newLeft);
                mMainContent.layout(newLeft, 0, newLeft + mWidth, mHeight);
            }
            Log.i(TAG, "onViewPositionChanged---- left: " + left + " dx: " + dx + " oldLeft:"
                    + changedView.getLeft());
            dispatchDragState(mMainContent.getLeft());
            invalidate(); // 解决2.3.3 上无法拖动的bug
        };

        // 当View被释放的时候调用
        // xvel: 松手时x轴方向上的速度, 如果向着x轴正方向松手, 值为正, 向着x轴负方向松手, 值为负
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            Log.i(TAG, "onViewReleased---- xvel: " + xvel);
            if (xvel == 0.0f && mMainContent.getLeft() < mDragRange * 0.5f) {
                close();
            } else if (xvel < 0) {
                close();
            } else {
                open();
            }
        };
    };
    private ViewGroup mLeftContent;
    private ViewGroup mMainContent;
    private int mWidth;
    private int mHeight;
    private int mDragRange;

    public enum State {
        CLOSE, OPEN, DRAGGING
    }

    private State mState = State.CLOSE;

    public interface OnDragStateChangeListener {
        void onClose();

        void onOpen();

        void onDragging(float percent);
    }

    private OnDragStateChangeListener mOnDragStateChangeListener;

    public State getState() {
        return mState;
    }

    public void setState(State state) {
        mState = state;
    }

    public OnDragStateChangeListener getOnDragStateChangeListener() {
        return mOnDragStateChangeListener;
    }

    public void setOnDragStateChangeListener(OnDragStateChangeListener onDragStateChangeListener) {
        mOnDragStateChangeListener = onDragStateChangeListener;
    }

    // 在代码里 new 出来
    public DragLayout(Context context) {
        this(context, null);
        // init();
    }

    protected void dispatchDragState(int left) {
        float percent = left * 1.0f / mDragRange;
        Log.i(TAG, "dispatchDragState---- percent: " + percent);
        State preState = mState;
        mState = updateState(percent);
        if(mOnDragStateChangeListener != null) {
            mOnDragStateChangeListener.onDragging(percent);
            if (preState != mState) {
                if (mState == State.CLOSE) {
                    mOnDragStateChangeListener.onClose();
                } else if (mState == State.OPEN) {
                    mOnDragStateChangeListener.onOpen();
                }
            }
        }
        animViews(percent);
    }

    private State updateState(float percent) {
        if (percent == 0.0f) {
            return State.CLOSE;
        } else if (percent == 1.0f) {
            return State.OPEN;
        } else {
            return State.DRAGGING;
        }
    }

    private void animViews(float percent) {
        // 1.6.1. 缩放(主面板, 左面板)
        // 1.0f - 0.8f ---> 0.0f -1.0f
        // mMainContent.setScaleX(1.0f + (0.8f - 1.0f) * percent);
        // mMainContent.setScaleY(1.0f + (0.8f - 1.0f) * percent);
        ViewHelper.setScaleX(mMainContent, 1.0f + (0.8f - 1.0f) * percent);
        ViewHelper.setScaleY(mMainContent, 1.0f + (0.8f - 1.0f) * percent);
        // mLeftContent.setScaleX(0.5f + (1.0f - 0.5f) * percent);
        // mLeftContent.setScaleY(0.5f + (1.0f - 0.5f) * percent);
        ViewHelper.setScaleX(mLeftContent, 0.5f + (1.0f - 0.5f) * percent);
        ViewHelper.setScaleY(mLeftContent, 0.5f + (1.0f - 0.5f) * percent);
        // 1.6.2. 平移(左面板)
        // mLeftContent.setTranslationX(EvaluateUtil.evaluateFloat(percent,
        // -mWidth * 0.5f, 0));
        ViewHelper.setTranslationX(mLeftContent,
                EvaluateUtil.evaluateFloat(percent, -mWidth * 0.5f, 0));
        // 1.6.3. 透明度(左面板)
        // mLeftContent.setAlpha(EvaluateUtil.evaluateFloat(percent, 0.0f,
        // 1.0f));
        ViewHelper.setAlpha(mLeftContent, EvaluateUtil.evaluateFloat(percent, 0.0f, 1.0f));
        // 1.6.4. 亮度(背景)
        getBackground().setColorFilter(
                (Integer) EvaluateUtil.evaluateArgb(percent, Color.BLACK, Color.TRANSPARENT),
                Mode.SRC_OVER);
    }

    protected void open() {
        open(true);
    }

    public void open(boolean isSmooth) {
        int finalLeft = mDragRange;
        if (isSmooth) {
            // 平滑打开
            mDragHelper.smoothSlideViewTo(mMainContent, finalLeft, 0);
            invalidate();
        } else {
            mMainContent.layout(finalLeft, 0, finalLeft + mWidth, mHeight);
        }
    }

    protected void close() {
        close(true);
    }

    public void close(boolean isSmooth) {
        int finalLeft = 0;
        if (isSmooth) {
            // 平滑关闭
            // "触发"一个平滑动画, 计算了第一帧
            mDragHelper.smoothSlideViewTo(mMainContent, finalLeft, 0);
            invalidate();
        } else {
            mMainContent.layout(finalLeft, 0, finalLeft + mWidth, mHeight);
        }
    }

    // 在布局文件里配置
    public DragLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    // 这个构造方法外部不调用, 是让前面两个构造方法调用的
    public DragLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // forParent: 要监视的父View.
        // callback: 提供信息, 接收事件
        mDragHelper = ViewDragHelper.create(this, mCallback);
    }

    // 让ViewDragHelper决定是否拦截事件
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mDragHelper.shouldInterceptTouchEvent(ev);
    }

    // 让ViewDragHelper处理触摸事件
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        // 健壮性检查
        if (getChildCount() < 2) {
            throw new RuntimeException("You must have at least 2 child views");
        }
        if (!(getChildAt(0) instanceof ViewGroup) || !(getChildAt(1) instanceof ViewGroup)) {
            throw new IllegalArgumentException("Your child views must be ViewGroup");
        }
        mLeftContent = (ViewGroup) getChildAt(0);
        mMainContent = (ViewGroup) getChildAt(1);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // getMeasuredWidth();
    }

    // 在View的宽高改变后调用, 一定是在onMeasure方法之后调用的
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // 获取测量好的宽高
        mWidth = mMainContent.getMeasuredWidth();
        mHeight = mMainContent.getMeasuredHeight();
        mDragRange = (int) (mWidth * 0.6f);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

}
