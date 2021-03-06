package com.fanwe.library.switchbutton;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * Created by Administrator on 2017/7/5.
 */

public class SDSwitchButton extends FrameLayout implements ISDSwitchButton
{
    public SDSwitchButton(@NonNull Context context)
    {
        super(context);
        init(context, null);
    }

    public SDSwitchButton(@NonNull Context context, @Nullable AttributeSet attrs)
    {
        super(context, attrs);

        init(context, attrs);
    }

    public SDSwitchButton(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private static final String TAG = "SDSwitchButton";

    /**
     * 是否选中
     */
    private boolean mIsChecked;
    /**
     * 正常view
     */
    private View mViewNormal;
    /**
     * 选中view
     */
    private View mViewChecked;
    /**
     * 手柄view
     */
    private View mViewThumb;
    /**
     * 手柄view是否被自定义
     */
    private boolean mIsViewThumbCustom;

    private ViewDragHelper mDragHelper;
    private GestureDetector mGestureDetector;
    private boolean mIsNeedProcess;
    /**
     * 是否是透明度模式来显示隐藏view的
     */
    private boolean mIsAlphaMode = true;

    private SBAttrModel mAttrModel = new SBAttrModel();

    private boolean mIsDebug;

    private OnCheckedChangedCallback mOnCheckedChangedCallback;
    private OnViewPositionChangedCallback mOnViewPositionChangedCallback;

    private void init(Context context, AttributeSet attrs)
    {
        mAttrModel.parse(context, attrs);
        addDefaultViews();
        initGestureDetector();
        initViewDragHelper();
    }

    public void setDebug(boolean debug)
    {
        mIsDebug = debug;
    }

    private void addDefaultViews()
    {
        ImageView imageNormal = new ImageView(getContext());
        imageNormal.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imageNormal.setImageResource(mAttrModel.getImageNormalResId());
        addView(imageNormal, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mViewNormal = imageNormal;

        ImageView imageChecked = new ImageView(getContext());
        imageChecked.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imageChecked.setBackgroundResource(mAttrModel.getImageCheckedResId());
        addView(imageChecked, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mViewChecked = imageChecked;

        ImageView imageThumb = new SDThumbImageView(getContext());
        imageThumb.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imageThumb.setBackgroundResource(mAttrModel.getImageThumbResId());
        LayoutParams pThumb = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        pThumb.gravity = Gravity.CENTER_VERTICAL;
        pThumb.leftMargin = mAttrModel.getMarginLeft();
        pThumb.topMargin = mAttrModel.getMarginTop();
        pThumb.rightMargin = mAttrModel.getMarginRight();
        pThumb.bottomMargin = mAttrModel.getMarginBottom();
        addView(imageThumb, pThumb);
        mViewThumb = imageThumb;

        setChecked(mAttrModel.isChecked(), false, false);
    }


    private void initGestureDetector()
    {
        mGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener()
        {
            @Override
            public boolean onSingleTapUp(MotionEvent e)
            {
                toggleChecked(mAttrModel.isNeedToggleAnim(), true);
                return super.onSingleTapUp(e);
            }
        });
    }

    private void initViewDragHelper()
    {
        mDragHelper = ViewDragHelper.create(this, 1.0f, new ViewDragHelper.Callback()
        {
            @Override
            public boolean tryCaptureView(View child, int pointerId)
            {
                return child == mViewThumb;
            }

            @Override
            public int clampViewPositionVertical(View child, int top, int dy)
            {
                return child.getTop();
            }

            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx)
            {
                return Math.min(Math.max(left, getLeftNormal()), getLeftChecked());
            }

            @Override
            public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy)
            {
                super.onViewPositionChanged(changedView, left, top, dx, dy);

                final float percent = getScrollPercent();
                if (mIsAlphaMode)
                {
                    ViewCompat.setAlpha(mViewChecked, percent);
                    ViewCompat.setAlpha(mViewNormal, 1 - percent);
                }

                if (mOnViewPositionChangedCallback != null)
                {
                    mOnViewPositionChangedCallback.onViewPositionChanged(SDSwitchButton.this);
                }
            }

            @Override
            public void onViewCaptured(View capturedChild, int activePointerId)
            {
                super.onViewCaptured(capturedChild, activePointerId);
                if (mIsDebug)
                {
                    Log.i(TAG, "onViewCaptured");
                }
            }

            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel)
            {
                super.onViewReleased(releasedChild, xvel, yvel);
                if (mIsDebug)
                {
                    Log.i(TAG, "onViewReleased");
                }
                if (releasedChild.getLeft() >= ((getLeftNormal() + getLeftChecked()) / 2))
                {
                    setChecked(true, true, true);
                } else
                {
                    setChecked(false, true, true);
                }
            }
        });
    }

    /**
     * 根据状态改变view
     *
     * @param anim
     */
    private void updateViewByState(boolean anim)
    {
        if (mIsDebug)
        {
            Log.i(TAG, "updateViewByState");
        }
        if (mIsChecked)
        {
            showCheckedView(true);
            showNormalView(false);

            final int left = getLeftChecked();
            if (mViewThumb.getLeft() != left)
            {
                if (anim)
                {
                    mDragHelper.smoothSlideViewTo(mViewThumb, left, mViewThumb.getTop());
                } else
                {
                    mViewThumb.layout(left, mViewThumb.getTop(), left + mViewThumb.getMeasuredWidth(), mViewThumb.getBottom());
                }
                invalidate();
            }
        } else
        {
            showCheckedView(false);
            showNormalView(true);

            final int left = getLeftNormal();
            if (mViewThumb.getLeft() != left)
            {
                if (anim)
                {
                    mDragHelper.smoothSlideViewTo(mViewThumb, left, mViewThumb.getTop());
                } else
                {
                    mViewThumb.layout(left, mViewThumb.getTop(), left + mViewThumb.getMeasuredWidth(), mViewThumb.getBottom());
                }
                invalidate();
            }
        }
        mViewThumb.setSelected(mIsChecked);

        if (mOnViewPositionChangedCallback != null)
        {
            mOnViewPositionChangedCallback.onViewPositionChanged(SDSwitchButton.this);
        }
    }

    private void showCheckedView(boolean show)
    {
        if (mIsAlphaMode)
        {
            ViewCompat.setAlpha(mViewChecked, show ? 1.0f : 0f);
        } else
        {
            mViewChecked.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        }
    }

    private void showNormalView(boolean show)
    {
        if (mIsAlphaMode)
        {
            ViewCompat.setAlpha(mViewNormal, show ? 1.0f : 0f);
        } else
        {
            mViewNormal.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        }
    }

    /**
     * 返回normal状态下手柄view的left值
     *
     * @return
     */
    private int getLeftNormal()
    {
        return getParamsThumbView().leftMargin;
    }

    /**
     * 返回checked状态下手柄view的left值
     *
     * @return
     */
    private int getLeftChecked()
    {
        return getMeasuredWidth() - mViewThumb.getMeasuredWidth() - getParamsThumbView().rightMargin;
    }

    /**
     * 返回手柄view可以移动的宽度大小
     *
     * @return
     */
    private int getAvailableWidth()
    {
        return getLeftChecked() - getLeftNormal();
    }

    /**
     * 返回手柄view滚动的距离
     *
     * @return
     */
    private int getScrollDistance()
    {
        return mViewThumb.getLeft() - getLeftNormal();
    }

    /**
     * 返回手柄view布局参数
     */
    private LayoutParams getParamsThumbView()
    {
        return (LayoutParams) mViewThumb.getLayoutParams();
    }

    @Override
    protected void onFinishInflate()
    {
        super.onFinishInflate();

        View normal = findViewById(R.id.lib_sb_view_normal);
        if (normal != null)
        {
            removeView(normal);
            setViewNormal(normal);
        }

        View checked = findViewById(R.id.lib_sb_view_checked);
        if (checked != null)
        {
            removeView(checked);
            setViewChecked(checked);
        }

        View thumb = findViewById(R.id.lib_sb_view_thumb);
        if (thumb != null)
        {
            removeView(thumb);
            setViewThumb(thumb);
        }
    }

    /**
     * 设置正常view
     *
     * @param viewNormal
     */
    private void setViewNormal(View viewNormal)
    {
        if (replaceOldView(mViewNormal, viewNormal))
        {
            mViewNormal = viewNormal;
        }
    }

    /**
     * 设置选中view
     *
     * @param viewChecked
     */
    private void setViewChecked(View viewChecked)
    {
        if (replaceOldView(mViewChecked, viewChecked))
        {
            mViewChecked = viewChecked;
        }
    }

    /**
     * 设置手柄view
     *
     * @param viewThumb
     */
    private void setViewThumb(View viewThumb)
    {
        if (replaceOldView(mViewThumb, viewThumb))
        {
            mViewThumb = viewThumb;
            mIsViewThumbCustom = true;
        }
    }

    private boolean replaceOldView(View viewOld, View viewNew)
    {
        if (viewNew != null && viewOld != viewNew)
        {
            int index = indexOfChild(viewOld);
            ViewGroup.LayoutParams params = viewOld.getLayoutParams();
            removeView(viewOld);

            if (viewNew.getLayoutParams() != null)
            {
                params = viewNew.getLayoutParams();
            }

            addView(viewNew, index, params);
            return true;
        } else
        {
            return false;
        }
    }

    @Override
    public void computeScroll()
    {
        if (mDragHelper.continueSettling(true))
        {
            invalidate();
        }
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

        switch (ev.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                mTouchHelper.setNeedConsume(true);
                if (getParent() != null)
                {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                break;
        }
        return mTouchHelper.isNeedConsume();
    }

    private boolean checkMoveParams()
    {
        return mTouchHelper.getDegreeX() < 45;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {

        mGestureDetector.onTouchEvent(event);
        mTouchHelper.processTouchEvent(event);
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                //触发ViewDragHelper的尝试捕捉
                mDragHelper.processTouchEvent(event);
                break;
            case MotionEvent.ACTION_MOVE:
                if (mIsNeedProcess)
                {
                    mDragHelper.processTouchEvent(event);
                } else
                {
                    if (checkMoveParams())
                    {
                        setNeedProcess(true, event);
                    } else
                    {
                        if (getParent() != null)
                        {
                            getParent().requestDisallowInterceptTouchEvent(false);
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (getParent() != null)
                {
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
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

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom)
    {
        super.onLayout(changed, left, top, right, bottom);

        updateThumbViewParams();
        updateViewByState(false);
    }

    /**
     * 更新手柄view的布局参数
     */
    private void updateThumbViewParams()
    {
        boolean needUpdate = false;
        LayoutParams params = getParamsThumbView();

        //----------margins----------
        if (!mIsViewThumbCustom)
        {
            if (mAttrModel.getMarginLeft() < 0)
            {
                mAttrModel.setMarginLeft(getMeasuredHeight() / 15);
            }
            if (mAttrModel.getMarginTop() < 0)
            {
                mAttrModel.setMarginTop(getMeasuredHeight() / 15);
            }
            if (mAttrModel.getMarginRight() < 0)
            {
                mAttrModel.setMarginRight(getMeasuredHeight() / 15);
            }
            if (mAttrModel.getMarginBottom() < 0)
            {
                mAttrModel.setMarginBottom(getMeasuredHeight() / 15);
            }
        } else
        {
            mAttrModel.setMarginLeft(params.leftMargin);
            mAttrModel.setMarginTop(params.topMargin);
            mAttrModel.setMarginRight(params.rightMargin);
            mAttrModel.setMarginBottom(params.bottomMargin);
        }

        if (params.leftMargin != mAttrModel.getMarginLeft())
        {
            params.leftMargin = mAttrModel.getMarginLeft();
            needUpdate = true;
        }
        if (params.topMargin != mAttrModel.getMarginTop())
        {
            params.topMargin = mAttrModel.getMarginTop();
            needUpdate = true;
        }
        if (params.rightMargin != mAttrModel.getMarginRight())
        {
            params.rightMargin = mAttrModel.getMarginRight();
            needUpdate = true;
        }
        if (params.bottomMargin != mAttrModel.getMarginBottom())
        {
            params.bottomMargin = mAttrModel.getMarginBottom();
            needUpdate = true;
        }

        if (needUpdate)
        {
            mViewThumb.setLayoutParams(params);
        }
    }

    //----------ISDSwitchButton implements start----------

    @Override
    public boolean isChecked()
    {
        return mIsChecked;
    }

    @Override
    public void setChecked(boolean checked, boolean anim, boolean notifyCallback)
    {
        if (mIsChecked != checked)
        {
            mIsChecked = checked;
            if (notifyCallback)
            {
                if (mOnCheckedChangedCallback != null)
                {
                    mOnCheckedChangedCallback.onCheckedChanged(mIsChecked, this);
                }
            }
        }

        updateViewByState(anim);
    }

    @Override
    public void toggleChecked(boolean anim, boolean notifyCallback)
    {
        setChecked(!mIsChecked, anim, notifyCallback);
    }

    @Override
    public void setOnCheckedChangedCallback(OnCheckedChangedCallback onCheckedChangedCallback)
    {
        mOnCheckedChangedCallback = onCheckedChangedCallback;
    }

    @Override
    public void setOnViewPositionChangedCallback(OnViewPositionChangedCallback onViewPositionChangedCallback)
    {
        mOnViewPositionChangedCallback = onViewPositionChangedCallback;
    }

    @Override
    public void setAlphaMode(boolean alphaMode)
    {
        mIsAlphaMode = alphaMode;
    }

    @Override
    public float getScrollPercent()
    {
        return getScrollDistance() / (float) getAvailableWidth();
    }

    @Override
    public View getViewNormal()
    {
        return mViewNormal;
    }

    @Override
    public View getViewChecked()
    {
        return mViewChecked;
    }

    @Override
    public View getViewThumb()
    {
        return mViewThumb;
    }

    //----------ISDSwitchButton implements end----------

}
