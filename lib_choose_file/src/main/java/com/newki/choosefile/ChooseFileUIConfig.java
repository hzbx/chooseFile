package com.newki.choosefile;

import android.graphics.Color;

public class ChooseFileUIConfig {

    private int statusBarColor;   //状态栏颜色

    private int titleBarBgColor;  //标题栏的背景颜色
    private int titleBarBackRes;  //标题栏的返回按钮资源
    private int titleBarTitleColor; //标题栏的标题文字颜色
    private int titleBarTitleSize; //标题栏的标题文字大小(sp)

    private int navigationBarColor; //底部导航栏颜色

    private int fileNavBarTextColor; //文件导航栏的文本颜色
    private int fileNavBarTextSize; //文件导航栏的文本大小
    private int fileNavBarArrowIconRes; //文件导航栏的箭头图标资源

    private int fileNameTextColor;  //文件(夹)名称字体颜色
    private int fileNameTextSize;  //文件(夹)名称字体大小(sp)
    private int fileInfoTextColor;  //文件(夹)提示信息字体大小
    private int fileInfoTextSize;  //文件(夹)提示信息字体大小(sp)

    private ChooseFileUIConfig() {
    }

    private ChooseFileUIConfig(Builder builder) {
        this.statusBarColor = builder.statusBarColor;
        this.titleBarBgColor = builder.titleBarBgColor;
        this.titleBarBackRes = builder.titleBarBackRes;
        this.titleBarTitleColor = builder.titleBarTitleColor;
        this.titleBarTitleSize = builder.titleBarTitleSize;
        this.navigationBarColor = builder.navigationBarColor;
        this.fileNavBarTextColor = builder.fileNavBarTextColor;
        this.fileNavBarTextSize = builder.fileNavBarTextSize;
        this.fileNavBarArrowIconRes = builder.fileNavBarArrowIconRes;
        this.fileNameTextColor = builder.fileNameTextColor;
        this.fileNameTextSize = builder.fileNameTextSize;
        this.fileInfoTextColor = builder.fileInfoTextColor;
        this.fileInfoTextSize = builder.fileInfoTextSize;
    }

    public int getStatusBarColor() {
        return statusBarColor;
    }

    public void setStatusBarColor(int statusBarColor) {
        this.statusBarColor = statusBarColor;
    }

    public int getTitleBarBgColor() {
        return titleBarBgColor;
    }

    public void setTitleBarBgColor(int titleBarBgColor) {
        this.titleBarBgColor = titleBarBgColor;
    }

    public int getTitleBarBackRes() {
        return titleBarBackRes;
    }

    public void setTitleBarBackRes(int titleBarBackRes) {
        this.titleBarBackRes = titleBarBackRes;
    }

    public int getTitleBarTitleColor() {
        return titleBarTitleColor;
    }

    public void setTitleBarTitleColor(int titleBarTitleColor) {
        this.titleBarTitleColor = titleBarTitleColor;
    }

    public int getTitleBarTitleSize() {
        return titleBarTitleSize;
    }

    public void setTitleBarTitleSize(int titleBarTitleSize) {
        this.titleBarTitleSize = titleBarTitleSize;
    }

    public int getNavigationBarColor() {
        return navigationBarColor;
    }

    public void setNavigationBarColor(int navigationBarColor) {
        this.navigationBarColor = navigationBarColor;
    }

    public int getFileNavBarTextColor() {
        return fileNavBarTextColor;
    }

    public void setFileNavBarTextColor(int fileNavBarTextColor) {
        this.fileNavBarTextColor = fileNavBarTextColor;
    }

    public int getFileNavBarTextSize() {
        return fileNavBarTextSize;
    }

    public void setFileNavBarTextSize(int fileNavBarTextSize) {
        this.fileNavBarTextSize = fileNavBarTextSize;
    }

    public int getFileNavBarArrowIconRes() {
        return fileNavBarArrowIconRes;
    }

    public void setFileNavBarArrowIconRes(int fileNavBarArrowIconRes) {
        this.fileNavBarArrowIconRes = fileNavBarArrowIconRes;
    }

    public int getFileNameTextColor() {
        return fileNameTextColor;
    }

