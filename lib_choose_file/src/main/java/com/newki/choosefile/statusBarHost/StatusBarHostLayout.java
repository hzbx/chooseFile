package com.newki.choosefile.statusBarHost;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.core.content.ContextCompat;


/**
 * 宿主的布局
 */
@SuppressLint("ViewConstructor")
public class StatusBarHostLayout extends LinearLayout {

    private Activity mActivity;
    private StatusView mStatusView;
    private NavigationView mNavigationView;
    private FrameLayout mContentLayout;

    StatusBarHostLayout(Activity activity) {
        super(activity);

        this.mActivity = activity;

        //加载自定义的宿主布局
        if (mStatusView == null && mContentLayout == null) {
            setOrientation(LinearLayout.VERTICAL);

            mStatusView = new StatusView(mActivity);
            mStatusView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            addView(mStatusView);

            mContentLayout = new FrameLayout(mActivity);
            mContentLayout.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f));
            addView(mContentLayout);

            mNavigationView = new NavigationView(mActivity);
            mNavigationView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            addView(mNavigationView);
        }

        //替换宿主的contentView为外界设置的View
        replaceContentView();

        //设置原生的状态栏沉浸式，使用自定义的状态栏布局
        StatusBarHostUtils.immersiveStatusBar(mActivity);
        StatusBarHostUtils.setStatusBarColor(mActivity, Color.TRANSPARENT);

        //设置原生导航栏为沉浸式，使用自定义导航栏
        StatusBarHostUtils.immersiveNavigationBar(mActivity);
        StatusBarHostUtils.setNavigationBarColor(mActivity, Color.TRANSPARENT);
    }

    @Override
    public final WindowInsets onApplyWindowInsets(WindowInsets insets) {
        //这里只判断底部导航栏是否需要隐藏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            StatusBarHostUtils.hasNavigationBars(mActivity, new BooleanValueCallback() {
                @Override
                public void onBoolean(boolean success) {
                    mNavigationView.setVisibility(success ? VISIBLE : GONE);
                }
            });
        }

        return insets;
    }

    private void replaceContentView() {
        Window window = mActivity.getWindow();
        ViewGroup contentLayout = window.getDecorView().findViewById(Window.ID_ANDROID_CONTENT);
        if (contentLayout.getChildCount() > 0) {
            //先找到DecorView的容器移除掉已经设置的ContentView
            View contentView = contentLayout.getChildAt(0);
            contentLayout.removeView(contentView);
            ViewGroup.LayoutParams contentParams = contentView.getLayoutParams();

            //外部设置的ContentView添加到宿主中来
            mContentLayout.addView(contentView, contentParams.width, contentParams.height);
        }
        //再把整个宿主添加到Activity对应的DecorView中去
        contentLayout.addView(this, -1, -1);
    }


    /**
     * 设置状态栏文本颜色为黑色
     */
    public StatusBarHostLayout setStatusBarBlackText() {
        StatusBarHostUtils.setStatusBarDarkFont(mActivity, true);
        return this;
    }

    /**
     * 设置状态栏文本颜色为白色
     */
    public StatusBarHostLayout setStatusBarWhiteText() {
        StatusBarHostUtils.setStatusBarDarkFont(mActivity, false);
        return this;
    }

    /**
     * 设置导航栏图片颜色为黑色
     */
    public StatusBarHostLayout setNavigatiopnBarIconBlack() {
        StatusBarHostUtils.setNavigationBarDrak(mActivity, true);
        return this;
    }

    /**
     * 设置导航栏图片颜色为白色
     */
    public StatusBarHostLayout setNavigatiopnBarIconWhite() {
        StatusBarHostUtils.setNavigationBarDrak(mActivity, false);
        return this;
    }

    /**
     * 设置自定义状态栏布局的背景颜色
     */
    public StatusBarHostLayout setStatusBarBackground(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mStatusView.setBackgroundColor(color);
        } else {
            //6.0以下不能白色状态栏
            if (color == Color.WHITE) {
                color = Color.parseColor("#B0B0B0");
            }

            mStatusView.setBackgroundColor(color);
        }

        return this;
    }

    /**
     * 设置自定义状态栏布局的背景图片
     */
    public StatusBarHostLayout setStatusBarBackground(Drawable drawable) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mStatusView.setBackground(drawable);
        } else {
            mStatusView.setBackgroundDrawable(drawable);
        }
        return this;
    }

    /**
     * 设置自定义状态栏布局的透明度
     */
    public StatusBarHostLayout setStatusBarBackgroundAlpha(int alpha) {
        Drawable background = mStatusView.getBackground();
        if (background != null) {
            background.mutate().setAlpha(alpha);
        }
        return this;
    }

    /**
     * 设置自定义状态栏布局的背景颜色
     */
    public StatusBarHostLayout setNavigationBarBackground(int color) {
        if (mNavigationView != null)
            mNavigationView.setBackgroundColor(color);
        return this;
    }

    /**
     * 设置自定义状态栏布局的背景图片
     */
    public StatusBarHostLayout setNavigationBarBackground(Drawable drawable) {
        if (mNavigationView != null)
            mNavigationView.setBackground(drawable);
        return this;
    }

    /**
     * 设置自定义状态栏布局的透明度
     */
    public StatusBarHostLayout setNavigationBarBackgroundAlpha(int alpha) {
        if (mNavigationView != null) {
            Drawable background = mNavigationView.getBackground();
            if (background != null) {
                background.mutate().setAlpha(alpha);
            }
        }
        return this;
    }

    /**
     * 给指定的布局适配状态栏高度，设置paddingTop
     */
    public StatusBarHostLayout setViewFitsStatusBarView(View view) {

        //方式一 ： 添加PaddingTop的方式
//        //增加高度
//        ViewGroup.LayoutParams lp = view.getLayoutParams();
//        lp.height += mStatusView.getStatusBarHeight();
//
//        //设置PaddingTop
//        view.setPadding(view.getPaddingLeft(),
//                view.getPaddingTop() + mStatusView.getStatusBarHeight(),
//                view.getPaddingRight(),
//                view.getPaddingBottom());
//
//        return this;

        //方式二 ：设置MaginTop的方式
        if (view.getLayoutParams() instanceof MarginLayoutParams) {

            //已经添加过了不要再次设置
            if (view.getTag() != null && view.getTag().equals("fitStatusBar")) {
                return this;
            }

            MarginLayoutParams layoutParams = (MarginLayoutParams) view.getLayoutParams();
            int marginTop = layoutParams.topMargin;
            int setMarginTop = marginTop + mStatusView.getStatusBarHeight();
            view.setTag("fitStatusBar");
            layoutParams.topMargin = setMarginTop;
            view.requestLayout();
        }


        //方式三 ：使用LinearLayout包装
//        ViewParent fitParent = view.getParent();
//        if (fitParent != null) {
//
//            if (((fitParent instanceof LinearLayout) &&
//                    ((ViewGroup) fitParent).getTag() != null &&
//                    ((ViewGroup) fitParent).getTag().equals("fitLayout"))) {
//                //已经添加过了不要再次设置
//                return this;
//            }
//
//            //给当前布局包装一个适应布局
//            ViewGroup fitGroup = (ViewGroup) fitParent;
//            fitGroup.removeView(view);
//
//            LinearLayout fitLayout = new LinearLayout(mActivity);
//            fitLayout.setOrientation(LinearLayout.VERTICAL);
//            fitLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//            fitLayout.setTag("fitLayout");
//
//            //先加一个状态栏高度的布局
//            StatusView statusView = new StatusView(mActivity);
//            fitLayout.addView(statusView);
//
//            ViewGroup.LayoutParams fitViewParams = view.getLayoutParams();
//            YYLogUtils.w("原始的高度：" + fitViewParams.height);
//            fitLayout.addView(view);
//
//            fitGroup.addView(fitLayout);
//        }

        return this;
    }


    /**
     * 设置自定义状态栏的沉浸式
     */
    public StatusBarHostLayout setStatusBarImmersive(boolean needImmersive, int statusBarColor) {
        layoutimmersive(needImmersive, statusBarColor);
        return this;
    }

    //具体的沉浸式逻辑
    private void layoutimmersive(boolean needImmersive, int statusBarColor) {

        if (needImmersive) {
            mStatusView.setVisibility(GONE);
        } else {
            mStatusView.setVisibility(VISIBLE);
            mStatusView.setBackgroundColor(ContextCompat.getColor(mActivity, statusBarColor));
        }

    }

}
