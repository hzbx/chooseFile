
package com.newki.choosefile.statusBarHost;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

/**
 * 自定义状态栏的View，用于StatusBarHostLayout中使用
 */
class StatusView extends View {

    private int mBarSize;

    public StatusView(Context context) {
        this(context, null, 0);
    }

    public StatusView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StatusView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);


        StatusBarHostUtils.getStatusBarHeight(this, new HeightValueCallback() {
            @Override
            public void onHeight(int height) {

                mBarSize = height;
            }
        });

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), mBarSize);
        } else {
            setMeasuredDimension(0, 0);
        }

    }

    //获取到当前的状态栏高度
    public int getStatusBarHeight() {
        return mBarSize;
    }
}