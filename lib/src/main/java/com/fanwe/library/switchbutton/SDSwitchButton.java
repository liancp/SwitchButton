package com.fanwe.library.switchbutton;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by Administrator on 2017/7/5.
 */

public class SDSwitchButton extends FrameLayout
{
    public SDSwitchButton(@NonNull Context context)
    {
        super(context);
        init();
    }

    public SDSwitchButton(@NonNull Context context, @Nullable AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    public SDSwitchButton(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init();
    }

    private static final String TAG = "SDSwitchButton";

    private boolean mIsChecked;

    private View mNormalView;
    private View mCheckedView;
    private View mHandleView;

    private ViewDragHelper mDragHelper;
    private boolean mIsNeedProcess;

    private boolean mIsDebug;

    private OnCheckedChangedCallback mOnCheckedChangedCallback;

    private void init()
    {
        if (getBackground() == null)
        {
            setBackgroundResource(R.drawable.layer_bg);
        }
        addDefaultViews();
        initViewDragHelper();
    }

    private void addDefaultViews()
    {
        mNormalView = new View(getContext());
        addView(mNormalView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        mCheckedView = new View(getContext());
        addView(mCheckedView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        LayoutParams paramsHandle = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        mHandleView = new View(getContext());
        addView(mHandleView, paramsHandle);
    }

    public void setNormalView(View normalView)
    {
        if (replaceOldView(mNormalView, normalView))
        {
            mNormalView = normalView;
        }
    }

    public void setCheckedView(View checkedView)
    {
        if (replaceOldView(mCheckedView, checkedView))
        {
            mCheckedView = checkedView;
        }
    }

    public void setHandleView(View handleView)
    {
        if (replaceOldView(mHandleView, handleView))
        {
            mHandleView = handleView;
        }
    }

    private boolean replaceOldView(View oldView, View newView)
    {
        if (newView != null && oldView != newView)
        {
            int index = indexOfChild(oldView);
            removeView(oldView);
            addView(newView, index);
            return true;
        } else
        {
            return false;
        }
    }

    private void initViewDragHelper()
    {
        mDragHelper = ViewDragHelper.create(this, 1.0f, new ViewDragHelper.Callback()
        {
            @Override
            public boolean tryCaptureView(View child, int pointerId)
            {
                return child == mHandleView;
            }

            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx)
            {
                return Math.min(Math.max(left, getLeftNormal()), getLeftChecked());
            }

            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel)
            {
                super.onViewReleased(releasedChild, xvel, yvel);

                if (getLeft() >= getAvailableWidth() / 2)
                {
                    setChecked(true, true);
                } else
                {
                    setChecked(false, true);
                }
            }
        });
    }

    public void setChecked(boolean checked, boolean anim)
    {
        setChecked(checked, anim, true);
    }

    public void setChecked(boolean checked, boolean anim, boolean notifyEvent)
    {
        mIsChecked = checked;
        changeViewByCheckedState(anim);
        if (notifyEvent)
        {
            if (mOnCheckedChangedCallback != null)
            {
                mOnCheckedChangedCallback.onCheckedChanged(mIsChecked, this);
            }
        }
    }

    private void changeViewByCheckedState(boolean anim)
    {
        if (mIsChecked)
        {
            showNormalView(false);
            showCheckedView(true);
            if (anim)
            {
                mDragHelper.smoothSlideViewTo(mHandleView, getLeftChecked(), mHandleView.getTop());
            } else
            {

            }
        } else
        {
            showNormalView(true);
            showCheckedView(false);
            if (anim)
            {
                mDragHelper.smoothSlideViewTo(mHandleView, getLeftNormal(), mHandleView.getTop());
            } else
            {

            }
        }
    }

    /**
     * 返回normal状态下的left值
     *
     * @return
     */
    private int getLeftNormal()
    {
        return getLeft() + getPaddingLeft();
    }

    /**
     * 返回checked状态下的left值
     *
     * @return
     */
    private int getLeftChecked()
    {
        return getWidth() - mHandleView.getWidth() - getPaddingRight();
    }

    private int getAvailableWidth()
    {
        return getLeftChecked() - getLeftNormal();
    }

    private void showNormalView(boolean show)
    {
        if (show)
        {
            mNormalView.setVisibility(View.VISIBLE);
        } else
        {
            mNormalView.setVisibility(View.INVISIBLE);
        }
    }

    private void showCheckedView(boolean show)
    {
        if (show)
        {
            mCheckedView.setVisibility(View.VISIBLE);
        } else
        {
            mCheckedView.setVisibility(View.INVISIBLE);
        }
    }

    public void setOnCheckedChangedCallback(OnCheckedChangedCallback onCheckedChangedCallback)
    {
        mOnCheckedChangedCallback = onCheckedChangedCallback;
    }

    @Override
    protected void onFinishInflate()
    {
        super.onFinishInflate();
    }

    private SDTouchEventHelper mTouchHelper = new SDTouchEventHelper();

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev)
    {
        if (mIsDebug)
        {
            if (ev.getAction() == MotionEvent.ACTION_DOWN)
            {
                Log.i(TAG, "onInterceptTouchEvent:" + ev.getAction() + "--------------------");
            } else
            {
                Log.i(TAG, "onInterceptTouchEvent:" + ev.getAction());
            }
        }
        if (mTouchHelper.isNeedConsume())
        {
            if (mIsDebug)
            {
                Log.e(TAG, "onInterceptTouchEvent Intercept success because isNeedConsume is true with action----------" + ev.getAction());
            }
            return true;
        }

        mTouchHelper.processTouchEvent(ev);
        switch (ev.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                mTouchHelper.setNeedConsume(false);
                mDragHelper.processTouchEvent(ev);
                break;
            case MotionEvent.ACTION_MOVE:
                if (isViewCaptured() && checkMoveParams())
                {
                    mTouchHelper.setNeedConsume(true);
                }
                break;
            default:
                mDragHelper.processTouchEvent(ev);
                break;
        }
        return mTouchHelper.isNeedConsume();
    }

    private boolean isViewCaptured()
    {
        return mDragHelper.getViewDragState() == ViewDragHelper.STATE_DRAGGING;
    }

    private boolean checkMoveParams()
    {
        return mTouchHelper.getDegreeX() < 30;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {

        mTouchHelper.processTouchEvent(event);
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                if (!isViewCaptured())
                {
                    //触发ViewDragHelper的尝试捕捉
                    mDragHelper.processTouchEvent(event);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mIsNeedProcess)
                {
                    if (isViewCaptured())
                    {
                        mDragHelper.processTouchEvent(event);
                    }
                } else
                {
                    if (isViewCaptured() && checkMoveParams())
                    {
                        setNeedProcess(true, event);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                setNeedProcess(false, event);
                mDragHelper.processTouchEvent(event);
                if (mIsDebug)
                {
                    if (mTouchHelper.isNeedConsume())
                    {
                        Log.e(TAG, "onTouchEvent Intercept released with action " + event.getAction());
                    }
                }
                mTouchHelper.setNeedConsume(false);
                break;
            default:
                mDragHelper.processTouchEvent(event);
                break;
        }

        boolean result = super.onTouchEvent(event) || mIsNeedProcess || event.getAction() == MotionEvent.ACTION_DOWN;
        return result;
    }

    private void setNeedProcess(boolean needProcess, MotionEvent event)
    {
        if (mIsNeedProcess != needProcess)
        {
            mIsNeedProcess = needProcess;
        }
    }

    public interface OnCheckedChangedCallback
    {
        void onCheckedChanged(boolean checked, SDSwitchButton view);
    }
}