    public void setFileNameTextColor(int fileNameTextColor) {
        this.fileNameTextColor = fileNameTextColor;
    }

    public int getFileNameTextSize() {
        return fileNameTextSize;
    }

    public void setFileNameTextSize(int fileNameTextSize) {
        this.fileNameTextSize = fileNameTextSize;
    }

    public int getFileInfoTextColor() {
        return fileInfoTextColor;
    }

    public void setFileInfoTextColor(int fileInfoTextColor) {
        this.fileInfoTextColor = fileInfoTextColor;
    }

    public int getFileInfoTextSize() {
        return fileInfoTextSize;
    }

    public void setFileInfoTextSize(int fileInfoTextSize) {
        this.fileInfoTextSize = fileInfoTextSize;
    }

    public static class Builder {

        private int statusBarColor = Color.parseColor("#0689FB");   //状态栏颜色

        private int titleBarBgColor = Color.parseColor("#0689FB");  //标题栏的背景颜色
        private int titleBarBackRes = R.drawable.cf_back;  //标题栏的返回按钮资源
        private int titleBarTitleColor = Color.parseColor("#FFFFFF"); //标题栏的标题文字颜色
        private int titleBarTitleSize = 20; //标题栏的标题文字大小(sp)

        private int navigationBarColor = Color.parseColor("#F7F7FB"); //底部导航栏颜色

        private int fileNavBarTextColor = Color.parseColor("#333333"); //文件导航栏的文本颜色
        private int fileNavBarTextSize = 15; //文件导航栏的文本大小
        private int fileNavBarArrowIconRes = R.drawable.cf_next; //文件导航栏的箭头图标资源

        private int fileNameTextColor = Color.BLACK;  //文件(夹)名称字体颜色
        private int fileNameTextSize = 16;  //文件(夹)名称字体大小(sp)
        private int fileInfoTextColor = Color.parseColor("#A9A9A9");  //文件(夹)提示信息字体大小
        private int fileInfoTextSize = 14;  //文件(夹)提示信息字体大小(sp)

        public Builder() {
        }

        public Builder statusBarColor(int statusBarColor) {
            this.statusBarColor = statusBarColor;
            return this;
        }

        public Builder titleBarBgColor(int titleBarBgColor) {
            this.titleBarBgColor = titleBarBgColor;
            return this;
        }

        public Builder titleBarBackRes(int titleBarBackRes) {
            this.titleBarBackRes = titleBarBackRes;
            return this;
        }

        public Builder titleBarTitleColor(int titleBarTitleColor) {
            this.titleBarTitleColor = titleBarTitleColor;
            return this;
        }

        public Builder titleBarTitleSize(int titleBarTitleSize) {
            this.titleBarTitleSize = titleBarTitleSize;
            return this;
        }

        public Builder navigationBarColor(int navigationBarColor) {
            this.navigationBarColor = navigationBarColor;
            return this;
        }

        public Builder fileNavBarTextColor(int fileNavBarTextColor) {
            this.fileNavBarTextColor = fileNavBarTextColor;
            return this;
        }

        public Builder fileNavBarTextSize(int fileNavBarTextSize) {
            this.fileNavBarTextSize = fileNavBarTextSize;
            return this;
        }

        public Builder fileNavBarArrowIconRes(int fileNavBarArrowIconRes) {
            this.fileNavBarArrowIconRes = fileNavBarArrowIconRes;
            return this;
        }

        public Builder fileNameTextColor(int fileNameTextColor) {
            this.fileNameTextColor = fileNameTextColor;
            return this;
        }

        public Builder fileNameTextSize(int fileNameTextSize) {
            this.fileNameTextSize = fileNameTextSize;
            return this;
        }

        public Builder fileInfoTextColor(int fileInfoTextColor) {
            this.fileInfoTextColor = fileInfoTextColor;
            return this;
        }

        public Builder fileInfoTextSize(int fileInfoTextSize) {
            this.fileInfoTextSize = fileInfoTextSize;
            return this;
        }

        public ChooseFileUIConfig build() {
            return new ChooseFileUIConfig(this);
        }

    }

}
