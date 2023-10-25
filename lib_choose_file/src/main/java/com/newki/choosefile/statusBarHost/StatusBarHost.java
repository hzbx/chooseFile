package com.newki.choosefile.statusBarHost;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

/**
 * 宿主替换布局的方式管理状态栏与内容的布局
 */
public class StatusBarHost {

    private StatusBarHost() {
    }

    public static StatusBarHostLayout inject(Activity activity) {
        Window window = activity.getWindow();
        ViewGroup contentLayout = window.getDecorView().findViewById(Window.ID_ANDROID_CONTENT);
        if (contentLayout.getChildCount() > 0) {
            View contentView = contentLayout.getChildAt(0);
            //如果当前是宿主的包装类，直接强转
            if (contentView instanceof StatusBarHostLayout) {
                return (StatusBarHostLayout) contentView;
            }
        }
        //如果不是我们封装一个宿主包装类
        return new StatusBarHostLayout(activity);
    }
}
