package com.newki.choosefile.statusBarHost;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

/**
 * 状态栏工具类,只用于内部使用
 */
public class StatusBarHostUtils {

    // =======================  StatusBar begin ↓ =========================

    /**
     * 5.0以上设置沉浸式状态
     */
    public static void immersiveStatusBar(Activity activity) {
        //方式一
        //false 表示沉浸，true表示不沉浸
        WindowCompat.setDecorFitsSystemWindows(activity.getWindow(), false);

        //方式二：添加Flag，两种方式都可以，都是5.0以上使用
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            Window window = activity.getWindow();
//            View decorView = window.getDecorView();
//            decorView.setSystemUiVisibility(decorView.getSystemUiVisibility()
//                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
//            window.setStatusBarColor(Color.TRANSPARENT);
//        }
    }

    /**
     * 设置当前页面的状态栏颜色，使用宿主方案一般不用这个修改颜色，只是用于沉浸式之后修改状态栏颜色为透明
     */
    public static void setStatusBarColor(Activity activity, int statusBarColor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(statusBarColor);
        }
    }

    /**
     * 6.0版本及以上可以设置黑色的状态栏文本
     *
     * @param activity
     * @param dark     是否需要黑色文本
     */
    public static void setStatusBarDarkFont(Activity activity, boolean dark) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = activity.getWindow();
            View decorView = window.getDecorView();
            if (dark) {
                decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            } else {
                decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }

    }

    /**
     * 老的方法获取状态栏高度
     */
    private static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * 新方法获取状态栏高度
     */
    public static void getStatusBarHeight(Activity activity, HeightValueCallback callback) {
        getStatusBarHeight(activity.findViewById(android.R.id.content), callback);
    }

    /**
     * 新方法获取状态栏高度
     */
    public static void getStatusBarHeight(View view, HeightValueCallback callback) {

        boolean attachedToWindow = view.isAttachedToWindow();

        if (attachedToWindow) {

            WindowInsetsCompat windowInsets = ViewCompat.getRootWindowInsets(view);
            assert windowInsets != null;
            int top = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            int bottom = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars()).bottom;
            int height = Math.abs(bottom - top);
            if (height > 0) {
                callback.onHeight(height);
            } else {
                callback.onHeight(getStatusBarHeight(view.getContext()));
            }

        } else {

            view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View v) {

                    WindowInsetsCompat windowInsets = ViewCompat.getRootWindowInsets(v);
                    assert windowInsets != null;
                    int top = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
                    int bottom = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars()).bottom;
                    int height = Math.abs(bottom - top);
                    if (height > 0) {
                        callback.onHeight(height);
                    } else {
                        callback.onHeight(getStatusBarHeight(view.getContext()));
                    }
                }

                @Override
                public void onViewDetachedFromWindow(View v) {
                }
            });
        }
    }

    // =======================  NavigationBar begin ↓ =========================

    /**
     * 5.0以上-设置NavigationBar底部导航栏的沉浸式
     */
    public static void immersiveNavigationBar(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            View decorView = window.getDecorView();
            decorView.setSystemUiVisibility(decorView.getSystemUiVisibility()
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

            window.setNavigationBarColor(Color.TRANSPARENT);
        }
    }

    /**
     * 设置底部导航栏的颜色
     */
    public static void setNavigationBarColor(Activity activity, int navigationBarColor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setNavigationBarColor(navigationBarColor);
        }
    }

    /**
     * 底部导航栏的Icon颜色白色和灰色切换，高版本系统才会生效
     */
    public static void setNavigationBarDrak(Activity activity, boolean isDarkFont) {
        WindowInsetsControllerCompat controller = ViewCompat.getWindowInsetsController(activity.findViewById(android.R.id.content));
        if (controller != null) {
            if (!isDarkFont) {
                controller.setAppearanceLightNavigationBars(false);
            } else {
                controller.setAppearanceLightNavigationBars(true);
            }
        }
    }

    /**
     * 老的方法获取导航栏的高度
     */
    private static int getNavigationBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * 获取底部导航栏的高度
     */
    public static void getNavigationBarHeight(Activity activity, HeightValueCallback callback) {
        getNavigationBarHeight(activity.findViewById(android.R.id.content), callback);
    }

    /**
     * 获取底部导航栏的高度
     */
    public static void getNavigationBarHeight(View view, HeightValueCallback callback) {

        boolean attachedToWindow = view.isAttachedToWindow();

        if (attachedToWindow) {

            WindowInsetsCompat windowInsets = ViewCompat.getRootWindowInsets(view);
            assert windowInsets != null;
            int top = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars()).top;
            int bottom = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
            int height = Math.abs(bottom - top);
            if (height > 0) {
                callback.onHeight(height);
            } else {
                callback.onHeight(getNavigationBarHeight(view.getContext()));
            }

        } else {

            view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View v) {

                    WindowInsetsCompat windowInsets = ViewCompat.getRootWindowInsets(v);
                    assert windowInsets != null;
                    int top = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars()).top;
                    int bottom = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
                    int height = Math.abs(bottom - top);
                    if (height > 0) {
                        callback.onHeight(height);
                    } else {
                        callback.onHeight(getNavigationBarHeight(view.getContext()));
                    }
                }

                @Override
                public void onViewDetachedFromWindow(View v) {
                }
            });
        }
    }

    // =======================  NavigationBar StatusBar Hide Show begin ↓ =========================

    /**
     * 显示隐藏底部导航栏（注意不是沉浸式效果）
     */
    public static void showHideNavigationBar(Activity activity, boolean isShow) {

        View decorView = activity.findViewById(android.R.id.content);
        WindowInsetsControllerCompat controller = ViewCompat.getWindowInsetsController(decorView);

        if (controller != null) {
            if (isShow) {
                controller.show(WindowInsetsCompat.Type.navigationBars());
                controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_TOUCH);
            } else {
                controller.hide(WindowInsetsCompat.Type.navigationBars());
                controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        }
    }

    /**
     * 显示隐藏顶部的状态栏（注意不是沉浸式效果）
     */
    public static void showHideStatusBar(Activity activity, boolean isShow) {

        View decorView = activity.findViewById(android.R.id.content);
        WindowInsetsControllerCompat controller = ViewCompat.getWindowInsetsController(decorView);

        if (controller != null) {
            if (isShow) {
                controller.show(WindowInsetsCompat.Type.statusBars());
            } else {
                controller.hide(WindowInsetsCompat.Type.statusBars());
            }
        }

    }

    /**
     * 当前是否显示了底部导航栏
     */
    public static void hasNavigationBars(Activity activity, BooleanValueCallback callback) {

        View decorView = activity.findViewById(android.R.id.content);
        boolean attachedToWindow = decorView.isAttachedToWindow();

        if (attachedToWindow) {

            WindowInsetsCompat windowInsets = ViewCompat.getRootWindowInsets(decorView);

            if (windowInsets != null) {

                boolean hasNavigationBar = windowInsets.isVisible(WindowInsetsCompat.Type.navigationBars()) &&
                        windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom > 0;

                callback.onBoolean(hasNavigationBar);
            }

        } else {

            decorView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View v) {

                    WindowInsetsCompat windowInsets = ViewCompat.getRootWindowInsets(v);

                    if (windowInsets != null) {

                        boolean hasNavigationBar = windowInsets.isVisible(WindowInsetsCompat.Type.navigationBars()) &&
                                windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom > 0;

                        callback.onBoolean(hasNavigationBar);
                    }
                }

                @Override
                public void onViewDetachedFromWindow(View v) {
                }
            });
        }
    }

}